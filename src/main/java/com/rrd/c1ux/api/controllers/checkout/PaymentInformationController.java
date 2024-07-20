/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *	09/19/23	L De Leon			CAP-43665		Initial Version
 *	09/20/23	S Ramachandran		CAP-43668		Added handling for Payment Information Save in checkout
 *	09/22/23	L De Leon			CAP-44032		Added service call to load payment information
 *	09/22/23	S Ramachandran		CAP-44048		BE added service method call for payment Information Save in checkout
 */
package com.rrd.c1ux.api.controllers.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.checkout.PaymentInformationResponse;
import com.rrd.c1ux.api.models.checkout.PaymentSaveRequest;
import com.rrd.c1ux.api.models.checkout.PaymentSaveResponse;
import com.rrd.c1ux.api.services.checkout.PaymentInformationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("PaymentInformationController")
@Tag(name = "Payment Information API")
public class PaymentInformationController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentInformationController.class);

	private final PaymentInformationService paymentInformationService;

	protected PaymentInformationController(TokenReader tokenReader, CPSessionReader sessionReader, PaymentInformationService paymentInformationService) {
		super(tokenReader, sessionReader);
		this.paymentInformationService = paymentInformationService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@GetMapping(value = RouteConstants.GET_PAYMENT_INFO, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Load payment information")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<PaymentInformationResponse> getPaymentInformation(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getPaymentInformation()");

		// sc will be passed in to the service call to copy order
		SessionContainer sc = getSessionContainer(ttsession);

		PaymentInformationResponse response = paymentInformationService.getPaymentInformation(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	@PostMapping(value = RouteConstants.SAVE_PAYMENT_INFO, 
			produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save payment information")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<PaymentSaveResponse> savePaymentInformation(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody PaymentSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In savePaymentInformation()");

		SessionContainer sc = getSessionContainer(ttsession);
		PaymentSaveResponse response = paymentInformationService.savePaymentInformation(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
}