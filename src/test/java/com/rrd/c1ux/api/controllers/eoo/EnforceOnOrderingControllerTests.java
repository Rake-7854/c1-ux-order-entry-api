/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  02/01/24 	Satishkumar A		CAP-46675		C1UX BE - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 */
package com.rrd.c1ux.api.controllers.eoo;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.eoo.ValidateCheckoutResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class EnforceOnOrderingControllerTests extends BaseMvcTest {

	private static final String EXCEPTION_RESPONSE_MESSAGE = "$.title";

	@BeforeEach
	void setUp() throws Exception {
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_validateEOO_returns_BAD_REQUEST() throws Exception {

		AtWinXSException e = new AtWinXSException("validateEOO error", String.class.getName());

		when(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).thenThrow(e);
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_VALIDATE_EOO_CHECKOUT).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, startsWith("validateEOO")));

	}
	
	@Test
	void that_validateEOO_returns_Success() throws Exception {

		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockEnforceOnOrderingService.validateCheckout(any(), any())).thenReturn(getValidateCheckoutResponse());

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_VALIDATE_EOO_CHECKOUT).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

	}
	
	private ValidateCheckoutResponse getValidateCheckoutResponse() {
		ValidateCheckoutResponse response = new ValidateCheckoutResponse();
		response.setSuccess(true);
		return response;
	}

}
