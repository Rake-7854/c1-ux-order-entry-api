/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	01/22/24	Satishkumar A		CAP-46407				C1UX API - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 *	02/01/24	Satishkumar A		CAP-46675				C1UX BE - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 *  02/20/24	T Harmon			CAP-46543				Added code for EOO
 */
package com.rrd.c1ux.api.controllers.eoo;

import javax.servlet.http.HttpServletRequest;

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
import com.rrd.c1ux.api.models.eoo.ValidateCheckoutResponse;
import com.rrd.c1ux.api.models.shoppingcart.SaveSelectedAttributesRequest;
import com.rrd.c1ux.api.models.shoppingcart.SaveSelectedAttributesResponse;
import com.rrd.c1ux.api.services.eoo.EnforceOnOrderingService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("EnforceOnOrderingController")
@Tag(name = "Enforce On Ordering API")
public class EnforceOnOrderingController extends BaseCPApiController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EnforceOnOrderingController.class);
	
	private final EnforceOnOrderingService enforceOnOrderingService;
	
	protected EnforceOnOrderingController(TokenReader tokenReader, CPSessionReader sessionReader, EnforceOnOrderingService mEnforceOnOrderingService) {
		super(tokenReader, sessionReader);
		this.enforceOnOrderingService = mEnforceOnOrderingService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	//CAP-46543
	@PostMapping(value=RouteConstants.SAVE_EOO_ATTRIBUTES, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save EOO Attributes")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SaveSelectedAttributesResponse> saveEooAttributes(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID, 
			@RequestBody SaveSelectedAttributesRequest sAttributeRequest) throws AtWinXSException {

		LOGGER.debug("In saveEooAttributes()");
		SaveSelectedAttributesResponse response = new SaveSelectedAttributesResponse();
		
		SessionContainer sc = getSessionContainer(sessionID);
		
		try {
		response = enforceOnOrderingService.saveEooAttributes(sc, sAttributeRequest);
		}catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getLocalizedMessage());
		}

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	@GetMapping(value = RouteConstants.GET_VALIDATE_EOO_CHECKOUT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<ValidateCheckoutResponse> validateCheckout(
			@RequestHeader(value = "ttsession", required = false) String ttsession, HttpServletRequest httpServletRequest) throws AtWinXSException {

		LOGGER.debug("In validateCheckout()");
		ValidateCheckoutResponse response = new ValidateCheckoutResponse();
		
		SessionContainer sc = getSessionContainer(ttsession);
		try {
		response = enforceOnOrderingService.validateCheckout(sc, httpServletRequest);
		}catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getLocalizedMessage());
		}

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
