/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	JIRA #            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 * 	05/28/24	A Boomker			CAP-48604		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXSaveListResponse", description = "Response Class for saving a single list", type = "object")
public class C1UXSaveListResponse extends BaseResponse {
	@Schema(name = "listID", description = "Manage list stored ID", type = "number")
	private int listID = AtWinXSConstant.INVALID_ID;
	@Schema(name = "listName", description = "Manage list stored name", type = "string")
	private String listName = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "listRecords", description = "Manage list number of records", type = "number")
	private int listRecords = AtWinXSConstant.INVALID_ID;
}
