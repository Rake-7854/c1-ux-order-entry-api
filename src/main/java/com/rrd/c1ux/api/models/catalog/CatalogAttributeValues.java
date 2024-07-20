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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CatalogAttributeValues {
	
	@Schema(name = "attributeID", description = "The attribute ID that the value belongs with", type = "int")
	int attributeID;
	@Schema(name = "attributeValueID", description = "The attribute value ID for the value", type = "int")
	int attributeValueID;
	@Schema(name = "attributeValueDisplay", description = "The string holding the display name for the attribute", type = "String")
	String attributeValueDisplay;
	@Schema(name = "isSelected", description = "a boolean indicating if the attribute is selected", type = "boolean")
	boolean isSelected;

}
