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
 *  10/25/23	N Caceres			CAP-44349       Added variable to hold HTML content for assigned to the selected category ID
 *	05/13/24	L De Leon			CAP-48938		Added deliveryOptionsList
 *	07/01/24	S Ramachandran		CAP-50502		Add locations for the add buttons when in KitTemplateMode 
 */
package com.rrd.c1ux.api.models.catalogitems;
import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.models.kittemplate.KitContainerLocation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CatalogItemsResponse", description = "Response class for retrieving catalog items", type = "object")
public class CatalogItemsResponse extends BaseResponse{

	@Schema(name ="itemThumbnailCellData", description = "Item Thumbnail Cell Data", type = "array")
	private List<ItemThumbnailCellData> itemThumbnailCellData;
	
	@Schema(name ="showOrderLinePrice", description = "Show order line price", type = "boolean", example="true / false")
	private boolean showOrderLinePrice;
	
	@Schema(name ="showOrderLineAvailability", description = "Show order line availability", type = "boolean", example="true / false")
	private boolean showOrderLineAvailability;
	
	@Schema(name ="priceLineAvailabilityLbl", description = "Price Line Availability Label", type = "string", example="Check Current Pricing And Availability")
	private String priceLineAvailabilityLbl;
	
	@Schema(name ="resetPageNumber", description = "Reset page number", type = "string", example="Y / N")
	private String resetPageNumber = "Y";
	
	@Schema(name ="categoryHtml", description = "HTML text assigned to the selected category", type = "string", example="<h1>Heading 1</h1>")
	private String categoryHtml;

	// CAP-48977
	@Schema(name ="deliveryOptionsList", description = "List of file delivery option codes and equivalent options and display label.", type = "array")
	private List<FileDeliveryOption> deliveryOptionsList;
	
	// CAP-50502
	@Schema(name = "kitTemplateMode", description = "Boolean value indicates browse is in kit template mode or not", type = "boolean", allowableValues = {"false", "true"})
	private boolean kitTemplateMode = false;
	
	// CAP-50502
	@Schema(name ="kitContainerLocations", description = "List of Kit Container Locations", type = "array")
	private List<KitContainerLocation> kitContainerLocations;
}