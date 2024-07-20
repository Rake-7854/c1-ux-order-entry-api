/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  06/15/22	C Codina		SonarQube: Possible NullPointerException bug in PNAProcessorImpl - DEV Only
 *  10/18/23	Krishna Natarajan CAP-44347	Updated methods to cover the added new method checkAndShowAvailability()
 *  04/04/24	Krishna Natarajan CAP-48388 Added tests for PNA with budgetallocations WRT checkBudgetAllocations()
 */
package com.rrd.c1ux.api.services.items;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.items.PNARequest;
import com.rrd.c1ux.api.models.items.PNAResponse;
import com.rrd.c1ux.api.services.common.exception.CORPCException;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.standardoptions.entity.StandardOption;
import com.wallace.atwinxs.catalogs.dao.CatalogDefaultDAO;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

class PNAProcessorImplTest extends BaseServiceTest{
	
	@InjectMocks
	PNAProcessorImpl pnaProcessorServiceImpl;
	
	@Mock
	PriceAndAvailabilityVO mockPriceAndAvailabilityVO;
	
	@Mock
	StandardOption mockStandardOption;
	
	@Mock
	OrderEntryUtil mockOrderEntryUtil;
	
	@Mock
	PNAResponse pnaresponse;
	
	@Mock
	OECheckoutAssembler mockOECheckoutAssembler;
	
	@Mock
	AllocationQuantitiesCompositeVO mockallocQty;

	@Test
	void testPNA() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      })) {
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      })) {
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 10);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 400);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(10, 400, response);
		
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_second() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      })) {
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 0);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 0);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(0, 0, response);
		
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_third() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      })) {
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 0);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 100);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(0, 100, response);
		
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_fourth() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      })) {
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 100);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 0);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(100, 0, response);
		
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_fifth() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      }); MockedStatic<TranslationTextTag> mockedStatic = Mockito.mockStatic(TranslationTextTag.class)){
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockIOEPricingAndAvailability.getAvailableCode(any(), anyBoolean())).thenReturn("A");
		mockedStatic.when(()-> TranslationTextTag.processMessage(any(),any(), any(),any())).thenReturn("code");
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 100);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 0);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(any())).thenReturn(mockIOEPricingAndAvailability);
		pnaProcessorServiceImpl.checkAvailability(true, response, mockAppSessionBean, mockPriceAndAvailabilityVO, isCustomDoc, testItemNum);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(100, 0, response);
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_sixth() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      }); MockedStatic<TranslationTextTag> mockedStatic = Mockito.mockStatic(TranslationTextTag.class); 
	      MockedStatic<OrderEntryUtil> mockedOrderEntryUtil = Mockito.mockStatic(OrderEntryUtil.class)){
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockIOEPricingAndAvailability.getAvailableCode(any(), anyBoolean())).thenReturn("B");
		mockedStatic.when(()-> TranslationTextTag.processMessage(any(),any(), any(),any())).thenReturn("code");
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 100);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 0);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(any())).thenReturn(mockIOEPricingAndAvailability);
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockStandardOption);
		when(mockStandardOption.isBatchOrder(any(), anyInt())).thenReturn(false);
		mockedOrderEntryUtil.when(()-> OrderEntryUtil.isSuppressBackorderedStatusForBU(anyInt())).thenReturn(true);
		pnaProcessorServiceImpl.checkAvailability(true, response, mockAppSessionBean, mockPriceAndAvailabilityVO, isCustomDoc, testItemNum);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(100, 0, response);
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_seventh() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      }); MockedStatic<TranslationTextTag> mockedStatic = Mockito.mockStatic(TranslationTextTag.class); 
	      MockedStatic<OrderEntryUtil> mockedOrderEntryUtil = Mockito.mockStatic(OrderEntryUtil.class)){
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockIOEPricingAndAvailability.getAvailableCode(any(), anyBoolean())).thenReturn("B");
		mockedStatic.when(()-> TranslationTextTag.processMessage(any(),any(), any(),any())).thenReturn("code");
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 100);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 0);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(any())).thenReturn(mockIOEPricingAndAvailability);
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockStandardOption);
		when(mockStandardOption.isBatchOrder(any(), anyInt())).thenReturn(true);
		mockedOrderEntryUtil.when(()-> OrderEntryUtil.isSuppressBackorderedStatusForBU(anyInt())).thenReturn(true);
		pnaProcessorServiceImpl.checkAvailability(true, response, mockAppSessionBean, mockPriceAndAvailabilityVO, isCustomDoc, testItemNum);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(100, 0, response);
		assertNotNull(response);
		}
	}
	
	@Test
	void testPNA_build_eighth() throws AtWinXSException, CORPCException {
		
		PNARequest pnaRequest = new PNARequest();
		
		String testPriceClass  = "TEST";
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		
		try (MockedConstruction<CatalogDefaultDAO> mockedCatalogDefaultDAO = Mockito.mockConstruction(CatalogDefaultDAO.class, (mock, context) -> {

	        when(mock.retrieve(any())).thenReturn(mockCatDefVO);

	      }); MockedStatic<TranslationTextTag> mockedStatic = Mockito.mockStatic(TranslationTextTag.class); 
	      MockedStatic<OrderEntryUtil> mockedOrderEntryUtil = Mockito.mockStatic(OrderEntryUtil.class)){
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOEManageOrdersComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEManageOrdersComponent);
		when(mockOESessionBean.getOrderFilters()).thenReturn(null);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAdminUtilService.getWCSSPriceClassCd(any(), any())).thenReturn(testPriceClass);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOEPricingAndAvailability);
		when(mockIOEPricingAndAvailability.getAvailableCode(any(), anyBoolean())).thenReturn("B");
		mockedStatic.when(()-> TranslationTextTag.processMessage(any(),any(), any(),any())).thenReturn("code");
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		
		PNAResponse response = pnaProcessorServiceImpl.processPNA(pnaRequest, mockSessionContainer);
		doReturn("abc").when(mockTranslationService).processMessage(any(), any(), anyString());
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("AP");
		HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
		addFlagsAndUOM.put("availableQty", 100);
		addFlagsAndUOM.put("itemNumber", "1018");
		addFlagsAndUOM.put("uomFactor", 0);
		when(mockOEPricingAndAvailabilityComponentLocatorService.locate(any())).thenReturn(mockIOEPricingAndAvailability);
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockStandardOption);
		mockedOrderEntryUtil.when(()-> OrderEntryUtil.isSuppressBackorderedStatusForBU(anyInt())).thenReturn(false);
		pnaProcessorServiceImpl.checkAvailability(true, response, mockAppSessionBean, mockPriceAndAvailabilityVO, isCustomDoc, testItemNum);
		pnaProcessorServiceImpl.buildResponse(mockPriceAndAvailabilityVO, mockAppSessionBean, true, false, true, true, addFlagsAndUOM);
		pnaProcessorServiceImpl.checkAndShowAvailability(100, 0, response);
		assertNotNull(response);
		}
	}
	
	@Test
	void TestCheckBudgetAllocations() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_isShowOrderLinePrice_false() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(false);
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_NoOrderWOBudget() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isAllowOrderingWithoutBudget()).thenReturn(false);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(null);
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_OrderWOBudget() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isAllowOrderingWithoutBudget()).thenReturn(true);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(null);
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_PriceMorethanzero() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(1.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_OrderWOBudget_priceLessthanZero() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isAllowUnavailablePriceOrd()).thenReturn(true);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(-1.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_NoOrderWOBudget_priceLessthanzero() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isAllowUnavailablePriceOrd()).thenReturn(false);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(-1.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_NoOrderWOBudget_pricelessthanzeroAllocremQty() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(false);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		when(mockallocQty.getRemainingQuantity()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_NoOrderWOBudget_pricelessthanzeroAllocremQty2() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(true);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		when(mockallocQty.getRemainingQuantity()).thenReturn(2.0);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getItemSellPrice()).thenReturn(1.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
	@Test
	void TestCheckBudgetAllocations_LinePrice_AndBudget_NoOrderWOBudget_pricelessthanzeroAllocremQty3() throws CORPCException, AtWinXSException {
		PNARequest pnaRequest = new PNARequest();
		int quantity=0;
		String selectedUOM="test_sel_UOM";
		String testVendItemNum = "test_vend_item_num";
		String testItemNum = "test_item_num";
		boolean isCustomDoc=false;;
		pnaRequest.setItemNumber(testItemNum);
		pnaRequest.setVendItemNum(testVendItemNum);
		pnaRequest.setQuantity(quantity);
		pnaRequest.setSelectedUOM(selectedUOM);
		pnaRequest.setCustomDoc(isCustomDoc);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(false);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockOECheckoutAssembler);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOECheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(mockallocQty);
		when(mockallocQty.getRemainingQuantity()).thenReturn(2.0);
		when(mockPriceAndAvailabilityVO.getItemExtendedSellPrice()).thenReturn(0.0);
		when(mockPriceAndAvailabilityVO.getItemSellPrice()).thenReturn(1.0);
		when(mockPriceAndAvailabilityVO.getPriceType()).thenReturn("NF");
		pnaProcessorServiceImpl.checkBudgetAllocations(pnaresponse, mockUserSettings, mockPriceAndAvailabilityVO, mockAppSessionBean, mockSessionContainer, mockOESessionBean);
		assertNotNull(pnaresponse);
	}
	
}
