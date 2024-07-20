
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/18/24	Sakthi M			CAP-50145				Initial Version
 *	07/03/24	Satishkumar A		CAP-50560				searchTerm added
 */
package com.rrd.c1ux.api.models.kittemplate;

import java.util.Collection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "KitTemplateAddToCartRequest", description = "Request Class for Kit Template add to cart", type = "object")
public class KitTemplateAddToCartRequest {
	
	@Schema(name = "componentItems", description = "Collection of Component items", type = "object")
	private Collection<ComponentItems> componentItems;
	
	@Schema(name = "assemblyInstructions", description = "Assembly Instructions", type = "string", example="test")
	private String assemblyInstructions;
	
	//CAP-50560
	@Schema(name = "searchTerm", description = "A String holding the catalog search term that is used on the page", type = "string", example="test")
	private String searchTerm;
	
}

