/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			JIRA #			Description
 *	--------	-----------			----------		-----------------------------------------------------------
 *	09/12/23	M Sakthi			CAP-41591		Initial Version
 *	11/20/23	Satishkumar A		CAP-38135		C1UX BE - Modification of Manual Enter Address to use new USPS validation
 *	12/18/23	M Sakthi			CAP-45544		C1UX BE - Fix code to only search/edit/save for the originator PAB addresses in OOB mode
 *	03/14/24	M Sakthi			CAP-47840		C1UX BE - Create API to retrieve list of Distribution Lists.
 *	03/18/24	S Ramachandran		CAP-48002		Junit test added for uploadDistListFileAndGetWorksheetName
 *	03/27/24	S Ramachandran		CAP-48277		Junit tests added for uploadDistList controller handler  
 *	04/01/24	Satishkumar A		CAP-48123		C1UX BE - Create API to retrieve Dist List addresses. 
 *	04/09/24	S Ramachandran		CAP-48434		Junit tests modified for uploadDistList controller handler
 */
package com.rrd.c1ux.api.controllers.checkout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.admin.PABSearchRequest;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveRequest;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveResponse;
import com.rrd.c1ux.api.models.checkout.CreateListVarsRequest;
import com.rrd.c1ux.api.models.checkout.DistListAddressRequest;
import com.rrd.c1ux.api.models.checkout.DistListAddressResponse;
import com.rrd.c1ux.api.models.checkout.DistListCountRequest;
import com.rrd.c1ux.api.models.checkout.DistListCountResponse;
import com.rrd.c1ux.api.models.checkout.DistributionListResponse;
import com.rrd.c1ux.api.models.checkout.SaveManagedFieldsResponse;
import com.rrd.c1ux.api.models.checkout.SaveMappingRequest;
import com.rrd.c1ux.api.models.checkout.SaveMappingResponse;
import com.rrd.c1ux.api.models.checkout.UploadDistListRequest;
import com.rrd.c1ux.api.models.checkout.UploadDistListResponse;
import com.rrd.c1ux.api.models.checkout.WorksheetResponse;
import com.rrd.c1ux.api.models.common.GenericSearchCriteria;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

@WithMockUser
class CODeliveryInformationControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	
	private List<Integer> testAddressIDs = new ArrayList<>();

	private static final int TEST_ADDRESS_ID = 12345;
	
	private PABSearchRequest pabSearchRequest;
	private PABSearchResponse pabSearchResponseSuccess;
	private PABSearchResponse pabSearchResponseFailed;
	private static final String VALID_SEARCH_CRITERIA_MSG = "Enter valid Criteria" ;
	private static final String TEST_SEARCH_CRITERIA_KEY = "country" ;
	private static final String TEST_SEARCH_CRITERIA_VALUE = "USA" ;
	
	//CAP-48123
	private static final String TEST_ENCRYPTED_DIST_LIST_ID = "IOT95YCWzokxXhfYreccuTIEjxVaNrKZ" ;
	
	//CAP-47840
	private DistributionListResponse distributionListResponseSuccess;
	private DistributionListResponse distributionListResponseFailed;
	
	//CAP-47998
	private SaveManagedFieldsResponse saveManagedFieldsResponseSuccess;
	private SaveManagedFieldsResponse saveManagedFieldsResponseFailed;

	//CAP-48002
	private WorksheetResponse worksheetResponseSuccess;
	private WorksheetResponse worksheetResponseFailed;
	
	//CAP-47999
	private DistListCountResponse distributionListCountResponseSuccess;
	private DistListCountResponse distributionListCountResponseFailed;
	private DistListCountRequest distListCountRequest;
	
	//CAP-48123
	private DistListAddressResponse distListAddressResponseSuccess;
	private DistListAddressResponse distListAddressResponseFailed;
	private DistListAddressRequest distListAddressRequest;
	
	// CAP-48277
	private UploadDistListResponse uploadDistListResponseSuccess;
	private UploadDistListResponse uploadDistListResponseFailed;
	
	// CAP-48497
	private SaveMappingResponse saveMappingResponseSuccess;
	private SaveMappingResponse saveMappingResponseFailed;
	private SaveMappingRequest saveMappingRequest;

	@Mock
	OEResolvedUserSettingsSessionBean mockOEResolvedUserSettingsSessionBean;

	@BeforeEach
	void setUp() throws Exception {
 
		setupBaseMockSessions();

		testAddressIDs.add(TEST_ADDRESS_ID);
		pabSearchRequest = gePABSearchRequest();
		pabSearchResponseSuccess = getPABSearchResponseSuccessTest();
		pabSearchResponseFailed = gePABSearchResponseFailedTest();
		
		//CAP-47840
		distributionListResponseSuccess=getDistributionListResponseSuccessTest();
		distributionListResponseFailed=getDistributionListResponseFailedTest();
		
		//CAP-47998
		saveManagedFieldsResponseSuccess=getSaveManagedFieldsResponseSuccessTest();
		saveManagedFieldsResponseFailed=getSaveManagedFieldsResponseFailedTest();
		
		distributionListCountResponseSuccess = getDistributionListCountResponseSuccessTest();
		distributionListCountResponseFailed = getDistributionListCountResponseFailedTest();
		distListCountRequest = getDistListCountRequest();

		//CAP-48123
		distListAddressResponseSuccess = getDistListAddressResponseSuccessTest();
		distListAddressResponseFailed = getDistListAddressResponseFailedTest();
		distListAddressRequest = getDistListAddressRequest();

		//CAP-48002
		worksheetResponseSuccess = worksheetResponseSuccessTest();
		worksheetResponseFailed = worksheetResponseFailedTest();
		
		// CAP-48277
		uploadDistListResponseSuccess = uploadDistListInfoResponseSuccessTest();
		uploadDistListResponseFailed = uploadDistListInfoResponseFailedTest();
		
		// CAP-48497
		saveMappingResponseSuccess = saveMappingResponseSuccessTest();
		saveMappingResponseFailed = saveMappingResponseFailedTest();
		saveMappingRequest = saveMappingRequestTest();
		
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	
	
	
	@Test
	void that_searchPABAddress_returnsExpected() throws Exception {
		
		// when searchPAB returns a valid PABSearchResponse object
		when(mockSelfAdminService.searchPAB(any(SessionContainer.class), any(PABSearchRequest.class),anyBoolean()))
				.thenReturn(pabSearchResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(pabSearchRequest);
		
		when(mockOEResolvedUserSettingsSessionBean.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(new CompositeProfileBean());
		when(mockAppSessionBean.getRequestorProfile()).thenReturn(new CompositeProfileBean());
		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockOEResolvedUserSettingsSessionBean);
			
		
			
		// when searchPAB is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_OE_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		}
	}
	@Test
	void that_searchPABAddress_returnsErrorMessage_whenError() throws Exception {

		// when searchPAB returns a valid PABSearchResponse object
		when(mockSelfAdminService.searchPAB(any(SessionContainer.class), any(PABSearchRequest.class),anyBoolean()))
				.thenReturn(pabSearchResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(pabSearchRequest);
		
		when(mockOEResolvedUserSettingsSessionBean.isAllowOrderOnBehalf()).thenReturn(false);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(new CompositeProfileBean());
		when(mockAppSessionBean.getRequestorProfile()).thenReturn(null);
		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockOEResolvedUserSettingsSessionBean);

		// when searchPAB is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_OE_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(VALID_SEARCH_CRITERIA_MSG));
		}
	}
	//CAP-38135
	@Test
	void that_saveDeliveryInformation_returnsExpected() throws Exception {

		// when saveDeliveryInformationV1 returns a valid CODeliveryInformationSaveResponse object
		when(mockCODeliveryInformationService.saveDeliveryInformation(any(SessionContainer.class), any(CODeliveryInformationSaveRequest.class),anyBoolean()))
				.thenReturn(getCODeliveryInformationSaveResponseSuccessTest());

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCODeliveryInformationSaveRequest());

		// when saveDeliveryInformationV1 is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_DELIVERY_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		
	}
	//CAP-38135
	@Test
	void that_saveDeliveryInformation_returnsErrorMessage_whenError1() throws Exception {

		// when saveDeliveryInformationV1 returns a valid CODeliveryInformationSaveResponse object
		when(mockCODeliveryInformationService.saveDeliveryInformation(any(SessionContainer.class), any(CODeliveryInformationSaveRequest.class),anyBoolean()))
				.thenReturn(getCODeliveryInformationSaveResponseFailureTest());

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCODeliveryInformationSaveRequest());

		// when searchPAB is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_DELIVERY_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	//CAP-38135
	@Test
	void that_saveDeliveryInformationV1_returnsExpected() throws Exception {

		// when saveDeliveryInformationV1 returns a valid CODeliveryInformationSaveResponse object
		when(mockCODeliveryInformationService.saveDeliveryInformation(any(SessionContainer.class), any(CODeliveryInformationSaveRequest.class),anyBoolean()))
				.thenReturn(getCODeliveryInformationSaveResponseSuccessTest());

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCODeliveryInformationSaveRequest());

		// when saveDeliveryInformationV1 is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_DELIVERY_INFO_V1).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	//CAP-38135
	@Test
	void that_saveDeliveryInformationV1_returnsErrorMessage_whenError1() throws Exception {

		// when saveDeliveryInformationV1 returns a valid CODeliveryInformationSaveResponse object
		when(mockCODeliveryInformationService.saveDeliveryInformation(any(SessionContainer.class), any(CODeliveryInformationSaveRequest.class),anyBoolean()))
				.thenReturn(getCODeliveryInformationSaveResponseFailureTest());

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCODeliveryInformationSaveRequest());

		// when searchPAB is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_DELIVERY_INFO_V1).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	//CAP-38135
	private CODeliveryInformationSaveResponse getCODeliveryInformationSaveResponseSuccessTest() {

		CODeliveryInformationSaveResponse saveResponse = new CODeliveryInformationSaveResponse();
		saveResponse.setSuccess(true);
		saveResponse.setMessage(AtWinXSConstant.EMPTY_STRING);
		return saveResponse;
	}
	//CAP-38135
	private CODeliveryInformationSaveResponse getCODeliveryInformationSaveResponseFailureTest() {

		CODeliveryInformationSaveResponse saveResponse = new CODeliveryInformationSaveResponse();
		saveResponse.setSuccess(false);
		saveResponse.setMessage(AtWinXSConstant.EMPTY_STRING);
		return saveResponse;
	}
	private PABSearchResponse getPABSearchResponseSuccessTest() {

		pabSearchResponseSuccess = new PABSearchResponse();
		pabSearchResponseSuccess.setSuccess(true);
		pabSearchResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return pabSearchResponseSuccess;
	}
	
	private PABSearchResponse gePABSearchResponseFailedTest() {

		pabSearchResponseFailed = new PABSearchResponse();
		pabSearchResponseFailed.setSuccess(false);
		pabSearchResponseFailed.setMessage(VALID_SEARCH_CRITERIA_MSG);
		return pabSearchResponseFailed;
	}
	
	private PABSearchRequest gePABSearchRequest() {
		
		PABSearchRequest pabSearchRequest = new PABSearchRequest();
		
		GenericSearchCriteria genericSearchCriteria = new GenericSearchCriteria();
		genericSearchCriteria.setCriteriaFieldKey(TEST_SEARCH_CRITERIA_KEY);
		genericSearchCriteria.setCriteriaFieldValue(TEST_SEARCH_CRITERIA_VALUE);
		
		List<GenericSearchCriteria> genericSearchCriterias = new ArrayList<GenericSearchCriteria>();
		genericSearchCriterias.add(genericSearchCriteria);
		pabSearchRequest.setGenericSearchCriteria(genericSearchCriterias);

		return pabSearchRequest;
	}

	//CAP-38135
	private CODeliveryInformationSaveRequest getCODeliveryInformationSaveRequest() {
		
		CODeliveryInformationSaveRequest request = new CODeliveryInformationSaveRequest();
		request.setAddressLine1("Address1");
		request.setAddressLine1("Address2");
		request.setCity("City");
		request.setStateOrProvince("State");
		request.setPostalCode("11111");
		return request;
		
	}
	
	//CAP-47840
	@Test
	void that_coDeliveryInformationController_Distribution_list_returnsExpected() throws Exception {
	when(mockCODeliveryInformationService.getManagedLists(any(SessionContainer.class),anyBoolean()))
				.thenReturn(distributionListResponseSuccess);
		
	mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_DISTRIBUTION_LIST).contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
			
	}	
	
	@Test
	void that_coDeliveryInformationController_Distribution_list_returnsFailed() throws Exception {
	when(mockCODeliveryInformationService.getManagedLists(any(SessionContainer.class),anyBoolean()))
				.thenReturn(distributionListResponseFailed);
		
	mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_DISTRIBUTION_LIST).contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
			
	}	
	
	private DistributionListResponse getDistributionListResponseSuccessTest() {

		distributionListResponseSuccess = new DistributionListResponse();
		distributionListResponseSuccess.setSuccess(true);
		distributionListResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return distributionListResponseSuccess;
	}
	
	private DistributionListResponse getDistributionListResponseFailedTest() {

		distributionListResponseFailed = new DistributionListResponse();
		distributionListResponseFailed.setSuccess(false);
		return distributionListResponseFailed;
	}
	
	
	//CAP-47998
	@Test
	void that_coDeliveryInformationController_saved_managed_fields_returnsExpected() throws Exception {
		when(mockCODeliveryInformationService.saveManagedListFields(any(SessionContainer.class),any()))
					.thenReturn(saveManagedFieldsResponseSuccess);
			
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCreateListVarsRequest());

		// when searchPAB is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.CREATE_LIST_VARS).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
		
		@Test
		void that_coDeliveryInformationController_saved_managed_fields_returnsFailed() throws Exception {
			when(mockCODeliveryInformationService.saveManagedListFields(any(SessionContainer.class),any()))
			.thenReturn(saveManagedFieldsResponseFailed);
	
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
				ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
				String requestString = ow.writeValueAsString(getCreateListVarsRequest());
				
				// when searchPAB is called, expect 422 status
				mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.CREATE_LIST_VARS).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
						.content(requestString).characterEncoding("utf-8"))
						.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
						.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
								
		}	
		
		private SaveManagedFieldsResponse getSaveManagedFieldsResponseSuccessTest() {

			saveManagedFieldsResponseSuccess = new SaveManagedFieldsResponse();
			saveManagedFieldsResponseSuccess.setSuccess(true);
			saveManagedFieldsResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
			return saveManagedFieldsResponseSuccess;
		}
		
		private SaveManagedFieldsResponse getSaveManagedFieldsResponseFailedTest() {

			saveManagedFieldsResponseFailed = new SaveManagedFieldsResponse();
			saveManagedFieldsResponseFailed.setSuccess(false);
			return saveManagedFieldsResponseFailed;
		}
		
		
		private CreateListVarsRequest getCreateListVarsRequest() {
			CreateListVarsRequest request = new CreateListVarsRequest();
			request.setListName("Test1");
			request.setListDescription("Test Desc");
			request.setHeaders(true);
			request.setSharedList(true);
			return request;
			
		}
		
	//CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_returns_expectedSuccess200() throws Exception {
		when(mockCODeliveryInformationService.uploadDistListFileAndGetWorksheetName(any(SessionContainer.class),any()))
					.thenReturn(worksheetResponseSuccess);
			
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCreateListVarsRequest());

		// when uploadDistributionListFile() is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_WORKSHEETS).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	//CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_returns_expectedFailed403() throws Exception {
		when(mockCODeliveryInformationService.uploadDistListFileAndGetWorksheetName(any(SessionContainer.class),any()))
		.thenReturn(worksheetResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getCreateListVarsRequest());
		
		// when uploadDistributionListFile() is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_WORKSHEETS).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	
	//CAP-48002
	private WorksheetResponse worksheetResponseSuccessTest() {

		worksheetResponseSuccess = new WorksheetResponse();
		worksheetResponseSuccess.setSuccess(true);
		worksheetResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return worksheetResponseSuccess;
	}
	
	//CAP-48002
	private WorksheetResponse worksheetResponseFailedTest() {

		worksheetResponseFailed = new WorksheetResponse();
		worksheetResponseFailed.setSuccess(false);
		return worksheetResponseFailed;
	}
	
	@Test
	void that_getDistributionListCount_returnsexpected() throws Exception {
		when(mockCODeliveryInformationService.getDistributionListRecordCount(any(SessionContainer.class), any(DistListCountRequest.class)))
		.thenReturn(distributionListCountResponseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(distListCountRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_DISTRIBUTION_LIST_RECORD_COUNT)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
				.content(requestString))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
		
	}
	
	@Test
	void that_getDistributionListCount_returnsFailed() throws Exception {
		when(mockCODeliveryInformationService.getDistributionListRecordCount(any(SessionContainer.class), any(DistListCountRequest.class)))
		.thenReturn(distributionListCountResponseFailed);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(distListCountRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_DISTRIBUTION_LIST_RECORD_COUNT)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
				.content(requestString))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		
	}
	
	
	//CAP-48123
	@Test
	void that_getDistributionListAddresses_returnsexpected() throws Exception {
		when(mockCODeliveryInformationService.getDistributionListAddresses(any(SessionContainer.class), any(DistListAddressRequest.class)))
		.thenReturn(distListAddressResponseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(distListAddressRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_DISTRIBUTION_LIST_ADDRESSES)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
				.content(requestString))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
		
	}
	//CAP-48123
	@Test
	void that_getDistributionListAddresses_returnsFailed() throws Exception {
		when(mockCODeliveryInformationService.getDistributionListAddresses(any(SessionContainer.class), any(DistListAddressRequest.class)))
		.thenReturn(distListAddressResponseFailed);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(distListAddressRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_DISTRIBUTION_LIST_ADDRESSES)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
				.content(requestString))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		
	}
	
	
	private DistListCountResponse getDistributionListCountResponseSuccessTest() {
		distributionListCountResponseSuccess = new DistListCountResponse();
		distributionListCountResponseSuccess.setSuccess(true);
		distributionListCountResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return distributionListCountResponseSuccess;
	}
	private DistListCountResponse getDistributionListCountResponseFailedTest() {
		distributionListCountResponseFailed = new DistListCountResponse();
		distributionListCountResponseFailed.setSuccess(false);
		return distributionListCountResponseFailed;
	}
	private DistListCountRequest getDistListCountRequest() {
		DistListCountRequest countRequest = new DistListCountRequest();
		countRequest.setWorksheetName("TestWorksheetName");
		
		return countRequest;
		
	}
	
	//CAP-48123
	private DistListAddressResponse getDistListAddressResponseSuccessTest() {
		distListAddressResponseSuccess = new DistListAddressResponse();
		distListAddressResponseSuccess.setSuccess(true);
		distListAddressResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return distListAddressResponseSuccess;
	}
	//CAP-48123
	private DistListAddressResponse getDistListAddressResponseFailedTest() {
		distListAddressResponseFailed = new DistListAddressResponse();
		distListAddressResponseFailed.setSuccess(false);
		return distListAddressResponseFailed;
	}
	//CAP-48123
	private DistListAddressRequest getDistListAddressRequest() {
		DistListAddressRequest countRequest = new DistListAddressRequest();
		countRequest.setDistListID(TEST_ENCRYPTED_DIST_LIST_ID);
		return countRequest;
		
	}	
	
	// CAP-48277, CAP-48434
	@Test
	void that_uploadDistListInfo_returnsExpected200() throws Exception {

		// when uploadDistList returns a UploadDistListResponse with 200 success
		when(mockCODeliveryInformationService.uploadDistList(any(SessionContainer.class),
				any(UploadDistListRequest.class), anyBoolean(), anyBoolean()))
				.thenReturn(uploadDistListResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(uploadDistListResponseSuccess);

		// when uploadDistList is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.UPLOAD_DIST_LIST)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}

	// CAP-48277, CAP-48434
	@Test
	void that_uploadDistListInfo_returnsFailed() throws Exception {

		// when uploadDistList returns a UploadDistListResponse with 422 Error
		when(mockCODeliveryInformationService.uploadDistList(any(SessionContainer.class),
				any(UploadDistListRequest.class), anyBoolean(), anyBoolean())).thenReturn(uploadDistListResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(uploadDistListResponseFailed);

		// when uploadDistList is called, failed 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.UPLOAD_DIST_LIST)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	
	// CAP-48277
	private UploadDistListResponse uploadDistListInfoResponseSuccessTest() {

		uploadDistListResponseSuccess = new UploadDistListResponse();
		uploadDistListResponseSuccess.setSuccess(true);
		uploadDistListResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return uploadDistListResponseSuccess;
	}
	
	// CAP-48277
	private UploadDistListResponse uploadDistListInfoResponseFailedTest() {

		uploadDistListResponseFailed = new UploadDistListResponse();
		uploadDistListResponseFailed.setSuccess(false);
		return uploadDistListResponseFailed;
	}
	
	
	//CAP-48497
	
		@Test
		void that_saveListMappings_returnsexpected() throws Exception {
			when(mockCODeliveryInformationService.saveListMappings(any(SessionContainer.class),
					any(SaveMappingRequest.class), anyBoolean(), anyBoolean(), any())).thenReturn(saveMappingResponseSuccess);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(saveMappingRequest);

			mockMvc.perform(
					MockMvcRequestBuilders.post(RouteConstants.SAVE_LIST_MAPPINGS).accept(MediaType.APPLICATION_JSON)
							.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
							.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON).content(requestString))
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

		}

		@Test
		void that_saveListMappings_returnsFailed() throws Exception {
			when(mockCODeliveryInformationService.saveListMappings(any(SessionContainer.class),
					any(SaveMappingRequest.class), anyBoolean(), anyBoolean(), any())).thenReturn(saveMappingResponseFailed);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(saveMappingRequest);

			mockMvc.perform(
					MockMvcRequestBuilders.post(RouteConstants.SAVE_LIST_MAPPINGS).accept(MediaType.APPLICATION_JSON)
							.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
							.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON).content(requestString))
					.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

		}

		private SaveMappingResponse saveMappingResponseSuccessTest() {
			saveMappingResponseSuccess = new SaveMappingResponse();
			saveMappingResponseSuccess.setSuccess(true);
			saveMappingResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
			return saveMappingResponseSuccess;
		}

		private SaveMappingResponse saveMappingResponseFailedTest() {
			saveMappingResponseFailed = new SaveMappingResponse();
			saveMappingResponseFailed.setSuccess(false);
			return saveMappingResponseFailed;
		}

		private SaveMappingRequest saveMappingRequestTest() {
			SaveMappingRequest lmapper = new SaveMappingRequest();
			return lmapper;

		}
}
