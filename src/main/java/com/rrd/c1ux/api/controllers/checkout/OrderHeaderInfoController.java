/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/14/23				A Salcedo				CAP-38152					Initial Version
 *	03/29/23				A Salcedo				CAP-39396					Added service method.
 *	05/08/23				A Boomker				CAP-38153					Added save of order header info
 *	05/17/23				A Boomker				CAP-40687					Implement save
 */
package com.rrd.c1ux.api.controllers.checkout;

import java.lang.reflect.InvocationTargetException;

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
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoRequest;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoResponse;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveRequest;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveResponse;
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

@RestController("OrderHeaderInfoController")
@Tag(name = "Order Header Info APIs")
public class OrderHeaderInfoController extends BaseCPApiController{

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderHeaderInfoController.class);

	@Autowired
	OrderHeaderInfoService service;

	protected OrderHeaderInfoController(TokenReader tokenReader, CPSessionReader sessionReader) {
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

	@PostMapping(value = RouteConstants.ORDER_HEADER_INFO_LOAD, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Order Header Info for Checkout Details")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<OrderHeaderInfoResponse> getOrderHeaderInfo(@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody OrderHeaderInfoRequest orderHeaderInfoRequest) throws AtWinXSException, IllegalAccessException, InvocationTargetException {

		LOGGER.debug("In getOrderHeaderInfo()");
		SessionContainer sc = getSessionContainer(ttsession);

		OrderHeaderInfoResponse response = service.getOrderHeaderInfo(sc, orderHeaderInfoRequest);//CAP-39396

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-38153 - add saving of order header info
	@PostMapping(value = RouteConstants.ORDER_HEADER_INFO_SAVE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save Order Header Info for Checkout Details")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<OrderInfoHeaderSaveResponse> saveOrderHeaderInfo(@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody OrderInfoHeaderSaveRequest orderHeaderInfoRequest) throws AtWinXSException {

		LOGGER.debug("In saveOrderHeaderInfo()");
		SessionContainer sc = getSessionContainer(ttsession);

		OrderInfoHeaderSaveResponse response = service.saveOrderHeaderInfo(sc, orderHeaderInfoRequest);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

}
