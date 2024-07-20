/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	01/26/24	A Boomker			CAP-46336		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocGridAssignment", description = "Information about a cell assignment within a cust doc grid group", type = "object")
public class C1UXCustDocGridAssignment  {

	@Schema(name = "gridCellId", description = "This is the ID that must be attached to the TD element in the generated HTML for the group grid. Format is \"td\" + group Number + \"Col\" + column number + \"Row\" + row number", type = "String", example = "")
	private String gridCellId = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "row", description = "Row number. This must be 1 or greater to be valid.", type = "number")
	protected int row = 0;
	@Schema(name = "column", description = "Column number. This must be 1 or greater to be valid.", type = "number")
	protected int column = 0;
	@Schema(name = "columnSpan", description = "Column span. This must be 1 or greater to be valid.", type = "number")
	protected int columnSpan = 1;

}
