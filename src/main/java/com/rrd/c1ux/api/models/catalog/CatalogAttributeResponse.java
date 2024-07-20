/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/08/24				C Codina				CAP-46200					Initial Version
 *	01/18/24				S Ramachandran			CAP-46304					Retrieve standard options attribute to use for the Catalog Page
 *	01/30/24				Krishna Natarajan		CAP-46821					Added a String field to set the standard attribute header label
 *  02/22/24				Krishna Natarajan		CAP-47345					Added a hasWizard boolean flag to indicate Allow Order Wizard
 *  02/22/24				Krishna Natarajan		CAP-47356					Added a hasRefine boolean flag to indicate Allow Refine Order Search
 */

package com.rrd.c1ux.api.models.catalog;

import java.util.ArrayList;
import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CatalogAttributeResponse extends BaseResponse {
	 @Schema(name ="standardAttributeHeaderLabel", description = "label to be shown up for standard attributes", type = "String")
	 String standardAttributeHeaderLabel= "";//CAP-46821
	
	@Schema(name = "catalogAttributes", description = "List that will contain the attribute information", type = "List")
	Collection<CatalogAttributes> catalogAttributes = new ArrayList<>();
	
	@Schema(name = "standardAttributes", description = "Standard attribute information", type = "object")
	StandardAttributesC1UX standardAttributes;
	
	@Schema(name = "hasWizard", description = "boolean value to show order wizard is enabled", type = "boolean", allowableValues = {"false", "true"})
	boolean hasWizard=false;
	
	@Schema(name = "hasRefine", description = "boolean value to show Refine Search is enabled", type = "boolean", allowableValues = {"false", "true"})
	boolean hasRefine=false;
}
