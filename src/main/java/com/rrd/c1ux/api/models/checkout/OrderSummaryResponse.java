/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/23/23				A Boomker				CAP-38155					Initial Version
 *	04/15/24				N Caceres				CAP-48487					Added expeditedOrderFee
 *	04/23/24				Krishna Natarajan		CAP-48866					Added Is OderRequest flag and notes map
 *	05/01/24				L De Leon				CAP-48972					Added allowOrderTemplate
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.Map;

import javax.validation.constraints.Min;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderSummaryResponse", description = "Response Class for Order Summary to load the Summary Modal", type = "object")
public class OrderSummaryResponse extends BaseResponse
{
	@Schema(name="itemCount", description="Number of top-level items currently in the cart. This number should not include components.", type="int", example="1")
	@Min(0)
	private int itemCount = 0;
	@Schema(name ="disclaimers", description = "Disclaimers to display for legal purposes", type = "string", example="Taxes are estimated only.")
	private String disclaimers = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "translationFramework", description = "Map of translation text used for displaying text in the order summary framework", type = "object")
	private Map<String, String> translationFramework;
	@Schema(name = "translationDelivery", description = "Map of translation text used for displaying text in the order delivery section", type = "object")
	private Map<String, String> translationDelivery;
	@Schema(name = "translationInfo", description = "Map of translation text used for displaying text in the order header info section", type = "object")
	private Map<String, String> translationInfo;
	@Schema(name = "translationSummary", description = "Map of translation text used for displaying text in the order summary modal", type = "object")
	private Map<String, String> translationSummary;

	@Schema(name ="totalItemPrice", description = "Price to display for the sum of the individual items only.", type = "string", example="TBD")
	private String totalItemPrice = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="shippingPrice", description = "Price to display for the freight charges only. Empty prices should not be shown.", type = "string", example="TBD")
	private String shippingPrice = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="taxPrice", description = "Price to display for the taxes only. Empty prices should not be shown.", type = "string", example="TBD")
	private String taxPrice = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="orderAndLinePrice", description = "Price to display for the sum of the order and line charges only. Empty prices should not be shown.", type = "string", example="TBD")
	private String orderAndLinePrice = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="grandTotalPrice", description = "Sum of all prices displayed. If any are TBD, this will be too.", type = "string", example="TBD")
	private String grandTotalPrice = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="expeditedOrderFee", description = "Expedite fee", type = "string", example="TBD")
	private String expeditedOrderFee = AtWinXSConstant.EMPTY_STRING;
	
	//CAP-48866
	@Schema(name ="isOrderRequest", description = "Sets Order Request to true or false based on the settings", type = "boolean", example="false")
	private boolean isOrderRequest = false;

	// CAP-48972
	@Schema(name ="allowOrderTemplate", description = "This will be set to true if the \"Allow Users to Order From Templates\" is Yes, or should return false if the setting is No", type = "boolean", example="false")
	private boolean allowOrderTemplate = false;

	@Schema(name = "translationNotes", description = "Map of translation text used for displaying notes in the order summary modal", type = "object")
	private Map<String, String> translationNotes;
}
