/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By	        DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	12/19/23	D Li				CAP-43217		Create app alive page to test health of app and databases
 */
package com.rrd.c1ux.api.controllers;

import com.wallace.atwinxs.admin.ao.SiteAssembler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("AppAliveController")
@RequestMapping(RouteConstants.API_ALIVE)
@Tag(name = "alive")
public class AppAliveController {
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get app alive status", description = "Gets the health of the app/databases")
    public ResponseEntity<Map<String, String>> getHealth() {

    	Map<String, String> returnMap = new HashMap<>();
    	String serverName = System.getProperty("XS.SERVER.TOKEN") + System.getProperty("XS.SERVER.ID");
    	
    	// Do a DB call, set status to non-2xx if db call fails
    	try {
			SiteAssembler siteAssembler = new SiteAssembler(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN, null);
			siteAssembler.getSite(AtWinXSConstant.DEFAULT_SITE_ID);
		} catch (Exception e) {
			returnMap.put("error", e.getMessage());
			return new ResponseEntity<>(returnMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	
    	returnMap.put("serverName", serverName);
    	return new ResponseEntity<>(returnMap, HttpStatus.OK);
    }
    
}