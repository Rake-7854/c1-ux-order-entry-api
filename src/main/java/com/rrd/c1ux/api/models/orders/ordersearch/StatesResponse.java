/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/16/23				Krishna Natarajan		CAP-45181			Initial Version
 */
package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.util.NameValuePair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="StatesResponse", description = "Response Class for retrieving state list", type = "object")
public class StatesResponse extends BaseResponse {

	@Schema(name = "stateOptions", description = "List of states and their corresponding state codes", example = "[{\"name\":\"Alabama\", \"value\":\"AL\"},{\"name\":\"Alaska\", \"value\":\"AK\"}, {...}]", type = "array")
	private List<NameValuePair<String>> stateOptions;
}

