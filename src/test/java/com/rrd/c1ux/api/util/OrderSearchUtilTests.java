/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	11/20/23	N Caceres		CAP-45040					JUnits for adding customer reference 1 - 3 search criteria
 *	11/20/23	C Codina		CAP-45054					Junits for Wcss Order Number
 *	11/27/23	M Sakthi		CAP-45057					Junit for Ship To Name and Ship To Attention as search criteria for Order Search
 */

package com.rrd.c1ux.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COSharedSearchCriteriaRequest;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchCriteria;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchCriteriaImpl;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

class OrderSearchUtilTests extends BaseServiceTest{
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final String CARD_NO_A="***********1234";
	private static final String CARD_NO_VM="************1234";
	private static final String EXP_MONTH_YEAR="02/23";
	private static final String CARD_EMPTY="";
	private static final String EXP_MONTH_YEAR_EMPTY="";
	
	@InjectMocks
	OrderSearchUtil serviceToTest;
	
	@Mock
	SimpleDateFormat mockSDF;
	
	@Mock
	OrderStatusSearchCriteria mockOrderStatusSearchCriteria;
	   
	@Test
	void testPopulateCardNumberTypeAm() throws AtWinXSException {
		String cardNo=OrderSearchUtil.populateCardNumber("A", "1234");
		assertEquals(CARD_NO_A,cardNo);
		
	}
	
	@Test
	void testPopulateCardNumberTypeVisa() throws AtWinXSException {
		String cardNo=OrderSearchUtil.populateCardNumber("V", "1234");
		assertEquals(CARD_NO_VM,cardNo);
		
	}
	
	@Test
	void testPopulateCardNumberTypeMaster() throws AtWinXSException {
		String cardNo=OrderSearchUtil.populateCardNumber("M", "1234");
		assertEquals(CARD_NO_VM,cardNo);
		
	}
	
	@Test
	void testPopulateCardNumberEmpty() throws AtWinXSException {
		String cardNo=OrderSearchUtil.populateCardNumber("", "");
		assertEquals(CARD_EMPTY,cardNo);
		
	}
	
	@Test
	void testpopulateSavedExpirationDate() throws AtWinXSException {
		String exMonthYear=OrderSearchUtil.populateSavedExpirationDate("02", "23");
		assertEquals(EXP_MONTH_YEAR,exMonthYear);
		
	}
	
	
	@Test
	void testpopulateSavedExpirationDateMonthEmpty() throws AtWinXSException {
		String exMonthYear=OrderSearchUtil.populateSavedExpirationDate("", "23");
		assertEquals(EXP_MONTH_YEAR_EMPTY,exMonthYear);
		
	}
	
	@Test
	void testpopulateSavedExpirationDateYearEmpty() throws AtWinXSException {
		String exMonthYear=OrderSearchUtil.populateSavedExpirationDate("02", "");
		assertEquals(EXP_MONTH_YEAR_EMPTY,exMonthYear); 
		
	}

	@Test 
	void testGetOrderStatusSearchCriteria() {
		COOrderSearchRequest searchData = buildSearchData();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		try (MockedStatic<ObjectMapFactory> mockObjectMapFactory = mockStatic(ObjectMapFactory.class)) {
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrderStatusSearchCriteria);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			OrderStatusSearchCriteria searchCriteria = OrderSearchUtil.getOrderStatusSearchCriteria(searchData, mockAppSessionBean, sdf);
			assertNotNull(searchCriteria);
		} 
	}
		
	@ParameterizedTest
	@MethodSource("getTestData")
	void testValidateCriteriaName_custRef(String criteria) {
		OrderStatusSearchCriteria searchCriteria = new OrderStatusSearchCriteriaImpl();
		Map<String, Integer> ordering = new HashMap<>();
		int idx = 0;
		assertEquals(1, OrderSearchUtil.validateCriteriaName(searchCriteria, ordering, idx, buildSearchCriteriaRequest(criteria)));
	}
	
	
	//CAP-45055
	
	@Test
	void testgetOrderStatusSearchCriteriaInvoiceNo() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestForSearchInvoiceNo();
		String criteriaName="CriteriaInvoiceNumber";
		try(MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)){
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockOrderStatusSearchCriteria);
			when(mockOrderStatusSearchCriteria.getSalesReferenceNbr()).thenReturn(criteriaName);
			OrderStatusSearchCriteria resp=OrderSearchUtil.getOrderStatusSearchCriteria(request, mockAppSessionBean, mockSDF);
			assertEquals(criteriaName, resp.getSalesReferenceNbr());
		}	
	}
	
	@Test
	void testgetOrderStatusSearchCriteriaOrderName() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestForSearchOrderName();
		String criteriaName="CriteriaOrderTitle";
		try(MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)){
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockOrderStatusSearchCriteria);
			when(mockOrderStatusSearchCriteria.getPurchaseOrdNbr()).thenReturn(criteriaName);
			OrderStatusSearchCriteria resp=OrderSearchUtil.getOrderStatusSearchCriteria(request, mockAppSessionBean, mockSDF);
			assertEquals(criteriaName, resp.getPurchaseOrdNbr());
		}	
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
	
	private COOrderSearchRequest coOrderSearchRequestForSearchOrderName() { 
    	COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaOrderTitle");
		criteriaRequest.setCriteriaFieldValue("RDG Test");
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
	
	private COOrderSearchRequest buildSearchData() {
		COOrderSearchRequest searchData = new COOrderSearchRequest();
		ArrayList<COSharedSearchCriteriaRequest> searchCriteriaRequest = buildSearchCriteriaRequests();
		searchData.setFromDate("11/01/2023");
		searchData.setToDate("12/01/2023");
		searchData.setScope("M");
		searchData.setSearchCriteriaRequest(searchCriteriaRequest);
		return searchData;
	}
	
	private ArrayList<COSharedSearchCriteriaRequest> buildSearchCriteriaRequests() {
		ArrayList<COSharedSearchCriteriaRequest> searchCriteriaRequests = new ArrayList<>();
		COSharedSearchCriteriaRequest searchCriteriaRequest = buildSearchCriteriaRequest("CriteriaCustRef1");
		searchCriteriaRequests.add(searchCriteriaRequest);
		return searchCriteriaRequests;
	}

	private COSharedSearchCriteriaRequest buildSearchCriteriaRequest(String criteria) {
		COSharedSearchCriteriaRequest searchCriteriaRequest = new COSharedSearchCriteriaRequest();
		searchCriteriaRequest.setCriteriaName(criteria);
		searchCriteriaRequest.setCriteriaFieldValue("Criteria Value");
		searchCriteriaRequest.setCriteriaFieldModifier(">=");
		return searchCriteriaRequest;
	}

	private static Stream<Arguments> getTestData() {
		return Stream.of(Arguments.of("CriteriaCustRef1"),
				Arguments.of("CriteriaCustRef1"),
				Arguments.of("CriteriaCustRef2"),
				Arguments.of("CriteriaCustRef3"),
				Arguments.of("CriteriaItemNumber"),
				Arguments.of("CriteriaStatusCode"),
				Arguments.of("CriteriaSalesRef"),
				Arguments.of("CriteriaOrderNumber"),
				Arguments.of("CriteriaShipToCountry"),
				Arguments.of("CriteriaShipToState"),
				Arguments.of("CriteriaShipToZip"));
	}
	
	//CAP-45057
	private COOrderSearchRequest coOrderSearchRequestForShipToName() { 
    	COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaShipToName");
		criteriaRequest.setCriteriaFieldValue("Test");
		criteriaRequest.setCriteriaFieldModifier("");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("11/01/2023"); 
		request.setToDate("12/01/2023");
		request.setScope("A");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}
	
	private COOrderSearchRequest coOrderSearchRequestForShipToAttn() { 
	    COOrderSearchRequest request=new COOrderSearchRequest();
		COSharedSearchCriteriaRequest criteriaRequest=new COSharedSearchCriteriaRequest();
		criteriaRequest.setCriteriaName("CriteriaShipToAttention");
		criteriaRequest.setCriteriaFieldValue("Test");
		criteriaRequest.setCriteriaFieldModifier("");
		ArrayList<COSharedSearchCriteriaRequest> listCriteria=new ArrayList<>();
		listCriteria.add(criteriaRequest);
		request.setSearchCriteriaRequest(listCriteria);
		request.setFromDate("11/01/2023"); 
		request.setToDate("12/01/2023");
		request.setScope("A");
		request.setRecentOrderSearch(false);
		request.setSearchForWidgetDisplay(false);
		request.setRepeatSearch(false);
		return request;
	}
		
	@Test
	void testgetOrderStatusSearchCriteriaShipToName() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestForShipToName();
		String criteriaName="CriteriaShipToName";
		try(MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)){
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockOrderStatusSearchCriteria);
			when(mockOrderStatusSearchCriteria.getSalesReferenceNbr()).thenReturn(criteriaName);
			OrderStatusSearchCriteria resp=OrderSearchUtil.getOrderStatusSearchCriteria(request, mockAppSessionBean, mockSDF);
			assertEquals(criteriaName, resp.getSalesReferenceNbr());
		}	
	}
	
	@Test
	void testgetOrderStatusSearchCriteriaShipToAttn() throws AtWinXSException{
		COOrderSearchRequest request = coOrderSearchRequestForShipToAttn();
		String criteriaName="CriteriaShipToAttention";
		try(MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)){
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockOrderStatusSearchCriteria);
			when(mockOrderStatusSearchCriteria.getPurchaseOrdNbr()).thenReturn(criteriaName);
			OrderStatusSearchCriteria resp=OrderSearchUtil.getOrderStatusSearchCriteria(request, mockAppSessionBean, mockSDF);
			assertEquals(criteriaName, resp.getPurchaseOrdNbr());
		}	
	}
		
	
}
