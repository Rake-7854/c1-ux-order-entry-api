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
 *	03/15/24	L De Leon			CAP-47894		Added test method to check when force CC option is on
 */
package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.checkout.PaymentInformationResponse;
import com.rrd.c1ux.api.models.checkout.PaymentSaveRequest;
import com.rrd.c1ux.api.models.checkout.PaymentSaveResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.entity.OrderDetailsBillingInfo;
import com.rrd.custompoint.orderentry.entity.ProfileCreditCard;
import com.wallace.atwinxs.framework.dao.ConnectionFactory;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.util.NameValuePair;
import com.wallace.atwinxs.orderentry.util.OrderDetailsBillingInfoFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

class PaymentInformationServiceImplTests extends BaseOEServiceTest {


	private static final int TEST_ORDER_ID = 604534;
	private static final int TEST_PROFILE_NR = 12345;
	private static final String SELECT_OPTION = "Select Card Type...";
	private static final String AMEX_OPTION = "American Express";
	private static final String MC_OPTION = "Master Card";
	private static final String VISA_OPTION = "Visa";
	private static final String JAN_OPTION = "01";
	private static final String FEB_OPTION = "02";
	private static final String YEAR1_VAL = "23";
	private static final String YEAR2_VAL = "24";
	private static final String YEAR1_OPTION = "2023";
	private static final String YEAR2_OPTION = "2024";
	private static final String AMEX_LAST_FOUR = "1234";
	private static final String TEST_NAME = "John Doe";
	private static final String TEST_EMAIL = "john.doe@rrd.com";
	private static final String SAVED_CC_CD = "A1234";

	private PaymentInformationResponse paymentInformationResponse;
	private List<ProfileCreditCard> profileCreditCards = new ArrayList<>();
	private List<NameValuePair> cardOptions = new ArrayList<>();
	private List<NameValuePair> monthOptions = new ArrayList<>();
	private List<NameValuePair> yearOptions = new ArrayList<>();

	//CAP-44048:Start

	private PaymentSaveRequest paymentSaveRequest;
	private PaymentSaveResponse paymentSaveResponse;

	private static final String TEST_NEWCC_PAYMETHOD = "CC";
	private static final String TEST_STOREDCC_PAYMETHOD = "A2732";
	private static final String TEST_IV_PAYMETHOD = "IV";
	private static final String TEST_INVALID_PAYMETHOD = "INVLD";


	private static final String TEST_AMEX_CARD_OPTION = "A";
	private static final String TEST_MASTER_CARD_OPTION = "M";
	private static final String TEST_VISA_CARD_OPTION = "V";

	private static final String TEST_PAYMENT_NAME = "John Doe";
	private static final String TEST_AECC_NUMBER = "************2732";
	private static final String TEST_AECC_LAST_FOUR = "2732";

	private static final String TEST_INVALID_AECC_NUMBER = "1111111111112732";
	private static final String TEST_AECC_TOKEN = "1111111111112732";
	private static final String TEST_EXP_MONTH = "12";
	private static final String TEST_EXP_YEAR = String.valueOf(LocalDate.now().getYear());
	private static final boolean TEST_CC_SAVE_FLAG = true;
	private static final boolean TEST_CC_NOSAVE_FLAG = false;
	private static final String TEST_CC_EMAIl = "test@connecone.rrd.com";
	private static final boolean TEST_CC_EMAIl_FLAG = true;
	private static final boolean TEST_CC_NOEMAIl_FLAG = false;

	private static final String TEST_BLANK = "";
	private static final String TEST_EXCEED25_CHARLENGTH = "V1111 EXCEED 25 CHARCTERS LENGTH.";
	private static final String TEST_EXCEED650_CHARLENGTH = TEST_EXCEED25_CHARLENGTH.repeat(26);

	private static final String TEST_INVALID_MIN_EXP_MONTH = "09";
	private static final String TEST_INVALID_MIN_EXP_YEAR = "2023";

	private static final String TEST_INVALID_MAX_EXP_MONTH = "01";
	private static final String TEST_INVALID_MAX_EXP_YEAR = String.valueOf(LocalDate.now().plusYears(10L).getYear());

	//CAP-44048:End

	@Mock
	private EntityObjectMap mockEntityObjectMap;

	@Mock
	private CreditCardAuthorization mockCreditCardAuthorization;

	@Mock(extraInterfaces = { OrderDetailsBillingInfoFormBean.class })
	private OrderDetailsBillingInfo mockBillingInfo;

	@Mock
	private ProfileCreditCard mockProfileCreditCard;

	@Mock
	private NameValuePair monthOption;

	@Mock
	private NameValuePair yearOption;

	@InjectMocks
	private PaymentInformationServiceImpl service;

	@BeforeEach
	void setup() throws Exception {


		paymentInformationResponse = null;
		paymentSaveResponse = null;
		cardOptions.add(new NameValuePair(SELECT_OPTION, AtWinXSConstant.EMPTY_STRING));
		cardOptions.add(new NameValuePair(AMEX_OPTION, OrderEntryConstants.CREDIT_CARD_TYPE_AMEX));
		cardOptions.add(new NameValuePair(MC_OPTION, OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD));
		cardOptions.add(new NameValuePair(VISA_OPTION, OrderEntryConstants.CREDIT_CARD_TYPE_VISA));
		monthOptions.add(new NameValuePair(JAN_OPTION, JAN_OPTION));
		monthOptions.add(new NameValuePair(FEB_OPTION, FEB_OPTION));
		yearOptions.add(new NameValuePair(YEAR1_OPTION, YEAR1_VAL));
		yearOptions.add(new NameValuePair(YEAR2_OPTION, YEAR2_VAL));
	}

	@Test
	void that_getPaymentInformation_success() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);
		service = Mockito.spy(service);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doReturn(AMEX_OPTION).when(mockProfileCreditCard).getCardTypeOption();
		doReturn(AMEX_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		doReturn(SAVED_CC_CD).when(mockOEOrderSession).getCardTypeAndNumber();
		doReturn(cardOptions).when(mockBillingInfo).getCardTypeOptions();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		doReturn(monthOptions).when(mockBillingInfo).getCardTypeExpMonthOptions();
		doReturn(yearOptions).when(mockBillingInfo).getCardTypeExpYearOptions();
		doReturn(TEST_NAME).when(mockBillingInfo).getCardHolderName();
		doReturn(AMEX_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NR);
		when(mockUserSettings.getReceiptEmailOption()).thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceipt())
				.thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceiptAddress()).thenReturn(TEST_EMAIL);

		paymentInformationResponse = service.getPaymentInformation(mockSessionContainer);

		assertNotNull(paymentInformationResponse);
		assertTrue(paymentInformationResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(paymentInformationResponse.getMessage()));
	}

	void that_getPaymentInformation_has_forceCCOption_return_success() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);
		service = Mockito.spy(service);

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockOEOrderSession.isForceCCOptionAllocation()).thenReturn(true);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doReturn(AMEX_OPTION).when(mockProfileCreditCard).getCardTypeOption();
		doReturn(AMEX_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		doReturn(SAVED_CC_CD).when(mockOEOrderSession).getCardTypeAndNumber();
		doReturn(cardOptions).when(mockBillingInfo).getCardTypeOptions();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		doReturn(monthOptions).when(mockBillingInfo).getCardTypeExpMonthOptions();
		doReturn(yearOptions).when(mockBillingInfo).getCardTypeExpYearOptions();
		doReturn(TEST_NAME).when(mockBillingInfo).getCardHolderName();
		doReturn(AMEX_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();
		when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NR);

		paymentInformationResponse = service.getPaymentInformation(mockSessionContainer);

		assertNotNull(paymentInformationResponse);
		assertTrue(paymentInformationResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(paymentInformationResponse.getMessage()));
	}

	@Test
	void that_getPaymentInformation_returnErrorMessage_hasNoActiveOrderFail() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		service = Mockito.spy(service);

		doReturn(false).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		paymentInformationResponse = service.getPaymentInformation(mockSessionContainer);

		assertNotNull(paymentInformationResponse);
		assertFalse(paymentInformationResponse.isSuccess());
	}

	@Test
	void that_getPaymentInformation_returnErrorMessage_throwExceptionFail() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		service = Mockito.spy(service);

		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());
		doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		paymentInformationResponse = service.getPaymentInformation(mockSessionContainer);

		assertNotNull(paymentInformationResponse);
		assertFalse(paymentInformationResponse.isSuccess());
	}

	@Test
	void that_getPaymentInformation_returnAccessNotAllowedMessage() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(false);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getPaymentInformation(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	//CAP-44048:Start

	@Test
	void that_saveNewCCAEPaymentInformation_success() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());

		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();

		doReturn(TEST_AECC_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		doReturn(TEST_PAYMENT_NAME).when(mockBillingInfo).getCardHolderName();
		doReturn(TEST_AECC_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();
		//when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NR);
		//when(mockUserSettings.getReceiptEmailOption()).thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceipt())
				.thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceiptAddress()).thenReturn(TEST_CC_EMAIl);

		when(mockCreditCardAuthorization.authorizeCreditCard(any(), any(), any(), any(), any())).thenReturn(true);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getPaymetricTransactionID())
		.thenReturn("1234567890");

		try (MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class))
			{
				mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
				paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest());

		}

		assertNotNull(paymentSaveResponse);
		assertTrue(paymentSaveResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(paymentSaveResponse.getMessage()));
	}

	@Test
	void that_saveNewMasterCCPaymentInformation_success() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedCardOption(TEST_MASTER_CARD_OPTION);
		paymentSaveRequest.setEmailCreditReceiptFlag(TEST_CC_NOEMAIl_FLAG);
		paymentSaveRequest.setSaveCreditCardFlag(TEST_CC_NOSAVE_FLAG);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());

		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();

		doReturn(TEST_AECC_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		doReturn(TEST_PAYMENT_NAME).when(mockBillingInfo).getCardHolderName();
		doReturn(TEST_AECC_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();
		//when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NR);
		//when(mockUserSettings.getReceiptEmailOption()).thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceipt())
				.thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceiptAddress()).thenReturn(TEST_CC_EMAIl);

		when(mockCreditCardAuthorization.authorizeCreditCard(any(), any(), any(), any(), any())).thenReturn(true);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getPaymetricTransactionID())
		.thenReturn("1234567890");

		try (MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class))
			{
				mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
				paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);

		}

		assertNotNull(paymentSaveResponse);
		assertTrue(paymentSaveResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(paymentSaveResponse.getMessage()));
	}



	@Test
	void that_saveNewVISACCPaymentInformation_success() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedCardOption(TEST_VISA_CARD_OPTION);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());

		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		//when(mockBillingInfo.isSaveCardforFutureUse()).thenReturn(true);

		doReturn(TEST_AECC_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		doReturn(TEST_PAYMENT_NAME).when(mockBillingInfo).getCardHolderName();
		doReturn(TEST_AECC_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();
		//when(mockAppSessionBean.getProfileNumber()).thenReturn(TEST_PROFILE_NR);
		//when(mockUserSettings.getReceiptEmailOption()).thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceipt())
				.thenReturn(RouteConstants.YES_FLAG);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getCcEmailReceiptAddress()).thenReturn(TEST_CC_EMAIl);

		when(mockCreditCardAuthorization.authorizeCreditCard(any(), any(), any(), any(), any())).thenReturn(true);
		when(((OrderDetailsBillingInfoFormBean) mockBillingInfo).getPaymetricTransactionID())
		.thenReturn("1234567890");

		try (MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class))
			{
				mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
				paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);

		}

		assertNotNull(paymentSaveResponse);
		assertTrue(paymentSaveResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(paymentSaveResponse.getMessage()));
	}

	@Test
	void that_saveStoredCCPaymentInformation_success() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		//doNothing().when(service).validatePaymentMethod(any(PaymentSaveRequest.class), any(PaymentSaveResponse.class),
		//		eq(mockAppSessionBean), any(OrderDetailsBillingInfo.class));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());

		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();

		doReturn(TEST_AECC_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		doReturn(TEST_PAYMENT_NAME).when(mockBillingInfo).getCardHolderName();
		doReturn(TEST_AECC_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getStoredCCPaymentSaveRequest());
			assertNotNull(paymentSaveResponse);
			assertTrue(paymentSaveResponse.isSuccess());
			assertTrue(Util.isBlankOrNull(paymentSaveResponse.getMessage()));
		}
	}

	@Test
	void that_saveIVPaymentInformation_success() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		//doNothing().when(service).validatePaymentMethod(any(PaymentSaveRequest.class), any(PaymentSaveResponse.class),
		//		eq(mockAppSessionBean), any(OrderDetailsBillingInfo.class));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();

		doNothing().when(mockBillingInfo).setOrderID(anyInt());
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);

		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);
		try (

			MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class)) {

				mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);

				paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getIVPaymentSaveRequest());
				assertNotNull(paymentSaveResponse);
				assertTrue(paymentSaveResponse.isSuccess());
				assertTrue(Util.isBlankOrNull(paymentSaveResponse.getMessage()));
			}
	}

	@Test
	void that_saveNewCCPaymentInformation_returnErrorMessage_hasNoActiveOrderFail() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();

		service = Mockito.spy(service);
		doReturn(false).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest());
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
	}

	@Test
	void that_saveNewCCPaymentInformation_returnErrorMessage_throwExceptionFail() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest());
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
	}

	@Test
	void that_saveNewCCPaymentInformation_returnAccessNotAllowedMessage() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest());
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_saveNewCCPaymentInformation_returnAccessNotAllowedMessage1() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest());
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}


	@Test
	void that_validateBillingInfo_IsBlank_failed() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
			.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest_isBlank());
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}


	@Test
	void that_validateBillingInfo_isGreaterThanMaxChar_failed() throws Exception {

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, getNewCCPaymentSaveRequest_isGreaterThanMaxChar());
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_validateBillingInfo_CCEmail_Isblank_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setEmailCreditReceiptName(TEST_BLANK);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
			.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_validateBillingInfo_CCEmail_isGreaterThanMaxChar_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setEmailCreditReceiptName(TEST_EXCEED650_CHARLENGTH);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_saveStoredCCPaymentInformation_CCEmail_Isblank_failed() throws Exception {

		paymentSaveRequest = getStoredCCPaymentSaveRequest();
		paymentSaveRequest.setEmailCreditReceiptName(TEST_BLANK);

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		//doNothing().when(service).validatePaymentMethod(any(PaymentSaveRequest.class), any(PaymentSaveResponse.class),
		//		eq(mockAppSessionBean), any(OrderDetailsBillingInfo.class));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());

		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();

		doReturn(TEST_AECC_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		//doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		//doReturn(TEST_PAYMENT_NAME).when(mockBillingInfo).getCardHolderName();
		//doReturn(TEST_AECC_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		//doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		//doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

				mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
				mockTranslationTextTag
				.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
						mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
				.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
			assertNotNull(paymentSaveResponse);
			assertFalse(paymentSaveResponse.isSuccess());
			assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
		}
	}

	@Test
	void that_saveStoredCCPaymentInformation_CCEmail_isGreaterThanMaxChar_failed() throws Exception {

		paymentSaveRequest = getStoredCCPaymentSaveRequest();
		paymentSaveRequest.setEmailCreditReceiptName(TEST_EXCEED650_CHARLENGTH);

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		//doNothing().when(service).validatePaymentMethod(any(PaymentSaveRequest.class), any(PaymentSaveResponse.class),
		//		eq(mockAppSessionBean), any(OrderDetailsBillingInfo.class));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		doReturn(TEST_ORDER_ID).when(mockVolatileSessionBean).getOrderId();
		doNothing().when(mockBillingInfo).setOrderID(anyInt());

		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();

		doReturn(TEST_AECC_LAST_FOUR).when(mockProfileCreditCard).getCardLastFourDigit();
		doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockProfileCreditCard).getCardType();
		//doReturn(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX).when(mockBillingInfo).getCreditCardType();
		//doReturn(TEST_PAYMENT_NAME).when(mockBillingInfo).getCardHolderName();
		//doReturn(TEST_AECC_LAST_FOUR).when(mockBillingInfo).getCreditCardLast4();
		//doReturn(JAN_OPTION).when(mockBillingInfo).getCreditCardExpMonth();
		//doReturn(YEAR2_VAL).when(mockBillingInfo).getCreditCardExpYear();

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

				mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
				mockTranslationTextTag
				.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
						mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
				.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
			assertNotNull(paymentSaveResponse);
			assertFalse(paymentSaveResponse.isSuccess());
			assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
		}
	}


	@Test
	void that_validateBillingInfo_PaymentMethod_Isblank_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_INVALID_PAYMETHOD);

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
			.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}


	@Test
	void that_validateBillingInfo_PaymentMethod_isGreaterThanMaxChar_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_EXCEED25_CHARLENGTH);

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_validateBillingInfo_PaymentMethod_isInvalid_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_INVALID_PAYMETHOD);

		// add mock Objects for sessions
		setUpModuleSession();
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
			.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_validateBillingInfo_InvalidCardNumber_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setCardNumber(TEST_INVALID_AECC_NUMBER);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_CARD_NUM_ERROR, null))
			.thenReturn(TranslationTextConstants.TRANS_NM_ENTER_VALID_CARD_NUM_ERROR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_validateBillingInfo_InvalidMinExpMonthYear_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedExpirationDateMonth(TEST_INVALID_MIN_EXP_MONTH);
		paymentSaveRequest.setSelectedExpirationDateYear(TEST_INVALID_MIN_EXP_YEAR);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR, null))
			.thenReturn(TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	@Test
	void that_validateBillingInfo_InvalidMaxExpMonthYear_failed() throws Exception {

		paymentSaveRequest = getNewCCPaymentSaveRequest();
		paymentSaveRequest.setSelectedExpirationDateMonth(TEST_INVALID_MAX_EXP_MONTH);
		paymentSaveRequest.setSelectedExpirationDateYear(TEST_INVALID_MAX_EXP_YEAR);

		// add mock Objects for sessions
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getProcurementCardOption()).thenReturn(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED);
		profileCreditCards.add(mockProfileCreditCard);

		service = Mockito.spy(service);
		doReturn(true).when(service).validateOrder(any(BaseResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockBillingInfo).when(mockEntityObjectMap).getEntity(OrderDetailsBillingInfo.class, null);
		when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
		doReturn(profileCreditCards).when(mockBillingInfo).getProfileCreditCards();
		doNothing().when(mockBillingInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSession);

		try(MockedStatic<ConnectionFactory> mockConnectionFactory = Mockito.mockStatic(ConnectionFactory.class);
			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockConnectionFactory.when(() -> ConnectionFactory.createConnection()).thenReturn(mockConnection);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR, null))
			.thenReturn(TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR);

			paymentSaveResponse = service.savePaymentInformation(mockSessionContainer, paymentSaveRequest);
		}
		assertNotNull(paymentSaveResponse);
		assertFalse(paymentSaveResponse.isSuccess());
		assertFalse(paymentSaveResponse.getFieldMessages().isEmpty());
	}

	private PaymentSaveRequest getNewCCPaymentSaveRequest_isGreaterThanMaxChar() {

		paymentSaveRequest = new PaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_NEWCC_PAYMETHOD);
		paymentSaveRequest.setSelectedCardOption(TEST_EXCEED25_CHARLENGTH);
		paymentSaveRequest.setPaymentName(TEST_EXCEED25_CHARLENGTH);
		paymentSaveRequest.setCardNumber(TEST_EXCEED25_CHARLENGTH);
		paymentSaveRequest.setCreditCardToken(TEST_EXCEED25_CHARLENGTH);
		paymentSaveRequest.setSelectedExpirationDateMonth(TEST_EXCEED25_CHARLENGTH);
		paymentSaveRequest.setSelectedExpirationDateYear(TEST_EXCEED25_CHARLENGTH);
		paymentSaveRequest.setEmailCreditReceiptName(TEST_EXCEED650_CHARLENGTH);
		paymentSaveRequest.setEmailCreditReceiptFlag(true);
		paymentSaveRequest.setSaveCreditCardFlag(true);
		return paymentSaveRequest;
}

	private PaymentSaveRequest getNewCCPaymentSaveRequest_isBlank() {

		paymentSaveRequest = new PaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_NEWCC_PAYMETHOD);
		paymentSaveRequest.setSelectedCardOption("A");
		paymentSaveRequest.setPaymentName(TEST_BLANK);
		paymentSaveRequest.setCardNumber(TEST_BLANK);
		paymentSaveRequest.setCreditCardToken(TEST_BLANK);
		paymentSaveRequest.setSelectedExpirationDateMonth(TEST_BLANK);
		paymentSaveRequest.setSelectedExpirationDateYear(TEST_BLANK);
		paymentSaveRequest.setEmailCreditReceiptName(TEST_BLANK);
		paymentSaveRequest.setEmailCreditReceiptFlag(true);
		paymentSaveRequest.setSaveCreditCardFlag(true);
		return paymentSaveRequest;
	}

	private PaymentSaveRequest getNewCCPaymentSaveRequest() {

		paymentSaveRequest = new PaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_NEWCC_PAYMETHOD);
		paymentSaveRequest.setSelectedCardOption(TEST_AMEX_CARD_OPTION);
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

	private PaymentSaveRequest getStoredCCPaymentSaveRequest() {

		paymentSaveRequest = new PaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_STOREDCC_PAYMETHOD);
		paymentSaveRequest.setEmailCreditReceiptName(TEST_CC_EMAIl);
		paymentSaveRequest.setEmailCreditReceiptFlag(TEST_CC_EMAIl_FLAG);
		return paymentSaveRequest;
	}

	private PaymentSaveRequest getIVPaymentSaveRequest() {

		paymentSaveRequest = new PaymentSaveRequest();
		paymentSaveRequest.setSelectedPaymentMethod(TEST_IV_PAYMETHOD);
		return paymentSaveRequest;
	}

	//CAP-44048:End
}