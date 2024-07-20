/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  09/01/22    S Ramachandran  CAP-35358   Get URL for components to load for standard style sheet
 *  01/19/23    Sumit Kumar		CAP-37862	get the list of widgets   
 */

package com.rrd.c1ux.api.models.addtocart.landing;


import java.util.List;

import com.rrd.c1ux.api.controllers.RouteConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LandingResponse {

	private String status = RouteConstants.REST_RESPONSE_FAIL;
	private String landingHTML; 
	private List<String> assignedWidgets;//CAP-37862 : change according new widget list requirements
}

