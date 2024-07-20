/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *	04/12/23	A Boomker			CAP-38160		Add API to get expansion detail for saved order
 */
package com.rrd.c1ux.api.models.orders.savedorders;

import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SavedOrderExpansionRequest", description = "Request Class for Saved Order Expansion details", type = "object")
public class SavedOrderExpansionRequest {

	@Schema(name ="order", description = "Integer order ID for the order seeking details on", type = "number")
	@Min(0)
	private int order;

}
