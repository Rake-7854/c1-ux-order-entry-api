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
 *  08/22/23	S Ramachandran		CAP-43234		Junit for Controller - Order Routing Information For Order Search 
 *  08/30/23	Satishkumar A		CAP-43283		C1UX BE - Routing Information For Justification Section on Review Order Page
 */

package com.rrd.c1ux.api.controllers.routing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

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
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationRequest;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationResponse;
import com.rrd.c1ux.api.models.routing.RoutingInformationResponse;
import com.rrd.custompoint.gwt.ordersearch.orderdetails.routinginfo.widget.OSDetailsRoutingInfoBean;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class RoutingControllerTests extends BaseMvcTest {

	
	private static final int TEST_ORDER_ID = 609614;
	private static final int TEST_NUMBER = 1;
	private static final String TEST_RESULT = "Line Approved";
	private static final String TEST_YOURITEM = "1018";
	private static final String TEST_QUEUE_DATE ="08/18/2023";
	private static final String TEST_ROUTING_REASON ="Ordered item requiring line review.";
	private static final String TEST_APPROVAL_DATE ="08/18/2023";
	private static final String TEST_APPROVAL_QUEUE_NAME ="IDC Item Level Approver2 (IDC-CP-USER2 SUBBU)";
	private static final String TEST_APPROVER_NAME ="IDC-CP-USER2 SUBBU (IDC-CP-USER2)";
	private static final String TEST_APPROVER_MESSAGE ="Line Item Level- Approved by IDC-CP-USER2";
	
	private String  requestString;;
	private OrderRoutingInformationResponse responseSuccess;
	private OrderRoutingInformationResponse responseFailed;
	String TEST_ENCRYPTED_SESSIONID;
	
	RoutingInformationResponse routingInformationResponse = getRoutingReviewResponse();
	RoutingInformationResponse routingInformationResponse_422 = getRoutingReviewResponse_422();

	
	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		
		requestString = getOrderRoutingInformationRequest();
		responseSuccess = getOrderRoutingInformationResponseSuccess();
		responseFailed = getOrderRoutingInformationResponseFailed(); 
    
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockAppSessionBean).getEncodedSessionId();
	}
	
	@Test
	void that_getOSRoutingInfos_returns200_Expected() throws Exception {
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockOrderSearchService.getOSRoutingInfos(any(SessionContainer.class),any(OrderRoutingInformationRequest.class)))
				.thenReturn(responseSuccess);

		// when getOSRoutingInfos is called, expect 200 status in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.ORDER_SEARCH_ROUTING_DETAILS)
					.accept(MediaType.APPLICATION_JSON)
					.header("ttsession", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
					.content(requestString)
					.characterEncoding("utf-8")
				)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
	}
	
	
	@Test
	void that_getOSRoutingInfos_returns422_whenError() throws Exception {
		
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockOrderSearchService.getOSRoutingInfos(any(SessionContainer.class),any(OrderRoutingInformationRequest.class)))
				.thenReturn(responseFailed);
		
		// when getOSRoutingInfos is called, expect 422 status in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.ORDER_SEARCH_ROUTING_DETAILS)
					.accept(MediaType.APPLICATION_JSON)
					.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestString)
					.characterEncoding("utf-8")
				)
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
	}
	
	@Test
	void that_getOSRoutingInfos_returns403_whenUnAuthorized () throws Exception {
		
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
		when(mockOrderSearchService.getOSRoutingInfos(any(SessionContainer.class),any(OrderRoutingInformationRequest.class)))
				.thenReturn(responseFailed);
		
		// when getOSRoutingInfos is called, expect 422 status in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.ORDER_SEARCH_ROUTING_DETAILS)
					.accept(MediaType.APPLICATION_JSON)
					.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestString)
					.characterEncoding("utf-8")
				)
				.andExpect(MockMvcResultMatchers.status().isForbidden())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
				
	}
	
	
	@Test
	void that_getOSRoutingInfos_returns400_whenBadRequest() throws Exception {
		
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockOrderSearchService.getOSRoutingInfos(any(SessionContainer.class),any(OrderRoutingInformationRequest.class)))
				.thenReturn(responseFailed);
		
		// when getOSRoutingInfos is called, expect 422 status in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.ORDER_SEARCH_ROUTING_DETAILS)
					.accept(MediaType.APPLICATION_JSON)
					.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{ \"orderID: 0}\n")
					.characterEncoding("utf-8")
				)
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
				
	}
	

	//CAP-43234 - Request Object for getOrderRoutingInformation
	private String getOrderRoutingInformationRequest() throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

	    OrderRoutingInformationRequest request = new OrderRoutingInformationRequest();
		request.setOrderID(TEST_ORDER_ID);
		
	    String requestInJson=ow.writeValueAsString(request);

	    return requestInJson;
	}
	
	
	//CAP-43234 - Response Success(200) Object for getOrderRoutingInformation
	private OrderRoutingInformationResponse getOrderRoutingInformationResponseSuccess() {
		
		OrderRoutingInformationResponse tempResponseSuccess= new OrderRoutingInformationResponse(); 
		
		Collection<OSDetailsRoutingInfoBean> routingDetails = new LinkedList<OSDetailsRoutingInfoBean>(); 
		
		Collection<String> routingReasons = new ArrayList<String>();
		routingReasons.add(TEST_ROUTING_REASON);
		
		OSDetailsRoutingInfoBean osDetailsRoutingInfoBean = new OSDetailsRoutingInfoBean();
		
		osDetailsRoutingInfoBean.setNumber(TEST_NUMBER);
		osDetailsRoutingInfoBean.setResult(TEST_RESULT);
		osDetailsRoutingInfoBean.setYourItem(TEST_YOURITEM);
		osDetailsRoutingInfoBean.setQueueDate(TEST_QUEUE_DATE);
		osDetailsRoutingInfoBean.setRoutingReasons(routingReasons);
		osDetailsRoutingInfoBean.setApprovalQueueName(TEST_APPROVAL_QUEUE_NAME);
		osDetailsRoutingInfoBean.setApprovalDate(TEST_APPROVAL_DATE);
		osDetailsRoutingInfoBean.setApproverName(TEST_APPROVER_NAME);
		osDetailsRoutingInfoBean.setApproverMessage(TEST_APPROVER_MESSAGE);

		routingDetails.add(osDetailsRoutingInfoBean);
		
		tempResponseSuccess.setRoutingDetails(routingDetails);
		tempResponseSuccess.setSuccess(true);
		return tempResponseSuccess;
	}	
	
	
	//CAP-43234 - Response Failed(422) Object for getOrderRoutingInformation
	private OrderRoutingInformationResponse getOrderRoutingInformationResponseFailed() {
		
		OrderRoutingInformationResponse tempResponseSuccess= new OrderRoutingInformationResponse(); 
		tempResponseSuccess.setSuccess(false);
		return tempResponseSuccess;
	}
	
	@Test
	void that_routingreview_returnsExpected() throws Exception, AtWinXSException {
		
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		when(mockRoutingInformationService.getRoutingInformation(mockSessionContainer)).thenReturn(routingInformationResponse);
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_ROUTING_INFORMATION).contentType(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	@Test
	void that_routingreview_returns422Expected() throws Exception, AtWinXSException {
		
		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
		when(mockRoutingInformationService.getRoutingInformation(mockSessionContainer)).thenReturn(routingInformationResponse_422);
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_ROUTING_INFORMATION).contentType(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));

	}
	
	private RoutingInformationResponse getRoutingReviewResponse() {
		RoutingInformationResponse response = new RoutingInformationResponse();
		response.setSuccess(true);
		response.setMessage("");
		return response;
		
	}
	private RoutingInformationResponse getRoutingReviewResponse_422() {
		RoutingInformationResponse response = new RoutingInformationResponse();
		response.setSuccess(false);
		response.setMessage("");
		return response;
		
	}


	
}
