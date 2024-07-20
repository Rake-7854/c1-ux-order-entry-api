/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/16/23				Krishna Natarajan		CAP-45181					Initial Version
 */
package com.rrd.c1ux.api.models.orders.ordersearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CountryCodeRequest", description = "Request Class to send the country code", type = "object")
public class CountryCodeRequest {
	@Schema(name ="countryCode", description = "Country code", example = "USA", type = "String")
	private String countryCode;
}
