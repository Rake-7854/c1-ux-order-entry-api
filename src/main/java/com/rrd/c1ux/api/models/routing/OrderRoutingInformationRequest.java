/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			DTS#        	Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  08/09/23	S Ramachandran		CAP-42746       Request Object of Order Routing Information for an Order
 */
package com.rrd.c1ux.api.models.routing;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderRoutingInformationRequest", description = "Request Class to retrieve 'Routing Information of an Order'", type = "object")
public class OrderRoutingInformationRequest {

	@Schema(name ="orderID", description = "'Order ID' of an Order.'0' means order was not placed through Custom Point", type = "int", example="609332")
	@Min(0)
	@Max(2147483647)
	private int orderID;

}