/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/05/23	L De Leon			CAP-45653				Initial Version
 *	12/22/23 	Satishkumar A		CAP-45709				C1UX BE - Set OOB Mode for CustomPoint session
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
import com.rrd.c1ux.api.models.orders.oob.OOBRequest;
import com.rrd.c1ux.api.models.orders.oob.OOBResponse;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchRequest;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class OrderOnBehalfControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private OrderOnBehalfSearchRequest orderOnBehalfSearchRequest;
	private OrderOnBehalfSearchResponse orderOnBehalfSearchResponseSuccess;
	private OrderOnBehalfSearchResponse orderOnBehalfSearchResponseFailed;
	
	//CAP-45709
	private OOBRequest setOOBOrSelfRequest;
	private OOBResponse setOOBOrSelfResponseSuccess;
	private OOBResponse setOOBOrSelfResponseFailed;

	public static final String EXPECTED_422MESSAGE = "Generic Error";


	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		orderOnBehalfSearchRequest = new OrderOnBehalfSearchRequest();
		orderOnBehalfSearchResponseSuccess = getOrderOnBehalfSearchResponseSuccessTest();
		orderOnBehalfSearchResponseFailed = getOrderOnBehalfSearchResponseFailedTest();

		//CAP-45709
		setOOBOrSelfRequest = new OOBRequest();
		setOOBOrSelfResponseSuccess = setOrderForSelfOrOOBModeResponseSuccessTest();
		setOOBOrSelfResponseFailed = setOrderForSelfOrOOBModeResponseFailedTest();
		
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_getDeliveryInformation_returnsExpected() throws Exception {

		// when orderOnBehalfSearch returns a valid OrderOnBehalfSearchResponse object
		when(mockOrderOnBehalfService.getOOBInfo(any(SessionContainer.class), any(OrderOnBehalfSearchRequest.class)))
				.thenReturn(orderOnBehalfSearchResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(orderOnBehalfSearchRequest);

		// when orderOnBehalfSearch is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_OOB_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_getDeliveryInformation_returnsErrorMessage_whenError() throws Exception {

		// when orderOnBehalfSearch returns a valid OrderOnBehalfSearchResponse object
		when(mockOrderOnBehalfService.getOOBInfo(any(SessionContainer.class), any(OrderOnBehalfSearchRequest.class)))
				.thenReturn(orderOnBehalfSearchResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(orderOnBehalfSearchRequest);

		// when orderOnBehalfSearch is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_OOB_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}

	//CAP-45709
	@Test
	void that_getOOBToggleCall_returnsExpected() throws Exception {

		// when orderOnBehalfSearch returns a valid OrderOnBehalfSearchResponse object
		when(mockOrderOnBehalfService.setOrderForSelfOrOOBMode(any(SessionContainer.class), any(OOBRequest.class)))
				.thenReturn(setOOBOrSelfResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(setOOBOrSelfRequest);

		// when orderOnBehalfSearch is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.OOB_TOGGLE_CALL).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-45709
	@Test
	void that_getOOBToggleCall_returnsErrorMessage_whenError() throws Exception {

		// when orderOnBehalfSearch returns a valid OrderOnBehalfSearchResponse object
		when(mockOrderOnBehalfService.setOrderForSelfOrOOBMode(any(SessionContainer.class), any(OOBRequest.class)))
				.thenReturn(setOOBOrSelfResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(setOOBOrSelfRequest);

		// when orderOnBehalfSearch is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.OOB_TOGGLE_CALL).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	
	private OrderOnBehalfSearchResponse getOrderOnBehalfSearchResponseSuccessTest() {

		orderOnBehalfSearchResponseSuccess = new OrderOnBehalfSearchResponse();
		orderOnBehalfSearchResponseSuccess.setSuccess(true);
		orderOnBehalfSearchResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return orderOnBehalfSearchResponseSuccess;
	}

	private OrderOnBehalfSearchResponse getOrderOnBehalfSearchResponseFailedTest() {

		orderOnBehalfSearchResponseFailed = new OrderOnBehalfSearchResponse();
		orderOnBehalfSearchResponseFailed.setSuccess(false);
		orderOnBehalfSearchResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return orderOnBehalfSearchResponseFailed;
	}
	//CAP-45709
	private OOBResponse setOrderForSelfOrOOBModeResponseSuccessTest() {

		setOOBOrSelfResponseSuccess = new OOBResponse();
		setOOBOrSelfResponseSuccess.setSuccess(true);
		setOOBOrSelfResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return setOOBOrSelfResponseSuccess;
	}
	//CAP-45709
	private OOBResponse setOrderForSelfOrOOBModeResponseFailedTest() {

		setOOBOrSelfResponseFailed = new OOBResponse();
		setOOBOrSelfResponseFailed.setSuccess(false);
		setOOBOrSelfResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return setOOBOrSelfResponseFailed;
	}

}