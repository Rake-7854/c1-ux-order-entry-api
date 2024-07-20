/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  04/19/22    S Ramachandran  CAP-33763   Initial Creation
 */

package com.rrd.c1ux.api.models.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PNATieredPriceRequest
{

	@Schema(name = "corpNum", description = "A string that holds the value corporate number", type = "String")
	private String corpNum;
	@Schema(name = "soldToNumber", description = "A string that holds the value sold to number", type = "String")
	private String soldToNumber;
	@Schema(name = "orderType", description = "A string that holds the value of order type", type = "String")
	private String orderType;
	@Schema(name = "useCatalogPrice", description = "A flag that indicates to use the catalog price", type = "Boolean")
	private boolean useCatalogPrice;
	@Schema(name = "checkJobs", description = "A flag that indicates to check jobs", type = "Boolean")
	private boolean checkJobs;
	@Schema(name = "useCSSListPrice", description = "A flag that indicates to use CSS List Price", type = "Boolean")
	private boolean useCSSListPrice;
	@Schema(name = "useCustomersCustomerPrice", description = "A flag that indicates to use Customers Price", type = "Boolean")
	private boolean useCustomersCustomerPrice;
	@Schema(name = "useJLJLSPrice", description = "A flag that indicates the to use JLJLS Price", type = "Boolean")
	private boolean useJLJLSPrice;
	@Schema(name = "skipNSTPrice", description = "A flag that indicates to skip NST Price", type = "Boolean")
	private boolean skipNSTPrice;
	@Schema(name = "useTPP", description = "A flag that indicates to use TPP", type = "Boolean")
	private boolean useTPP;
	@Schema(name = "priceClass", description = "A string that holds the value of price class", type = "String")
	private String priceClass;
	@Schema(name = "showPrice", description = "A flag that indicates to show price", type = "Boolean")
	private boolean showPrice;
	@Schema(name = "siteId", description = "An integer that holds the value site ID", type = "Integer")
	private int siteId;
	@Schema(name = "wcssItemNum", description = "A string that holds the value of wcss item number", type = "Integer")
	private String wcssItemNum;
	@Schema(name = "orderQtyEA", description = "An integer that holds the value of order quantity", type = "Integer")
	private int orderQtyEA;

}
