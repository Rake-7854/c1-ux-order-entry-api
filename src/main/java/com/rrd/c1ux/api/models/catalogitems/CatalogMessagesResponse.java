/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#										Description
 * 	--------	-----------				----------------------------------------	------------------------------
 *	04/15/24	Krishna Natarajan		CAP-48534									Added the Response class with fields required for catalog messages
 */

package com.rrd.c1ux.api.models.catalogitems;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CatalogSearchResults", description = "Response class for retrieving html text assigned to the selected category", type = "object")
public class CatalogMessagesResponse {
	@Schema(name = "catalogMessageLocation", description = "Location indicating if the message will be displayed at the Top/Bottom", type = "string", example = "T")
	private String catalogMessageLocation = "";
	@Schema(name = "catalogMessage", description = "The catalog message as configured", type = "string", example = "New Message")
	private String catalogMessage = "";
}
