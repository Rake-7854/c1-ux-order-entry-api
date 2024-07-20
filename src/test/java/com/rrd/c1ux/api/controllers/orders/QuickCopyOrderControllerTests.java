/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	06/28/23				L De Leon				CAP-41373					Initial Version
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
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class QuickCopyOrderControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private QuickCopyOrderRequest quickCopyOrderRequest;
	private QuickCopyOrderResponse quickCopyOrderResponseSuccess;
	private QuickCopyOrderResponse quickCopyOrderResponseFailed;

	private static final int TEST_ORDER_ID = 604534;

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		quickCopyOrderRequest = new QuickCopyOrderRequest(TEST_ORDER_ID);
		quickCopyOrderResponseSuccess = getQuickCopyOrderResponseSuccessTest();
		quickCopyOrderResponseFailed = getQuickCopyOrderResponseFailedTest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_quickCopyOrder_returnsExpected() throws Exception {

		// when quickCopyOrder returns a valid QuickCopyOrderResponse object
		when(mockQuickCopyOrderService.quickCopyOrder(any(SessionContainer.class), any(QuickCopyOrderRequest.class)))
				.thenReturn(quickCopyOrderResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(quickCopyOrderRequest);

		// when quickCopyOrder is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.QUICK_COPY_ORDER).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_quickCopyOrder_returnsErrorMessage_whenError() throws Exception {

		// when quickCopyOrder returns a valid QuickCopyOrderResponse object
		when(mockQuickCopyOrderService.quickCopyOrder(any(SessionContainer.class), any(QuickCopyOrderRequest.class)))
				.thenReturn(quickCopyOrderResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(quickCopyOrderRequest);

		// when quickCopyOrder is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.QUICK_COPY_ORDER).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG));
	}

	private QuickCopyOrderResponse getQuickCopyOrderResponseSuccessTest() {

		quickCopyOrderResponseSuccess = new QuickCopyOrderResponse();
		quickCopyOrderResponseSuccess.setSuccess(true);
		quickCopyOrderResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return quickCopyOrderResponseSuccess;
	}

	private QuickCopyOrderResponse getQuickCopyOrderResponseFailedTest() {

		quickCopyOrderResponseFailed = new QuickCopyOrderResponse();
		quickCopyOrderResponseFailed.setSuccess(false);
		quickCopyOrderResponseFailed.setMessage(SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG);
		return quickCopyOrderResponseFailed;
	}
}
