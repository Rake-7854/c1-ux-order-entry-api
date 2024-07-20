/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By          JIRA#           Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *	05/04/23    Satishkumar A   	CAP-37503       API Build - Save Order assuming all data already saved
 */
package com.rrd.c1ux.api.models.orders.savedorders;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="SaveOrderResponse", description = "Response Class for Save Order", type = "object")
public class SaveOrderResponse extends BaseResponse {

}
