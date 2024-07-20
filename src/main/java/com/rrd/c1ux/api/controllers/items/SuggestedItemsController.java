
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By         DTS#           	 Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *  05/29/24    M Sakthi			CAP-49694		C1UX API - Create new API /api/items/suggesteditems to return companion items for items in cart or an order
 */

package com.rrd.c1ux.api.controllers.items;

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
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.SuggestedItemsRequest;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.suggesteditems.SuggestedItemsService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Sakthivel Mottaiyan
 * 
 * This is a controller class to get the list of suggested items
 *
 */
@RestController("SuggestedItemsController")
public class SuggestedItemsController extends BaseCPApiController{

@Autowired
SuggestedItemsService suggestedItemsService;

private static final Logger logger = LoggerFactory.getLogger(SuggestedItemsController.class);

/**
 * @param tokenReader {@link TokenReader}
 * @param sessionReader {@link CPSessionReader}
 * @param favoriteItems {@link FavoriteItemsMapper}
 */
protected SuggestedItemsController(TokenReader tokenReader, CPSessionReader sessionReader) 
{
        super(tokenReader, sessionReader);
}

/**
 *@return a constant ORDERS_SERVICE_ID AtWinXSConstant {@link AtWinXSConstant}
 */

/**
* setting up of ServletContext
*/

/**
 * @param ttsession {@link String}
 * @return List of SuggestedItems {@link SuggestedItems}
 * @throws AtWinXSException
 * @throws InvocationTargetException 
 * @throws IllegalAccessException 
 */

@PostMapping(value=RouteConstants.GET_SUGGESTED_ITEMS , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
@Tag(name = "Suggested Item API")
@Operation(summary = "Get Suggested items")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
public ResponseEntity<CatalogItemsResponse> getSuggestedItems(@RequestHeader(value = "ttsession", required=false) String ttsession
		,@RequestBody SuggestedItemsRequest request) throws AtWinXSException, IllegalAccessException, InvocationTargetException{
    logger.debug("In getSuggestedItems");
    SessionContainer sc = getSessionContainer(ttsession);
    boolean isIgnoreSessionSave=false;
    if(!Util.isBlank(request.getVendorItemNumber())  && !Util.isBlank(request.getOrderLineNumber())) {
    	isIgnoreSessionSave=true;
    }
	CatalogItemsResponse response = suggestedItemsService.getSuggestedItems(sc,request.getVendorItemNumber(),request.getItemNumber(),request.getOrderLineNumber(), isIgnoreSessionSave);	
	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	
}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
}
