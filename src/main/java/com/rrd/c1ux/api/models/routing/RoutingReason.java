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
@Schema(name ="RoutingReason", description = "Response Class for Routing Information - Routing Reason", type = "object")

public class RoutingReason {

	@Schema(name ="routingCounter", description = "A String simple counter (formatted to 3 digits) that will show the line number of the reason.  Simply start at 001.", type = "string", example="001")
	private String routingCounter;
	
	@Schema(name ="vendorItemNumber", description = "A string which will show the vendor item number if the routing reason is a line level reason.  Will be blank if order level.", type = "string", example="")
	private String vendorItemNumber;
	
	@Schema(name ="itemNumber", description = "A String which will show the item number for the routing reason if a line level reason.  Will be blank if order level.", type = "string", example="")
	private String itemNumber;
	
	@Schema(name ="approvalQueue", description = "A String that will hold the display approval queue name.", type = "string", example="")
	private String approvalQueue;
	
	@Schema(name ="reasonDescription", description = "A String that will hold the reason description for the routing reason.", type = "string", example="")
	private String reasonDescription;

}
