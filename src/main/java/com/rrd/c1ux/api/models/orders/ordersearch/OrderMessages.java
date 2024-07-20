/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------------
 *  12/05/23	Krishna Natarajan	CAP-45058		Initial version of orderMessages
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessages {
	@Schema(name = "messageText", description = "Order messages to get the message text and type of the message", type = "string")
	public String messageText;

	@Schema(name = "messageType", description = "Order messages to get the message text and type of the message", type = "string")
	public String messageType;
}
