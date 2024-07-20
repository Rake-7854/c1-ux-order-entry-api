/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By			JIRA#		Description
 * 	--------	----------------	---------	--------------------------------------------------
 *	06/03/24	C Codina			CAP-38842	Initial Version
 */
package com.rrd.c1ux.api.controllers.items;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.PNATieredPriceRequest;
import com.rrd.c1ux.api.models.items.PNATieredPriceResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class PNATieredRestControllerTests extends BaseMvcTest{
	
	String TEST_ENCRYPTED_SESSIONID;
	private PNATieredPriceResponse pnaTieredPriceSuccess;
	private PNATieredPriceResponse pnaTieredPriceFailed;
	private PNATieredPriceRequest pnaTieredPriceRequest;
	
	@BeforeEach
	void setup() throws Exception {
		setupBaseMockSessions();
		pnaTieredPriceSuccess = getPNATieredPriceResponseSuccessTest();
		pnaTieredPriceFailed = getPNATieredPriceResponseFailedTest();
		pnaTieredPriceRequest = getPNATieredPriceRequest();
		
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		
	}
	
	@Test
	void that_test_process_pna_tier_price_returnsExpected() throws Exception {
		when(mockPNATieredProcessor.processPNATierPrice(any(PNATieredPriceRequest.class), any(SessionContainer.class)))
				.thenReturn(pnaTieredPriceSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(pnaTieredPriceRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_PNATIERED)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestString)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
		
	}
	
	@Test
	void that_test_process_pna_tier_price_failed() throws Exception {
		when(mockPNATieredProcessor.processPNATierPrice(any(PNATieredPriceRequest.class), any(SessionContainer.class)))
				.thenReturn(pnaTieredPriceFailed);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(pnaTieredPriceRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_PNATIERED)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestString)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		
	}
	
	private PNATieredPriceResponse getPNATieredPriceResponseSuccessTest() {
		pnaTieredPriceSuccess = new PNATieredPriceResponse();
		pnaTieredPriceSuccess.setSuccess(true);
		pnaTieredPriceSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return pnaTieredPriceSuccess;
		
	}
	private PNATieredPriceResponse getPNATieredPriceResponseFailedTest() {
		pnaTieredPriceFailed = new PNATieredPriceResponse();
		pnaTieredPriceFailed.setSuccess(false);
		pnaTieredPriceFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return pnaTieredPriceFailed;
	}
	private PNATieredPriceRequest getPNATieredPriceRequest() {
		PNATieredPriceRequest tieredPriceRequest = new PNATieredPriceRequest();
		tieredPriceRequest.setCorpNum("TestCorporateNum");
		tieredPriceRequest.setOrderType("TestOrderType");
		tieredPriceRequest.setShowPrice(true);
		
		return tieredPriceRequest;
	}

}
