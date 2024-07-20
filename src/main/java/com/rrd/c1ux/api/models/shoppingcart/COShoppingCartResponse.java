/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By          JIRA#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------------
 * 	05/05/23	A Boomker			CAP-40333		Added showSaveOrderButton and OpenAPI3 documentation
 * 	04/11/24	Krishna Natarajan	CAP-48606		Added a new field doNotShareListsInd to update BU Manage List admin settings
 *	05/13/24	L De Leon			CAP-48977		Added deliveryOptionsList
 */
package com.rrd.c1ux.api.models.shoppingcart;

import java.util.List;
import java.util.Map;

import com.rrd.c1ux.api.models.catalogitems.FileDeliveryOption;
import com.rrd.c1ux.api.models.items.UOMForCartItems;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COShoppingCartResponse", description = "Response Class for Shopping Cart actions", type = "object")
public class COShoppingCartResponse {
	@Schema(name ="status", description = "String indicating success or failure", type = "string")
	private String status;
	@Schema(name ="statusMessage", description = "Error or success message corresponding to the status", type = "string")
	private String statusMessage;
	@Schema(name ="oeShoppingCartFormBean", description = "CustomPoint Shopping Cart Form Bean", type = "object")
	private OEShoppingCartFormBean oeShoppingCartFormBean;
	@Schema(name ="uomForCartItems", description = "List of the unit of measure options for each cart item", type = "array")
	private List<UOMForCartItems> uomForCartItems;
	@Schema(name ="disclaimers", description = "String of legal disclaimers to be shown in the summary modal in the cart", type = "string")
	private String disclaimers = AtWinXSConstant.EMPTY_STRING;//CAP-36082 added a new variable
	@Schema(name ="shoppingCartUomErrorMsg", description = "Error message used by the FE to show to the user when that error occurs in the cart", type = "string")
	private String shoppingCartUomErrorMsg = AtWinXSConstant.EMPTY_STRING;//CAP-36164
	@Schema(name ="shoppingCartQuantityErrorMsg", description = "Error message used by the FE to show to the user when that error occurs in the cart", type = "string")
	private String shoppingCartQuantityErrorMsg = AtWinXSConstant.EMPTY_STRING;//CAP-36164
	@Schema(name ="updateCartNoError", description = "Y/N flag indicating if there were any CustomPoint errors returned on update cart that must be shown.", type = "string", allowableValues = {"", "Y", "N"})
	private String updateCartNoError = AtWinXSConstant.EMPTY_STRING;//CAP-35439
	@Schema(name ="punchoutTransferCartValidationSuccess", description = "Error message used by the FE to show to the user when that error occurs in the cart", type = "string", allowableValues = {"", "Y", "N"})
	private String punchoutTransferCartValidationSuccess = AtWinXSConstant.EMPTY_STRING;//CAP-35439
	@Schema(name="showVendorItemNum", description="Determination of whether to show vendor item number. This is based on admin settings only.", type="boolean", example="false")
	private boolean showVendorItemNum=false;//CAP-37191 Add the show vendor flag
	@Schema(name ="translation", description = "Translation messages for cart page retrieved from \'shoppingcart\' file. ", type = "object",  example="\"translation\": { \"OrigDateLbl\": \"Original Date\"}")
	private Map<String, String> translation;//CAP-38710 Added for translation text
	// CAP-40333 - add flag for FE to know if should show button
	@Schema(name="showSaveOrderButton", description="Determination of whether to show save order button. This is based on admin settings and specific order characteristics.", type="boolean", example="false")
	private boolean showSaveOrderButton = false;
	//CAP-48606
	@Schema(name="doNotShareListsInd", description="Determination of whether show Dist List Shared settings at the BU level", type="boolean", example="false")
	private boolean doNotShareListsInd =false;

	// CAP-48977
	@Schema(name ="deliveryOptionsList", description = "List of file delivery option codes and equivalent options and display label.", type = "array")
	private List<FileDeliveryOption> deliveryOptionsList;
}
