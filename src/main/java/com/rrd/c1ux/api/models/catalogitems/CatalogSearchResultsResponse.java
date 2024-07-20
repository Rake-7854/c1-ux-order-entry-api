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
 *  10/06/23	N Caceres			CAP-44349       Retrieves the HTML text assigned to the selected category - response 
 */

package com.rrd.c1ux.api.models.catalogitems;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CatalogSearchResults", description = "Response class for retrieving html text assigned to the selected category", type = "object")
public class CatalogSearchResultsResponse extends BaseResponse {
	
	@Schema(name ="categoryHtml", description = "HTML text assigned to the selected category", type = "string", example="<h1>Heading 1</h1>")
	private String categoryHtml;
	
	@Schema(name ="itemThumbnailCellData", description = "Item Thumbnail Cell Data", type = "object")
	private List<ItemThumbnailCellData> searchResults;
}
