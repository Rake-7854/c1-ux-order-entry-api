/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		Jira#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  11/9/2022	T Harmon									Added handler to add new paths for file static resources
 *  07/24/2023  E Anderson      CAP-42229                   JavaScript resource changes for CustDocs.
 *  12/04/2023  C Porter        CAP-45576                   Update property names  
 */

package com.rrd.c1ux.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rrd.c1ux.api.config.CSPPageResourceTransformer;
import com.rrd.c1ux.api.filter.CSPHeaderResolver;

@Configuration
public class StaticResourceHandler implements WebMvcConfigurer {

	@Value("${uri.customerfiles.matcher}")
	private String uriCustomerFiles;
	@Value("${uri.customerfiles.path}")
	private String uriCustomerFilesPath;
	
	@Value("${uri.stylesheets.matcher}")
	private String uriStyleSheets;
	@Value("${uri.stylesheets.path}")
	private String uriStyleSheetsPath;
	
	@Value("${uri.javascript.files.matcher}")
	private String uriJavaScriptFiles;
	@Value("${uri.javascript.files.path}")
	private String uriJavaScriptFilesPath;
	
	@Value("${angular.resource.location}")
	private String angularResourceLocation;
	
	@Autowired
	private CSPHeaderResolver cspHeaderResolver;
	
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	//stylesheets
    	registry
        .addResourceHandler(uriStyleSheets) 
        .addResourceLocations(uriStyleSheetsPath);
    	
    	//customerfiles
    	registry
        .addResourceHandler(uriCustomerFiles) 
        .addResourceLocations(uriCustomerFilesPath);
    	
    	//javascript files
    	registry
    	.addResourceHandler(uriJavaScriptFiles)
    	.addResourceLocations(uriJavaScriptFilesPath);

    	//The index page.  
    	//Need to tell spring to look for it here instead of one of the default static locations of the following (in order of highest to lowest priority):
    	//src/main/resources/META-INF/resources/index.html
    	//src/main/resources/resources/index.html
    	//src/main/resources/static/index.html
    	//src/main/resources/public/index.html
		registry
		.addResourceHandler("/index.html**")
		.addResourceLocations(angularResourceLocation)
		.resourceChain(false)
		.addTransformer(new CSPPageResourceTransformer(cspHeaderResolver));

    	//Angular artifacts
    	registry
        .addResourceHandler("/**")
        .addResourceLocations(angularResourceLocation);
    }
    
	@Override 
	public void addViewControllers(ViewControllerRegistry registry) 
	{
		registry.addRedirectViewController("/", "/index.html");
	}
}
