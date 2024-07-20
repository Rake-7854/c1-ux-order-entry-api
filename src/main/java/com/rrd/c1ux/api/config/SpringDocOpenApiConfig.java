package com.rrd.c1ux.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configures the Spring OpenAPI documentation.  See: https://springdoc.org/ for more.
 */
@Configuration
public class SpringDocOpenApiConfig {

    @Bean
    public OpenAPI connectOneOrderEntryOpenAPI() {

        String version = getClass().getPackage().getImplementationVersion();
        
        return new OpenAPI()
                .info(new Info().title("ConnectOne OrderEntry API")
                .description("Provides ConnectOne OrderEntry APIs")
                .version(version))
                .externalDocs(new ExternalDocumentation()
                .description("ConnectOne OrderEntry API - Readme")
                .url("https://bitbucket.org/rrdonnelley-bb/c1-ux-order-entry-api/src/master/README.md"));
    }
}
