/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         		JIRA #			Description
 *	--------    ---------------------	-----------		-----------------------------------------------------------
 *	12/20/23	S Ramachandran			CAP-45953		Controller for USPS validation
 *	12/22/23	S Ramachandran			CAP-46081		Integrated Service for USPS validation to Controller
 */

package com.rrd.c1ux.api.controllers.usps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.usps.USPSValidationRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.usps.USPSValidationService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("USPSValidationController")
@Tag(name = "USPS Validation APIs")
public class USPSValidationController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(USPSValidationController.class);

	private final USPSValidationService mUSPSValidationService;
	
	/**
	 * @param tokenReader   {@link TokenReader}
	 * @param sessionReader {@link CPSessionReader}
	 */
	protected USPSValidationController(TokenReader tokenReader, CPSessionReader sessionReader, 
			USPSValidationService uspsValidationService) {
	
		super(tokenReader, sessionReader);
		mUSPSValidationService = uspsValidationService; 
	}

	@Override
	protected int getServiceID() {

		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	//CAP-45953, CAP-46081
	@PostMapping(value = RouteConstants.VALIDATE_US_ADDRESS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate US Address")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<USPSValidationResponse> validateUSAddress(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody USPSValidationRequest request) throws AtWinXSException {

		logger.debug("In validateUSAddress()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		USPSValidationResponse response = mUSPSValidationService.validateUSPS(sc,request);	
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}