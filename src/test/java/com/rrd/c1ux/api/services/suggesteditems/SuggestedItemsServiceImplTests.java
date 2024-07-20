
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
 *	06/27/24	Krishna Natarajan	CAP-50540		Added a mock to call the setImageURL method
 *  07/01/24	Krishna Natarajan	CAP-50540		Added a mock for Item and CatalogItem
 *	07/09/24	L De Leon			CAP-50834		Added tests for isItemInCart() method
 */

package com.rrd.c1ux.api.services.suggesteditems;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.services.items.locator.CMCompanionItemComponentLocatorServiceImpl;
import com.rrd.custompoint.admin.entity.Message;
import com.rrd.custompoint.admin.entity.Messages;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.Item;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.catalogs.ao.CMCompanionItemAssembler;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.catalogs.vo.AlternateCatalogData;
import com.wallace.atwinxs.catalogs.vo.CompanionItemRelationshipCompositeVO;
import com.wallace.atwinxs.catalogs.vo.CompanionItemRelationshipVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVOKey;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.ICompanionItem;
import com.wallace.atwinxs.interfaces.IFeaturedItems;
import com.wallace.atwinxs.interfaces.IOECompanionItemComponent;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.component.OECompanionItemComponent;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemResultsExtendedVO;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemResultsVO;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemResultsVOKey;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemSearchCriteriaVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

class SuggestedItemsServiceImplTests extends BaseServiceTest {

	@InjectMocks
	private SuggestedItemsServiceImpl serviceToTest;

	@Mock
	private IFeaturedItems mockFeaturedItems;

	@Mock
	private ICatalog mockCatalog;

	@Mock
	private OEItemSearchCriteriaSessionBean mockSearchCriteriaBean;

	@Mock
	private UserGroup mockUserGroup;

	@Mock
	private Messages mockMessage;
	
	@Mock
	private Message mockMsg;
	
	@Mock
	OECompanionItemResultsVO mockOECompanionItemResultsVO;
	
	@Mock
	CMCompanionItemComponentLocatorServiceImpl mockCMCompanionItemComponentLocatorServiceImpl;

	@Mock
	ICompanionItem mockICompanionItem;
	
	@Mock
	private AlternateCatalogData mockAlternateCatalogData;
	
	@Mock
	OECompanionItemResultsVOKey mockOECompanionItemResultsVOKey;
	
	@Mock
	CompanionItemRelationshipCompositeVO mockCompanionItemRelationshipCompositeVO;
	
	@Mock
	OECompanionItemComponent mockOECompanionItemComponent;
	
	@Mock
	OECompanionItemSearchCriteriaVO mockOECompanionItemSearchCriteriaVO;
	
	@Mock
	CustomDocumentItem mockCustomDocumentItem;
	
	@Mock
	OECompanionItemResultsExtendedVO mockOECompanionItemResultsExtendedVO;
	
	@Mock
	OrderLineVO mockOrderLineVO;
	
	@Mock
	CatalogSearchFeaturesFavoritesBean mockCatalogSearchFeaturesFavoritesBean;
	
	@Mock
	ItemImagesVO mockItemImagesVO;
	
	@Mock
	ItemImagesVOKey mockItemImagesVOKey;
	
	@Mock
	CompanionItemRelationshipVO mockCompanionItemRelationshipVO;
	
	@Mock
	Item mockItem;
	
	
	@BeforeEach
	public void setup() throws Exception {
		
	}

	@Test
	void that_getItemLevelSuggestedItem_return_expected() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockCMCompanionItemComponentLocatorServiceImpl.locate(any())).thenReturn(mockICompanionItem);
		when(mockICompanionItem.getCompanionAssignmentsByID(anyInt(), anyInt())).thenReturn(mockCompanionItemRelationshipCompositeVO);
		Collection<OECompanionItemResultsVO> resultsVOs = new ArrayList<>();
		resultsVOs.add(mockOECompanionItemResultsVO);
		when(mockOECompanionItemResultsVO.getAltCatData()).thenReturn(mockAlternateCatalogData);
		when(mockOECompanionItemResultsVO.getCompanionItemKey()).thenReturn(mockOECompanionItemResultsVOKey);
		try (MockedConstruction<CMCompanionItemAssembler> mockedCMCompanionItemAssembler = Mockito.mockConstruction(CMCompanionItemAssembler.class, (mock, context) -> {

	        when(mock.retrieveCompanionItemsByUseLevelItemLevel(anyInt(), any(), any(), any(), any(), anyBoolean(), any(), any(), any())).thenReturn(resultsVOs);

	      })) {
		
		Collection<OECompanionItemResultsExtendedVO> retriveItem = serviceToTest.getItemLevelSuggestedItem(mockSessionContainer, mockOESession, true, "157869", "157869", "977107");
		assertNotNull(retriveItem);
		assertTrue( retriveItem.size()>0);
		}
	}
	
	
	@Test
	void that_getItemLevelSuggestedItem_return_failed() throws Exception {
		Collection<OECompanionItemResultsVO> resultsVOs = new ArrayList<>();
		resultsVOs.add(mockOECompanionItemResultsVO);
		try (MockedConstruction<CMCompanionItemAssembler> mockedCMCompanionItemAssembler = Mockito.mockConstruction(CMCompanionItemAssembler.class, (mock, context) -> {

	        when(mock.retrieveCompanionItemsByUseLevelItemLevel(anyInt(), any(), any(), any(), any(), anyBoolean(), any(), any(), any())).thenReturn(resultsVOs);

	      })) {
		
		Collection<OECompanionItemResultsExtendedVO> retriveItem = serviceToTest.getItemLevelSuggestedItem(mockSessionContainer, mockOESession, false, "157869", "157869", "977107");
		assertTrue(retriveItem.isEmpty());
		}
	}


	@Test
	void that_getSuggestedItems_return_expected() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.isIconPlusViewEnabled()).thenReturn(true);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockUserSettings.getSugItemsDfltDsply()).thenReturn("L");
		Collection<OECompanionItemResultsVO> resultsVOs = new ArrayList<>();
		resultsVOs.add(mockOECompanionItemResultsVO);
		
		Collection<OECompanionItemResultsExtendedVO> suggestedVOs = new ArrayList<>();
		suggestedVOs.add(mockOECompanionItemResultsExtendedVO);
		
		when(mockOECompanionItemResultsExtendedVO.getAltCatData()).thenReturn(mockAlternateCatalogData);
		when(mockAlternateCatalogData.getAlternateCatalogDesc()).thenReturn("test");
		when(mockAlternateCatalogData.getAlternateCatalogDescDisplayType()).thenReturn("A");
		
		Collection<OrderLineVO> orderLineVo=new ArrayList<>();
		orderLineVo.add(mockOrderLineVO);
		
		doReturn(mockOECompanionItemSearchCriteriaVO).when(serviceToTest).createSearchCriteriaVO(mockAppSessionBean, mockOESessionBean, 0);
		
		when(mockObjectMapFactoryService.getComponentObjectMap()).thenReturn(mockComponentObjectMap);
		when(mockComponentObjectMap.getObject(eq(IOECompanionItemComponent.class), isA(CustomizationToken.class))).thenReturn(mockOECompanionItemComponent);
		when(mockComponentObjectMap.getObject(eq(ICatalog.class), isA(CustomizationToken.class))).thenReturn(mockCatalog);
		
	//	doReturn(mockCatalog).when(mockComponentObjectMap).getObject(ICatalog.class, null);
		when(mockOECompanionItemComponent.retrieveCompanionItemsFromCache(any())).thenReturn(suggestedVOs);
		
		doReturn(mockItemImagesVO).when(mockCatalog).getImagesForItem(any());
		
		
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(CustomDocumentItem.class), any()))
				.thenReturn(mockCustomDocumentItem);
		
		when(mockEntityObjectMap.getEntity(eq(Item.class), any())).thenReturn(mockItem);//CAP-50540
		when(mockEntityObjectMap.getEntity(eq(CatalogItem.class), any())).thenReturn(mockCatalogItem);
		when(mockOECompanionItemResultsExtendedVO.getItemNum()).thenReturn("123");
		
		try (MockedStatic<ItemHelper> mockedItemHelper = mockStatic(ItemHelper.class); MockedConstruction<OEShoppingCartAssembler> mockedOEShoppingCartAssembler = Mockito.mockConstruction(OEShoppingCartAssembler.class, (mock, context) -> {

	        when(mock.loadCompanionItems(any(), any(), any(), any(), anyBoolean())).thenReturn(true);
	      });
		
		
			MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
			when(mock.getOrderLines(anyInt())).thenReturn(orderLineVo);
		});
				
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
			
		  ){
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockUtil.when(()->Util.isBlank(any())).thenReturn(true);
			mockUtil.when(()->Util.htmlEncodeQuotes(any())).thenReturn("test");
			
			mockedItemHelper.when(() -> ItemHelper.hasFeaturedItems(any(), any(), anyBoolean())).thenAnswer((Answer<Void>) invocation -> null);
			
			//CAP-50540
			List<ItemThumbnailCellData> suggestedItemsResults = new ArrayList<>();
			mockSearchResult.setImgURL("/images/global/NoImageAvailable.png");
			suggestedItemsResults.add(mockSearchResult);
			serviceToTest.setImageURL(suggestedItemsResults);
			
			CatalogItemsResponse resp=serviceToTest.getSuggestedItems(mockSessionContainer, "", "", "", false);
		
		Assertions.assertNotNull(resp);
		Assertions.assertEquals(true, resp.isSuccess());
     	}
	}
	
	@Test
	void that_getSuggestedItems_return_expected2() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.isIconPlusViewEnabled()).thenReturn(true);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockUserSettings.getSugItemsDfltDsply()).thenReturn("L");
		Collection<OECompanionItemResultsVO> resultsVOs = new ArrayList<>();
		resultsVOs.add(mockOECompanionItemResultsVO);
		
		Collection<OECompanionItemResultsExtendedVO> suggestedVOs = new ArrayList<>();
		when(mockOECompanionItemResultsExtendedVO.getShowRatioInd()).thenReturn(true);
		when(mockOECompanionItemResultsExtendedVO.getRelationshipLvlCd()).thenReturn("test");
		suggestedVOs.add(mockOECompanionItemResultsExtendedVO);
		
		when(mockOECompanionItemResultsExtendedVO.getAltCatData()).thenReturn(mockAlternateCatalogData);
		when(mockAlternateCatalogData.getAlternateCatalogDesc()).thenReturn("test");
		when(mockAlternateCatalogData.getAlternateCatalogDescDisplayType()).thenReturn("R");
		
		Collection<OrderLineVO> orderLineVo=new ArrayList<>();
		orderLineVo.add(mockOrderLineVO);
		
		doReturn(mockOECompanionItemSearchCriteriaVO).when(serviceToTest).createSearchCriteriaVO(mockAppSessionBean, mockOESessionBean, 0);
		
		when(mockObjectMapFactoryService.getComponentObjectMap()).thenReturn(mockComponentObjectMap);
		when(mockComponentObjectMap.getObject(eq(IOECompanionItemComponent.class), isA(CustomizationToken.class))).thenReturn(mockOECompanionItemComponent);
		when(mockComponentObjectMap.getObject(eq(ICatalog.class), isA(CustomizationToken.class))).thenReturn(mockCatalog);
		
		when(mockOECompanionItemComponent.retrieveCompanionItemsFromCache(any())).thenReturn(suggestedVOs);
		
		doReturn(mockItemImagesVO).when(mockCatalog).getImagesForItem(any());
		doReturn("test").when(mockItemImagesVO).getItemFullImgLocURL();
		doReturn("test").when(mockItemImagesVO).getItemMedImgLocURL();
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(CustomDocumentItem.class), any()))
				.thenReturn(mockCustomDocumentItem);
		
		when(mockEntityObjectMap.getEntity(eq(Item.class), any())).thenReturn(mockItem);//CAP-50540
		when(mockEntityObjectMap.getEntity(eq(CatalogItem.class), any())).thenReturn(mockCatalogItem);
		when(mockOECompanionItemResultsExtendedVO.getItemNum()).thenReturn("123");
		
		try (MockedStatic<ItemHelper> mockedItemHelper = mockStatic(ItemHelper.class); MockedConstruction<OEShoppingCartAssembler> mockedOEShoppingCartAssembler = Mockito.mockConstruction(OEShoppingCartAssembler.class, (mock, context) -> {

	        when(mock.loadCompanionItems(any(), any(), any(), any(), anyBoolean())).thenReturn(true);

	      });
		
		
			MockedConstruction<OECatalogAssembler> mockedCatalog = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
			when(mock.getOrderLines(anyInt())).thenReturn(orderLineVo);
		});
				
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
			
		  ){
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockUtil.when(()->Util.isBlank(any())).thenReturn(true);
			mockUtil.when(()->Util.htmlEncodeQuotes(any())).thenReturn("test");
			mockUtil.when(()->Util.isBlankOrNull(any())).thenReturn(true);
			
			mockedItemHelper.when(() -> ItemHelper.hasFeaturedItems(any(), any(), anyBoolean())).thenAnswer((Answer<Void>) invocation -> null);

		
		CatalogItemsResponse resp=serviceToTest.getSuggestedItems(mockSessionContainer, "", "", "", false);
		Assertions.assertNotNull(resp);
		Assertions.assertEquals(true, resp.isSuccess());
     	}
	}
	
	
	@Test
	void that_getRatioMessage_i_return() throws Exception {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockCMCompanionItemComponentLocatorServiceImpl.locate(any())).thenReturn(mockICompanionItem);
		when(mockICompanionItem.retrieveCompanionItemRelationship(anyInt(), anyInt())).thenReturn(mockCompanionItemRelationshipCompositeVO);
		String retriveItem = serviceToTest.getRatioMessage("I", mockApplicationSession, mockOECompanionItemResultsExtendedVO);
		assertNull(retriveItem);
	}

	@Test
	void that_getRatioMessage_o_return() throws Exception {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		String retriveItem = serviceToTest.getRatioMessage("O", mockApplicationSession, mockOECompanionItemResultsExtendedVO);
		assertNull(retriveItem);
	}
	
	
	
	@Test
	void that_getRatioMessage_other_return() throws Exception {
		String retriveItem = serviceToTest.getRatioMessage("test", mockApplicationSession, mockOECompanionItemResultsExtendedVO);
		assertNotNull(retriveItem);
	}
	
	
	@Test
	void that_createSearchCriteriaVO_return() throws Exception {
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowCatalogViewSelection()).thenReturn(true);
		OECompanionItemSearchCriteriaVO createSearchCriteriaVO=serviceToTest.createSearchCriteriaVO(mockAppSessionBean, mockOESessionBean, 1);
		assertNotNull(createSearchCriteriaVO);
	}

	// CAP-50834
	@Test
	void that_isItemInCart_return_false() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);

		Collection<OrderLineVO> orderLineVo = new ArrayList<>();
		orderLineVo.add(mockOrderLineVO);

		doReturn(true).when(serviceToTest).isCustomizableItem(any(), any(), any(), any());

		boolean isItemInCart = serviceToTest.isItemInCart(mockApplicationSession, orderLineVo,
				mockOECompanionItemResultsExtendedVO);
		Assertions.assertFalse(isItemInCart);
	}

	// CAP-50834
	@Test
	void that_isItemInCart_return_true() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);

		Collection<OrderLineVO> orderLineVo=new ArrayList<>();
		orderLineVo.add(mockOrderLineVO);

		doReturn(false).when(serviceToTest).isCustomizableItem(any(), any(), any(), any());

		try (MockedStatic<ItemHelper> mockedItemHelper = mockStatic(ItemHelper.class)) {

			mockedItemHelper.when(() -> ItemHelper.isItemInCart(any(), any(), any(), any())).thenReturn(true);

			boolean isItemInCart = serviceToTest.isItemInCart(mockApplicationSession, orderLineVo,
					mockOECompanionItemResultsExtendedVO);
			Assertions.assertTrue(isItemInCart);
		}
	}
}