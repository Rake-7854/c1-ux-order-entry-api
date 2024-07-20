/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/08/24				C Codina				CAP-46200					Initial Version
 *	01/26/24				L De Leon				CAP-46322					Added fields for toggling attribute values
 *	02/05/24				M Sakthi				CAP-46865					Added clearAll boolean value
 *  03/07/24				T Harmon				CAP-46723					Add small logic to remove attribtues from filters if wizard is on
 */
package com.rrd.c1ux.api.models.catalog;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class CatalogAttributeRequest {

	@Schema(name = "selectedAttributeID", description = "'Attribute ID' of the attribute where the selected attribute value belongs", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int selectedAttributeID = AtWinXSConstant.INVALID_ID;

	@Schema(name = "selectedAttributeValueID", description = "'Attribute Value ID' of the selected attribute value", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int selectedAttributeValueID = AtWinXSConstant.INVALID_ID;

	@Schema(name = "toggleFeature", description = "The standard option the user want to toggle to on or off", type = "String", example = "", allowableValues = {
			"", "favorite", "newItem", "<feature type ID>" })
	String toggleFeature;
	@Schema(name = "clearAll", description = "Flag indicating the clear all the attribute values.", type = "boolean")
	boolean clearAll=false;
	@Schema(name = "isWizard", description = "Flag indicating attribute call is for wizard", type = "boolean")
	boolean isWizard=false;	  // CAP-46723
}