/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/16/23	L De Leon			CAP-48457				Initial Version
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
import com.rrd.c1ux.api.models.checkout.DateToDestinationRequest;
import com.rrd.c1ux.api.models.checkout.DateToDestinationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class DateToDestinationControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private DateToDestinationRequest dateToDestinationRequest;
	private DateToDestinationResponse dateToDestinationResponseSuccess;
	private DateToDestinationResponse dateToDestinationResponseFailed;

	public static final String EXPECTED_422MESSAGE = "Generic Error";

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		dateToDestinationRequest = new DateToDestinationRequest();
		dateToDestinationResponseSuccess = getDateToDestinationResponseSuccessTest();
		dateToDestinationResponseFailed = getDateToDestinationResponseFailedTest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_doDateToDestination_returnsExpected() throws Exception {

		when(mockOrderheaderInfoService.doDateToDestination(any(SessionContainer.class),
				any(DateToDestinationRequest.class))).thenReturn(dateToDestinationResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(dateToDestinationRequest);

		// when doDateToDestination is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.DATE_TO_DESTINATION)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(requestString))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_doDateToDestination_returnsErrorMessage_whenError() throws Exception {

		when(mockOrderheaderInfoService.doDateToDestination(any(SessionContainer.class),
				any(DateToDestinationRequest.class))).thenReturn(dateToDestinationResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(dateToDestinationRequest);

		// when doDateToDestination is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.DATE_TO_DESTINATION)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(requestString))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}

	private DateToDestinationResponse getDateToDestinationResponseSuccessTest() {

		dateToDestinationResponseSuccess = new DateToDestinationResponse();
		dateToDestinationResponseSuccess.setSuccess(true);
		dateToDestinationResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return dateToDestinationResponseSuccess;
	}

	private DateToDestinationResponse getDateToDestinationResponseFailedTest() {

		dateToDestinationResponseFailed = new DateToDestinationResponse();
		dateToDestinationResponseFailed.setSuccess(false);
		dateToDestinationResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return dateToDestinationResponseFailed;
	}
}