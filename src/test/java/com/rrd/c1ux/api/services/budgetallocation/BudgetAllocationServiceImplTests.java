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
 *	03/22/24	L De Leon			CAP-47969				Added test methods for validating budget allocation
 *	04/02/24	Krishna Natarajan	CAP-48380				Changed the test steps as required
*	04/05/24	Krishna Natarajan	CAP-48419				Added additional mocks method to throw error based on budget allocation admin settings
 *  04/09/24	Krishna Natarajan	CAP-48511				Removed unnecessary stubbing on access forbidden check on remaining budget tests
 */
package com.rrd.c1ux.api.services.budgetallocation;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.budgetallocation.AllocationSummaryResponse;
import com.rrd.c1ux.api.models.budgetallocation.BudgetAllocationResponse;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartService;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.gwt.ordersearch.widget.CopyOrderResult;
import com.rrd.custompoint.orderentry.entity.BudgetAllocation;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.locale.ao.I18nBean;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartLineFormBean;

class BudgetAllocationServiceImplTests extends BaseOEServiceTest {

	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;

	private static final String FAIL = "Failed";
	private static final int TEST_SITE_ID = 1234;
	private static final int TEST_BU_ID = 4321;
	private static final int TEST_PROFILE_NBR = 12345;
	private static final int TEST_ALLOC_GRP_ATTR_ID = 123;
	private static final String TEST_ALLOC_LVL_CD = "P";
	private static final String TEST_TIMEFRAME_CD = "M";
	private static final String TEST_ALLOC_BUDGET_CD = "U";
	private static final double TEST_BUDGET_AMOUNT = 500.0;
	private static final double TEST_BUDGET_AMOUNT_ZERO = 0.0;
	private static final Date TEST_START_DATE = new Date(1704067200000L);
	private static final Date TEST_END_DATE = new Date(1706745600000L);
	private static final String TEST_BUDGET_AMOUNT_STR = "$ 500.00";
	private static final String TEST_START_DATE_STR = "1/1/2024";
	private static final String TEST_END_DATE_STR = "2/1/2024";

	private BudgetAllocationResponse budgetAllocationResponse;
	//CAP-47325
	private AllocationSummaryResponse allocationSummaryResponse;
	private AllocationQuantitiesCompositeVO allocQtyVO = getAllocQtyVO();


	@Mock
	private EntityObjectMap mockEntityObjectMap;

	@Mock
	private BudgetAllocation mockBudgetAllocation;

	@Mock
	AllocationQuantitiesCompositeVO mockAllocationQuantitiesCompositeVO;

	@Mock
	XSCurrency mockXSCurrency;

	@InjectMocks
	private BudgetAllocationServiceImpl service;

	//CAP-47325
	@Mock
	private ShoppingCartService mockShoppingCartService;
	@Mock
	private OEShoppingCartFormBean mockOEShoppingCartFormBean;

	@BeforeEach
	void setup() throws Exception {

		budgetAllocationResponse = null;
		allocationSummaryResponse = null;
	}

	@Test
	void that_getBannerMessage_return_success() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);//CAP-48419
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		when(mockUserSettings.getAllocationsGroupAttributeId()).thenReturn(TEST_ALLOC_GRP_ATTR_ID);
		when(mockUserSettings.getAllocationsLevelCode()).thenReturn(TEST_ALLOC_LVL_CD);
		when(mockUserSettings.getBudgetAllocTimeFrameCode()).thenReturn(TEST_TIMEFRAME_CD);
		when(mockUserSettings.getBudgetAllocationLevelCd()).thenReturn(TEST_ALLOC_BUDGET_CD);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBudgetAllocation).when(mockEntityObjectMap).getEntity(BudgetAllocation.class, null);
		doNothing().when(mockBudgetAllocation).populateWithNoOrder(eq(TEST_SITE_ID), eq(TEST_BU_ID), eq(TEST_BU_ID),
				eq(TEST_ALLOC_GRP_ATTR_ID), eq(TEST_ALLOC_LVL_CD), eq(TEST_TIMEFRAME_CD), any(I18nBean.class),
				eq(TEST_ALLOC_BUDGET_CD));
		doReturn(TEST_BUDGET_AMOUNT).when(mockBudgetAllocation).getCurrentBudgetAmount();
		doReturn(TEST_START_DATE).when(mockBudgetAllocation).getPeriodStartDate();
		doReturn(TEST_END_DATE).when(mockBudgetAllocation).getPeriodEndDate();

		try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
			mockedUtil.when(()-> Util.getStringFromCurrency(TEST_BUDGET_AMOUNT, null, false, 2)).thenReturn(mockXSCurrency);
			when(mockXSCurrency.getAmountText()).thenReturn(TEST_BUDGET_AMOUNT_STR);
			mockedUtil.when(()-> Util.getStringFromDate(TEST_START_DATE, null)).thenReturn(TEST_START_DATE_STR);
			mockedUtil.when(()-> Util.getStringFromDate(TEST_END_DATE, null)).thenReturn(TEST_END_DATE_STR);

			budgetAllocationResponse = service.getBannerMessage(mockSessionContainer);

			assertNotNull(budgetAllocationResponse);
			assertTrue(budgetAllocationResponse.isSuccess());
			assertTrue(budgetAllocationResponse.getMessage().isEmpty());
		}
	}

	@Test
	void that_getBannerMessage_return_access_forbidden_exception() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(false);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getBannerMessage(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_getPatternAfterUsers_returnErrorMessage_when_exception_thrown() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);//CAP-48419
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		when(mockUserSettings.getAllocationsGroupAttributeId()).thenReturn(TEST_ALLOC_GRP_ATTR_ID);
		when(mockUserSettings.getAllocationsLevelCode()).thenReturn(TEST_ALLOC_LVL_CD);
		when(mockUserSettings.getBudgetAllocTimeFrameCode()).thenReturn(TEST_TIMEFRAME_CD);
		when(mockUserSettings.getBudgetAllocationLevelCd()).thenReturn(TEST_ALLOC_BUDGET_CD);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBudgetAllocation).when(mockEntityObjectMap).getEntity(BudgetAllocation.class, null);
		doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockBudgetAllocation).populateWithNoOrder(eq(TEST_SITE_ID), eq(TEST_BU_ID), eq(TEST_BU_ID),
				eq(TEST_ALLOC_GRP_ATTR_ID), eq(TEST_ALLOC_LVL_CD), eq(TEST_TIMEFRAME_CD), any(I18nBean.class),
				eq(TEST_ALLOC_BUDGET_CD));
		doReturn(0d).when(mockBudgetAllocation).getCurrentBudgetAmount();
		doReturn(null).when(mockBudgetAllocation).getPeriodStartDate();
		doReturn(null).when(mockBudgetAllocation).getPeriodEndDate();

		try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
			mockedUtil.when(()-> Util.getStringFromCurrency(0d, null, false, 2)).thenReturn(mockXSCurrency);
			when(mockXSCurrency.getAmountText()).thenReturn(AtWinXSConstant.EMPTY_STRING);
			mockedUtil.when(()-> Util.getStringFromDate(null, null)).thenReturn(AtWinXSConstant.EMPTY_STRING);
			mockedUtil.when(()-> Util.getStringFromDate(null, null)).thenReturn(AtWinXSConstant.EMPTY_STRING);

			budgetAllocationResponse = service.getBannerMessage(mockSessionContainer);

			assertNotNull(budgetAllocationResponse);
			assertTrue(budgetAllocationResponse.isSuccess());
			assertTrue(budgetAllocationResponse.getMessage().isEmpty());
		}
	}

	//CAP-47325
	@Test
	void that_getRemainingBudgetAllocations_return_success() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOEOrderSession.getOrderScenarioNumber()).thenReturn(1);
		when(mockShoppingCartService.loadShoppingCart(any(), any(), anyBoolean())).thenReturn(null);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);

		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		when(mockUserSettings.getBudgetAllocationLevelCd()).thenReturn(TEST_ALLOC_BUDGET_CD);

		try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
			mockedUtil.when(()-> Util.getStringFromCurrency(TEST_BUDGET_AMOUNT, null, false, 2)).thenReturn(mockXSCurrency);
			mockedUtil.when(()-> Util.getStringFromDate(TEST_START_DATE, null)).thenReturn(TEST_START_DATE_STR);
			mockedUtil.when(()-> Util.getStringFromDate(TEST_END_DATE, null)).thenReturn(TEST_END_DATE_STR);

			allocationSummaryResponse = service.getRemainingBudgetAllocations(mockSessionContainer);

			assertNotNull(allocationSummaryResponse);
			assertTrue(allocationSummaryResponse.isSuccess());
			assertTrue(allocationSummaryResponse.getMessage().isEmpty());
		}
	}

	//CAP-47325
	void that_getRemainingBudgetAllocations_return_access_forbidden_exception() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOEOrderSession.getOrderScenarioNumber()).thenReturn(1);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(false);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getRemainingBudgetAllocations(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	//CAP-47325
	@Test
	void that_getRemainingBudgetAllocations_return_422_when_allocations_null() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOEOrderSession.getOrderScenarioNumber()).thenReturn(1);
		when(mockShoppingCartService.loadShoppingCart(any(), any(), anyBoolean())).thenReturn(mockOEShoppingCartFormBean);
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(getOEShoppingCartLineFormBean());

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(true);
		when(mockUserSettings.isAllowOrderingWithoutBudget()).thenReturn(false);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);

		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		when(mockUserSettings.getBudgetAllocationLevelCd()).thenReturn(TEST_ALLOC_BUDGET_CD);

		allocationSummaryResponse = service.getRemainingBudgetAllocations(mockSessionContainer);

		assertNotNull(allocationSummaryResponse);
		assertFalse(allocationSummaryResponse.isSuccess());
		assertTrue(allocationSummaryResponse.getMessage().isEmpty());

		when(mockOEShoppingCartFormBean.getItems()).thenReturn(null);

		allocationSummaryResponse = service.getRemainingBudgetAllocations(mockSessionContainer);

		assertNotNull(allocationSummaryResponse);
		assertTrue(allocationSummaryResponse.isSuccess());
		assertTrue(allocationSummaryResponse.getMessage().isEmpty());

		when(mockUserSettings.isAllowOrderingWithoutBudget()).thenReturn(true);
		allocationSummaryResponse = service.getRemainingBudgetAllocations(mockSessionContainer);

		assertNotNull(allocationSummaryResponse);
		assertTrue(allocationSummaryResponse.isSuccess());
		assertTrue(allocationSummaryResponse.getMessage().isEmpty());

	}

	//CAP-47325
	@Test
	void that_getRemainingBudgetAllocations_return_422_when_allocations_not_null() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOEOrderSession.getOrderScenarioNumber()).thenReturn(1);
		when(mockShoppingCartService.loadShoppingCart(any(), any(), anyBoolean())).thenReturn(mockOEShoppingCartFormBean);
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(getOEShoppingCartLineFormBean());

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(true);
		doReturn(allocQtyVO).when(service).getAllocQty(any(), any(), any(), any());

		try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
			mockedUtil.when(()-> Util.getStringFromCurrency(TEST_BUDGET_AMOUNT, null, false, 2)).thenReturn(mockXSCurrency);
			mockedUtil.when(()-> Util.getStringFromDate(TEST_START_DATE, null)).thenReturn(TEST_START_DATE_STR);
			mockedUtil.when(()-> Util.getStringFromDate(TEST_END_DATE, null)).thenReturn(TEST_END_DATE_STR);

			allocationSummaryResponse = service.getRemainingBudgetAllocations(mockSessionContainer);

			assertNotNull(allocationSummaryResponse);
			assertTrue(allocationSummaryResponse.isSuccess());
			assertTrue(allocationSummaryResponse.getMessage().isEmpty());
		}
	}

	//CAP-47325
	private OEShoppingCartLineFormBean[] getOEShoppingCartLineFormBean() {

		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setAvailabilityCode("B");
		return items;
	}
	//CAP-47325
	private AllocationQuantitiesCompositeVO getAllocQtyVO() {
		AllocationQuantitiesCompositeVO allocQty = new AllocationQuantitiesCompositeVO(TEST_SITE_ID, TEST_PROFILE_NBR, TEST_BU_ID, TEST_BUDGET_AMOUNT_ZERO, TEST_ALLOC_GRP_ATTR_ID, FAIL, TEST_END_DATE, EXPECTED_403MESSAGE);
		return allocQty;
	}

	@Test
	void that_validateBudgetAllocations_hasAllocationsOff() throws Exception {

		service = Mockito.spy(service);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(false);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false);

		assertTrue(errorMsg.isEmpty());
	}

	@Test
	void that_validateBudgetAllocations_isNotRedirectToCart() throws Exception {

		service = Mockito.spy(service);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false);

		assertTrue(errorMsg.isEmpty());
	}

	@Test

	void that_validateBudgetAllocations_hasNoBudgetSetup() throws Exception {

		service = Mockito.spy(service);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		when(service.getBudgetAllocation(mockAppSessionBean, mockUserSettings)).thenReturn(null);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true);

		assertTrue(errorMsg.isEmpty());
	}

	@Test

	void that_validateBudgetAllocations_hasNoCurrentBudgetSetup() throws Exception {

		service = Mockito.spy(service);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		doReturn(mockBudgetAllocation).when(service).getBudgetAllocation(mockAppSessionBean, mockUserSettings);
		when(mockBudgetAllocation.getCurrentBudget()).thenReturn(null);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true);

		assertTrue(errorMsg.isEmpty());
	}

	@Test

	void that_validateBudgetAllocations_hasRemainingBudget() throws Exception {

		service = Mockito.spy(service);


		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		doReturn(mockBudgetAllocation).when(service).getBudgetAllocation(mockAppSessionBean, mockUserSettings);
		when(mockBudgetAllocation.getCurrentBudget()).thenReturn(mockAllocationQuantitiesCompositeVO);
		when(mockAllocationQuantitiesCompositeVO.getRemainingQuantity()).thenReturn(100d);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true);

		assertTrue(errorMsg.isEmpty());
	}

	@Test

	void that_validateBudgetAllocations_hasNoRemainingBudget_ccPaymentForced() throws Exception {

		service = Mockito.spy(service);

		CopyOrderResult copyOrderResult = new CopyOrderResult();
		copyOrderResult.setRedirectToCart(true);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		doReturn(mockBudgetAllocation).when(service).getBudgetAllocation(mockAppSessionBean, mockUserSettings);
		when(mockBudgetAllocation.getCurrentBudget()).thenReturn(mockAllocationQuantitiesCompositeVO);
		when(mockAllocationQuantitiesCompositeVO.getRemainingQuantity()).thenReturn(0d);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(true);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true);

		assertTrue(errorMsg.isEmpty());
	}

	@Test

	void that_validateBudgetAllocations_hasNoRemainingBudget_ccPaymentNotForced() throws Exception {

		service = Mockito.spy(service);

		CopyOrderResult copyOrderResult = new CopyOrderResult();
		copyOrderResult.setRedirectToCart(true);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		doReturn(mockBudgetAllocation).when(service).getBudgetAllocation(mockAppSessionBean, mockUserSettings);
		when(mockBudgetAllocation.getCurrentBudget()).thenReturn(mockAllocationQuantitiesCompositeVO);
		when(mockAllocationQuantitiesCompositeVO.getRemainingQuantity()).thenReturn(0d);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(false);

		String errorMsg = service.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true);

		assertFalse(errorMsg.isEmpty());
	}


}