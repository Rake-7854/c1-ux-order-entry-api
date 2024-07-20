/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	05/20/24		R Ruth				CAP-42228				Initial request
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;

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
@Schema(name ="C1UXCustDocSaveListMappingRequest", description = "Request for a save list for mapping", type = "object")
public class C1UXCustDocSaveListMappingRequest {
	@Schema(name = "listId", description = "Provide the list ID", type = "number", example = "11111")
   	private int listId = AtWinXSConstant.INVALID_ID;

	@Schema(name = "listColumnMap", description = "list of column map", type = "array")
   	private List<C1UXCustDocSaveListMappingBean> listColumnMap = null;

	@Schema(name = "maxColumns", description = "Provide the max number of columns", type = "number")
	private int maxColumns = AtWinXSConstant.INVALID_ID;
}
