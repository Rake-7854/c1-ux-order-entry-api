/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *	09/19/23	L De Leon			CAP-43665		Initial Version
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.util.NameValuePair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PaymentInformationResponse", description = "Response Class for loading Payment Information", type = "object")
public class PaymentInformationResponse extends BaseResponse {

	@Schema(name = "paymentMethods", description = "List of payment method codes and their corresponding display text", type = "array")
	private List<NameValuePair<String>> paymentMethods;

	@Schema(name = "selectedPaymentMethod", description = "Selected payment method which is one of the values listed in payment method codes", type = "string")
	private String selectedPaymentMethod;

	@Schema(name = "cardOptions", description = "List of credit card types and their corresponding display text", type = "array")
	private List<NameValuePair<String>> cardOptions;

	@Schema(name = "selectedCardOption", description = "Selected credit card type which is one of the values listed in card type codes", type = "string")
	private String selectedCardOption;

	@Schema(name = "paymentName", description = "Name of the credit card owner", type = "string")
	private String paymentName;

	@Schema(name = "cardNumber", description = "Encoded credit card number, blank if selected payment method is CC or masked credit card number showing last 4 gigits if selected payment method is existing credit card", type = "string")
	private String cardNumber;

	@Schema(name = "expirationDateMonths", description = "List of months for the credit card expiration date", type = "array")
	private List<NameValuePair<String>> expirationDateMonths;

	@Schema(name = "expirationDateYears", description = "List of years for the credit card expiration date, starts with current year and go for 10 years total", type = "array")
	private List<NameValuePair<String>> expirationDateYears;

	@Schema(name = "selectedExpirationDateMonth", description = "Selected month for the credit card expiration date, value must be from 01 to 12", type = "string")
	private String selectedExpirationDateMonth;

	@Schema(name = "selectedExpirationDateYear", description = "Selected month for the credit card expiration date, value must be from current year up to a total of 10 years", type = "string")
	private String selectedExpirationDateYear;

	@Schema(name = "showSaveCreditCardFlag", description = "Show or hide save credit card for future use checkbox", type = "boolean")
	private boolean showSaveCreditCardFlag;

	@Schema(name = "saveCreditCardFlag", description = "Flag to check if credit card needed to be saved for future use", type = "boolean")
	private boolean saveCreditCardFlag;

	@Schema(name = "showEmailReceipt", description = "Show or hide email receipt checkbox", type = "boolean")
	private boolean showEmailReceipt;

	@Schema(name = "emailCreditReceiptSetting", description = "Require email receipt checkbox or not", type = "boolean")
	private boolean emailCreditReceiptSetting;

	@Schema(name = "emailCreditReceiptFlag", description = "Flag to check if email receipt is needed", type = "boolean")
	private boolean emailCreditReceiptFlag;

	@Schema(name = "emailCreditReceiptName", description = "Email for the credit card receipt", type = "string")
	private String emailCreditReceiptName;

	@Schema(name = "savedCreditCardName", description = "Name of the saved credit card holder", type = "string")
	private String savedCreditCardName;

	@Schema(name = "savedCreditLast4Digits", description = "Last 4 digits of the saved credit card", type = "string")
	private String savedCreditLast4Digits;

	@Schema(name = "savedCreditExpiration", description = "Month/Year of the saved credit card's expiration date", type = "string")
	private String savedCreditExpiration;

	@Schema(name = "savedCreditCardType", description = "Card type of the saved credit card", type = "string")
	private String savedCreditCardType;
}