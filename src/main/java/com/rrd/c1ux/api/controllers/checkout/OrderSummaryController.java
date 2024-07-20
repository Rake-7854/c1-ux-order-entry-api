/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/23/23				A Boomker				CAP-38155					Initial Version
 *	04/03/23				A Boomker				CAP-39512					Adding functional changes
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
import com.rrd.c1ux.api.models.checkout.OrderSummaryRequest;
import com.rrd.c1ux.api.models.checkout.OrderSummaryResponse;
import com.rrd.c1ux.api.services.checkout.OrderSummaryService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("OrderSummaryController")
@Tag(name = "Order Summary API calls")
public class OrderSummaryController extends BaseCPApiController{

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderSummaryController.class);

	OrderSummaryService orderSummaryService;

	protected OrderSummaryController(TokenReader tokenReader, CPSessionReader sessionReader, OrderSummaryService summaryService) {
		super(tokenReader, sessionReader);
		orderSummaryService = summaryService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.ORDER_SUMMARY_LOAD, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Load info for order summary modal")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<OrderSummaryResponse> getOrderSummaryInfo(@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody OrderSummaryRequest orderSummaryRequest) throws AtWinXSException {

		LOGGER.debug("In getOrderSummaryInfo()");
		SessionContainer sc = getSessionContainer(ttsession);

		OrderSummaryResponse response = orderSummaryService.populateSummaryFromOrder(null, orderSummaryRequest.isReview(), sc); // CAP-39512

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
