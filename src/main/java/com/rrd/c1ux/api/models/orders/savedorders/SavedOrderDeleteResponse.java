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

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="SavedOrderDeleteResponse", description = "Response Class for deleting a single saved order", type = "object")
public class SavedOrderDeleteResponse extends BaseResponse {

}
