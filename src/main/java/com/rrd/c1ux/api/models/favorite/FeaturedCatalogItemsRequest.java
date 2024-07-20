/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			DTS#        	Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  07/31/23	S Ramachandran		CAP-41784       Get featured catalog items - request
 */
package com.rrd.c1ux.api.models.favorite;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="FeaturedCatalogItemsRequest", description = "Request Class to retrieve Catalog Items with selected Featured", type = "object")
public class FeaturedCatalogItemsRequest {
	
	@Schema(name ="featuredType", description = "Featured Type Code. Sample 'Empty List' or [\"1\", \"2\", \"3\", \"21\", \"22\", \"41\"]", type = "array", example = "[]")
	private List<String> featuredType;

}
