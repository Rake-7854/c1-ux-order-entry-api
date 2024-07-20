package com.rrd.c1ux.api.controllers.admin;

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
import com.rrd.c1ux.api.models.admin.PABDeleteRequest;
import com.rrd.c1ux.api.models.admin.PABDeleteResponse;
import com.rrd.c1ux.api.models.admin.PABExportResponse;
import com.rrd.c1ux.api.models.admin.PABImportRequest;
import com.rrd.c1ux.api.models.admin.PABImportResponse;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.admin.PABSearchRequest;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.models.common.GenericSearchCriteria;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

@WithMockUser
class SelfAdminPABControllerTests extends BaseMvcTest{
	
	private List<Integer> testAddressIDs = new ArrayList<>();
	String TEST_ENCRYPTED_SESSIONID;
	private static final String VALID_SEARCH_CRITERIA_MSG = "Enter valid Criteria" ;
	private static final String TEST_SEARCH_CRITERIA_KEY = "country" ;
	private static final String TEST_SEARCH_CRITERIA_VALUE = "USA" ;
	private static final int TEST_ADDRESS_ID = 12345;
	
	private PABSearchRequest pabSearchRequest;
	private PABSearchRequest pabSearchCallingFromRequest;
	private PABSearchResponse pabSearchResponseSuccess;
	private PABSearchResponse pabSearchResponseFailed;
	
	private PABSaveResponse pabSaveResponseSuccess;
	private PABSaveResponse pabSaveResponseFailed;
	
	private PABDeleteResponse pabDeleteResponseSuccess;
	private PABDeleteResponse pabDeleteResponseFailed;
	
	
	private PABImportRequest pabImportReq=new PABImportRequest();
	private PABImportResponse pabImportResponseSuccess;
	private PABImportResponse pabImportResponseFailed;
	
	
	private PABExportResponse pabExportResponseSuccess;
	
	@Mock
	OEResolvedUserSettingsSessionBean mockOEResolvedUserSettingsSessionBean;
	
	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		testAddressIDs.add(TEST_ADDRESS_ID);
		pabSearchRequest = gePABSearchRequest();
		
		pabSearchCallingFromRequest=gePABSearchCallingFromRequest();
		
		pabSearchResponseSuccess = getPABSearchResponseSuccessTest();
		pabSearchResponseFailed = getPABSearchResponseFailedTest();
		
		pabSaveResponseSuccess=getPABSaveResponseSuccessTest();
		pabSaveResponseFailed=getPABSaveResponseFailedTest();
		
		
		pabDeleteResponseSuccess=getPABDeleteResponseSuccessTest();
		pabDeleteResponseFailed=getPABDeleteResponseFailedTest();
		
		pabImportResponseSuccess=getPABImportResponseSuccessTest();
		pabImportResponseFailed=getPABImportResponseFailedTest();
		
		pabExportResponseSuccess=getPABExportResponseSuccessTest();
		

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
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		}
	}
	
	@Test
	void that_searchPABAddressCallingFromAdmin_returnsExpected() throws Exception {
		
		// when searchPAB returns a valid PABSearchResponse object
		when(mockSelfAdminService.searchPAB(any(SessionContainer.class), any(PABSearchRequest.class),anyBoolean()))
				.thenReturn(pabSearchResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(pabSearchCallingFromRequest);
		
		when(mockOEResolvedUserSettingsSessionBean.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(new CompositeProfileBean());
		when(mockAppSessionBean.getRequestorProfile()).thenReturn(new CompositeProfileBean());
		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class)	){
			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(),anyInt(),any())).thenReturn(mockOEResolvedUserSettingsSessionBean);
			
		
			
		// when searchPAB is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_PAB).accept(MediaType.APPLICATION_JSON)
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
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(VALID_SEARCH_CRITERIA_MSG));
		}
	}
	
	
	
	@Test
	void that_selfAdminPABContoller_Save_returnsExpected() throws Exception {
	when(mockSelfAdminService.savePABAddress(any(SessionContainer.class),any(PABSaveRequest.class),anyBoolean(), anyBoolean()))
				.thenReturn(pabSaveResponseSuccess);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getPABSaveRequest());

		// when savePAB is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
				
	}	
	
	
	
	@Test
	void that_selfAdminPABContoller_Save_returnsFailed() throws Exception {
		when(mockSelfAdminService.savePABAddress(any(SessionContainer.class),any(PABSaveRequest.class),anyBoolean(), anyBoolean()))
		.thenReturn(pabSaveResponseFailed);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getPABSaveRequest());

		// when Save is called, expect 403 status
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		
	}
	
	
	@Test
	void that_selfAdminPABContoller_Save_V1_returnsExpected() throws Exception {
	when(mockSelfAdminService.savePABAddress(any(SessionContainer.class),any(PABSaveRequest.class),anyBoolean(), anyBoolean()))
				.thenReturn(pabSaveResponseSuccess);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getPABSaveRequest());

		// when savePAB is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_PAB_V1).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
				
	}	
	
	
	
	@Test
	void that_selfAdminPABContoller_Save_V1_returnsFailed() throws Exception {
		when(mockSelfAdminService.savePABAddress(any(SessionContainer.class),any(PABSaveRequest.class),anyBoolean(), anyBoolean()))
		.thenReturn(pabSaveResponseFailed);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getPABSaveRequest());

		// when Save is called, expect 403 status
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_PAB_V1).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		
	}
	
	@Test
	void that_selfAdminPABContoller_Delete_returnsExpected() throws Exception {
	when(mockSelfAdminService.deletePABAddress(any(PABDeleteRequest.class),any(SessionContainer.class),anyBoolean()))
				.thenReturn(pabDeleteResponseSuccess);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getPABSaveRequest());

		// when savePAB is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.DELETE_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
				
	}	
	
	
	
	@Test
	void that_selfAdminPABContoller_Delete_returnsFailed() throws Exception {
		when(mockSelfAdminService.deletePABAddress(any(PABDeleteRequest.class),any(SessionContainer.class),anyBoolean()))
		.thenReturn(pabDeleteResponseFailed);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(getPABSaveRequest());

		// when Save is called, expect 403 status
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.DELETE_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		
	}
	
	
	
	@Test
	void that_selfAdminPABContoller_Import_returnsExpected() throws Exception {
	when(mockSelfAdminService.importAllPABAddresses(any(SessionContainer.class),any(PABImportRequest.class),anyBoolean()))
				.thenReturn(pabImportResponseSuccess);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(new PABImportRequest());

		// when savePAB is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.IMPORT_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
				
	}	
	
	@Test
	void that_selfAdminPABContoller_Import_returnsFailed() throws Exception {
	when(mockSelfAdminService.importAllPABAddresses(any(SessionContainer.class),any(PABImportRequest.class),anyBoolean()))
				.thenReturn(pabImportResponseFailed);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(new PABImportRequest());

		// when savePAB is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.IMPORT_PAB).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
				
	}	
	
	
	@Test
	void that_selfAdminPABContoller_Export_returnsExpected() throws Exception {
	when(mockSelfAdminService.exportPABAddresses(any(SessionContainer.class),any(),anyBoolean()))
				.thenReturn(pabExportResponseSuccess);
		
		// when export is called, expect 200 status
	mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.EXPORT_PAB).contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.characterEncoding("utf-8").accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk());
			
	}	

	private PABSearchRequest gePABSearchRequest() {
		
			PABSearchRequest pabSearchRequest = new PABSearchRequest();
			
			GenericSearchCriteria genericSearchCriteria = new GenericSearchCriteria();
			genericSearchCriteria.setCriteriaFieldKey(TEST_SEARCH_CRITERIA_KEY);
			genericSearchCriteria.setCriteriaFieldValue(TEST_SEARCH_CRITERIA_VALUE);
			
			List<GenericSearchCriteria> genericSearchCriterias = new ArrayList<GenericSearchCriteria>();
			genericSearchCriterias.add(genericSearchCriteria);
			pabSearchRequest.setGenericSearchCriteria(genericSearchCriterias);
			pabSearchRequest.setCallingFrom("Checkout");
	
			return pabSearchRequest;
	}
	
	private PABSearchRequest gePABSearchCallingFromRequest() {
		
		PABSearchRequest pabSearchRequest = new PABSearchRequest();
		
		GenericSearchCriteria genericSearchCriteria = new GenericSearchCriteria();
		genericSearchCriteria.setCriteriaFieldKey(TEST_SEARCH_CRITERIA_KEY);
		genericSearchCriteria.setCriteriaFieldValue(TEST_SEARCH_CRITERIA_VALUE);
		
		List<GenericSearchCriteria> genericSearchCriterias = new ArrayList<GenericSearchCriteria>();
		genericSearchCriterias.add(genericSearchCriteria);
		pabSearchRequest.setGenericSearchCriteria(genericSearchCriterias);
		pabSearchRequest.setCallingFrom("");

		return pabSearchRequest;
}
	
	
	private PABSearchResponse getPABSearchResponseSuccessTest() {

		pabSearchResponseSuccess = new PABSearchResponse();
		pabSearchResponseSuccess.setSuccess(true);
		pabSearchResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return pabSearchResponseSuccess;
	}
	
	private PABSearchResponse getPABSearchResponseFailedTest() {
		pabSearchResponseFailed = new PABSearchResponse();
		pabSearchResponseFailed.setSuccess(false);
		pabSearchResponseFailed.setMessage(VALID_SEARCH_CRITERIA_MSG);
		return pabSearchResponseFailed;
	}
	
	private PABSaveRequest getPABSaveRequest() {
		PABSaveRequest request=new PABSaveRequest();
		request.setAddressID(1234);  
		request.setName("Test Name");
		request.setName2("");
		request.setAddress1("Test Address1");
		request.setAddress2("");
		request.setAddress3("");
		request.setCity("New Yark");
		request.setState("IL");
		request.setZip("12134");
		request.setCountry("USA");
		request.setShipToAttn("Test attn");
		request.setPhoneNumber("2342");
		request.setHasPassedZipValidation(false);
		request.setShowShipToAtn(false);
		request.setDefaultAddress(false);
		request.setCorporateAddress(false);
		request.setExtendedAddress(false);
		
		return request;
	}
	
	private PABSaveResponse getPABSaveResponseSuccessTest() {
		PABSaveResponse saveResponseSuccess=new PABSaveResponse();
		saveResponseSuccess.setSuccess(true);
		saveResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return saveResponseSuccess;
	}
	
	private PABSaveResponse getPABSaveResponseFailedTest() {
		PABSaveResponse saveResponseFailed=new PABSaveResponse();
		saveResponseFailed.setSuccess(false);
		saveResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return saveResponseFailed;
	}
	
	
	private PABDeleteResponse getPABDeleteResponseSuccessTest() {

		pabDeleteResponseSuccess = new PABDeleteResponse();
		pabDeleteResponseSuccess.setSuccess(true);
		pabDeleteResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return pabDeleteResponseSuccess;
	}
	
	private PABDeleteResponse getPABDeleteResponseFailedTest() {
		pabDeleteResponseFailed = new PABDeleteResponse();
		pabDeleteResponseFailed.setSuccess(false);
		pabDeleteResponseFailed.setMessage(VALID_SEARCH_CRITERIA_MSG);
		return pabDeleteResponseFailed;
	}
	
	
	private PABImportResponse getPABImportResponseSuccessTest() {
		PABImportResponse importResponseSuccess=new PABImportResponse();
		importResponseSuccess.setSuccess(true);
		importResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return importResponseSuccess;
	}
	
	private PABImportResponse getPABImportResponseFailedTest() {
		PABImportResponse importResponseFailed=new PABImportResponse();
		importResponseFailed.setSuccess(false);
		importResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return importResponseFailed;
	}
	
	
	private PABExportResponse getPABExportResponseSuccessTest() {
		PABExportResponse exportResponseSuccess=new PABExportResponse();
		exportResponseSuccess.setSuccess(true);
		exportResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return exportResponseSuccess;
	}


}
