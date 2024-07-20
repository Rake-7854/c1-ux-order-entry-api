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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="RoutingSettings", description = "Response Class for Routing Information - Routing Settings", type = "object")
public class RoutingSettings {

	@Schema(name ="showVendorItemNumber", description = "Flag which indicates if the vendor item number should be shown for the routing information.", type = "boolean")
	private boolean showVendorItemNumber;
	
	@Schema(name ="showApprovalQueue", description = "Flag which indicates if the approval queue information should be shown in the routing information.", type = "boolean")
	private boolean showApprovalQueue;
	
	@Schema(name ="showRoutingJustification", description = "Flag which indicates if we need a Routing Justification.", type = "boolean")
	private boolean showRoutingJustification;
	
	@Schema(name ="ohAlwaysRouteOrder", description = "Flag which indicates if the order is set to always route.", type = "boolean")
	private boolean ohAlwaysRouteOrder;
	
	@Schema(name ="ohRouteDollar", description = "Flag which indicates if the order is set to route by dollar.  Will be true if the User Group Manage Order Admin/User Manage Order Admin “Route Dollar ($) Amount” > 0, false otherwise.", type = "boolean")
	private Boolean ohRouteDollar;
	
	@Schema(name ="ohRouteByDollarAmount", description = "A double value which holds the amount to route an order on.", type = "double")
	private double ohRouteByDollarAmount;
	
	@Schema(name ="ohRouteShippingChange", description = "Flag which indicates if the order routes on a shipping change.", type = "boolean")
	private boolean ohRouteShippingChange;
	
	@Schema(name ="ohApprovalQueueID", description = "An integer which will hold the assigned approval queue ID.", type = "int")
	private int ohApprovalQueueID;
	
}
