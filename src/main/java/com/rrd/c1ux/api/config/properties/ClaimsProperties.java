/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		 Modified By		Jira#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	2022.08.16	 T Harmon		 CAP-35537					 Initial creation
 *  2022.09.16   E Anderson      CAP-35362                   Add account.
 *  10/25/22	A Boomker		CAP-36153					Add entry point
 */


package com.rrd.c1ux.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "connect-one-api.claims")
public class ClaimsProperties {
    
    private String accountClaimUri;
    private String cpSessionId;
    private String firstName;
    private String lastName;
    private String email;
    private String account; //CAP-35362
    private String entryPoint; 
}
