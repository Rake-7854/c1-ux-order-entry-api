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
 *  04/21/22    S Ramachandran  CAP-33686   Initial Creation, added for Multiple Item Search (predictive search)
 *  05/02/22    S Ramachandran  CAP-34043   Cloned CP methods to C1 to detach GWT dependency
 *	09/19/22	A Boomker		CAP-35958	Efficiency improvements
 *	05/31/23	N Caceres		CAP-39050	Resolve concurrency issues in UniversalSearch Service
 */

package com.rrd.c1ux.api.services.items;

import java.util.Collection;

import javax.servlet.ServletContext;

import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.models.items.UniversalSearchRequest;
import com.rrd.c1ux.api.models.items.UniversalSearchResponse;
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
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

public interface UniversalSearchService {

	public UniversalSearchResponse processUniversalSearch(SessionContainer mainSession, ServletContext servletContext, UniversalSearchRequest request) throws AtWinXSException;
	
	public CatalogLineVO[] removeDuplicateItems(CatalogLineVO[] items);
 
	public String getAdditionalIconPlusField(OEResolvedUserSettingsSessionBean userSettings, String itemNum);
 
	public SearchResult getItemThumbnail(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings, ApplicationSession appSession, CatalogLineVO vo,
			ServletContext servletContext,
			ApplicationVolatileSession volatileSession, OEItemSearchCriteriaSessionBean criteria, String routingShippingMethodMsg, String routingExceedAmountMsg,
			String routingAlwaysRouteOrders, boolean hasFeaturedItems, Collection<OrderLineVO> orderLines, ICatalog catalogComp,
			IManageItemsInterface itemInterface, KitSession kitSession)			throws AtWinXSException;
 
	public String doAlternateDescriptionProcessing(CatalogLineVO vo) throws AtWinXSException;
 
	public String removeLineBreaks(String altDescDisp);
 
	public void setIconPlusFldAndVal(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings);
 
	public void processItemPriceLimit(SearchResult thumb, double itemPrcLimitPerOrder, double unitPriceAmount);
 
	public boolean showAddToCart(SearchResult result, boolean isCustomizableItem, boolean isItemInCart);
 
	public boolean isItemInCart(CatalogLineVO line, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException;
	
}
