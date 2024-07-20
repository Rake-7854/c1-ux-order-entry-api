/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  04/08/22    S Ramachandran  CAP-33686   Initial Creation, added for Multiple Item Search (predictive search)
 *  04/08/22    S Ramachandran  CAP-34334   http method for getUniversalSearch() changed to POST from GET 
 *  06/08/22    S Ramachandran  CAP-34498   Added unit test cases for Universal Search Controller
 *  07/12/22    S Ramachandran  CAP-34884   Retrieve LineItems result for Universal Search 
 *	08/29/22	A Boomker		CAP-35537	Make session optional on all API calls
 *  09/08/2022  Sakthi M        CAP-35437   Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
 *  10/12/22    Sakthi M        CAP-35938	Modify 4 API responses for repeat and original item search AND category load and repeat 
 *  										category load to return flag indicating reset to page 1
 */

package com.rrd.c1ux.api.controllers.items;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.rrd.c1ux.api.models.items.UniversalSearchRequest;
import com.rrd.c1ux.api.models.items.UniversalSearchResponse;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.items.UniversalSearchService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("UniversalSearchController")
public class UniversalSearchController extends BaseCPApiController implements ServletContextAware  {

	private static final Logger logger = LoggerFactory.getLogger(UniversalSearchController.class);
	
	private ServletContext servletContext;
	
	private CatalogItemsMapper mCatalogItemsMapper;
	
	@Autowired
	private UniversalSearchService mService;
	
	@Autowired
	private CatalogItemRetriveServices mCatalogItemServices;

	@Override
	public void setServletContext(ServletContext servletContext) {
		
		this.servletContext = servletContext;
	}
	
	protected int getServiceID() {
		
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	//CAP-34498   added & assigned universalSearchService parameter in constructor 
	//CAP-34884   initialised catalogItemsMapper used to send response for getUniversalSearchLineitemResult()   
	protected UniversalSearchController(TokenReader tokenReader, CPSessionReader sessionReader,
			CatalogItemsMapper catalogItemsMapper, UniversalSearchService universalSearchService) {
		super(tokenReader, sessionReader);
		mCatalogItemsMapper = catalogItemsMapper;
		mService=universalSearchService;
	}

	
	/**
	 * 
	 * @param sessionID - {@link String}
	 * @param request - {@link String UniversalSearchRequest}
	 * @return - This will return UniversalSearchResponse which includes
 	 *           Array of search Item(s) & Status {@link UniversalSearchResponse}
	 * @throws AtWinXSException
	 */	
	@Tag(name = "items/universal-search")
	@PostMapping(value = RouteConstants.ITEMS_UNIVERSAL_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get item search term (predictive search) automatically does a search after 3 char are typed")
	public UniversalSearchResponse getUniversalSearch(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID,
			@RequestBody UniversalSearchRequest request) throws AtWinXSException {

		logger.debug("In UniversalSearchController");

		SessionContainer mainSession = getSessionContainer(sessionID);

		return mService.processUniversalSearch(mainSession, servletContext, request);

	}
	
	
	/**
	 * 
	 * @param sessionID - {@link String}
	 * @param request - {@link String UniversalSearchRequest}
	 * @return - This will return CatalogItemsResponse which includes
 	 *           List of Line items(s) as ItemThumbnailCellData {@link CatalogItemsResponse}
	 * @throws AtWinXSException
	 */	
	@Tag(name = "items/universal-search-lineitem-result")
	@PostMapping(value = RouteConstants.ITEMS_UNIVERSAL_SEARCH_LINEITEM_RESULT, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Line item results for universal search")
	public CatalogItemsResponse getUniversalSearchLineitemResult(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID,
			@RequestBody UniversalSearchRequest request) throws AtWinXSException {

		logger.debug("In UniversalSearchController - getUniversalSearchLineitemResult()");

		SessionContainer mainSession = getSessionContainer(sessionID);

		CatalogItemsResponse item=null;
		item = mCatalogItemServices.getUniversalSearchLineitemResult(mainSession, servletContext, request,false); //CAP-35938 added repeat

		return mCatalogItemsMapper.getCatalogItems(item);
		
	}
	
	
	/**
	 * 
	 * @param sessionID - {@link String}
	 * @return - This will return CatalogItemsResponse which includes
 	 * List of Line items(s) as ItemThumbnailCellData {@link CatalogItemsResponse}
	 * @throws AtWinXSException
	 */	
	@Tag(name = "items/repeatItemSearch")
	@PostMapping(value = RouteConstants.REPEAT_ITEM_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Line item results for universal search")
	public CatalogItemsResponse getUniversalSearchLineitemResult(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID) throws AtWinXSException {

		logger.debug("In UniversalSearchController - getUniversalSearchLineitemResult()");
		SessionContainer mainSession = getSessionContainer(sessionID);
		return mCatalogItemsMapper.getCatalogItems(mCatalogItemServices.repeatItemSearch(mainSession, servletContext));
		
	}

}
