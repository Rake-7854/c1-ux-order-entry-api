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
 *	04/12/23	A Boomker			CAP-38160		Add API to get expansion detail for saved order
 */
package com.rrd.c1ux.api.models.orders.savedorders;

import java.util.HashMap;
import java.util.Map;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderDetailsResponseBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SavedOrderExpansionResponse", description = "Response Class for Saved Order Expansion for a single saved order", type = "object")
public class SavedOrderExpansionResponse extends BaseResponse {

	@Schema(name ="expansion", description = "Details of saved order", type = "object")
	private OESavedOrderDetailsResponseBean expansion;

	@Schema(name="fullTextUomMap", description="Map of strings to show for each line number item found within the items list for this order. "
			+ " Key is the line number and value is the full text string. ex. {{\"462534\",\"Carton of 1500\"}} ", type="array")
	private Map<String, String> fullTextUomMap = new HashMap<>();

	@Schema(name="showShipToAttention", description="Determination of whether to show ship to attention for this order. This is based on admin settings and specific order characteristics.", type="boolean", example="false")
	private boolean showShipToAttention = false;

	@Schema(name="shipToAttentionLabel", description="Administration customized label for ship to attention. Note that because this is custom, it is not translated unless the value is the default.", type="string", example="Ship To Attention")
	private String shipToAttentionLabel = AtWinXSConstant.EMPTY_STRING;

	@Schema(name="showBillToAttention", description="Determination of whether to show bill to attention for this order. This is based on admin settings and specific order characteristics.", type="boolean", example="false")
	private boolean showBillToAttention = false;

	@Schema(name="billToAttentionLabel", description="Administration customized label for bill to attention. Note that because this is custom, it is not translated unless the value is the default.", type="string", example="Bill To Attention")
	private String billToAttentionLabel = AtWinXSConstant.EMPTY_STRING;

}
