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
 */

package com.rrd.c1ux.api.models.styles;

import com.rrd.c1ux.api.controllers.RouteConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardStyleIncludeResponse {

	private String status = RouteConstants.REST_RESPONSE_FAIL;
	private String coStandardStyleUrl; 
}
