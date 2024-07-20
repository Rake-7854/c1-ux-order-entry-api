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
 * 	11/22/22	Sakthi M        	CAP-36560	    Created controller as per the requirement to fetch the sort,Page number
 *  11/28/22    Satishkumar A       CAP-36854       Order Search page filter option - navigation 
 *  11/25/22	Sumit Kumar			CAP-36559		Implement service method getSearchOrdersDetail to retrieve orders
 *  11/23/22	S Ramachandran  	CAP-36557   	Get Order details - Order info, customer info, Items ordered,  Ordered cost sections
 *  12/08/22	Sakthi M			CAP-37295 		Order search - Order Status Dropdown Menu values
 *  12/23/22	S Ramachandran		CAP-36916 		Get Shipments and Items under Shipping and tracking information
 *  01/03/23	Sumit Kumar			CAP-37779	 		Get Repeated searched orders information
 *  02/23/23	Sumit Kumar			CAP-38765 		removed 'api/orders/getordersearchrepeat'(CAP-37779) added its logic on 'api/orders/getsearchordersdetail'
 *  03/17/23	S Ramachandran		CAP-38720 		API Standardization - Order Detail by Sales ref in Order Search conversion to standards
 *  08/22/23	S Ramachandran		CAP-43234		Order Routing Information For Order Search
 *  11/14/23	N Caceres			CAP-45040		Add customer reference search criteria (cust ref 1, 2, 3) to Order Search
 */
package com.rrd.c1ux.api.services.orders.ordersearch;

import java.text.SimpleDateFormat;
import java.util.List;

import com.rrd.c1ux.api.models.orders.ordersearch.COOSDetailsItemsOrderedData;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSItemInShipmentTrackingRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentAndTrackingResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentDetail;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchCriteria;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchNavigationResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResultResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchScope;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderStatusCodeRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderStatusCodeResponse;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationRequest;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationResponse;
import com.rrd.c1ux.api.models.settingsandprefs.CatalogUtilityNavigationInfo;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSessionBean;

public interface OrderSearchService {
	
	public CatalogUtilityNavigationInfo getOrderSortPagination(SessionContainer sc) throws AtWinXSException;
	
	//CAP-36854       Order Search page filter option - navigation 
	public COOrderSearchNavigationResponse populateOrderDetailforNavigation(SessionContainer sc)
			throws AtWinXSException;
	
	//CAP-36854 - to get the order search criteria for navigation
	public List<COOrderSearchCriteria> getOrderSearchCriteriaForNagavation(AppSessionBean appSessionBean, OrderStatusSessionBean osBean) throws AtWinXSException ;

	//CAP-36854 - to get the order search scope for navigation
	public List<COOrderSearchScope> getOrderSearchScopeForNagavation(AppSessionBean appSessionBean, String orderStatusRestriction, String teamSharingAllowed ) throws AtWinXSException ;

	//CAP-36854 - to get the date range for order search navigation
	public COOrderSearchNavigationResponse getDateRanges(int dateRangeDays,SimpleDateFormat sdf, COOrderSearchNavigationResponse response) ;

	//CAP-36854 - to get the validation messages for order search navigation
	public COOrderSearchNavigationResponse getValidationMessages(AppSessionBean appSessionBean, COOrderSearchNavigationResponse response) throws AtWinXSException ;
	
	//CAP-36559	to retrieve order search records  
	public  COOrderSearchResultResponse getSearchOrdersDetail(SessionContainer sc, COOrderSearchRequest cOorderSearchRequest) throws AtWinXSException;
	
	//CAP-36557 - to get Order details - Order info, customer info, Items ordered,  Ordered cost sections  
	public COOSFullDetailResponse getOrderSearchFullDetailsWithSalesRef(SessionContainer mainSession, COOSFullDetailRequest request) 
			throws AtWinXSException;

	//CAP-43234	- to get Order Routing Information For Order Search
	public OrderRoutingInformationResponse getOSRoutingInfos(SessionContainer sc, OrderRoutingInformationRequest request) 
			throws AtWinXSException;
	
	//CAP-38720  - API Standardization - Order Detail by Sales ref in Order Search conversion to standards 
	//			 - with Order info, customer info, Items ordered,  Ordered cost sections
	public COOSFullDetailResponse getOrderSearchLoadDetail(SessionContainer sc, COOSFullDetailRequest request) 
			throws AtWinXSException;
	
	//CAP-36557 - to get itemsordered in a specific order#/salesref# 
	public List<COOSDetailsItemsOrderedData> getItemsOrdered(SessionContainer sc, COOSFullDetailRequest request) 
			throws AtWinXSException; 
	
	//CAP-37295- Order Status Dropdown Menu values
	public OrderStatusCodeResponse populateOrderStatus(SessionContainer sc, OrderStatusCodeRequest request) throws AtWinXSException;
	
	//CAP-36916 - to get Shipments and tracking information 
	public COOSShipmentAndTrackingResponse getOSShipmentAndTracking (SessionContainer sc, COOSFullDetailRequest request) 
			throws AtWinXSException;
	
	//CAP-36916 - to get Items under Shipments
	public COOSShipmentDetail getOSItemsInShipment(SessionContainer sc, COOSItemInShipmentTrackingRequest request) 
			throws AtWinXSException;

}
