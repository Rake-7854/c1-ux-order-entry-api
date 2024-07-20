/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/08/24				C Codina				CAP-46200					Initial Version
 */

package com.rrd.c1ux.api.models.catalog;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CatalogAttributes {
	
	@Schema(name = "attributeID", description = "The Attribute ID.", type = "int")
	int attributeID;
	@Schema(name = "attributeDisplayName", description = "A String holding the display for the attribute", type = "String")
	String attributeDisplayName;
	@Schema(name = "isMultiSelect", description = "a boolean indicating if the value is multi-select or not", type = "boolean")
	boolean isMultiSelect;
	@Schema(name = "catalogAttributeValues", description = "A collection of the attribute values", type = "List")
	List<CatalogAttributeValues> catalogAttributeValues = new ArrayList<>();

}
