
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
 * 	04/12/23	Satishkumar A      CAP-37497	   Saved Order – Getting the list of saved orders into the saved order page
 */ 
package com.rrd.c1ux.api.models.orders.savedorders;

import java.util.Map;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.orderentry.ao.OESavedOrdersFormBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SavedOrderResponse", description = "Response Class for Saved Orders List", type = "object")
public class SavedOrdersResponse extends BaseResponse {

	@Schema(name ="oeSavedOrdersFormBean", description = "List of saved orders", type = "object")
	private OESavedOrdersFormBean oeSavedOrdersFormBean;   
	
	@Schema(name ="translationSavedOrders", description = "Translation messages for Saved Order page retrieved from \'savedOrders\' file. ", type = "string",  example="\"translation\": { \"OrigDateLbl\": \"Original Date\"}")
	private Map<String, String> translationSavedOrders;

}
