/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/01/23				L De Leon				CAP-42519					Initial Version
 */
package com.rrd.c1ux.api.controllers.orders;

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
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class CopyRecentOrderControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private CopyRecentOrderRequest copyRecentOrderRequest;
	private CopyRecentOrderResponse copyRecentOrderResponseSuccess;
	private CopyRecentOrderResponse copyRecentOrderResponseFailed;

	private static final int TEST_ORDER_ID = 604534;

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		copyRecentOrderRequest = new CopyRecentOrderRequest(TEST_ORDER_ID);
		copyRecentOrderResponseSuccess = getCopyRecentOrderResponseSuccessTest();
		copyRecentOrderResponseFailed = getCopyRecentOrderResponseFailedTest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_copyRecentOrder_returnsExpected() throws Exception {

		// when copyRecentOrder returns a valid CopyRecentOrderResponse object
		when(mockCopyRecentOrderService.copyRecentOrder(any(SessionContainer.class), any(CopyRecentOrderRequest.class)))
				.thenReturn(copyRecentOrderResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(copyRecentOrderRequest);

		// when copyRecentOrder is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.COPY_RECENT_ORDER).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_copyRecentOrder_returnsErrorMessage_whenError() throws Exception {

		// when copyRecentOrder returns a valid CopyRecentOrderResponse object
		when(mockCopyRecentOrderService.copyRecentOrder(any(SessionContainer.class), any(CopyRecentOrderRequest.class)))
				.thenReturn(copyRecentOrderResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(copyRecentOrderRequest);

		// when copyRecentOrder is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.COPY_RECENT_ORDER).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG));
	}

	private CopyRecentOrderResponse getCopyRecentOrderResponseSuccessTest() {

		copyRecentOrderResponseSuccess = new CopyRecentOrderResponse();
		copyRecentOrderResponseSuccess.setSuccess(true);
		copyRecentOrderResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return copyRecentOrderResponseSuccess;
	}

	private CopyRecentOrderResponse getCopyRecentOrderResponseFailedTest() {

		copyRecentOrderResponseFailed = new CopyRecentOrderResponse();
		copyRecentOrderResponseFailed.setSuccess(false);
		copyRecentOrderResponseFailed.setMessage(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG);
		return copyRecentOrderResponseFailed;
	}
}
