/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/27/24	L De Leon			CAP-49609				Initial Version
 *	05/31/24	Satishkumar A		CAP-49731				C1UX BE - Create API to login as a linked login ID/user
 */
package com.rrd.c1ux.api.controllers.admin;

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
import com.rrd.c1ux.api.models.admin.LinkedLoginResponse;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserRequest;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class LinkedLoginControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private LinkedLoginResponse linkedLoginResponseSuccess;
	private LinkedLoginResponse linkedLoginResponseFailed;
	
	//CAP-49731
	private LoginLinkedUserResponse loginLinkedUserResponseSuccess;
	private LoginLinkedUserResponse loginLinkedUserResponseFailed;
	private LoginLinkedUserRequest loginLinkedUserRequest;
	private static final String TEST_USER = "TEST_USER";

	public static final String EXPECTED_422MESSAGE = "Generic Error";

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		linkedLoginResponseSuccess = getLinkedLoginResponseSuccessTest();
		linkedLoginResponseFailed = getLinkedLoginResponseFailedTest();
		
		//CAP-49731
		loginLinkedUserResponseSuccess = getLoginLinkedUserResponseSuccess();
		loginLinkedUserResponseFailed = getLoginLinkedUserResponseFailed();
		loginLinkedUserRequest = getLoginLinkedUserRequest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_getLinkedLogins_returnsExpected() throws Exception {

		// when getLinkedLogins returns a success LinkedLoginResponse object
		when(mockLinkedLoginService.getLinkedLogins(any(SessionContainer.class)))
				.thenReturn(linkedLoginResponseSuccess);

		// when getLinkedLogins is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_LINKED_LOGINS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_getLinkedLogins_returnsErrorMessage_whenError() throws Exception {

		// when getLinkedLogins returns a failed LinkedLoginResponse object
		when(mockLinkedLoginService.getLinkedLogins(any(SessionContainer.class)))
				.thenReturn(linkedLoginResponseFailed);

		// when getLinkedLogins is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_LINKED_LOGINS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	
	//CAP-49731
	@Test
	void that_loginLinkedUser_returnsExpected() throws Exception {

		// when getLinkedLogins returns a success LinkedLoginResponse object
		when(mockLinkedLoginService.loginLinkedUser(any(SessionContainer.class), any()))
				.thenReturn(loginLinkedUserResponseSuccess);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(loginLinkedUserRequest);

		// when loginLinkedUser is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.LOGIN_LINKED_USER)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-49731
	@Test
	void that_loginLinkedUser_returns422() throws Exception {

		// when getLinkedLogins returns a failed LinkedLoginResponse object
		when(mockLinkedLoginService.loginLinkedUser(any(SessionContainer.class), any()))
				.thenReturn(loginLinkedUserResponseFailed);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(loginLinkedUserRequest);

		// when loginLinkedUser is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.LOGIN_LINKED_USER)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}

	private LinkedLoginResponse getLinkedLoginResponseSuccessTest() {

		linkedLoginResponseSuccess = new LinkedLoginResponse();
		linkedLoginResponseSuccess.setSuccess(true);
		linkedLoginResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return linkedLoginResponseSuccess;
	}

	private LinkedLoginResponse getLinkedLoginResponseFailedTest() {

		linkedLoginResponseFailed = new LinkedLoginResponse();
		linkedLoginResponseFailed.setSuccess(false);
		linkedLoginResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return linkedLoginResponseFailed;
	}
	
	//CAP-49731
	private LoginLinkedUserResponse getLoginLinkedUserResponseSuccess() {
		
		loginLinkedUserResponseSuccess = new LoginLinkedUserResponse();
		loginLinkedUserResponseSuccess.setSuccess(true);
		loginLinkedUserResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return loginLinkedUserResponseSuccess;
	}
	//CAP-49731
	private LoginLinkedUserResponse getLoginLinkedUserResponseFailed() {
		
		loginLinkedUserResponseFailed = new LoginLinkedUserResponse();
		loginLinkedUserResponseFailed.setSuccess(false);
		loginLinkedUserResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return loginLinkedUserResponseFailed;
	}
	//CAP-49731
	private LoginLinkedUserRequest getLoginLinkedUserRequest() {
		LoginLinkedUserRequest request = new LoginLinkedUserRequest();
		request.setLoginLinkedUserID(TEST_USER);
		return request;
	}
}