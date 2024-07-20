/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/25/23				S Ramachandran			CAP-38157					Initial Version, Submit Order API
 *	06/08/23				S Ramachandran			CAP-41235					Review order - Submit order 
 *	09/05/23				Satishkumar A      		CAP-42763					C1UX BE - Order Routing Justification Text Submit Order 
 */

package com.rrd.c1ux.api.controllers.checkout;

import javax.servlet.http.HttpServletRequest;

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
import com.rrd.c1ux.api.models.checkout.SubmitOrderRequest;
import com.rrd.c1ux.api.models.checkout.SubmitOrderResponse;
import com.rrd.c1ux.api.services.checkout.SubmitOrderService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("SubmitOrderController")
@Tag(name = "Submit Order API")
public class SubmitOrderController extends BaseCPApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubmitOrderController.class);

	private final SubmitOrderService submitOrderService;
	
	protected SubmitOrderController(TokenReader tokenReader, CPSessionReader sessionReader, SubmitOrderService mSubmitOrderService) {
		super(tokenReader, sessionReader);
		submitOrderService = mSubmitOrderService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	// CAP-38157
	@GetMapping(value = RouteConstants.SUBMIT_ORDER, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Submit order in checkout, once review completed")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<SubmitOrderResponse> submitOrder(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			HttpServletRequest httpServletRequest) throws AtWinXSException {

		LOGGER.debug("In submitorder()");
				
		SessionContainer sc = getSessionContainer(ttsession);

		SubmitOrderResponse response = submitOrderService.submitOrder(sc,httpServletRequest.getRemoteAddr());

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-42763
	@PostMapping(value = RouteConstants.SUBMIT_ORDER_JUSTIFICATION, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Submit order in checkout, once review completed")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<SubmitOrderResponse> submitOrder(
			@RequestHeader(value = "ttsession", required = false) String ttsession, 
			HttpServletRequest httpServletRequest, @RequestBody SubmitOrderRequest submitOrderRequest ) throws AtWinXSException {

		LOGGER.debug("In submitorder()");

		SessionContainer sc = getSessionContainer(ttsession);

		SubmitOrderResponse response = submitOrderService.submitOrder(sc,httpServletRequest.getRemoteAddr(),submitOrderRequest.getJustificationText());

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

}