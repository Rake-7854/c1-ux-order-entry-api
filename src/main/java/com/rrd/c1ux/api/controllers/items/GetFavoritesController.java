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
 * 	04/08/22	Krishna Natarajan	CAP-33867	    Created as per the requiremet to fetch the favorite items
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 *	06/05/23	N Caceres			CAP-39051		Resolve concurrency issues in GetFavorites Controller
 *  09/25/23    M Sakthi			CAP-38861		C1UX BE - API Standardization - Get favorite items for C1UX conversion to standard catalog item response
 */

package com.rrd.c1ux.api.controllers.items;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.items.FavouriteItems;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.rrd.c1ux.api.services.items.GetFavoritesService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Krishna Natarajan
 * 
 * This is a controller class to get the list of favorite items
 *
 */
@RestController("GetFavoritesController")
public class GetFavoritesController extends BaseCPApiController implements ServletContextAware {

@Autowired
private ServletContext servletContext;

@Autowired
GetFavoritesService sgetFavoriteService;

private static final Logger logger = LoggerFactory.getLogger(GetFavoritesController.class);

private FavoriteItemsMapper mfavoriteItems;

/**
 * @param tokenReader {@link TokenReader}
 * @param sessionReader {@link CPSessionReader}
 * @param favoriteItems {@link FavoriteItemsMapper}
 */
protected GetFavoritesController(TokenReader tokenReader, CPSessionReader sessionReader, FavoriteItemsMapper favoriteItems, GetFavoritesService getFavoriteService) 
{
        super(tokenReader, sessionReader);
        mfavoriteItems = favoriteItems;
        sgetFavoriteService = getFavoriteService;
}

/**
 *@return a constant ORDERS_SERVICE_ID AtWinXSConstant {@link AtWinXSConstant}
 */

/**
* setting up of ServletContext
*/
@Override
public void setServletContext(ServletContext servletContext) {
	this.servletContext = servletContext;
}

/**
 * @param ttsession {@link String}
 * @return List of FavouriteItems {@link FavouriteItems}
 * @throws AtWinXSException
 */

@GetMapping(value=RouteConstants.GET_FAVORITE , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
@Tag(name = "User Favorite Item API")
@Operation(summary = "Get favorite items")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
public ResponseEntity<CatalogItemsResponse> getFavoriteItems(@RequestHeader(value = "ttsession", required=false) String ttsession) throws AtWinXSException {
    logger.debug("In getFavoriteItems");

    // CAP-39051 Resolve concurrency issues in GetFavorites Controller 
	SessionContainer sc = getSessionContainer(ttsession);
	CatalogItemsResponse response = sgetFavoriteService.getFavoriteItemsProcessed(sc,mfavoriteItems,servletContext);	
	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	
}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
}
