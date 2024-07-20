/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#				Description
 *	----------	-----------			------------		--------------------------------
 *	04/04/2024	L De Leon			CAP-48274			Initial Version
 */
package com.rrd.c1ux.api.controllers.checkout;

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
import com.rrd.c1ux.api.models.checkout.DateToDestinationRequest;
import com.rrd.c1ux.api.models.checkout.DateToDestinationResponse;
import com.rrd.c1ux.api.services.checkout.OrderHeaderInfoService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("DateToDestinationController")
@Tag(name = "Date To Destination API")
public class DateToDestinationController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DateToDestinationController.class);

	private final OrderHeaderInfoService orderHeaderInfoService;

	protected DateToDestinationController(TokenReader tokenReader, CPSessionReader sessionReader,
			OrderHeaderInfoService orderHeaderInfoService) {
		super(tokenReader, sessionReader);
		this.orderHeaderInfoService = orderHeaderInfoService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.DATE_TO_DESTINATION, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve banner message for remaining budget allocation")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<DateToDestinationResponse> doDateToDestination(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody DateToDestinationRequest request) throws AtWinXSException {

		LOGGER.debug("In doDateToDestination()");
		SessionContainer sc = getSessionContainer(ttsession);
		DateToDestinationResponse response = orderHeaderInfoService.doDateToDestination(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}