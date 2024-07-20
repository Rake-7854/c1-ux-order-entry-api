/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/31/24				N Caceres				CAP-46698					Initial Version
 */
package com.rrd.c1ux.api.models.catalog.wizard;

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
public class OrderWizardQuestionRequest {
	
	@Schema(name = "attributeID", description = "'Attribute ID' of the attribute where the selected attribute value belongs", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int attributeID = AtWinXSConstant.INVALID_ID;

	@Schema(name = "attributeValueID", description = "'Attribute Value ID' of the selected attribute value", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int attributeValueID = AtWinXSConstant.INVALID_ID;
	
	@Schema(name = "attributeQuestionID", description = "'Attribute Question ID' of the selected question", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int attributeQuestionID = AtWinXSConstant.INVALID_ID;
	
	@Schema(name = "wizardQuestionID", description = "'Wizard Question ID' of the selected question", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int wizardQuestionID = AtWinXSConstant.INVALID_ID;
}
