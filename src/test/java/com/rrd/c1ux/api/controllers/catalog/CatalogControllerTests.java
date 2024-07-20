/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/23/24				C Codina 				CAP-46379					C1UX BE - Method to retrieve Attribute Filters for Order Entry
 *	02/22/24				C Codina				CAP-47086					C1UX BE - Order wizard api that will perform search
 */

package com.rrd.c1ux.api.controllers.catalog;

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
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeRequest;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeResponse;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchRequest;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchResponse;
import com.rrd.c1ux.api.models.catalog.OrderWizardSelectedAttributes;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class CatalogControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private CatalogAttributeResponse catalogResponseSuccess;
	private CatalogAttributeResponse catalogResponseFailed;
	private OrderWizardSearchResponse wizardSearchResponseSuccess;
	private OrderWizardSearchResponse wizardSearchResponseFailed;
	private OrderWizardSearchRequest wizardSearchRequest;

	@BeforeEach
	void setup() throws Exception {
		setupBaseMockSessions();
		catalogResponseSuccess = getCatalogResponseSuccessTest();
		catalogResponseFailed = getCatalogResponseFailedTest();
		wizardSearchResponseSuccess = getOrderSearchWizardResponseSuccess();
		wizardSearchResponseFailed = getOrderSearchWizardResponseFailed();
		wizardSearchRequest = getOrderWizardSearchRequest();
	
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_getCatalogAttributes_returnsExpected() throws Exception{
		when(mockCatalogService.getCatalogAttributes(any(SessionContainer.class), any(CatalogAttributeRequest.class)))
		.thenReturn(catalogResponseSuccess);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_ATTRIBUTE_FILTERS)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
		
	}

	@Test
	void that_getCatalogAttributes_returnsErrorMessage_whenError() throws Exception{
		when(mockCatalogService.getCatalogAttributes(any(SessionContainer.class), any(CatalogAttributeRequest.class)))
		.thenReturn(catalogResponseFailed);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_ATTRIBUTE_FILTERS)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}

	@Test
	void that_performWizardSearch_returnsExpected() throws Exception{
		when(mockOrderWizardService.performWizardSearch(any(SessionContainer.class), any(OrderWizardSearchRequest.class)))
		.thenReturn(wizardSearchResponseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(wizardSearchRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.WIZARD_SEARCH)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestString)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
				
	}

	@Test
	void that_performWizardSearch_returnsErrorMessage_whenError() throws Exception{
		when(mockOrderWizardService.performWizardSearch(any(SessionContainer.class), any(OrderWizardSearchRequest.class)))
		.thenReturn(wizardSearchResponseFailed);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(wizardSearchRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.WIZARD_SEARCH)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestString)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").
						value(SFTranslationTextConstants.INLIVAD_WIZARD_SEARCH_VAL));
	}

	private CatalogAttributeResponse getCatalogResponseSuccessTest() {
		catalogResponseSuccess = new CatalogAttributeResponse();
		catalogResponseSuccess.setSuccess(true);
		catalogResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return catalogResponseSuccess;
	}

	private CatalogAttributeResponse getCatalogResponseFailedTest() {
		catalogResponseFailed = new CatalogAttributeResponse();
		catalogResponseFailed.setSuccess(false);
		catalogResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return catalogResponseFailed;
	}

	private OrderWizardSearchResponse getOrderSearchWizardResponseSuccess() {
		wizardSearchResponseSuccess = new OrderWizardSearchResponse();
		wizardSearchResponseSuccess.setSuccess(true);
		wizardSearchResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return wizardSearchResponseSuccess;
	}

	private OrderWizardSearchResponse getOrderSearchWizardResponseFailed() {
		wizardSearchResponseFailed = new OrderWizardSearchResponse();
		wizardSearchResponseFailed.setSuccess(false);
		wizardSearchResponseFailed.setMessage(SFTranslationTextConstants.INLIVAD_WIZARD_SEARCH_VAL);
		return wizardSearchResponseFailed;
	}
	private OrderWizardSearchRequest getOrderWizardSearchRequest() {
		OrderWizardSearchRequest searchRequest = new OrderWizardSearchRequest();
		OrderWizardSelectedAttributes attributeValue = new OrderWizardSelectedAttributes();

		List<OrderWizardSelectedAttributes> attributeValueList = new ArrayList<>();
		
		attributeValue.setAttributeID(5404);
		attributeValue.setAttributeValueID(258600);
		attributeValueList.add(attributeValue);
		
		searchRequest.setAttributeValues(attributeValueList);
		return searchRequest;
		
	}

}
