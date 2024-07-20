/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	12/05/23				C Codina				CAP-45054					Initial Version. Modified to add call to the service to copy order
 *	03/13/24				S Ramachandran			CAP-47841					Added Junit test for controller handler downloadOrderFileFromOS 
 */

package com.rrd.c1ux.api.controllers.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResultResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSession;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSessionBean;

@WithMockUser
class OrderSearchControllerTests extends BaseMvcTest{
	
	String TEST_ENCRYPTED_SESSIONID;
	private COOrderSearchRequest coOrderSearchRequest;
	private COOrderSearchResultResponse responseSuccess;
	private COOrderSearchResultResponse responseFailed;
	
	//CAP-47841
	private DownloadOrderFileResponse downloadOrderFileResponseSuccess;
	private DownloadOrderFileResponse downloadOrderFileResponseFailed;
	
	@Mock
	OrderStatusSession mockOrderStatusSession;
	@Mock
	OrderStatusSessionBean mockOrderStatusSessionBean;
	
	@BeforeEach
	void setUp() throws Exception {
		setupBaseMockSessions();
		
		coOrderSearchRequest = new COOrderSearchRequest();
		responseSuccess = getCOOrderSearchResultResponseSuccessTest();
		responseFailed = getCOOrderSearchResultResponseFailedTest();
		
		//CAP-47841
		downloadOrderFileResponseSuccess = getDownloadOrderFileFromOSSuccessTest();
		downloadOrderFileResponseFailed = getDownloadOrderFileFromOSFailedTest();
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderStatusSession);
		when(mockOrderStatusSession.getOrderStatusSettingsBean()).thenReturn(mockOrderStatusSessionBean);
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}
	@Test
	void that_getSearchOrdersDetail_expected() throws Exception {
		
		when(mockOrderSearchService.getSearchOrdersDetail(any(SessionContainer.class), any(COOrderSearchRequest.class)))
		.thenReturn(responseSuccess);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(coOrderSearchRequest);

		// when getSearchordersDetail is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_ORDERS_DETAIL)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	@Test
	void that_getSearchOrdersDetail_returnsErrorMessage_whenError() throws Exception {
		when(mockOrderSearchService.getSearchOrdersDetail(any(SessionContainer.class), any(COOrderSearchRequest.class)))
		.thenReturn(responseFailed);

				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
				ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
				String requestString = ow.writeValueAsString(coOrderSearchRequest);

				// when getSearchordersDetail is called, expect 422 status
				mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SEARCH_ORDERS_DETAIL).accept(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
						.content(requestString).characterEncoding("utf-8"))
						.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
						.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
								.jsonPath("$.message").value(SFTranslationTextConstants.NUMERIC_ERROR_DEF));
			}

	
	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_expected200() throws Exception {
		
		when(mockCOOrderFilesService.downloadOrderFileFromOS(any(SessionContainer.class), 
			any(HttpServletResponse.class), anyString()))
				.thenReturn(downloadOrderFileResponseSuccess);
		
		// when getSearchordersDetail is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.DOWNLOAD_ORDER_FILES)
			.accept(MediaType.APPLICATION_JSON)
			.param("a", "")
			.header("ttsession", TEST_ENCRYPTED_SESSIONID)
			.contentType(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
			.characterEncoding("utf-8"))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	//CAP-47841
	@Test
	void that_downloadOrderFileFromOS_whenError_returns403() throws Exception {
		
		when(mockCOOrderFilesService.downloadOrderFileFromOS(any(SessionContainer.class), 
			any(HttpServletResponse.class), anyString()))
				.thenReturn(downloadOrderFileResponseFailed);

		// when getSearchordersDetail is called, expect 403 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.DOWNLOAD_ORDER_FILES)
			.accept(MediaType.APPLICATION_JSON)
			.param("a", "")
			.header("ttsession", TEST_ENCRYPTED_SESSIONID)
			.contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8"))
			.andExpect(MockMvcResultMatchers.status().isForbidden())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	
	private COOrderSearchResultResponse getCOOrderSearchResultResponseSuccessTest(){
		responseSuccess = new COOrderSearchResultResponse();
		responseSuccess.setSuccess(true);
		responseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return responseSuccess;
	}
	private COOrderSearchResultResponse getCOOrderSearchResultResponseFailedTest() {
		responseFailed = new COOrderSearchResultResponse();
		responseFailed.setSuccess(false);
		responseFailed.setMessage(SFTranslationTextConstants.NUMERIC_ERROR_DEF);
		return responseFailed;
	}
	
	//CAP-47841
	private DownloadOrderFileResponse getDownloadOrderFileFromOSSuccessTest() {
		
		downloadOrderFileResponseSuccess = new DownloadOrderFileResponse();
		downloadOrderFileResponseSuccess.setSuccess(true);
		downloadOrderFileResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return downloadOrderFileResponseSuccess;
	}
	
	//CAP-47841
	private DownloadOrderFileResponse getDownloadOrderFileFromOSFailedTest() {
		
		downloadOrderFileResponseFailed = new DownloadOrderFileResponse();
		downloadOrderFileResponseFailed.setSuccess(false);
		return downloadOrderFileResponseFailed;
	}

}
