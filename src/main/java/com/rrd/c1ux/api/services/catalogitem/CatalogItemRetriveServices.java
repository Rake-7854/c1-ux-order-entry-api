/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#										Description
 * 	--------	-----------		----------------------------------------	------------------------------
 *	09/19/22	A Boomker		CAP-35958									Efficiency improvements
 *	09/08/23	N Caceres		CAP-42856									Featured items should return standard catalog item response
 *  09/15/23	T Harmon		CAP-43670									Added new parm to getItemThumbnail
 *  10/06/23	N Caceres		CAP-44349									Retrieves the HTML text assigned to the selected category
 */
package com.rrd.c1ux.api.services.catalogitem;

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import com.rrd.c1ux.api.models.catalogitems.CacheResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogMessageResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogNavigationCacheRequest;
import com.rrd.c1ux.api.models.catalogitems.CatalogSearchResultsResponse;
import com.rrd.c1ux.api.models.favorite.FeaturedCatalogItemsRequest;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.models.items.UniversalSearchRequest;
import com.rrd.c1ux.api.rest.catalog.CatalogItemRequest;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

public interface CatalogItemRetriveServices {
	
	public CatalogItemsResponse getCatalogItems(ServletContext servletContext,
			ApplicationSession appSession, ApplicationVolatileSession volatileSession, OrderEntrySession oeSession,
			OEItemSearchCriteriaSessionBean criteria, boolean hideHiddenFeatured, CatalogItemRequest request) throws AtWinXSException;
	
	public CacheResponse cacheNavigation(SessionContainer sc, CatalogNavigationCacheRequest request) throws AtWinXSException;
	
	public CatalogItemsResponse getUniversalSearchLineitemResult(SessionContainer mainSession, 
			ServletContext servletContext, UniversalSearchRequest request,boolean repeat) throws AtWinXSException;
	
	public  List<ItemThumbnailCellData> doSearch(ServletContext servletContext, ApplicationSession appSession, 
			ApplicationVolatileSession volatileSession, OrderEntrySession oeSession,OEItemSearchCriteriaSessionBean criteria,
			boolean hideHiddenFeatured) throws AtWinXSException;
	
	public List<ItemThumbnailCellData> populateSearchResults(CatalogLineVO[] items, ApplicationSession appSession,
			ServletContext servletContext, OrderEntrySession oeSession, ApplicationVolatileSession volatileSession, OEItemSearchCriteriaSessionBean criteria,
			boolean hideHiddenFeatured) throws AtWinXSException;
	
	public CatalogLineVO[] searchItems(OEItemSearchCriteriaSessionBean criteria, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeSessionBean) throws AtWinXSException;
	
	public SearchResult getItemThumbnail(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings, ApplicationSession appSession, CatalogLineVO vo,
			ServletContext servletContext,ApplicationVolatileSession volatileSession, OEItemSearchCriteriaSessionBean criteria, String routingShippingMethodMsg, String routingExceedAmountMsg,
			String routingAlwaysRouteOrders, boolean hasFeaturedItems, Collection<OrderLineVO> orderLines, ICatalog catalogComp, 
			IManageItemsInterface itemInterface, KitSession kitSession, OEOrderSessionBean oeOrderSessionBean)	throws AtWinXSException;
	
	public String doAlternateDescriptionProcessing(CatalogLineVO vo) throws AtWinXSException;
	public void setIconPlusFldAndVal(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings);
	public void processItemPriceLimit(SearchResult thumb, double itemPrcLimitPerOrder, double unitPriceAmount);
	public String removeLineBreaks(String altDescDisp);
	public String getVariantLabelValues(boolean priceValue,boolean availabilityValue,AppSessionBean asb) throws AtWinXSException;
//	public String getUOMAcronyms(String strDesc,boolean fullDesc);
	
	//CAP-35437-Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
	public CatalogItemsResponse repeatItemSearch(SessionContainer mainSession,ServletContext servletContext) throws AtWinXSException;
	public CatalogItemsResponse getCatalogItemsRepeat(SessionContainer sc,ServletContext servletContext,
			OEItemSearchCriteriaSessionBean criteria) throws AtWinXSException;

	// CAP-42856
	public CatalogItemsResponse getFeaturedCatalogItems(ServletContext servletContext, ApplicationSession appSession, ApplicationVolatileSession volatileSession,
			OrderEntrySession oeSession, OEItemSearchCriteriaSessionBean criteria, FeaturedCatalogItemsRequest request)
			throws AtWinXSException;
	
	// CAP-44349
	public CatalogSearchResultsResponse doSearchWithCategory(ServletContext servletContext,
			ApplicationSession appSession, ApplicationVolatileSession volatileSession, OrderEntrySession oeSession,
			OEItemSearchCriteriaSessionBean criteria, boolean hideHiddenFeatured, CatalogItemRequest request) throws AtWinXSException;
	
	//CAP-48534
	public CatalogMessageResponse getCatalogMessages(ApplicationSession appSession) throws AtWinXSException;
			
}
