
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
 *  08/09/23 	Satishkumar A		CAP-42720		C1UX API - API Build - Favorite Toggle Call
 */
package com.rrd.c1ux.api.controllers.favorite;

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
import com.rrd.c1ux.api.models.favorite.ToggleFavoriteResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class ToggleFavoriteControllerTests extends BaseMvcTest {

	private static final String EXCEPTION_RESPONSE_MESSAGE = "$.message";
	private static final String EXCEPTION_RESPONSE_TITLE = "$.title";
	private static final boolean TEST_SUCCESS_TRUE = true;

	@BeforeEach
	void setUp() throws Exception {
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_ToggleFavorite_returns_BAD_REQUEST() throws Exception {

		AtWinXSException e = new AtWinXSException("toggleFavorite error", String.class.getName());

		when(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).thenThrow(e);
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.FAVORITE_TOGGLE_CALL).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
						.content("{ \"customerItemNumber\": \"10116020\",  \"vendorItemNumber\": \"JL10116020\"}\n"))
				.andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("toggleFavorite")))
				.andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, emptyString()));
	}

	@Test
	void that_ToggleFavorite_returns_422_expected() throws Exception {

		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockToggleFavoriteService.toggleFavorite(any(), any(), any())).thenReturn(getToggleFavorite422Response());
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.FAVORITE_TOGGLE_CALL).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
						.content("{ \"customerItemNumber\": \"10116020\",  \"vendorItemNumber\": \"JL10116020\"}\n"))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}

	@Test
	void that_ToggleFavorite_returns_expected() throws Exception {

		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockToggleFavoriteService.toggleFavorite(any(), any(), any())).thenReturn(getToggleFavoriteResponse());
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.FAVORITE_TOGGLE_CALL).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
						.content("{ \"customerItemNumber\": \"10116020\",  \"vendorItemNumber\": \"JL10116020\"}\n"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(TEST_SUCCESS_TRUE))
				.andExpect(MockMvcResultMatchers.jsonPath("$.isFavorite").value(TEST_SUCCESS_TRUE));
	}

	private ToggleFavoriteResponse getToggleFavorite422Response() {
		return new ToggleFavoriteResponse(true);
	}

	private ToggleFavoriteResponse getToggleFavoriteResponse() {
		ToggleFavoriteResponse response = new ToggleFavoriteResponse(true);
		response.setSuccess(true);
		return response;
	}

}
