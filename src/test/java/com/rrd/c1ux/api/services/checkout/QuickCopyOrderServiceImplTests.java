/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date					Modified By				JIRA#						Description
 *	--------				-----------				-----------------------		--------------------------------
 *	06/28/23				L De Leon				CAP-41373					Initial Version
 *	03/22/24				L De Leon				CAP-47969					Added test methods for validating budget allocations
 */
package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderResponse;
import com.rrd.c1ux.api.services.budgetallocation.BudgetAllocationService;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEReplacementsSessionBean;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;

class QuickCopyOrderServiceImplTests extends BaseOEServiceTest {


	private static final int TEST_ORDER_ID = 604534;

	private QuickCopyOrderResponse quickCopyOrderResponse;
	private QuickCopyOrderRequest quickCopyOrderRequest;

	// CAP-47969
	public static final String EXCLUDE_SETUP_METHOD = "EXCLUDE_SETUP_METHOD";

	@Mock
	BudgetAllocationService mockBudgetAllocationService;

	@InjectMocks
	private QuickCopyOrderServiceImpl service;

	@BeforeEach
	void setup(TestInfo info) throws Exception {

		if (info.getTags().isEmpty()) {
			// add mock Objects for sessions
			setUpModuleSession();
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
			quickCopyOrderRequest = new QuickCopyOrderRequest(TEST_ORDER_ID);
			quickCopyOrderResponse = null;
		}
	}

	@Test
	void that_quickCopyOrder_success() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(QuickCopyOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(QuickCopyOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		when(mockVolatileSessionBean.getLastSubmittedOrderID()).thenReturn(TEST_ORDER_ID);
		doReturn(mockSavedOrderAssembler).when(service).getSavedOrderAssembler(mockAppSessionBean,
				mockVolatileSessionBean);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(false);
		when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		doReturn(false).when(mockSavedOrderAssembler).orderLocksListFeedRecords(anyInt(), any(ArrayList.class),
				eq(mockAppSessionBean));
		doReturn(false).when(mockSavedOrderAssembler).copyRecentOrder(anyInt(), anyString(), anyInt(),
				eq(mockOEOrderSession), any(ArrayList.class), eq(mockAppSessionBean), anyBoolean(), anyString(),
				anyBoolean(), eq(null), anyBoolean(), anyBoolean(), any(OEReplacementsSessionBean.class),
				eq(mockVolatileSessionBean), anyBoolean());
		doNothing().when(mockVolatileSessionBean).clearLastSubmittedOrderID();
		doNothing().when(service).persistInSession(mockApplicationVolatileSession);

		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertTrue(quickCopyOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(quickCopyOrderResponse.getMessage()));
	}

	@Test
	void that_quickCopyOrder_returnErrorMessage_hasExistingOrderFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(TEST_ORDER_ID);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);

		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertFalse(quickCopyOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_IN_PROGRESS_DEF_ERR_MSG, quickCopyOrderResponse.getMessage());
	}

	@Test
	void that_quickCopyOrder_returnErrorMessage_isSubmittedOrderFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(QuickCopyOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		when(mockOEManageOrdersComponentLocatorService.locate(null)).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderHeader(isA(OrderHeaderVOKey.class))).thenReturn(null);

		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertFalse(quickCopyOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_SUBMITTED_DEF_ERR_MSG, quickCopyOrderResponse.getMessage());
	}

	@Test
	void that_quickCopyOrder_returnErrorMessage_matchedOrderIdFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(QuickCopyOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(QuickCopyOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		when(mockVolatileSessionBean.getLastSubmittedOrderID()).thenReturn(AtWinXSConstant.INVALID_ID, 1);

		// last submitted order ID from session is invalid
		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertFalse(quickCopyOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG, quickCopyOrderResponse.getMessage());

		// last submitted order ID from session does not match requested order ID
		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertFalse(quickCopyOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG, quickCopyOrderResponse.getMessage());
	}

	@Test
	void that_quickCopyOrder_returnErrorMessage_throwExceptionFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(QuickCopyOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(QuickCopyOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		when(mockVolatileSessionBean.getLastSubmittedOrderID()).thenReturn(TEST_ORDER_ID);
		doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(service).handleCopyOrder(eq(mockOESession),
				eq(mockOEOrderSession), eq(mockAppSessionBean), eq(mockVolatileSessionBean),
				any(QuickCopyOrderResponse.class));

		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertFalse(quickCopyOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_AT_THIS_TIME_DEF_ERR_MSG,
				quickCopyOrderResponse.getMessage());
	}

	@Test
	void that_quickCopyOrder_returnErrorMessage_handleCopyErrorFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(QuickCopyOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(QuickCopyOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		when(mockVolatileSessionBean.getLastSubmittedOrderID()).thenReturn(TEST_ORDER_ID);
		doReturn(mockSavedOrderAssembler).when(service).getSavedOrderAssembler(mockAppSessionBean,
				mockVolatileSessionBean);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);
		doReturn(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG).when(service)
				.getTranslation(eq(mockAppSessionBean), anyString(), anyString());

		quickCopyOrderResponse = service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);

		assertNotNull(quickCopyOrderResponse);
		assertFalse(quickCopyOrderResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(quickCopyOrderResponse.getMessage()));
	}

	@Test
	void that_quickCopyOrder_returnAccessNotAllowedMessage_quickCopySettingOff() throws Exception {

		// set quick copy setting to false, throw 403
		when(mockUserSettings.isAllowQuickCopy()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_quickCopyOrder_returnAccessNotAllowedMessage_punchoutActive() throws Exception {

		// set punchout to true, throw 403
		when(mockUserSettings.isAllowQuickCopy()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(true);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.quickCopyOrder(mockSessionContainer, quickCopyOrderRequest);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	// CAP-47969 Start
	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_handleBudgetAllocations_has_noErrorMsg() throws Exception {

		service = Mockito.spy(service);

		Message message = new Message();

		when(mockBudgetAllocationService.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false)).thenReturn(AtWinXSConstant.EMPTY_STRING);

		String errorMsg = service.handleBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false, message);

		assertTrue(Util.isBlankOrNull(errorMsg));
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_handleBudgetAllocations_has_noBudgetErrorMsg() throws Exception {

		service = Mockito.spy(service);

		Message message = new Message();
		message.setErrGeneralMsg(GENERIC_ERROR_MSG);

		when(mockBudgetAllocationService.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false)).thenReturn(AtWinXSConstant.EMPTY_STRING);

		String errorMsg = service.handleBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false, message);

		assertFalse(Util.isBlankOrNull(errorMsg));
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_handleBudgetAllocations_has_budgetErrorMsg() throws Exception {

		service = Mockito.spy(service);

		Message message = new Message();
		message.setErrGeneralMsg(GENERIC_ERROR_MSG);
		message.setErrMsgItems(Collections.emptyList());

		when(mockBudgetAllocationService.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true)).thenReturn(GENERIC_ERROR_MSG);

		String errorMsg = service.handleBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true, message);

		assertFalse(Util.isBlankOrNull(errorMsg));
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_handleBudgetAllocations_has_allErrorMsg() throws Exception {

		service = Mockito.spy(service);

		Message message = new Message();
		message.setErrGeneralMsg(GENERIC_ERROR_MSG);
		Collection<String> errorMsgs = new ArrayList<>();
		errorMsgs.add(GENERIC_ERROR_MSG);
		message.setErrMsgItems(errorMsgs);

		when(mockBudgetAllocationService.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true)).thenReturn(GENERIC_ERROR_MSG);

		service.handleBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true, message);

		assertFalse(Util.isBlankOrNull(message.getErrGeneralMsg()));
	}
	// CAP-47969 End
}