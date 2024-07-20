/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/20/24				R Ruth				CAP-42295					Initial Version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="C1UXCustDocSaveListMappingBean", description = "Bean for C1UXCustDocSaveListMappingRequest", type = "object")
public class C1UXCustDocSaveListMappingBean {
	@Schema(name ="columnHeading", description = "Column Heading shown for this column", type = "string")
	protected String columnHeading = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="variableDisplayLabel", description = "Merge variable display label if a variable is chosen in the dropdown (blank if not mapped)", type = "string")
	protected String variableDisplayLabel = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="variableName", description = "Merge variable name if a merge variable is chosen in the dropdown (blank if not mapped)", type = "string")
	protected String variableName = AtWinXSConstant.EMPTY_STRING;
}
