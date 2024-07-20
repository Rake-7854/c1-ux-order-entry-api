/*
 *	Copyright (c) RR Donnelley. All Rights Reserved.
 *	This software is the confidential and proprietary information of RR Donnelley.
 *	You shall not disclose such confidential information.
 *	
 *	Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  10/16/23    S Ramachandran	CAP-44515					Retrieve Order Approval Alerts -Juits
 *  10/31/23	Satishkumar A	CAP-44996					C1UX BE - Create service to show if there are any alerts for the logged in user  
 */

package com.rrd.c1ux.api.controllers.alert;

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
import com.rrd.c1ux.api.models.alert.AlertsResponse;
import com.rrd.c1ux.api.models.alert.CheckAlertsResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class AlertsControllerTest  extends BaseMvcTest {

	private AlertsResponse alertsResponseSuccess = getAlertsResponseSuccessTest();
	private AlertsResponse alertsResponseFailed = getAlertsResponseFailedTest();

	private static final String GENERIC_ERROR_MSG = "Failure message generic";

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);

		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockAppSessionBean).getEncodedSessionId();
	}

	//CAP-44515:Starts
	@Test
	void that_getAlerts_returnsExpected() throws Exception, AtWinXSException {

		// given that getAlerts returns a valid AlertsResponse object
		when(mockAlertsService.getAlerts(any(SessionContainer.class))).thenReturn(alertsResponseSuccess);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when getAlerts is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_ALERTS).contentType(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	@Test
	void that_getAlerts_returns422_whenError() throws Exception, AtWinXSException {

		// given that getAlerts returns a valid AlertsResponse object
		when(mockAlertsService.getAlerts(any(SessionContainer.class))).thenReturn(alertsResponseFailed);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when getAlerts is called, expect 422 status and error message
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_ALERTS).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
						.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(GENERIC_ERROR_MSG));
	}
	
	private AlertsResponse getAlertsResponseSuccessTest() {

		AlertsResponse alertsResponseSuccess = new AlertsResponse();
		alertsResponseSuccess.setSuccess(true);
		alertsResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return alertsResponseSuccess;
	}
	
	private AlertsResponse getAlertsResponseFailedTest() {

		AlertsResponse alertsResponse = new AlertsResponse();
		alertsResponse.setSuccess(false);
		alertsResponse.setMessage(GENERIC_ERROR_MSG);
		return alertsResponse;
	}
	//CAP-44515:Ends
	
	//CAP-44996:Starts
	@Test
	void that_checkAlerts_returnsExpected() throws Exception, AtWinXSException {

		// given that checkAlerts returns a valid CheckAlertsResponse object
		when(mockAlertsService.checkAlerts(any(SessionContainer.class))).thenReturn(checkAlertsResponseSuccessTest());

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when checkAlerts is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.CHECK_ALERTS).contentType(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	@Test
	void that_checkAlerts_returns422_whenError() throws Exception, AtWinXSException {

		// given that checkAlerts returns a valid CheckAlertsResponse object
		when(mockAlertsService.checkAlerts(any(SessionContainer.class))).thenReturn(checkAlertsResponseFailedTest());

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when checkAlerts is called, expect 422 status and error message
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.CHECK_ALERTS).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
						.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(GENERIC_ERROR_MSG));
	}
	
	private CheckAlertsResponse checkAlertsResponseSuccessTest() {

		CheckAlertsResponse alertsResponseSuccess = new CheckAlertsResponse();
		alertsResponseSuccess.setSuccess(true);
		alertsResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return alertsResponseSuccess;
	}
	
	private CheckAlertsResponse checkAlertsResponseFailedTest() {

		CheckAlertsResponse alertsResponse = new CheckAlertsResponse();
		alertsResponse.setSuccess(false);
		alertsResponse.setMessage(GENERIC_ERROR_MSG);
		return alertsResponse;
	}
	//CAP-44996:End
}