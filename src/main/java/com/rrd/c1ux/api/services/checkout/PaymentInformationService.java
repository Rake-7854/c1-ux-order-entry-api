/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *	09/22/23	L De Leon			CAP-44032		Initial Version
 *	09/22/23	S Ramachandran		CAP-44048		added service method definition to validate/save payment info during checkout
 */
package com.rrd.c1ux.api.services.checkout;

import com.rrd.c1ux.api.models.checkout.PaymentInformationResponse;
import com.rrd.c1ux.api.models.checkout.PaymentSaveRequest;
import com.rrd.c1ux.api.models.checkout.PaymentSaveResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface PaymentInformationService {
	
	// CAP-44048
	final int PAY_PAYMENT_METHOD_MAX_SIZE_5 = 5;
	final int PAY_CARD_OPTION_MAX_SIZE_1 = 1;
	final int PAY_PAYMENT_NAME_MAX_SIZE_25 = 25;
	final int PAY_CC_TOKEN_MAX_SIZE_16 = 16;
	final int PAY_CC_EMAIL_MAX_SIZE_650 = 650;
	
	// CAP-44048
	final String PAY_PAYMENTMETHOD = "selectedPaymentMethod";
	final String PAY_CARDTYPE = "selectedCardOption";
	final String PAY_CARDHOLDERNAME = "paymentName";
	final String PAY_CARDNUMBER = "cardNumber";
	final String PAY_CARDTOKEN = "creditCardToken";
	final String PAY_EXPMONTH = "selectedExpirationDateMonth";
	final String PAY_EXPYEAR = "selectedExpirationDateYear";
	final String PAY_CCEMAILRECEIPTNAME = "emailCreditReceiptName";

	public PaymentInformationResponse getPaymentInformation(SessionContainer sc) throws AtWinXSException;
	public PaymentSaveResponse savePaymentInformation(SessionContainer sc, PaymentSaveRequest paymentSaveRequest) 
			throws AtWinXSException;
	
}

