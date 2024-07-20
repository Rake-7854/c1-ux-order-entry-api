/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#										Description
 * 	--------	-----------				----------------------------------------	------------------------------
 *	04/15/24	Krishna Natarajan		CAP-48534									Added the Response class required for catalog messages
 */
package com.rrd.c1ux.api.models.catalogitems;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CatalogMessageResponse", description = "Response class for retrieving catalog messages", type = "object")
public class CatalogMessageResponse extends BaseResponse {
	@Schema(name ="CatalogMessagesResponse", description = "List of Catalog Messages", type = "object")
	private List<CatalogMessagesResponse> catalogMessagesResponse;
}
