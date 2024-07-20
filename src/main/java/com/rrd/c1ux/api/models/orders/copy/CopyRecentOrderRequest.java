/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/18/23				L De Leon				CAP-41552					Initial Version
 */
package com.rrd.c1ux.api.models.orders.copy;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CopyRecentOrderRequest", description = "Request Class for copying an order that is already submitted", type = "object")
public class CopyRecentOrderRequest {

	@Schema(name = "orderID", description = "'Order ID' of an Order that is already submitted", type = "int", example = "581863")
	@Min(0)
	@Max(2147483647)
	private int orderID;
}