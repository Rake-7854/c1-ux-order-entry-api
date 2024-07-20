/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 * 	05/23/23	A Boomker		CAP-40687					Initial
 */
package com.rrd.c1ux.api.controllers.checkout;

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
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveRequest;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class OrderHeaderInfoControllerTests extends BaseMvcTest{

    private OrderInfoHeaderSaveRequest saveRequest;
    private OrderInfoHeaderSaveResponse saveResponse;
    private static final String GENERIC_ERROR_MSG = "Failure message generic";
    String TEST_ENCRYPTED_SESSIONID;
	@BeforeEach
	public void setUp() throws Exception {
	    saveRequest = new OrderInfoHeaderSaveRequest();
	    setupBaseMockSessions();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_saveOrderHeaderInfo_returnsExpected() throws Exception, AtWinXSException {
//		setUp();
		when(mockOrderheaderInfoService.saveOrderHeaderInfo(any(SessionContainer.class), any(OrderInfoHeaderSaveRequest.class)))
				.thenReturn(getSaveResponseSuccessTest());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestString = ow.writeValueAsString(saveRequest);

		// when getDetail is called, expect 200 status and item numbers in JSON
		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.ORDER_HEADER_INFO_SAVE)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString)
				.characterEncoding("utf-8")
				)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
			    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}

	@Test
	void that_saveOrderHeaderInfo_returns422_whenError() throws Exception, AtWinXSException {
//		setUp();
		when(mockOrderheaderInfoService.saveOrderHeaderInfo(any(SessionContainer.class), any(OrderInfoHeaderSaveRequest.class)))
			.thenReturn(getSaveResponseFailedTest());
	       ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	      String requestString = ow.writeValueAsString(saveRequest);

		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.ORDER_HEADER_INFO_SAVE)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString)
				.characterEncoding("utf-8")				)
					.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MSG));
	}

	private OrderInfoHeaderSaveResponse getSaveResponseSuccessTest() {
		saveResponse = new OrderInfoHeaderSaveResponse();
		saveResponse.setSuccess(true);
		return saveResponse;
	}

	private OrderInfoHeaderSaveResponse getSaveResponseFailedTest() {
		saveResponse = new OrderInfoHeaderSaveResponse();
		saveResponse.setSuccess(false);
		saveResponse.setMessage(GENERIC_ERROR_MSG);
		return saveResponse;
	}
}

