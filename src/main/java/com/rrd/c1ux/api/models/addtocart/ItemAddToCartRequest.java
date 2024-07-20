/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	01/06/23	Krishna Natarajan		CAP-41083				Added availabilityCode variable in this request
 */

package com.rrd.c1ux.api.models.addtocart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="ItemAddToCartRequest", description = "Request object with parameters sent while adding item to cart", type = "object")
public class ItemAddToCartRequest {

	 String itemNumber="";
	 String vendorItemNumber="";
	 int catalogLineNumber=0;
	 String price="";
	 String selectedUom = "";
	 int itemQuantity = 0;
		@Schema(name = "availabilityCode", description = "The availability code of the item uom/qty combination getting added to the cart", type = "String", example = "A", allowableValues = {"", "A", "B", "J" })
	 String availabilityCode="";
}
