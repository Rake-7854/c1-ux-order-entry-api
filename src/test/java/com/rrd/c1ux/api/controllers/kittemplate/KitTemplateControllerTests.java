/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/10/24	N Caceres			CAP-50036				Initial Version
 *	06/17/24	L De Leon			CAP-50101				Added tests for initKitTemplate() method
 *	06/25/2024	N Caceres			CAP-50260				Added tests for adding wild card item to kit
 *	06/28/24	Satishkumar A		CAP-50504				C1UX BE - Creation of service to reload KitSession when coming back to kit editor from search or custom docs
 *	07/02/24	M Sakthi			CAP-50330				Added tests for addToCartKitComponent() method
 *	07/08/24	Satishkumar A		CAP-50737				C1UX BE - Create API to perform Catalog Search for Kit Templates
 */
package com.rrd.c1ux.api.controllers.kittemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

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
import com.rrd.c1ux.api.models.kittemplate.ComponentItems;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateRequest;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateSearchResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class KitTemplateControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private InitKitTemplateRequest initKitTemplateRequest;
	private InitKitTemplateResponse initKitTemplateResponseSuccess;
	private InitKitTemplateResponse initKitTemplateResponseFailed;

	public static final String EXPECTED_422MESSAGE = "Generic Error";
	
	@BeforeEach
	void setUp() throws Exception {
	
		setupBaseMockSessions();
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		initKitTemplateRequest = new InitKitTemplateRequest();
		initKitTemplateResponseSuccess = getInitKitTemplateResponseSuccessTest();
		initKitTemplateResponseFailed = getInitKitTemplateResponseFailedTest();
	}
	
	@Test
	void that_addKitComponent_returnsExpected200() throws Exception {
		KitTemplateAddCompRequest request = buildAddKitRequest();
		KitTemplateAddCompResponse response = new KitTemplateAddCompResponse();
		response.setSuccess(true);
		
		when(mockKitTemplateService.addKitComponent(any(), any())).thenReturn(response);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(request);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_ADD_COMPONENT).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	private KitTemplateAddCompRequest buildAddKitRequest() {
		KitTemplateAddCompRequest request = new KitTemplateAddCompRequest();
		request.setCompCustomerItemNumber("HRB1000");
		request.setCompVendorItemNumber("1000");
		request.setLocationCode(1);
		return request;
	}
	
	@Test
	void that_removeKitComponent_returnsExpected() throws Exception, AtWinXSException {
		
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		KitTemplateRemoveCompRequest request = buildRemoveKitRequest();
		KitTemplateRemoveCompResponse response = new KitTemplateRemoveCompResponse();
		response.setSuccess(true);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(request);
		
		when(mockKitTemplateService.removeKitComponent(any(), any())).thenReturn(response);

		// when removeKitComponent is called, expect 200 status 
        mockMvc.perform(
                MockMvcRequestBuilders.post(RouteConstants.KIT_REMOVE_COMPONENT)
                .accept(MediaType.APPLICATION_JSON)
                .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString)
                .characterEncoding("utf-8")
                )
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	@Test
	void that_removeKitComponent_returns422() throws Exception, AtWinXSException {
		
		
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		KitTemplateRemoveCompRequest request = buildRemoveKitRequest();
		KitTemplateRemoveCompResponse response = new KitTemplateRemoveCompResponse();
		response.setSuccess(false);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(request);
		
		when(mockKitTemplateService.removeKitComponent(any(), any())).thenReturn(response);


		// when removeKitComponent is called, expect 200 status 
        mockMvc.perform(
                MockMvcRequestBuilders.post(RouteConstants.KIT_REMOVE_COMPONENT)
                .accept(MediaType.APPLICATION_JSON)
                .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString)
                .characterEncoding("utf-8")
                )
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	private KitTemplateRemoveCompRequest buildRemoveKitRequest() {
		KitTemplateRemoveCompRequest request = new KitTemplateRemoveCompRequest();
		request.setKitLineNumber(1234);
		return request;
	}

	@Test
	void that_initKitTemplate_returnsExpected() throws Exception {

		when(mockKitTemplateService.initKitTemplate(any(SessionContainer.class),
				any(InitKitTemplateRequest.class))).thenReturn(initKitTemplateResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(initKitTemplateRequest);

		// when initKitTemplate is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.INIT_KIT_TEMPLATE)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(requestString))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_initKitTemplate_returnsErrorMessage_whenError() throws Exception {

		when(mockKitTemplateService.initKitTemplate(any(SessionContainer.class),
				any(InitKitTemplateRequest.class))).thenReturn(initKitTemplateResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(initKitTemplateRequest);

		// when initKitTemplate is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.INIT_KIT_TEMPLATE)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(requestString))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}

	private InitKitTemplateResponse getInitKitTemplateResponseSuccessTest() {

		initKitTemplateResponseSuccess = new InitKitTemplateResponse();
		initKitTemplateResponseSuccess.setSuccess(true);
		initKitTemplateResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return initKitTemplateResponseSuccess;
	}

	private InitKitTemplateResponse getInitKitTemplateResponseFailedTest() {

		initKitTemplateResponseFailed = new InitKitTemplateResponse();
		initKitTemplateResponseFailed.setSuccess(false);
		initKitTemplateResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return initKitTemplateResponseFailed;
	}
	
	// CAP-50260
	@Test
	void that_addWildCardComponent_returnsExpected200() throws Exception {
		KitTemplateAddCompRequest request = buildAddKitRequest();
		KitTemplateAddCompResponse response = new KitTemplateAddCompResponse();
		response.setSuccess(true);
		
		when(mockKitTemplateService.addWildCardComponent(any(), any())).thenReturn(response);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(request);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_ADD_WILD_CARD_COMPONENT).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	@Test
	void that_addWildCardComponent_returnsError422() throws Exception {
		KitTemplateAddCompRequest request = buildAddKitRequest();
		KitTemplateAddCompResponse response = new KitTemplateAddCompResponse();
		response.setSuccess(false);
		response.setMessage(EXPECTED_422MESSAGE);

		when(mockKitTemplateService.addWildCardComponent(any(), any())).thenReturn(response);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(request);

		// when addWildCardComponent is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_ADD_WILD_CARD_COMPONENT)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(requestString))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	
	//CAP-50504
	@Test
	void that_reloadKitTemplate_http200() throws Exception {
		
		when(mockKitTemplateService.reloadKitTemplate(any())).thenReturn(buildReloadKitSessionResponse(true));
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.RELOAD_KIT_TEMPLATE).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	//CAP-50504
	@Test
	void that_reloadKitTemplate_http422() throws Exception {

		when(mockKitTemplateService.reloadKitTemplate(any())).thenReturn(buildReloadKitSessionResponse(false));
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.RELOAD_KIT_TEMPLATE).accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	//CAP-50504
	InitKitTemplateResponse buildReloadKitSessionResponse(boolean isSuccess) {
		InitKitTemplateResponse response = new InitKitTemplateResponse();
		response.setSuccess(isSuccess);
		return response;
		
	}
	
	
	//CAP-50330
		@Test
		void that_addToCartKitTemplate_http200() throws Exception {
			
			
			KitTemplateAddToCartRequest request = buildAddtoCartKitRequest();
			KitTemplateAddToCartResponse response = new KitTemplateAddToCartResponse();
			response.setSuccess(true);
		
			when(mockKitTemplateService.addToCartKitTemplate(any(), any())).thenReturn(response);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(request);

			
			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_ADD_TO_CART).accept(MediaType.APPLICATION_JSON)
					.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
					.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
			
		}
		@Test
		void that_addToCartKitTemplate_http422() throws Exception {
			
			
			KitTemplateAddToCartRequest request = buildAddtoCartKitRequest();
			KitTemplateAddToCartResponse response = new KitTemplateAddToCartResponse();
			response.setSuccess(false);
			response.setMessage(EXPECTED_422MESSAGE);

			when(mockKitTemplateService.addToCartKitTemplate(any(), any())).thenReturn(response);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(request);


			// when addWildCardComponent is called, expect 422 status
			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_ADD_TO_CART)
					.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(requestString))
					.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXPECTED_422MESSAGE));
		}
		
		
		private KitTemplateAddToCartRequest buildAddtoCartKitRequest() {
			KitTemplateAddToCartRequest request = new KitTemplateAddToCartRequest();
			ComponentItems componentItems=new ComponentItems();
			Collection<ComponentItems> componentItemsList=new ArrayList<>();
			componentItems.setKitLineNumber("1234");
			componentItems.setSequenceLocationId(1);
			componentItems.setItemSequenceNumber(1);
			componentItems.setUomCode("EA");
			componentItems.setQuantity(2);
			componentItems.setCriticalIndicator("Y");
			componentItemsList.add(componentItems);
			request.setComponentItems(componentItemsList);
			request.setAssemblyInstructions("Test");
			return request;
		}
	
	
		//CAP-50737
		@Test
		void that_catalogSearchForKitTemplates_http200() throws Exception {
			
			KitTemplateAddToCartRequest request = buildAddtoCartKitRequest();
			request.setSearchTerm("test");
			
			when(mockKitTemplateService.catalogSearchForKitTemplates(any(),any())).thenReturn(buildCatalogSearchForKitTemplatesResponse(true));
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(request);

			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_CATALOG_SEARCH).accept(MediaType.APPLICATION_JSON)
					.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
					.characterEncoding("utf-8").content(requestString)).andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		}
		//CAP-50737
		@Test
		void that_catalogSearchForKitTemplates_http422() throws Exception {

			KitTemplateAddToCartRequest request = buildAddtoCartKitRequest();
			request.setSearchTerm("test");
			
			when(mockKitTemplateService.catalogSearchForKitTemplates(any(),any())).thenReturn(buildCatalogSearchForKitTemplatesResponse(false));
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(request);

			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.KIT_CATALOG_SEARCH).accept(MediaType.APPLICATION_JSON)
					.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
					.characterEncoding("utf-8").content(requestString))
					.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

		}
		//CAP-50737
		KitTemplateSearchResponse buildCatalogSearchForKitTemplatesResponse(boolean isSuccess) {
			KitTemplateSearchResponse response = new KitTemplateSearchResponse();
			response.setSuccess(isSuccess);
			return response;
			
		}
}