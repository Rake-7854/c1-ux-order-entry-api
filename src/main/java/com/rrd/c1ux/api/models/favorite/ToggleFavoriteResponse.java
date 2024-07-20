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
 */

package com.rrd.c1ux.api.models.favorite;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="ToggleFavoriteResponse", description = "Response Class for Favorite Item Toggle", type = "object")
public class ToggleFavoriteResponse extends BaseResponse {
	
	@Schema(name ="isFavorite", description = "Flag that represents whether the item is favorite item or not. ", type = "boolean")
	private Boolean isFavorite;

}
