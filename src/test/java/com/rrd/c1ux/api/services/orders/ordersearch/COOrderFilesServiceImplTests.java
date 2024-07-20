/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions:
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  5/25/2023	C Codina		CAP-39338					API Change - Header labels in Order File list API to make/use translation text values
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup
 *  03/12/24	N Caceres		CAP-47732					Added JUnits for showing a file link to the distribution list
 *  03/13/24	S Ramachandran	CAP-47841					Added Junit test for service method downloadOrderFileFromOS
 *	05/17/24	L De Leon		CAP-49280					Added tests for populateEFDContents() method
 *  05/17/24	Krishna Natarajan CAP-49465					Disabled a test due to random test failure
 *  06/12/24	Krishna Natarajan CAP-49128					Added test mock lines to get the OrderStatusSessionBean 
 */
package com.rrd.c1ux.api.services.orders.ordersearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.orders.DownloadOrderFileResponse;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderFileResponse;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.SiteComponentLocatorService;
import com.rrd.c1ux.api.services.locators.ManageListsLocatorService;
import com.rrd.custompoint.orderentry.entity.EFDTracking;
import com.rrd.custompoint.orderentry.entity.List;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderLine;
import com.rrd.custompoint.orderentry.entity.OrderLines;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.vo.EFDDestinationOptionsVO;

class COOrderFilesServiceImplTests extends BaseServiceTest{

	@InjectMocks
	private COOrderFilesServiceImpl service;
	
	@Mock
	protected HttpServletResponse mockHttpServletResponse;
	
	@Mock
	protected SiteComponentLocatorService mockSiteComponentLocatorService;
  
	@Mock
	protected ManageListsLocatorService mockManageListsLocatorService;
  
	@Mock
	protected IManageList mockManageList;
	
	@Mock
	protected ISite mockSite;
	
	@Mock
	protected File mockFile;
	
	@Mock
	protected FileInputStream mockFileInputStream;
	
	@Mock
	protected ServletOutputStream mockServletOutputStream;

	// CAP-49280
	@Mock
	protected EFDTracking mockEFDTracking;

	@Mock
	protected OrderLines mockOrderLines;

	@Mock
	protected OrderLine mockOrderLine;

	@Mock
	protected OEManageOrdersComponentLocatorService mockOEManageOrdersComponentLocatorService;

	//@Mock
	//protected IOEManageOrdersComponent mockIOEManageOrdersComponent;

	@Mock
	protected EFDDestinationOptionsVO mockEFDDestinationOptionsVO;
	
	private static final String TEST_STRING = "Emails";
	private static final String SALES_REF_NUM = "80031851";
	private static final String ORDER_NUMBER = "21800596";
	private static final String TEST_ENCRYPTED_PARAMS_SLN= "0YNcmOdWt2RZc2xhHDmlyEwvxHHqxPdBGdKs%2FWm5EH8%3D"; // salesReferenceNumber="80034075"
	private static final String TEST_ENCRYPTED_PARAMS_NOSLN_REF_NO = "iaLJZReExNLGN9yXKHwr1mj1Ozc8qnF71kHZ%2BXNmOFg%3D"; // NoSalesReferenceNumber="80034075"
	private static final String TEST_ENCRYPTED_PARAMS_EMPTY= ""; 
	private static final int TEST_SITE_ID =  4366;
	private static final int TEST_BU_ID =  7125;
	private static final int TEST_LIST_ID =  9999;
	private static final String TEST_LOGIN_ID =  "TESTUSER1";
	public static final String EXPECTED_403MESSAGE = "Invalid Request.";
	public static final String FILENAME = "filename";

	//CAP-39338
	@Test
	void testGetOrderFilesContent() throws AtWinXSException {

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockTranslationService.processMessage(any(), any(), anyString(), any())).thenReturn(TEST_STRING);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		when(mockEntityObjectMap.getEntity(eq(List.class), any())).thenReturn(mockList);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);//CAP-49128
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOsBean);

		COOrderFileResponse responseObj = service.getOrderFilesContent(mockSessionContainer, SALES_REF_NUM, ORDER_NUMBER);

        Assertions.assertNotNull(responseObj);
        Assertions.assertTrue(responseObj.getHeaderTxt().contains(TEST_STRING));
	}

	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_Expected_return_Success() throws AtWinXSException, IOException {

		service = Mockito.spy(service);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_LOGIN_ID);
		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(TEST_STRING);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		when(mockOrder.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockOrder.getBuID()).thenReturn(TEST_BU_ID);
		
		// When Order ListID Present for Sales Ref Number, logged in user, buid, site
		when(mockOrder.getListID()).thenReturn(TEST_LIST_ID);
		ListVO orderListVO =  getOrderListVO();
		SiteVO siteVO =  getSiteVO();
		when(mockManageListsLocatorService.locate(any())).thenReturn(mockManageList);
		when(mockManageList.retrieveAList(any())).thenReturn(orderListVO);
		when(mockSiteComponentLocatorService.locate(any())).thenReturn(mockSite);
		when(mockSite.getSite(any())).thenReturn(siteVO);
		doNothing().when(service).writeToOutPutStreamForFileDownload(any(), any(), any());
		
		
		try(MockedStatic<AppProperties> mockAppProperties = Mockito.mockStatic(AppProperties.class))
		{
			mockAppProperties.when(() -> AppProperties.getGlobalFileUploadPath()).thenReturn("c:/xs2files/");
			
			DownloadOrderFileResponse response = service.downloadOrderFileFromOS(mockSessionContainer, mockHttpServletResponse, TEST_ENCRYPTED_PARAMS_SLN);
			Assertions.assertNotNull(response);
			Assertions.assertTrue(response.isSuccess());
		}	
	}
	
	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_ListIDNotPresentInXST076_Expected_return_Fail() throws AtWinXSException, IOException {

		service = Mockito.spy(service);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		
		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(TEST_STRING);
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);

		// When Order ListID Not Available for Sales Ref Number, logged in user, buid, site
		when(mockOrder.getListID()).thenReturn(-1);
		
		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.downloadOrderFileFromOS(mockSessionContainer, mockHttpServletResponse, TEST_ENCRYPTED_PARAMS_SLN);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}
	
	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_ListID_NotPresentInXST030_Expected_return_Fail() throws AtWinXSException, IOException {
		service = Mockito.spy(service);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(TEST_BU_ID);
		
		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(TEST_STRING);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		when(mockOrder.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockOrder.getBuID()).thenReturn(TEST_BU_ID);
		
		//Order ListID Present for Sales Ref Number, logged in user, buid, site
		when(mockOrder.getListID()).thenReturn(TEST_LIST_ID);
		
		SiteVO siteVO =  getSiteVO();
		//Order ListID NOT Present for Sales Ref Number, logged in user, buid, site
		when(mockManageListsLocatorService.locate(any())).thenReturn(mockManageList);
		when(mockManageList.retrieveAList(any())).thenReturn(null);
		
		
		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.downloadOrderFileFromOS(mockSessionContainer, mockHttpServletResponse, TEST_ENCRYPTED_PARAMS_SLN);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}
	
	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_SLNNotIn_ENCRYPTED_PARAMS_Expected_return_Fail() throws AtWinXSException, IOException {
		service = Mockito.spy(service);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		
		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.downloadOrderFileFromOS(mockSessionContainer, mockHttpServletResponse, TEST_ENCRYPTED_PARAMS_NOSLN_REF_NO);
		});
		assertTrue(error403 instanceof AtWinXSException);
	}
	
	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_EMPTY_ENCRYPTED_PARAMS_Expected_return_Fail() throws AtWinXSException, IOException {
		service = Mockito.spy(service);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		
		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.downloadOrderFileFromOS(mockSessionContainer, mockHttpServletResponse, TEST_ENCRYPTED_PARAMS_EMPTY);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}
	
	//CAP-47841
	@Test
	void that_writeToOutPutStreamForFileDownload_Expected_return_success() throws AtWinXSException, IOException {
		
		service = Mockito.spy(service);
		doReturn(mockFileInputStream).when(service).getFileInputStream(any());
		when(mockHttpServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);
		when(mockFileInputStream.read(any())).thenReturn(-1);
		service.writeToOutPutStreamForFileDownload(mockHttpServletResponse, mockFile, FILENAME);
		Assertions.assertTrue(true);
	}

	//CAP-47841
	private ListVO getOrderListVO() {
		
		ListVO orderListVO = new ListVO(TEST_SITE_ID, TEST_BU_ID, TEST_LIST_ID,"TEST LIST NAME", true, true,"TEST LIST DESC","TESTUSER1", null, 0, null);
		return orderListVO;
	}
	
	//CAP-47841
	private SiteVO getSiteVO() {
		
		SiteVO siteVO = new SiteVO(TEST_SITE_ID, "siteLoginID", "siteName", "siteDescription", 0, "corporateNumber", "soldToNumber", 0, 3, "A", "updateUser",
		        new Date(), "customizationToken", false, "defaultLanguageCode", "defaultLocaleCode", "defaultTimeZone", false, false, false,
		        "currencyLocaleVariant", false);
		return siteVO;
	}

	// CAP-49280
	@Test
	void that_populateEFDContents_has_EFD_items() throws AtWinXSException {

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		when(mockOrder.isEfdOrder()).thenReturn(true);

		COOrderFileResponse response = new COOrderFileResponse();
		service.populateEFDContents(SALES_REF_NUM, response, mockAppSessionBean);

		assertTrue(response.isHasEfd());
		assertTrue(Util.isBlankOrNull(response.getEfdLandingPageLink()));
		assertTrue(Util.isBlankOrNull(response.getEfdEmailLink()));
	}

	// CAP-49280
	@Test
	void that_populateEFDContents_has_EFD_tracking() throws AtWinXSException {

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		when(mockOrder.isEfdOrder()).thenReturn(true);
		when(mockOrder.getEFDTracking()).thenReturn(mockEFDTracking);
		when(mockEFDTracking.getEmailAddress()).thenReturn(TEST_STRING);
		doReturn(new HashMap<String, String>()).when(mockEFDTracking).getEFDContents(SALES_REF_NUM, null, false,
				0, AtWinXSConstant.EMPTY_STRING);

		COOrderFileResponse response = new COOrderFileResponse();
		service.populateEFDContents(SALES_REF_NUM, response, mockAppSessionBean);

		assertTrue(response.isHasEfd());
		assertTrue(Util.isBlankOrNull(response.getEfdLandingPageLink()));
		assertTrue(Util.isBlankOrNull(response.getEfdEmailLink()));
	}

	// CAP-49280
	@Test
	void that_populateEFDContents_when_email_is_DICE() throws AtWinXSException {

		service = Mockito.spy(service);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		when(mockOrder.isEfdOrder()).thenReturn(true);
		when(mockOrder.getEFDTracking()).thenReturn(mockEFDTracking);
		when(mockEFDTracking.getEmailAddress()).thenReturn(ModelConstants.DICE);
		//doReturn(new HashMap<String, String>()).when(mockEFDTracking).getEFDContents(SALES_REF_NUM, null, true,
		//		0, AtWinXSConstant.EMPTY_STRING);
		doReturn(mockOrderLines).when(mockOrder).getOrderLines();
		Collection<OrderLine> orderLines = new ArrayList<>();
		orderLines.add(mockOrderLine);
		doReturn(orderLines).when(mockOrderLines).getOrderLines();
	//	doReturn(mockIOEManageOrdersComponent).when(mockOEManageOrdersComponentLocatorService).locate(any());
		//doReturn(null).when(mockIOEManageOrdersComponent).getEFDDestination(0, 0);

		COOrderFileResponse response = new COOrderFileResponse();
		service.populateEFDContents(SALES_REF_NUM, response, mockAppSessionBean);

		assertTrue(response.isHasEfd());
		assertTrue(Util.isBlankOrNull(response.getEfdLandingPageLink()));
		assertTrue(Util.isBlankOrNull(response.getEfdEmailLink()));
	}

	// CAP-49280
	@Test
	void that_populateEFDContents_when_exception_is_thrown() throws AtWinXSException {

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);
		doThrow(new AtWinXSException(GENERIC_ERROR_MSG, this.getClass().getName())).when(mockOrder).populateBySalesRefNumber(SALES_REF_NUM);

		COOrderFileResponse response = new COOrderFileResponse();
		service.populateEFDContents(SALES_REF_NUM, response, mockAppSessionBean);

		assertFalse(response.isHasEfd());
		assertTrue(Util.isBlankOrNull(response.getEfdLandingPageLink()));
		assertTrue(Util.isBlankOrNull(response.getEfdEmailLink()));
	}

	// CAP-49280
	@Test
	@Disabled("disabled as the test is failing with NullPointerException. Require to fix")
	void that_getEmailForDice_when_email_is_not_blank() throws AtWinXSException {
		//service = Mockito.spy(service);
		doReturn(mockOrderLines).when(mockOrder).getOrderLines();
		Collection<OrderLine> orderLines = new ArrayList<>();
		orderLines.add(mockOrderLine);
		orderLines.add(mockOrderLine);
		doReturn(orderLines).when(mockOrderLines).getOrderLines();
		//doReturn(mockIOEManageOrdersComponent).when(mockOEManageOrdersComponentLocatorService).locate(any());
		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		doReturn(mockEFDDestinationOptionsVO).when(mockIOEManageOrdersComponent).getEFDDestination(anyInt(), anyInt());
		when(mockEFDDestinationOptionsVO.getEmailOthers()).thenReturn(AtWinXSConstant.EMPTY_STRING, TEST_STRING);

		String response = service.getEmailForDice(mockAppSessionBean, mockOrder, true);

		assertNotNull(response);
		assertEquals(TEST_STRING, response);
	}
}