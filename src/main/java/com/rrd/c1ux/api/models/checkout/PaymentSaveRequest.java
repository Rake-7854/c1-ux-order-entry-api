/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By			JIRA#			Description
 * 	--------	--------------		----------		------------------------------------------------
 *	09/20/23	S Ramachandran		CAP-43668		Request for Payment Information Save in checkout
 */
package com.rrd.c1ux.api.models.checkout;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="PaymentSaveRequest", description = "Request Class for Save Payment", type = "object")
public class PaymentSaveRequest {
	
	@Schema(name = "selectedPaymentMethod", description = "Selected Payment Method which is one of the values "
			+ "listed in payment method codes", type = "string", example="V1111" )
	@Size(max=5, min=2)
	private String selectedPaymentMethod;

	@Schema(name = "selectedCardOption", description = "Selected credit card type which is one of the values listed in card type codes", 
			type = "string", allowableValues = { "A", "M", "V" }, example="V" )
	@Size(max=1, min=1)
	private String selectedCardOption;
	
	@Schema(name = "paymentName", description = "Name of the credit card owner", type = "string", example="Joseph")
	@Size(max=25, min=1)
	private String paymentName;

	@Schema(name = "cardNumber", description = "Encoded credit card number, blank if selected payment method is CC or "
			+ "masked credit card number showing last 4 digits if selected payment method is existing credit card", 
			type = "string", example="************1234")
	@Size(max=16, min=1)
	private String cardNumber;
	
	@Schema(name = "creditCardToken", description = "Credit card token which received from credit card frame", 
			type = "string", example="1111111111111234")
	@Size(max=16)
	private String creditCardToken;
	
	@Schema(name = "selectedExpirationDateMonth", description = "Selected month for the credit card expiration date, "
			+ "value must be from 01 to 12", type = "string",  
			allowableValues = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" }, example="01" )
	@Size(min=2, max=2)
	private String selectedExpirationDateMonth;

	@Schema(name = "selectedExpirationDateYear", description = "Selected month for the credit card expiration date, "
			+ "value must be from current year up to a total of 10 years", 
			type = "string", example="2025")
	@Size(min=4, max=4)
	private String selectedExpirationDateYear;

	@Schema(name = "saveCreditCardFlag", description = "Flag to indicate if credit card needed to be saved for future use", 
			type = "boolean", example="false")
	private boolean saveCreditCardFlag;

	@Schema(name = "emailCreditReceiptFlag", description = "Flag to indicate if email receipt is needed", 
			type = "boolean", example="false")
	private boolean emailCreditReceiptFlag;

	@Schema(name = "emailCreditReceiptName", description = "Email for the credit card receipt", 
			type = "string", example="JKRowling@warnerbros.com")
	@Size(max=16, min=1)
	private String emailCreditReceiptName;
	
}
