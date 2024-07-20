/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/09/24	Satishkumar A		CAP-49204				C1UX API - Create new API to save EFD information
 *	05/10/24	S Ramachandran		CAP-49205				Added controller API handler to return style information for EFD order
 *	05/13/24	N Caceres			CAP-49151				Added API to get EFD Options
 *	05/13/24	S Ramachandran		CAP-49326				Integrated service method in API handler to get order style info
 *	05/16/24	Satishkumar A		CAP-49311				C1UX BE - Create new API to save EFD information
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
import com.rrd.c1ux.api.models.checkout.EFDOptionsResponse;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationRequest;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationResponse;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationRequest;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationResponse;
import com.rrd.c1ux.api.services.checkout.EFDDestinationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("EFDDestinationController")
@Tag(name = "Electronic File Delivery(EFD) API")
public class EFDDestinationController  extends BaseCPApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EFDDestinationController.class);
	
	private final EFDDestinationService efdDestinationService;
	
	protected EFDDestinationController(TokenReader tokenReader, CPSessionReader sessionReader,
			EFDDestinationService efdDestinationService) {
		super(tokenReader, sessionReader);
		this.efdDestinationService = efdDestinationService;
		
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	//CAP-49311
	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}
	
	//CAP-49204
	@PostMapping(value=RouteConstants.SAVE_EFD_INFORMATION, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save EFD information")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SaveEfdInformationResponse> saveEfdInformation(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID, 
			@RequestBody SaveEfdInformationRequest saveEfdInformationRequest) throws AtWinXSException {

		LOGGER.debug("In saveEfdInformation()");
		SessionContainer sc = getSessionContainer(sessionID);
		SaveEfdInformationResponse response = efdDestinationService.saveEfdInformation(sc, saveEfdInformationRequest);


		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-49205, CAP-49326
	@PostMapping(value = RouteConstants.GET_EFD_STYLEINFO, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get EFD Style Information of an Order")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<EFDStyleInformationResponse> getEfdStyleInformation(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody EFDStyleInformationRequest request) throws AtWinXSException {

		LOGGER.debug("In getEfdStyleInformation()");
		SessionContainer sc = getSessionContainer(ttsession);

		EFDStyleInformationResponse response = efdDestinationService.getEfdStyleInformationForOrder(sc,request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	
	// CAP-49151
	@GetMapping(value = RouteConstants.GET_EFD_OPTIONS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get EFD Options for new section during Checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<EFDOptionsResponse> getEfdOptions(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getEfdOptions()");
		SessionContainer sc = getSessionContainer(ttsession);

		EFDOptionsResponse response = efdDestinationService.getEFDOptions(sc);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
