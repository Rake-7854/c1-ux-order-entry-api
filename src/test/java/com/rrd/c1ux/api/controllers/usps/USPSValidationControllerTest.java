/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			JIRA #			Description
 *	--------	-----------			----------		-----------------------------------------------------------
 *	12/22/23	S Ramachandran		CAP-46081		Initial Version. Added test for USPS validation Controller
 */

package com.rrd.c1ux.api.controllers.usps;

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
import com.rrd.c1ux.api.models.usps.USPSValidationRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class USPSValidationControllerTest extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	
	private USPSValidationRequest uspsValidationResponseRequest;
	private USPSValidationResponse uspsValidationResponseSuccess;
	private USPSValidationResponse uspsValidationResponseFailed;
	private static final String TEST_USPSVALIDATION_FAILED = "failed" ;
	
	@BeforeEach
	void setUp() throws Exception {
 
		setupBaseMockSessions();

		//testAddressIDs.add(TEST_ADDRESS_ID);
		uspsValidationResponseRequest = getUSPSValidationRequest();
		uspsValidationResponseSuccess = getUSPSValidationResponseSuccessTest();
		uspsValidationResponseFailed = getUSPSValidationResponseFailedTest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}
	
	@Test
	void that_validateUSAddress_returnsExpected() throws Exception {
		
		// when validateUSPS returns SUCCESS
		when(mockUSPSValidationService.validateUSPS(any(SessionContainer.class), any(USPSValidationRequest.class)))
				.thenReturn(uspsValidationResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(uspsValidationResponseRequest);
		
		// when validateUSPS is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_US_ADDRESS).accept(MediaType.APPLICATION_JSON)
			.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
			.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
			.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	@Test
	void that_searchPABAddress_returnsErrorMessage_whenError() throws Exception {

		// when validateUSPS returns FAIL
		when(mockUSPSValidationService.validateUSPS(any(SessionContainer.class), any(USPSValidationRequest.class)))
			.thenReturn(uspsValidationResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(uspsValidationResponseRequest);
		
		// when validateUSPS is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_US_ADDRESS).accept(MediaType.APPLICATION_JSON)
			.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
			.content(requestString).characterEncoding("utf-8"))
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
			.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(TEST_USPSVALIDATION_FAILED));
	}
	
	private USPSValidationResponse getUSPSValidationResponseSuccessTest() {

		uspsValidationResponseSuccess = new USPSValidationResponse();
		uspsValidationResponseSuccess.setSuccess(true);
		uspsValidationResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return uspsValidationResponseSuccess;
	}
	
	private USPSValidationResponse getUSPSValidationResponseFailedTest() {

		uspsValidationResponseFailed = new USPSValidationResponse();
		uspsValidationResponseFailed.setSuccess(false);
		uspsValidationResponseFailed.setMessage(TEST_USPSVALIDATION_FAILED);
		return uspsValidationResponseFailed;
	}
	
	private USPSValidationRequest getUSPSValidationRequest() {
		
		USPSValidationRequest uspsValidationResponseRequest = new USPSValidationRequest();
		return uspsValidationResponseRequest;
	}
}