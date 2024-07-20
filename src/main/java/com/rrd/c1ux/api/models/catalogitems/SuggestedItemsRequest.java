/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		--------------		--------------------------------
 *	05/29/24    Sakthi M        CAP-49694          	C1UX API - Create new API /api/items/suggesteditems to return companion items for items in cart or an order
 */

package com.rrd.c1ux.api.models.catalogitems;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestedItemsRequest {
	@Schema(name = "itemNumber", description = "Item number", type = "string", example = "1234")
	String itemNumber="";
	
	@Schema(name ="vendorItemNumber", description = "Vendor Item Number", type = "string", example = "Test1234")
	String vendorItemNumber="";
	
	@Schema(name ="orderLineNumber", description = "Line Number ",  type = "string", example = "4451234")
	String orderLineNumber="";
	
}



