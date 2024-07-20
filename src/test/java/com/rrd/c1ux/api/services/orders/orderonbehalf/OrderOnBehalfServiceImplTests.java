/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/05/23	L De Leon			CAP-45653				Initial Version
 *	12/19/23	L De Leon			CAP-45939				Added test that_getOOBInfo_search_success()
 *	12/22/23 	Satishkumar A		CAP-45709				C1UX BE - Set OOB Mode for CustomPoint session
 *	02/08/24 	Satishkumar A		CAP-46959				C1UX BE - Handle OrderOnBehalf (API- toggleoob) to swap to requestors without checking isAllowOrderOnBehalf flag
 */
package com.rrd.c1ux.api.services.orders.orderonbehalf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.orders.oob.OOBRequest;
import com.rrd.c1ux.api.models.orders.oob.OOBResponse;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchRequest;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchResponse;
import com.rrd.custompoint.framework.util.objectfactory.ComponentObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.gwt.orderonbehalf.client.OrderOnBehalfSearchCriteria;
import com.rrd.custompoint.gwt.orderonbehalf.client.OrderOnBehalfSearchResult;
import com.rrd.custompoint.orderentry.entity.OrderOnBehalfRecords;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.ProfileSearchCriteriaVO;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOrderOnBehalfComponent;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.ao.OrderOnBehalfAssembler;
import com.wallace.atwinxs.orderentry.ao.OrderOnBehalfUtil;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderonbehalf.session.OrderOnBehalfSelectUserFormBean;
import com.wallace.atwinxs.orderonbehalf.session.OrderOnBehalfSessionBean;

class OrderOnBehalfServiceImplTests extends BaseOEServiceTest {


	private static final String FAIL = "Failed";
	private static final String TEST_USER_ID = "USER_ID";
	private static final String TEST_PROFILE_ID = "PROFILE_ID";
	private static final String TEST_NAME = "NAME";
	private static final int TEST_SITE_ID = 1234;
	private static final int TEST_BU_ID = 4321;
	private static final String TEST_GROUP_NAME = "USER_GROUP";
	private static final int TEST_PROFILE_NBR = 12345;
	private static final String TEST_129_CHARS = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
	private static final String TEST_SHARED_ID = "SHARED_ID";
	private static final String TEST_SHARED_ID_DESC = "SHARED_ID_DESC";

	private OrderOnBehalfSearchResponse orderOnBehalfSearchResponse;
	private OOBResponse oobResponse;
	private List<OrderOnBehalfSearchResult> oobUsers = new ArrayList<>();
	private List<String[]> loginIdList = new ArrayList<>();

	@Mock
	private ComponentObjectMap mockComponentObjectMap;

	@Mock
	private EntityObjectMap mockEntityObjectMap;

	@Mock
	private OrderOnBehalfSearchRequest mockOrderOnBehalfSearchRequest;

	@Mock
	private CompositeProfileBean mockCompositeProfileBean;

	@Mock
	private OrderOnBehalfRecords mockOrderOnBehalfRecords;

	@Mock
	private IOrderOnBehalfComponent mockOrderOnBehalfComponent;


	//CAP-45709
	@Mock
	private OrderOnBehalfSessionBean mockOOBSessionBean;

	@Mock
	private OrderOnBehalfSelectUserFormBean mockOOBSelectUserFormBean;

	@InjectMocks
	private OrderOnBehalfServiceImpl service;

	@BeforeEach
	void setup() throws Exception {

		orderOnBehalfSearchResponse = null;
	}

	@Test
	void that_getOOBInfo_success() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(true);
		doReturn(false).when(service).hasValidCart(mockSessionContainer);

		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getRequest());

		assertNotNull(orderOnBehalfSearchResponse);
		assertTrue(orderOnBehalfSearchResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(orderOnBehalfSearchResponse.getMessage()));
		assertTrue(orderOnBehalfSearchResponse.isInOOBMode());
	}

	// CAP-45939
	void that_getOOBInfo_search_success() throws Exception {

		OrderOnBehalfSearchResult oobUser = new OrderOnBehalfSearchResult();
		oobUsers.add(oobUser);
		loginIdList.add(new String[] {TEST_SHARED_ID, TEST_SHARED_ID_DESC});

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		doReturn(false).when(service).hasValidCart(mockSessionContainer);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockCompositeProfileBean.getGroupName()).thenReturn(TEST_GROUP_NAME);
		when(mockCompositeProfileBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockOrderOnBehalfRecords).when(mockEntityObjectMap).getEntity(OrderOnBehalfRecords.class, null);
		doReturn(new ArrayList<>()).when(mockOrderOnBehalfRecords).populate(any(OrderOnBehalfSearchCriteria.class));

		// Scenario: No oob users
		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getSearchRequest());

		assertNotNull(orderOnBehalfSearchResponse);
		assertTrue(orderOnBehalfSearchResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(orderOnBehalfSearchResponse.getMessage()));
		assertEquals(0, orderOnBehalfSearchResponse.getCount());
		assertTrue(orderOnBehalfSearchResponse.getOobSharedIdUsers().isEmpty());

		doReturn(oobUsers).when(mockOrderOnBehalfRecords).populate(any(OrderOnBehalfSearchCriteria.class));
		doReturn(mockComponentObjectMap).when(mockObjectMapFactoryService).getComponentObjectMap();
		doReturn(mockOrderOnBehalfComponent).when(mockComponentObjectMap).getComponent(IOrderOnBehalfComponent.class, null);
		doReturn(loginIdList).when(mockOrderOnBehalfComponent).getOOBSharedID(any(ProfileSearchCriteriaVO.class));

		// Scenario: At least one user id is blank
		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getSearchRequest());

		assertNotNull(orderOnBehalfSearchResponse);
		assertTrue(orderOnBehalfSearchResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(orderOnBehalfSearchResponse.getMessage()));
		assertEquals(1, orderOnBehalfSearchResponse.getCount());
		assertFalse(orderOnBehalfSearchResponse.getOobSharedIdUsers().isEmpty());

		// Scenario: All oob users have user id
		oobUser.setUserID(TEST_USER_ID);

		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getSearchRequest());

		assertNotNull(orderOnBehalfSearchResponse);
		assertTrue(orderOnBehalfSearchResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(orderOnBehalfSearchResponse.getMessage()));
		assertEquals(1, orderOnBehalfSearchResponse.getCount());
		assertTrue(orderOnBehalfSearchResponse.getOobSharedIdUsers().isEmpty());
	}

	@Test
	void that_getOOBInfo_returnErrorMessage_throwExceptionFail() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		doReturn(false).when(service).hasValidCart(mockSessionContainer);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockCompositeProfileBean.getGroupName()).thenReturn(TEST_GROUP_NAME);
		when(mockCompositeProfileBean.getProfileNumber()).thenReturn(TEST_PROFILE_NBR);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockOrderOnBehalfRecords).when(mockEntityObjectMap).getEntity(OrderOnBehalfRecords.class, null);
		doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockOrderOnBehalfRecords).populate(any(OrderOnBehalfSearchCriteria.class));

		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getSearchRequest());

		assertNotNull(orderOnBehalfSearchResponse);
		assertFalse(orderOnBehalfSearchResponse.isSuccess());
	}

	@Test
	void that_getOOBInfo_returnErrorMessage_hasAllCriteriaBlank() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		doReturn(false).when(service).hasValidCart(mockSessionContainer);

		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getSearchRequestAllBlank());

		assertNotNull(orderOnBehalfSearchResponse);
		assertFalse(orderOnBehalfSearchResponse.isSuccess());
	}

	@Test
	void that_getOOBInfo_returnErrorMessage_hasCriteriaExceedMaxLength() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		doReturn(false).when(service).hasValidCart(mockSessionContainer);

		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, getSearchRequestAllExceedMax());

		assertNotNull(orderOnBehalfSearchResponse);
		assertFalse(orderOnBehalfSearchResponse.isSuccess());
	}

	@Test
	void that_getOOBInfo_returnErrorMessage_hasNoActiveOrderFail() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		doReturn(true).when(service).hasValidCart(mockSessionContainer);

		orderOnBehalfSearchResponse = service.getOOBInfo(mockSessionContainer, mockOrderOnBehalfSearchRequest);

		assertNotNull(orderOnBehalfSearchResponse);
		assertFalse(orderOnBehalfSearchResponse.isSuccess());
	}

	@Test
	void that_getOOBInfo_returnAccessNotAllowedMessage() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(false);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getOOBInfo(mockSessionContainer, mockOrderOnBehalfSearchRequest);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	//CAP-45709
	@Test
	void that_setOrderForSelfOrOOBMode_hasValidCart() throws Exception {

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(123456);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(6);

		oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOOBOrSelfModeRequest());

		assertNotNull(oobResponse);
		assertFalse(oobResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(oobResponse.getMessage()));
	}
	//CAP-45709 CAP-46959
	@Test
	void that_setOrderForSelfOrOOBMode_profileNumber_LoginID_Blank() throws Exception {

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockUserSettings);
		oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOOBOrSelfModeRequest());

		assertNotNull(oobResponse);
		assertFalse(oobResponse.isSuccess());
		}

	}
	//CAP-45709
	@Test
	void that_setOrderForSelf_success() throws Exception {

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		try(MockedConstruction<OrderEntrySession> mockedOESession =
	    		Mockito.mockConstruction(OrderEntrySession.class, (mock, context) -> {
	    			doNothing().when(mock).init(any());
	    		}); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)	){

			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);

			oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForSelfRequest());
			assertNotNull(oobResponse);
			assertTrue(oobResponse.isSuccess());

			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenThrow(new AccessForbiddenException(FAIL));

			oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForSelfRequest());
			assertNotNull(oobResponse);
			assertFalse(oobResponse.isSuccess());

		}

	}
	//CAP-45709 CAP-46959
	@Test
	void that_setOOBMode_isAllowOrderOnBehalf_false() throws Exception {

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(false);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockUserSettings);

			assertThrows(AccessForbiddenException.class, () -> service.setOrderForSelfOrOOBMode(mockSessionContainer, setOOBOrSelfModeRequest()));
		}

	}
	//CAP-45709 CAP-46959
	@Test
	void that_setOOBMode_invalid_request() throws Exception {

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		try(MockedConstruction<OrderEntrySession> mockedOESession =
	    		Mockito.mockConstruction(OrderEntrySession.class, (mock, context) -> {
	    			doNothing().when(mock).init(any());
	    		}); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){

			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockUserSettings);
		oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForSelfInvalidRequest1());

		assertNotNull(oobResponse);
		assertFalse(oobResponse.isSuccess());

		oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForSelfInvalidRequest2());

		assertNotNull(oobResponse);
		assertFalse(oobResponse.isSuccess());

		}

	}
	//CAP-45709 CAP-46959
	@Test
	void that_setOOBMode_Validation_request() throws Exception {

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		try(MockedConstruction<OrderEntrySession> mockedOESession =
	    		Mockito.mockConstruction(OrderEntrySession.class, (mock, context) -> {
	    			doNothing().when(mock).init(any());
	    		}); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){

			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockUserSettings);
			oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForSelfValidRequest());

		assertNotNull(oobResponse);
		assertFalse(oobResponse.isSuccess());
		}

	}

	// CAP-45709 CAP-46959
	@Test
	void that_setOOBMode_Valid_request() throws Exception {

		ArrayList<String[]> criteria = new ArrayList<>();
		String[] arr = { "USER-TONY", "SharedID description" };
		criteria.add(arr);

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOESessionBean.getOobSessionBean()).thenReturn(mockOOBSessionBean);
        when(mockComponentLocator.locateLoginComponent(any())).thenReturn(mockLoginComponent);

		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(new OrderOnBehalfVO());
		when(mockAppSessionBean.getRequestorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockOOBSessionBean.getOobSelectUserCriteria()).thenReturn(mockOOBSelectUserFormBean);

		try (MockedConstruction<OrderEntrySession> mockedOESession = Mockito.mockConstruction(OrderEntrySession.class,
				(mock, context) -> {
					doNothing().when(mock).init(any());
				});
				MockedConstruction<OrderOnBehalfUtil> mockedOOBUtil = Mockito.mockConstruction(OrderOnBehalfUtil.class,
						(mock1, context) -> {
							doNothing().when(mock1).loadASBForRequestor(any(), anyInt(), any());
						});
				MockedConstruction<OrderOnBehalfAssembler> mockOOBAssembler = Mockito
						.mockConstruction(OrderOnBehalfAssembler.class, (mock2, context) -> {
							when((mock2).getOOBSharedID(any())).thenReturn(criteria);
						});

				MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)) {

			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
					.thenAnswer((Answer<Void>) invocation -> null);

			when(mockLoginComponent.getLogin(any())).thenReturn(null);

			try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
				mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockUserSettings);
				oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForOOBValidRequest());

				assertNotNull(oobResponse);
				assertFalse(oobResponse.isSuccess());

			when(mockLoginComponent.getLogin(any())).thenReturn(getTestSharedLoginVO("USER-TONY", 7125));

			oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForSelfValidRequest());

			assertNotNull(oobResponse);
			assertFalse(oobResponse.isSuccess());

			when(mockLoginComponent.getLogin(any())).thenReturn(getTestSharedLoginVO("USER-TONY", 7125));

			oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForOOBValidRequest());

			assertNotNull(oobResponse);
			assertTrue(oobResponse.isSuccess());
			}

		}

	}

	// CAP-45709 CAP-46959
	@Test
	void that_setOOBMode_Valid_request2() throws Exception {

		ArrayList<String[]> criteria = new ArrayList<>();

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOESessionBean.getOobSessionBean()).thenReturn(mockOOBSessionBean);
        when(mockComponentLocator.locateLoginComponent(any())).thenReturn(mockLoginComponent);
 		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(new OrderOnBehalfVO());
		when(mockAppSessionBean.getRequestorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedConstruction<OrderEntrySession> mockedOESession = Mockito.mockConstruction(OrderEntrySession.class,
				(mock, context) -> {
					doNothing().when(mock).init(any());
				});
				MockedConstruction<OrderOnBehalfUtil> mockedOOBUtil = Mockito.mockConstruction(OrderOnBehalfUtil.class,
						(mock1, context) -> {
							doNothing().when(mock1).loadASBForRequestor(any(), anyInt(), any());
						});
				MockedConstruction<OrderOnBehalfAssembler> mockOOBAssembler = Mockito
						.mockConstruction(OrderOnBehalfAssembler.class, (mock2, context) -> {
							when((mock2).getOOBSharedID(any())).thenReturn(criteria);
						});

				MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)) {

			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
					.thenAnswer((Answer<Void>) invocation -> null);


			when(mockLoginComponent.getLogin(any())).thenReturn(getTestSharedLoginVO("USER-TONY", 7125));

			when(mockAppSessionBean.getSiteID()).thenReturn(1234);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockAppSessionBean.getSessionID()).thenReturn(123);
			when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

			try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
				mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockUserSettings);

			when(mockLoginComponent.getLogin(any())).thenReturn(getTestLoginVO("USER-TONY", 7125));

			String[] arr = { "USER-TONY", "SharedID description" };
			criteria.add(arr);

			oobResponse = service.setOrderForSelfOrOOBMode(mockSessionContainer, setOrderForOOBValidRequest());

			assertNotNull(oobResponse);
			assertTrue(oobResponse.isSuccess());
			}
		}

	}

	private OrderOnBehalfSearchRequest getSearchRequestAllBlank() {
		OrderOnBehalfSearchRequest orderOnBehalfSearchRequest = new OrderOnBehalfSearchRequest();
		orderOnBehalfSearchRequest.setSearch(true);
		return orderOnBehalfSearchRequest;
	}

	private OrderOnBehalfSearchRequest getSearchRequestAllExceedMax() {
		OrderOnBehalfSearchRequest orderOnBehalfSearchRequest = new OrderOnBehalfSearchRequest();
		orderOnBehalfSearchRequest.setSearch(true);
		orderOnBehalfSearchRequest.setFirstName(TEST_129_CHARS);
		orderOnBehalfSearchRequest.setLastName(TEST_129_CHARS);
		orderOnBehalfSearchRequest.setUserID(TEST_129_CHARS);
		orderOnBehalfSearchRequest.setProfileID(TEST_129_CHARS);
		return orderOnBehalfSearchRequest;
	}

	private OrderOnBehalfSearchRequest getSearchRequest() {
		OrderOnBehalfSearchRequest orderOnBehalfSearchRequest = new OrderOnBehalfSearchRequest();
		orderOnBehalfSearchRequest.setSearch(true);
		orderOnBehalfSearchRequest.setFirstName(AtWinXSConstant.EMPTY_STRING);
		orderOnBehalfSearchRequest.setLastName(TEST_NAME);
		orderOnBehalfSearchRequest.setUserID(TEST_USER_ID);
		orderOnBehalfSearchRequest.setProfileID(TEST_PROFILE_ID);
		return orderOnBehalfSearchRequest;
	}

	private OrderOnBehalfSearchRequest getRequest() {
		OrderOnBehalfSearchRequest orderOnBehalfSearchRequest = new OrderOnBehalfSearchRequest();
		orderOnBehalfSearchRequest.setSearch(false);
		return orderOnBehalfSearchRequest;
	}
	//CAP-45709
	private OOBRequest setOOBOrSelfModeRequest() {
		OOBRequest oobRequest = new OOBRequest();
		return oobRequest;
	}
	//CAP-45709
	private OOBRequest setOrderForSelfRequest() {
		OOBRequest oobRequest = new OOBRequest();
		oobRequest.setOrderForSelf(true);
		return oobRequest;
	}
	private OOBRequest setOrderForSelfInvalidRequest1() {
		OOBRequest oobRequest = new OOBRequest();
		oobRequest.setOrderForSelf(false);
		return oobRequest;
	}
	private OOBRequest setOrderForSelfInvalidRequest2() {
		OOBRequest oobRequest = new OOBRequest();
		oobRequest.setOrderForSelf(false);
		oobRequest.setLoginID("ABC");
		oobRequest.setProfileNumber("a10000");
		return oobRequest;
	}
	//CAP-45709
	private OOBRequest setOrderForSelfValidRequest() {
		OOBRequest oobRequest = new OOBRequest();
		oobRequest.setOrderForSelf(false);
		oobRequest.setLoginID("ABC");
		oobRequest.setProfileNumber("100000");
		return oobRequest;
	}
	//CAP-45709
	private OOBRequest setOrderForOOBValidRequest() {
		OOBRequest oobRequest = new OOBRequest();
		oobRequest.setOrderForSelf(false);
		oobRequest.setLoginID("USER-TONY");
		oobRequest.setProfileNumber("1");
		return oobRequest;
	}

}