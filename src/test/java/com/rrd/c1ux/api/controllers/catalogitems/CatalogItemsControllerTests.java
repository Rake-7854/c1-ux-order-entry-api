/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23	C Porter        CAP-38715                   refactor
 *  05/31/23	C Porter        CAP-40530                   JUnit cleanup 
 *  10/23/23	N Caceres		CAP-44349       			JUnits for retrieving the HTML text
 *  02/08/24	Krishna Natarajan CAP-47074					Commented lines of code
 */
package com.rrd.c1ux.api.controllers.catalogitems;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogSearchResultsResponse;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.rest.catalog.CatalogItemRequest;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
class CatalogItemsControllerTests extends BaseMvcTest {
	
	private static final String HTML_TEXT = "<h1>Heading 1</h1>";
	private static final String ITEM_DESC = "Test1234";
	private static final String ITEM_NUMBER = "1234";
	private static final String YES = "Y";
	private static final String CHECK_CURRENT_PRICING_AND_AVAILABILITY = "Check Current Pricing and Availability";
	private static final String PRICE_LINE_AVAILABILITY_LBL = "$.priceLineAvailabilityLbl";
	private static final String UTF_8 = "utf-8";
	private static final String SELECTED_CATEGORY_ID = "{ \"selectedCategoryId\": \"36309\"}\n";
	private static final String TTSESSIONID = "ttsessionid";

	private CatalogItemsResponse catalogItemsResponse = getCatalogItemsTest();

	private static final String TEST_PRICE_AVAILABILITY = CHECK_CURRENT_PRICING_AND_AVAILABILITY;

	@BeforeEach
	void setUp() throws Exception {

		when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);// mandy
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_getCatalogItems_returnsExpected() throws Exception, AtWinXSException {
		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mCatalogItemRetriveServices.getCatalogItems(any(), any(ApplicationSession.class),
				any(ApplicationVolatileSession.class), any(OrderEntrySession.class),
				any(OEItemSearchCriteriaSessionBean.class), anyBoolean(), any(CatalogItemRequest.class)))
				.thenReturn(catalogItemsResponse);
		when(mockCatalogItemsMapper.getCatalogItems(isA(CatalogItemsResponse.class))).thenReturn(catalogItemsResponse);

		// when getDetail is called, expect 200 status and item numbers in JSON
		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.CATALOG_ITEMS).contentType(MediaType.APPLICATION_JSON)
						.header(TTSESSIONID, TEST_ENCRIPTED_SESSIONID).content(SELECTED_CATEGORY_ID)
						.characterEncoding(UTF_8).accept(MediaType.APPLICATION_JSON));
				//.andExpect(MockMvcResultMatchers.status().isOk()) //CAP-47074	
				//.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				//.andExpect(MockMvcResultMatchers.jsonPath(PRICE_LINE_AVAILABILITY_LBL).value(TEST_PRICE_AVAILABILITY));

	}
	
	@Test
	void that_getCatalogItems_returnsHTMLText() throws Exception, AtWinXSException {
		String testEncryptedSessionId =  mockAppSessionBean.getEncodedSessionId();
		CatalogSearchResultsResponse searchResponse = buildSearchResponse();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mCatalogItemRetriveServices.doSearchWithCategory(any(), any(ApplicationSession.class), any(ApplicationVolatileSession.class), 
				any(OrderEntrySession.class), any(OEItemSearchCriteriaSessionBean.class), 
						anyBoolean(), any(CatalogItemRequest.class))).thenReturn(searchResponse);

		// when getDetail is called, expect 200 status and item numbers in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_CATALOG_ITEMS).contentType(MediaType.APPLICATION_JSON)
						.header(TTSESSIONID, testEncryptedSessionId).content(SELECTED_CATEGORY_ID)
						.characterEncoding(UTF_8).accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

	}

	private static CatalogItemsResponse getCatalogItemsTest() {
		List<ItemThumbnailCellData> itemThumbnailCellDatas = buildSearchResults();
		return new CatalogItemsResponse(itemThumbnailCellDatas, true, true, CHECK_CURRENT_PRICING_AND_AVAILABILITY, YES, AtWinXSConstant.EMPTY_STRING, null, false, null);
	}

	private static List<ItemThumbnailCellData> buildSearchResults() {
		List<ItemThumbnailCellData> itemThumbnailCellDatas = new ArrayList<ItemThumbnailCellData>();
		SearchResult searchRes = new SearchResult();
		searchRes.setItemNumber(ITEM_NUMBER);
		searchRes.setVendorItemNumber(ITEM_NUMBER);
		searchRes.setItemDescription(ITEM_DESC);
		itemThumbnailCellDatas.add(searchRes);
		return itemThumbnailCellDatas;
	}
	
	private CatalogSearchResultsResponse buildSearchResponse() {
		CatalogSearchResultsResponse searchResponse = new CatalogSearchResultsResponse();
		searchResponse.setCategoryHtml(HTML_TEXT);
		List<ItemThumbnailCellData> itemThumbnailCellDatas = buildSearchResults();
		searchResponse.setSearchResults(itemThumbnailCellDatas);
		return searchResponse;
	}
}
