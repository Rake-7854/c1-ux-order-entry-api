/*
 *	Copyright (c) RR Donnelley. All Rights Reserved.
 *	This software is the confidential and proprietary information of RR Donnelley.
 *	You shall not disclose such confidential information.
 *	
 *	Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  10/16/23    S Ramachandran	CAP-44515					Retrieve Order Approval Alerts -Junits  
 *  10/31/23	Satishkumar A	CAP-44996					C1UX BE - Create service to show if there are any alerts for the logged in user
 *  10/30/23    S Ramachandran	CAP-44469					Retrieve Catalog Alerts - Junits test case
 *  10/30/23    M Sakthi		CAP-44514					Retrieve Item Alerts - JUnits test case
 *    
 */


package com.rrd.c1ux.api.services.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.alert.AlertsCategory;
import com.rrd.c1ux.api.models.alert.AlertsResponse;
import com.rrd.c1ux.api.models.alert.CheckAlertsResponse;
import com.rrd.custompoint.orderentry.entity.RoutedOrders;
import com.rrd.custompoint.orderentry.entity.RoutingApprovalQueue;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.framework.ao.HomePageAssembler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

class AlertsServiceImplTest extends BaseServiceTest {

	public static final String GENERIC_422MESSAGE = "GENERIC_ERROR";
	
	
	public static final String TEST_ORDER = "Order";
	public static final String TEST_INVENTORY = "Inventory";
	public static final String TEST_APP_REV_LEVEL_CD = "E";

	private LinkedHashMap<String, List<AlertCountResponseBean>> testCpOrderAlertsBean;
	private List<RoutingApprovalQueue> testRoutingApprovalQueueLst;
	private LinkedHashMap<String,List<AlertCountResponseBean>> testHashMapAlertsBean;
	private AlertsResponse alertsResponse;
	private CheckAlertsResponse checkAlertsResponse;
	
	@InjectMocks
	private AlertsServiceImpl serviceToTest;

	@BeforeEach
	void setup() {

		alertsResponse = new AlertsResponse();
	}

	
	//CAP-44515 - Starts
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndOrdersAlertsExists() throws AtWinXSException, Exception {
		
		// Test Orders Alerts Exists , RoutingApprovalQueue Exists Samples
		testCpOrderAlertsBean = getCpOrderAlertsBeanOrderCatExist();
		testRoutingApprovalQueueLst = getOrderRoutingApprovalQueueExists();
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		    when(mockEntityObjectMap.getEntity(eq(RoutedOrders.class), any())).thenReturn(mockRoutedOrders);

		    // With Order Alerts   
		    when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_ORDER);
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			when(mockLoginOrderPropertiesVO.getApproverReviewLevelCode()).thenReturn(TEST_APP_REV_LEVEL_CD);
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		    when(mockRoutedOrders.populateRoutingOrderQueue(anyInt(),any())).thenReturn(testRoutingApprovalQueueLst);
			
			alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
	
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndOrdersAlertsEmpty() throws AtWinXSException, Exception {
		
		// Test Orders Alerts Exists, RoutingApprovalQueue is Empty  Samples
		testCpOrderAlertsBean = getCpOrderAlertsBeanOrderCatExist();
		testRoutingApprovalQueueLst = getOrderRoutingApprovalQueueEmpty();
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		    when(mockEntityObjectMap.getEntity(eq(RoutedOrders.class), any())).thenReturn(mockRoutedOrders);

		    // With Order Alerts   
		    when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_ORDER);
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			when(mockLoginOrderPropertiesVO.getApproverReviewLevelCode()).thenReturn(TEST_APP_REV_LEVEL_CD);
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		    when(mockRoutedOrders.populateRoutingOrderQueue(anyInt(),any())).thenReturn(testRoutingApprovalQueueLst);
			
			alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
	
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndOrdersAlertsIsNULL() throws AtWinXSException, Exception {
		
		// Test Orders Alerts Exists, RoutingApprovalQueue is NULL Samples
		testCpOrderAlertsBean = getCpOrderAlertsBeanOrderCatExist();
		testRoutingApprovalQueueLst = null;
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
		    // With Order Alerts   
		    when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_ORDER);
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			when(mockLoginOrderPropertiesVO.getApproverReviewLevelCode()).thenReturn(TEST_APP_REV_LEVEL_CD);
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
		    //when(mockRoutedOrders.populateRoutingOrderQueue(anyInt(),any())).thenReturn(testRoutingApprovalQueueLst);
			
		    alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
		
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndOrderAlertsBeanIsNULL() throws AtWinXSException, Exception {
		
		// Test Orders Alerts is NULL , RoutingApprovalQueue Exists Samples
		testCpOrderAlertsBean = null;
		testRoutingApprovalQueueLst = getOrderRoutingApprovalQueueExists();
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			
		    alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
	
	
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndOrderAlertsBeanIsEmpty() throws AtWinXSException, Exception {
		
		// Test Orders Alerts is NULL , RoutingApprovalQueue Exists Samples
		testCpOrderAlertsBean = getCpOrderAlertsBeanIsEmpty();
		testRoutingApprovalQueueLst = getOrderRoutingApprovalQueueExists();
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);

		    alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
	
	
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndOrdersAlertsThrowsSQLException() throws AtWinXSException, Exception {
		
		// Test Orders Alerts Exists, RoutingApprovalQueue Exists, but throws Exception Samples
		testCpOrderAlertsBean = getCpOrderAlertsBeanOrderCatExist();
		testRoutingApprovalQueueLst = getOrderRoutingApprovalQueueExists();
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		    when(mockEntityObjectMap.getEntity(eq(RoutedOrders.class), any())).thenReturn(mockRoutedOrders);

		    // With Order Alerts   
		    when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_ORDER);
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			when(mockLoginOrderPropertiesVO.getApproverReviewLevelCode()).thenReturn(TEST_APP_REV_LEVEL_CD);
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		    when(mockRoutedOrders.populateRoutingOrderQueue(anyInt(),any())).thenThrow(new AtWinXSException("testing", "MyJunitsClass"));
			
		    alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
	
	@Test
	void that_getAlerts_returnSuccess200_WithOrderServiceAndNoOrdersCatAlerts() throws AtWinXSException, Exception {
		
		// Test Other Alerts Exists But No Order Alert Category, RoutingApprovalQueue Samples
		testCpOrderAlertsBean = getCpOrderAlertsBeanWithNoOrderCat();   
		testRoutingApprovalQueueLst = getOrderRoutingApprovalQueueExists();
		
		try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
				(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
				) {

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
			when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
			when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
			
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		    when(mockEntityObjectMap.getEntity(eq(RoutedOrders.class), any())).thenReturn(mockRoutedOrders);
			
		    // With Order Alerts   
		    when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_ORDER);
		    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			when(mockLoginOrderPropertiesVO.getApproverReviewLevelCode()).thenReturn(TEST_APP_REV_LEVEL_CD);
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
			
		    alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
			assertNotNull(alertsResponse);
			assertTrue(alertsResponse.isSuccess());
		}
	}
	
	// Test Orders Alerts Samples for OrderAlertsBean retrieved through getAlertsForHomePage()    
	private LinkedHashMap<String, List<AlertCountResponseBean>> getCpOrderAlertsBeanOrderCatExist() {
		
		AlertCounts []  arrAlertCounts = new AlertCounts[2];
		arrAlertCounts[0] = new AlertCounts("","Orders Awaiting Approval",1,1);
		arrAlertCounts[1] = new AlertCounts("","Backordered",3,1);
		AlertCountResponseBean alertsBean = new AlertCountResponseBean(arrAlertCounts);
		alertsBean.setActionID(""); 
		
		ArrayList<AlertCountResponseBean> arrLstAlertsBean = new ArrayList<>();
		arrLstAlertsBean.add(alertsBean);
		
		LinkedHashMap<String,List<AlertCountResponseBean>> hashMapAlertsBean = new LinkedHashMap<String,List<AlertCountResponseBean>>();
		hashMapAlertsBean.put("Order",arrLstAlertsBean);
		
		return hashMapAlertsBean;
	}
	
	// Test Orders Alerts Samples for OrderAlertsBean retrieved through getAlertsForHomePage()    
	private LinkedHashMap<String, List<AlertCountResponseBean>> getCpOrderAlertsBeanWithNoOrderCat() {
		
		AlertCounts []  arrAlertCounts = new AlertCounts[2];
		arrAlertCounts[0] = new AlertCounts("","Orders Awaiting Approval",1,1);
		arrAlertCounts[1] = new AlertCounts("","Backordered",3,1);
		AlertCountResponseBean alertsBean = new AlertCountResponseBean(arrAlertCounts);
		alertsBean.setActionID("");
		
		ArrayList<AlertCountResponseBean> arrLstAlertsBean = new ArrayList<>();
		arrLstAlertsBean.add(alertsBean);
		
		LinkedHashMap<String,List<AlertCountResponseBean>> hashMapAlertsBean = new LinkedHashMap<String,List<AlertCountResponseBean>>();
		hashMapAlertsBean.put("Item",arrLstAlertsBean);
		
		return hashMapAlertsBean;
	}
	
	// Test Orders Alerts Samples for OrderAlertsBean retrieved through getAlertsForHomePage()    
	private LinkedHashMap<String, List<AlertCountResponseBean>> getCpOrderAlertsBeanIsEmpty() {
		LinkedHashMap<String,List<AlertCountResponseBean>> hashMapAlertsBean = new LinkedHashMap<String,List<AlertCountResponseBean>>();
		return hashMapAlertsBean;
	}
	

	// Test RoutingApprovalQueue with Order Alerts Exists Samples for populateRoutingOrderQueue()
	private List<RoutingApprovalQueue> getOrderRoutingApprovalQueueExists() {
		
		List<RoutingApprovalQueue> queue = new ArrayList<RoutingApprovalQueue>();  
		
		RoutingApprovalQueue bean = new RoutingApprovalQueue();
		bean.setApprovalQueueName("Order Approval Queue");
		bean.setApprovalQueueID(1781);
		bean.setApprovalQueueCount(1);
		queue.add(bean);
		
		bean = new RoutingApprovalQueue();
		bean.setApprovalQueueName("Release Items Approval Queue");
		bean.setApprovalQueueID(1782);
		bean.setApprovalQueueCount(0);
		queue.add(bean);
		return queue;
	}
	
	// Test RoutingApprovalQueue with Empty Order Samples for populateRoutingOrderQueue()
	private List<RoutingApprovalQueue> getOrderRoutingApprovalQueueEmpty() {
		
		List<RoutingApprovalQueue> queue = new ArrayList<RoutingApprovalQueue>();  
		return queue;
	}
	
	//CAP-44743 Inventory Alerts test case
		@Test
		void that_getAlerts_returnSuccess200_WithInveteryServiceAndInventorysAlertsExists() throws AtWinXSException, Exception {
			
			// Test Inventory Alerts Exists , Back Ordered, Low Stock and Warning:Low Stock Exists Samples
			testCpOrderAlertsBean = getCpInventoryAlertsBeanInventoryExist();
			testHashMapAlertsBean = null;
			
			try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
					(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
					) {

				when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
				when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
				when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
				when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
				when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
				when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
				
			   // With Inventory Alerts   
			   	when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
				
				alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
				assertNotNull(alertsResponse);
				assertTrue(alertsResponse.isSuccess());
			}
		}
		
		
		
		// 
		@Test
		void that_getAlerts_returnSuccess200_WithInveteryServiceAndInventorysAlertsEmpty() throws AtWinXSException, Exception {

			// Test Inventory Alerts Exists , Back Ordered, Low Stock and Warning:Low Stock Exists empty
			testCpOrderAlertsBean = null;
			testHashMapAlertsBean = getCpOrderAlertsBeanIsEmpty();
			
			try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
					(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
					) {

				when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
				when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
				when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
				when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
				when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
				
				// With Order Alerts   
			    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			    
				alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
				assertNotNull(alertsResponse);
				assertTrue(alertsResponse.isSuccess());
		  }
		
		}	
		
		// Test Inventory Alerts Samples for InventoryAlertsBean retrieved through getAlertsForHomePage()    
		private LinkedHashMap<String, List<AlertCountResponseBean>> getCpInventoryAlertsBeanInventoryExist() {
			
			AlertCounts []  arrAlertCounts = new AlertCounts[3];
			
			arrAlertCounts[0] = new AlertCounts("BKO","Backordered",3,0,0,"");
			arrAlertCounts[1] = new AlertCounts("LST","Low Stock",3,2,0,"");
			arrAlertCounts[2] = new AlertCounts("LOWSTK","WARNING Low Stock",3,1,1002,"N");
			
			AlertCountResponseBean alertsBean = new AlertCountResponseBean(arrAlertCounts);
				
			ArrayList<AlertCountResponseBean> arrLstAlertsBean = new ArrayList<>();
			arrLstAlertsBean.add(alertsBean);
			
			LinkedHashMap<String,List<AlertCountResponseBean>> hashMapAlertsBean = new LinkedHashMap<String,List<AlertCountResponseBean>>();
			hashMapAlertsBean.put("Inventory",arrLstAlertsBean);
			
			return hashMapAlertsBean;
		}
		
		//CAP-44469:Starts
		@Test
		void that_getAlerts_returnSuccess200_WithCatalogServiceAndCatalogAlertExists() throws AtWinXSException, Exception {
			
			// Test Catalog Alerts Exists , Invalid Item Alert Samples
			testCpOrderAlertsBean = getCpCatalogAlertsBeanCatalogExist();
			try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
					(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
					) {

				when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
				when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
				when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
				when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
				when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
				when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
				
			   // With Catalog Alerts   
			   	when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
				
				alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
				assertNotNull(alertsResponse);
				assertTrue(alertsResponse.isSuccess());
			}
		}
		
		@Test
		void that_getAlerts_returnSuccess200_WithCatalogServiceAndCatalogAlertIsEmpty() throws AtWinXSException, Exception {

			// Test Catalog Alerts Exists , Invalid Item is empty
			testCpOrderAlertsBean = null;
			getCpOrderAlertsBeanIsEmpty();
			
			try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito.mockConstruction(HomePageAssembler.class, 
					(mock, context) -> { doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(), any()); })
					) {

				when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
				when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
				when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIOrderAdmin);
				when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN)).thenReturn(mockIServiceInterface);
				when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);
				
				// With Order Alerts   
			    when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
			    
				alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
				assertNotNull(alertsResponse);
				assertTrue(alertsResponse.isSuccess());
		  }
		
		}	
		
		// Catalog Alerts Samples for CatalogAlertsBean retrieved through getAlertsForHomePage()    
		private LinkedHashMap<String, List<AlertCountResponseBean>> getCpCatalogAlertsBeanCatalogExist() {
			
			AlertCounts []  arrAlertCounts = new AlertCounts[1];
			
			//alertCd =INV , alertName=Invalid Item , alertSrvcId=7, alertCount=1
			arrAlertCounts[0] = new AlertCounts("INV","Invalid Item",7,1,0,"");
			
			AlertCountResponseBean alertsBean = new AlertCountResponseBean(arrAlertCounts);
			
			ArrayList<AlertCountResponseBean> arrLstAlertsBean = new ArrayList<>();
			arrLstAlertsBean.add(alertsBean);
			
			LinkedHashMap<String,List<AlertCountResponseBean>> hashMapAlertsBean = new LinkedHashMap<String,List<AlertCountResponseBean>>();
			hashMapAlertsBean.put("Inventory",arrLstAlertsBean);
			
			return hashMapAlertsBean;
		}
		//CAP-44469:End
		
		//CAP-44996 - Starts
		@Test
		void that_checkAlerts_returnSuccess200_WithOrderServiceAndOrdersAlertsDoesNotExists() throws AtWinXSException, Exception {
			
				alertsResponse = new AlertsResponse();
				serviceToTest = Mockito.spy(serviceToTest);
			    doReturn(alertsResponse).when(serviceToTest).getAlerts(mockSessionContainer);
				checkAlertsResponse = serviceToTest.checkAlerts(mockSessionContainer);
				assertNotNull(checkAlertsResponse);
				assertEquals(0,checkAlertsResponse.getCount());
				assertEquals(false,checkAlertsResponse.isAlertsExist());
				assertTrue(checkAlertsResponse.isSuccess());
				
				Collection<AlertsCategory> arrLstAlertCategory = new ArrayList<>();
				alertsResponse.setAlertsCategory(arrLstAlertCategory);
				checkAlertsResponse = serviceToTest.checkAlerts(mockSessionContainer);
				assertNotNull(checkAlertsResponse);
				assertEquals(0,checkAlertsResponse.getCount());
				assertEquals(false,checkAlertsResponse.isAlertsExist());
				assertTrue(checkAlertsResponse.isSuccess());


		}
		
		//CAP-44996 
		@Test
		void that_checkAlerts_returnSuccess200_WithOrderServiceAndOrdersAlertsExists() throws AtWinXSException, Exception {
			
				alertsResponse = new AlertsResponse();
				Collection<AlertsCategory> arrLstAlertCategory = new ArrayList<>();
				AlertsCategory alertsCategory = new AlertsCategory();
				alertsCategory.setCategoryCount(6);
				arrLstAlertCategory.add(alertsCategory);
				alertsResponse.setAlertsCategory(arrLstAlertCategory);
				
				serviceToTest = Mockito.spy(serviceToTest);
			    doReturn(alertsResponse).when(serviceToTest).getAlerts(mockSessionContainer);
				checkAlertsResponse = serviceToTest.checkAlerts(mockSessionContainer);
				assertNotNull(checkAlertsResponse);
				assertEquals(6,checkAlertsResponse.getCount());
				assertEquals(true,checkAlertsResponse.isAlertsExist());
				
				assertTrue(checkAlertsResponse.isSuccess());

		}
		
		
		// 44514: Start

		@Test
		void that_getAlerts_returnSuccess200_WithItemServiceAndItemAlertExists() throws AtWinXSException, Exception {

			// Test Item Alerts Exists , Invalid Item Alert Samples
			testCpOrderAlertsBean = getCpItemAlertsBeanItemExist();
			testHashMapAlertsBean = null;

			try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito
					.mockConstruction(HomePageAssembler.class, (mock, context) -> {
						doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(),
								any());
					})) {

				when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
				when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
				when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
				when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
				when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
						.thenReturn(mockIServiceInterface);
				when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);

				// With Item Alerts
				when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);

				alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
				assertNotNull(alertsResponse);
				assertTrue(alertsResponse.isSuccess());
			}
		}

		@Test
		void that_getAlerts_returnSuccess200_WithItemServiceAndItemAlertIsEmpty() throws AtWinXSException, Exception {

			// Test Item Alerts Exists , Invalid Item is empty
			testCpOrderAlertsBean = null;
			testHashMapAlertsBean = getCpItemAlertsBeanItemExist();

			try (MockedConstruction<HomePageAssembler> mockedHomePageAssembler = Mockito
					.mockConstruction(HomePageAssembler.class, (mock, context) -> {
						doReturn(testCpOrderAlertsBean).when(mock).getAlertsForHomePage(any(), any(), any(), any(),
								any());
					})) {

				when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
				when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
				when(mockoaOrderAdminService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
						.thenReturn(mockIOrderAdmin);
				when(mockscLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
						.thenReturn(mockIServiceInterface);
				when(mockIOrderAdmin.getLoginOrderDetails(any())).thenReturn(mockLoginOrderPropertiesVO);

				when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);

				alertsResponse = serviceToTest.getAlerts(mockSessionContainer);
				assertNotNull(alertsResponse);
				assertTrue(alertsResponse.isSuccess());
			}

		}

		// Item Alerts Samples retrieved through getAlertsForHomePage()
		private LinkedHashMap<String, List<AlertCountResponseBean>> getCpItemAlertsBeanItemExist() {

			AlertCounts[] arrAlertCounts = new AlertCounts[4];

			// alertCd =BKO , alertName=Backordered, alertSrvcId=4, alertCount=1
			arrAlertCounts[0] = new AlertCounts("BKO", "Backordered", 4, 1, 0, "");
			// alertCd =FUN, alertName=Follow Up Note, alertSrvcId=4, alertCount=1
			arrAlertCounts[1] = new AlertCounts("FUN", "Follow Up Note", 4, 1, 0, "");
			// alertCd =INV, alertName=Invalid Item, alertSrvcId=4, alertCount=1
			arrAlertCounts[2] = new AlertCounts("INV", "Invalid Item", 4, 1, 0, "");
			// alertCd =LST, alertName=Low Stock, alertSrvcId=4, alertCount=1
			arrAlertCounts[3] = new AlertCounts("LST", "Low Stock", 4, 1, 0, "");

			AlertCountResponseBean alertsBean = new AlertCountResponseBean(arrAlertCounts);

			ArrayList<AlertCountResponseBean> arrLstAlertsBean = new ArrayList<>();
			arrLstAlertsBean.add(alertsBean);

			LinkedHashMap<String, List<AlertCountResponseBean>> hashMapAlertsBean = new LinkedHashMap<String, List<AlertCountResponseBean>>();
			hashMapAlertsBean.put("Item", arrLstAlertsBean);

			return hashMapAlertsBean;
		}
		// CAP-44514:End
}
