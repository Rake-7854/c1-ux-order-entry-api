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
 *	04/26/23	A Boomker			CAP-39341		Add API to resume saved order
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
@Schema(name ="SavedOrderResumeRequest", description = "Request Class for resuming a Saved Order", type = "object")
public class SavedOrderResumeRequest {

	@Schema(name ="order", description = "Integer order ID for the order to be resumed", type = "number")
	@Min(0)
	private int order;

	@Schema(name ="overrideWarning", description = "Pass true to resume an order even after being warned that items may be removed if you continue. "
			+ "Pass false if a warning should be returned if there are items that would be removed by continuing. Defaults to false.", type = "boolean", example="false")
	private boolean overrideWarning = false;

}
