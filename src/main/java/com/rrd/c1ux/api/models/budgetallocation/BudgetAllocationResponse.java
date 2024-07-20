/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/13/24	L De Leon			CAP-46960				Initial Version
 */
package com.rrd.c1ux.api.models.budgetallocation;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "BudgetAllocationResponse", description = "Response Class for retrieving the remaining budget allocation in the homepage", type = "object")
public class BudgetAllocationResponse extends BaseResponse {

	@Schema(name ="budgetMessage", description = "The remaining budget allocation before before completing an order", type = "string", example="Budget Allocation: $500.00")
	private String budgetMessage = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="timeframeMessage", description = "The timeframe for the budget allocation. This can be blank.", type = "string", example="From 01/01/2024 to 12/31/2024")
	private String timeframeMessage = AtWinXSConstant.EMPTY_STRING;
}