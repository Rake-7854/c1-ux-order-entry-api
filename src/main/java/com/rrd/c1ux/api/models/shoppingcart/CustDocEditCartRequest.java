/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	08/11/23	A Boomker			CAP-42295		Initial version
 */
package com.rrd.c1ux.api.models.shoppingcart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name ="CustDocEditCartRequest", description = "Request Class for the shopping cart to edit the selected custom document's order line. This extends the update cart request.", type = "object")
public class CustDocEditCartRequest extends COShoppingCartRequest {
	@Schema(name = "orderLineNumber", description = "Order line number for the line that is requested to be modified.", type = "number")
	protected int orderLineNumber = 0;

}
