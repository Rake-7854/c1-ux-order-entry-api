/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By          JIRA#            Description
 *	--------    -----------         ----------       -----------------------------------------------------------
 *	05/04/23    Satishkumar A   	CAP-37503        API Build - Save Order assuming all data already saved
 */
package com.rrd.c1ux.api.models.orders.savedorders;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SaveOrderRequest", description = "Request Class for Save Order", type = "object")
public class SaveOrderRequest {

	@Schema(name ="orderName", description = "Order name for the order to be saved", type = "string")
	@Size(min=1, max=150)
	private String orderName;

}
