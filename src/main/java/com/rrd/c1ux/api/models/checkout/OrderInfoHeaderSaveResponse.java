/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date				Modified By				JIRA#						Description
 * 	--------			-----------				-----------------------		--------------------------------
 *	05/8/23				A Boomker				CAP-38153					Initial Version
 */
package com.rrd.c1ux.api.models.checkout;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="OrderInfoHeaderSaveResponse", description = "Response Class for Order Info Header save request API", type = "object")
public class OrderInfoHeaderSaveResponse extends BaseResponse
{

}
