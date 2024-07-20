/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	06/28/23	Satishkumar A      CAP-41594	   C1UX API - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 * 	07/11/23	Satishkumar A      CAP-41970		C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 */
package com.rrd.c1ux.api.models.util;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.util.CountryBean;
import com.rrd.custompoint.gwt.common.util.NameValuePair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CountriesAndStatesResponse", description = "Response Class for State/Country List information. ", type = "object")
public class CountriesAndStatesResponse extends BaseResponse{
	
	@Schema(name ="countriesAndStates", description = "List of country codes and their corresponding country information, including the list of states or provinces", type = "array")
	private List<NameValuePair<CountryBean>> countriesAndStates;

}
