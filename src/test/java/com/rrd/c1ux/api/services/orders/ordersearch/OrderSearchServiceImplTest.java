/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions:
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  6/05/2023	C Codina		CAP-39049					Address Concurrency issues in OrderSearch Service
 *  06/28/23	M Sakthi		CAP-40615					SonarQube: Bugs in OrderSearchServiceImpl
 *  7/06/2023	C Codina		CAP-38766					Sequence number should match with the one in shipment level and Item desc & Item Number should not populate when the Order line number is not passed
 *  07/27/23	Satishkumar A	CAP-41553					C1UX BE - API Change - Order Search Results Listing changes needed for new copy order flags
 *  08/22/23	S Ramachandran	CAP-43234					Get order routing information for Order Search
 *  08/30/23	Satishkumar A	CAP-43283					C1UX BE - Routing Information For Justification Section on Review Order Page
 *  11/13/23	T Harmon		CAP-45241					Fixed test issue with new OrderSearchServiceImpl code
 *  11/20/23	N Caceres		CAP-45040					JUnits for adding customer reference 1 - 3 search criteria
 *  11/21/23	C Codina		CAP-45054					Added Junits for Wcss Order Number
 *  12/05/23	Krishna Natarajan CAP-45058					Added code to the test method to handle messages
 *  01/29/23	Krishna Natarajan CAP-46677					Added a mock for Profile class to return mockProfile
 *  04/12/23	S Ramachandran	CAP-48488					Modify Order Search to show expedite information if an order is expedited.
 *  05/16/24	Krishna Natarajan CAP-49259					Added mock for the Order Class whereever required
 */
package com.rrd.c1ux.api.services.orders.ordersearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentAndTrackingResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentDetail;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchCriteria;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchNavigationResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResultResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COSharedSearchCriteriaRequest;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationRequest;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationResponse;
import com.rrd.c1ux.api.util.OrderSearchUtil;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderStatusMessage;
import com.rrd.custompoint.orderentry.entity.OrderStatusMessage.Type;
import com.rrd.custompoint.orderentry.entity.OrderStatusMessageImpl;
import com.rrd.custompoint.orderentry.entity.OrderStatusMessages;
import com.rrd.custompoint.orderentry.entity.RAOrderInformation;
import com.rrd.custompoint.orderentry.entity.RAOrderInformationImpl;
import com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBean;
import com.rrd.custompoint.orderstatus.ao.RAOrderDetailBean;
import com.rrd.custompoint.orderstatus.entity.OSDetailsItemsOrdered;
import com.rrd.custompoint.orderstatus.entity.OrderStatusCodes;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearch;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchCriteria;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchResult;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchResultImpl;
import com.rrd.custompoint.orderstatus.entity.OrderStatusTrackingInfo;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.util.LookAndFeelFeature;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVOKey;

class OrderSearchServiceImplTest extends BaseServiceTest {
	private COOSFullDetailResponse coOSFullDetailResponse=new COOSFullDetailResponse();
	private COOSFullDetailRequest coOSFullDetailRequest=getCOOSFullDetailRequest();

	@InjectMocks
	OrderSearchServiceImpl orderSearchServiceImpl;

	@Mock
	SimpleDateFormat mockSDF;

	@Mock
	OrderStatusSearchCriteria cpSearchCriteria ;

	@Mock
	COOrderSearchRequest mockSearchRequest;

	@Mock
	COOrderSearchResultResponse mockSearchResultResponse;

	@Mock
	OrderStatusMessage mockOrderStatusMessage;

	@Mock
	OrderStatusMessages mockOrderStatusMessages;

	@Mock
	private OrderHeaderVO mockOrderHeaderVo;

	@Mock
	private OrderHeaderVOKey mockOrderHeaderVOKey;
	
	@Mock
	private OrderShippingVO mockOrderShippingVO;

	private static final String ENCODED_SESSION_ID = "TEST";
	private static final int TEST_APP_SESS_ID= 12345;
	private static final int TEST_ORDER_SERV_ID = 4567;
	private static final String TEST_WCSS_ORDER_NUM = "99592065";
	private static final String TEST_SEQ_NO = "0004";
	private static final String TEST_SALES_REF_NUM = "80031755";
	private static final int TEST_ORDER_LINE_NUM = -1;
	private static final int TEST_ORDER_FOUND_ONE = 1;


	private static final int TEST_QUEUE_ID= 2642;
	private static final String TEST_CHANGE_USER_ID="IDC-CP-USER2";
	private static final String TEST_CREATE_USER_ID="IDC-CP-USER2";
	private static final String TEST_lOGIN_USER_ID="IDC-CP-USER2";
	private static final String TEST_JUSTIFICATION="Routed to Line level Approval to IDC-CP-USER2 SUBBUand IDC-CP-USER1 SUBBU\r\nAnd Order Level Approval to IDC-CP-USER1 SUBBU";

	private static final int TEST_ORDER_ID = 609614;
	private static final String TEST_RESULT_APPROVED = "Line Approved";
	private static final String TEST_RESULT_PENDING = "Pending Approval";
	private static final String TEST_YOURITEM = "1018";
	private static final String TEST_ROUTING_REASON ="Ordered item requiring line review.";
	private static final String TEST_APPROVER_NAME ="IDC-CP-USER2 SUBBU (IDC-CP-USER2)";
	private static final String TEST_APPROVER_MESSAGE ="Line Item Level- Approved by IDC-CP-USER2";
	private static final String TEST_DB_ERROR = "DB Error- Please contact your Administrator.";

	private static final String TEST_USER_ID = "TEST_USER";
	private static final int TEST_PROFILE_NBR = 12345;

	@BeforeEach
	void setUp() throws Exception {
		coOSFullDetailResponse=new COOSFullDetailResponse();


	}

	@Test
	void testGetTrackingDetails() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		COOSShipmentAndTrackingResponse detail = new COOSShipmentAndTrackingResponse();
		Collection<OrderStatusTrackingInfo> orderStatusTrackingInfos  = new ArrayList<>();

		orderStatusTrackingInfos.add(mockOrderStatusTrackingInfo);

		detail.setOrderNo("test_order_num");
		detail.setSalesRefNum("test_sales_ref_num");
		detail.setShowPrice(false);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getEncodedSessionId()).thenReturn(ENCODED_SESSION_ID);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		doNothing().when(mockOrderStatusSession).setClearParameters(false);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrderSearchDetailFromBean).thenReturn(mockOrderStatusTrackingInfo);

		when(mockDetailRequest.getOrderNum()).thenReturn("test_order_num");
		when(mockDetailRequest.getSalesReferenceNumber()).thenReturn("test_sales_ref_num");
		when(mockOrderStatusTrackingInfo.getOrderStatusShipmentAndTrackingInfo(ENCODED_SESSION_ID, mockDetailRequest.getOrderNum(), mockDetailRequest.getSalesReferenceNumber())).thenReturn(orderStatusTrackingInfos);

		COOSShipmentAndTrackingResponse response = orderSearchServiceImpl.getTrackingDetails(mockSessionContainer, mockDetailRequest);

		assertEquals(detail.getOrderNo(), response.getOrderNo());
		assertEquals(detail.getSalesRefNum(), response.getSalesRefNum());
		assertEquals(detail.isShowPrice(), response.isShowPrice());

	}
	@Test
	void testGetItemsInShipmentDetails() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getTtSession()).thenReturn(mockTTSession);
		when(mockTTSession.getId()).thenReturn(TEST_APP_SESS_ID);
		when(mockAppSessionBean.getEncodedSessionId()).thenReturn(ENCODED_SESSION_ID);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);

		COOSShipmentDetail response = orderSearchServiceImpl.getItemsInShipmentDetails(mockSessionContainer, TEST_WCSS_ORDER_NUM, TEST_SEQ_NO, TEST_SALES_REF_NUM, TEST_ORDER_LINE_NUM);

		assertNotNull(response);
	}

	@Test
	void testPopulateOrderDetailforNavigation() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)	){
			COOrderSearchNavigationResponse response =orderSearchServiceImpl.populateOrderDetailforNavigation(mockSessionContainer);
			assertTrue(response.isSuccess());
		}
	}

	@Test
	void testGetSearchOrdersDetail() throws AtWinXSException, ParseException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSDF.parse(any())).thenReturn(new java.util.Date());

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusCodes.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusCodes);

		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<OrderSearchUtil> mockOrderSearchUtil = Mockito.mockStatic(OrderSearchUtil.class);MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockOrderSearchUtil.when(() -> OrderSearchUtil.getOrderStatusSearchCriteria(any(),
					any(), any())).thenReturn(cpSearchCriteria);
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequest());
			Assertions.assertEquals(0, response.getOrdersFound());
		}
	}


	@Test
	void testGetSearchOrdersDetail_403_exception() throws AtWinXSException, ParseException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockOsBean.isShowRecentOrders()).thenReturn(false);
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequestForRecentOrders());
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		}
	}

	@Test
	void testGetSearchOrdersDetail_exception() throws AtWinXSException, ParseException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockOsBean.getOrderStatusRestriction()).thenReturn("Y");
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusCodes.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusCodes);
		when(mockSDF.parse(any())).thenReturn(new java.util.Date());

		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class); MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"from_date_lbl_error")).thenReturn("from_date_lbl_error error message");
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"to_date_lbl_error")).thenReturn("to_date_lbl_error error message");
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"invalidErrMsg")).thenReturn("invalidErrMsg error message");
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"visibilityScopeLbl")).thenReturn("visibilityScopeLbl error message");

			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequestWithDatesEmpty());
			assertFalse(response.isSuccess());

		}
	}

	@Test
	void testGetSearchOrdersDetail_recentOrders() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getTtSession()).thenReturn(mockTTSession);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockTTSession.getId()).thenReturn(0);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockOsBean.isShowRecentOrders()).thenReturn(true);
		when(mockOsBean.isSimplifiedView()).thenReturn(true);
		when(mockOrderStatusSearch.doSearch(null, false)).thenReturn(coOrderSearchResponse());
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);

		when(mockOsBean.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);

		when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");

			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequestForRecentOrders());
			Assertions.assertEquals(TEST_ORDER_FOUND_ONE, response.getOrdersFound());
			Assertions.assertTrue( response.isSuccess());
			Assertions.assertTrue( response.isRecentOrderSearch());
		}

		when(mockOsBean.isSimplifiedView()).thenReturn(false);

		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");

			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequestForRecentOrders());
			Assertions.assertEquals(TEST_ORDER_FOUND_ONE, response.getOrdersFound());
			Assertions.assertTrue( response.isSuccess());
			Assertions.assertTrue( response.isRecentOrderSearch());
		}
		when(mockOrderStatusSearch.doSearch(null, false)).thenReturn(coOrderSearchResponseWithOrderStatusAdvncDefnTag());
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");

			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequestForRecentOrders());
			Assertions.assertEquals(TEST_ORDER_FOUND_ONE, response.getOrdersFound());
			Assertions.assertTrue( response.isSuccess());
			Assertions.assertTrue( response.isRecentOrderSearch());
		}

	}

	@Test
	void testGetSearchOrdersDetail_repeatSearch() throws AtWinXSException, ParseException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		COOrderSearchRequest request = coOrderSearchRequestForRepeatSearch();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockSDF.parse(any())).thenReturn(new java.util.Date());

		when(mockOsBean.isShowCopyRecentOrder()).thenReturn(true);
		when(mockAppSessionBean.isPunchout()).thenReturn(false);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);

		when(mockOrderStatusSession.getParameter("newSearchBean")).thenReturn("yes");

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<OrderSearchUtil> mockOrderSearchUtil = Mockito.mockStatic(OrderSearchUtil.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockOrderSearchUtil.when(() -> OrderSearchUtil.getOrderStatusSearchCriteria(any(),
					any(), any())).thenReturn(cpSearchCriteria);
			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, request);
			Assertions.assertEquals(0, response.getOrdersFound());
			Assertions.assertTrue( response.isSuccess());

		}

		when(mockOrderStatusSession.getParameter("newSearchBean")).thenReturn("no");
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<OrderSearchUtil> mockOrderSearchUtil = Mockito.mockStatic(OrderSearchUtil.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockOrderSearchUtil.when(() -> OrderSearchUtil.getOrderStatusSearchCriteria(any(),
					any(), any())).thenReturn(cpSearchCriteria);
			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, request);
			Assertions.assertEquals(0, response.getOrdersFound());
			Assertions.assertTrue( response.isSuccess());

		}

		when(mockOrderStatusSession.getParameter("newSearchBean")).thenReturn(null);
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<OrderSearchUtil> mockOrderSearchUtil = Mockito.mockStatic(OrderSearchUtil.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockOrderSearchUtil.when(() -> OrderSearchUtil.getOrderStatusSearchCriteria(any(),
					any(), any())).thenReturn(cpSearchCriteria);
			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, request);
			Assertions.assertEquals(0, response.getOrdersFound());
			Assertions.assertTrue( response.isSuccess());

		}
	}



	private HashMap<String, Integer> getCriteria() {
		HashMap<String, Integer> criteria = new HashMap<String, Integer>();
		criteria.put("CriteriaSalesRef", 80032142);
		return criteria;
	}
	private COOrderSearchRequest coOrderSearchRequest() {
		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaSalesRef");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("06/30/2023");
		request.setToDate("06/12/2023");
		request.setScope("M");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}

	private COOrderSearchRequest coOrderSearchRequestwithrecenttrue() {
		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaSalesRef");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("01/25/2023");
		request.setToDate("01/25/2023");
		request.setScope("M");
		request.setRecentOrderSearch(true);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}

	private COOrderSearchRequest coOrderSearchRequestForRecentOrders() {

		COOrderSearchRequest request=new COOrderSearchRequest();
		request.setRecentOrderSearch(true);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}

	private COOrderSearchRequest coOrderSearchRequestForRepeatSearch() {

		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaSalesRef");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("06/30/2023");
		request.setToDate("06/12/2023");
		request.setScope("M");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(true);
		return request;
	}

	private COOrderSearchRequest coOrderSearchRequestWithDatesEmpty() {
		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaSalesRef");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("");
		request.setToDate("");
		request.setScope("A");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(true);
		request.setRepeatSearch(false);
		return request;
	}
	private Collection<OrderStatusSearchResult> coOrderSearchResponse() {

		Collection<OrderStatusSearchResult> convertedResults = new ArrayList<>();
		OrderStatusSearchResult convertedResult = new OrderStatusSearchResultImpl();
		convertedResult.setSalesReferenceNbr(TEST_SALES_REF_NUM);
		convertedResult.setOrderId(TEST_ORDER_SERV_ID);
		convertedResult.setOrderDate(new java.util.Date());
		convertedResults.add(convertedResult);
		return convertedResults;
	}

	private Collection<OrderStatusSearchResult> coOrderSearchResponseWithOrderStatusAdvncDefnTag() {

		Collection<OrderStatusSearchResult> convertedResults = new ArrayList<>();
		OrderStatusSearchResult convertedResult = new OrderStatusSearchResultImpl();
		convertedResult.setSalesReferenceNbr(TEST_SALES_REF_NUM);
		convertedResult.setOrderId(TEST_ORDER_SERV_ID);
		convertedResult.setOrderDate(new java.util.Date());
		convertedResult.setOrderStatusAdvncDefnTag("test");
		convertedResults.add(convertedResult);
		return convertedResults;
	}


	@Test
	void that_getOSRoutingInfo_approved_returnsExpected() throws Exception {

		//CAP-43234	- Get order routing infos for Approved Routing
		try (

			MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class);
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);)	{

			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

			OrderRoutingInformationRequest request = new OrderRoutingInformationRequest();
			request.setOrderID(TEST_ORDER_ID);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrder);

			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isShowAssignedApprover()).thenReturn(true);

			Collection<RAOrderInformation> routingInformation = new ArrayList<RAOrderInformation>();
			RAOrderInformation objRAOrderInformation = getRAOrderInformation();
			objRAOrderInformation = Mockito.spy(objRAOrderInformation);

			Mockito.doReturn(getOSDetailsRoutingInfoBeans_approved()).when(objRAOrderInformation).getRoutingDetailBean(any(Locale.class));
			routingInformation.add(objRAOrderInformation);

			when(mockOrder.getRoutingInformation(mockAppSessionBean.getFeatureSetting(LookAndFeelFeature.FeatureNames.UXRA.toString()))).thenReturn(routingInformation);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

			OrderRoutingInformationResponse response = orderSearchServiceImpl.getOSRoutingInfos(mockSessionContainer,request);
			assertTrue(response.isSuccess());
		}
	}


	@Test
	void that_getOSRoutingInfo_pending_returnsExpected() throws Exception {

		//CAP-43234	- Get order routing infos for Pending Routing
		try (

			MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class);
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {

			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

			OrderRoutingInformationRequest request = new OrderRoutingInformationRequest();
			request.setOrderID(TEST_ORDER_ID);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrder);

			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isShowAssignedApprover()).thenReturn(true);

			Collection<RAOrderInformation> routingInformation = new ArrayList<RAOrderInformation>();
			RAOrderInformation objRAOrderInformation = getRAOrderInformation();
			objRAOrderInformation = Mockito.spy(objRAOrderInformation);

			Mockito.doReturn(getOSDetailsRoutingInfoBeans_pending()).when(objRAOrderInformation).getRoutingDetailBean(any(Locale.class));
			routingInformation.add(objRAOrderInformation);

			when(mockOrder.getRoutingInformation(mockAppSessionBean.getFeatureSetting(LookAndFeelFeature.FeatureNames.UXRA.toString()))).thenReturn(routingInformation);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

			OrderRoutingInformationResponse response = orderSearchServiceImpl.getOSRoutingInfos(mockSessionContainer,request);
			assertTrue(response.isSuccess());
		}
	}


	@Test
	void that_getOSRoutingInfo_RoutingInfoIsNull_returnsExpected() throws Exception {

		//CAP-43234	- Get order routing infos if RoutingInfo is Null
		try (

		MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class);
		MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {

		mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

		OrderRoutingInformationRequest request = new OrderRoutingInformationRequest();
		request.setOrderID(TEST_ORDER_ID);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrder);

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowAssignedApprover()).thenReturn(true);

		Collection<RAOrderInformation> routingInformation = null;

		when(mockOrder.getRoutingInformation(mockAppSessionBean.getFeatureSetting(LookAndFeelFeature.FeatureNames.UXRA.toString()))).thenReturn(routingInformation);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

		OrderRoutingInformationResponse response = orderSearchServiceImpl.getOSRoutingInfos(mockSessionContainer,request);
		assertTrue(response.isSuccess());

		}
	}


	@Test
	void that_getOSRoutingInfo_RoutingInfoIsEmpty_returnsExpected() throws Exception {

		//CAP-43234	- Get order routing infos if RoutingInfo is Empty
		try (

		MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class);
		MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {

		mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

		OrderRoutingInformationRequest request = new OrderRoutingInformationRequest();
		request.setOrderID(TEST_ORDER_ID);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrder);

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowAssignedApprover()).thenReturn(true);

		Collection<RAOrderInformation> routingInformation =  Collections.emptyList();

		when(mockOrder.getRoutingInformation(mockAppSessionBean.getFeatureSetting(LookAndFeelFeature.FeatureNames.UXRA.toString()))).thenReturn(routingInformation);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

		OrderRoutingInformationResponse response = orderSearchServiceImpl.getOSRoutingInfos(mockSessionContainer,request);
		assertTrue(response.isSuccess());
		}
	}

	@Test
	void that_getOSRoutingInfo_returns422Error() throws Exception {

		//CAP-43234	- Get order routing infos if returns422 Error
		try (

			MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class);
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {

		mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

		OrderRoutingInformationRequest request = new OrderRoutingInformationRequest();
		request.setOrderID(TEST_ORDER_ID);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrder);

		AtWinXSException e = new AtWinXSException(TEST_DB_ERROR, String.class.getName());

		when(mockOrder.getRoutingInformation(mockAppSessionBean.getFeatureSetting(
				LookAndFeelFeature.FeatureNames.UXRA.toString()))).thenThrow(e);

		OrderRoutingInformationResponse response = orderSearchServiceImpl.getOSRoutingInfos(mockSessionContainer,request);
		assertFalse(response.isSuccess());
		}
	}


	//CAP-43234	- getRAOrderInformation
	private static RAOrderInformation getRAOrderInformation() {

		RAOrderInformation objRAOrderInformation = new RAOrderInformationImpl();
		objRAOrderInformation.setApprovalDate(new Date());
		objRAOrderInformation.setApprovalQueueID(TEST_QUEUE_ID);
		objRAOrderInformation.setChangeUserID(TEST_CHANGE_USER_ID);
		objRAOrderInformation.setCreateUserID(TEST_CREATE_USER_ID);
		objRAOrderInformation.setJustificationText(TEST_JUSTIFICATION);
		objRAOrderInformation.setLoginID(TEST_lOGIN_USER_ID);
		objRAOrderInformation.setMessage(TEST_APPROVER_MESSAGE);
		objRAOrderInformation.setOrderID(TEST_ORDER_ID);

		return objRAOrderInformation;
	}


	//CAP-43234	- getOSDetailsRoutingInfoBeans_approved
	private static Collection<RAOrderDetailBean> getOSDetailsRoutingInfoBeans_approved() {

		Collection<RAOrderDetailBean> routingInfoBeans = new LinkedList<RAOrderDetailBean>();

		Collection<String> routingReasons = new ArrayList<String>();
		routingReasons.add(TEST_ROUTING_REASON);

		RAOrderDetailBean raOrderDetailBean = new RAOrderDetailBean();
		raOrderDetailBean.setResult(TEST_RESULT_APPROVED);
		raOrderDetailBean.setItemNumber(TEST_YOURITEM);
		raOrderDetailBean.setQueueDate(new Date());
		raOrderDetailBean.setRoutingReasons(routingReasons);
		raOrderDetailBean.setApprovalDate(new Date());
		raOrderDetailBean.setApprover(TEST_APPROVER_NAME);
		raOrderDetailBean.setAppoverMessage(TEST_APPROVER_MESSAGE);
		routingInfoBeans.add(raOrderDetailBean);

		return routingInfoBeans;
	}


	//CAP-43234	- getOSDetailsRoutingInfoBeans_pending
	private static Collection<RAOrderDetailBean> getOSDetailsRoutingInfoBeans_pending() {

		Collection<RAOrderDetailBean> routingInfoBeans = new LinkedList<RAOrderDetailBean>();

		Collection<String> routingReasons = new ArrayList<String>();
		routingReasons.add(TEST_ROUTING_REASON);

		RAOrderDetailBean raOrderDetailBean = new RAOrderDetailBean();
		raOrderDetailBean.setResult(TEST_RESULT_PENDING);
		raOrderDetailBean.setItemNumber(TEST_YOURITEM);
		raOrderDetailBean.setQueueDate(new Date());
		raOrderDetailBean.setRoutingReasons(routingReasons);
		raOrderDetailBean.setApprovalDate(new Date());
		raOrderDetailBean.setApprover(TEST_APPROVER_NAME);
		raOrderDetailBean.setAppoverMessage(TEST_APPROVER_MESSAGE);
		routingInfoBeans.add(raOrderDetailBean);

		return routingInfoBeans;
	}

	//CAP-44979 //CAP-45058 //CAP-48488
	@Test
	void testGetOrderSearchFullDetailsWithSalesRef() throws AtWinXSException, ParseException {
		Collection<OrderStatusMessage> oms = new ArrayList<OrderStatusMessage>();

		orderSearchServiceImpl = Mockito.spy(orderSearchServiceImpl);

		OrderStatusMessage orderMessageRequiredTypes = new OrderStatusMessageImpl();
		orderMessageRequiredTypes.setMessageText("ABC");
		orderMessageRequiredTypes.setMessageType("");
		oms.add(orderMessageRequiredTypes);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

	    when(mockEntityObjectMap.getEntity(eq(OrderSearchDetailFormBean.class), any())).thenReturn(mockOrderSearchDetailFromBean);
	    when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), any())).thenReturn(mockOrderStatusSearch);
	    when(mockEntityObjectMap.getEntity(eq(OSDetailsItemsOrdered.class), any())).thenReturn(mockOSDetailsItemsOrdered);
	    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-49259
	    when(mockOrderSearchDetailFromBean.getOSBatchOrderStatusBean()).thenReturn(mockOSBatchOrderStatusBean);
	    when(mockOrderSearchDetailFromBean.getHeaderVo()).thenReturn(mockOrderStatusHeaderVO);
	    when(mockOrderStatusHeaderVO.getOrderNum()).thenReturn("123456");
	    when(mockOrderSearchDetailFromBean.getPaymentVerification()).thenReturn(mockPaymentVerification);

	    when(mockOrderSearchDetailFromBean.getOrderMessages()).thenReturn(mockOrderStatusMessages);
	    when(mockOrderStatusMessages.getOrderMessages(Type.All)).thenReturn(oms);

	    //CAP-45812
	    when(mockOEManageOrdersComponentLocatorService.locate(null)).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderHeader(isA(OrderHeaderVOKey.class))).thenReturn(mockOrderHeaderVo);
		when(mockOrderHeaderVo.getLoginID()).thenReturn(TEST_USER_ID);
		when(mockOrderHeaderVo.getProfileNum()).thenReturn(TEST_PROFILE_NBR);
		when(mockOrderHeaderVo.getOriginatorProfileNum()).thenReturn(123456);
		when(mockOrderHeaderVo.getOriginatorLoginID()).thenReturn("12345");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockProfile).when(mockEntityObjectMap).getEntity(Profile.class, mockToken);


	    when(mockPaymentVerification.getHolderName()).thenReturn("Sakthi");
	    when(mockPaymentVerification.getTypeCode()).thenReturn("V");
	    when(mockPaymentVerification.getLast4()).thenReturn("1234");
	    when(mockPaymentVerification.getExpMonth()).thenReturn("02");
	    when(mockPaymentVerification.getExpYear()).thenReturn("23");
	    when(mockPaymentVerification.getEmailAddress()).thenReturn("test@test.com");
	    orderSearchServiceImpl=Mockito.spy(orderSearchServiceImpl);

	    when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderInfoShipping(isA(OrderShippingVOKey.class))).thenReturn(mockOrderInfoShippingVO);
		
		//CAP-48488 - Shipment Not Null &  ExpeditedOrderDate exists 
		when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(mockOrderShippingVO);
		when(mockOrderShippingVO.getExpeditedOrderDate()).thenReturn(new Date());
		
	    try (MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
	    	mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any())).thenReturn(mockUserSettings);
	    	coOSFullDetailResponse = orderSearchServiceImpl.getOrderSearchFullDetailsWithSalesRef(mockSessionContainer,coOSFullDetailRequest);
			assertNotNull(coOSFullDetailResponse);
			assertTrue(coOSFullDetailResponse.isSuccess());

	    }

	}

	@Test
	void testGetOrderSearchCriteriaForNagavation() throws AtWinXSException {
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockTranslationService.processMessage(any(), any(), anyString())).thenReturn("Order Status");
		when(mockOsBean.isLimitQuickSearchOptions()).thenReturn(false);
		when(mockOsBean.getShowCustRef(anyInt())).thenReturn("H");
		List<COOrderSearchCriteria> oscArrList = orderSearchServiceImpl.getOrderSearchCriteriaForNagavation(mockAppSessionBean, mockOsBean);
		assertNotNull(oscArrList);
	}

	private COOSFullDetailRequest getCOOSFullDetailRequest() {
		COOSFullDetailRequest request=new COOSFullDetailRequest();
		request.setSearchResultNumber("1");
		request.setOrderID(614225);
		request.setOrderNum("40026714");
		request.setSalesReferenceNumber("80033267");
		request.setSortLinesBy("ORD_LN_NR");
		return request;
	}
	@ParameterizedTest
	@MethodSource("getTestData_success")
	void testValidateCoOrderSearchRequestV3_success(String criteriaName, String criteriaValue) throws AtWinXSException {
		COOrderSearchResultResponse response = new COOrderSearchResultResponse();
		COOrderSearchRequest request = new COOrderSearchRequest();
		ArrayList<COSharedSearchCriteriaRequest> searchCriteriaRequest = new ArrayList<>();
		COSharedSearchCriteriaRequest criteriaRequest = new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName(criteriaName);
		criteriaRequest.setCriteriaFieldValue(criteriaValue);
		searchCriteriaRequest.add(criteriaRequest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		request.setSearchCriteriaRequest(searchCriteriaRequest);


		orderSearchServiceImpl.validateCoOrderSearchRequestV3(request, response, mockSessionContainer);
	}
	@ParameterizedTest
	@MethodSource("getTestData_fail")
	void testValidateCoOrderSearchRequestV3_fail(String criteriaName, String criteriaValue) throws AtWinXSException {
		COOrderSearchResultResponse response = new COOrderSearchResultResponse();
		COOrderSearchRequest request = new COOrderSearchRequest();
		ArrayList<COSharedSearchCriteriaRequest> searchCriteriaRequest = new ArrayList<>();
		COSharedSearchCriteriaRequest criteriaRequest = new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName(criteriaName);
		criteriaRequest.setCriteriaFieldValue(criteriaValue);
		searchCriteriaRequest.add(criteriaRequest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockTranslationService.processMessage(any(), any(), anyString(), anyMap())).thenReturn(criteriaValue);
		request.setSearchCriteriaRequest(searchCriteriaRequest);


		orderSearchServiceImpl.validateCoOrderSearchRequestV3(request, response, mockSessionContainer);
	}

	private static Stream<Arguments> getTestData_success() {
		return Stream.of(Arguments.of("CriteriaOrderNumber", "53211857", true),
				Arguments.of("CriteriaStatusCode", "5321", true),
				Arguments.of("CriteriaItemNumber", "TestValidCriteriaFieldValue123", true),
				Arguments.of("CriteriaPONumber", "TestValidCriteria123", true),
				Arguments.of("CriteriaSalesRef", "TestValidCriteria123", true));
	}
	private static Stream<Arguments> getTestData_fail() {
		return Stream.of(Arguments.of("CriteriaOrderNumber", "532118571", false),
				Arguments.of("CriteriaStatusCode", "53121", false),
				Arguments.of("CriteriaItemNumber", "TestInvalidValidCriteriaFieldValue123", false),
				Arguments.of("CriteriaPONumber", "TestInvalidValidCriteria123", false),
				Arguments.of("CriteriaSalesRef", "TestInvalidValidCriteria123", false));
	}

	//CAP-45055

	@Test
	void testPopulateOrderDetailforNavigationLimitedSearchNo() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockOsBean.isLimitQuickSearchOptions()).thenReturn(false);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)	){
			COOrderSearchNavigationResponse response =orderSearchServiceImpl.populateOrderDetailforNavigation(mockSessionContainer);
			assertTrue(response.isSuccess());
		}
	}

	@Test
	void testPopulateOrderDetailforNavigationLimitedSearchYes() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockOsBean.isLimitQuickSearchOptions()).thenReturn(false);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)	){
			COOrderSearchNavigationResponse response =orderSearchServiceImpl.populateOrderDetailforNavigation(mockSessionContainer);
			assertTrue(response.isSuccess());
		}
	}

	//CAP-45481
	@Test
	void testGetSearchOrdersDetailLimitedSearch() throws AtWinXSException, ParseException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSDF.parse(any())).thenReturn(new java.util.Date());
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearch);
		when(mockEntityObjectMap.getEntity(eq(OrderStatusCodes.class), isA(CustomizationToken.class))).thenReturn(mockOrderStatusCodes);

		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<OrderSearchUtil> mockOrderSearchUtil = Mockito.mockStatic(OrderSearchUtil.class);MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockOrderSearchUtil.when(() -> OrderSearchUtil.getOrderStatusSearchCriteria(any(),
					any(), any())).thenReturn(cpSearchCriteria);
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequest());
			Assertions.assertEquals(0, response.getOrdersFound());
		}

	}

	void testGetSearchOrdersDetailwithoutLimitedSearch() throws AtWinXSException, ParseException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockOsBean.isShowRecentOrders()).thenReturn(true);
		try(MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class); MockedStatic<OrderSearchUtil> mockOrderSearchUtil = Mockito.mockStatic(OrderSearchUtil.class);MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockUtil.when(() -> Util.getDateFormatForLocale(mockAppSessionBean.getDefaultLocale())).thenReturn("");
			mockOrderSearchUtil.when(() -> OrderSearchUtil.getOrderStatusSearchCriteria(any(),
					any(), any())).thenReturn(cpSearchCriteria);
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			orderSearchServiceImpl = Mockito.spy(orderSearchServiceImpl);
			when(orderSearchServiceImpl.validateLimitedSearch(anyBoolean(), any())).thenReturn(true);
		    when(mockTranslationService.processMessage(any(), any(), anyString())).thenReturn("Search not allowed based on settings");
			COOrderSearchResultResponse response=orderSearchServiceImpl.getSearchOrdersDetail(mockSessionContainer, coOrderSearchRequestwithrecenttrue());
			Assertions.assertEquals(0, response.getOrdersFound());
		}

	}

	@Test
	void testgetOrderSearchwithoutLimitedoption() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestForSearchInvoiceNo();
		boolean limitedSearch=false;
		limitedSearch=orderSearchServiceImpl.validateLimitedSearch(false,request);
		assertFalse(limitedSearch);

		limitedSearch=orderSearchServiceImpl.validateLimitedSearch(true,request);
		assertTrue(limitedSearch);
	}

	@Test
	void testgetOrderSearchwithLimitedoptionsalesref() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequest();
		boolean limitedSearch=false;
		limitedSearch=orderSearchServiceImpl.validateLimitedSearch(true,request);
		assertFalse(limitedSearch);
	}

	@Test
	void testgetOrderSearchwithLimitedoptionpo() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestpono();
		boolean limitedSearch=false;
		limitedSearch=orderSearchServiceImpl.validateLimitedSearch(true,request);
		assertFalse(limitedSearch);
	}

	@Test
	void testgetOrderSearchwithLimitedoptionorderno() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestordno();
		boolean limitedSearch=false;
		limitedSearch=orderSearchServiceImpl.validateLimitedSearch(true,request);
		assertFalse(limitedSearch);
	}

	@Test
	void testgetOrderSearchwithLimitedoptionItemno() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestItemno();
		boolean limitedSearch=false;
		limitedSearch=orderSearchServiceImpl.validateLimitedSearch(true,request);
		assertFalse(limitedSearch);
	}

		private COOrderSearchRequest coOrderSearchRequestForSearchInvoiceNo() {
    	COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaInvoiceNumber");
		criteriaRequest.setCriteriaFieldValue("753242206");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("06/30/2023");
		request.setToDate("06/12/2023");
		request.setScope("A");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}


	private COOrderSearchRequest coOrderSearchRequestpono() {
		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaPONumber");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("06/30/2023");
		request.setToDate("06/12/2023");
		request.setScope("M");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}


	private COOrderSearchRequest coOrderSearchRequestordno() {
		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaOrderNumber");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("06/30/2023");
		request.setToDate("06/12/2023");
		request.setScope("M");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}


	private COOrderSearchRequest coOrderSearchRequestItemno() {
		COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaItemNumber");
		criteriaRequest.setCriteriaFieldValue("80032142");
		criteriaRequest.setCriteriaFieldModifier(">=");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("06/30/2023");
		request.setToDate("06/12/2023");
		request.setScope("M");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}


	//CAP-45812
	@Test
	void testGetOrderSearchFullDetailsWithSalesRef_withoutOriginator_profileSame() throws AtWinXSException, ParseException {
		Collection<OrderStatusMessage> oms = new ArrayList<OrderStatusMessage>();

		orderSearchServiceImpl = Mockito.spy(orderSearchServiceImpl);

		OrderStatusMessage orderMessageRequiredTypes = new OrderStatusMessageImpl();
		orderMessageRequiredTypes.setMessageText("ABC");
		orderMessageRequiredTypes.setMessageType("");
		oms.add(orderMessageRequiredTypes);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

	    when(mockEntityObjectMap.getEntity(eq(OrderSearchDetailFormBean.class), any())).thenReturn(mockOrderSearchDetailFromBean);
	    when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), any())).thenReturn(mockOrderStatusSearch);
	    when(mockEntityObjectMap.getEntity(eq(OSDetailsItemsOrdered.class), any())).thenReturn(mockOSDetailsItemsOrdered);
	    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-49259
	    when(mockOrderSearchDetailFromBean.getOSBatchOrderStatusBean()).thenReturn(mockOSBatchOrderStatusBean);
	    when(mockOrderSearchDetailFromBean.getHeaderVo()).thenReturn(mockOrderStatusHeaderVO);
	    when(mockOrderStatusHeaderVO.getOrderNum()).thenReturn("123456");
	    when(mockOrderSearchDetailFromBean.getPaymentVerification()).thenReturn(mockPaymentVerification);

	    when(mockOrderSearchDetailFromBean.getOrderMessages()).thenReturn(mockOrderStatusMessages);
	    when(mockOrderStatusMessages.getOrderMessages(Type.All)).thenReturn(oms);

	    //CAP-45812
	    when(mockOEManageOrdersComponentLocatorService.locate(null)).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderHeader(isA(OrderHeaderVOKey.class))).thenReturn(mockOrderHeaderVo);
		when(mockOrderHeaderVo.getLoginID()).thenReturn(TEST_USER_ID);
		when(mockOrderHeaderVo.getProfileNum()).thenReturn(TEST_PROFILE_NBR);
		when(mockOrderHeaderVo.getOriginatorProfileNum()).thenReturn(12345);
		when(mockOrderHeaderVo.getOriginatorLoginID()).thenReturn("12345");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();


	    when(mockPaymentVerification.getHolderName()).thenReturn("Sakthi");
	    when(mockPaymentVerification.getTypeCode()).thenReturn("V");
	    when(mockPaymentVerification.getLast4()).thenReturn("1234");
	    when(mockPaymentVerification.getExpMonth()).thenReturn("02");
	    when(mockPaymentVerification.getExpYear()).thenReturn("23");
	    when(mockPaymentVerification.getEmailAddress()).thenReturn("test@test.com");
	    orderSearchServiceImpl=Mockito.spy(orderSearchServiceImpl);

	    when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderInfoShipping(isA(OrderShippingVOKey.class))).thenReturn(mockOrderInfoShippingVO);

	    try (MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
	    	mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any())).thenReturn(mockUserSettings);
	    	coOSFullDetailResponse = orderSearchServiceImpl.getOrderSearchFullDetailsWithSalesRef(mockSessionContainer,coOSFullDetailRequest);
			assertNotNull(coOSFullDetailResponse);
			assertTrue(coOSFullDetailResponse.isSuccess());

	    }

	}

	@Test
	void testGetOrderSearchFullDetailsWithSalesRef_withoutOriginator_loginIdSame() throws AtWinXSException, ParseException {
		Collection<OrderStatusMessage> oms = new ArrayList<OrderStatusMessage>();

		orderSearchServiceImpl = Mockito.spy(orderSearchServiceImpl);

		OrderStatusMessage orderMessageRequiredTypes = new OrderStatusMessageImpl();
		orderMessageRequiredTypes.setMessageText("ABC");
		orderMessageRequiredTypes.setMessageType("");
		oms.add(orderMessageRequiredTypes);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

	    when(mockEntityObjectMap.getEntity(eq(OrderSearchDetailFormBean.class), any())).thenReturn(mockOrderSearchDetailFromBean);
	    when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), any())).thenReturn(mockOrderStatusSearch);
	    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-49259
	    when(mockEntityObjectMap.getEntity(eq(OSDetailsItemsOrdered.class), any())).thenReturn(mockOSDetailsItemsOrdered);
	    when(mockOrderSearchDetailFromBean.getOSBatchOrderStatusBean()).thenReturn(mockOSBatchOrderStatusBean);
	    when(mockOrderSearchDetailFromBean.getHeaderVo()).thenReturn(mockOrderStatusHeaderVO);
	    when(mockOrderStatusHeaderVO.getOrderNum()).thenReturn("123456");
	    when(mockOrderSearchDetailFromBean.getPaymentVerification()).thenReturn(mockPaymentVerification);

	    when(mockOrderSearchDetailFromBean.getOrderMessages()).thenReturn(mockOrderStatusMessages);
	    when(mockOrderStatusMessages.getOrderMessages(Type.All)).thenReturn(oms);

	    //CAP-45812
	    when(mockOEManageOrdersComponentLocatorService.locate(null)).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderHeader(isA(OrderHeaderVOKey.class))).thenReturn(mockOrderHeaderVo);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();


	    when(mockPaymentVerification.getHolderName()).thenReturn("Sakthi");
	    when(mockPaymentVerification.getTypeCode()).thenReturn("V");
	    when(mockPaymentVerification.getLast4()).thenReturn("1234");
	    when(mockPaymentVerification.getExpMonth()).thenReturn("02");
	    when(mockPaymentVerification.getExpYear()).thenReturn("23");
	    when(mockPaymentVerification.getEmailAddress()).thenReturn("test@test.com");
	    orderSearchServiceImpl=Mockito.spy(orderSearchServiceImpl);

	    when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderInfoShipping(isA(OrderShippingVOKey.class))).thenReturn(mockOrderInfoShippingVO);

	    try (MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
	    	mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any())).thenReturn(mockUserSettings);
	    	coOSFullDetailResponse = orderSearchServiceImpl.getOrderSearchFullDetailsWithSalesRef(mockSessionContainer,coOSFullDetailRequest);
			assertNotNull(coOSFullDetailResponse);
			assertTrue(coOSFullDetailResponse.isSuccess());

	    }

	}

	@Test
	void testGetOrderSearchFullDetailsWithSalesRef_withoutOriginator_sharedUser() throws AtWinXSException, ParseException {
		Collection<OrderStatusMessage> oms = new ArrayList<OrderStatusMessage>();

		orderSearchServiceImpl = Mockito.spy(orderSearchServiceImpl);

		OrderStatusMessage orderMessageRequiredTypes = new OrderStatusMessageImpl();
		orderMessageRequiredTypes.setMessageText("ABC");
		orderMessageRequiredTypes.setMessageType("");
		oms.add(orderMessageRequiredTypes);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

	    when(mockEntityObjectMap.getEntity(eq(OrderSearchDetailFormBean.class), any())).thenReturn(mockOrderSearchDetailFromBean);
	    when(mockEntityObjectMap.getEntity(eq(OrderStatusSearch.class), any())).thenReturn(mockOrderStatusSearch);
	    when(mockEntityObjectMap.getEntity(eq(OSDetailsItemsOrdered.class), any())).thenReturn(mockOSDetailsItemsOrdered);
	    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-49259
	    when(mockOrderSearchDetailFromBean.getOSBatchOrderStatusBean()).thenReturn(mockOSBatchOrderStatusBean);
	    when(mockOrderSearchDetailFromBean.getHeaderVo()).thenReturn(mockOrderStatusHeaderVO);
	    when(mockOrderStatusHeaderVO.getOrderNum()).thenReturn("123456");
	    when(mockOrderSearchDetailFromBean.getPaymentVerification()).thenReturn(mockPaymentVerification);

	    when(mockOrderSearchDetailFromBean.getOrderMessages()).thenReturn(mockOrderStatusMessages);
	    when(mockOrderStatusMessages.getOrderMessages(Type.All)).thenReturn(oms);

	    //CAP-45812
	    when(mockOEManageOrdersComponentLocatorService.locate(null)).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderHeader(isA(OrderHeaderVOKey.class))).thenReturn(mockOrderHeaderVo);
		when(mockOrderHeaderVo.getOriginatorProfileNum()).thenReturn(-1);
		when(mockOrderHeaderVo.getLoginID()).thenReturn("TEST_USER");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(Profile.class, mockToken)).thenReturn(mockProfile);
		when(mockEntityObjectMap.getEntity(User.class, mockToken)).thenReturn(mockUser);


	    when(mockPaymentVerification.getHolderName()).thenReturn("Sakthi");
	    when(mockPaymentVerification.getTypeCode()).thenReturn("V");
	    when(mockPaymentVerification.getLast4()).thenReturn("1234");
	    when(mockPaymentVerification.getExpMonth()).thenReturn("02");
	    when(mockPaymentVerification.getExpYear()).thenReturn("23");
	    when(mockPaymentVerification.getEmailAddress()).thenReturn("test@test.com");
	    orderSearchServiceImpl=Mockito.spy(orderSearchServiceImpl);

	    when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderInfoShipping(isA(OrderShippingVOKey.class))).thenReturn(mockOrderInfoShippingVO);

	    try (MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
	    	mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any())).thenReturn(mockUserSettings);
	    	coOSFullDetailResponse = orderSearchServiceImpl.getOrderSearchFullDetailsWithSalesRef(mockSessionContainer,coOSFullDetailRequest);
			assertNotNull(coOSFullDetailResponse);
			assertTrue(coOSFullDetailResponse.isSuccess());

	    }

	}



}


