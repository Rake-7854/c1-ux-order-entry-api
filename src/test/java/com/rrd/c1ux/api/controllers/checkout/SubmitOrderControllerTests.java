/*
 *	Copyright (c) RR Donnelley. All Rights Reserved.
 *	This software is the confidential and proprietary information of RR Donnelley.
 *	You shall not disclose such confidential information.
 *	
 *	Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/08/23	S Ramachandran	CAP-41235					Review order - Submit order - Junit 
 *  06/22/2023  C Porter        CAP-41584                   Remove excess logging statements.
 *  09/12/2023 	Satishkumar A	CAP-42763					C1UX BE - Order Routing Justification Text Submit Order   
 */

package com.rrd.c1ux.api.controllers.checkout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.checkout.SubmitOrderResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class SubmitOrderControllerTests extends BaseMvcTest {

	private SubmitOrderResponse submitOrderResponseSuccess = getSubmitOrderResponseSuccessTest();
	private SubmitOrderResponse submitOrderResponseFailed = getSubmitOrderResponseFailedTest();

	private static final String TEST_SALE_REFERENCE_NUMBER = "80031955";
	private static final String TEST_WCSS_ORDER_NUMBER = "21316398";
	private static final int TEST_ORDER_ID = 604534;

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);

		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_submitOrder_returnsExpected() throws Exception, AtWinXSException {

		// given that submitOrder returns a valid SubmitOrderResponse object
		when(mockSubmitOrderService.submitOrder(any(SessionContainer.class), anyString()))
				.thenReturn(submitOrderResponseSuccess);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when submitOrder is called, expect 200 status and Sales Ref#, WCSS
		// OrderNumber, Order ID in JSON
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.SUBMIT_ORDER).contentType(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRIPTED_SESSIONID).characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING))
				.andExpect(MockMvcResultMatchers.jsonPath("$.salesReferenceNumber").value(TEST_SALE_REFERENCE_NUMBER))
				.andExpect(MockMvcResultMatchers.jsonPath("$.wcssOrderNumber").value(TEST_WCSS_ORDER_NUMBER))
				.andExpect(MockMvcResultMatchers.jsonPath("$.orderIdCanQuickCopy").value(TEST_ORDER_ID));
	}

	@Test
	void that_submitOrder__returns422_whenError() throws Exception, AtWinXSException {

		// given that submitOrder returns a valid SubmitOrderResponse object
		when(mockSubmitOrderService.submitOrder(any(SessionContainer.class), anyString()))
				.thenReturn(submitOrderResponseFailed);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when submitOrder is called, expect 422 status and error message
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.SUBMIT_ORDER).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
						.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(SFTranslationTextConstants.ORDER_SUBMIT_COULD_NOT_COMPLETE_ERR));
	}
	
	@Test
	void that_submitOrder_justify_returnsExpected() throws Exception, AtWinXSException {

		// given that submitOrder returns a valid SubmitOrderResponse object
		when(mockSubmitOrderService.submitOrder(any(SessionContainer.class), anyString(), anyString()))
				.thenReturn(submitOrderResponseSuccess);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when submitOrder is called, expect 200 status and Sales Ref#, WCSS
		// OrderNumber, Order ID in JSON
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SUBMIT_ORDER_JUSTIFICATION)
				.contentType(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON)
		        .content("{ \"justificationText\": \"justificationText\"}\n"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING))
				.andExpect(MockMvcResultMatchers.jsonPath("$.salesReferenceNumber").value(TEST_SALE_REFERENCE_NUMBER))
				.andExpect(MockMvcResultMatchers.jsonPath("$.wcssOrderNumber").value(TEST_WCSS_ORDER_NUMBER))
				.andExpect(MockMvcResultMatchers.jsonPath("$.orderIdCanQuickCopy").value(TEST_ORDER_ID));
	}
	
	@Test
	void that_submitOrder_justify_returns422_whenError() throws Exception, AtWinXSException {

		// given that submitOrder returns a valid SubmitOrderResponse object
		when(mockSubmitOrderService.submitOrder(any(SessionContainer.class), anyString(), anyString()))
				.thenReturn(submitOrderResponseFailed);

		String TEST_ENCRIPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		// when submitOrder is called, expect 422 status and error message
		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.SUBMIT_ORDER_JUSTIFICATION)
				.contentType(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
						.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON)
						.content("{ \"justificationText\": \"justificationText\"}\n"))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(SFTranslationTextConstants.ORDER_SUBMIT_COULD_NOT_COMPLETE_ERR));
	}

	private SubmitOrderResponse getSubmitOrderResponseSuccessTest() {

		submitOrderResponseSuccess = new SubmitOrderResponse();
		submitOrderResponseSuccess.setSuccess(true);
		submitOrderResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		submitOrderResponseSuccess.setSalesReferenceNumber(TEST_SALE_REFERENCE_NUMBER);
		submitOrderResponseSuccess.setWcssOrderNumber(TEST_WCSS_ORDER_NUMBER);
		submitOrderResponseSuccess.setOrderIdCanQuickCopy(TEST_ORDER_ID);
		return submitOrderResponseSuccess;
	}

	private SubmitOrderResponse getSubmitOrderResponseFailedTest() {

		submitOrderResponseFailed = new SubmitOrderResponse();
		submitOrderResponseFailed.setSuccess(false);
		submitOrderResponseFailed.setMessage(SFTranslationTextConstants.ORDER_SUBMIT_COULD_NOT_COMPLETE_ERR);
		return submitOrderResponseFailed;
	}
}
