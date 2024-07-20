/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	11/27/23	L De Leon			CAP-44467				Initial Version
 *	12/04/23	Satishkumar A		CAP-45280				C1UX API - Set OOB Mode for CustomPoint session
 *	12/22/23 	Satishkumar A		CAP-45709				C1UX BE - Set OOB Mode for CustomPoint session
 */
package com.rrd.c1ux.api.controllers.orders;

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
import com.rrd.c1ux.api.models.orders.oob.OOBRequest;
import com.rrd.c1ux.api.models.orders.oob.OOBResponse;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchRequest;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchResponse;
import com.rrd.c1ux.api.services.orders.orderonbehalf.OrderOnBehalfService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("OrderOnBehalfController")
@Tag(name = "Order On Behalf API")
public class OrderOnBehalfController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderOnBehalfController.class);

	private final OrderOnBehalfService orderOnBehalfService;

	protected OrderOnBehalfController(TokenReader tokenReader, CPSessionReader sessionReader,
			OrderOnBehalfService orderOnBehalfService) {
		super(tokenReader, sessionReader);
		this.orderOnBehalfService = orderOnBehalfService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.GET_OOB_INFO, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve selected Order On Behalf user and/or search for Order On Behalf users")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<OrderOnBehalfSearchResponse> getOOBInfo(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody OrderOnBehalfSearchRequest request) throws AtWinXSException {

		LOGGER.debug("In getOOBInfo()");
		SessionContainer sc = getSessionContainer(ttsession);
		OrderOnBehalfSearchResponse response = orderOnBehalfService.getOOBInfo(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-45280 //CAP-45709
	@PostMapping(value = RouteConstants.OOB_TOGGLE_CALL, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Set Order for Self or OOB Mode for CustomPoint session")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<OOBResponse> getOOBToggleCall(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody OOBRequest request) throws AtWinXSException  {

		LOGGER.debug("In getOOBToggleCall()");
		SessionContainer sc = getSessionContainer(ttsession);
		OOBResponse response = orderOnBehalfService.setOrderForSelfOrOOBMode(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}