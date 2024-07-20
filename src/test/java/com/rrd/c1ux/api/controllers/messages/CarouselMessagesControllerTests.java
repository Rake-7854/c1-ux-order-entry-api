/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/03/23				C Codina				CAP-44742					Initial Version
*/
package com.rrd.c1ux.api.controllers.messages;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.messages.CarouselResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class CarouselMessagesControllerTests extends BaseMvcTest {
	
	String TEST_ENCRYPTED_SESSIONID;
	private CarouselResponse carouselResponseSuccess;
	private CarouselResponse carouselResponseFailed;
	
	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();
		carouselResponseSuccess = getCarouselResponseSuccessTest();
		carouselResponseFailed = getCarouselResponseFailedTest();
		
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		
	}
	
	@Test
	void that_getCarouselMessages_returnsExpected() throws Exception {
		when(mockCarouselMessageService.getCarouselMessages(any(SessionContainer.class))).thenReturn(carouselResponseSuccess);
		
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_CAROUSEL_MESSAGES).contentType(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
		.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
				
	}
	@Test
	void that_getCarouselMessages_returnsErrorMessage_whenError() throws Exception {
		when(mockCarouselMessageService.getCarouselMessages(any(SessionContainer.class))).thenReturn(carouselResponseFailed);
		
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_CAROUSEL_MESSAGES).contentType(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
		.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SFTranslationTextConstants.CAROUSEL_MESSAGE_ERR_MSG));
				
	}	
	private CarouselResponse getCarouselResponseSuccessTest() {
		carouselResponseSuccess = new CarouselResponse();
		carouselResponseSuccess.setSuccess(true);
		carouselResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return carouselResponseSuccess;
		
	}
	private CarouselResponse getCarouselResponseFailedTest() {
		carouselResponseFailed = new CarouselResponse();
		carouselResponseFailed.setSuccess(false);
		carouselResponseFailed.setMessage(SFTranslationTextConstants.CAROUSEL_MESSAGE_ERR_MSG);
		return carouselResponseFailed;
	}
}

	


