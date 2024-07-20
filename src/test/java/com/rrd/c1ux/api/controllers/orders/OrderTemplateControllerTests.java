/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *  5/01/24		M Sakthi			CAP-48745				C1UX BE - Create new tests for API to start the save order template process
 *	4/30/24		C Codina			CAP-48890				Initial Version
 *	04/30/24	S Ramachandran		CAP-48889				Added Junit test for deletetemplate controller handler
 *	05/07/24	Satishkumar A		CAP-48975				C1UX BE - Create new API to actually load template order and to use in cart
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
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.TemplateOrderListResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class OrderTemplateControllerTests extends BaseMvcTest {
	String TEST_ENCRYPTED_SESSIONID;
	private LoadSaveOrderTemplateRequest loadSaveOrderTemplateRequest;
	private LoadSaveOrderTemplateResponse responseSuccess;
	private LoadSaveOrderTemplateResponse responseFailed;

	private TemplateOrderListResponse templateOrderListResponseSuccess;
	private TemplateOrderListResponse templateOrderListResponseFailed;
	private DeleteTemplateRequest deleteOrderTemplateRequest;
	private DeleteTemplateResponse deleteOrderTemplateResponseSuccess;
	private DeleteTemplateResponse deleteOrderTemplateResponseFailed;
	
	private UseOrderTemplateResponse loadTemplateOrderResponseSuccess;
	private UseOrderTemplateResponse loadTemplateOrderResponseFailed;
	private UseOrderTemplateRequest loadOrderTemplateRequest;
	
	@BeforeEach
	void setUp() throws Exception {
		setupBaseMockSessions();
		responseSuccess = getLoadSaveOrderTemplateResponseSuccessTest();
		responseFailed = getLoadSaveOrderTemplateResponseFailedTest();
		loadSaveOrderTemplateRequest = loadSaveOrderTemplateRequestTest();
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		
		loadTemplateOrderResponseSuccess = loadTemplateOrderResponseSuccessTest();
		loadTemplateOrderResponseFailed = loadTemplateOrderResponseFailedTest();
		loadOrderTemplateRequest = getLoadOrderTemplateRequestTest();
	}
	
	@BeforeEach
	void setup() throws Exception {

		setupBaseMockSessions();
		templateOrderListResponseSuccess = getTemplateOrderListResponseSuccessTest();
		templateOrderListResponseFailed = getTemplateOrderListResponseFailedTest();
		deleteOrderTemplateResponseSuccess = getDeleteOrderTemplateSuccessTest();
		deleteOrderTemplateResponseFailed = getDeleteOrderTemplateFailedTest();
		deleteOrderTemplateRequest = getDeleteOrderTemplateRequestTest();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_loadSaveOrderTemplate_expected() throws Exception {

		when(mockOrderTemplateService.getOrderTemplateDetails(any(SessionContainer.class),
				any(LoadSaveOrderTemplateRequest.class))).thenReturn(responseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(loadSaveOrderTemplateRequest);

		// when getSearchordersDetail is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.LOAD_SAVE_ORDER_TEMPLATE)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_loadSaveOrderTemplate_failed() throws Exception {

		when(mockOrderTemplateService.getOrderTemplateDetails(any(SessionContainer.class),
				any(LoadSaveOrderTemplateRequest.class))).thenReturn(responseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(loadSaveOrderTemplateRequest);

		// when getSearchordersDetail is called, expect 400 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.LOAD_SAVE_ORDER_TEMPLATE)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(SFTranslationTextConstants.INVALID_ORDER_TEMPLATES));
	}

	private LoadSaveOrderTemplateResponse getLoadSaveOrderTemplateResponseSuccessTest() {
		responseSuccess = new LoadSaveOrderTemplateResponse();
		responseSuccess.setSuccess(true);
		responseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return responseSuccess;
	}

	private LoadSaveOrderTemplateResponse getLoadSaveOrderTemplateResponseFailedTest() {
		responseFailed = new LoadSaveOrderTemplateResponse();
		responseFailed.setSuccess(false);
		responseFailed.setMessage(SFTranslationTextConstants.INVALID_ORDER_TEMPLATES);
		return responseFailed;
	}

	private LoadSaveOrderTemplateRequest loadSaveOrderTemplateRequestTest() {
		LoadSaveOrderTemplateRequest req = new LoadSaveOrderTemplateRequest();
		req.setOrderTemplateID("-1");
		return req;
	}


	@Test
	void that_testGetTemplateOrderList_returnsExpected() throws Exception {
		when(mockOrderTemplateService.getTemplateOrderList(any(SessionContainer.class)))
				.thenReturn(templateOrderListResponseSuccess);

		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_TEMPLATE_ORDER_LIST)
				.accept(MediaType.APPLICATION_JSON).header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_testGetTemplateOrderList_Error() throws Exception {
		when(mockOrderTemplateService.getTemplateOrderList(any(SessionContainer.class)))
				.thenReturn(templateOrderListResponseFailed);

		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_TEMPLATE_ORDER_LIST)
				.accept(MediaType.APPLICATION_JSON).header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}

	private TemplateOrderListResponse getTemplateOrderListResponseSuccessTest() {
		templateOrderListResponseSuccess = new TemplateOrderListResponse();
		templateOrderListResponseSuccess.setSuccess(true);
		templateOrderListResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return templateOrderListResponseSuccess;
	}

	private TemplateOrderListResponse getTemplateOrderListResponseFailedTest() {
		templateOrderListResponseFailed = new TemplateOrderListResponse();
		templateOrderListResponseFailed.setSuccess(false);
		templateOrderListResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return templateOrderListResponseFailed;
	}
	
	// CAP-48889
	@Test
	void that_deleteOrderTemplate_expected200() throws Exception {

		when(mockOrderTemplateService.deleteOrderTemplate(any(SessionContainer.class),
				any(DeleteTemplateRequest.class))).thenReturn(deleteOrderTemplateResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(deleteOrderTemplateRequest);

		// when deleteOrderTemplate is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.DELETE_ORDER_TEMPLATE)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	// CAP-48889
	@Test
	void that_deleteOrderTemplate_failed422() throws Exception {

		when(mockOrderTemplateService.deleteOrderTemplate(any(SessionContainer.class),
				any(DeleteTemplateRequest.class))).thenReturn(deleteOrderTemplateResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(deleteOrderTemplateRequest);

		// when deleteOrderTemplate is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.DELETE_ORDER_TEMPLATE)
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	// CAP-48889
	private DeleteTemplateResponse getDeleteOrderTemplateSuccessTest() {

		deleteOrderTemplateResponseSuccess = new DeleteTemplateResponse();
		deleteOrderTemplateResponseSuccess.setSuccess(true);
		deleteOrderTemplateResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return deleteOrderTemplateResponseSuccess;
	}

	// CAP-48889
	private DeleteTemplateResponse getDeleteOrderTemplateFailedTest() {

		deleteOrderTemplateResponseFailed = new DeleteTemplateResponse();
		deleteOrderTemplateResponseFailed.setSuccess(false);
		deleteOrderTemplateResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return deleteOrderTemplateResponseFailed;
	}

	// CAP-48889
	private DeleteTemplateRequest getDeleteOrderTemplateRequestTest() {

		DeleteTemplateRequest request = new DeleteTemplateRequest();
		request.setOrderTemplateID("-1");
		return request;
	}
	
	@Test
	void that_loadTemplateOrder_returnsExpected() throws Exception {
		when(mockOrderTemplateService.loadTemplateOrder(any(SessionContainer.class), any(UseOrderTemplateRequest.class)))
				.thenReturn(loadTemplateOrderResponseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(loadOrderTemplateRequest);

		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.LOAD_TEMPLATE_ORDER)
				.accept(MediaType.APPLICATION_JSON).header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_loadTemplateOrder_Error() throws Exception {
		when(mockOrderTemplateService.loadTemplateOrder(any(SessionContainer.class), any(UseOrderTemplateRequest.class)))
				.thenReturn(loadTemplateOrderResponseFailed);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(loadOrderTemplateRequest);
		
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.LOAD_TEMPLATE_ORDER)
				.accept(MediaType.APPLICATION_JSON).header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}

	private UseOrderTemplateResponse loadTemplateOrderResponseSuccessTest() {
		loadTemplateOrderResponseSuccess = new UseOrderTemplateResponse();
		loadTemplateOrderResponseSuccess.setSuccess(true);
		loadTemplateOrderResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return loadTemplateOrderResponseSuccess;
	}

	private UseOrderTemplateResponse loadTemplateOrderResponseFailedTest() {
		loadTemplateOrderResponseFailed = new UseOrderTemplateResponse();
		loadTemplateOrderResponseFailed.setSuccess(false);
		loadTemplateOrderResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return loadTemplateOrderResponseFailed;
	}
	
	private UseOrderTemplateRequest getLoadOrderTemplateRequestTest() {

		UseOrderTemplateRequest request = new UseOrderTemplateRequest();
		request.setOrderTemplateID("-1");
		return request;
	}
}
