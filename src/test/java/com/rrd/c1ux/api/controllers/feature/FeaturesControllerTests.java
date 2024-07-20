/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  09/20/23	N Caceres		CAP-42856					Initial version
 */
package com.rrd.c1ux.api.controllers.feature;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.favorite.FeaturedCatalogItemsRequest;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class FeaturesControllerTests extends BaseMvcTest {

	private static final String PRICE_LINE_AVAILABILITY_LBL = "$.priceLineAvailabilityLbl";
	private static final String UTF_8 = "utf-8";
	private static final String TTSESSION = "ttsession";
	private static final String YES = "Y";
	private static final String CHECK_MESSAGE = "Check Current Pricing and Availability";
	private static final String DESC = "Test1234";
	private static final String ITEM_NUMBER = "1234";
	private CatalogItemsResponse catalogItemsResponse = getCatalogItemsTest();
	private FeaturedCatalogItemsRequest request = new FeaturedCatalogItemsRequest();

	private static final String TEST_PRICE_AVAILABILITY = CHECK_MESSAGE;

	@BeforeEach
	void setUp() throws Exception {
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_retrieveFeaturedCatalogItemValues_returnsExpected() throws Exception, AtWinXSException {
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		buildRequest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mCatalogItemRetriveServices.getFeaturedCatalogItems(any(), any(), any(), 
				any(), any(), any())).thenReturn(catalogItemsResponse);
		when(mockCatalogItemsMapper.getCatalogItems(isA(CatalogItemsResponse.class))).thenReturn(catalogItemsResponse);

		mockMvc.perform(post(RouteConstants.GET_FEATURED_ITEMS).contentType(MediaType.APPLICATION_JSON)
				.header(TTSESSION, TEST_ENCRIPTED_SESSIONID).content(createContent())
				.characterEncoding(UTF_8).accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath(PRICE_LINE_AVAILABILITY_LBL).value(TEST_PRICE_AVAILABILITY));
	}

	private static CatalogItemsResponse getCatalogItemsTest() {
		List<ItemThumbnailCellData> itemThumbnailCellDatas = new ArrayList<ItemThumbnailCellData>();
		SearchResult searchRes = new SearchResult();
		searchRes.setItemNumber(ITEM_NUMBER);
		searchRes.setVendorItemNumber(ITEM_NUMBER);
		searchRes.setItemDescription(DESC);
		itemThumbnailCellDatas.add(searchRes);
		return new CatalogItemsResponse(itemThumbnailCellDatas, true, true, CHECK_MESSAGE, YES, AtWinXSConstant.EMPTY_STRING, null, false, null);
	}
	
	private void buildRequest() {
		List<String> featureTypes = new ArrayList<>();
		request.setFeaturedType(featureTypes);
	}
	
	private String createContent() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String retrieveAllFeatureItems = ow.writeValueAsString(request);
		return retrieveAllFeatureItems;
	}
}
