/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/25/23				A Boomker				CAP-42223					Flag to redirect to cust doc UI added
 *	06/05/24				C Codina				CAP-49893					Flag to redirect to Kit Builder
 */
package com.rrd.c1ux.api.models.addtocart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemAddToCartResponse {

	 String status="";
	 boolean isItemAddedToCart=false;
	 String message="";
	 //CAP-35263 - BE -Add total item Count response attribute addtocart API
	 String itemCountInShopingCart="";
	@Schema(name = "redirectToCustomDocumentUI", description = "Value indicating the item was a cust doc and the UI has successfully initialized in session. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	 boolean redirectToCustomDocumentUI = false; // this will return true if the item was determined to be a custom document
	@Schema(name = "redirectToKitBuilder", description = "Value that indicates if the item should be redirected to kit builder", type = "boolean", allowableValues = {"false", "true"})
	boolean redirectToKitBuilder = false;
}
