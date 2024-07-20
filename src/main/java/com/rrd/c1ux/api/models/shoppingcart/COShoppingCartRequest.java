/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-------------------------------------------------------------------
 *  03/28/24	N Caceres		CAP-47795	Add validation for Budget Allocation
 */

package com.rrd.c1ux.api.models.shoppingcart;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COShoppingCartRequest", description = "Request Class for updating items in the shopping cart.", type = "object")
public class COShoppingCartRequest implements Serializable
{
	@Schema(name ="coLineItems", description = "line item information from the cart", type = "array")
	private COShoppingCartLineFormBean[] coLineItems;
	@Schema(name ="userIPAddress", description = "IP address if that info is needed", type = "string")
	private String userIPAddress = null;
	@Schema(name ="backOrderWarned", description = "Flag indicating the user has already been shown the backorder warning for these items. Default is false.", type = "boolean")
	private boolean backOrderWarned = false;
	
	@Schema(name ="checkBudgetWarning", description = "Flag to check budget warning", type = "boolean")
	private boolean checkBudgetWarning = true;
}
