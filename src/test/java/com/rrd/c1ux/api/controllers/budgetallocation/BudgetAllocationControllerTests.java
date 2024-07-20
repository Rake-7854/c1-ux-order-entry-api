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
 */
package com.rrd.c1ux.api.controllers.budgetallocation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.budgetallocation.AllocationSummaryResponse;
import com.rrd.c1ux.api.models.budgetallocation.BudgetAllocationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class BudgetAllocationControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private BudgetAllocationResponse selfRegistrationPatternAfterResponseSuccess;
	private BudgetAllocationResponse selfRegistrationPatternAfterResponseFailed;
	
	//CAP-47325
	private AllocationSummaryResponse remainingBudgetAllocationsResponseSuccess;
	private AllocationSummaryResponse remainingBudgetAllocationsResponseFailed;
	

	public static final String EXPECTED_422MESSAGE = "Generic Error";

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		selfRegistrationPatternAfterResponseSuccess = getBudgetAllocationResponseSuccessTest();
		selfRegistrationPatternAfterResponseFailed = getBudgetAllocationResponseFailedTest();
		
		//CAP-47325
		remainingBudgetAllocationsResponseSuccess = getRemainingBudgetAllocationsResponseSuccessTest();
		remainingBudgetAllocationsResponseFailed = getRemainingBudgetAllocationsResponseFailedTest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_getBannerMessage_returnsExpected() throws Exception {

		// when getBannerMessage returns a success BudgetAllocationResponse object
		when(mockBudgetAllocationService.getBannerMessage(any(SessionContainer.class)))
				.thenReturn(selfRegistrationPatternAfterResponseSuccess);

		// when getBannerMessage is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_BUDGET_ALLOCATION_BANNER_MSG)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_getBannerMessage_returnsErrorMessage_whenError() throws Exception {

		// when getBannerMessage returns a failed BudgetAllocationResponse object
		when(mockBudgetAllocationService.getBannerMessage(any(SessionContainer.class)))
				.thenReturn(selfRegistrationPatternAfterResponseFailed);

		// when getBannerMessage is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_BUDGET_ALLOCATION_BANNER_MSG)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	
	//CAP-47325
	@Test
	void that_getRemainingBalance_returnsExpected() throws Exception {

		// when getRemainingBudgetAllocations returns a success AllocationSummaryResponse object
		when(mockBudgetAllocationService.getRemainingBudgetAllocations(any(SessionContainer.class)))
				.thenReturn(remainingBudgetAllocationsResponseSuccess);

		// when getRemainingBudgetAllocations is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_REMAINING_BUDGET_ALLOCATIONS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-47325
	@Test
	void that_getRemainingBalance_returnsErrorMessage_whenError() throws Exception {

		// when getRemainingBudgetAllocations returns a failed AllocationSummaryResponse object
		when(mockBudgetAllocationService.getRemainingBudgetAllocations(any(SessionContainer.class)))
		.thenReturn(remainingBudgetAllocationsResponseFailed);

		// when getRemainingBudgetAllocations is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_REMAINING_BUDGET_ALLOCATIONS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}


	private BudgetAllocationResponse getBudgetAllocationResponseSuccessTest() {

		selfRegistrationPatternAfterResponseSuccess = new BudgetAllocationResponse();
		selfRegistrationPatternAfterResponseSuccess.setSuccess(true);
		selfRegistrationPatternAfterResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return selfRegistrationPatternAfterResponseSuccess;
	}

	private BudgetAllocationResponse getBudgetAllocationResponseFailedTest() {

		selfRegistrationPatternAfterResponseFailed = new BudgetAllocationResponse();
		selfRegistrationPatternAfterResponseFailed.setSuccess(false);
		selfRegistrationPatternAfterResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return selfRegistrationPatternAfterResponseFailed;
	}

	//CAP-47325
	private AllocationSummaryResponse getRemainingBudgetAllocationsResponseSuccessTest() {
		
		remainingBudgetAllocationsResponseSuccess = new AllocationSummaryResponse();
		remainingBudgetAllocationsResponseSuccess.setSuccess(true);
		remainingBudgetAllocationsResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return remainingBudgetAllocationsResponseSuccess;
	}
	//CAP-47325
	private AllocationSummaryResponse getRemainingBudgetAllocationsResponseFailedTest() {
		
		remainingBudgetAllocationsResponseFailed = new AllocationSummaryResponse();
		remainingBudgetAllocationsResponseFailed.setSuccess(false);
		remainingBudgetAllocationsResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return remainingBudgetAllocationsResponseFailed;
	}
}