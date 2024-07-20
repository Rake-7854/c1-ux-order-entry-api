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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrderWizardQuestionResponse extends BaseResponse {

	@Schema(name ="name", description = "The wizard name", type = "String")
	String name = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name = "attributeID", description = "'Attribute ID' of the attribute where the selected attribute value belongs", type = "int", example = "-1")
	@Min(0)
	@Max(2147483647)
	int attributeID = AtWinXSConstant.INVALID_ID;
	
	@Schema(name = "attributeValues", description = "The list of attribute values and their corresponding descriptions", type = "List")
	List<OrderWizardAttributeValues> attributeValues = new ArrayList<>();
	
	@Schema(name = "keyAttribute", description = "Flag indicating if this is the key attribute or not", type = "boolean", allowableValues = {"false", "true"})
	boolean keyAttribute = false;
	
	@Schema(name ="attributeQuestion", description = "The question to ask for the key attribute", type = "String")
	String attributeQuestion = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name = "noMoreQuestion", description = "Flag indicating there are no more question", type = "boolean", allowableValues = {"false", "true"})
	boolean noMoreQuestion = false;
}
