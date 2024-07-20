/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	08/11/23	A Boomker			CAP-42295		Initial version
 */
package com.rrd.c1ux.api.models.shoppingcart;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Schema(name ="CustDocEditCartResponse", description = "Response Class for request on the shopping cart to edit the selected custom document's order line.", type = "object")
public class CustDocEditCartResponse extends BaseResponse {

}
