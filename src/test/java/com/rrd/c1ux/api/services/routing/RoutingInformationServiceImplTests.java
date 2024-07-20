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
 *  08/30/23	Satishkumar A		CAP-43283		C1UX BE - Routing Information For Justification Section on Review Order Page
 *  09/05/23	S Ramachandran		CAP-43193		Added tests for Order Line Routing at header level
 */
package com.rrd.c1ux.api.services.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.routing.RoutingInformationResponse;
import com.rrd.c1ux.api.models.routing.RoutingReason;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderLinesRoutingFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderRoutingFormBean;

@WithMockUser
class RoutingInformationServiceImplTests extends BaseServiceTest  {

	@InjectMocks
	private RoutingInformationServiceImpl serviceToTest;

	// CAP-43193 - Order Line Routing Message at header level
	private static final String TEST_VENDOR_ITEM_NUMBER = "255105";
	private static final String TEST_APPROVAL_QUEUE = "Order Level Approver Queue Name (SF APPROVER NAme)";
	private static final String TEST_APPROVAL_REASON_DESC = ""
			+ "1) Quantity Ordered for Item exceeds the Quantity Limit of 2 -14444  FASSON 14444 TEST 2 - default $3 each<br>"
			+ "1) Ordered Item Requires Approval -BC3TEST  BCSI BUSINESS CARD 3 TEST ITEM<br>";
	OEOrderRoutingFormBean oeOrderRoutingFormBeanWithOLHLRoutingReasons = getOEOrderRoutingFormBeanWithOLHLRoutingReasons();

	OEOrderRoutingFormBean oeOrderRoutingFormBean = getOEOrderRoutingFormBean();
	OEOrderRoutingFormBean oeOrderRoutingFormBeanWithRoutingReasion = getOEOrderRoutingFormBeanWithRoutingReasion();
	OEOrderRoutingFormBean oeOrderRoutingFormBeanWithRoutingReasionEmpty = getOEOrderRoutingFormBeanWithRoutingReasionEmpty();
	@BeforeEach
	public void setup() throws AtWinXSException {
		setupBaseMockSessions();

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

	}
	
	//CAP-43193 - Order Line Routing Message at header level
	@Test
	void that_routingInformation_OrderLineHeaderLevel_success() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(123);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);
		when(mockUserSettings.getRouteDollarAmount()).thenReturn(0.0);
		when(mockUserSettings.isShowAssignedApprover()).thenReturn(true);
		when(mockAppSessionBean.isShowWCSSItemNumber()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);//CAP-44145
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);//CAP-44145

		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean))
							.thenReturn(oeOrderRoutingFormBeanWithOLHLRoutingReasons);
				})) {

			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
			List<RoutingReason> rountingReasons = (List<RoutingReason>) response.getRoutingReasons()
					.getRoutingReasonList();
			assertEquals(TEST_APPROVAL_QUEUE, rountingReasons.get(1).getApprovalQueue());
			assertEquals(TEST_APPROVAL_REASON_DESC, rountingReasons.get(1).getReasonDescription());

		}
	}
	
	@Test
	void that_routingInformation_success() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(123);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);
		when(mockUserSettings.getRouteDollarAmount()).thenReturn(0.0);


		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito.mockConstruction(OECheckoutAssembler.class,
				(mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean)).thenReturn(oeOrderRoutingFormBeanWithRoutingReasion);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_routingInformation_RoutingReasionEmpty() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(123);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);
		when(mockUserSettings.getRouteDollarAmount()).thenReturn(0.0);


		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito.mockConstruction(OECheckoutAssembler.class,
				(mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean)).thenReturn(oeOrderRoutingFormBeanWithRoutingReasionEmpty);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_routingInformation_lineDetailsNull() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(123);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);
		when(mockUserSettings.getRouteDollarAmount()).thenReturn(2.0);
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito.mockConstruction(OECheckoutAssembler.class,
				(mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean)).thenReturn(oeOrderRoutingFormBean);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_routingInformation_OrderId_Zero() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(0);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito.mockConstruction(OECheckoutAssembler.class,
				(mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean)).thenReturn(oeOrderRoutingFormBeanWithRoutingReasion);
				});MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),SFTranslationTextConstants.ORDER_NOT_FOUND_FOR_ROUTING_INFO)).thenReturn("Order not found to retrieve routing information.");
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());
		}
	}
	
	@Test
	void that_routingInformation_OrderId_null() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(null);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito.mockConstruction(OECheckoutAssembler.class,
				(mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean)).thenReturn(oeOrderRoutingFormBeanWithRoutingReasionEmpty);
				});MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),SFTranslationTextConstants.ORDER_NOT_FOUND_FOR_ROUTING_INFO)).thenReturn("Order not found to retrieve routing information.");
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());
		}
	}
	
	@Test
	void that_routingInformation_OrderId_notNull_isSuccess_false() throws Exception {

		RoutingInformationResponse response = new RoutingInformationResponse();
		
		when(mockVolatileSessionBean.getOrderId()).thenReturn(123);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito.mockConstruction(OECheckoutAssembler.class,
				(mock, context) -> {
					when(mock.getOrderRoutingDetails(mockOESessionBean, mockAppSessionBean)).thenReturn(oeOrderRoutingFormBean);
				});MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),SFTranslationTextConstants.ORDER_NOT_FOUND_FOR_ROUTING_INFO)).thenReturn("Order not found to retrieve routing information.");
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.getRoutingInformation(mockSessionContainer);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());
		}
	}

	private OEOrderRoutingFormBean getOEOrderRoutingFormBean() {
		OEOrderRoutingFormBean oeOrderRoutingFormBean = new OEOrderRoutingFormBean();
		oeOrderRoutingFormBean.setOrderLineDetailsFormBean(null);
		oeOrderRoutingFormBean.setOrderRoutingReason(null);
		
		return oeOrderRoutingFormBean;
		
	}
	
	private OEOrderRoutingFormBean getOEOrderRoutingFormBeanWithRoutingReasion() {
		OEOrderRoutingFormBean oeOrderRoutingFormBean = new OEOrderRoutingFormBean();
		OEOrderLinesRoutingFormBean[] list = new OEOrderLinesRoutingFormBean[1];
		OEOrderLinesRoutingFormBean bean = new OEOrderLinesRoutingFormBean();
		bean.setLineNum(0);
		list[0]=bean;
		oeOrderRoutingFormBean.setOrderLineDetailsFormBean(list);
		oeOrderRoutingFormBean.setOrderRoutingReason("Routing Reasion");
		return oeOrderRoutingFormBean;
		
	}
	
	private OEOrderRoutingFormBean getOEOrderRoutingFormBeanWithRoutingReasionEmpty() {
		OEOrderRoutingFormBean oeOrderRoutingFormBean = new OEOrderRoutingFormBean();
		OEOrderLinesRoutingFormBean[] list = new OEOrderLinesRoutingFormBean[1];
		OEOrderLinesRoutingFormBean bean = new OEOrderLinesRoutingFormBean();
		bean.setLineNum(0);
		list[0]=bean;
		oeOrderRoutingFormBean.setOrderLineDetailsFormBean(list);
		oeOrderRoutingFormBean.setOrderRoutingReason("");
		return oeOrderRoutingFormBean;
		
	}

	// CAP-43193 - Order Line Routing Message at header level - Routing Reason Msg
	private OEOrderRoutingFormBean getOEOrderRoutingFormBeanWithOLHLRoutingReasons() {
		OEOrderRoutingFormBean oeOrderRoutingFormBean = new OEOrderRoutingFormBean();
		OEOrderLinesRoutingFormBean[] list = new OEOrderLinesRoutingFormBean[1];
		OEOrderLinesRoutingFormBean bean = new OEOrderLinesRoutingFormBean();
		bean.setLineNum(0);
		list[0] = bean;
		oeOrderRoutingFormBean.setOrderLineDetailsFormBean(list);
		oeOrderRoutingFormBean.setOrderRoutingReason(TEST_APPROVAL_REASON_DESC);
		oeOrderRoutingFormBean.setApprvQueueOwner(TEST_APPROVAL_QUEUE);
		return oeOrderRoutingFormBean;
	}
}
