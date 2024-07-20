/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/18/23				L De Leon				CAP-41552					Initial Version
 *	08/01/23				L De Leon				CAP-42519					Modified copyRecentOrder() to add call to the service to copy order
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
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderResponse;
import com.rrd.c1ux.api.services.orders.copy.CopyRecentOrderService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CopyRecentOrderController")
@Tag(name = "Copy Recent Order API")
public class CopyRecentOrderController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CopyRecentOrderController.class);

	// CAP-42519
	private final CopyRecentOrderService copyRecentService;

	protected CopyRecentOrderController(TokenReader tokenReader, CPSessionReader sessionReader, CopyRecentOrderService copyRecentService) {
		super(tokenReader, sessionReader);
		this.copyRecentService = copyRecentService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.COPY_RECENT_ORDER, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Copy an order that is already submitted")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CopyRecentOrderResponse> copyRecentOrder(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody CopyRecentOrderRequest request) throws AtWinXSException {

		LOGGER.debug("In copyRecentOrder()");

		// sc will be passed in to the service call to copy order
		SessionContainer sc = getSessionContainer(ttsession);

		CopyRecentOrderResponse response = copyRecentService.copyRecentOrder(sc, request); // CAP-42519

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}