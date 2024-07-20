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
 *  10/03/23	Satishkumar A	CAP-43282	C1UX BE - API Build - Get OE Item Filter Options - including favorites, featured types
 */
package com.rrd.c1ux.api.services.standardattributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
import com.rrd.c1ux.api.models.standardattributes.StandardAttributesResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.catalog.entity.StandardAttributes;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@WithMockUser
class StandardAttributesServiceImplTests extends BaseServiceTest {

	@InjectMocks
	private StandardAttributesServiceImpl serviceToTest;
	
	@Mock 
	OEItemSearchCriteriaSessionBean mockOEItemSearchCriteriaSessionBean;
	
	@Mock
	StandardAttributes mockStandardAttributes;
	
	@Mock
	CatalogSearchFeaturesFavoritesBean mockCatalogSearchFeaturesFavoritesBean;
	
	@BeforeEach
	public void setup() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
	
	}
	
	@Test
	void that_StandardAttributes_ValuesFromSession_success() throws Exception {
		

		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);

		StandardAttributesResponse response = new StandardAttributesResponse();
		when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(mockStandardAttributes);
		when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
		when(mockStandardAttributes.isFilterNewItems()).thenReturn(false);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);

		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		}
		
		when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(true);
		when(mockStandardAttributes.isFilterNewItems()).thenReturn(true);
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);

		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		}

		when(mockOEItemSearchCriteriaSessionBean.isNewItemsOnly()).thenReturn(false);
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);

		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		}
	}

	@Test
	void that_StandardAttributes_NoStandardAttributesInSession_success() throws Exception {
		


		when(mockOESessionBean.getSearchCriteriaBean()).thenReturn(mockOEItemSearchCriteriaSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);

		StandardAttributesResponse response = new StandardAttributesResponse();
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockOEItemSearchCriteriaSessionBean.getStandardAttributes()).thenReturn(null);
		doReturn(null).when(serviceToTest).getCatSearchFeatFaveBean(any(),any(),any(),any());
		when(mockOESessionBean.isAllowUserFavorites()).thenReturn(true);
		when(mockOESessionBean.getUserSettings()).thenReturn(null);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");
		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		}
		

		doReturn(mockCatalogSearchFeaturesFavoritesBean).when(serviceToTest).getCatSearchFeatFaveBean(any(),any(),any(),any());
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isNewItemsFlag()).thenReturn(true);
		when(mockOESessionBean.isAllowUserFavorites()).thenReturn(false);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");

		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		
		
		}
		
		when(mockUserSettings.isNewItemsFlag()).thenReturn(false);
		when(mockOESessionBean.isAllowUserFavorites()).thenReturn(true);
		when(mockCatalogSearchFeaturesFavoritesBean.hasFavoriteItems()).thenReturn(false);
		when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsSearch()).thenReturn(new HashMap<>());
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");

		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		}
		AlertCounts[] alert = new AlertCounts[1];
		alert[0] = new AlertCounts("A", "", 0, 0);
		when(mockCatalogSearchFeaturesFavoritesBean.getFeaturedItemsDefined()).thenReturn(alert);
		
		when(mockCatalogSearchFeaturesFavoritesBean.hasFavoriteItems()).thenReturn(true);
		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)){
			mockSessionHandler.when(() -> SessionHandler.saveSession(any(), anyInt(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");

		response = serviceToTest.getStandardAttributeList(mockSessionContainer);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		}

	}
	
	@Test
	void that_StandardAttributes_getCatSearchFeatFaveBean_success() throws Exception {
		
		CatalogSearchFeaturesFavoritesBean response = new CatalogSearchFeaturesFavoritesBean();
		serviceToTest = Mockito.spy(serviceToTest);

	
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(new Locale("US-en"));

		when(mockAppSessionBean.getEncodedSessionId()).thenReturn(ENCRYPTED_SESSION_ID);

		try(MockedStatic<SessionHandler> mockSessionHandler = Mockito.mockStatic(SessionHandler.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)){
			mockSessionHandler.when(() -> SessionHandler.persistServiceInSession(any(), any(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), "favoritesStandardAttrLbl")).thenReturn("");

			try(MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
				when(mock.getFavoritesFeaturedItemsCount(any())).thenReturn(getCatSearchFeatFaveBeanResponse());
			})) {
		response = serviceToTest.getCatSearchFeatFaveBean(mockSessionContainer, mockApplicationSession, mockOESession, mockCatalogSearchFeaturesFavoritesBean);
		Assertions.assertNotNull(response);
		//Assertions.assertEquals(true, response.isSuccess());
			
		}
	}

	}
	public List<AlertCountResponseBean> getCatSearchFeatFaveBeanResponse(){
		List<AlertCountResponseBean> quickFindAlert = new ArrayList<AlertCountResponseBean>();
		 AlertCounts faveCntAlert = new AlertCounts("AlertName", "AlertName", 0, 1);
		 AlertCountResponseBean faveResBean = new AlertCountResponseBean(new AlertCounts [] {faveCntAlert});
		 faveResBean.setAlertCategory(AtWinXSConstant.QUICK_FIND_FAVORITE_ITEMS);
		 faveResBean.setActionID(OrderEntryConstants.BROKER_OE_CATALOG);
		 faveResBean.setEventID(OrderEntryConstants.QUICK_FIND_FAVORITE_ITEMS_SEARCH_EVT);
		 quickFindAlert.add(faveResBean);
		 return quickFindAlert;

	}

}
