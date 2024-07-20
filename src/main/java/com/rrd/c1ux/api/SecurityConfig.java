/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  CAP-35537   T Harmon                                    Changes for SAML
 *              T Harmon                                    Modified code to allow both User login and SAML
 *  04/05/2023  C Porter        CAP-39674                   Intermittent Session Timeout fix
 *  05/12/2023  C Porter        CAP-39738                   Update Spring Boot and Tomcat versions 
 *  06/22/2023  C Porter        CAP-41584                   Address server 500 issue when user session times out.  
 *  07/18/2023  C Porter        CAP-39073                   Address Invicti security issue for X-XSS-Protection header.
 *  07/24/2023  E Anderson      CAP-42229                   JavaScript resource changes for CustDocs.   
 *  09/21/2023  C Porter                                    All for per-environment configuration of static HTTP repsonse headers
 *  09/29/2023  C Porter        CAP-44263                   Update Content Security Policy for Storefront
 *  10/18/2023  C Porter        CAP-44260                   Allow for custom Content Security Policies by site
 */

package com.rrd.c1ux.api;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.rrd.c1ux.api.config.properties.CorsProperties;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.filter.CSPHeaderResolver;
import com.rrd.c1ux.api.filter.PropertyHeaderWriter;
import com.rrd.c1ux.api.services.security.HeaderWriterFilterPostProcessor;
import com.rrd.c1ux.api.services.session.C1UXSamlAuthenticationSuccessHandler;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.util.PropertyUtilService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // needs to match the spring.security entry in the application.yml file
    private static final String SAML2_AUTHENTICATE_PINGFED = "/saml2/authenticate/pingfed";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    public static final String DEFAULT_ROLE = "ROLE_USER";
    
    protected static final String[] PERMIT_ALL_MATCHERS = {
        RouteConstants.OAUTH_LOGOUT,
        RouteConstants.LOGOUT,
        RouteConstants.OAUTH_LOGIN,
        RouteConstants.LOGIN,
        RouteConstants.HEALTH,
        RouteConstants.VERSION,
        RouteConstants.SSO_LOGIN_SAML2,
        RouteConstants.API_ALIVE,
        "/index.html",
        "/invalidSession.html",
        "/favicon.ico",
        "/style/**",
        "/customerfiles/**",
        "/js/**"
    };

    @Bean
    CSPHeaderResolver cspHaderResolver(PropertyUtilService propUtilService, CPSessionReader cpSessionReader) {
    	return new CSPHeaderResolver(cpSessionReader, propUtilService);
    }
    
    @Bean
    PropertyHeaderWriter contentSecurityPolicyPropertyHeaderWriter(CSPHeaderResolver headerResolver) {
		return new PropertyHeaderWriter(headerResolver);
	}

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, Environment env, CorsProperties corsProperties,
                                 C1UXSamlAuthenticationSuccessHandler samlSuccessHandler, StaticHeadersProperties staticHeadersProperties,
                                 PropertyHeaderWriter contentSecurityPolicyHeaderWriter)
            throws Exception {
      
        var portMapper = portMapper();
        var cache = new HttpSessionRequestCache();
        cache.setPortResolver(portResolver(portMapper));
        http.setSharedObject(HttpSessionRequestCache.class, cache);
      
        http = http
            .authorizeHttpRequests()
            .antMatchers(PERMIT_ALL_MATCHERS)  // CAP-35537
            .permitAll()
            .anyRequest()
            .authenticated()
            .and().csrf().disable();
  
		http.headers().frameOptions().disable()
				.httpStrictTransportSecurity().includeSubDomains(true).preload(true)
				.and().addObjectPostProcessor(new HeaderWriterFilterPostProcessor());

		// add service that writes Content-Security-Policy header from system properties
		http.headers().addHeaderWriter(contentSecurityPolicyHeaderWriter);

        List<Header> headers = staticHeadersProperties.asHeaders();
        if (!headers.isEmpty() ) {
            http.headers().addHeaderWriter(new StaticHeadersWriter(headers));
        }
        
        CorsConfiguration config = corsProperties.getCors();
        if (!CollectionUtils.isEmpty(config.getAllowedOrigins())) {

             http.cors().and();
        }
    
        if (isSamlRegistration(env)) {

            // set up for Identify Provider login (OAuth/OIDC)
            // see: https://www.yenlo.com/blogs/wso2is-spring-application-saml2/
            http.saml2Login().successHandler(samlSuccessHandler)
                .and().exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()));

        }
        
        return http.build();

    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {

      return web -> {
        
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowedHttpMethods(List.of(DELETE.name(), GET.name(), HEAD.name(), PATCH.name(), POST.name(), PUT.name()));
        web.httpFirewall(firewall);

      };

    }

    /*
     * handler for the StrictHttpFirewall throws a RequestRejectedException
     */
    @Bean
    HttpStatusRequestRejectedHandler requestRejectedHandler() {
      return new HttpStatusRequestRejectedHandler();
    }

    /*
     * PortMapper maps one port to another port. The default configuration 
     * is to map 80 -> 443, and 8080 -> 8443. This does not work for apps
     * if the port should not be remapped.  This is the case for C1UX 
     * running on port 8080.
     * 
     * If the application ever runs on a port other than 80 or 8080,
     * then the portMapper bean and all related code can be removed.
     */
    @Bean
    PortMapper portMapper() {
      var portMapper = new PortMapperImpl();
      portMapper.setPortMappings(Map.of("8080", "8080"));
      return portMapper;
    }

    /*
     * PortResolver has a default implementation of PortMapper that
     * needs to be overridden. It will check the scheme of the
     * request (http or https), and remap the port based on convention.
     * Thus, a request for https://localhost:8080/app would be remapped
     * to https://localhost:8443/app with the default configuration.
     */
    @Bean
    PortResolver portResolver(PortMapper portMapper) {
      var portResolver = new PortResolverImpl();
      portResolver.setPortMapper(portMapper);
      return portResolver;
    }

    /*
     * Create a LoginUrlAuthenticationEntryPoint that uses the customized
     * PortMapper and PortResolver.  Otherwise the default configuration
     * for LoginUrlAuthenticationEntryPoint will attempt to 
     * remap https/8080 to https/8443.
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
      var portMapper = portMapper();
      var entryPoint = new LoginUrlAuthenticationEntryPoint(SAML2_AUTHENTICATE_PINGFED);
      entryPoint.setPortMapper(portMapper);
      entryPoint.setPortResolver(portResolver(portMapper));
      return entryPoint;
    }

    @Bean
    CorsFilter corsFilter(CorsProperties corsProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = corsProperties.getCors();

        if (!CollectionUtils.isEmpty(config.getAllowedOrigins())) {
            LOGGER.info("Registering CORS filter");
            LOGGER.info("CORS Allowed Origins: {}", (config.getAllowedOrigins() != null ? config.getAllowedOrigins() : new String[] {""}));
            LOGGER.info("CORS Allowed Methods: {}", (config.getAllowedMethods() != null ? config.getAllowedMethods() : new String[] {""}));
            LOGGER.info("CORS Allowed Headers: {}", (config.getAllowedHeaders() != null ? config.getAllowedHeaders() : new String[] {""}));
            source.registerCorsConfiguration("/**", config);
        }

        return new CorsFilter(source);
    }

    public static boolean isSamlRegistration(Environment env) {

        // CAP-35537
        String samlIdpEntity = env.getProperty(
            "spring.security.saml2.relyingparty.registration.pingfed.assertingparty.entity-id");

        return StringUtils.isNotBlank(samlIdpEntity);
    }

    public static boolean isBasicAuth(Environment env) {

        String basicAuthUsername = getBasicAuthUserName(env);

        return StringUtils.isNotBlank(basicAuthUsername);
    }

    public static String getBasicAuthUserName(Environment env) {

        return env.getProperty("spring.security.user.name");
    }

    public static String getBasicAuthPassword(Environment env) {

        return env.getProperty("spring.security.user.password");
    }
}
