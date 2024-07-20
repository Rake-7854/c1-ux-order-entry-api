/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  03/14/23   C Porter        CAP-37146                   Spring Session
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.controllers.notiereditems;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.notiereditems.PNANoTieredPriceRequest;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

@WithMockUser
class RetrievePNANoTieredRestControllerTests extends BaseMvcTest {


	private static final double TEST_ITEM_SELL_PRICE = 0.5084;
	private static final String TEST_PRICE_TYPE = "VC";

	private PriceAndAvailabilityVO mPriceAndAvailabilityVO = getPriceAndAvailability();

	@BeforeEach
	void setUp() throws Exception {

		when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);// mandy
		doReturn("").when(mockAppSessionBean). getEncodedSessionId();
	}

	@Test
	void that_PNANoTieredServices_returnsExpected() throws Exception, AtWinXSException {

		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockPNANoTieredServices.getPNANoTieredItemPricing(any(ApplicationSession.class),
				any(ApplicationVolatileSession.class), any(OrderEntrySession.class),
				any(PNANoTieredPriceRequest.class))).thenReturn(mPriceAndAvailabilityVO);
		when(mockNoTieredItemMapper.getNoTieredItemPricing(mPriceAndAvailabilityVO)).thenReturn(mPriceAndAvailabilityVO);

		// when getDetail is called, expect 200 status and itemSellPrice, priceType in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.NO_TIERED_ITEM_PRICING)
				.contentType(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
				.content(
						"{ \"showPrice\": \"true\",  \"showAvailability\": \"true\",  \"itemNumber\": \"157869\",  \"orderType\": \"ED\",  \"orderQtyEA\": \"2\",  \"corporateNumber\": \"0000097000\",  \"soldToNumber\": \"\",  \"checkJobs\": \"true\",  \"useCSSListPrice\": \"false\",  \"useJLJLSPrice\": \"true\",  \"useCustomersCustomerPrice\": \"false\",  \"useCatalogPrice\": \"true\",  \"siteID\": \"4366\",  \"customerItemNumber\": \"157869\",  \"lastJLMarkupPct\": \"0.0\",  \"promoCode\": \"\",  \"rounding\": \"2\",  \"useTPP\": \"false\",  \"tppClass\": \"\",  \"component\": \"false\"}\n")
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.itemSellPrice").value(TEST_ITEM_SELL_PRICE))
				.andExpect(MockMvcResultMatchers.jsonPath("$.priceType").value(TEST_PRICE_TYPE));

	}

	private static PriceAndAvailabilityVO getPriceAndAvailability() {

		return new PriceAndAvailabilityVO(0.5084, 1.02, -1, -1, "", 0, "VC", "", "", "", "", "", false, true, false,
				"C", "157869", 2, "", null);
	}

}
