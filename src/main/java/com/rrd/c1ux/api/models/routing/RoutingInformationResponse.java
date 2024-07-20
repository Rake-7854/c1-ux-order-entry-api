/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  08/16/23    Satishkumar A      	CAP-42745       C1UX API - Routing Information For Justification Section on Review Order Page
 */
package com.rrd.c1ux.api.models.routing;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="RoutingInformationResponse", description = "Response Class for Routing Information - Routing Settings and Routing Reasons", type = "object")
public class RoutingInformationResponse extends BaseResponse{

	@Schema(name ="routingSettings", description = "Response Class for Routing Information - Routing Settings", type = "object")
	private RoutingSettings routingSettings;
	
	@Schema(name ="routingReasons", description = "Response Class for Routing Information - Routing Reasons", type = "object")
	private RoutingReasons routingReasons;
	
}
