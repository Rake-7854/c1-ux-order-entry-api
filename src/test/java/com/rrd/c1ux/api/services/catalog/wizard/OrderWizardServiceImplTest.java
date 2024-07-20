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
 *  02/20/24	N Caceres			CAP-47141		Initial Junit test case creation
 *  02/23/24	C Codina			CAP-47086		Added JUnits for Order Wizard Search
 */
package com.rrd.c1ux.api.services.catalog.wizard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchRequest;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchResponse;
import com.rrd.c1ux.api.models.catalog.OrderWizardSelectedAttributes;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionRequest;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionResponse;
import com.rrd.c1ux.api.services.admin.locators.SiteAttributeComponentLocatorService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.ISiteAttribute;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardFormBean;
import com.wallace.atwinxs.orderentry.admin.vo.OrderWizardQuestionVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;

class OrderWizardServiceImplTest extends BaseServiceTest {

	@InjectMocks
	private OrderWizardServiceImpl testService;
	
	@Mock
	private OAOrderWizardFormBean mockOrderWizardFormBean;
	
	@Mock
	private SiteAttributeComponentLocatorService siteAttributeComponentLocatorService;
	
	@Mock
	private ISiteAttribute mockSiteAttribute;
	
	@Mock
	OrderWizardSearchRequest mockSearchRequest;
	
	@Mock
	CatalogSearchResultsCriteria mockSearchCriteria;
	
	@Mock
	OEItemSearchCriteriaSessionBean mockSearchCriteriaBean;
	
	@Mock
	OrderWizardSelectedAttributes mockWizardSelectedAttributes;
	
	@ParameterizedTest
	@MethodSource("testData")
	void that_getOrderWizardQuestion_getMainOrderWizardQuestion_success(int attributeId, 
			boolean isSinglePath) throws AccessForbiddenException {
		
		mockCommonMethods();
		when(mockOrderWizardFormBean.isSinglePath()).thenReturn(isSinglePath);
		
		try(MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {
							doNothing().when(mock).setSkippedEOOAttributeValues(mockAppSessionBean, mockVolatileSessionBean, mockOESessionBean, true);
						});
				MockedConstruction<OAOrderWizardAssembler> mockedOAWizard = mockConstruction(OAOrderWizardAssembler.class,
						(mock, context) -> {
							when(mock.getOrderWizard(anyInt(), anyInt())).thenReturn(mockOrderWizardFormBean);
						});
				MockedConstruction<OEOrderWizardAssembler> mockedOEWizard = mockConstruction(OEOrderWizardAssembler.class,
						(mock, context) -> {
							when(mock.getNextQuestion(anyInt(), anyInt(), anyInt())).thenReturn(buildWizardQuestion());
							when(mock.getOrderWizardRequestObject(any(), anyMap(), any(), 
									any(), any(), any())).thenReturn(buildRequestMap());
						})) {
			
			when(mockOrderWizardFormBean.getKeyAttributeId()).thenReturn(attributeId);
			if (!isSinglePath) {
				when(mockVolatileSessionBean.getSelectedSiteAttribute()).thenReturn(buildSelectedSiteAttributes(false));
			}
			OrderWizardQuestionResponse response = testService.getOrderWizardQuestion(mockSessionContainer, buildMainOrderWizardRequest());
			if (!isSinglePath) {
				assertTrue(response.isSuccess());
			} else {
				assertFalse(response.isSuccess());
			}
		}
	}
	
	private static Stream<Arguments> testData() {
	    return Stream.of(
	      Arguments.of(5404, false),
	      Arguments.of(0, false),
	      Arguments.of(5404, true)

	    );
	}

	@Test
	void that_getOrderWizardQuestion_isSinglePath() throws AccessForbiddenException {
		
		mockCommonMethods();
		when(mockOrderWizardFormBean.isSinglePath()).thenReturn(true);
		
		try(MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {
							doNothing().when(mock).setSkippedEOOAttributeValues(mockAppSessionBean, mockVolatileSessionBean, mockOESessionBean, true);
						});
				MockedConstruction<OAOrderWizardAssembler> mockedOAWizard = mockConstruction(OAOrderWizardAssembler.class,
						(mock, context) -> {
							when(mock.getOrderWizard(anyInt(), anyInt())).thenReturn(mockOrderWizardFormBean);
						});
				MockedConstruction<OEOrderWizardAssembler> mockedOEWizard = mockConstruction(OEOrderWizardAssembler.class,
						(mock, context) -> {
							when(mock.getNextQuestion(anyInt(), anyInt(), anyInt())).thenReturn(buildWizardQuestion());
							when(mock.getOrderWizardRequestObject(any(), anyMap(), any(), 
									any(), any(), any())).thenReturn(buildRequestMap());
						})) {
			
			OrderWizardQuestionResponse response = testService.getOrderWizardQuestion(mockSessionContainer, buildMainOrderWizardRequest());
			assertFalse(response.isSuccess());
		}
	}

	private void mockCommonMethods() {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
	    when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
	    when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);
	    when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
	}
	
	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void that_getOrderWizardQuestion_getNextOrderWizardQuestion_success(boolean isShopping) throws AtWinXSException {

		mockCommonMethods();
		
		Map<String, Object> requestMap = new HashMap<>();
		
		try(MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {
							doNothing().when(mock).setSkippedEOOAttributeValues(mockAppSessionBean, mockVolatileSessionBean, mockOESessionBean, true);
						});
				MockedConstruction<OAOrderWizardAssembler> mockedOAWizard = mockConstruction(OAOrderWizardAssembler.class,
						(mock, context) -> {
							when(mock.getOrderWizard(anyInt(), anyInt())).thenReturn(buildWizardFormBean());
						});
				MockedConstruction<OEOrderWizardAssembler> mockedOEWizard = mockConstruction(OEOrderWizardAssembler.class,
						(mock, context) -> {
							when(mock.getOrderWizardRequestObject(any(), anyMap(), any(), 
									any(), any(), any())).thenReturn(requestMap);
							when(mock.getNextQuestion(anyInt(), anyInt(), anyInt())).thenReturn(buildWizardQuestion());
							when(mock.getOrderWizardNextValues(any(), anyMap(), anyInt(), anyInt(), any(), any())).thenReturn(buildQuestionValues());
						})) {
			when(mockVolatileSessionBean.getSelectedSiteAttribute()).thenReturn(buildSelectedSiteAttributes(isShopping));
			if (isShopping) {
			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(1);
			}
			when(siteAttributeComponentLocatorService.locate(any())).thenReturn(mockSiteAttribute);
			when(mockSiteAttribute.getAttributesForFamily(anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(familyAttributes());
			when(mockSiteAttribute.getAttrValuesPerFamily(any(), anyMap())).thenReturn(buildSiteAttrValueVOList());
			
			OrderWizardQuestionResponse response = testService.getOrderWizardQuestion(mockSessionContainer, buildNextOrderWizardRequest());
			assertTrue(response.isSuccess());
		}
	
	}
	@Test
	void performWizardSearchTest() throws AtWinXSException{
		testService = Mockito.spy(testService);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);
		
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockSearchCriteriaBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSearchRequest.getAttributeValues()).thenReturn(buildOrderWizardSelectedAttributes());
		
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); 
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)){
			mockSessionHandler.when(() -> SessionHandler.persistServiceInSession(any(), any(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
	
			try(MockedConstruction<OAOrderWizardAssembler> mockedWizardAssembler = mockConstruction(OAOrderWizardAssembler.class, (mock, context) -> {
				when(mock.getOrderWizard(anyInt(), anyInt())).thenReturn(mockOrderWizardFormBean);
			})){
			
			when(mockUserSettings.getOrderWizardID()).thenReturn(1234);
			when(mockAppSessionBean.getSiteID()).thenReturn(456);
			
			when(siteAttributeComponentLocatorService.locate(any())).thenReturn(mockSiteAttribute);
			when(mockSiteAttribute.getAttributesForFamily(anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(familyAttributes());
			when(mockSiteAttribute.getAttrValuesPerFamily(any(), anyMap())).thenReturn(buildSiteAttrValueVOList());
			
			
			OrderWizardSearchResponse response = testService.performWizardSearch(mockSessionContainer, mockSearchRequest);
			assertTrue(response.isSuccess());
	}
		}
	}
	
	private List<OrderWizardSelectedAttributes> buildOrderWizardSelectedAttributes() {
		OrderWizardSelectedAttributes selectedAttribute = new OrderWizardSelectedAttributes();
		List<OrderWizardSelectedAttributes> attributeValues = new ArrayList<>();
		selectedAttribute.setAttributeID(5404);
		selectedAttribute.setAttributeValueID(258600);
		attributeValues.add(selectedAttribute);
		return attributeValues;
	}
	
	private OrderWizardQuestionRequest buildMainOrderWizardRequest() {
		OrderWizardQuestionRequest request = new OrderWizardQuestionRequest();
		request.setAttributeID(AtWinXSConstant.INVALID_ID);
		request.setAttributeQuestionID(AtWinXSConstant.INVALID_ID);
		request.setAttributeValueID(AtWinXSConstant.INVALID_ID);
		request.setWizardQuestionID(AtWinXSConstant.INVALID_ID);
		return request;
	}
	
	private OrderWizardQuestionRequest buildNextOrderWizardRequest() {
		OrderWizardQuestionRequest request = new OrderWizardQuestionRequest();
		request.setAttributeID(5404);
		request.setAttributeQuestionID(5404);
		request.setAttributeValueID(258600);
		request.setWizardQuestionID(AtWinXSConstant.INVALID_ID);
		return request;
	}
	
	private OAOrderWizardFormBean buildWizardFormBean() {
		OAOrderWizardFormBean formBean = new OAOrderWizardFormBean();
		formBean.setKeyAttributeId(5404);
		return formBean;
	}
	
	private Map<Integer, String> familyAttributes() {
		Map<Integer, String> familyAttrs = new HashMap<>();
		familyAttrs.put(5404, "Car Wizard");
		return familyAttrs;
	}
	
	private Map<Integer, Collection<SiteAttrValuesVO>> buildSiteAttrValueVOList() {
		Map<Integer, Collection<SiteAttrValuesVO>> siteAttrValueVOList = new HashMap<>();
		Collection<SiteAttrValuesVO> siteAttrValuesVO = new ArrayList<>();
		SiteAttrValuesVO attributeValue = new SiteAttrValuesVO(0, 5404, 258600);
		siteAttrValuesVO.add(attributeValue);
		siteAttrValueVOList.put(5404, siteAttrValuesVO);
		return siteAttrValueVOList;
	}
	
	private OrderWizardQuestionVO buildWizardQuestion() {
		OrderWizardQuestionVO owqVO = new OrderWizardQuestionVO(1106, 1322, 5404, "What model of car do you want?", 
				5405, 1, AtWinXSConstant.EMPTY_STRING);
		return owqVO;
	}
	
	private Map<Integer,String> buildQuestionValues() {
		Map<Integer,String> questionValues = new HashMap<>();
		questionValues.put(0, "Select");
		questionValues.put(5404, "Option~~");
		return questionValues;
	}
	
	private Map<String, Object> buildRequestMap() {
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("keyAttrValues", buildQuestionValues());
		requestMap.put("keyattrvaldesc", buildQuestionValues());
		return requestMap;
	}
	
	private HashMap<Integer, SiteAttrValuesVO[]> buildSelectedSiteAttributes(boolean isShopping) {
		HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttributes = new HashMap<>();
		SiteAttrValuesVO eooValue = new SiteAttrValuesVO(0, 5404, 258600);
		SiteAttrValuesVO[] eooValues = new SiteAttrValuesVO[]{eooValue};
		selectedSiteAttributes.put(5404, eooValues);
		if (!isShopping) {
			selectedSiteAttributes.put(5405, eooValues);
		}
		return selectedSiteAttributes;
	}
}
