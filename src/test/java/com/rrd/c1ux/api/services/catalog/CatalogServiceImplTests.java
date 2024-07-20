/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By			JIRA#		Description
 * 	--------	----------------	---------	--------------------------------------------------
 *	01/18/24	S Ramachandran		CAP-46304	Added Junit tests to retrieve standard options attributes
 *	01/23/24	C Codina 			CAP-46379	C1UX BE - Method to retrieve Attribute Filters for Order Entry
 *	01/26/24	L De Leon			CAP-46322	Updated tests for the toggling of selected attribute value
 *	01/30/24	Krishna Natarajan	CAP-46821	Updated test to mock translations added to the method getCatalogAttributes()
 *	02/05/24	M Sakthi			CAP-46865	C1UX BE - Modify Attribute Filtering - add reset/clear all selection
 *  02/22/24	Krishna Natarajan	CAP-47345	Updated tests for added Allow Order Wizard flag
 *  02/22/24	Krishna Natarajan	CAP-47356	Updated tests for added Allow Refine Search flag
 */

package com.rrd.c1ux.api.services.catalog;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeRequest;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeResponse;
import com.rrd.c1ux.api.models.catalog.CatalogAttributes;
import com.rrd.c1ux.api.models.catalog.StandardAttributesC1UX;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.catalog.entity.OrderWizardFilters;
import com.rrd.custompoint.gwt.catalog.entity.StandardAttributes;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchVO;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardFormBean;
import com.wallace.atwinxs.orderentry.ao.DynamicItemAttributeVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@WithMockUser
class CatalogServiceImplTests extends BaseServiceTest {
	
	public final int attrId = 933;

	@InjectMocks
	private CatalogServiceImpl serviceToTest;
	
	@Mock
	UserGroupSearchVO mockUgVo;
	
	@Mock
	CatalogAttributeRequest mockCatalogRequest;
	
	@Mock
	StandardAttributesC1UX mockStandardAttributesC1UX;
	
	@Mock
	StandardAttributes mockStandardAttributes;
	
	@Mock 
	OEItemSearchCriteriaSessionBean mockOEItemSearchCriteriaSessionBean;
	
	@Mock
	CatalogSearchFeaturesFavoritesBean mockCatalogSearchFeaturesFavoritesBean;

	@Mock
	OEItemSearchCriteriaSessionBean mockSearchCriteriaBean;
	
	@Mock
	OAOrderWizardFormBean mockOAOrderWizardFormBean;
	
	List<AlertCountResponseBean> featFaveItemsLst;
	CatalogAttributeRequest catalogAttributeRequest; 
	StandardAttributes standardAttributes;
	CatalogAttributeResponse response;
	
	Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes;
	Map<String, Map<String,String>> wizardFilters = new LinkedHashMap<String,Map<String,String>>();
	
	@BeforeEach
	public void setup() throws AtWinXSException {
		
	}
	
	// CAP-46304
	@Test
	void that_CatalogAttributes_StandardAttributesInSession_success() throws Exception {

		//Catalog
		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		buildMockUserGroupSearchOptions();
		
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {	
			
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "lblStandardAttr")).thenReturn("");//CAP-46821
					
		
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
				.thenAnswer((Answer<Void>) invocation -> null);
	
			catalogAttributeRequest = getCatalogAttributesRequest();
			standardAttributes = getStandardAttributes();

			//NewItemOnly=TRUE, FilterNewItems=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(standardAttributes);
			standardAttributes.setFilterNewItems(false);
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(true);//CAP-47356
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());

			//NewItemOnly=TRUE, FilterNewItems=TRUE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		
			//NewItemOnly=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(false);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	

	// CAP-46304
	@Test
	void that_CatalogAttributes_NoStandardAttributesInSession_success() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		buildMockUserGroupSearchOptions();

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); 
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {
			
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
				.thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");

			CatalogAttributeResponse response = new CatalogAttributeResponse();
			catalogAttributeRequest = getCatalogAttributesRequest();

			
			//AllowUserFavorites=TRUE, UserSettingsSessionBean & CatSearchFeatFaveBean is NULL
			when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(null);
			doReturn(null).when(serviceToTest).getCatSearchFeatFaveBean(any(),any(),any());
			when(mockOESessionBean.isAllowUserFavorites()).thenReturn(true);
			when(mockOESessionBean.getUserSettings()).thenReturn(null);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(true);//CAP-47356
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		
			//AllowUserFavorites=FALSE, NewItemsFlag=TRUE
			doReturn(mockCatalogSearchFeaturesFavoritesBean).when(serviceToTest).getCatSearchFeatFaveBean(any(),any(),any());
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isNewItemsFlag()).thenReturn(true);
			when(mockOESessionBean.isAllowUserFavorites()).thenReturn(false);
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
			
			//AllowUserFavorites=FALSE, NewItemsFlag=TRUE, FavoriteItems=FALSE
			when(mockUserSettings.isNewItemsFlag()).thenReturn(false);
			when(mockOESessionBean.isAllowUserFavorites()).thenReturn(true);
			when(mockCatalogSearchFeaturesFavoritesBean.hasFavoriteItems()).thenReturn(false);
			when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsSearch()).thenReturn(new HashMap<>());
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());

			//FavoriteItems=TRUE
			AlertCounts[] alert = new AlertCounts[1];
			alert[0] = new AlertCounts("A", "", 0, 0);
			when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsDefined()).thenReturn(alert);
			when(mockCatalogSearchFeaturesFavoritesBean.hasFavoriteItems()).thenReturn(true);
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}

	}
	
	// CAP-46304
	@Test
	void that_CatalogAttributes_getCatSearchFeatFaveBean_success() throws Exception {
		
		serviceToTest = Mockito.spy(serviceToTest);
		
		CatalogSearchFeaturesFavoritesBean response = new CatalogSearchFeaturesFavoritesBean();
		featFaveItemsLst = getCatSearchFeatFaveBeanResponse();

		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(new Locale("US-en"));
		when(mockAppSessionBean.getEncodedSessionId()).thenReturn(ENCRYPTED_SESSION_ID);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); 
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, 
					(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
					.thenReturn(featFaveItemsLst);})) {
			
			mockSessionHandler.when(() -> SessionHandler.persistServiceInSession(any(), any(), anyInt()))
				.thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");
			
			response = serviceToTest.getCatSearchFeatFaveBean(mockAppSessionBean, mockOESession, mockCatalogSearchFeaturesFavoritesBean);
			
			Assertions.assertNotNull(response);
		}
	}
	
	// CAP-46304
	private StandardAttributes getStandardAttributes() {
		StandardAttributes standardAttributes = new StandardAttributes();
		return standardAttributes;
	}
	
	// CAP-46304
	private CatalogAttributeRequest getCatalogAttributesRequest() {
		CatalogAttributeRequest request = new CatalogAttributeRequest();
		request.setSelectedAttributeID(AtWinXSConstant.INVALID_ID);
		request.setSelectedAttributeValueID(AtWinXSConstant.INVALID_ID);
		request.setToggleFeature(AtWinXSConstant.EMPTY_STRING);
		request.setClearAll(false);;
		return request;
	}
	
	// CAP-46304
	public List<AlertCountResponseBean> getCatSearchFeatFaveBeanResponse() {
		
		List<AlertCountResponseBean> featFaveItemsLst = new ArrayList<AlertCountResponseBean>();
		AlertCounts featFaveCounts = new AlertCounts("AlertName", "AlertName", 0, 1);
		
		AlertCountResponseBean featFaveItems = new AlertCountResponseBean(new AlertCounts [] {featFaveCounts});
		featFaveItems.setAlertCategory(AtWinXSConstant.QUICK_FIND_FAVORITE_ITEMS);
		featFaveItems.setActionID(OrderEntryConstants.BROKER_OE_CATALOG);
		featFaveItems.setEventID(OrderEntryConstants.QUICK_FIND_FAVORITE_ITEMS_SEARCH_EVT);
		
		featFaveItemsLst.add(featFaveItems);
		return featFaveItemsLst;
	}
	@Test
	void testGetCatalogAttributes() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); 
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {

			mockSessionHandler.when(() -> SessionHandler.persistServiceInSession(any(), any(), anyInt()))
			.thenAnswer((Answer<Void>) invocation -> null);

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockSearchCriteriaBean);
			when(mockSearchCriteriaBean.getAttributesCriteria()).thenReturn(mockHashMap);
			when(mockCatalogRequest.getSelectedAttributeID()).thenReturn(AtWinXSConstant.INVALID_ID);
			when(mockCatalogRequest.getSelectedAttributeValueID()).thenReturn(AtWinXSConstant.INVALID_ID);
			when(mockCatalogRequest.isClearAll()).thenReturn(false);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(true);//CAP-47356
			buildMockUserGroupSearchOptions();
			CatalogAttributeResponse response = serviceToTest.getCatalogAttributes(mockSessionContainer, mockCatalogRequest);
			assertNotNull(response);
			assertNotNull(response.getCatalogAttributes());
		}
	}

	protected void buildMockUserGroupSearchOptions() {
		ArrayList<UserGroupSearchVO> getUsrSrchOptions = new ArrayList<>();
		UserGroupSearchVO ugVo = new UserGroupSearchVO(attrId, attrId, "Test", attrId, "Test");
		ugVo.setAttrDispName("Test");
		getUsrSrchOptions.add(ugVo);
		when(mockOESessionBean.getUsrSrchOptions()).thenReturn(getUsrSrchOptions);
	}
	@Test
	void testConvertList() {
		
		CatalogAttributes catalogAttributes = new CatalogAttributes();
		List<CatalogAttributes> result = new ArrayList<>();
		
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockSearchCriteriaBean);
		when(mockSearchCriteriaBean.getAttributesCriteria()).thenReturn(mockHashMap);

		catalogAttributes.setAttributeID(attrId);
		catalogAttributes.setAttributeDisplayName("Test");
		catalogAttributes.setMultiSelect(true);
		result.add(catalogAttributes);
		
		List<DynamicItemAttributeVO> voList = new ArrayList<>();
		List<SiteAttrValuesVO> siteAttrValueVOList = new ArrayList<>();
		ArrayList<UserGroupSearchVO> ugVoList = new ArrayList<>();
		UserGroupSearchVO ugVo = new UserGroupSearchVO(attrId, attrId, BLANK_NOT_ALLOWED_ERR_FRENCH, attrId, BLANK_NOT_ALLOWED_ERR_ENGLISH);
		DynamicItemAttributeVO vo = new DynamicItemAttributeVO(mockUgVo, siteAttrValueVOList, ugVoList, false);
		DynamicItemAttributeVO vo2 = new DynamicItemAttributeVO(mockUgVo, siteAttrValueVOList, ugVoList, false);
		
		
		SiteAttrValuesVO siteVO = new SiteAttrValuesVO(attrId, attrId, attrId);
		siteAttrValueVOList.add(siteVO);
		ugVo.setAttrDispName("Color");
		ugVo.setAttrName("Test");
		voList.add(vo);
		voList.add(vo2);
		ugVoList.add(ugVo);
		when(mockUgVo.getSearchCatalogInd()).thenReturn(45);
		when(mockUgVo.getAttrID()).thenReturn(attrId);

		serviceToTest.convertList(voList, mockOESessionBean);
		assertNotNull(voList);
		
	}
	
	
	//CAP-46865
	@Test
	void testGetCatalogAttributes_clearAll() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); 
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {

			mockSessionHandler.when(() -> SessionHandler.persistServiceInSession(any(), any(), anyInt()))
			.thenAnswer((Answer<Void>) invocation -> null);

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockSearchCriteriaBean);
			when(mockSearchCriteriaBean.getAttributesCriteria()).thenReturn(mockHashMap);
			when(mockCatalogRequest.isClearAll()).thenReturn(true);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(true);//CAP-47356
			buildMockUserGroupSearchOptions();
			CatalogAttributeResponse response = serviceToTest.getCatalogAttributes(mockSessionContainer, mockCatalogRequest);
			assertNotNull(response);
			assertNotNull(response.getCatalogAttributes());
		}
	}

	@Test
	void that_CatalogAttributes_StandardAttributesInSession_success_haswizard_hasrefine_false() throws Exception {

		//Catalog
		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		buildMockUserGroupSearchOptions();
		
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {	
			
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "lblStandardAttr")).thenReturn("");//CAP-46821
					
		
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
				.thenAnswer((Answer<Void>) invocation -> null);
	
			catalogAttributeRequest = getCatalogAttributesRequest();
			standardAttributes = getStandardAttributes();

			//NewItemOnly=TRUE, FilterNewItems=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(standardAttributes);
			standardAttributes.setFilterNewItems(false);
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(false);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(false);//CAP-47356
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());

			//NewItemOnly=TRUE, FilterNewItems=TRUE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		
			//NewItemOnly=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(false);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_CatalogAttributes_StandardAttributesInSession_success_haswizard_hasrefine_false_true() throws Exception {

		//Catalog
		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		buildMockUserGroupSearchOptions();
		
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {	
			
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "lblStandardAttr")).thenReturn("");//CAP-46821
					
		
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
				.thenAnswer((Answer<Void>) invocation -> null);
	
			catalogAttributeRequest = getCatalogAttributesRequest();
			standardAttributes = getStandardAttributes();

			//NewItemOnly=TRUE, FilterNewItems=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(standardAttributes);
			standardAttributes.setFilterNewItems(false);
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(false);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(true);//CAP-47356
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());

			//NewItemOnly=TRUE, FilterNewItems=TRUE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		
			//NewItemOnly=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(false);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_CatalogAttributes_StandardAttributesInSession_success_haswizard_hasrefine_true_false() throws Exception {

		//Catalog
		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);//CAP-47345
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		buildMockUserGroupSearchOptions();
		
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog1 = mockConstruction(OECatalogAssembler.class, 
						(mock, context) -> {when(mock.getFavoritesFeaturedItemsCount(any()))
							.thenReturn(featFaveItemsLst);})) {	
			
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "lblStandardAttr")).thenReturn("");//CAP-46821
					
		
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt()))
				.thenAnswer((Answer<Void>) invocation -> null);
	
			catalogAttributeRequest = getCatalogAttributesRequest();
			standardAttributes = getStandardAttributes();

			//NewItemOnly=TRUE, FilterNewItems=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(standardAttributes);
			standardAttributes.setFilterNewItems(false);
			when(mockUserSettings.isAllowOrderWizard()).thenReturn(true);//CAP-47345
			when(mockUserSettings.isAllowRefineWizardSearch()).thenReturn(false);//CAP-47356
			
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());

			//NewItemOnly=TRUE, FilterNewItems=TRUE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		
			//NewItemOnly=FALSE
			//when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(false);
			standardAttributes.setFilterNewItems(true);
		
			response = serviceToTest.getCatalogAttributes(mockSessionContainer, catalogAttributeRequest);
			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}
	
	@Test
	void that_get_wizard_filters_success() throws Exception 
	{
		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getOrderWizardID()).thenReturn(12345);
		
		try(MockedConstruction<OAOrderWizardAssembler> mockedoaasm = mockConstruction(OAOrderWizardAssembler.class, 
						(mock, context) -> {
							when(mock.getOrderWizard(anyInt(), anyInt())).thenReturn(mockOAOrderWizardFormBean);
							});
			MockedConstruction<OEOrderWizardAssembler> mockedoeasm = mockConstruction(OEOrderWizardAssembler.class, 
				(mock, context) -> {when(mock.getOrderWizardFilters(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any()))
					.thenReturn(wizardFilters);})) 
		{
			when(mockOAOrderWizardFormBean.isSinglePath()).thenReturn(false);
			when(mockOAOrderWizardFormBean.getKeyAttributeId()).thenReturn(12345);
			when(mockOESessionBean.getOrderWizardSearchAttributes()).thenReturn(orderWizardSearchAttributes);
			
			OrderWizardFilters orderWizardFilters = serviceToTest.getWizardFilters(mockSessionContainer);
			Assertions.assertNotNull(orderWizardFilters);		
		}
	}
	
	@Test
	void that_get_wizard_filters_search_attr_null() throws Exception 
	{
		serviceToTest = Mockito.spy(serviceToTest);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getOrderWizardID()).thenReturn(12345);
		
		try(MockedConstruction<OAOrderWizardAssembler> mockedoaasm = mockConstruction(OAOrderWizardAssembler.class, 
						(mock, context) -> {
							when(mock.getOrderWizard(anyInt(), anyInt())).thenReturn(mockOAOrderWizardFormBean);
							});
			MockedConstruction<OEOrderWizardAssembler> mockedoeasm = mockConstruction(OEOrderWizardAssembler.class, 
				(mock, context) -> {when(mock.getOrderWizardFilters(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any()))
					.thenReturn(wizardFilters);})) 
		{
			when(mockOAOrderWizardFormBean.isSinglePath()).thenReturn(false);
			when(mockOAOrderWizardFormBean.getKeyAttributeId()).thenReturn(12345);
			when(mockOESessionBean.getOrderWizardSearchAttributes()).thenReturn(null);
			
			OrderWizardFilters orderWizardFilters = serviceToTest.getWizardFilters(mockSessionContainer);
			Assertions.assertNull(orderWizardFilters.getWizardFilters());		
		}
	}
	
	@Test
	void that_process_order_wizard_filters_success() throws Exception 
	{
		serviceToTest = Mockito.spy(serviceToTest);
		
		OrderWizardFilters orderWizardFilters = new OrderWizardFilters();
		LinkedHashMap<String,Map<String,String>> wizardFilters = new LinkedHashMap<String,Map<String,String>>();
		Map<String,String> keyedFilterQuestionValues = new LinkedHashMap<String,String>();
		keyedFilterQuestionValues.put("4403~0", "Please make a selection");
		keyedFilterQuestionValues.put("4403~253915", "Yes");
		keyedFilterQuestionValues.put("4403~253916", "No");
		wizardFilters.put("Misc questions?", keyedFilterQuestionValues);
		Map<String,String> keyedFilterQuestionValues2 = new LinkedHashMap<String,String>();
		keyedFilterQuestionValues2.put("4405~0", "Please make a selection");
		keyedFilterQuestionValues2.put("4405~253920", "Really awesome");
		keyedFilterQuestionValues2.put("4405~253921", "Super awesome");
		keyedFilterQuestionValues2.put("4405~253922", "Totally awesome");
		wizardFilters.put("awe", keyedFilterQuestionValues2);
		orderWizardFilters.setWizardFilters(wizardFilters);
		
		List<CatalogAttributes> attributeListOptionList = serviceToTest.processOrderWizardFilters(orderWizardFilters, null);

		Assertions.assertNotNull(attributeListOptionList);		
	}
	
	@Test
	void that_process_order_wizard_filters_selected_success() throws Exception 
	{
		serviceToTest = Mockito.spy(serviceToTest);
		
		OrderWizardFilters orderWizardFilters = new OrderWizardFilters();
		LinkedHashMap<String,Map<String,String>> wizardFilters = new LinkedHashMap<String,Map<String,String>>();
		Map<String,String> keyedFilterQuestionValues = new LinkedHashMap<String,String>();
		keyedFilterQuestionValues.put("4403~0", "Please make a selection");
		keyedFilterQuestionValues.put("4403~253915", "Yes");
		keyedFilterQuestionValues.put("4403~253916", "No");
		wizardFilters.put("Misc questions?", keyedFilterQuestionValues);
		Map<String,String> keyedFilterQuestionValues2 = new LinkedHashMap<String,String>();
		keyedFilterQuestionValues2.put("4405~0", "Please make a selection");
		keyedFilterQuestionValues2.put("4405~253920", "Really awesome");
		keyedFilterQuestionValues2.put("4405~253921", "Super awesome");
		keyedFilterQuestionValues2.put("4405~253922", "Totally awesome");
		wizardFilters.put("awe", keyedFilterQuestionValues2);
		orderWizardFilters.setWizardFilters(wizardFilters);
		
		Map<Integer, List<Integer>> attributesCriteria = new HashMap<Integer, List<Integer>>();
		List<Integer> arrtID = new ArrayList<>();
		arrtID.add(253906);
		attributesCriteria.put(4401, arrtID);
		List<Integer> arrtID2 = new ArrayList<>();
		arrtID.add(253909);
		attributesCriteria.put(4402, arrtID2);
		List<Integer> arrtID3 = new ArrayList<>();
		arrtID.add(253916);
		attributesCriteria.put(4403, arrtID3);
		List<Integer> arrtID4 = new ArrayList<>();
		arrtID.add(253921);
		attributesCriteria.put(4405, arrtID4);
		
		List<CatalogAttributes> attributeListOptionList = serviceToTest.processOrderWizardFilters(orderWizardFilters, attributesCriteria);

		Assertions.assertNotNull(attributeListOptionList);		
	}
}
