/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  11/29/2023  C Porter        CAP-45570                   Enable Jasypt for spring configurations   
 *  20/07/2024                  CAP-TEST                    Only For Testing Purpose Rakesh Added Here
 */

package com.rrd.c1ux.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableEncryptableProperties
@ImportResource("classpath:beans.xml")
public class ApiApplication {
    //CAP-TEST
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
		System.err.println("Hiii DEMO 1st Committ...");
	}

}
