
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
 *  07/31/23    Satishkumar A      	CAP-33059       C1UX API - API Build - Favorite Toggle Call
 *  08/09/23 	Satishkumar A		CAP-42720		C1UX API - API Build - Favorite Toggle Call
 */
package com.rrd.c1ux.api.controllers.favorite;

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
import com.rrd.c1ux.api.models.favorite.ToggleFavoriteRequest;
import com.rrd.c1ux.api.models.favorite.ToggleFavoriteResponse;
import com.rrd.c1ux.api.services.favorite.ToggleFavoriteService;
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
@RestController("ToggleFavoriteController")
@Tag(name = "Favorite Toggle Call")
public class ToggleFavoriteController extends BaseCPApiController{

	private static final Logger LOGGER = LoggerFactory.getLogger(ToggleFavoriteController.class);
	
    private final ToggleFavoriteService toggleFavoriteService;

    
	protected ToggleFavoriteController(TokenReader tokenReader, CPSessionReader sessionReader, ToggleFavoriteService mtoggleFavoriteService) {
		super(tokenReader, sessionReader);
		toggleFavoriteService = mtoggleFavoriteService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}
	

	@PostMapping(value = RouteConstants.FAVORITE_TOGGLE_CALL, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Favorite Toggle Call")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<ToggleFavoriteResponse> setUnsetFavoriteItem(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody ToggleFavoriteRequest request) throws AtWinXSException {

		LOGGER.debug("In setUnsetFavoriteItem()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		
		ToggleFavoriteResponse response = toggleFavoriteService.toggleFavorite(sc, request.getCustomerItemNumber(), request.getVendorItemNumber());
	
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
}
