/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  10/03/23	Satishkumar A	CAP-43282	C1UX BE - API Build - Get OE Item Filter Options - including favorites, featured types
 */
package com.rrd.c1ux.api.controllers.standardattributes;

import static org.hamcrest.Matchers.emptyString;
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
import com.rrd.c1ux.api.models.standardattributes.StandardAttributesResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class StandardAttributesControllerTests extends BaseMvcTest {
	
	private static final String EXCEPTION_RESPONSE_MESSAGE = "$.message";
	private static final String EXCEPTION_RESPONSE_TITLE = "$.title";
	private static final boolean TEST_SUCCESS_TRUE = true;

	@BeforeEach
	void setUp() throws Exception {
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}
	
	@Test
	void that_StandardAttributes_returns_BAD_REQUEST() throws Exception {

		AtWinXSException e = new AtWinXSException("StandardAttributes error", String.class.getName());

		when(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).thenThrow(e);
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_ITEM_FILTERS).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("StandardAttributes")))
				.andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, emptyString()));
	}

	@Test
	void that_StandardAttributes_returns_422_expected() throws Exception {

		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockStandardAttributesService.getStandardAttributeList(any())).thenReturn(getStandardAttributes_422_Response());
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_ITEM_FILTERS).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}

	@Test
	void that_ToggleFavorite_returns_expected() throws Exception {

		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockStandardAttributesService.getStandardAttributeList(any())).thenReturn(getStandardAttributes_200_Response());
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_ITEM_FILTERS).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(TEST_SUCCESS_TRUE));

	}

	private StandardAttributesResponse getStandardAttributes_200_Response() {
		StandardAttributesResponse response = new StandardAttributesResponse();
		response.setSuccess(TEST_SUCCESS_TRUE);
		return response;
	}
	
	private StandardAttributesResponse getStandardAttributes_422_Response() {
		return new StandardAttributesResponse();
	}
}
