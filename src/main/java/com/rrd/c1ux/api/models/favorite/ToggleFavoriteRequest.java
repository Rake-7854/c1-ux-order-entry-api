/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 *  07/31/23    Satishkumar A      CAP-33059       C1UX API - API Build - Favorite Toggle Call
 *  08/09/23 	Satishkumar A		CAP-42720		C1UX API - API Build - Favorite Toggle Call
 */
package com.rrd.c1ux.api.models.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="ToggleFavoriteRequest", description = "Request Class for Favorite Item Toggle", type = "object")
public class ToggleFavoriteRequest {
	
	@Schema(name ="customerItemNumber", description = "Customer Item Number", type = "string")
	private String customerItemNumber = "";
	@Schema(name ="vendorItemNumber", description = "Vendor Item Number", type = "string")
	private String vendorItemNumber = "";

}
