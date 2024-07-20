/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 *  08/09/23    M Sakthi			CAP-42562       C1UX BE - Self Admin – Update Profile User Defined Fields (API Build)
 *  02/01/24	S Ramachandran		CAP-46801		Junit test added for save site attribute	
 */


package com.rrd.c1ux.api.controllers.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributeValue;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesRequest;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesResponse;
import com.rrd.c1ux.api.models.admin.UserDefinedFieldsRequest;
import com.rrd.c1ux.api.models.admin.UserDefinedfieldsResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class SelfAdminControllerTests extends BaseMvcTest {

	private UserDefinedfieldsResponse userDefinedfieldsResponseSuccess;
	private UserDefinedfieldsResponse userDefinedfieldsResponseFailed;
	String TEST_ENCRYPTED_SESSIONID;
	private UserDefinedFieldsRequest  request =  new UserDefinedFieldsRequest();
	
	//CAP-46801
	private C1UserSiteAttributesResponse c1UserSiteAttributesResponseSuccess; 
	private C1UserSiteAttributesResponse c1UserSiteAttributesResponseFailed;
	private C1UserSiteAttributesRequest c1UserSiteAttributesRequest_Valid;
	private static final int TEST_ATTR_ID=4444;
	private static final int TEST_ATTR_VAL_ID=444444;
	
	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();
		userDefinedfieldsResponseSuccess = getUserDefinedfieldsResponseSuccessTest();
        userDefinedfieldsResponseFailed = getUserDefinedfieldsResponseFailedTest();
        
        //CAP-46801
        c1UserSiteAttributesResponseSuccess = getc1UserSiteAttributesResponseSuccessTest(); 
    	c1UserSiteAttributesResponseFailed = getc1UserSiteAttributesResponseFailedTest();
    	c1UserSiteAttributesRequest_Valid = getc1UserSiteAttributesRequest();
    	

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
	}
	
	
	
	@Test
	void that_selfAdminContoller_returnsExpected() throws Exception {
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSelfAdminService.updateUserDefinedFields(any(UserDefinedFieldsRequest.class),any(SessionContainer.class)))
				.thenReturn(userDefinedfieldsResponseSuccess);
   
		getUDFRequest();
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String updateUDF = ow.writeValueAsString(request);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.UPDATE_USER_DEFINE_FIELDS_API).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(updateUDF).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
	}
	
	
	@Test
	void that_selfAdminContoller_returnFailed() throws Exception {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSelfAdminService.updateUserDefinedFields(any(UserDefinedFieldsRequest.class),any(SessionContainer.class)))
				.thenReturn(userDefinedfieldsResponseFailed);

		getUDFRequest();
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String updateUDF = ow.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.UPDATE_USER_DEFINE_FIELDS_API).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(updateUDF).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
				
	}
	
	//CAP-46801
	@Test
	void that_selfAdminController_saveSiteAttributes_returnSuccess() throws Exception {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSelfAdminService.saveUserSiteAttributes(any(C1UserSiteAttributesRequest.class), any(SessionContainer.class)))
			.thenReturn(c1UserSiteAttributesResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owc1UserSiteAttributesRequest_Valid = ow.writeValueAsString(c1UserSiteAttributesRequest_Valid);

        mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_SITE_ATTRIBUTES)
        	.accept(MediaType.APPLICATION_JSON)
			.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
			.content(owc1UserSiteAttributesRequest_Valid).characterEncoding("utf-8"))
        	.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
	}
	
	
	//CAP-46801
	@Test
	void that_selfAdminController_saveSiteAttributes_returnFailed() throws Exception {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSelfAdminService.saveUserSiteAttributes(any(C1UserSiteAttributesRequest.class), any(SessionContainer.class)))
			.thenReturn(c1UserSiteAttributesResponseFailed);
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owc1UserSiteAttributesRequest_Valid = ow.writeValueAsString(c1UserSiteAttributesRequest_Valid);

        mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_SITE_ATTRIBUTES)
        	.accept(MediaType.APPLICATION_JSON)
			.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
			.content(owc1UserSiteAttributesRequest_Valid).characterEncoding("utf-8"))
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	
	
	private UserDefinedfieldsResponse getUserDefinedfieldsResponseSuccessTest() {
		userDefinedfieldsResponseSuccess = new UserDefinedfieldsResponse();
		userDefinedfieldsResponseSuccess.setSuccess(true);
		return userDefinedfieldsResponseSuccess;
	}
	
	private UserDefinedfieldsResponse getUserDefinedfieldsResponseFailedTest() {

		userDefinedfieldsResponseFailed = new UserDefinedfieldsResponse();
		userDefinedfieldsResponseFailed.setSuccess(false);
		return userDefinedfieldsResponseFailed;
	}
	
	private void getUDFRequest() {
		C1UserDefinedField udfField=new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("Test123");
		ArrayList<C1UserDefinedField> userDefinedFields=new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
	}
	
	//CAP-46801
	private C1UserSiteAttributesResponse getc1UserSiteAttributesResponseSuccessTest() {
		
		C1UserSiteAttributesResponse c1UserSiteAttributesResponse = new C1UserSiteAttributesResponse();
		c1UserSiteAttributesResponse.setSuccess(true);
		return c1UserSiteAttributesResponse;
	}
	
	//CAP-46801
	private C1UserSiteAttributesResponse getc1UserSiteAttributesResponseFailedTest() {
		
		C1UserSiteAttributesResponse c1UserSiteAttributesResponse = new C1UserSiteAttributesResponse();
		c1UserSiteAttributesResponse.setSuccess(false);
		return c1UserSiteAttributesResponse;
	}
	
	//CAP-46801
	private C1UserSiteAttributesRequest getc1UserSiteAttributesRequest() {
		
		c1UserSiteAttributesRequest_Valid = new C1UserSiteAttributesRequest();
		
		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst = new ArrayList<>();
		C1UserSiteAttributeValue c1UserSiteAttributeValue = new C1UserSiteAttributeValue(TEST_ATTR_VAL_ID);
		c1UserSiteAttributeValueLst.add(c1UserSiteAttributeValue);
		
		List<C1UserSiteAttribute> c1UserSiteAttributeLst = new ArrayList<>();
		C1UserSiteAttribute c1UserSiteAttribute = new C1UserSiteAttribute(TEST_ATTR_ID, c1UserSiteAttributeValueLst);
		c1UserSiteAttributeLst.add(c1UserSiteAttribute);
		
		c1UserSiteAttributesRequest_Valid.setC1UserSiteAttributes(c1UserSiteAttributeLst);
		
		return c1UserSiteAttributesRequest_Valid;
	}
	
}







