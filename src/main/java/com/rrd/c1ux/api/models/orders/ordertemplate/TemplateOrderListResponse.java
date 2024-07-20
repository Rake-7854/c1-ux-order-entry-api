/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/23/24	C Codina			CAP-48623				Initial Version
 */
package com.rrd.c1ux.api.models.orders.ordertemplate;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name ="TemplateOrderListResponse", description = "Response Class for Template Order List", type = "object")
public class TemplateOrderListResponse extends BaseResponse {
	
	@Schema(name = "orderTemplateCount", description = "Integer holding the number of templates the user has access to", type = "Integer")
	private int orderTemplateCount;
	
	@Schema(name = "itemInCart", description = "A boolean which will be returned indicating if the user has an item in cart.", type = "Boolean")
	boolean itemInCart;
	
	@Schema(name = "orderTemplates", description = "A list of order templates.", type = "List")
	private List<OrderTemplate> orderTemplates;

}
