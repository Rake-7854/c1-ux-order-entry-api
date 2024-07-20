/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	07/11/23	Satishkumar A      CAP-41970		C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 */
package com.rrd.c1ux.api.controllers.util;

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
import com.rrd.c1ux.api.models.util.CountriesAndStatesResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class CountriesAndStatesControllerTests extends BaseMvcTest {

	@BeforeEach
	void setUp() throws Exception {

		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_getCountriesAndStatesList_returns_UNPROCESSABLE_ENTITY() throws Exception, AtWinXSException {

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockCountriesAndStatesService.getCountriesAndStatesOrProvincesList(mockSessionContainer))
				.thenReturn(getUnprocessableResponse());

		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.COUNTRY_STATE_LIST)
				.contentType(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

	}

	@Test
	void that_getCountriesAndStatesList_returns_expected() throws Exception, AtWinXSException {

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockCountriesAndStatesService.getCountriesAndStatesOrProvincesList(mockSessionContainer))
				.thenReturn(getSuccessResponse());

		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.COUNTRY_STATE_LIST)
				.contentType(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

	}

	private static CountriesAndStatesResponse getSuccessResponse() {
		CountriesAndStatesResponse response = new CountriesAndStatesResponse();
		response.setSuccess(true);
		return response;
	}

	private static CountriesAndStatesResponse getUnprocessableResponse() {
		CountriesAndStatesResponse response = new CountriesAndStatesResponse();
		response.setSuccess(false);
		return response;
	}
}
