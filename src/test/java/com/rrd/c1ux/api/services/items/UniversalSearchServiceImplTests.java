/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/31/2023  N Caceres		CAP-39050	Initial version
 *	09/15/2023	L De Leon		CAP-43196	Added test methods for setFeatureMap() method
 */
package com.rrd.c1ux.api.services.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.FeatureFavoriteItemData;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;

class UniversalSearchServiceImplTests extends BaseServiceTest {

	private static final int ORDER_ID = 1;
	private static final String ITEM_NUMBER = "10116020";
	private static final String ITEM_DESC = "ITEM_DESC";
	private static final String MED_IMG = "MED_IMG";
	private static final String LARGE_IMG = "LARGE_IMG";
	
	@Mock
	CatalogLineVO mockLineVo;

	@InjectMocks
	private UniversalSearchServiceImpl serviceToTest;
	
	@Mock
	FeatureFavoriteItemData featureFavoriteItemData;
	
	@Test
	void that_isItemInCart_is_true() throws AtWinXSException {
		CatalogLineVO line  = new CatalogLineVO();
		buidLine(line);
		try (MockedStatic<ItemHelper> mockedStatic = mockStatic(ItemHelper.class);
				MockedConstruction<OECatalogAssembler> mockedAssembler = mockConstruction(OECatalogAssembler.class, (mock, context) -> {
		          when(mock.getOrderLines(anyInt())).thenReturn(new ArrayList<>());
		        })) {
			mockedStatic.when(() -> ItemHelper.isItemInCart(any(AppSessionBean.class), anyString(), anyString(), anyCollection())).thenReturn(true);
			
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockVolatileSessionBean.getOrderId()).thenReturn(ORDER_ID);
			
			assertTrue(serviceToTest.isItemInCart(line, mockAppSessionBean, mockVolatileSessionBean));
		}
		
	}
	
	@Test
	void  that_showAddToCart_is_true() {
		SearchResult result = new SearchResult();
		buildResult(result);
		assertTrue(serviceToTest.showAddToCart(result, false, false));
	}
	
	private void buidLine(CatalogLineVO line) {
		line.setItemNum(ITEM_NUMBER);
		line.setWcsItemNum(ITEM_NUMBER);
	}
	
	private void buildResult(SearchResult result) {
		result.setViewOnlyFlag(false);
		result.setInvalidItemFlag(false);
		result.setItemOrderable(true);
	}

	// CAP-43196
	@Test
	void that_setFeatureMap_has_featuredItems() throws AtWinXSException {

		SearchResult thumb = new SearchResult();
		try (MockedStatic<ItemHelper> mockedStatic = mockStatic(ItemHelper.class);
				MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
			mockedStatic.when(() -> ItemHelper.hasFeaturedItems(any(AppSessionBean.class),
					any(VolatileSessionBean.class), anyBoolean())).thenReturn(true);
			//CAP-43967 made change to adapt the ItemUtlity buildFeatureMap() method changes
			mockedItemUtility.when(()-> ItemUtility.buildFeatureMap(any(), any())).thenReturn(featureFavoriteItemData);
			serviceToTest.setFeatureMap(thumb, mockAppSessionBean, mockVolatileSessionBean, mockLineVo);
			assertNotNull(thumb.getFeatureFavoriteItemData());
		}
	}

	@Test
	void that_setFeatureMap_has_noFeaturedItems() throws AtWinXSException {

		SearchResult thumb = new SearchResult();
		try (MockedStatic<ItemHelper> mockedStatic = mockStatic(ItemHelper.class)) {
			mockedStatic.when(() -> ItemHelper.hasFeaturedItems(any(AppSessionBean.class),
					any(VolatileSessionBean.class), anyBoolean())).thenReturn(false);

			serviceToTest.setFeatureMap(thumb, mockAppSessionBean, mockVolatileSessionBean, mockLineVo);
			assertNull(thumb.getFeatureMap());
		}
	}

	@Test
	void that_setItemDetailsFieldValues_is_success() throws AtWinXSException {

		SearchResult thumb = new SearchResult();
		serviceToTest.setItemDetailsFieldValues(thumb, mockLineVo, ITEM_DESC, MED_IMG, LARGE_IMG);
		assertFalse(Util.isBlankOrNull(thumb.getLargeImageURL()));
		assertEquals(LARGE_IMG, thumb.getLargeImageURL());
		assertFalse(Util.isBlankOrNull(thumb.getCartImgURL()));
		assertEquals(MED_IMG, thumb.getCartImgURL());
	}
}
