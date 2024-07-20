/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/10/24	N Caceres			CAP-50036				Initial Version
 *	06/25/24	C Codina			CAP-50033				Added JUnits for build kit template component
 *	06/25/24	N Caceres			CAP-50260				Add JUnits for adding wild card items to kit
 *	06/25/24	S Ramachandran		CAP-50356				Added tests to validate amount of custom docs items exceed to kits
 *	06/28/24	Satishkumar A		CAP-50504				C1UX BE - Creation of service to reload KitSession when coming back to kit editor from search or custom docs
 *	06/26/24	N Caceres			CAP-50537				Test methods for Catalog Browse API
 *	07/05/24	M Sakthi			CAP-50651				Test methods for Add to Cart Kit Template
 *	07/05/24	S Ramachandran		CAP-50732				Added tests to Validate location code
 *	07/09/24	L De Leon			CAP-50842				Added tests for populateUomDisplayForComponentItems method
 */
package com.rrd.c1ux.api.services.kittemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.kittemplate.ComponentItems;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateResponse;
import com.rrd.c1ux.api.models.kittemplate.KitCatalogBrowseResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateSearchResponse;
import com.rrd.custompoint.orderentry.ao.KitFormBean;
import com.rrd.custompoint.orderentry.entity.KitFormEntityImpl;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.framework.util.textprocessing.TextProcessor;
import com.wallace.atwinxs.kits.ao.MKTemplateOrderAssembler;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKComponentInfo;
import com.wallace.atwinxs.kits.session.MKContainerSequenceLocationInfo;
import com.wallace.atwinxs.kits.session.MKContainerTypeInfo;
import com.wallace.atwinxs.kits.session.MKHeaderInfo;
import com.wallace.atwinxs.kits.session.MKUOMInfo;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OECatalogTreeResponseBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

class KitTemplateServiceImplTests extends BaseOEServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(KitTemplateServiceImplTests.class);
	private static final String KIT_LINE_NUMBER = "2001";
	private static final String CUST_ITEM_NUMBER = "1000";
	private static final String VENDOR_ITEM_NUMBER = "HRB1000";
	private static final String KIT_ADD_ERROR = "This item has already been added. Could not add this item again.";
	private static final String KIT_ADD_OPTIONAL_EXISTS_ERROR = "Selected optional component exists as part of the kit template. The number of remaining optional components to be added will not be reduced by your selection.";

	@InjectMocks
	private KitTemplateServiceImpl service;
	
	@Mock
	private XSProperties mockedXSProperties;
	
	@Mock
	private KitSession mockKitSession;
	
	@Mock
	private MKHeaderInfo mockMKHeaderInfo;
	
	@Mock
	private KitFormBean mockKitFormBean;
	
	@Mock
	private KitFormEntityImpl mockKitFormBean1;
	
	@Mock
	Map<Integer, Collection<MKComponentInfo>> mockKitComponentsDistributionMap;
	
	@Mock
	Entry<Integer, Collection<MKComponentInfo>> mockEntry;
	
	
	@ParameterizedTest
	@MethodSource("addKitComponentRequests")
	void testAddKitComponent(Integer locationCode, String vendorItemNumber, String custItemNumber, boolean isSuccess) throws AtWinXSException,  Exception {
		service = Mockito.spy(service);
		
		KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(locationCode, vendorItemNumber, custItemNumber);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
		when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(2);
		
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) ->{
					doNothing().when(mock).validateKitComponentVendorItemNumber(anyInt(), anyString(), any(), any());
					when(mock.addComponentToKit(anyInt(), anyString(), any(), any(), 
							any(), any(), any())).thenReturn(isSuccess);
				})) {
		
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			
			if (isSuccess) {
				when(mockedXSProperties.getProperty("default_language")).thenReturn("en");
				when(mockedXSProperties.getProperty("default_dictionary")).thenReturn("RR Donnelley");
				when(mockedXSProperties.getProperty("server_port")).thenReturn("40000");
				when(mockedXSProperties.getProperty("server_name")).thenReturn("rrwin-cpltt01.na.ad.rrd.com");
				when(mockedXSProperties.getProperty("translation_cache_size")).thenReturn("500");
			}
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			
			KitTemplateAddCompResponse response = service.addKitComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(isSuccess, response.isSuccess());
			
		}
		
	}
	
	private static Stream<Arguments> addKitComponentRequests(){
		return Stream.of(
				Arguments.of(1, VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER, true),
				Arguments.of(1, "HRB9999", "9999", false));
		
	}
	
	private MKComponentInfo[] buildKitComponents(boolean hasItemUoms, boolean hasSelectedUom) throws Exception{
		List<MKComponentInfo> components = new ArrayList<>();
		Collection<MKUOMInfo> availableUOMs = new ArrayList<>();
		MKComponentInfo component = new MKComponentInfo();
		MKUOMInfo uomInfo = new MKUOMInfo();
		
		component.setWCSSItemNum(VENDOR_ITEM_NUMBER);
		component.setCustomerItemNum(CUST_ITEM_NUMBER);
		component.setInOrderInd(false);
		component.setKitLineNum(KIT_LINE_NUMBER);
		component.setTmpltComponentItemCriticalInd("Y");
		component.setRequiredItemInd("N");
		component.setCustomerItemDesc("ENGLISH_COUPON_FLYER_WITH_NO_SPACES");
		component.setItemSequenceNum("1");
		component.setSequenceLocationID("1");
		component.setItemImageURL("/images/global/NoImageAvailable.png\"");
		component.setItemClassificationCd("");
		
		if (hasItemUoms) {
			availableUOMs.add(new MKUOMInfo("test", "test", true));
			availableUOMs.add(new MKUOMInfo("Test", "Test", false));

			Field field = MKComponentInfo.class.getDeclaredField("itemUOMs");
			field.setAccessible(true);
			field.set(component, availableUOMs.toArray(new MKUOMInfo[0]));
		}

		if (hasSelectedUom) {
			uomInfo.setUOMIndAvail(true);
			uomInfo.setUOMCd("EA");
			uomInfo.setUomDisplay("Each of 1");
			uomInfo.setUOMMinimumQty("1");
			uomInfo.setUOMMaximumQty("25600");

			component.setSelectedItemUOM(uomInfo);
		}

		component.setItemQty("1");
		components.add(component);
		
		return components.toArray(new MKComponentInfo[components.size()]);
	}
	
	// CAP-50732
	@Test
	void testAddKitComponentForVendorItemValidation() throws AtWinXSException,  Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
		
		doReturn(1).when(service).getComponentIndex(anyString(),anyString(), any(MKComponentInfo[].class));
		
		KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(1,
				"", CUST_ITEM_NUMBER);
		kitTemplateAddCompRequest.setLocationCode(null);
		
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
						MKTemplateOrderAssembler.class, (mock, context) -> {
						
							doThrow(new AtWinXSException("Error", "class")).
							when(mock).validateKitComponentVendorItemNumber(anyInt(), anyString(), any(), any());			
				})) {
		
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			
			KitTemplateAddCompResponse response = service.addKitComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(false, response.isSuccess());
		}
		
	}
	
	// CAP-50732
	@Test
	void testAddKitComponentForLocationCodeValidation() throws AtWinXSException,  Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
		
		KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(2,
				VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER);
		 kitTemplateAddCompRequest.setLocationCode(-1);
		 
//		when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(2);
		//when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(true);
		
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
						MKTemplateOrderAssembler.class, (mock, context) -> {
						
				})) {
		
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			
			KitTemplateAddCompResponse response = service.addKitComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(false, response.isSuccess());
			
		}
	}
	
	@ParameterizedTest
	@MethodSource("removeKitComponentRequests")
	void testRemoveKitComponent( int kitLineNum, boolean isSuccess, String exception) throws AtWinXSException, Exception{
		service = Mockito.spy(service);
		
		KitTemplateRemoveCompRequest kitTemplateRemoveCompRequest = new KitTemplateRemoveCompRequest(kitLineNum);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) ->{
					
					when(mock.isRemoveCompSuccessful(any(), anyInt(), any(), any(), any(), any(), any())).thenReturn(isSuccess);
				});MockedStatic<SessionHandler> mockedSessionHandler = Mockito.mockStatic(SessionHandler.class)) {
		
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			if(!"excep2".equalsIgnoreCase(exception)) {
				when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
				when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
				mockedSessionHandler.when(()-> SessionHandler.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
			}

			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			
			if("excep1".equalsIgnoreCase(exception)) {
				doThrow(new AtWinXSException("Error", "class")).when(mockSessionHandlerService).saveFullSessionInfo(any(), anyInt(), anyInt()); 
			}
			
			KitTemplateRemoveCompResponse response = service.removeKitComponent(mockSessionContainer, kitTemplateRemoveCompRequest);
			assertEquals(isSuccess, response.isSuccess());
			
		}
		
	}
	
	@Test
	void testRemoveKitComponentException() throws AtWinXSException {
		
		service = Mockito.spy(service);
		
		KitTemplateRemoveCompRequest kitTemplateRemoveCompRequest = new KitTemplateRemoveCompRequest();
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
				
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.removeKitComponent(mockSessionContainer, kitTemplateRemoveCompRequest);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		
	}
	
	private static Stream<Arguments> removeKitComponentRequests(){
		return Stream.of(
				Arguments.of(2001,true,""),
				Arguments.of(2001,false,""),
				Arguments.of(-1,false,""),
				Arguments.of(2001,false,"excep1"),
				Arguments.of(2001,false,"excep2")
				);
		
	}
	
	//CAP-50504
	@ParameterizedTest
	@MethodSource("reloadKitSession")
	void testReloadKitTemplate( boolean isSuccess, boolean hasService , boolean kitSession) throws AtWinXSException {
		
		service = Mockito.spy(service);
		
		InitKitTemplateResponse response = new InitKitTemplateResponse();
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(hasService);

		if(!hasService) {
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.reloadKitTemplate(mockSessionContainer);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		} else {
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			if(kitSession) {
				when(service.populateKitFormBean(any(), any(), any(), any(), any(), any())).thenReturn(mockKitFormBean);
				doNothing().when(service).setAllowDuplicateCustDoc(any(), any(), any());
			}else {
				when(service.populateKitFormBean(any(), any(), any(), any(), any(), any())).thenReturn(null);
			}
			
			try(MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) ->{
				
				when(mock.isAllowDupCustDoc(anyInt(), anyInt())).thenReturn(true);
			})){
				response = service.reloadKitTemplate(mockSessionContainer);
			}
			assertEquals(isSuccess, response.isSuccess());

		}
	
	}
	
	private static Stream<Arguments> reloadKitSession(){
		return Stream.of(
				Arguments.of(false, false, false),
				Arguments.of(false, true, false),
				Arguments.of(true, true, true)
				);
		
	}

	// CAP-50260
	@ParameterizedTest
	@MethodSource("addWildCardComponentRequests")
	void testAddWildCardComponent(Integer locationCode, String vendorItemNumber, String custItemNumber, boolean isSuccess, boolean hasKitSession,
			String errorMessage) throws AtWinXSException, Exception {
		service = Mockito.spy(service);
		KitSession kitSession = null;
		if (hasKitSession) {
			kitSession = mockKitSession;
		}
		KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(locationCode, vendorItemNumber, custItemNumber);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(kitSession);
		
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) -> {
				})) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			if (isSuccess) {
				when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
				when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(2);
			} else {
				if (hasKitSession) {
					when(mockVolatileSessionBean.getErrorInKitBuild()).thenReturn(errorMessage);
					when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(2);
				}
			}
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			KitTemplateAddCompResponse response = service.addWildCardComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(isSuccess, response.isSuccess());
			
		}
		
	}
	
	private static Stream<Arguments> addWildCardComponentRequests(){
		return Stream.of(
				Arguments.of(1, VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER, true, true, AtWinXSConstant.EMPTY_STRING),
				Arguments.of(1, VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER, false, true, KIT_ADD_ERROR),
				Arguments.of(1, VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER, false, false, AtWinXSConstant.EMPTY_STRING),
				Arguments.of(1, VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER, true, true, KIT_ADD_OPTIONAL_EXISTS_ERROR),
				Arguments.of(1, AtWinXSConstant.EMPTY_STRING, CUST_ITEM_NUMBER, true, true, AtWinXSConstant.EMPTY_STRING)
				);
	}
	
	@Test
	void that_addWildCardComponent_hasNoAccess() throws AtWinXSException {
		
		service = Mockito.spy(service);
		
		KitTemplateAddCompRequest request = new KitTemplateAddCompRequest();
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
				
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.addWildCardComponent(mockSessionContainer, request);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		
	}

	// CAP-50732
	@Test
	void testAddKitComponent_AtWinXSExceptionHandling() throws AtWinXSException, Exception {
	    service = Mockito.spy(service);

	    when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
	    when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	    when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
	    when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
	    when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	    when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
	    when(mockAppSessionBean.getSessionID()).thenReturn(1);
	    when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	    when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
	    when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(2);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
		
	    KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(2,
	            VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER);
	    kitTemplateAddCompRequest.setLocationCode(3);

	    int componentIndex = 1;
	    
	    try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
	         MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
	             MKTemplateOrderAssembler.class, (mock, context) -> {
	            		doThrow(new AtWinXSException("Error", "class")).
						    when(mock).validateKitComponentVendorItemNumber(anyInt(), anyString(), any(), any());
	            		
						when(mock.addComponentToKit(eq(1), anyString(), any(), any(), any(), any(), any()))
								.thenThrow(new AtWinXSException("Error", "class"));
					})) {
	    	mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
	    	TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
	    	doReturn(mockTextProcessor).when(service).getTextProcessor(any());
	        
	        KitTemplateAddCompResponse response = service.addKitComponent(mockSessionContainer, kitTemplateAddCompRequest);

	        service.validateVendorItem(kitTemplateAddCompRequest, componentIndex, mockTextProcessor, mockKitSession, mockedAssembler.constructed().get(0), mockAppSessionBean, response);

	        assertEquals(false, response.isSuccess());
	    }
	}

	
	// CAP-50732
	@Test
	void testAddWildCardComponentLocationCodeValidation() throws AtWinXSException, Exception {
		service = Mockito.spy(service);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
		//when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(true);
		when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(2);
			
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
						MKTemplateOrderAssembler.class, (mock, context) -> {
						})) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(2,
					VENDOR_ITEM_NUMBER, CUST_ITEM_NUMBER);
			 kitTemplateAddCompRequest.setLocationCode(2);

			KitTemplateAddCompResponse response = service.addWildCardComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(false, response.isSuccess());
		}
	}
	
	
	// CAP-50356
	@ParameterizedTest
	@MethodSource("addKitComponentValidateCustDocItemsRequests")
	void testAddKitComponentValidateCustDocItems(Integer locationCode, String vendorItemNumber, String custItemNumber, boolean isFailed,
			String errorMsg) throws AtWinXSException {
		service = Mockito.spy(service);
		
		KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(locationCode, vendorItemNumber, custItemNumber);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponentsValidateCustDocItems());
		//when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
		//when(mockMKHeaderInfo.getOrderNum()).thenReturn(null);

		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
						MKTemplateOrderAssembler.class, (mock, context) -> {
							doNothing().when(mock).validateKitComponentVendorItemNumber(anyInt(), anyString(), any(),
									any());
							when(mock.addComponentToKit(anyInt(), anyString(), any(), any(), any(), any(), any()))
									.thenThrow(new AtWinXSException("Error", "class"));
							when(mock.validateAddComponent(any(), any(), anyString(), anyString(), anyString(), any(),
									anyInt(), anyInt())).thenAnswer(new Answer<Boolean>() {
										@Override
										public Boolean answer(InvocationOnMock invocation) throws Throwable {
											ArrayList<String> errors = invocation.getArgument(5);
											errors.add(errorMsg);
											return false;
										}
									});
						})) {

			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			//when(mockedXSProperties.getProperty("default_language")).thenReturn("en");
			//when(mockedXSProperties.getProperty("default_dictionary")).thenReturn("RR Donnelley");
			//when(mockedXSProperties.getProperty("server_port")).thenReturn("40000");
			//when(mockedXSProperties.getProperty("server_name")).thenReturn("rrwin-cpltt01.na.ad.rrd.com");
			//when(mockedXSProperties.getProperty("translation_cache_size")).thenReturn("500");
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			
			KitTemplateAddCompResponse response = service.addKitComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(isFailed, response.isSuccess());
		}
	}
	
	// CAP-50356
	@ParameterizedTest
	@MethodSource("addKitComponentValidateCustDocItemsRequests")
	void testAddKitComponentValidateCustDocItemsWithOrderId(Integer locationCode, String vendorItemNumber, String custItemNumber, boolean isFailed,
			String errorMsg) throws AtWinXSException {
		service = Mockito.spy(service);
		
		KitTemplateAddCompRequest kitTemplateAddCompRequest = new KitTemplateAddCompRequest(locationCode, vendorItemNumber, custItemNumber);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponentsValidateCustDocItems());
		//when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
		//when(mockMKHeaderInfo.getOrderNum()).thenReturn("1111");
		//when(mockVolatileSessionBean.getOrderId()).thenReturn(1111);
		

		try (MockedStatic<PropertyUtil> mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
						MKTemplateOrderAssembler.class, (mock, context) -> {
							doNothing().when(mock).validateKitComponentVendorItemNumber(anyInt(), anyString(), any(),
									any());
							when(mock.addComponentToKit(anyInt(), anyString(), any(), any(), any(), any(), any()))
									.thenThrow(new AtWinXSException("Error", "class"));
							when(mock.validateAddComponent(any(), any(), anyString(), anyString(), anyString(), any(),
									anyInt(), anyInt())).thenAnswer(new Answer<Boolean>() {
										@Override
										public Boolean answer(InvocationOnMock invocation) throws Throwable {
											ArrayList<String> errors = invocation.getArgument(5);
											errors.add(errorMsg);
											return false;
										}
									});
						})) {

			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(any())).thenReturn(mockedXSProperties);
			//when(mockedXSProperties.getProperty("default_language")).thenReturn("en");
			//when(mockedXSProperties.getProperty("default_dictionary")).thenReturn("RR Donnelley");
			//when(mockedXSProperties.getProperty("server_port")).thenReturn("40000");
			//when(mockedXSProperties.getProperty("server_name")).thenReturn("rrwin-cpltt01.na.ad.rrd.com");
			//when(mockedXSProperties.getProperty("translation_cache_size")).thenReturn("500");
			TextProcessor mockTextProcessor = Mockito.mock(TextProcessor.class);
			doReturn(mockTextProcessor).when(service).getTextProcessor(any());
			
			KitTemplateAddCompResponse response = service.addKitComponent(mockSessionContainer, kitTemplateAddCompRequest);
			assertEquals(isFailed, response.isSuccess());
		}
	}
	
	@ParameterizedTest
	@MethodSource("browseCatalogData")
	void testKitBrowseCatalog(boolean isSuccess, boolean hasKitSession, boolean hasPunchOut) throws AtWinXSException, Exception {
		service = Mockito.spy(service);

		KitSession kitSession = null;
		if (hasKitSession) {
			kitSession = mockKitSession;
		}
		
		KitTemplateAddToCartRequest request = buildKitAddToCardRequest();
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(kitSession);
		
		if(hasPunchOut) {
			when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
			when(mockPunchoutSessionBean.getPunchoutType()).thenReturn(OrderEntryConstants.PUNCHOUT_TYPE_AISLE);
		}
		
		if (hasKitSession) {
			when(mockKitSession.isAllowWildCard()).thenReturn(isSuccess);
		}
		
		if (isSuccess) {
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(true);
			when(mockKitSession.getHeader()).thenReturn(buildMKHeaderInfo());
			when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
		}
		
		try (MockedConstruction<OECatalogAssembler> mockedCatalogAssembler = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
					when(mock.getCatalog(any(), anyString(), anyString(), any(), anyString(), anyBoolean())).thenReturn(buildCatalogTree());
					when(mock.getPunchoutCatalog(any(), anyString(), anyString(), any(), anyString(), anyString(), anyBoolean())).thenReturn(buildCatalogTree());
				})) {
			KitCatalogBrowseResponse response = service.kitBrowseCatalog(mockSessionContainer, request);
			assertEquals(isSuccess, response.isSuccess());
		}
	}
	
	@Test
	void that_kitBrowseCatalog_hasNoAccess() throws AtWinXSException {
		
		service = Mockito.spy(service);
		
		KitTemplateAddToCartRequest request = buildKitAddToCardRequest();
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
				
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.kitBrowseCatalog(mockSessionContainer, request);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		
	}
	
	// CAP-50356
	private static Stream<Arguments> addKitComponentValidateCustDocItemsRequests() {
		return Stream.of(Arguments.of(1, "2521057", "UPS MAXI CTN L", false,
				"Your current settings only allow 1 Custom Document imprint item(s) per Kit Template order."));

	}
	
	// CAP-50356
	private MKComponentInfo[] buildKitComponentsValidateCustDocItems() {
		List<MKComponentInfo> components = new ArrayList<>();
		MKComponentInfo component = new MKComponentInfo();
		component.setWCSSItemNum("2521057");
		component.setCustomerItemNum("UPS MAXI CTN L");
		component.setInOrderInd(false);
		component.setKitLineNum(KIT_LINE_NUMBER);
		components.add(component);
		return components.toArray(new MKComponentInfo[components.size()]);
	}
	
	private static Stream<Arguments> browseCatalogData(){
		return Stream.of(
				Arguments.of(true, true, false),
				Arguments.of(true, true, true),
				Arguments.of(false, true, false),
				Arguments.of(false, false, false)
				);
	}
	
	private KitTemplateAddToCartRequest buildKitAddToCardRequest() {
		KitTemplateAddToCartRequest request = new KitTemplateAddToCartRequest();
		Collection<ComponentItems> componentItems = new ArrayList<>();
		ComponentItems componentItem = new ComponentItems();
		componentItem.setKitLineNumber(KIT_LINE_NUMBER);
		componentItem.setUomCode("EA");
		componentItem.setQuantity(1);
		componentItem.setSequenceLocationId(1);
		componentItem.setItemSequenceNumber(1);
		componentItem.setCriticalIndicator("Y");
		componentItems.add(componentItem);
		request.setComponentItems(componentItems);
		request.setAssemblyInstructions("");
		return request;
	}
	
	private MKHeaderInfo buildMKHeaderInfo() {
		MKHeaderInfo header = new MKHeaderInfo();
		header.setKitInd("O");
		
		MKContainerTypeInfo selectedContainerType = new MKContainerTypeInfo();
		selectedContainerType.setContainerTypeID("003");
		selectedContainerType.setContainerTypeDesc("Envelope");
		selectedContainerType.setStandardAllowedInd(true);
		selectedContainerType.setCustomAllowedInd(false);
		selectedContainerType.setImageURLAd("/kits/img/kit_envelope_small.png");
		selectedContainerType.setContainerSequenceLocations(new MKContainerSequenceLocationInfo[1]);
		header.setSelectedContainerType(selectedContainerType);
		
		return header;
	}
	
	private OECatalogTreeResponseBean buildCatalogTree() {
		OECatalogTreeResponseBean catalogTree = new OECatalogTreeResponseBean();
		catalogTree.setSelectedCategory(35711);
		Collection<TreeNodeVO> categories = new ArrayList<>();
		categories.add(new TreeNodeVO());
		catalogTree.setCategories(categories);
		return catalogTree;
	}
	
	
	//CAP-50651
		private MKComponentInfo[] buildKitComponentscart() throws Exception{
		List<MKComponentInfo> components = new ArrayList<>();
		Collection<MKUOMInfo> availableUOMs = new ArrayList<>();
		MKComponentInfo component = new MKComponentInfo();
		MKUOMInfo uomInfo = new MKUOMInfo();
		
		component.setWCSSItemNum(VENDOR_ITEM_NUMBER);
		component.setCustomerItemNum(CUST_ITEM_NUMBER);
		component.setInOrderInd(true);
		component.setKitLineNum(KIT_LINE_NUMBER);
		component.setTmpltComponentItemCriticalInd("Y");
		component.setRequiredItemInd("N");
		component.setCustomerItemDesc("ENGLISH_COUPON_FLYER_WITH_NO_SPACES");
		component.setItemSequenceNum("1");
		component.setSequenceLocationID("1");
		component.setItemImageURL("/images/global/NoImageAvailable.png\"");
		component.setItemClassificationCd("");
		
		uomInfo.setUOMIndAvail(true);
		uomInfo.setUOMCd("EA");
		uomInfo.setUomDisplay("Each of 1");
		uomInfo.setUOMMinimumQty("1");
		uomInfo.setUOMMaximumQty("25600");
		
		availableUOMs.add(new MKUOMInfo("test", "test", true));
		availableUOMs.add(new MKUOMInfo("Test", "Test", false));
		
		Field field = MKComponentInfo.class.getDeclaredField("itemUOMs");
        field.setAccessible(true);
        field.set(component, availableUOMs.toArray(new MKUOMInfo[0]));
		
		component.setSelectedItemUOM(uomInfo);
		component.setItemQty("1");
		components.add(component);
		
		return components.toArray(new MKComponentInfo[components.size()]);
	}
	
	
	@Test
	void testAddToCartKitTemplate_success() throws Exception {
		service = Mockito.spy(service);
		KitTemplateAddToCartRequest kitTemplateAddToCartRequest = buildKitAddToCardRequest();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponentscart());
		
		Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = new HashedMap();
		MKComponentInfo[] compInfoCollection=buildKitComponentscart();
		
		
		Collection<MKComponentInfo> compInfoCollection1=Arrays.asList(compInfoCollection);
		kitComponentsDistributionMap.put(1,compInfoCollection1);
		
		when(mockKitFormBean1.getKitItemDistributionMap()).thenReturn(kitComponentsDistributionMap);
		doReturn(mockKitFormBean1).when(service).getKitFormBeanValues(any(), any(), any(), any(), any(), any(), any());
		
		doNothing().when(mockKitFormBean1).updateSelectedItemUOM();
		doNothing().when(mockKitFormBean1).updateSessionComponents(any());
		
		doReturn(mockKitFormBean1).when(service).populateKitFormBean(any(), any(), any(), any(), any(), any());
		doReturn(true).when(mockKitFormBean1).validateMininmunItemsReached(mockKitSession);
		
		
		try (MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) -> {
				})) {
				mockSessionHandler.when(()-> SessionHandler.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
				KitTemplateAddToCartResponse response = service.addToCartKitTemplate(mockSessionContainer,
					kitTemplateAddToCartRequest);
			assertTrue(response.isSuccess());

		}

	}
	
	
	
	@Test
	void testAddToCartKitTemplate_fail() throws Exception {
		service = Mockito.spy(service);
		KitTemplateAddToCartRequest kitTemplateAddToCartRequest = buildKitAddToCardRequest();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponentscart());
		
		Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = new HashedMap();
		MKComponentInfo[] compInfoCollection=buildKitComponentscart();
		
		
		Collection<MKComponentInfo> compInfoCollection1=Arrays.asList(compInfoCollection);
		kitComponentsDistributionMap.put(1,compInfoCollection1);
		
		when(mockKitFormBean1.getKitItemDistributionMap()).thenReturn(kitComponentsDistributionMap);
		doReturn(mockKitFormBean1).when(service).getKitFormBeanValues(any(), any(), any(), any(), any(), any(), any());
		
		doNothing().when(mockKitFormBean1).updateSelectedItemUOM();
		doNothing().when(mockKitFormBean1).updateSessionComponents(any());
		
		doReturn(mockKitFormBean1).when(service).populateKitFormBean(any(), any(), any(), any(), any(), any());
		doReturn(false).when(mockKitFormBean1).validateMininmunItemsReached(mockKitSession);
		
		
		try (MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) -> {
				})) {
				mockSessionHandler.when(()-> SessionHandler.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
				KitTemplateAddToCartResponse response = service.addToCartKitTemplate(mockSessionContainer,
					kitTemplateAddToCartRequest);
			assertFalse(response.isSuccess());

		}

	}
	
	
	@Test
	void testAddToCartKitTemplate_validateAssemblyInstruction_fail() throws Exception {
		service = Mockito.spy(service);
		KitTemplateAddToCartRequest kitTemplateAddToCartRequest = buildKitAddToCardRequest2();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
		when(mockKitSession.getComponents()).thenReturn(buildKitComponentscart());
		
		Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = new HashedMap();
		MKComponentInfo[] compInfoCollection=buildKitComponentscart();
		
		
		Collection<MKComponentInfo> compInfoCollection1=Arrays.asList(compInfoCollection);
		kitComponentsDistributionMap.put(1,compInfoCollection1);
		
		when(mockKitFormBean1.getKitItemDistributionMap()).thenReturn(kitComponentsDistributionMap);
		doReturn(mockKitFormBean1).when(service).getKitFormBeanValues(any(), any(), any(), any(), any(), any(), any());
		
		doNothing().when(mockKitFormBean1).updateSelectedItemUOM();
		doNothing().when(mockKitFormBean1).updateSessionComponents(any());
		
		doReturn(mockKitFormBean1).when(service).populateKitFormBean(any(), any(), any(), any(), any(), any());
		
		
		try (MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) -> {
				})) {
				mockSessionHandler.when(()-> SessionHandler.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
				KitTemplateAddToCartResponse response = service.addToCartKitTemplate(mockSessionContainer,
					kitTemplateAddToCartRequest);
			assertFalse(response.isSuccess());

		}

	}
	
	
	@Test
	void testAddToCartKitTemplate_kitHeader_null() throws Exception {
		service = Mockito.spy(service);
		KitTemplateAddToCartRequest kitTemplateAddToCartRequest = buildKitAddToCardRequest2();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockKitSession.getHeader()).thenReturn(null);
		
		Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = new HashedMap();
		MKComponentInfo[] compInfoCollection=buildKitComponentscart();
		
		
		Collection<MKComponentInfo> compInfoCollection1=Arrays.asList(compInfoCollection);
		kitComponentsDistributionMap.put(1,compInfoCollection1);
		
		doReturn(mockKitFormBean1).when(service).populateKitFormBean(any(), any(), any(), any(), any(), any());
		
		
		try (MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) -> {
				})) {
				mockSessionHandler.when(()-> SessionHandler.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
				KitTemplateAddToCartResponse response = service.addToCartKitTemplate(mockSessionContainer,
					kitTemplateAddToCartRequest);
			assertFalse(response.isSuccess());

		}

	}
	
	
	@Test
	void testAddToCartKitTemplate_validkitsession_fail() throws Exception {
		service = Mockito.spy(service);
		KitTemplateAddToCartRequest kitTemplateAddToCartRequest = buildKitAddToCardRequest3();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockKitSession.getHeader()).thenReturn(mockMKHeaderInfo);
		
		Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = new HashedMap();
		MKComponentInfo[] compInfoCollection=buildKitComponentscart();
		
		
		Collection<MKComponentInfo> compInfoCollection1=Arrays.asList(compInfoCollection);
		kitComponentsDistributionMap.put(1,compInfoCollection1);
		
		when(mockKitFormBean1.getKitItemDistributionMap()).thenReturn(kitComponentsDistributionMap);
		doReturn(mockKitFormBean1).when(service).getKitFormBeanValues(any(), any(), any(), any(), any(), any(), any());
		doReturn(mockKitFormBean1).when(service).populateKitFormBean(any(), any(), any(), any(), any(), any());
		
		
		try (MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) -> {
				})) {
				mockSessionHandler.when(()-> SessionHandler.loadSession(anyInt(), anyInt())).thenReturn(mockKitSession);
				KitTemplateAddToCartResponse response = service.addToCartKitTemplate(mockSessionContainer,
					kitTemplateAddToCartRequest);
			assertFalse(response.isSuccess());

		}

	}
	
	//CAP-50737
	@Test
	void that_catalogSearchForKitTemplates_hasNoAccess() throws AtWinXSException {
		
		service = Mockito.spy(service);
		
		KitTemplateAddToCartRequest request = buildKitAddToCardRequest();
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
				
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.catalogSearchForKitTemplates(mockSessionContainer, request);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		
	}
	
	//CAP-50737
	@ParameterizedTest
	@MethodSource("catalogSearchData")
	void testCatalogSearchForKitTemplates(boolean isSuccess, boolean hasKitSession,boolean allowWildCardSearch, boolean hasSearchTerm) throws AtWinXSException, Exception {

		KitSession kitSession = null;
		if (hasKitSession) {
			kitSession = mockKitSession;
			when(mockKitSession.isAllowWildCard()).thenReturn(allowWildCardSearch);
		}
		
		KitTemplateAddToCartRequest request = buildKitAddToCardRequest();
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(1);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionHandlerService.loadSession(anyInt(), anyInt())).thenReturn(kitSession);
		
		if(hasSearchTerm) {
			request.setSearchTerm("test");
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

		}
		
		if (isSuccess) {
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockKitSession.getHeader()).thenReturn(buildMKHeaderInfo());
			when(mockKitSession.getComponents()).thenReturn(buildKitComponents());
		}
		
		try (MockedConstruction<OECatalogAssembler> mockedCatalogAssembler = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
					when(mock.getCatalog(any(), anyString(), anyString(), any(), anyString(), anyBoolean())).thenReturn(buildCatalogTree());
					when(mock.getPunchoutCatalog(any(), anyString(), anyString(), any(), anyString(), anyString(), anyBoolean())).thenReturn(buildCatalogTree());
				})) {
			KitTemplateSearchResponse response = service.catalogSearchForKitTemplates(mockSessionContainer, request);
			assertEquals(isSuccess, response.isSuccess());
		}
	}
	
	//CAP-50737
	private static Stream<Arguments> catalogSearchData(){
		return Stream.of(
				Arguments.of(false, false, false, false),
				Arguments.of(false, true, false, false),
				Arguments.of(false, true, true, false),
				Arguments.of(true, true, true, true)
				
				);
	}
	
	private KitTemplateAddToCartRequest buildKitAddToCardRequest2() {
		KitTemplateAddToCartRequest request = new KitTemplateAddToCartRequest();
		Collection<ComponentItems> componentItems = new ArrayList<>();
		ComponentItems componentItem = new ComponentItems();
		componentItem.setKitLineNumber(KIT_LINE_NUMBER);
		componentItem.setUomCode("EA");
		componentItem.setQuantity(1);
		componentItem.setSequenceLocationId(1);
		componentItem.setItemSequenceNumber(1);
		componentItem.setCriticalIndicator("Y");
		componentItems.add(componentItem);
		request.setComponentItems(componentItems);
		request.setAssemblyInstructions("WhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPIt"
				+ "oaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPItoaddthcomponentstoourorderWhenaddingcomponentsforakiweneedtocreateanAPI"
				+ "toaddthcomponentstoourorder");
		return request;
	}
	
	
	private KitTemplateAddToCartRequest buildKitAddToCardRequest3() {
		KitTemplateAddToCartRequest request = new KitTemplateAddToCartRequest();
		Collection<ComponentItems> componentItems = new ArrayList<>();
		ComponentItems componentItem = new ComponentItems();
		componentItem.setKitLineNumber("3234242");
		componentItem.setUomCode("EA");
		componentItem.setQuantity(1);
		componentItem.setSequenceLocationId(1);
		componentItem.setItemSequenceNumber(2);
		componentItem.setCriticalIndicator("Y");
		componentItems.add(componentItem);
		request.setComponentItems(componentItems);
		request.setAssemblyInstructions("Whenaddingcomponentsfor");
		return request;
	}

	// CAP-50842
	@Test
	void that_populateUomDisplayForComponentItems_hasNoComponentItems() throws AtWinXSException {

		MKComponentInfo[] components = null;

		when(mockKitSession.getComponents()).thenReturn(components);

		service.populateUomDisplayForComponentItems(mockAppSessionBean, mockKitSession);

		assertNull(components);
	}

	// CAP-50842
	@Test
	void that_populateUomDisplayForComponentItems_hasNoItemUoms() throws AtWinXSException {

		MKComponentInfo[] components = null;
		try {
			components = buildKitComponents(false, false);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		when(mockKitSession.getComponents()).thenReturn(components);

		service.populateUomDisplayForComponentItems(mockAppSessionBean, mockKitSession);

		assertNotNull(components);
		assertNull(components[0].getItemUOMs());
		assertNull(components[0].getSelectedItemUOM());
	}

	// CAP-50842
	@Test
	void that_populateUomDisplayForComponentItems_hasComponentItems() throws AtWinXSException {

		MKComponentInfo[] components = null;
		try {
			components = buildKitComponents();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		when(mockKitSession.getComponents()).thenReturn(components);

		service.populateUomDisplayForComponentItems(mockAppSessionBean, mockKitSession);

		assertNotNull(components);
		assertFalse(Util.isBlankOrNull(components[0].getItemUOMs()[0].getUomDisplay()));
		assertNotNull(components[0].getSelectedItemUOM());
	}

	private MKComponentInfo[] buildKitComponents() throws Exception {
		return buildKitComponents(true, true);
	}
}