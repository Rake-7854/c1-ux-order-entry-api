/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      ------------------------------------------
 *  07/31/23	S Ramachandran		CAP-41784       Get featured catalog items - response 
 */

package com.rrd.c1ux.api.models.favorite;

import java.util.ArrayList;
import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.wallace.atwinxs.catalogs.ao.CMFeaturedItemsTypesBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="FeaturedCatalogItemsResponse", description = "Response Class to retrieve CatalogItems with Featured details", type = "object")
public class FeaturedCatalogItemsResponse extends BaseResponse {
	
	@Schema(name ="itemThumbnailCellData", description = "Item Thumbnail Cell Data", type = "object")
	List<ItemThumbnailCellData> itemThumbnailCellData;
	
	@Schema(name ="showOrderLinePrice", description = "Flag to show Order Line Price", type = "boolean")
	private boolean showOrderLinePrice;
	
	@Schema(name ="showOrderLineAvailability", description = "Flag to show Order Line Availability", type = "boolean")
	private boolean showOrderLineAvailability;
	
	@Schema(name ="showOrderLineAvailability", description = "Show Order Line Availability", type = "String")
	private String priceLineAvailabilityLbl;
	
	@Schema(name ="resetPageNumber", description = "Reset Page Number", type = "String", example = "Y")
	private String resetPageNumber="Y";
	
	@Schema(name ="featuredTypes", description = "Featured ItemsTypes", type = "object")
	private ArrayList<CMFeaturedItemsTypesBean> featuredTypes; 

}
