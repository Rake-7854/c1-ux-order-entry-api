/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/27/23	L De Leon			CAP-45907				Initial Version
 *	01/12/24	Satishkumar A		CAP-46380				C1UX BE - Create api to retrieve initial user/profile information
 *	02/28/24	M Sakthi			CAP-47450				C1UX BE - Create API to validate the extended profile fields
 *	03/02/24    Satishkumar A		CAP-47592				C1UX BE - Create validation story for Password validation
 *	02/28/24	S Ramachandran		CAP-47410				Added junit tests for API controller saveSelfRegUser
 *	03/05/24	S Ramachandran		CAP-47629				Added junit tests for API validate basic profile
 *	03/06/24	Satishkumar A		CAP-47672				C1UX BE - Create Validation method for UDF fields for Self Registration 
 */
package com.rrd.c1ux.api.controllers.selfreg;

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
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationPatternAfterResponse;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveRequest;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class SelfRegistrationControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private SelfRegistrationPatternAfterResponse selfRegistrationPatternAfterResponseSuccess;
	private SelfRegistrationPatternAfterResponse selfRegistrationPatternAfterResponseFailed;
	
	private SelfRegistrationSaveResponse selfRegistrationSaveResponseSuccess;
	private SelfRegistrationSaveResponse selfRegistrationSaveResponseFailed;
	private SelfRegistrationSaveRequest request;
	

	public static final String EXPECTED_422MESSAGE = "Generic Error";

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		selfRegistrationPatternAfterResponseSuccess = getSelfRegistrationPatternAfterResponseSuccessTest();
		selfRegistrationPatternAfterResponseFailed = getSelfRegistrationPatternAfterResponseFailedTest();
		
		
		selfRegistrationSaveResponseSuccess= getSelfRegistrationSaveResponseSuccessTest();
		selfRegistrationSaveResponseFailed = getSelfRegistrationSaveResponseFailedTest();
		request=getSelfRegistrationSaveRequest();
		

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_getPatternAfterUsers_returnsExpected() throws Exception {
		
		// when getPatternAfterUsers returns a success SelfRegistrationPatternAfterResponse object
		when(mockSelfRegistrationService.getPatternAfterUsers(any(SessionContainer.class)))
				.thenReturn(selfRegistrationPatternAfterResponseSuccess);
		
		// when getPatternAfterUsers is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_PATTERN_AFTER_USERS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_getPatternAfterUsers_returnsErrorMessage_whenError() throws Exception {

		// when getPatternAfterUsers returns a failed SelfRegistrationPatternAfterResponse object
		when(mockSelfRegistrationService.getPatternAfterUsers(any(SessionContainer.class)))
				.thenReturn(selfRegistrationPatternAfterResponseFailed);

		// when getPatternAfterUsers is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_PATTERN_AFTER_USERS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	//CAP-46380
	@Test
	void that_getInitialSelfRegUser_returnsExpected() throws Exception {

		when(mockSelfRegistrationService.getInitialSelfRegUser(any(SessionContainer.class), any(String.class)))
				.thenReturn(selfRegistrationPatternAfterResponseSuccess);

		// when getInitialSelfRegUser is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_INITIAL_SELF_REG_USER)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content("{ \"patternAfterUser\": \"USER-RRD\" }\n"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-46380
	@Test
	void that_getInitialSelfRegUser_returnsErrorMessage_whenError() throws Exception {

		when(mockSelfRegistrationService.getInitialSelfRegUser(any(SessionContainer.class), any(String.class)))
		.thenReturn(selfRegistrationPatternAfterResponseFailed);

		// when getInitialSelfRegUser is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_INITIAL_SELF_REG_USER)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content("{ \"patternAfterUser\": \"USER-RRD\" }\n"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	
	//CAP-47450
	@Test
	void that_getExtendedProfile_returnsExpected() throws Exception {
	
		// when validateExtendedProfile returns a success SelfRegistrationSaveResponse object
		when(mockSelfRegistrationService.validateExtendedProfile(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
				.thenReturn(selfRegistrationSaveResponseSuccess);
		
			ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	        String selfRegEPRequestString = ow.writeValueAsString(request);

		// when validateExtendedProfile is called, expect 200 status
	        mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_EXTENDED_PROFILE)
					.accept(MediaType.APPLICATION_JSON)
					.header("ttsession", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(selfRegEPRequestString)
					.characterEncoding("utf-8")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true)); 
  
	}
	
	
	@Test
	void that_getExtendedProfile_returnsFailed() throws Exception {

		// when validateExtendedProfile returns a success SelfRegistrationSaveResponse
		// object
		when(mockSelfRegistrationService.validateExtendedProfile(any(SessionContainer.class),
				any(SelfRegistrationSaveRequest.class))).thenReturn(selfRegistrationSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String selfRegEPRequestString = ow.writeValueAsString(request);

		// when validateExtendedProfile is called, expect 422 status
		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.VALIDATE_EXTENDED_PROFILE).accept(MediaType.APPLICATION_JSON)
						.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
						.content(selfRegEPRequestString).characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)); 
	}
	
	//CAP-47410
	@Test
	void that_saveSelfRegUserr_returnsSuccess200_Expected() throws Exception {

		//when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSelfRegistrationService.saveSelfRegistration(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
			.thenReturn(selfRegistrationSaveResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owSelfRegistrationSaveRequestValid = ow.writeValueAsString(request);
		
		// when saveSelfRegistration is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_SELF_REG_USER)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(owSelfRegistrationSaveRequestValid).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-47410
	@Test
	void that_saveSelfRegUser_returnsErrorMessage_whenError() throws Exception {

		when(mockSelfRegistrationService.saveSelfRegistration(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
			.thenReturn(selfRegistrationSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owSelfRegistrationSaveRequestValid = ow.writeValueAsString(request);

        // when saveSelfRegistration is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_SELF_REG_USER)
				.accept(owSelfRegistrationSaveRequestValid)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(owSelfRegistrationSaveRequestValid).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	
	
	//CAP-47410
	@Test
	void that_validateBasicProfile_returnsSuccess200_Expected() throws Exception {

		//when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSelfRegistrationService.validateBasicProfile(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
			.thenReturn(selfRegistrationSaveResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owsfValidateBasicProfileRequestValid = ow.writeValueAsString(request);
		
		// when saveSelfRegistration is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_BASIC_PROFILE)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(owsfValidateBasicProfileRequestValid).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-47410
	@Test
	void that_validateBasicProfile_returnsErrorMessage_whenError() throws Exception {

		when(mockSelfRegistrationService.validateBasicProfile(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
			.thenReturn(selfRegistrationSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owsfValidateBasicProfileRequestValid = ow.writeValueAsString(request);

        // when saveSelfRegistration is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_BASIC_PROFILE)
				.accept(owsfValidateBasicProfileRequestValid)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(owsfValidateBasicProfileRequestValid).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	

	//CAP-47592
	@Test
	void that_validatePassword_returnsExpected() throws Exception {

		when(mockSelfRegistrationService.validatePasswordAndPatternAfterUser(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
				.thenReturn(selfRegistrationSaveResponseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String selfRegValidatePasswordString = ow.writeValueAsString(request);

		// when validatePassword is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_PASSWORD)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content(selfRegValidatePasswordString)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-47592
	@Test
	void that_validatePassword_returnsErrorMessage_whenError() throws Exception {

		when(mockSelfRegistrationService.validatePasswordAndPatternAfterUser(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class)))
		.thenReturn(selfRegistrationSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String selfRegValidatePasswordString = ow.writeValueAsString(request);

		// when validatePassword is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_PASSWORD)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content(selfRegValidatePasswordString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}

	//CAP-47672
	@Test
	void that_validateUserDefinedFields_returnsExpected() throws Exception {

		when(mockSelfRegistrationService.validateUserDefinedFields(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class), any(SelfRegistrationSaveResponse.class)))
				.thenReturn(selfRegistrationSaveResponseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String validateUDFString = ow.writeValueAsString(request);

		// when validatePassword is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_USER_DEFINED_FIELDS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content(validateUDFString)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	//CAP-47672
	@Test
	void that_validateUserDefinedFields_returnsErrorMessage_whenError() throws Exception {

		when(mockSelfRegistrationService.validateUserDefinedFields(any(SessionContainer.class), any(SelfRegistrationSaveRequest.class), any(SelfRegistrationSaveResponse.class)))
		.thenReturn(selfRegistrationSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String validateUDFString = ow.writeValueAsString(request);

		// when validatePassword is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_USER_DEFINED_FIELDS)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content(validateUDFString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}

	private SelfRegistrationPatternAfterResponse getSelfRegistrationPatternAfterResponseSuccessTest() {

		selfRegistrationPatternAfterResponseSuccess = new SelfRegistrationPatternAfterResponse();
		selfRegistrationPatternAfterResponseSuccess.setSuccess(true);
		selfRegistrationPatternAfterResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return selfRegistrationPatternAfterResponseSuccess;
	}

	private SelfRegistrationPatternAfterResponse getSelfRegistrationPatternAfterResponseFailedTest() {

		selfRegistrationPatternAfterResponseFailed = new SelfRegistrationPatternAfterResponse();
		selfRegistrationPatternAfterResponseFailed.setSuccess(false);
		selfRegistrationPatternAfterResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return selfRegistrationPatternAfterResponseFailed;
	}
	
	
	private SelfRegistrationSaveResponse getSelfRegistrationSaveResponseSuccessTest() {

		selfRegistrationSaveResponseSuccess = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponseSuccess.setSuccess(true);
		selfRegistrationSaveResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return selfRegistrationSaveResponseSuccess;
	}
	
	private SelfRegistrationSaveResponse getSelfRegistrationSaveResponseFailedTest() {

		selfRegistrationSaveResponseFailed = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponseFailed.setSuccess(false);
		selfRegistrationSaveResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return selfRegistrationSaveResponseFailed;
	}
	
	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest() {
		SelfRegistrationSaveRequest registrationSaveRequest=new SelfRegistrationSaveRequest();
		registrationSaveRequest.setFirstName("TestFName");
		registrationSaveRequest.setLastName("TestLname");
		registrationSaveRequest.setEpName2("Contact Name1");
		return registrationSaveRequest;
	}
	
	

	//CAP-47450
	@Test
	void that_getAttributes_returnsExpected() throws Exception {

		// when validateExtendedProfile returns a success SelfRegistrationSaveResponse
		// object
		when(mockSelfRegistrationService.validateAttributes(any(SessionContainer.class),
				any(SelfRegistrationSaveRequest.class))).thenReturn(selfRegistrationSaveResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String selfRegEPRequestString = ow.writeValueAsString(request);

		// when validateAttribute is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_SELF_REG_ATTRIBUTES)
				.accept(MediaType.APPLICATION_JSON).header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(selfRegEPRequestString).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

	}

	@Test
	void that_getAttributes_returnsFailed() throws Exception {

		// when validateExtendedProfile returns a success SelfRegistrationSaveResponse
		// object
		when(mockSelfRegistrationService.validateAttributes(any(SessionContainer.class),
				any(SelfRegistrationSaveRequest.class))).thenReturn(selfRegistrationSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String selfRegEPRequestString = ow.writeValueAsString(request);

		// when validateExtendedProfile is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.VALIDATE_SELF_REG_ATTRIBUTES)
				.accept(MediaType.APPLICATION_JSON).header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(selfRegEPRequestString).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}

}