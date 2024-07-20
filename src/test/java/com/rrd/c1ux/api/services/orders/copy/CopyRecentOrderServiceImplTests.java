/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date					Modified By				JIRA#						Description
 *	--------				-----------				-----------------------		--------------------------------
 *	08/01/23				L De Leon				CAP-42519					Initial Version
 *	08/15/23				L De Leon				CAP-42519					Added that_copyRecentOrder_returnErrorMessage_isSubmittedBySameProfileFail()
 *	03/21/24				L De Leon				CAP-47969					Added test methods for handling budget allocations
 */
package com.rrd.c1ux.api.services.orders.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderResponse;
import com.rrd.c1ux.api.services.budgetallocation.BudgetAllocationService;
import com.rrd.custompoint.gwt.ordersearch.widget.CopyOrderResult;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.admin.vo.OrderAttributeVO;
import com.wallace.atwinxs.orderentry.session.OEReplacementsSessionBean;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;

class CopyRecentOrderServiceImplTests extends BaseOEServiceTest {


	private static final int TEST_ORDER_ID = 604534;
	private static final String TEST_USER_ID = "TEST_USER";
	private static final int TEST_PROFILE_NBR = 12345;

	public static final String EXCLUDE_SETUP_METHOD = "EXCLUDE_SETUP_METHOD";

	private CopyRecentOrderResponse copyRecentOrderResponse;
	private CopyRecentOrderRequest copyRecentOrderRequest;

	@Mock
	private ArrayList<OrderAttributeVO> mockOrderAttrVOs;

	@Mock
	private HashMap<String, String> mockSelectedMASSSiteAttribute;

	@Mock
	private HashMap<Integer, SiteAttrValuesVO[]> mockSiteAttribMap;

	@Mock
	private Order mockOrder;

	@Mock
	private OEReplacementsSessionBean replacements;

	@Mock
	private OrderHeaderVO mockOrderHeaderVo;

	// CAP-47969
	@Mock
	BudgetAllocationService mockBudgetAllocationService;

	@InjectMocks
	private CopyRecentOrderServiceImpl service;

	@BeforeEach
	void setup(TestInfo info) throws Exception {

		if (info.getTags().isEmpty()) {
			// add mock Objects for sessions
			setUpModuleSession();
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
			copyRecentOrderRequest = new CopyRecentOrderRequest(TEST_ORDER_ID);
			copyRecentOrderResponse = null;
		}
	}

	@Test
	void that_copyRecentOrder_success() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(CopyRecentOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderSubmittedBySameProfile(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(mockSavedOrderAssembler).when(service).getSavedOrderAssembler(mockAppSessionBean,
				mockVolatileSessionBean);
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
		doReturn(mockOrderAttrVOs).when(mockSavedOrderAssembler).getOrderAttrsByOrderId(anyInt());
		doReturn(mockSelectedMASSSiteAttribute).when(mockSavedOrderAssembler).getMassAttributeMap(mockOrderAttrVOs);
		doNothing().when(mockVolatileSessionBean).setSelectedMASSSiteAttribute(mockSelectedMASSSiteAttribute);
		doReturn(true).when(mockSavedOrderAssembler).validateOrdAttrWithProfileAttr(any(ArrayList.class),
				any(ArrayList.class), any(HashMap.class), anyInt());
		doReturn(mockSiteAttribMap).when(mockSavedOrderAssembler).getSiteAttribMap(any(ArrayList.class),
				eq(mockAppSessionBean));
		doNothing().when(mockSavedOrderAssembler).loadOrderAttributeForValidOrder(mockSiteAttribMap, mockOEOrderSession,
				mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockOrder).when(mockEntityObjectMap).getEntity(Order.class, null);
		doNothing().when(mockOrder).populate(anyInt());
		doReturn(false).when(mockSavedOrderAssembler).copyRecentOrder(eq(AtWinXSConstant.DEFAULT_SITE_ID), eq(null),
				eq(TEST_ORDER_ID), eq(mockOEOrderSession), eq(new ArrayList<String>()), eq(mockAppSessionBean),
				eq(false), eq(AtWinXSConstant.EMPTY_STRING), eq(false), eq(null), eq(false), eq(false),
				any(OEReplacementsSessionBean.class), eq(mockVolatileSessionBean), eq(false));
		when(mockVolatileSessionBean.getOrderId()).thenReturn(TEST_ORDER_ID);
		doNothing().when(service).persistInSession(mockApplicationVolatileSession);

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertTrue(copyRecentOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(copyRecentOrderResponse.getMessage()));
	}

	@Test
	void that_copyRecentOrder_returnErrorMessage_hasExistingOrderFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(TEST_ORDER_ID);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_IN_PROGRESS_DEF_ERR_MSG, copyRecentOrderResponse.getMessage());
	}

	@Test
	void that_copyRecentOrder_returnErrorMessage_isSubmittedOrderFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(CopyRecentOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		when(mockOEManageOrdersComponentLocatorService.locate(null)).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderHeader(isA(OrderHeaderVOKey.class))).thenReturn(null);

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_SUBMITTED_DEF_ERR_MSG, copyRecentOrderResponse.getMessage());
	}

	@Test
	void that_copyRecentOrder_returnErrorMessage_isSubmittedBySameProfileFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(CopyRecentOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(null, mockOrderHeaderVo).when(service).getOrderHeader(anyInt());
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER_ID);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		when(mockOrderHeaderVo.getLoginID()).thenReturn(AtWinXSConstant.EMPTY_STRING ,TEST_USER_ID);
		when(mockOrderHeaderVo.getProfileNum()).thenReturn(AtWinXSConstant.INVALID_ID);

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG,
				copyRecentOrderResponse.getMessage());

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG,
				copyRecentOrderResponse.getMessage());

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG,
				copyRecentOrderResponse.getMessage());

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG,
				copyRecentOrderResponse.getMessage());
	}

	@Test
	void that_copyRecentOrder_returnErrorMessage_throwExceptionFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(CopyRecentOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(mockOrderHeaderVo).when(service).getOrderHeader(anyInt());
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER_ID);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		when(mockOrderHeaderVo.getLoginID()).thenReturn(TEST_USER_ID);
		when(mockOrderHeaderVo.getProfileNum()).thenReturn(TEST_PROFILE_NBR);
		doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(service).handleCopyOrder(TEST_ORDER_ID,
				mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean);

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertEquals(SFTranslationTextConstants.ORDER_NOT_COPIED_AT_THIS_TIME_DEF_ERR_MSG,
				copyRecentOrderResponse.getMessage());
	}

	@Test
	void that_copyRecentOrder_returnErrorMessage_handleCopyExceptionFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(CopyRecentOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderSubmittedBySameProfile(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(mockSavedOrderAssembler).when(service).getSavedOrderAssembler(mockAppSessionBean,
				mockVolatileSessionBean);
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(false);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockOrder).when(mockEntityObjectMap).getEntity(Order.class, null);
		doNothing().when(mockOrder).populate(anyInt());
		doReturn(AtWinXSConstant.ORDERS_SERVICE_ID).when(mockOrder).getProfileNumber();
		doReturn(AtWinXSConstant.DEFAULT_SITE_ID).when(mockAppSessionBean).getProfileNumber();

		doReturn(true).when(mockSavedOrderAssembler).copyRecentOrder(eq(AtWinXSConstant.DEFAULT_SITE_ID), eq(null),
				eq(TEST_ORDER_ID), eq(mockOEOrderSession), eq(new ArrayList<String>()), eq(mockAppSessionBean),
				eq(false), eq(AtWinXSConstant.EMPTY_STRING), eq(false), eq(null), eq(false), eq(true),
				any(OEReplacementsSessionBean.class), eq(mockVolatileSessionBean), eq(false));
		doNothing().when(service).persistInSession(mockApplicationVolatileSession);
		doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
				any(String.class));

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(copyRecentOrderResponse.getMessage()));

		doReturn(false).when(mockSavedOrderAssembler).copyRecentOrder(eq(AtWinXSConstant.DEFAULT_SITE_ID), eq(null),
				eq(TEST_ORDER_ID), eq(mockOEOrderSession), eq(new ArrayList<String>()), eq(mockAppSessionBean),
				eq(false), eq(AtWinXSConstant.EMPTY_STRING), eq(false), eq(null), eq(false), eq(true),
				any(OEReplacementsSessionBean.class), eq(mockVolatileSessionBean), eq(false));
		when(mockVolatileSessionBean.getOrderId()).thenReturn(TEST_ORDER_ID);
		doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(service).copyOrderSaveSessions(
				eq(mockAppSessionBean), any(), eq(new ArrayList<String>()), eq(mockVolatileSessionBean), any());

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(copyRecentOrderResponse.getMessage()));
	}

	@Test
	void that_copyRecentOrder_returnErrorMessage_handleCopyErrorFail() throws Exception {

		service = Mockito.spy(service);

		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		doReturn(true).when(service).validateNoOrderInProgress(any(CopyRecentOrderResponse.class),
				eq(mockSessionContainer), eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderExistandSubmitted(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(true).when(service).validateOrderSubmittedBySameProfile(any(CopyRecentOrderResponse.class), anyInt(),
				eq(mockAppSessionBean));
		doReturn(mockSavedOrderAssembler).when(service).getSavedOrderAssembler(mockAppSessionBean,
				mockVolatileSessionBean);
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
		doReturn(null).when(mockSavedOrderAssembler).getOrderAttrsByOrderId(anyInt());
		doReturn(mockSelectedMASSSiteAttribute).when(mockSavedOrderAssembler).getMassAttributeMap(null);
		doNothing().when(mockVolatileSessionBean).setSelectedMASSSiteAttribute(mockSelectedMASSSiteAttribute);
		doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
				any(String.class));

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(copyRecentOrderResponse.getMessage()));

		doReturn(mockOrderAttrVOs).when(mockSavedOrderAssembler).getOrderAttrsByOrderId(anyInt());
		doReturn(mockSelectedMASSSiteAttribute).when(mockSavedOrderAssembler).getMassAttributeMap(mockOrderAttrVOs);
		doNothing().when(mockVolatileSessionBean).setSelectedMASSSiteAttribute(mockSelectedMASSSiteAttribute);
		doReturn(false).when(mockSavedOrderAssembler).validateOrdAttrWithProfileAttr(any(ArrayList.class),
				any(ArrayList.class), any(HashMap.class), anyInt());

		copyRecentOrderResponse = service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);

		assertNotNull(copyRecentOrderResponse);
		assertFalse(copyRecentOrderResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(copyRecentOrderResponse.getMessage()));
	}

	@Test
	void that_copyRecentOrder_returnAccessNotAllowedMessage_quickCopySettingOff() throws Exception {

		// set quick copy setting to false, throw 403
		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_copyRecentOrder_returnAccessNotAllowedMessage_punchoutActive() throws Exception {

		// set punchout to true, throw 403
		when(mockUserSettings.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(true);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.copyRecentOrder(mockSessionContainer, copyRecentOrderRequest);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_processNonTemplateResult_hasReplacements_success() throws Exception {

		service = Mockito.spy(service);

		CopyOrderResult copyOrderResult = new CopyOrderResult();
		List<String> errorMessages = new ArrayList<>();
		ArrayList<String> errorlist = new ArrayList<>();
		errorlist.add(GENERIC_ERROR_MSG);

		when(replacements.isEmpty()).thenReturn(true, false);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(null);
		doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
				any(String.class));

		service.processNonTemplateResult(mockAppSessionBean, copyOrderResult, errorMessages, mockVolatileSessionBean,
				TEST_ORDER_ID, errorlist, replacements);

		assertFalse(copyOrderResult.isRedirectToCart());
		assertFalse(copyOrderResult.getError().isEmpty());

		service.processNonTemplateResult(mockAppSessionBean, copyOrderResult, errorMessages, mockVolatileSessionBean,
				TEST_ORDER_ID, errorlist, replacements);

		assertFalse(copyOrderResult.isRedirectToCart());
		assertFalse(copyOrderResult.getError().isEmpty());
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_copyOrderSaveSessions_hasErrors_success() throws Exception {

		service = Mockito.spy(service);

		CopyOrderResult copyOrderResult = new CopyOrderResult();
		List<String> errorMessages = new ArrayList<>();
		errorMessages.add(GENERIC_ERROR_MSG);
		errorMessages.add(GENERIC_ERROR_MSG);
		copyOrderResult.setError(errorMessages);
		ArrayList<String> errorlist = new ArrayList<>();
		errorlist.add(GENERIC_ERROR_MSG);

		doReturn(mockSessionContainer).when(service).getSessionContainer(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(mockOESession).when(service).loadOESession(anyInt(), eq(mockAppSessionBean), any(Boolean.class));
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		doNothing().when(mockOEOrderSession).setUsabilityRedirectWarningMessage(any(String.class));
		doNothing().when(mockApplicationVolatileSession).putParameter(any(String.class), any());
		doNothing().when(service).saveSession(any(), anyInt(), anyInt());

		service.copyOrderSaveSessions(mockAppSessionBean, copyOrderResult, errorlist, mockVolatileSessionBean,
				LoggerFactory.getLogger(CopyRecentOrderServiceImplTests.class));

		assertTrue(copyOrderResult.isRedirectToCart());
		assertFalse(copyOrderResult.getError().isEmpty());
	}

	// CAP-47969 Start
	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_handleBudgetAllocations_has_noErrorMsg() throws Exception {

		service = Mockito.spy(service);

		CopyOrderResult copyOrderResult = new CopyOrderResult();
		copyOrderResult.setRedirectToCart(false);

		when(mockBudgetAllocationService.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, false)).thenReturn(AtWinXSConstant.EMPTY_STRING);

		service.handleBudgetAllocations(mockOEOrderSession, mockAppSessionBean, copyOrderResult);

		assertTrue(copyOrderResult.getError().isEmpty());
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_handleBudgetAllocations_has_errorMsg() throws Exception {

		service = Mockito.spy(service);

		CopyOrderResult copyOrderResult = new CopyOrderResult();

		when(mockBudgetAllocationService.validateBudgetAllocations(mockOEOrderSession, mockAppSessionBean, true)).thenReturn(GENERIC_ERROR_MSG);

		service.handleBudgetAllocations(mockOEOrderSession, mockAppSessionBean, copyOrderResult);

		assertFalse(copyOrderResult.getError().isEmpty());
	}
	// CAP-47969 End
}