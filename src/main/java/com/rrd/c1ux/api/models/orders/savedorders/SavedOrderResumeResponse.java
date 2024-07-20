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

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SavedOrderResumeResponse", description = "Response Class for resuming a single saved order", type = "object")
public class SavedOrderResumeResponse extends BaseResponse {

	@Schema(name ="promptForContinue", description = "Returns true if the header message specifies that items may be removed if you continue. "
			+ "Returns false if no items were removed or if items were removed but the request sent overrideWarning to true. Defaults to false.", type = "boolean", example="false")
	private boolean promptForContinue = false;

	@Schema(name ="hardStop", description = "Passes true if the user has attempted to return an order and that cannot be allowed or failed on the attempt for a reason in the header message. "
			+ "Pass false if either promptForContinue is true or if the resumption of the order was successful. Defaults to false.", type = "boolean", example="false")
	private boolean hardStop = false;

}
