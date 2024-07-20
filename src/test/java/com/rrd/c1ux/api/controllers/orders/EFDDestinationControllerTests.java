/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/13/24	S Ramachandran		CAP-49326				Added junit tests for getEfdStyleInformation handler
 *	05/16/24	N Caceres			CAP-49344				Added test methods for Get EFD Options API
 *	05/21/24	Satishkumar A		CAP-49453				C1UX BE - Create new API to save EFD information
 */

package com.rrd.c1ux.api.controllers.orders;

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
import com.rrd.c1ux.api.models.checkout.EFDOptionsResponse;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationRequest;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationResponse;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationRequest;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class EFDDestinationControllerTests extends BaseMvcTest {
	
	String TEST_ENCRYPTED_SESSIONID;
	private EFDStyleInformationRequest efdStyleInformationRequest;
	private EFDStyleInformationResponse efdStyleInfoResponseSuccess;
	private EFDStyleInformationResponse efdStyleInfoResponseFailed;
	
	//CAP-49453
	private SaveEfdInformationResponse saveEfdInfoResponseSuccess;
	private SaveEfdInformationResponse saveEfdInfoResponseFailure;
	private SaveEfdInformationRequest saveEfdInfoRequest;


	@BeforeEach
	void setUp() throws Exception {
	
		setupBaseMockSessions();
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
	}

	// CAP-49326
	@Test
	void that_getEfdStyleInformation_returnsExpected200() throws Exception {

		efdStyleInformationRequest = getEFDStyleInformationRequestTest();
		efdStyleInfoResponseSuccess = getEFDStyleInformationResponseSuccessTest();
		
		when(mockEFDDestinationService.getEfdStyleInformationForOrder(any(SessionContainer.class),
				any(EFDStyleInformationRequest.class))).thenReturn(efdStyleInfoResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(efdStyleInformationRequest);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_EFD_STYLEINFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	// CAP-49326
	@Test
	void that_getEfdStyleInformation_returnFailedError422() throws Exception {

		efdStyleInformationRequest = getEFDStyleInformationRequestTest();
		efdStyleInfoResponseFailed = getEFDStyleInformationResponseFailedTest();

		when(mockEFDDestinationService.getEfdStyleInformationForOrder(any(SessionContainer.class),
				any(EFDStyleInformationRequest.class))).thenReturn(efdStyleInfoResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(efdStyleInformationRequest);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_EFD_STYLEINFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	@Test
	void that_getEfdOptions_http200() throws Exception {
		when(mockEFDDestinationService.getEFDOptions(any())).thenReturn(buildEFDOptionsResponse(true));
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_EFD_OPTIONS).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	@Test
	void that_getEfdOptions_http422() throws Exception {

		when(mockEFDDestinationService.getEFDOptions(any())).thenReturn(buildEFDOptionsResponse(false));
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_EFD_OPTIONS).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	//CAP-49453
	@Test
	void that_saveEfdInformation_returnsExpected200() throws Exception {

		saveEfdInfoResponseSuccess =  getEfdSaveResponseSuccess();
		saveEfdInfoRequest = getSaveEfdInformationRequest();
		
		when(mockEFDDestinationService.saveEfdInformation(any(SessionContainer.class),
				any(SaveEfdInformationRequest.class))).thenReturn(saveEfdInfoResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(saveEfdInfoRequest);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_EFD_INFORMATION).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-49453
	@Test
	void that_saveEfdInformation_returnFailedError422() throws Exception {

		saveEfdInfoResponseFailure = getEfdSaveResponseFailure();
		saveEfdInfoRequest = getSaveEfdInformationRequest();

		when(mockEFDDestinationService.saveEfdInformation(any(SessionContainer.class),
				any(SaveEfdInformationRequest.class))).thenReturn(saveEfdInfoResponseFailure);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(saveEfdInfoRequest);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_EFD_INFORMATION).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}

	// CAP-49326
	private EFDStyleInformationResponse getEFDStyleInformationResponseSuccessTest() {
		efdStyleInfoResponseSuccess = new EFDStyleInformationResponse();
		efdStyleInfoResponseSuccess.setSuccess(true);
		efdStyleInfoResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return efdStyleInfoResponseSuccess;
	}

	// CAP-49326
	private EFDStyleInformationResponse getEFDStyleInformationResponseFailedTest() {
		efdStyleInfoResponseFailed = new EFDStyleInformationResponse();
		efdStyleInfoResponseFailed.setSuccess(false);
		efdStyleInfoResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return efdStyleInfoResponseFailed;
	}

	// CAP-49326
	private EFDStyleInformationRequest getEFDStyleInformationRequestTest() {

		EFDStyleInformationRequest efdStyleInformationRequest = new EFDStyleInformationRequest();
		efdStyleInformationRequest.setStyleID("-1");
		return efdStyleInformationRequest;
	}
	
	private EFDOptionsResponse buildEFDOptionsResponse(boolean status) {
		EFDOptionsResponse response = new EFDOptionsResponse();
		response.setSuccess(status);
		response.setMessage(AtWinXSConstant.EMPTY_STRING);
		return response;
	}
	
	//CAP-49453
	private SaveEfdInformationResponse getEfdSaveResponseSuccess() {
		SaveEfdInformationResponse response = new SaveEfdInformationResponse();
		response.setSuccess(true);
		return response;
	}
	
	//CAP-49453
	private SaveEfdInformationResponse getEfdSaveResponseFailure() {
		SaveEfdInformationResponse response = new SaveEfdInformationResponse();
		response.setSuccess(false);
		return response;
	}
	
	//CAP-49453
	private SaveEfdInformationRequest getSaveEfdInformationRequest() {
		SaveEfdInformationRequest request = new SaveEfdInformationRequest();
		return request;
	}
}
