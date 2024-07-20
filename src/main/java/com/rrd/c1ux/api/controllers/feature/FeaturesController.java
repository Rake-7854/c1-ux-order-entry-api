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
 *  07/31/23	S Ramachandran		CAP-41784       Get featured catalog items
 *  09/08/23	N Caceres			CAP-42856		Featured items should return standard catalog item response
 */
package com.rrd.c1ux.api.controllers.feature;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.mappers.CatalogItemsMapper;
import com.rrd.c1ux.api.models.favorite.FeaturedCatalogItemsRequest;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("FeatureController")
@Tag(name = "Features apis - oe/getfeatured")
public class FeaturesController extends BaseCPApiController implements ServletContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeaturesController.class);

	protected final CatalogItemRetriveServices mCatalogItemServices;
	private ServletContext servletContext;
	private CatalogItemsMapper mCatalogItemsMapper;
	
	protected FeaturesController(TokenReader tokenReader, CPSessionReader sessionReader, CatalogItemRetriveServices mCatalogItemServices, 
			CatalogItemsMapper catalogItemsMapper){
		super(tokenReader, sessionReader);
		this.mCatalogItemServices = mCatalogItemServices;
		mCatalogItemsMapper = catalogItemsMapper;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@PostMapping(value = RouteConstants.GET_FEATURED_ITEMS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get featured items")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public CatalogItemsResponse retrieveFeaturedCatalogItemValues(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody FeaturedCatalogItemsRequest request) throws AtWinXSException {
		LOGGER.debug("In retrieveFeaturedCatalogItemValues()");
		OEItemSearchCriteriaSessionBean criteria = new OEItemSearchCriteriaSessionBean();
		SessionContainer sc = getSessionContainer(ttsession);
		ApplicationSession appSession = sc.getApplicationSession();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		CatalogItemsResponse items = null;
		
		try {
			items = mCatalogItemServices.getFeaturedCatalogItems(servletContext, appSession, volatileSession, oeSession, criteria, request);
			items.setResetPageNumber("N");
		} catch (Exception e) {
			LOGGER.error("Exception thrown in call to getFeaturedCatalogItems()" + e.getMessage(), e);
		}
		return mCatalogItemsMapper.getCatalogItems(items);
	}
	
}