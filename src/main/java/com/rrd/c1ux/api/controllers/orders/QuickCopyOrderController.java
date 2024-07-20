/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/25/23				L De Leon				CAP-38158					Initial Version
  *	06/22/23				L De Leon				CAP-41373					Modified to add call to the service to copy order
 */
package com.rrd.c1ux.api.controllers.orders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderResponse;
import com.rrd.c1ux.api.services.orders.copy.QuickCopyOrderService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("QuickCopyOrderController")
@Tag(name = "Quick Copy Order API")
public class QuickCopyOrderController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickCopyOrderController.class);

	// CAP-41373
	@Autowired
	QuickCopyOrderService quickCopyService;

	protected QuickCopyOrderController(TokenReader tokenReader, CPSessionReader sessionReader) {
		super(tokenReader, sessionReader);
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.QUICK_COPY_ORDER, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Copy an order that has just been submitted")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<QuickCopyOrderResponse> quickCopyOrder(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody QuickCopyOrderRequest request) throws AtWinXSException {

		LOGGER.debug("In quickCopyOrder()");
		// CAP-41373
		SessionContainer sc = getSessionContainer(ttsession);

		QuickCopyOrderResponse response = quickCopyService.quickCopyOrder(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}