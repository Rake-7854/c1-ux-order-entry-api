/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/25/23				L De Leon				CAP-38158					Initial Version
 */
package com.rrd.c1ux.api.models.orders.copy;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "QuickCopyOrderResponse", description = "Response Class for copying an order from the order confirmation page for an order that has just been submitted", type = "object")
public class QuickCopyOrderResponse extends BaseResponse {

}