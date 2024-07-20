/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	JIRA #          Description
 *	--------	-----------        	---------		-------------------------------------------------------------------------
 *	09/15/23	S Ramachandran		CAP-43669		Initial version, Routing Info for UnivSearch Line Items search results
 *	09/20/23	N Caceres			CAP-42856		Retrieve all featured items
 *	10/23/23	N Caceres			CAP-44349       JUnits for retrieving the HTML text
 *  02/15/24	Krishna Natarajan	CAP-46323		Resolved Junits with adding required mocks
 *	03/28/24	Krishna Natarajan	CAP-48287		Added logic in building criteria in buildCriteriaCatalogItems method
 *	04/15/24	Krishna Natarajan	CAP-48534		Added a new test case for covering getCatalogMessages
 *	07/01/24	S Ramachandran		CAP-50502		Add test cases for kit container with and without locations names 
 */

package com.rrd.c1ux.api.services.catalogitem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogMessageResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogSearchResultsResponse;
import com.rrd.c1ux.api.models.favorite.FeaturedCatalogItemsRequest;
import com.rrd.c1ux.api.rest.catalog.CatalogItemRequest;
import com.rrd.custompoint.admin.entity.Message;
import com.rrd.custompoint.admin.entity.Messages;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.rrd.custompoint.catalog.entity.CategoryHTML;
import com.rrd.custompoint.framework.util.SearchApplianceFactory.SearchAppliance;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.catalogs.vo.FeaturedItemsTypesVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IFeaturedItems;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;

class CatalogItemRetriveServicesImplTests extends BaseServiceTest {

	private static final String LOGIN_ID = "1";
	private static final String ES = "ES";
	private static final String NO = "N";
	public static final String SUCCESS = "Success";
	public static final String FAIL="Failed";
	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;
	public static final String GENERIC_422MESSAGE = "Validation failed";
	private static final int SITE_ID = 0;
	private static final int ID_NUM = 1;
	private static final int PROFILE_NUMBER = 2;
	private static final String USER_GROUP = "C1UX";
	private static List<String> kitTemplateContainerLocations = new ArrayList<>();;

	@InjectMocks
	private CatalogItemRetriveServicesImpl serviceToTest;

	@Mock
	private IFeaturedItems mockFeaturedItems;

	@Mock
	private ICatalog mockCatalog;

	@Mock
	private OEItemSearchCriteriaSessionBean mockSearchCriteriaBean;

	@Mock
	private CategoryHTML mockCategoryHTML;

	@Mock
	private AlertCounts mockAlertCounts;

	@Mock
	private CatalogSearchFeaturesFavoritesBean mockCatalogSearchFeaturesFavoritesBean;
	
	@Mock
	private UserGroup mockUserGroup;

	@Mock
	private Messages mockMessage;
	
	@Mock
	private Message mockMsg;

	private FeaturedCatalogItemsRequest featuredItemsRequest = new FeaturedCatalogItemsRequest();
	private CatalogItemRequest catalogItemsRequest = new CatalogItemRequest();

	@BeforeEach
	public void setup() throws Exception {
		catalogItemsRequest.setSelectedCategoryId(1);
	}

	//CAP-43630 - BEGIN
	@Test
	void that_retrieveRoutingInformationBadgeTest1_return_withRoutingKeyAndMsg() throws Exception {

		//Retrieve Routing Info based on admin setting ShowRT=Yes, IsAvalRT=Yes, SubToRnA=Yes
		when(mockUserSettings.isShowRoutingInfo()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);

		Map<String, String> routingBadge = serviceToTest.
				retrieveRoutingInformationBadge(mockSearchResult, mockAppSessionBean, mockUserSettings,
						mockOESessionBean);

		assertNotNull(routingBadge);
		assertTrue( routingBadge.size()>0);
	}

	@Test
	void that_retrieveRoutingInformationBadgeTest2_return_withOutRoutingKeyAndMsg() throws Exception {

		//Retrieve Routing Info based on admin setting ShowRT=No, IsAvalRT=Yes/No, SubToRnA=Yes/No
		when(mockUserSettings.isShowRoutingInfo()).thenReturn(false);

		Map<String, String> routingBadge = serviceToTest.
				retrieveRoutingInformationBadge(mockSearchResult, mockAppSessionBean, mockUserSettings,
						mockOESessionBean);

		assertNotNull(routingBadge);
		assertFalse( routingBadge.size()>0);
	}

	@Test
	void that_retrieveRoutingInformationBadgeTest3_return_withOutRoutingKeyAndMsg() throws Exception {

		//Retrieve Routing Info based on admin setting ShowRT=Yes, IsAvalRT=No, SubToRnA=Yes/No
		when(mockUserSettings.isShowRoutingInfo()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(false);

		Map<String, String> routingBadge = serviceToTest.
				retrieveRoutingInformationBadge(mockSearchResult, mockAppSessionBean, mockUserSettings,
						mockOESessionBean);

		assertNotNull(routingBadge);
		assertFalse( routingBadge.size()>0);
	}

	@Test
	void that_retrieveRoutingInformationBadgeTest4_return_withOutRoutingKeyAndMsg() throws Exception {

		//Retrieve Routing Info based on admin setting ShowRT=Yes, IsAvalRT=Yes, SubToRnA=No
		when(mockUserSettings.isShowRoutingInfo()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(false);


		Map<String, String> routingBadge = serviceToTest.
				retrieveRoutingInformationBadge(mockSearchResult, mockAppSessionBean, mockUserSettings,
						mockOESessionBean);

		assertNotNull(routingBadge);
		assertFalse( routingBadge.size()>0);
	}
	//CAP-43630 - END

	// CAP-42856
	//@Test commented as we have expedited CAP-47291
	void that_getFeaturedCatalogItems_returnFeaturedItemsOnly() throws AtWinXSException, IllegalAccessException, InvocationTargetException, CPRPCException {
		try (MockedStatic<ItemHelper> mockedItemHelper = mockStatic(ItemHelper.class);
				MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
					when(mock.isAttrUdfFilter(any(), any())).thenReturn(true);
				})) {
			mockedItemHelper.when(() -> ItemHelper.buildItemAttributesSearchCriteria(any(), isA(CatalogSearchResultsCriteria.class), any(), any(), any())).thenAnswer((Answer<Void>) invocation -> null);

			OEItemSearchCriteriaSessionBean criteria = new OEItemSearchCriteriaSessionBean();
			Collection<FeaturedItemsTypesVO> featuredTypes = buildFeaturedTypes();
			buildCriteriaForAllFeaturedItems(criteria);

			mockCommonMethods();
			when(mockAppSessionBean.getSiteID()).thenReturn(SITE_ID);
			when(mockFeaturedItemsLocator.locate(isA(CustomizationToken.class))).thenReturn(mockFeaturedItems);
			when(mockFeaturedItems.getFeaturedTypes(anyInt(), any(), anyBoolean())).thenReturn(featuredTypes);
			when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(true);
			when(mockOESessionBean.getCatalogSearchFeaturesFavoritesBean()).thenReturn(mockCatalogSearchFeaturesFavoritesBean);
			when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsDefined()).thenReturn(null);
			when(serviceToTest.doSearch(mockServletContext, mockApplicationSession, mockApplicationVolatileSession, mockOESession, criteria, false)).thenReturn(null);
			doNothing().when(serviceToTest).getStandardAttributeList(mockAppSessionBean, mockOESession);
			CatalogItemsResponse response = serviceToTest.getFeaturedCatalogItems(mockServletContext, mockApplicationSession,
					mockApplicationVolatileSession, mockOESession, criteria, featuredItemsRequest);

			assertNotNull(response);
		}
	}

	//@Test
	void that_getCatalogItems_returnCatalogItems() throws AtWinXSException {
		OEItemSearchCriteriaSessionBean criteria = new OEItemSearchCriteriaSessionBean();
		buildCriteriaCatalogItems(criteria);

		try (MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
			when(mock.isAttrUdfFilter(any(), any())).thenReturn(true);
		})) {
			mockCommonMethods();
			mockCategoryHTML();
			when(mockUserSettings.getPrimaryAttribute()).thenReturn(ID_NUM);
			when(mockOESessionBean.getCatalogSearchFeaturesFavoritesBean()).thenReturn(mockCatalogSearchFeaturesFavoritesBean);
			when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsDefined()).thenReturn(null);
			CatalogItemsResponse response = serviceToTest.getCatalogItems(mockServletContext, mockApplicationSession,
					mockApplicationVolatileSession, mockOESession, criteria, false, catalogItemsRequest);

			assertNotNull(response);
		}
	}

	//@Test
	void that_doSearchWithCategory_success() throws AtWinXSException {
		OEItemSearchCriteriaSessionBean criteria = new OEItemSearchCriteriaSessionBean();
		buildCriteriaCatalogItems(criteria);
		buildCatalogItemsRequest(catalogItemsRequest);

		try (MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
			when(mock.isAttrUdfFilter(any(), any())).thenReturn(true);
		})) {
			mockCommonMethods();
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockObjectMapFactoryService.getComponentObjectMap()).thenReturn(mockComponentObjectMap);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			mockCategoryHTML();
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockOESessionBean.getCatalogSearchFeaturesFavoritesBean()).thenReturn(mockCatalogSearchFeaturesFavoritesBean);
			when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsDefined()).thenReturn(null);

			CatalogSearchResultsResponse response = serviceToTest.doSearchWithCategory(mockServletContext, mockApplicationSession,
					mockApplicationVolatileSession, mockOESession, criteria, false, catalogItemsRequest);

			assertNotNull(response);
		}
	}

	private void mockCategoryHTML() throws AtWinXSException {
		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockSearchCriteriaBean);
		when(mockSearchCriteriaBean.getSelectedCategoryPeers()).thenReturn(selectedPeers());
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(CategoryHTML.class), isA(CustomizationToken.class))).thenReturn(mockCategoryHTML);
		when(mockCategoryHTML.getHtmlText(anyInt(), anyCollection())).thenReturn("");
	}

	private void mockCommonMethods() {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getComponentObjectMap()).thenReturn(mockComponentObjectMap);
		when(mockComponentObjectMap.getObject(eq(ICatalog.class), isA(CustomizationToken.class))).thenReturn(mockCatalog);
	}

	private void buildCriteriaForAllFeaturedItems(OEItemSearchCriteriaSessionBean criteria) throws AtWinXSException {
		criteria.setCanSearchOutsideCatalog(false);
		criteria.setSearchSelectedCategory(false);
		criteria.setUseFeaturedSearchOROperand(true);
		criteria.setFeaturedItemsOnly(true);
		criteria.setSiteID(SITE_ID);
		criteria.setBuID(ID_NUM);
		criteria.setUserGroup(USER_GROUP);
		criteria.setProfileNr(PROFILE_NUMBER);
		criteria.setSearchAppliance(null);

		criteria.getFeaturedItemsSearch().put(String.valueOf(1), true);
		criteria.getFeaturedItemsSearch().put(String.valueOf(2), true);
		criteria.getFeaturedItemsSearch().put(String.valueOf(3), true);
	}

	private Collection<FeaturedItemsTypesVO> buildFeaturedTypes() {
		Collection<FeaturedItemsTypesVO> featuredTypes = new ArrayList<FeaturedItemsTypesVO>();
		FeaturedItemsTypesVO featuredType = new FeaturedItemsTypesVO(ID_NUM, SITE_ID, "Featured", "url", ID_NUM, false);
		featuredTypes.add(featuredType);
		return featuredTypes;

	}

	private void buildCriteriaCatalogItems(OEItemSearchCriteriaSessionBean criteria) throws AtWinXSException {
		criteria.setAliasSetting(NO);
		criteria.setBuID(ID_NUM);
		criteria.setContainsForDescription(true);
		criteria.setDisplayUnassigned(true);
		criteria.setBrowse(true);
		criteria.setIncludeViewOnlyOnSearch(true);
		criteria.setSearchSelectedCategory(true);
		criteria.setLoginID(LOGIN_ID);
		criteria.setNewItemsDays(30);
		criteria.setProfileNr(PROFILE_NUMBER);
		criteria.setSearchAppliance(SearchAppliance.fromCode(ES));
		criteria.setSelectedCategoryId(ID_NUM);
		criteria.setSiteID(SITE_ID);
		criteria.setUserGroup(USER_GROUP);
		criteria.setSearchOptions(null);
		criteria.setUnifiedSearchCriteria("ABC");
		criteria.setAlternateCatalogDescInd(true);
	}
	private void buildCatalogItemsRequest(CatalogItemRequest request) {
		request.setSelectedCategoryId(37068);
	}

	private Collection<Integer> selectedPeers() {
		Collection<Integer> selectedPeers = new ArrayList<>();
		selectedPeers.add(1);
		selectedPeers.add(2);

		return selectedPeers;
	}
	
	private List<Message> buildMessage() {
		List<Message> msg= new ArrayList<>();
		msg.add(mockMsg);
		return msg;
	}
	
	//CAP-48534
	@Test
	void that_getCatalogMessages() throws Exception {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
		when(mockUserGroup.getCatalogMessages()).thenReturn(mockMessage);
		when(mockMessage.asCollection()).thenReturn(buildMessage());
		CatalogMessageResponse response = serviceToTest.getCatalogMessages(mockApplicationSession);
		assertNotNull(response);
	}
	
	@Test
	void that_getCatalogMessages_null() throws Exception {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
		when(mockUserGroup.getCatalogMessages()).thenReturn(mockMessage);
		when(mockMessage.asCollection()).thenReturn(null);
		CatalogMessageResponse response = serviceToTest.getCatalogMessages(mockApplicationSession);
		assertNotNull(response);
	}
	
	
	// CAP-50502
	@Test
	void that_setKitContainerLocations_kitTempModeTRUE_locationNamesAvailable() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		// CAP-50502 - when Kit Template Mode is TRUE and location names atleast one (
		// Left, Center, Right)
		when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(true);
		when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(3);
		kitTemplateContainerLocations.add(0, "Left");
		kitTemplateContainerLocations.add(1, "Center");
		kitTemplateContainerLocations.add(2, "Right");
		when(mockVolatileSessionBean.getKitTemplateContainerLocationNames()).thenReturn(kitTemplateContainerLocations);
		CatalogItemsResponse catalogItemsResponse = new CatalogItemsResponse();

		serviceToTest.setKitContainerLocations(mockSessionContainer, catalogItemsResponse);
		assertTrue(catalogItemsResponse.isKitTemplateMode());
		assertNotNull(catalogItemsResponse.getKitContainerLocations());
	}

	// CAP-50502
	@Test
	void that_setKitContainerLocations_kitTempModeTRUE_locationNameEmpty() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		// CAP-50502 - when Kit Template Mode is TRUE and location name empty
		when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(true);
		when(mockVolatileSessionBean.getKitTemplateContainerLocations()).thenReturn(1);
		when(mockVolatileSessionBean.getKitTemplateContainerLocationNames()).thenReturn(kitTemplateContainerLocations);
		CatalogItemsResponse catalogItemsResponse = new CatalogItemsResponse();

		serviceToTest.setKitContainerLocations(mockSessionContainer, catalogItemsResponse);
		assertTrue(catalogItemsResponse.isKitTemplateMode());
		assertNotNull(catalogItemsResponse.getKitContainerLocations());
	}

	// CAP-50502
	@Test
	void that_setKitContainerLocations_kitTempModeFALSE() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		// CAP-50502 - when Kit Template Mode is TRUE and location name empty
		when(mockVolatileSessionBean.isKitTemplateMode()).thenReturn(false);
		CatalogItemsResponse catalogItemsResponse = new CatalogItemsResponse();

		serviceToTest.setKitContainerLocations(mockSessionContainer, catalogItemsResponse);
		assertFalse(catalogItemsResponse.isKitTemplateMode());
		assertNull(catalogItemsResponse.getKitContainerLocations());
	}
}
