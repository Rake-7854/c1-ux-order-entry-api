/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *	04/26/23	A Boomker			CAP-39340		Add API to delete saved order
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
@Schema(name ="SavedOrderDeleteRequest", description = "Request Class for Deleting a Saved Order", type = "object")
public class SavedOrderDeleteRequest {

	@Schema(name ="order", description = "Integer order ID for the order to be deleted", type = "number")
	@Min(0)
	private int order;

}
