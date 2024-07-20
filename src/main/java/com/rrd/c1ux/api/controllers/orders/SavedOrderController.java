/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	04/12/23	Satishkumar A      CAP-37497	   Saved Order – Getting the list of saved orders into the saved order page
 *	04/12/23	A Boomker			CAP-38160		Add API to get expansion detail for saved order
 *  04/19/23	Satishkumar A      CAP-39934	   BE - Saved Order – Getting the list of saved orders into the saved order page
 *	04/24/23	A Boomker			CAP-40002		Added handling of expansion API
 *	04/26/23	A Boomker			CAP-39340		Add API to delete saved order
 *	04/26/23	A Boomker			CAP-39341		Add API to resume saved order
 *	05/04/23    Satishkumar A   	CAP-37503       API Build - Save Order assuming all data already saved
 */
package com.rrd.c1ux.api.controllers.orders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.rrd.c1ux.api.models.orders.savedorders.SaveOrderRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SaveOrderResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderDeleteRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderDeleteResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrdersResponse;
import com.rrd.c1ux.api.services.orders.savedorders.SavedOrderService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Satishkumar A
 *
 */
@RestController("SavedOrderController")
@Tag(name = "Saved Order APIs")
public class SavedOrderController extends BaseCPApiController {

	@Autowired
	private SavedOrderService savedOrderService;

	private static final Logger logger = LoggerFactory.getLogger(SavedOrderController.class);

	protected SavedOrderController(TokenReader tokenReader, CPSessionReader sessionReader, SavedOrderService mSavedOrderService) {
		super(tokenReader, sessionReader);
		savedOrderService = mSavedOrderService;

	}

	//CAP-39934
	@GetMapping(value = RouteConstants.SAVED_ORDERS_LIST, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Saved orders list")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<SavedOrdersResponse> getSavedOrdersList(
            @RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

        logger.debug("In SavedOrderController - getSavedOrdersList() ");
        SessionContainer sc = getSessionContainer(ttsession);
        SavedOrdersResponse response = savedOrderService.getSavedOrdersList(sc);

        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@Override
	protected int getServiceID() {

		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	// CAP-38160
	@PostMapping(value = RouteConstants.SAVED_ORDER_EXPANSION, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Saved order expansion details")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<SavedOrderExpansionResponse> getSavedOrderExpansion(
            @RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SavedOrderExpansionRequest expandRequest) throws AtWinXSException {

        logger.debug("In SavedOrderController - getSavedOrderExpansion() ");
        SessionContainer sc = getSessionContainer(ttsession);
        SavedOrderExpansionResponse response = savedOrderService.createExpansionDetails(expandRequest, sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	//CAP-39934
    @Override
    protected boolean checkAccessAllowed(AppSessionBean asb) {

      return asb.hasService(getServiceID());
    }

	// CAP-39340
	@PostMapping(value = RouteConstants.SAVED_ORDER_DELETE, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Saved order delete request")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<SavedOrderDeleteResponse> deleteSavedOrder(
            @RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SavedOrderDeleteRequest request) throws AtWinXSException {

        logger.debug("In SavedOrderController - deleteSavedOrder() ");
        SessionContainer sc = getSessionContainer(ttsession);
        SavedOrderDeleteResponse response = savedOrderService.deleteOrder(request, sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	// CAP-39341
	@PostMapping(value = RouteConstants.SAVED_ORDER_RESUME, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Saved order resume request")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<SavedOrderResumeResponse> resumeSavedOrder(
            @RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SavedOrderResumeRequest request) throws AtWinXSException {

        logger.debug("In SavedOrderController - resumeSavedOrder() ");
        SessionContainer sc = getSessionContainer(ttsession);
        SavedOrderResumeResponse response = savedOrderService.resumeOrder(request, sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	//CAP-37503
	@PostMapping(value = RouteConstants.SAVE_ORDER, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Save order name")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<SaveOrderResponse> saveOrderName(
            @RequestHeader(value = "ttsession", required = false) String ttsession, 
            @RequestBody SaveOrderRequest request)  throws AtWinXSException {

        logger.debug("In SavedOrderController - saveOrderName() ");
        SessionContainer sc = getSessionContainer(ttsession);
        SaveOrderResponse response = savedOrderService.saveOrder(request, sc);

        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

}
