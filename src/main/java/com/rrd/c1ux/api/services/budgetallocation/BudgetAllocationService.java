/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/13/24	L De Leon			CAP-46960				Initial Version
 *	02/26/24	Satishkumar A		CAP-47325				C1UX BE - Create API - Remaining Budget Allocations Order Entry	
 *	03/21/24	L De Leon			CAP-47969				Added getBudgetAllocation() method
 *  04/23/24	T Harmon			CAP-48796				Added new method for summary budget allocation
 */
package com.rrd.c1ux.api.services.budgetallocation;

import com.rrd.c1ux.api.models.budgetallocation.AllocationSummaryResponse;
import com.rrd.c1ux.api.models.budgetallocation.BudgetAllocationResponse;
import com.rrd.custompoint.orderentry.entity.BudgetAllocation;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

public interface BudgetAllocationService {

	public BudgetAllocationResponse getBannerMessage(SessionContainer sc) throws AtWinXSException;
	//CAP-47325
	public AllocationSummaryResponse getRemainingBudgetAllocations(SessionContainer sc) throws AtWinXSException;
	
	//CAP-48796
	public AllocationSummaryResponse getRemainingBudgetAllocationsSummary(SessionContainer sc) throws AtWinXSException;

	public BudgetAllocation getBudgetAllocation(AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings);

	public String validateBudgetAllocations(OEOrderSessionBean oeOrderSessionBean, AppSessionBean appSessionBean,
			boolean isRedirectToCart);
}