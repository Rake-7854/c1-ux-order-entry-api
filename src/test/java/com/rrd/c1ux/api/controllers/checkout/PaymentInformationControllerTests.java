/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *	09/22/23	L De Leon			CAP-44032		Initial Version
 *	09/22/23	S Ramachandran		CAP-44048		Save Payment Information 
 */
package com.rrd.c1ux.api.controllers.checkout;

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
import com.rrd.c1ux.api.models.checkout.PaymentInformationResponse;
import com.rrd.c1ux.api.models.checkout.PaymentSaveRequest;
import com.rrd.c1ux.api.models.checkout.PaymentSaveResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class PaymentInformationControllerTests extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private PaymentInformationResponse paymentInformationResponseSuccess;
	private PaymentInformationResponse paymentInformationResponseFailed;
	public static final String EXPECTED_422MESSAGE = "Generic Error";
	
	//CAP-44048:Starts
	private static final String TEST_NEWCC_PAYMETHOD = "CC";
	private static final String TEST_AE_CARD_OPTION = "A";
	private static final String TEST_PAYMENT_NAME = "John Doe";
	private static final String TEST_AECC_NUMBER = "************2732";
	private static final String TEST_AECC_TOKEN = "1111111111112732";
	private static final String TEST_EXP_MONTH = "12";
	private static final String TEST_EXP_YEAR = "2023";
	private static final String TEST_CC_EMAIl = "test@connecone.rrd.com";
	private static final boolean TEST_CC_EMAIl_FLAG = true;
	private static final boolean TEST_CC_SAVE_FLAG = true;
	
	private PaymentSaveRequest paymentSaveRequest;
	private PaymentSaveResponse paymentSaveResponseSuccess;
	private PaymentSaveResponse paymentSaveResponseFailed;
	//CAP-44048:Ends

	@BeforeEach
	void setUp() throws Exception {

		setupBaseMockSessions();

		paymentInformationResponseSuccess = getPaymentInformationResponseSuccessTest();
		paymentInformationResponseFailed = getPaymentInformationResponseFailedTest();
		
		//CAP-44048:Starts
		paymentSaveRequest = getPaymentSaveRequest();
		paymentSaveResponseSuccess = getPaymentSaveResponseSuccessTest();
		paymentSaveResponseFailed = getPaymentSaveResponseFailedTest();
		//CAP-44048:Ends

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);

		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
	}

	@Test
	void that_getDeliveryInformation_returnsExpected() throws Exception {

		// when paymentInformation returns a valid PaymentInformationResponse object
		when(mockPaymentInformationService.getPaymentInformation(any(SessionContainer.class)))
				.thenReturn(paymentInformationResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

		// when paymentInformation is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_PAYMENT_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_getDeliveryInformation_returnsErrorMessage_whenError() throws Exception {

		// when paymentInformation returns a valid PaymentInformationResponse object
		when(mockPaymentInformationService.getPaymentInformation(any(SessionContainer.class)))
				.thenReturn(paymentInformationResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

		// when paymentInformation is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.GET_PAYMENT_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}

	private PaymentInformationResponse getPaymentInformationResponseSuccessTest() {

		paymentInformationResponseSuccess = new PaymentInformationResponse();
		paymentInformationResponseSuccess.setSuccess(true);
		paymentInformationResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return paymentInformationResponseSuccess;
	}

	private PaymentInformationResponse getPaymentInformationResponseFailedTest() {

		paymentInformationResponseFailed = new PaymentInformationResponse();
		paymentInformationResponseFailed.setSuccess(false);
		paymentInformationResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return paymentInformationResponseFailed;
	}
	
	//CAP-44048:Starts
	@Test
	void that_savePaymentInformation_returnsExpected() throws Exception {

		// when savePaymentInformation returns a valid PaymentSaveResponse object
		when(mockPaymentInformationService.savePaymentInformation(any(SessionContainer.class),
				any(PaymentSaveRequest.class)))
				.thenReturn(paymentSaveResponseSuccess);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(paymentSaveRequest);
	
		// when savePaymentInformation is called, expect 200 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_PAYMENT_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void that_savePaymentInformation_returnsErrorMessage_whenError() throws Exception {

		// when savePaymentInformation returns a UnProcessable Error PaymentSaveResponse object
		when(mockPaymentInformationService.savePaymentInformation(any(SessionContainer.class),
				any(PaymentSaveRequest.class)))
				.thenReturn(paymentSaveResponseFailed);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestString = ow.writeValueAsString(paymentSaveRequest);


		// when savePaymentInformation is called, expect 422 status
		mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.SAVE_PAYMENT_INFO).accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.content(requestString).characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)).andExpect(MockMvcResultMatchers
						.jsonPath("$.message").value(EXPECTED_422MESSAGE));
	}
	
	private PaymentSaveRequest getPaymentSaveRequest() {
		
		paymentSaveRequest = new PaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_NEWCC_PAYMETHOD);
		paymentSaveRequest.setSelectedCardOption(TEST_AE_CARD_OPTION);
		paymentSaveRequest.setPaymentName(TEST_PAYMENT_NAME);
		paymentSaveRequest.setCardNumber(TEST_AECC_NUMBER);
		paymentSaveRequest.setCreditCardToken(TEST_AECC_TOKEN);
		paymentSaveRequest.setSelectedExpirationDateMonth(TEST_EXP_MONTH);
		paymentSaveRequest.setSelectedExpirationDateYear(TEST_EXP_YEAR);
		paymentSaveRequest.setEmailCreditReceiptName(TEST_CC_EMAIl);
		paymentSaveRequest.setEmailCreditReceiptFlag(TEST_CC_EMAIl_FLAG);
		paymentSaveRequest.setSaveCreditCardFlag(TEST_CC_SAVE_FLAG);
		
		return paymentSaveRequest;
	}
	
	private PaymentSaveResponse getPaymentSaveResponseSuccessTest() {

		paymentSaveResponseSuccess = new PaymentSaveResponse();
		paymentSaveResponseSuccess.setSuccess(true);
		paymentSaveResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return paymentSaveResponseSuccess;
	}

	private PaymentSaveResponse getPaymentSaveResponseFailedTest() {

		paymentSaveResponseFailed = new PaymentSaveResponse();
		paymentSaveResponseFailed.setSuccess(false);
		paymentSaveResponseFailed.setMessage(EXPECTED_422MESSAGE);
		return paymentSaveResponseFailed;
	}
	//CAP-44048:Ends
}