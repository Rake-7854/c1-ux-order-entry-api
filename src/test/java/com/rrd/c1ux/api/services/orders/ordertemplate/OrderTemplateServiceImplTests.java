/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions:
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  05/01/24		M Sakthi		CAP-48745					C1UX BE - Create new tests for API to start the save order template process
 *  04/30/24		C Codina		CAP-48890					Initial Version
 *  04/30/24		S Ramachandran	CAP-48889					Added Junit tests for deleteOrderTemplate service method
 *  05/02/24		N Caceres		CAP-48821					Added test methods for saving Order Template
 *  05/07/24		Satishkumar A	CAP-48975					C1UX BE - Create new API to actually load template order and to use in cart
 *  05/15/24	    Krishna Natarajan	CAP-49427				Commented out unnecessary line of stubbing
 *  05/21/24		Krishna Natarajan	CAP-49537				Updated tests for item utility methods for getting full names of OUM with the acronyms 
 */


package com.rrd.c1ux.api.services.orders.ordertemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.TemplateOrderListResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateResponse;
import com.rrd.c1ux.api.util.ItemUtility;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.admin.vo.OrderTemplateAttributeVO;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderAddressSessionBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderAddressesResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplateDetailsFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplateFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplatesFormBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderLineResponseBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderTemplateHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderTemplateOrderLineVO;

class OrderTemplateServiceImplTests extends BaseServiceTest{

	private static final String NEW_ORDER_TEMPLATE = "-1";

	@InjectMocks
	private OrderTemplateServiceImpl serviceToTest;
	
	@Mock
	OESavedOrderAssembler mockOESavedOrderAssembler;
	
	@Mock
	OEOrderTemplateDetailsFormBean mockOEOrderTemplateDetailsFormBean;
	
	@Mock
	OEOrderAddressesResponseBean mockOEOrderAddressesResponseBean;
	
	@Mock
	OEOrderAddressSessionBean mockOEOrderAddressSessionBean;
	
	@Mock
	OrderTemplateHeaderVO mockOrderTemplateHeaderVO;
	
	@Mock
	OrderAddressVO mockOrderAddressVO;

	@Mock
	OEOrderTemplatesFormBean mockTemplatesFormBean;
	
	@Mock
	OESavedOrderAssembler mockAssembler;
	
	@Mock
	OrderOnBehalfVO mockOrderOnBehalfVO;
	
	@Mock
	  private OEExtendedItemQuantityResponseBean mockInvalidItem1;
	
	private TemplateOrderListResponse orderListResponse;
	
	private static final int TEST_SITE_ID = 456;
	
	private static final String TEST_STRING = "Test";
	
	@BeforeEach
	public void setup() throws AtWinXSException {
		
	}
	
	
	
	@Test
	void that_getOrderTemplateDetails_orderfromSummary_success() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		LoadSaveOrderTemplateResponse response=new LoadSaveOrderTemplateResponse();
		LoadSaveOrderTemplateRequest request=getTemplateSummaryRequest();
		userHasAccessToOrderTemplate();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockOEOrderTemplateDetailsFormBean.getTemplateID()).thenReturn("2");
		//when(mockOEOrderTemplateDetailsFormBean.getTemplateUpdateLevel()).thenReturn("2");//CAP-49427
		when(mockOEOrderTemplateDetailsFormBean.getAddresses()).thenReturn(mockOEOrderAddressesResponseBean);
		//when(mockOEOrderAddressesResponseBean.getBillingAddress()).thenReturn(mockOEOrderAddressSessionBean);//CAP-49427
		when(mockOEOrderAddressesResponseBean.getShippingAddress()).thenReturn(mockOEOrderAddressSessionBean);
		when(mockOEOrderAddressSessionBean.getAddressLine1()).thenReturn("Test 1");
		when(mockOEOrderAddressSessionBean.getAddressLine2()).thenReturn("Test 2");
		
		OESavedOrderLineResponseBean[] ol1=new OESavedOrderLineResponseBean[1];
		OESavedOrderLineResponseBean oesBean=new OESavedOrderLineResponseBean();
		oesBean.setOrderQuantity("5");
		ol1[0]=oesBean;
		when(mockOEOrderTemplateDetailsFormBean.getOrderLines()).thenReturn(ol1);
		
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
					when(mock.getSavedOrderDetailForTemplate(anyInt(), any(), any(), any(), anyInt())).thenReturn(mockOEOrderTemplateDetailsFormBean);
				});MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getUOMAcronyms(mockInvalidItem1.getUOMCode(), false, mockAppSessionBean)).thenReturn("Carton");//CAP-49537

		response = serviceToTest.getOrderTemplateDetails(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
	  }	
	}

	private void userHasAccessToOrderTemplate() {
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);
	}
	
	@Test
	void that_getOrderTemplateDetails_orderfromTemplate_success() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		LoadSaveOrderTemplateResponse response=new LoadSaveOrderTemplateResponse();
		LoadSaveOrderTemplateRequest request=getTemplateRequest();
		userHasAccessToOrderTemplate();
		when(mockUserSettings.isShowBillToInfo()).thenReturn(true);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getTemplateUpdateLevel()).thenReturn("2");
	
		OrderTemplateOrderLineVO[] lineVO=new OrderTemplateOrderLineVO[1];
		OrderTemplateOrderLineVO bo=new OrderTemplateOrderLineVO(0, 0, null, null, null, null, null, 0, 0, null, null, null, 0, null, false, 0, null, 0, 0, null, false, null, 0, 0, 0, 0, null);
		lineVO[0]=bo;
		when(mockIOESavedOrderComponent.getTemplateExtendedQuantitySummary(anyInt())).thenReturn(lineVO);
		
		when(mockOESavedOrderComponentLocatorService.locate(any())).thenReturn(mockIOESavedOrderComponent);
		when(mockIOESavedOrderComponent.getTemplate(any())).thenReturn(mockOrderTemplateHeaderVO);
		
		when(mockIOESavedOrderComponent.getTemplateBillingAddress(any())).thenReturn(mockOrderAddressVO);
		when(mockIOESavedOrderComponent.getTemplateShippingAddress(any())).thenReturn(mockOrderAddressVO);
		

		
		OESavedOrderLineResponseBean[] ol1=new OESavedOrderLineResponseBean[1];
		OESavedOrderLineResponseBean oesBean=new OESavedOrderLineResponseBean();
		oesBean.setOrderQuantity("5");
		ol1[0]=oesBean;
		
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
					when(mock.getSavedOrderDetailForTemplate(anyInt(), any(), any(), any(), anyInt())).thenReturn(mockOEOrderTemplateDetailsFormBean);
				});MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getUOMAcronyms(mockInvalidItem1.getUOMCode(), false, mockAppSessionBean)).thenReturn("Carton");//CAP-49537

		
		response = serviceToTest.getOrderTemplateDetails(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
	  }	
	}
	
	public LoadSaveOrderTemplateRequest getTemplateSummaryRequest() {
		LoadSaveOrderTemplateRequest req=new LoadSaveOrderTemplateRequest();
		req.setOrderTemplateID("-1");
		return req;
	}
	
	public LoadSaveOrderTemplateRequest getTemplateRequest() {
		LoadSaveOrderTemplateRequest req=new LoadSaveOrderTemplateRequest();
		req.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX53AidKTi6Zo");
		return req;
	}
	
	
	@Test
	void that_getEditable_updateLevel1() throws Exception {
		boolean res=false;
		res = serviceToTest.getEditable(false, "1", mockOrderTemplateHeaderVO, mockAppSessionBean);
		Assertions.assertEquals(true, res);
	}
	
	@Test
	void that_getEditable_updateLevel0() throws Exception {
		boolean res=false;
		res = serviceToTest.getEditable(false, "0", mockOrderTemplateHeaderVO, mockAppSessionBean);
		Assertions.assertEquals(true, res);
	}
	
	
	
	@Test
	void that_getOrderTemplateDetails_orderfromTemplate_failed() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		
		LoadSaveOrderTemplateResponse response=new LoadSaveOrderTemplateResponse();
		LoadSaveOrderTemplateRequest request=getTemplateRequest();
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			serviceToTest.getOrderTemplateDetails(mockSessionContainer, request);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
		
		}
	
	
	@Test
	void that_getOrderTemplateDetails_orderfromSummary_failed() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

			
		LoadSaveOrderTemplateResponse response=new LoadSaveOrderTemplateResponse();
		LoadSaveOrderTemplateRequest request=getTemplateNotEncryptedRequest();
		userHasAccessToOrderTemplate();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
//		when(mockOEOrderTemplateDetailsFormBean.getTemplateID()).thenReturn("2");
//		when(mockOEOrderTemplateDetailsFormBean.getTemplateUpdateLevel()).thenReturn("2");
//		when(mockOEOrderTemplateDetailsFormBean.getAddresses()).thenReturn(mockOEOrderAddressesResponseBean);
//		when(mockOEOrderAddressesResponseBean.getBillingAddress()).thenReturn(mockOEOrderAddressSessionBean);
//		when(mockOEOrderAddressesResponseBean.getShippingAddress()).thenReturn(mockOEOrderAddressSessionBean);
//		when(mockOEOrderAddressSessionBean.getAddressLine1()).thenReturn("Test 1");
//		when(mockOEOrderAddressSessionBean.getAddressLine2()).thenReturn("Test 2");
		
		OESavedOrderLineResponseBean[] ol1=new OESavedOrderLineResponseBean[1];
		OESavedOrderLineResponseBean oesBean=new OESavedOrderLineResponseBean();
		oesBean.setOrderQuantity("5");
		ol1[0]=oesBean;
//		when(mockOEOrderTemplateDetailsFormBean.getOrderLines()).thenReturn(ol1);
		
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
					when(mock.getSavedOrderDetailForTemplate(anyInt(), any(), any(), any(), anyInt())).thenReturn(mockOEOrderTemplateDetailsFormBean);
				})) {
		response = serviceToTest.getOrderTemplateDetails(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(false, response.isSuccess());
	  }	
	}
	
	public LoadSaveOrderTemplateRequest getTemplateNotEncryptedRequest() {
		LoadSaveOrderTemplateRequest req=new LoadSaveOrderTemplateRequest();
		req.setOrderTemplateID("3701");
		return req;
	}

	@Test
	void that_getTemplateOrderList_test() throws AtWinXSException {
		serviceToTest = Mockito.spy(serviceToTest);
		
		mockSessions();
		
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);
		when(mockUserSettings.getTemplateUpdateLevel()).thenReturn("Test");
	
		doReturn(mockAssembler).when(serviceToTest).getAssembler(any(), any());
		
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_STRING);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getGroupName()).thenReturn(TEST_STRING);
		
		when(mockAssembler.getOrderTemplates(TEST_SITE_ID, TEST_STRING, TEST_SITE_ID, TEST_SITE_ID, ModelConstants.TEMPLATE_ORDER_TEMPLATE_NAME, TEST_STRING, TEST_STRING)).thenReturn(mockTemplatesFormBean);
		
		OEOrderTemplateFormBean[] getOrders = new OEOrderTemplateFormBean[1];
		getOrders[0] = new OEOrderTemplateFormBean(TEST_STRING, false, TEST_STRING, TEST_STRING, TEST_STRING, "1", TEST_SITE_ID, TEST_STRING, TEST_SITE_ID, TEST_STRING, TEST_STRING, TEST_STRING, TEST_STRING);
		
		when(mockTemplatesFormBean.getOrders()).thenReturn(getOrders);
		
		orderListResponse = serviceToTest.getTemplateOrderList(mockSessionContainer);
		assertTrue(orderListResponse.isSuccess());
		
	}
	
	// CAP-48889
	@Test
	void that_deleteOrderTemplate_ShowTemplatesLink_IsFALSE_failed403() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		mockSessions();

		// when ShowTemplatesLink is FALSE, expect 403 status
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(false);

		DeleteTemplateRequest request = new DeleteTemplateRequest();
		request.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya"); // Encrpted value of 3770

		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			serviceToTest.deleteOrderTemplate(mockSessionContainer, request);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}
	
	// CAP-48889
	@Test
	void that_deleteOrderTemplate_OrderTemplateID_IsEMPTY_failed422() throws Exception {

		mockSessions();
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);

		DeleteTemplateResponse response =new DeleteTemplateResponse();
		DeleteTemplateRequest request = new DeleteTemplateRequest();
		
		// when ShowTemplatesLink is TRUE and OrderTemplateID is EMPTY,  expect 422 status
		request.setOrderTemplateID("");
		response = serviceToTest.deleteOrderTemplate(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(false, response.isSuccess());
		
		// when ShowTemplatesLink is TRUE and Tampered Encrypted TemplateID,  expect 422 status
		request.setOrderTemplateID("NS0O"); //Invalid Tampered Encrypted Template ID 
		response = serviceToTest.deleteOrderTemplate(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(false, response.isSuccess());
	}
	
	// CAP-48889
	@Test
	void that_deleteOrderTemplate_NonEditableTemplateIDforLoggedInUser_failed422() throws Exception {

		mockSessions();
		
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);
		when(mockUserSettings.getTemplateUpdateLevel()).thenReturn(OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_OWN);
		
		when(mockAppSessionBean.getSiteID()).thenReturn(1111);
		when(mockAppSessionBean.getLoginID()).thenReturn("IDC-CP-USER1");
		when(mockAppSessionBean.getProfileNumber()).thenReturn(11111); 
		when(mockAppSessionBean.getBuID()).thenReturn(111); 
		when(mockAppSessionBean.getGroupName()).thenReturn("C1UXCompareCP");
		
		DeleteTemplateResponse response =new DeleteTemplateResponse();
		DeleteTemplateRequest request = new DeleteTemplateRequest();

		ArrayList<OEOrderTemplateFormBean> list = new ArrayList<>();
		OEOrderTemplateFormBean order = new OEOrderTemplateFormBean();

		order.setTemplateID("3770");
		order.setTemplateName("Template Name1");
		order.setTemplateType("N");
		order.setIsEditable(false);
		list.add(order);
		
		order = new OEOrderTemplateFormBean();
		order.setTemplateID("3771");
		order.setTemplateName("Template Name2");
		order.setTemplateType("Y");
		order.setIsEditable(false);
		list.add(order);
		
		OEOrderTemplatesFormBean formBeanToDelete = new OEOrderTemplatesFormBean();
		OEOrderTemplateFormBean[] ordersToDelete = new OEOrderTemplateFormBean[list.size()];
		
		formBeanToDelete.setLoginID("IDC-CP-USER1");
		formBeanToDelete.setSiteID(1111);
		formBeanToDelete.setOrders(list.toArray(ordersToDelete));

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {

					when(mock.getOrderTemplates(anyInt(), any(), anyInt(), anyInt(), any(), any(), any()))
							.thenReturn(formBeanToDelete);
				})) {
		
			request.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya"); // Encrpted value of 3770 
		
			response = serviceToTest.deleteOrderTemplate(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());
		}
	}
	
	// CAP-48889
	@Test
	void that_deleteOrderTemplate_InValidPrivilegeforLoggedInUser_failed422() throws Exception {

		mockSessions();
		
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);
		
		DeleteTemplateResponse response =new DeleteTemplateResponse();
		DeleteTemplateRequest request = new DeleteTemplateRequest();
		
		ArrayList<OEOrderTemplateFormBean> list = new ArrayList<>();
		OEOrderTemplateFormBean order = new OEOrderTemplateFormBean();

		order.setTemplateID("3770");
		order.setTemplateName("Template Name1");
		order.setTemplateType("N");
		order.setIsEditable(true);
		list.add(order);
		
		order = new OEOrderTemplateFormBean();
		order.setTemplateID("3771");
		order.setTemplateName("Template Name2");
		order.setTemplateType("Y");
		order.setIsEditable(true);
		list.add(order);
		
		OEOrderTemplatesFormBean formBeanToDelete = new OEOrderTemplatesFormBean();
		OEOrderTemplateFormBean[] ordersToDelete = new OEOrderTemplateFormBean[list.size()];
		
		formBeanToDelete.setLoginID("IDC-CP-USER1");
		formBeanToDelete.setSiteID(1111);
		formBeanToDelete.setOrders(list.toArray(ordersToDelete));

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {

					when(mock.getOrderTemplates(anyInt(), any(), anyInt(), anyInt(), any(), any(), any()))
							.thenReturn(formBeanToDelete);
				})) {
			
			request.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX5pqKt5TNC7r"); // Encrpted value of 3770 

			response = serviceToTest.deleteOrderTemplate(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());
			
		}
	}	
	
	// CAP-48889
	@Test
	void that_deleteOrderTemplate_ValidEncryptedTemplateID_success200() throws Exception {

		mockSessions();
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);
		when(mockUserSettings.getTemplateUpdateLevel()).thenReturn(OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_ALL);
		
		when(mockAppSessionBean.getSiteID()).thenReturn(1111);
		when(mockAppSessionBean.getLoginID()).thenReturn("IDC-CP-USER1");
		when(mockAppSessionBean.getProfileNumber()).thenReturn(11111); 
		when(mockAppSessionBean.getBuID()).thenReturn(111); 
		when(mockAppSessionBean.getGroupName()).thenReturn("C1UXCompareCP");
		
		DeleteTemplateResponse response =new DeleteTemplateResponse();
		DeleteTemplateRequest request = new DeleteTemplateRequest();
		
		ArrayList<OEOrderTemplateFormBean> list = new ArrayList<>();
		OEOrderTemplateFormBean order = new OEOrderTemplateFormBean();

		order.setTemplateID("3770");
		order.setTemplateName("Template Name1");
		order.setTemplateType("N");
		order.setIsEditable(true);
		list.add(order);
		
		order = new OEOrderTemplateFormBean();
		order.setTemplateID("3771");
		order.setTemplateName("Template Name2");
		order.setTemplateType("Y");
		order.setIsEditable(false);
		list.add(order);
		
		OEOrderTemplatesFormBean formBeanToDelete = new OEOrderTemplatesFormBean();
		OEOrderTemplateFormBean[] ordersToDelete = new OEOrderTemplateFormBean[list.size()];
		
		formBeanToDelete.setLoginID("IDC-CP-USER1");
		formBeanToDelete.setSiteID(1111);
		formBeanToDelete.setOrders(list.toArray(ordersToDelete));

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {

					when(mock.getOrderTemplates(anyInt(), any(), anyInt(), anyInt(), any(), any(), any()))
							.thenReturn(formBeanToDelete);
				})) {
		
			request.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya"); // Encrpted value of 3770 
		
			response = serviceToTest.deleteOrderTemplate(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_getFileDeliveryMethod_1() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("PE", "1", "EA", "");
		Assertions.assertNotNull(res);
	}
	
	@Test
	void that_getFileDeliveryMethod_2() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("PE", "0", "", "B");
		Assertions.assertNotNull(res);
	}
	
	@Test
	void that_getFileDeliveryMethod_3() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("PE", "0", "EA", "B");
		Assertions.assertNotNull(res);
	}
	
	@Test
	void that_getFileDeliveryMethod_4() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("EP", "0", "EA", "B");
		Assertions.assertNotNull(res);
	}
	
	@Test
	void that_getFileDeliveryMethod_5() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("", "0", "EA", "");
		Assertions.assertNotNull(res);
	}
	
	@Test
	void that_getFileDeliveryMethod_6() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("E", "0", "EA", "B");
		Assertions.assertNotNull(res);
	}
	
	@Test
	void that_getFileDeliveryMethod_7() throws Exception {
		String res=null;
		res = serviceToTest.getFileDeliveryMethod("A", "", "", "B");
		Assertions.assertNotNull(res);
	}



	private void mockSessions() {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
	}
	
	@ParameterizedTest
	@MethodSource("saveOrderTemplateData")
	void that_saveOrderTemplate_testAllScenarios(String templateID, String templateName, String templateUpdateLevel, 
			boolean isShared, boolean isSuccess, String duplicateErrorMsg) throws AtWinXSException {
		SaveOrderTemplateResponse response = new SaveOrderTemplateResponse();
		SaveOrderTemplateRequest request = new SaveOrderTemplateRequest();
		
		request.setOrderTemplateID(templateID);
		request.setOrderTemplateName(templateName);
		request.setShared(isShared);
		
		mockSessions();
		userHasAccessToOrderTemplate();
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockCompositeProfileBean.getLoginID()).thenReturn(TEST_STRING);
		when(mockCompositeProfileBean.getProfileNumber()).thenReturn(TEST_SITE_ID);
		when(mockUserSettings.getTemplateUpdateLevel()).thenReturn(templateUpdateLevel);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		
		
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
					when(mock.saveOrderTemplateWithValidation(any(), anyInt(), 
							any(), anyInt(), anyString(), anyInt(), any())).thenReturn(duplicateErrorMsg);
				})) {
			
			if (NEW_ORDER_TEMPLATE == templateID && isSuccess) {
				when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
				when(mockUserSettings.getTeamOrdSharingSiteAttr()).thenReturn(1111);
				when(mockOESavedOrderComponentLocatorService.locate(any())).thenReturn(mockIOESavedOrderComponent);
				when(mockIOESavedOrderComponent.validateOrderTemplate(any(), anyInt())).thenReturn(true);
			}
			
			if (NEW_ORDER_TEMPLATE == templateID && !isSuccess && !OrderEntryConstants.TEMPLATE_UPDATE_LVL_NONE.equalsIgnoreCase(templateUpdateLevel)) {
				when(mockOESavedOrderComponentLocatorService.locate(any())).thenReturn(mockIOESavedOrderComponent);
				when(mockIOESavedOrderComponent.validateOrderTemplate(any(), anyInt())).thenReturn(false);
			}
			
			if (OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_OWN.equalsIgnoreCase(templateUpdateLevel)
					|| (OrderEntryConstants.TEMPLATE_UPDATE_LVL_NONE.equalsIgnoreCase(templateUpdateLevel)
							&& !isShared)) {
				when(mockOESavedOrderComponentLocatorService.locate(any())).thenReturn(mockIOESavedOrderComponent);
				when(mockIOESavedOrderComponent.getTemplate(any())).thenReturn(mockOrderTemplateHeaderVO);
			}
			
			response = serviceToTest.saveOrderTemplate(mockSessionContainer, request);
			
			assertNotNull(response);
			assertEquals(isSuccess, response.isSuccess());
		}
	}
	
	private static Stream<Arguments> saveOrderTemplateData() {
	    return Stream.of(
	      Arguments.of("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya", "Test001", "2", false, true, AtWinXSConstant.EMPTY_STRING),
	      Arguments.of("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya", "Test001", "1", true, true, AtWinXSConstant.EMPTY_STRING),
	      Arguments.of("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya", "Test001", "0", false, true, AtWinXSConstant.EMPTY_STRING),
	      Arguments.of("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya", "Test001", "2", false, false, "Template Name is not unique."),
	      Arguments.of(NEW_ORDER_TEMPLATE, "Test002", "2", false, true, AtWinXSConstant.EMPTY_STRING),
	      Arguments.of(NEW_ORDER_TEMPLATE, "Test002", "0", true, false, AtWinXSConstant.EMPTY_STRING),
	      Arguments.of(NEW_ORDER_TEMPLATE, "Test002", "2", false, false, AtWinXSConstant.EMPTY_STRING),
	      Arguments.of("NS0OXfhl9YMYqgSdA66ZX3fLKDjSLRya", "Test003-1234567890-123467890-1234567890-1234567890-1234567890", "2", false, false, AtWinXSConstant.EMPTY_STRING)
	    );
	}
	
	@ParameterizedTest
	@MethodSource("saveOrderTemplateFailData")
	void that_saveOrderTemplate_testOtherFailScenarios(String templateID, String templateName, String templateUpdateLevel, boolean isShared) throws AtWinXSException {
		SaveOrderTemplateResponse response = new SaveOrderTemplateResponse();
		SaveOrderTemplateRequest request = new SaveOrderTemplateRequest();
		
		request.setOrderTemplateID(templateID);
		request.setOrderTemplateName(templateName);
		request.setShared(isShared);
		
		mockSessions();
		userHasAccessToOrderTemplate();
		
		response = serviceToTest.saveOrderTemplate(mockSessionContainer, request);
		
		assertNotNull(response);
		assertFalse(response.isSuccess());
	}
	
	private static Stream<Arguments> saveOrderTemplateFailData() {
	    return Stream.of(Arguments.of("-2", "Test002", "2", false, true)
	    );
	}
	
	//CAP-48975
	@Test
	void that_loadTemplateOrder_success() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(mockOrderOnBehalfVO);
		when(mockOrderOnBehalfVO.isInRequestorMode()).thenReturn(false);

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
		doReturn(true).when(serviceToTest).validateAndLoadAttrForEOO(any(), any(), any(), anyBoolean(), any(), any());

		UseOrderTemplateResponse response = new UseOrderTemplateResponse();
		UseOrderTemplateRequest request = loadOrderTemplateRequest();

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				})) {
			response = serviceToTest.loadTemplateOrder(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}

	//CAP-48975
	@Test
	void that_loadTemplateOrder_return422() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(mockOrderOnBehalfVO);
		when(mockOrderOnBehalfVO.isInRequestorMode()).thenReturn(false);

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);

		UseOrderTemplateResponse response = new UseOrderTemplateResponse();
		UseOrderTemplateRequest request = loadOrderTemplateRequest();

		UseOrderTemplateRequest request2 = loadOrderTemplateRequestText();
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
					// when(mock.getSavedOrderDetailForTemplate(anyInt(), any(), any(), any(),
					// anyInt())).thenReturn(mockOEOrderTemplateDetailsFormBean);
				})) {
			Exception exception = assertThrows(AtWinXSException.class, () -> {
				serviceToTest.loadTemplateOrder(mockSessionContainer, request2);
			});
			assertTrue(exception.getMessage().contains(""));

		}

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				})) {
			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(2);
			response = serviceToTest.loadTemplateOrder(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());

		}

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				}); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)) {
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
					.thenAnswer((Answer<Void>) invocation -> null);
			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);
			doReturn(false).when(serviceToTest).validateBudgetAllocation(any(), any(), any(), any(), any(),
					anyBoolean(), any());
			response = serviceToTest.loadTemplateOrder(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());

		}
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
					when(mock.getSavedOrderDetailForTemplate(anyInt(), any(), any(), any(), anyInt()))
							.thenReturn(mockOEOrderTemplateDetailsFormBean);
				}); MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			mockUtil.when(() -> Util.decryptString(any())).thenThrow(new RuntimeException(""));
			serviceToTest.loadTemplateOrder(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());

		}

		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				}); MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)) {
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
					.thenAnswer((Answer<Void>) invocation -> null);
			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);
			doThrow(new AtWinXSMsgException(null, "")).when(serviceToTest).validateBudgetAllocation(any(), any(), any(),
					any(), any(), anyBoolean(), any());
			response = serviceToTest.loadTemplateOrder(mockSessionContainer, request);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());

		}

	}
	
	//CAP-48975
	@Test
	void that_validateAndLoadAttrForEOO_return422() throws Exception {

		Message errors = new Message();
		when(mockOESessionBean.getUsrSrchOptions()).thenReturn(null);
		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn("");
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				})) {
			boolean isRedirectToCart =	serviceToTest.validateAndLoadAttrForEOO(mockAppSessionBean, null, mockOESavedOrderAssembler, false, errors, mockOESessionBean);
			Assertions.assertEquals(false, isRedirectToCart);
		}
		
		ArrayList orderTempAttrVOs = new ArrayList<>();
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				})) {
			boolean isRedirectToCart =	serviceToTest.validateAndLoadAttrForEOO(mockAppSessionBean, orderTempAttrVOs, mockOESavedOrderAssembler, true, errors, mockOESessionBean);
			Assertions.assertEquals(false, isRedirectToCart);
		}
		OrderTemplateAttributeVO vo = new OrderTemplateAttributeVO(TEST_SITE_ID, TEST_SITE_ID, TEST_SITE_ID, NEW_ORDER_TEMPLATE, null);
		orderTempAttrVOs.add(vo);		
		try (MockedConstruction<OESavedOrderAssembler> mockedOESavedOrderAssembler = mockConstruction(
				OESavedOrderAssembler.class, (mock, context) -> {
				when(mock.validateOrdAttrWithProfileAttr( any(), any(), any(), anyInt())).thenReturn(true);
				})) {
			boolean isRedirectToCart =	serviceToTest.validateAndLoadAttrForEOO(mockAppSessionBean, orderTempAttrVOs, mockOESavedOrderAssembler, true, errors, mockOESessionBean);
			Assertions.assertEquals(false, isRedirectToCart);
		}
	}

	//CAP-48975
	public UseOrderTemplateRequest loadOrderTemplateRequest() {
		UseOrderTemplateRequest req = new UseOrderTemplateRequest();
		req.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX1QWQqWculiu");
		return req;
	}

	//CAP-48975
	public UseOrderTemplateRequest loadOrderTemplateRequestText() {
		UseOrderTemplateRequest req = new UseOrderTemplateRequest();
		req.setOrderTemplateID("NS0OXfhl9YMYqgSdA66ZX/ZpGJ6YE4R9pmY3vVW0Brk=");
		return req;
	}
}
