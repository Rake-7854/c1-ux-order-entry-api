/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  08/09/23	S Ramachandran		CAP-42746       Order Routing Information for an Order Search
 *  08/22/23	S Ramachandran		CAP-43234		BE - Order Routing Information For Order Search
 *  08/16/23	Satishkumar A		CAP-42745		C1UX API - Routing Information For Justification Section on Review Order Page
 *  08/30/23	Satishkumar A		CAP-43283		C1UX BE - Routing Information For Justification Section on Review Order Page 
 */
package com.rrd.c1ux.api.controllers.routing;

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
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationRequest;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationResponse;
import com.rrd.c1ux.api.models.routing.RoutingInformationResponse;
import com.rrd.c1ux.api.services.orders.ordersearch.OrderSearchService;
import com.rrd.c1ux.api.services.routing.RoutingInformationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("RoutingController")
@Tag(name = "Routing Information API - Routing Details, Routing Settings and Routing Reasons.")
public class RoutingController extends BaseCPApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoutingController.class);
	
	private OrderSearchService mOrderSearchService;
	private RoutingInformationService mRoutingInformationService;

	
	protected RoutingController(TokenReader tokenReader, CPSessionReader sessionReader,OrderSearchService orderSearchService, RoutingInformationService routingInformationService) {
		super(tokenReader, sessionReader);
		mOrderSearchService = orderSearchService;
		mRoutingInformationService = routingInformationService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.ORDER_SEARCH_ROUTING_DETAILS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Routing Information for an Order")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<OrderRoutingInformationResponse> getOrderRoutingInformation(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession,
			@RequestBody OrderRoutingInformationRequest request) throws AtWinXSException {

		LOGGER.debug("In getOrderRoutingInformation()");

		SessionContainer sc = getSessionContainer(ttsession);

		OrderRoutingInformationResponse response = mOrderSearchService.getOSRoutingInfos(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	@GetMapping(value = RouteConstants.GET_ROUTING_INFORMATION, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Routing Information - Routing Settings and Routing Reasons")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<RoutingInformationResponse> getRoutingInformation(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getRoutingInformation()");
		SessionContainer sc = getSessionContainer(ttsession);
		
		RoutingInformationResponse response = mRoutingInformationService.getRoutingInformation(sc);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}


}