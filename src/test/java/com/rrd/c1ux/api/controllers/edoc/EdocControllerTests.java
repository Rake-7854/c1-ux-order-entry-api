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
 *  10/17/23	Satishkumar A	CAP-44664	C1UX BE - Create api to retrieve edoc for Storefront
 */
package com.rrd.c1ux.api.controllers.edoc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.edoc.EdocUrlResponse;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class EdocControllerTests extends BaseMvcTest {
	
	private EdocUrlResponse edocUrlResponseSuccess = getEdocUrlResponseSuccessTest();
	private EdocUrlResponse edocUrlResponseFailed = getEdocUrlResponseFailedTest();


	@BeforeEach
	void setUp() throws Exception {
		setupBaseMockSessions();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);

		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockAppSessionBean).getEncodedSessionId();

	}
	
	@Test
	void that_getEdocUrl_returnsExpected() throws Exception, AtWinXSException {
		
		// given that getEdocUrl returns a valid EdocUrlResponse object
		when(mockEdocService.getEdocUrl(any(AppSessionBean.class), any(), any()))
				.thenReturn(edocUrlResponseSuccess);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when getEdocUrl is called, expect 200 status 
        mockMvc.perform(
                MockMvcRequestBuilders.get(RouteConstants.GET_EDOC_URL)
                .accept(MediaType.APPLICATION_JSON)
                .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
                .contentType(MediaType.APPLICATION_JSON)
                .param("a", "xsdfDfsdSdef")
                )
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	@Test
	void that_getEdocUrl_returns422() throws Exception, AtWinXSException {
		
		// given that getEdocUrl returns a valid EdocUrlResponse object
		when(mockEdocService.getEdocUrl(any(AppSessionBean.class), any(), any()))
				.thenReturn(edocUrlResponseFailed);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when getEdocUrl is called, expect 422 status 
        mockMvc.perform(
                MockMvcRequestBuilders.get(RouteConstants.GET_EDOC_URL)
                .accept(MediaType.APPLICATION_JSON)
                .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
                .contentType(MediaType.APPLICATION_JSON)
                .param("a", "xsdfDfsdSdef")
                )
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	private EdocUrlResponse getEdocUrlResponseSuccessTest() {
		edocUrlResponseSuccess = new EdocUrlResponse();
		edocUrlResponseSuccess.setSuccess(true);
		edocUrlResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return edocUrlResponseSuccess;
		
	}
	private EdocUrlResponse getEdocUrlResponseFailedTest() {
		edocUrlResponseFailed = new EdocUrlResponse();
		edocUrlResponseFailed.setSuccess(false);
		edocUrlResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return edocUrlResponseFailed;
	}
}
