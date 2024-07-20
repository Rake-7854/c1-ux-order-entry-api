/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/16/23				L De Leon				CAP-45180					Initial Version
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
@Schema(name ="CountriesResponse", description = "Response Class for retrieving country list", type = "object")
public class CountriesResponse extends BaseResponse {

	@Schema(name ="countryOptions", description = "List of countries and their corresponding country codes", example = "[{\"name\":\"Canada\", \"value\":\"CAN\"}, {...}]", type = "array")
	private List<NameValuePair<String>> countryOptions;
}
