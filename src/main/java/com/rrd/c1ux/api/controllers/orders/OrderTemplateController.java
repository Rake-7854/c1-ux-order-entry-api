/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/19/24	N Caceres			CAP-48584				Initial Version
 *	04/23/24	C Codina			CAP-48623				C1UX API - Create API to show list of template orders that the user can order from
 *	04/23/24	S Ramachandran		CAP-48136				Added controller API handler to delete an order template
 *	04/25/24	Satishkumar A		CAP-48716				Added controller API handler to load template order 
 *	04/25/24	S Ramachandran		CAP-48889				Integrated service method in API handler to delete an order template	
 */
package com.rrd.c1ux.api.controllers.orders;

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
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.TemplateOrderListResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateResponse;
import com.rrd.c1ux.api.services.orders.ordertemplate.OrderTemplateService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("OrderTemplateController")
@Tag(name = "Order Template API")
public class OrderTemplateController extends BaseCPApiController {
	
	private final OrderTemplateService orderTemplateService;
	
	protected OrderTemplateController(TokenReader tokenReader, CPSessionReader sessionReader, OrderTemplateService orderTemplateService) {
		super(tokenReader, sessionReader);
		this.orderTemplateService = orderTemplateService;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateController.class);
	
	// CAP-48584
	@PostMapping(value = RouteConstants.SAVE_ORDER_TEMPLATE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save Order Template")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<SaveOrderTemplateResponse> saveOrderTemplate(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SaveOrderTemplateRequest request) throws AtWinXSException {
		LOGGER.debug("In saveOrderTemplate()");
		SessionContainer sc = getSessionContainer(ttsession);
		SaveOrderTemplateResponse response = orderTemplateService.saveOrderTemplate(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	//CAP-48623
	@GetMapping(value = RouteConstants.GET_TEMPLATE_ORDER_LIST, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Template Order List")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<TemplateOrderListResponse> getTemplateOrderList(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
		LOGGER.debug("In getTemplateOrderList");
		SessionContainer sc = getSessionContainer(ttsession);
		TemplateOrderListResponse response = orderTemplateService.getTemplateOrderList(sc);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
		
	}
	
	// CAP-48136
	@PostMapping(value = RouteConstants.DELETE_ORDER_TEMPLATE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Delete order template")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<DeleteTemplateResponse> deleteOrderTemplate(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody DeleteTemplateRequest request) throws AtWinXSException {

		LOGGER.debug("In deleteOrderTemplate()");
		SessionContainer sc = getSessionContainer(ttsession);
		
		DeleteTemplateResponse response = orderTemplateService.deleteOrderTemplate(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-48582
	@PostMapping(value = RouteConstants.LOAD_SAVE_ORDER_TEMPLATE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save Order Template")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<LoadSaveOrderTemplateResponse> loadSaveOrderTemplate(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody LoadSaveOrderTemplateRequest request) throws AtWinXSException {

		LOGGER.debug("In saveOrderTemplate()");
		SessionContainer sc = getSessionContainer(ttsession);
		LoadSaveOrderTemplateResponse response = orderTemplateService.getOrderTemplateDetails(sc,request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-48716
	@PostMapping(value = RouteConstants.LOAD_TEMPLATE_ORDER, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Load template order")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<UseOrderTemplateResponse> loadTemplateOrder(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UseOrderTemplateRequest request) throws AtWinXSException {

		LOGGER.debug("In loadTemplateOrder()");
		SessionContainer sc = getSessionContainer(ttsession);
		UseOrderTemplateResponse response = orderTemplateService.loadTemplateOrder(sc,request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
