/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/16/24	Satishkumar A		CAP-46961				Create API - Remaining Budget Allocations
 */
package com.rrd.c1ux.api.models.budgetallocation;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.orderentry.session.AllocationSummaryBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "AllocationSummaryResponse", description = "Response Class for retrieving the remaining budget allocations", type = "object")
public class AllocationSummaryResponse extends BaseResponse {
	
	@Schema(name = "AllocationSummaryBean", description = "Response Class budget allocation details", type = "object")
	private AllocationSummaryBean allocationSummaryBean;
	
	@Schema(name = "exceedBudgetWarned", description = "Flag to indicate budget exceed or not", type = "boolean")
	private boolean exceedBudgetWarned;
	
	@Schema(name = "budgetAllocationWarnMsg", description = "String to represent the budget allocation warning message", type = "string")
	private String budgetAllocationWarnMsg;
	

}
