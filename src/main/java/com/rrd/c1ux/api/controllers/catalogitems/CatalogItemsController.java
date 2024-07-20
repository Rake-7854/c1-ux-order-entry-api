/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	04/11/22	Sakthi M		CAP-33688- Catalog Items	Initial creation
 *  04/11/22	Sakthi M		CAP-34514- Get Variants		Get variants for user settings
 *	08/29/22	A Boomker		CAP-35537					Make session optional on all API calls
 *	09/01/22	Sumit Kumar		CAP-35732					API service to cache selected item navigation info from nav-bar calls
 *	09/08/2022  Sakthi M        CAP-35437  				    Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
 *	09/26/22	A Boomker		CAP-35610					Fix URL for catalog repeat load
 *  10/12/22    Sakthi M        CAP-35938					Modify 4 API responses for repeat and original item search AND category load and repeat 
 *  														category load to return flag indicating reset to page 1
 *  06/27/23	C Codina 		CAP-40833					Address Low Priority Security Hotspots Identified by SonarQube - Dev Only
 *	10/11/23	N Caceres		CAP-44349					API service to retrieve HTML text assigned to the selected category
 *	02/08/24	Krishna Natarajan CAP-47074					set the criteria from session to perform items search
 * 	04/15/24	Krishna Natarajan	CAP-48534				Added a new controller getCatalogMessages
 */


package com.rrd.c1ux.api.controllers.catalogitems;

import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

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
import org.springframework.web.context.ServletContextAware;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CacheResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogMessageResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogNavigationCacheRequest;
import com.rrd.c1ux.api.models.catalogitems.CatalogSearchResultsResponse;
import com.rrd.c1ux.api.models.catalogitems.mappers.CatalogItemsMapper;
import com.rrd.c1ux.api.rest.catalog.CatalogItemRequest;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CatalogItemsController")
public class CatalogItemsController extends BaseCPApiController implements ServletContextAware{

	private static final Logger logger = LoggerFactory.getLogger(CatalogItemsController.class);

	private CatalogItemsMapper mCatalogItemsMapper;
	private ServletContext servletContext;
	
	@Autowired
	private CatalogItemRetriveServices mCatalogItemServices;
	

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	protected CatalogItemsController(TokenReader tokenReader, CPSessionReader sessionReader,
			CatalogItemsMapper catalogItemsMapper,CatalogItemRetriveServices catalogItemRetriveServices) {

		super(tokenReader, sessionReader);
		mCatalogItemsMapper = catalogItemsMapper;
		mCatalogItemServices=catalogItemRetriveServices;
	}
	
	@PostMapping(value=RouteConstants.CATALOG_ITEMS , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "items/catalogitems")
	@Operation(summary = "Get catalog items for the user session in context")
	public CatalogItemsResponse retrieveCatalogItemValues(@RequestHeader(required=false) String ttsessionid,
			@RequestBody CatalogItemRequest request) throws AtWinXSException{
		SimpleDateFormat formatter = new SimpleDateFormat(OrderEntryConstants.CP_SHIPPING_LIST_DATE_FORMAT);
		logger.info("Got into retrieveCatalogItemValues at " + formatter.format(new java.util.Date()));
	
		SessionContainer sc = getSessionContainer(ttsessionid);
		
		ApplicationSession appSession = sc.getApplicationSession();
	
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		OEItemSearchCriteriaSessionBean criteria = oeSession.getOESessionBean().getSearchCriteriaBean();//CAP-47074	

		CatalogItemsResponse item=null;
		try {
			if (request.getSelectedCategoryId() <= 1) // CAP-35610
			{
				item = mCatalogItemServices.getCatalogItemsRepeat(sc,servletContext,criteria);
				//CAP-35938 - Flag to set item load when repeat item search.
				item.setResetPageNumber("N"); 
			}
			else
			{
				item = mCatalogItemServices.getCatalogItems(servletContext, appSession, volatileSession,
				    oeSession, criteria, false, request);
				//CAP-35938 - Flag to set item load when original item search.
				item.setResetPageNumber("Y"); 
			}
		} catch (Exception e) {
			//CAP-40833 - Replaced printStrackTrace with logger
			logger.error("Exception thrown in call to getCatalogItems()" + e.getMessage(), e);
		}
		logger.info("About to return catalog load from controller " + formatter.format(new java.util.Date()));
		return mCatalogItemsMapper.getCatalogItems(item);
	}
	
	//CAP-35732 Create API service to cache selected item navigation info from nav-bar calls 
	@PostMapping(value=RouteConstants.CACHE_CATALOG_UTILITY_NAVIGATION , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "items/cachenavigation")
	@Operation(summary = "cache selected item navigation infomation from nav-bar calls")
	public CacheResponse cacheNavigation (@RequestHeader(required=false) String ttsessionid,
			@RequestBody CatalogNavigationCacheRequest catalogNavigationCacheRequest) throws AtWinXSException{
	
		SessionContainer sc = getSessionContainer(ttsessionid);
		CacheResponse cacheResponse=null;
		try {
			cacheResponse = mCatalogItemServices.cacheNavigation(sc, catalogNavigationCacheRequest);
		} catch (Exception e) {
			//CAP-40833 - Replaced printStrackTrace with logger
			logger.error("Exception thrown in call to cacheNavigation()" + e.getMessage(), e);
		}
		return cacheResponse;
	}
	
	//CAP-35437-Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
	@PostMapping(value=RouteConstants.REPEAT_CATALOG_ITEMS , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "catalogitem/repeatItemSearch")
	@Operation(summary = "Get catalog items for the user session in context")
	public CatalogItemsResponse getCatalogItems(@RequestHeader(required=false) String ttsessionid
			) throws AtWinXSException{
		SimpleDateFormat formatter = new SimpleDateFormat(OrderEntryConstants.CP_SHIPPING_LIST_DATE_FORMAT);
		logger.info("Got into repeat getCatalogItems() at " + formatter.format(new java.util.Date()));

		OEItemSearchCriteriaSessionBean criteria = new OEItemSearchCriteriaSessionBean();
	
		SessionContainer sc = getSessionContainer(ttsessionid);
		CatalogItemsResponse item=null;
		try {
			item = mCatalogItemServices.getCatalogItemsRepeat(sc,servletContext,criteria);
			//CAP-35938 - Flag to set item load when repeat item search.
			item.setResetPageNumber("N");
		} catch (Exception e) {
			logger.info("About to return REPEAT catalog load from controller " + formatter.format(new java.util.Date()));
		}
		return mCatalogItemsMapper.getCatalogItems(item);
	}
	
	// CAP-44349
	@PostMapping(value=RouteConstants.GET_CATALOG_ITEMS , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "items/getcatalogitems")
	@Operation(summary = "Get catalog items and HTML content for the selected category ID")
	public ResponseEntity<CatalogSearchResultsResponse> getCatalogItems(@RequestHeader(required=false) String ttsessionid,
			@RequestBody CatalogItemRequest request) throws AtWinXSException{
		CatalogSearchResultsResponse response = new CatalogSearchResultsResponse();
		OEItemSearchCriteriaSessionBean criteria = new OEItemSearchCriteriaSessionBean();
		SessionContainer sc = getSessionContainer(ttsessionid);
		ApplicationSession appSession = sc.getApplicationSession();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		try {
			if (request.getSelectedCategoryId() >= 1)
			{
				response = mCatalogItemServices.doSearchWithCategory(servletContext, appSession, volatileSession, oeSession, criteria, false, request);
			}
		} catch (Exception e) {
			logger.error("Exception thrown in call to getCatalogItems()" + e.getMessage(), e);
		}
		
		if (Util.isBlank(response.getMessage())) {
			response.setSuccess(true);
		}
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext=servletContext;
	}
	
	// CAP-48534
	@GetMapping(value = RouteConstants.GET_CATALOG_MESSAGES, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "items/getcatalogmessages")
	@Operation(summary = "Get catalog messages")
	public ResponseEntity<CatalogMessageResponse> getCatalogMessages(
			@RequestHeader(required = false) String ttsessionid) throws AtWinXSException {
		SessionContainer sc = getSessionContainer(ttsessionid);
		ApplicationSession appSession = sc.getApplicationSession();
		CatalogMessageResponse response = mCatalogItemServices.getCatalogMessages(appSession);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

}
