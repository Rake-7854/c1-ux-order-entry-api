/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By          DTS#            	Description
 *	--------    -----------         ----------      	------------------------------------
 *  11/22/22	Sakthi M        	CAP-36560 	       	Created class for the CAP-36560
 *  11/28/22    Satishkumar A       CAP-36854       	Order Search page filter option - navigation
 *  11/25/22	Sumit Kumar			CAP-36559			Implement service method getSearchOrdersDetail to retrieve orders
 *  11/23/22	S Ramachandran  	CAP-36557   		Get Order details - Order info, customer info, Items ordered,  Ordered cost sections
 *  12/09/22    Sakthi M            CAP-37295        	Order search - Order Status Dropdown Menu values
 *	12/23/22	S Ramachandran		CAP-36916 			Get Shipments and Items under Shipping and tracking information
 *  12/09/22    Sakthi M            CAP-37295        	Order search - Order Status Dropdown Menu values
 *  01/03/23    Sakthi M			CAP-37757           Order Search/Details - API Creation - Order Files response for Order Search record
 *  01/03/23	Sumit Kumar			CAP-37779	 		Get Repeated searched orders information
 *  02/14/23    Satishkumar A       CAP-38540           Searching by order status with a empty modifier and empty field value should not display records.
 *  02/17/23    S Ramachandran      CAP-38355           API Definition Change -Missing Extended Customer Reference fields and other response problems.
 *  02/22/23	S Ramachandran		CAP-38538			write out 'N/A' if fulfillment type code is M(Kit Virtual Master)
 *  02/23/23	Sumit Kumar			CAP-38765 			removed 'api/orders/getordersearchrepeat'(CAP-37779) added its logic on 'api/orders/getsearchordersdetail'
 *  02/28/23    Satishkumar A       CAP-38709           API Fix - Add Translation to ResponseObject for /api/orders/order-search-navigation.
 *  03/06/23	Sumit Kumar			CAP-38707 			Add Translation to ResponseObject for api/orders/getsearchordersdetail
 *  03/06/23	Sakthi M			CAP-39070			getOrderSearchPagination API needs to return common Settings and Preferences object in response and correct bad data
 *  03/09/23	S Ramachandran		CAP-39140			Retrieve Extended Customer Reference fields, remove getItemsOrdered()->customer item# as vendor item#), to be handle at FE
 *  03/10/23	A Boomker			CAP-39173			In searching, do not re-initialize session every time through
 *  03/15/23    Satishkumar A       CAP-38736           API Standardization - Search Navigation in Order Search conversion to standards
 *  03/15/23    Sakthi M            CAP-38737		    API Standardization - Order Status Codes in Order Search conversion to standards
 *  03/17/23	S Ramachandran		CAP-38720 			API Standardization - Order Detail by Sales ref in Order Search conversion to standards
 *  03/21/23	A Boomker			CAP-39369			In populateOrderDetailforNavigation(), add handling for show recent orders flag
 *  03/23/23    E Anderson          CAP-38724           Add translation for orderHeader.
 *  03/22/23	Sumit Kumar			CAP-38651			API Standardization - Search Orders in Order Search conversion to standards
 *  03/30/23	N Caceres			CAP-39159			Order Search Order Details by sales ref and Order Search Shipments responses must reference C1UX no image instead of CP no image path on items
 *  05/02/23    SAkthi M            CAP-40284 			C1UX BE - API Fix - Item UOM translation was incorrect with getItemUOM, so replaced with getUomFactor for getorderdetails-forsalesref
 *  05/01/23 	Krishna Natarajan	CAP-39972 			added date error message and other validations including status code check in XST156
 *  05/31/23	C Codina			CAP-39049			Address Concurrency issues in OrderSearch Service
 *  06/27/23	A Boomker			CAP-41681			Trying to fix inefficiency
 *  06/28/23	M Sakthi			CAP-40615			SonarQube: Bugs in OrderSearchServiceImpl
 *  06/29/23	A Salcedo			CAP-41713			Added setShowBillToInfoAndAttn() for getOrderSearchLoadDetail() and getOrderSearchFullDetailsWithSalesRef().
 *	07/06/23	A Boomker			CAP-41840			Fix when initial WCSS search failed to pass back WCSS Order #
 *	07/06/23	C Codina			CAP-38766			Sequence number should match with the one in shipment level and Item desc & Item Number should not populate when the Order line number is not passed
 *	07/13/23	A Salcedo			CAP-38766 			Refactored getItemsInShipmentDetails() to eliminate SonarQube complexity issue.
 *	07/19/23	Satishkumar A		CAP-41553			C1UX BE - API Change - Order Search Results Listing changes needed for new copy order flags
 *	08/22/23	S Ramachandran		CAP-43234			Order Routing Information For Order Search
 *	08/30/23	Satishkumar A		CAP-43283			C1UX BE - Routing Information For Justification Section on Review Order Page 	
 *	11/10/23	M Sakthi			CAP-44979			C1UX BE - Modify order search to return credit card fields and carrier service fields
 *  11/13/23	T Harmon			CAP-45241			Fixed code for order search - not creating actions for CP anymore
 * 	11/14/23	C Codina			CAP-45054			C1UX BE - Add WCSS Order Number as search criteria for Order Search
 *  11/14/23	N Caceres			CAP-45040			Add customer reference search criteria (cust ref 1, 2, 3) to Order Search
 *  11/17/23	M Sakthi			CAP-45055			C1UX BE - Add Order Name and Invoice Number as search criteria for Order Search
 *  11/21/23	Krishna Natarajan	CAP-45429			Fixed code to check an replace with empty string if sales reference number is (Unavailable)
 *  11/24/23	N Caceres			CAP-45133			Add Ship To Country, Ship To State, Ship To Zip to Order Search Criteria
 *  11/27/23	M Sakthi			CAP-45057			C1UX BE - Add Ship To Name and Ship To Attention as search criteria for Order Search
 *  11/29/23	M Sakthi			CAP-45481			C1UX BE - Modify validation for order search to only allow search options for Limited Quick Search Option
 *  12/04/23	Krishna Natarajan	CAP-45548			Changed the boolean value to true, in order to call the update sales reference to set (Unavailable) in case of empty sales reference with order being placed outside SF
 *  12/05/23	Krishna Natarajan	CAP-45058			Added code to get the orderMessages in the COOSFullDetailResponse
 *  12/19/23	Krishna Natarajan	CAP-46011 			Added a condition to evaluate the request object coOrderSearchRequest is not null and not empty
 *  12/27/23	M Sakthi			CAP-45812			C1UX BE - Add order originator to order search details
 *  12/28/23	C Codina			CAP-45677			C1UX BE_Bug - Order Search API - Passing non-numeric invoice number is allowed in API
 *  01/11/24	Krishna Natarajan	CAP-45812			Separated the method to get the Originator profile updated and reduce complexity
 *  01/29/24	Krishna Natarajan	CAP-46677			addtions to handle the profile ID instead of profile num
 *  04/12/23	S Ramachandran		CAP-48488 			Modify Order Search to show expedite information if an order is expedited.
 *  05/16/24	Krishna Natarajan 	CAP-49259			Added logic to update distListText in the response	
 */
package com.rrd.c1ux.api.services.orders.ordersearch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSDetailsItemsOrderedData;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSItemInShipmentDetail;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSItemInShipmentTrackingRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentAndTrackingResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentDetail;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchCriteria;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchNavigationResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResult;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResultResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchScope;
import com.rrd.c1ux.api.models.orders.ordersearch.COSharedOrderStatusCode;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderMessages;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderStatusCodeRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderStatusCodeResponse;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationRequest;
import com.rrd.c1ux.api.models.routing.OrderRoutingInformationResponse;
import com.rrd.c1ux.api.models.settingsandprefs.CatalogUtilityNavigationInfo;
import com.rrd.c1ux.api.models.settingsandprefs.SettingsandPrefs;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.c1ux.api.util.OrderSearchCriteriaEnum;
import com.rrd.c1ux.api.util.OrderSearchUtil;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.ordersearch.orderdetails.routinginfo.widget.OSDetailsRoutingInfoBean;
import com.rrd.custompoint.orderentry.customdocs.CDFilesInformation;
import com.rrd.custompoint.orderentry.customdocs.CustomDocumentOrderLine;
import com.rrd.custompoint.orderentry.entity.CustomerReference;
import com.rrd.custompoint.orderentry.entity.EFDTracking;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderMessage;
import com.rrd.custompoint.orderentry.entity.OrderStatusMessage;
import com.rrd.custompoint.orderentry.entity.RAOrderInformation;
import com.rrd.custompoint.orderentry.util.OrderDetailsUtils;
import com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBean;
import com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl;
import com.rrd.custompoint.orderstatus.ao.RAOrderDetailBean;
import com.rrd.custompoint.orderstatus.entity.OSDetailsItemOrdered;
import com.rrd.custompoint.orderstatus.entity.OSDetailsItemsOrdered;
import com.rrd.custompoint.orderstatus.entity.OrderStatusAction;
import com.rrd.custompoint.orderstatus.entity.OrderStatusAction.ActionEnum;
import com.rrd.custompoint.orderstatus.entity.OrderStatusCode;
import com.rrd.custompoint.orderstatus.entity.OrderStatusCodeImpl;
import com.rrd.custompoint.orderstatus.entity.OrderStatusCodes;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearch;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchCriteria;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchCriteria.ScopeEnum;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchResult;
import com.rrd.custompoint.orderstatus.entity.OrderStatusTrackingInfo;
import com.rrd.custompoint.orderstatus.entity.SharedCriteriaEnum;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.util.LookAndFeelFeature;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVOKey;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CPUrlBuilder;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.Util.ContextPath;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.IOECustomDocumentComponent;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.interfaces.IOrderStatus;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.lists.locator.ManageListsLocator;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.lists.vo.ListVOKey;
import com.wallace.atwinxs.orderentry.locator.OECustomDocLocator;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.locator.OEShoppingCartComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.CustomDocumentOrderLineVO;
import com.wallace.atwinxs.orderentry.vo.CustomDocumentOrderLineVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderInfoShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVOKey;
import com.wallace.atwinxs.orderstatus.locator.OrderStatusLocator;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSession;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSessionBean;
import com.wallace.atwinxs.orderstatus.util.OrderStatusConstants;
import com.wallace.atwinxs.orderstatus.util.OrderStatusUtil;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusFulfillmentLineVO;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusHeaderVO;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusSearchResultVO;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusShipmentVO;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusShipmentVOKey;
import com.wallace.tt.vo.TTSession;

@Service
public class OrderSearchServiceImpl extends BaseOEService  implements OrderSearchService{

	protected OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService;
	
	//CAP-39049 Dependency Injection for new wrapper objects
	//CAP-38709
	protected OrderSearchServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService,
			OEManageOrdersComponentLocatorService oeManageOrdersLocatorService,
			OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService) {
		super(translationService,objectMapFactoryService,oeManageOrdersLocatorService);
		this.oeManageOrdersComponentLocatorService = oeManageOrdersComponentLocatorService;
	}

	private static final String DATE_FORMAT="MM/dd/yyyy";
	private SimpleDateFormat sdfUSLocale = new SimpleDateFormat(DATE_FORMAT);

	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public CatalogUtilityNavigationInfo  getOrderSortPagination(SessionContainer sc) throws AtWinXSException {

		SettingsandPrefs setandprefs = new SettingsandPrefs();
		Properties orderSearchBundleProps = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(), "orderSearchNav");
		CatalogUtilityNavigationInfo sortingPagenationRes=new CatalogUtilityNavigationInfo();

		Map<String, String> sortOpt1 = new HashMap<>();
		sortOpt1.put(ModelConstants.LABEL,orderSearchBundleProps.getProperty("salesrefnum"));
		sortOpt1.put(ModelConstants.VALUE, ModelConstants.SALES_REF_NO_NOSPACE);

		Map<String, String> sortOpt2= new HashMap<>();
		sortOpt2.put(ModelConstants.LABEL,orderSearchBundleProps.getProperty("ponum"));
		sortOpt2.put(ModelConstants.VALUE,ModelConstants.PO_NO_NOSPACE );


		Map<String, String> sortOpt3 = new HashMap<>();
		sortOpt3.put(ModelConstants.LABEL,orderSearchBundleProps.getProperty("orderdate"));
		sortOpt3.put(ModelConstants.VALUE, ModelConstants.ORDER_DATE_NOSPACE);

		Map<String, String> sortOpt4 = new HashMap<>();
		sortOpt4.put(ModelConstants.LABEL,orderSearchBundleProps.getProperty("orderstatus"));
		sortOpt4.put(ModelConstants.VALUE, ModelConstants.ORDER_STATUS_NOSPACE);

		setandprefs.getSortOptions().add(sortOpt1);
		setandprefs.getSortOptions().add(sortOpt2);
		setandprefs.getSortOptions().add(sortOpt3);
		setandprefs.getSortOptions().add(sortOpt4);


		Integer defaultNoOfItems=10;
		Map<String, Object> label1 = new HashMap<>();
		label1.put(ModelConstants.LABEL, "10");
		label1.put(ModelConstants.VALUE, 10);
		Map<String, Object> label2 = new HashMap<>();
		label2.put(ModelConstants.LABEL, "20");
		label2.put(ModelConstants.VALUE, 20);
		Map<String, Object> label3 = new HashMap<>();
		label3.put(ModelConstants.LABEL, "40");
		label3.put(ModelConstants.VALUE, 40);
		Map<String, Object> label4 = new HashMap<>();
		label4.put(ModelConstants.LABEL, "80");
		label4.put(ModelConstants.VALUE, 80);
		setandprefs.getShowNumberOptions().add(label1);
		setandprefs.getShowNumberOptions().add(label2);
		setandprefs.getShowNumberOptions().add(label3);
		setandprefs.getShowNumberOptions().add(label4);

		setandprefs.setDefaultSortBy(ModelConstants.ORDER_DATE_NOSPACE);
		setandprefs.setDefaultNoOfItems(defaultNoOfItems);
		sortingPagenationRes.setSettingsandPrefs(setandprefs);

	   return sortingPagenationRes;
	}

	//CAP-36854 - to populate order search navigation details
	@Override
	public COOrderSearchNavigationResponse populateOrderDetailforNavigation(SessionContainer sc)
			throws AtWinXSException {

		COOrderSearchNavigationResponse searchNavigationResponse = new COOrderSearchNavigationResponse();

		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		volatileSessionBean.setServiceID(AtWinXSConstant.ORDER_SEARCH_SERVICE_ID); // CP-12549
		OrderStatusSessionBean osBean = ((OrderStatusSession)sc.getModuleSession()).getOrderStatusSettingsBean();
		AppSessionBean appSessionBean =sc.getApplicationSession().getAppSessionBean();

		OrderStatusSearch orderStatusSearch = objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusSearch.class, appSessionBean.getCustomToken());

		//CP-11632 RAR - Call this new method to determin the default date range for the site.
		int dateRangeDays = orderStatusSearch.getOrderStatusDateRange(appSessionBean.getSiteID());
		String teamSharingAllowed = osBean.isAllowTeamOrderSharing()?Boolean.TRUE.toString():Boolean.FALSE.toString();

		//CP-11718 RAR - Create a new variable to store the max date range the user can use for searching orders.
		int maxDateRange = orderStatusSearch.getOrderStatusMaxDateRange(appSessionBean.getSiteID());
		String quickSearchPref = osBean.getQuickSearchPreference();
		String orderStatusRestriction = osBean.getOrderStatusRestriction();

		String orderSearchTitleLabel = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_LBL_TITLE);//ordersearch_lbl_title=Order Search
		String orderSearchLabel = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_LBL_SEARCH);//ordersearch_lbl_search=Search
		String orderSearchForLabel = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_LBL_FOR);//ordersearch_lbl_for=For
		String orderDateRangeLabel  = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_LBL_DATERANGE);//ordersearch_lbl_daterange=Date Range
		String orderSearchScopeLabel  = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_LBL_SCOPE);//ordersearch_lbl_scope=Scope

		searchNavigationResponse.setMaxDateRange(maxDateRange);
		searchNavigationResponse.setQuickSearchPreference(quickSearchMap.get(quickSearchPref));
		searchNavigationResponse.setOrderSearchTitleLabel(orderSearchTitleLabel);
		searchNavigationResponse.setOrderSearchLabel(orderSearchLabel);
		searchNavigationResponse.setOrderSearchForLabel(orderSearchForLabel);
		searchNavigationResponse.setOrderDateRangeLabel(orderDateRangeLabel);
		searchNavigationResponse.setOrderSearchScopeLabel(orderSearchScopeLabel);
		// CAP-39369
		searchNavigationResponse.setShowRecentOrders(osBean.isShowRecentOrders());

		List<COOrderSearchCriteria> oscArrList = getOrderSearchCriteriaForNagavation(appSessionBean, osBean) ;
		searchNavigationResponse.setOrderSearchCriteria(oscArrList);

		List<COOrderSearchScope> ossArrList = getOrderSearchScopeForNagavation(appSessionBean, orderStatusRestriction, teamSharingAllowed);
		searchNavigationResponse.setOrderSearchScope(ossArrList);

		searchNavigationResponse = getDateRanges(dateRangeDays,sdfUSLocale ,searchNavigationResponse);
		searchNavigationResponse = getValidationMessages(appSessionBean, searchNavigationResponse);

		//CAP-38709
		Properties orderSearchNavBundleProps = translationService.getResourceBundle(appSessionBean, "orderSearchNav");
		searchNavigationResponse.setTranslation(translationService.convertResourceBundlePropsToMap(orderSearchNavBundleProps));
		//CAP-38736
		searchNavigationResponse.setSuccess(true);

		//CAP-38724
		Properties orderHeaderBundleProps = translationService.getResourceBundle(appSessionBean, "orderHeader");
		searchNavigationResponse.setTranslationOrderHeader(translationService.convertResourceBundlePropsToMap(orderHeaderBundleProps));

		 return searchNavigationResponse;
	}

	// CAP-45133	
	public List<COOrderSearchCriteria> getOrderSearchCriteriaForNagavation(AppSessionBean appSessionBean,
			OrderStatusSessionBean osBean) throws AtWinXSException {
		List<COOrderSearchCriteria> oscArrList = new ArrayList<>();
		
		for (OrderSearchCriteriaEnum criteria : OrderSearchCriteriaEnum.values()) {
			buildOrderSearchCriteria(getTranslationValue(appSessionBean, criteria.getTranslationName()), 
					criteria.getCriteriaName(), criteria.getCriteriaSize(), 
					addCriteria(osBean.isLimitQuickSearchOptions(), criteria.isQuickSearch()), 
					oscArrList);
		}
		
		for (int occ = 0; occ < 3; occ++) {
			if (!ModelConstants.OPTION_NO.equalsIgnoreCase(osBean.getShowCustRef(occ))
					&& !ModelConstants.LINE_ONLY.equalsIgnoreCase(osBean.getShowCustRef(occ))) {
				buildOrderSearchCriteria(osBean.getCustRefLabelText(occ), 
						buildCustRefCriteriaValue(occ), ModelConstants.NUMERIC_25, 
						addCriteria(osBean.isLimitQuickSearchOptions(), false),
						oscArrList);
			}
		}
		return oscArrList;
	}

	// CAP-45040
	private void buildOrderSearchCriteria(String criteriaLabel, String criteriaValue, int criteriaLength , boolean isAdd, List<COOrderSearchCriteria> oscArrList) {
		COOrderSearchCriteria osc = null;
		osc = new COOrderSearchCriteria();
		osc.setCriteriaLabel(criteriaLabel);
		osc.setCriteriaValue(criteriaValue);
		osc.setCriteriaLength(criteriaLength);
		if (isAdd) {
			oscArrList.add(osc);
		}
	}
	
	// CAP-45040
	private String buildCustRefCriteriaValue(int occ) {
		StringBuilder builder = new StringBuilder();
		builder.append(OrderSearchUtil.CRITERIA_CUST_REF);
		builder.append(String.valueOf(occ + 1));
		return builder.toString();
	}
	
	private String getTranslationValue(AppSessionBean appSessionBean, String translationName) throws AtWinXSException {
		return translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), translationName);
	}
	
	private boolean addCriteria(boolean isLimitQuickSearchOptions, boolean quickSearch) {
		if (quickSearch) {
			return true;
		} else {
			if (!isLimitQuickSearchOptions) {
				return true;
			}
		}
		return false;
	}
	
	//CAP-36854 - to get the order search scope for navigation
	public List<COOrderSearchScope> getOrderSearchScopeForNagavation(AppSessionBean appSessionBean, String orderStatusRestriction, String teamSharingAllowed ) throws AtWinXSException {

		List<COOrderSearchScope> ossArrList=new ArrayList<>();

		COOrderSearchScope oss =null;
		String orderSearchScopeOptionLabel = null;
		 if("N".equalsIgnoreCase(orderStatusRestriction)) {
			 oss = new COOrderSearchScope();
			 orderSearchScopeOptionLabel = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_SCOPE_A);
			 oss.setScopeLabel(orderSearchScopeOptionLabel);
			 oss.setScopeValue(RouteConstants.CUSTOMTEXT_SCOPE_A_VAL);
			 ossArrList.add(oss);
		 }
		 if(!("Y".equalsIgnoreCase(orderStatusRestriction)) && teamSharingAllowed.equalsIgnoreCase("true")) {

				 oss = new COOrderSearchScope();
				 orderSearchScopeOptionLabel = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_SCOPE_T);
				 oss.setScopeLabel(orderSearchScopeOptionLabel);
				 oss.setScopeValue(RouteConstants.CUSTOMTEXT_SCOPE_T_VAL);
				 ossArrList.add(oss);
		 }
		 oss = new COOrderSearchScope();
		 orderSearchScopeOptionLabel = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CUSTOMTEXT_SCOPE_M);
		 oss.setScopeLabel(orderSearchScopeOptionLabel);
		 oss.setScopeValue(RouteConstants.CUSTOMTEXT_SCOPE_M_VAL);
		 ossArrList.add(oss);

		 return ossArrList;
	}

	//CAP-36854 - to get the date range for order search navigation
	public COOrderSearchNavigationResponse getDateRanges(int dateRangeDays,SimpleDateFormat sdf, COOrderSearchNavigationResponse response) {

		 Calendar c = Calendar.getInstance();
		 c.setTime(new Date());
		 String toDate = sdf.format(c.getTime());
		 c.add(Calendar.DATE, -dateRangeDays);
		 String fromDate = sdf.format(c.getTime());

		 response.setOrderDateRangeFrom(fromDate);
		 response.setOrderDateRangeTo(toDate);

		return response;
	}

	//CAP-36854 - to get the validation messages for order search navigation
	public COOrderSearchNavigationResponse getValidationMessages(AppSessionBean appSessionBean, COOrderSearchNavigationResponse response) throws AtWinXSException {

		 String ordersearchMsgCriteria =TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "ordersearch_msg_criteria");
		 String ordersearchMsgDateMissing =TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "ordersearch_msg_date_missing");
		 String ordersearchMsgDateInvalid =TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "ordersearch_msg_date_invalid");
		 String ordersearchMsgDateMax =TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "ordersearch_msg_date_max");

		 response.setOrderSearchMsgCriteria(ordersearchMsgCriteria);
		 response.setOrderSearchMsgDateMissing(ordersearchMsgDateMissing);
		 response.setOrderSearchMsgDateInvalid(ordersearchMsgDateInvalid);
		 response.setOrderSearchMsgDateMax(ordersearchMsgDateMax);

		 return response;

	}

	/** CP code reference : com.rrd.custompoint.gwt.ordersearch.widget.OrderSearchCriteria class **/
	protected static final Map<String, String> quickSearchMap;
	static {
		Map<String, String> iMap = new HashMap<>();
		iMap.put("P", OrderSearchUtil.CRITERIA_PO_NUMBER);
		iMap.put("R", "CriteriaRoutingNumber");
		iMap.put("S", OrderSearchUtil.CRITERIA_SALES_REF);
		iMap.put("C", "CriteriaCustRef1");
		iMap.put("W", OrderSearchUtil.CRITERIA_ORD_NUMBER);
		iMap.put("I", "CriteriaInvoiceNumber");

		quickSearchMap = java.util.Collections.unmodifiableMap(iMap);
	}

	/**
	 *
	 * @param sessionContainer     {@link-SessionContainer}
	 * @param COOrderSearchRequest {@link-COOrderSearchRequest}
	 * @return the COOrderSearchResultResponse object
	 *         {@link-COOrderSearchResultResponse}
	 * @throws AtWinXSException
	 */
	// CAP-36559 service method to retrieve searched order/s
	// copied this method logic from com.rrd.custompoint.service.orderstatus.OrderSearchServiceImpl class method doSearch()
	@Override
	public COOrderSearchResultResponse getSearchOrdersDetail(SessionContainer sc,
			COOrderSearchRequest coOrderSearchRequest) throws AtWinXSException{
		logger.debug("In OrderSearchServiceImpl to calling getSearchOrdersDetail()");
		COOrderSearchResultResponse coOrderSearchResultResponse = new COOrderSearchResultResponse();
		ApplicationSession appSession = sc.getApplicationSession();
		OrderStatusSession session = (OrderStatusSession) sc.getModuleSession();

		//CAP-41553
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		
		//CAP-45481
		boolean limitedQuickSearchOptions = ((OrderStatusSession)sc.getModuleSession()).getOrderStatusSettingsBean().isLimitQuickSearchOptions();
		if(validateLimitedSearch(limitedQuickSearchOptions, coOrderSearchRequest)) {
			coOrderSearchResultResponse.setSuccess(false);
			coOrderSearchResultResponse.setMessage(AtWinXSConstant.EMPTY_STRING);
			coOrderSearchResultResponse.setFieldMessage(coOrderSearchRequest.getSearchCriteriaRequest().get(0).getCriteriaName(), translationService.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(),SFTranslationTextConstants.LIMITED_SEARCH_ERR_MSG));
			return coOrderSearchResultResponse;
		}
	
		//CAP-38651 : API Standardization - Search Orders in Order Search conversion to standards
		coOrderSearchResultResponse.setSuccess(true);
		//validate search request but not repeat search
		if(!coOrderSearchRequest.isRepeatSearch())
		{
			validateCoOrderSearchRequest(coOrderSearchRequest, coOrderSearchResultResponse,sc, session);
			if(!coOrderSearchResultResponse.isSuccess())
				return coOrderSearchResultResponse;
		}

		if (session == null) {
			session = new OrderStatusSession();
			session.init(appSession.getAppSessionBean()); // CAP-39173 - do not re-initialize every time through
		}
		// CP-11771: initialize OrderStatusSession to get the updated
		// OrderStatusSessionBean
		OrderStatusSessionBean osBean = session.getOrderStatusSettingsBean();
		osBean.setSimplifiedView(true); // CAP-39173
		String dateFormat = Util.getDateFormatForLocale(appSession.getAppSessionBean().getDefaultLocale());
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		validateDates(coOrderSearchRequest, sdf, dateFormat);

		Collection<OrderStatusSearchResult> searchResults = null;
		String newSearchBeanName = "newSearchBean";
		List<COOrderSearchResult> result = new ArrayList<>();
		OrderStatusSearchCriteria cpSearchCriteria = null;
		OrderStatusSearch osSearch = null;
		try {
			// Now call for actual search
			osSearch = getImplementingClass(appSession.getAppSessionBean().getCustomToken());
			osSearch.initialize(appSession.getAppSessionBean(), Util.getUsabilityContextPath());


			//CAP-38765 - added repeatSearch logic here, which fetch value from cache
			//first check repeat search and not need to pass search criteria object(coOrderSearchRequest)
			if (coOrderSearchRequest.isRepeatSearch()) {
				cpSearchCriteria = OrderSearchUtil.getOrderStatusSearchCriteria(null,
						appSession.getAppSessionBean(), sdf);
				cpSearchCriteria.setRecentOrderSearch(false);
				cpSearchCriteria.setUpdateResultInfo(true);//CAP-45548 changed the boolean value to true, in order to call the update sales reference to set (Unavailable) in case of empty sales reference with order being placed outside SF
				cpSearchCriteria.setCacheTypeToClear(0);
				coOrderSearchRequest.setSearchForWidgetDisplay(false);
				searchResults = osSearch.getSearchResults(cpSearchCriteria, false);  // CAP-45241
				OrderSearchUtil.createActions(searchResults, appSession.getAppSessionBean(), osSearch);
				coOrderSearchResultResponse.setRecentOrderSearch(true);
				if(session.getParameter(newSearchBeanName)!=null && session.getParameter(newSearchBeanName).equals("yes"))
					coOrderSearchResultResponse.setRecentOrderSearch(false);
			}
			// second check recent orders
			else if(coOrderSearchRequest.isRecentOrderSearch())
			{
				// Get the actual search criteria object so we can save to session
				cpSearchCriteria = osSearch.getRecentOrderSearchCriteria(ScopeEnum.ONLY_MY_ONLINE_ORDS);
				coOrderSearchResultResponse.setRecentOrderSearch(true);
				session.putParameter(newSearchBeanName, "no");
				SessionHandler.saveSession(session, appSession.getAppSessionBean().getTtSession().getId() , AtWinXSConstant.ORDER_SEARCH_SERVICE_ID);
				searchResults = osSearch.doSearch(cpSearchCriteria, false);  // CAP-45241
			}
			//third search based on actual search criteria object(coOrderSearchRequest)
			else {
			cpSearchCriteria = OrderSearchUtil.getOrderStatusSearchCriteria(coOrderSearchRequest,
					appSession.getAppSessionBean(), sdf);
			coOrderSearchResultResponse.setRecentOrderSearch(false);
			
			//CAP-38540 - searching with empty field value should not display records.
			if(coOrderSearchRequest.isRecentOrderSearch() || ! cpSearchCriteria.getCriteriaOrdering().isEmpty()  ) {
					searchResults = osSearch.doSearch(cpSearchCriteria, false);  // CAP-45241
					session.putParameter(newSearchBeanName, "yes");
					SessionHandler.saveSession(session, appSession.getAppSessionBean().getTtSession().getId() , AtWinXSConstant.ORDER_SEARCH_SERVICE_ID);
				}
			}

			// RAR - Build the return object only if there are results.
			coOrderSearchResultResponse.setOrdersFound(0);
			
			//CAP-41553
			if(osBean.isShowCopyRecentOrder() && !(appSession.getAppSessionBean().isPunchout()) && (volatileSessionBean.getShoppingCartCount() <= 0)) {
				coOrderSearchResultResponse.setShowOrderOptions(true);
			}	

			if (isValidSearchResults(searchResults)) {
				result = OrderSearchUtil.convertSearchResults(searchResults, osBean.isSimplifiedView(),
						appSession.getAppSessionBean(), sdf,coOrderSearchRequest.isSearchForWidgetDisplay(), coOrderSearchResultResponse);
				coOrderSearchResultResponse.setOrdersFound((null != searchResults) ?searchResults.size() : 0);
			}

		} catch (Exception ex) {
			logger.error(String.format("Exception thrown in OrderSearchServiceImpl call to getSearchOrdersDetail() :- %s",ex));
		}

		coOrderSearchResultResponse.setSearchResults(result);

		//CAP-38707
		Properties resourceBundlePropsOrderSearch = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(), "orderSearch");
		Properties resourceBundlePropsRecetOrder = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(), "recentOrder");
		Properties resourceBundlePropsOrderDetails = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(), "orderDetails");

		coOrderSearchResultResponse.setTranslationOrderSearch(translationService.convertResourceBundlePropsToMap(resourceBundlePropsOrderSearch));
		coOrderSearchResultResponse.setTranslationRecentOrder(translationService.convertResourceBundlePropsToMap(resourceBundlePropsRecetOrder));
		coOrderSearchResultResponse.setTranslationOrderDetails(translationService.convertResourceBundlePropsToMap(resourceBundlePropsOrderDetails));

		//CAP-38651 : More than 400 records found
		validateRecordCounts(osSearch, coOrderSearchResultResponse, appSession);

		return coOrderSearchResultResponse;
	}

	protected OrderStatusSearch getImplementingClass(CustomizationToken customizationToken) {
		return objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusSearch.class, customizationToken);
	}


	//CAP-38720  - API Standardization - Order Detail by Sales ref in Order Search conversion to standards
	//			 - with Order info, customer info, Items ordered,  Ordered cost sections
	/**
	 * Method to getOrderSearchLoadDetail including Order info, customer info, Items ordered,  Ordered price  using order#, salesRef,wcss#
	 *
	 * @param SessionContainer
	 * @param COOSFullDetailRequest
	 * @return COOSFullDetailResponse
	 */
	@Override
	public COOSFullDetailResponse getOrderSearchLoadDetail(SessionContainer sc,
			COOSFullDetailRequest request) throws AtWinXSException {

		COOSFullDetailResponse objCOOSFullDetailResponse = new COOSFullDetailResponse();

		String errMsgUnabletoload = TranslationTextTag.processMessage(
				sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
				sc.getApplicationSession().getAppSessionBean().getCustomToken(),ModelConstants.ERROR_LOAD_ORDER);

		//CAP-38720 - validate request attribute searchResultNumber is empty/exceed length
		if ((null!=request.getSearchResultNumber())
				&& (request.getSearchResultNumber().length()<=ModelConstants.MINLEN_0
				|| request.getSearchResultNumber().length()>ModelConstants.MAXLEN_10)) {

			objCOOSFullDetailResponse.setSuccess(false);
			objCOOSFullDetailResponse.setMessage(errMsgUnabletoload);
			return objCOOSFullDetailResponse;
		}

		//CAP-38720 - set sortLinesBy always to ORD_LN_NR(XST093) to retrieve Ordered Items
		request.setSortLinesBy(ModelConstants.OS_SORTLINEBY);

		//CAP-38720 - catch exception if order status HeaderVo is null from CP and response with generic error message
		//(ie) could not find search result for searchResultNumber and  sessionID
		OrderSearchDetailFormBean objOrderSearchDetailFormBean = null;

		try	{

			objOrderSearchDetailFormBean = getOrderDetail(sc, request);

		}catch(AtWinXSMsgException ae) {

			objCOOSFullDetailResponse.setSuccess(false);
			objCOOSFullDetailResponse.setMessage(errMsgUnabletoload);
			return objCOOSFullDetailResponse;
		}

		//CAP-38720 - validate  order status HeaderVo if does not match values
		//			  sent in that request (sales ref #, order ID, WCSS order number)
		if(null==objOrderSearchDetailFormBean.getHeaderVo()
			||	(request.getOrderID()!=objOrderSearchDetailFormBean.getHeaderVo().getXsOrderID())
			|| !request.getOrderNum().equalsIgnoreCase(objOrderSearchDetailFormBean.getHeaderVo().getOrderNum())
			|| !request.getSalesReferenceNumber().equals(objOrderSearchDetailFormBean.getHeaderVo().getSalesOrderNum())) {

			objCOOSFullDetailResponse.setSuccess(false);
			objCOOSFullDetailResponse.setMessage(errMsgUnabletoload);
			return objCOOSFullDetailResponse;
		}

		objCOOSFullDetailResponse.setObjOrderStatusHeaderVO(objOrderSearchDetailFormBean.getHeaderVo());

		//CAP-39140 - Retrieve Extended Customer Reference fields for an Order based on UG setting of user.
		objCOOSFullDetailResponse.setCustomerReferenceFields(objOrderSearchDetailFormBean.getCustomerReferenceFields());

		objCOOSFullDetailResponse.setBillMethodValue(objOrderSearchDetailFormBean.getBillMethodValue());
		objCOOSFullDetailResponse.setStatusDescription(objOrderSearchDetailFormBean.getStatusValue());
		setActionOrderFile(sc,objCOOSFullDetailResponse,objOrderSearchDetailFormBean);
		objCOOSFullDetailResponse.setItemsOrderedData(getItemsOrdered(sc,request));

		//CAP-41713
		setShowBillToInfoAndAttn(objCOOSFullDetailResponse, sc);

		objCOOSFullDetailResponse.setSuccess(true);
		return objCOOSFullDetailResponse;
	}


	//CAP-36557 -	Get Order details - Order info, customer info, Items ordered,  Ordered cost sections
	/**
	 * Method to getOrderSearchFullDetails including Order info, customer info, Items ordered,  Ordered price  using order#, salesRef,wcss#
	 *
	 * @param SessionContainer
	 * @param COOSFullDetailRequest
	 * @return COOSFullDetailResponse
	 */
	@Override
	public COOSFullDetailResponse getOrderSearchFullDetailsWithSalesRef(SessionContainer sc,
			COOSFullDetailRequest request) throws AtWinXSException {

		COOSFullDetailResponse objCOOSFullDetailResponse = new COOSFullDetailResponse();
		
		OrderSearchDetailFormBean objOrderSearchDetailFormBean = getOrderDetail(sc, request);
		objCOOSFullDetailResponse.setObjOrderStatusHeaderVO(objOrderSearchDetailFormBean.getHeaderVo());

		//CAP-39140 - Retrieve Extended Customer Reference fields for an Order based on UG setting of user.
		objCOOSFullDetailResponse.setCustomerReferenceFields(objOrderSearchDetailFormBean.getCustomerReferenceFields());

		objCOOSFullDetailResponse.setBillMethodValue(objOrderSearchDetailFormBean.getBillMethodValue());
		objCOOSFullDetailResponse.setStatusDescription(objOrderSearchDetailFormBean.getStatusValue());
		setActionOrderFile(sc,objCOOSFullDetailResponse,objOrderSearchDetailFormBean);
		// CAP-41840 - must correct the wcss order number if it was updated during the details calls
		if (!request.getOrderNum().equals(objOrderSearchDetailFormBean.getHeaderVo().getOrderNum()))
		{
			request.setOrderNum(objOrderSearchDetailFormBean.getHeaderVo().getOrderNum());
		}
		
		////CAP-49259
		Order currentOrder = objectMapFactoryService.getEntityObjectMap()
				.getEntity(Order.class, sc.getApplicationSession().getAppSessionBean().getCustomToken());
		currentOrder.populateBySalesRefNumber(objOrderSearchDetailFormBean.getHeaderVo().getSalesOrderNum());
		if (currentOrder.isDistributionListOrder()) {
			objCOOSFullDetailResponse.setDistListText(getTranslation(sc.getApplicationSession().getAppSessionBean(),
					"distribution_list_lbl", "Distribution List"));
		}
		
		objCOOSFullDetailResponse.setItemsOrderedData(getItemsOrdered(sc,request));
		
		//CAP-44979- Start Here
		objCOOSFullDetailResponse.setPaymentName(objOrderSearchDetailFormBean.getPaymentVerification().getHolderName());
		objCOOSFullDetailResponse.setPaymentCardType(objOrderSearchDetailFormBean.getPaymentVerification().getTypeCode());
		
		if(!Util.isBlankOrNull(objOrderSearchDetailFormBean.getPaymentVerification().getLast4())){
			
			objCOOSFullDetailResponse.setPaymentCardLast4(OrderSearchUtil.populateCardNumber(objOrderSearchDetailFormBean.getPaymentVerification().getTypeCode(),objOrderSearchDetailFormBean.getPaymentVerification().getLast4()));
		}
		
		if(!Util.isBlankOrNull(objOrderSearchDetailFormBean.getPaymentVerification().getExpMonth())&& !Util.isBlankOrNull(objOrderSearchDetailFormBean.getPaymentVerification().getExpYear())) {
			
			objCOOSFullDetailResponse.setPaymentExpirationDate(OrderSearchUtil.populateSavedExpirationDate(objOrderSearchDetailFormBean.getPaymentVerification().getExpMonth(),objOrderSearchDetailFormBean.getPaymentVerification().getExpYear()));
		} 
		objCOOSFullDetailResponse.setPaymentReceiptEmail(objOrderSearchDetailFormBean.getPaymentVerification().getEmailAddress());
		
		// Get CarrierServiceLevel and ThirdPartyAccount
		OrderHeaderVOKey orderKey = new OrderHeaderVOKey(request.getOrderID());
		IOEManageOrdersComponent oeComp = oeManageOrdersLocatorService.locate(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		OrderInfoShippingVO orderInfoShipping=oeComp.getOrderInfoShipping(new OrderShippingVOKey(orderKey));
		if(orderInfoShipping !=null) {
			
			objCOOSFullDetailResponse.setCarrierServiceLevel(orderInfoShipping.getServiceTypeDescriptionTxt()); 
			objCOOSFullDetailResponse.setThirdPartyAccount(orderInfoShipping.getThirdPartyShipperNum());
		}
		//CAP-41713
		setShowBillToInfoAndAttn(objCOOSFullDetailResponse, sc);
		
		//CAP-45058	Added to set the orderMessages
		Collection<OrderStatusMessage> orderMessageRequiredTypes = objOrderSearchDetailFormBean.getOrderMessages()
				.getOrderMessages(OrderStatusMessage.Type.All);
		orderMessageRequiredTypes.addAll(
				objOrderSearchDetailFormBean.getOrderMessages().getOrderMessages(OrderStatusMessage.Type.Billing));
		orderMessageRequiredTypes.addAll(objOrderSearchDetailFormBean.getOrderMessages()
				.getOrderMessages(OrderStatusMessage.Type.RecordedSequences));
		orderMessageRequiredTypes.addAll(objOrderSearchDetailFormBean.getOrderMessages()
				.getOrderMessages(OrderStatusMessage.Type.ShippingCustomer));
		 
		List<OrderMessages> orderMessages = new ArrayList<>();
		for (OrderStatusMessage orderStatusMessageIterator : orderMessageRequiredTypes) {
			OrderMessages orderMessagesTypeAndText = new OrderMessages();
			orderMessagesTypeAndText.setMessageType(orderStatusMessageIterator.getMessageType().getCode());
			orderMessagesTypeAndText.setMessageText(orderStatusMessageIterator.getMessageText());
			orderMessages.add(orderMessagesTypeAndText);
		}
		objCOOSFullDetailResponse.setOrderMessages(orderMessages);
		
		// CAP-48488 - set order expedite Info
		setExpediteInfo(sc, objCOOSFullDetailResponse, objOrderSearchDetailFormBean);
		
		//CAP-45812
		if(request.getOrderID()>0) {	
			OrderHeaderVO header = getOrderHeader(request.getOrderID());
			objCOOSFullDetailResponse=setOrginatorValues(objCOOSFullDetailResponse,header,sc);
		}	
		objCOOSFullDetailResponse.setSuccess(true);
		return objCOOSFullDetailResponse;
	}
	
	// CAP-48488
	public void setExpediteInfo(SessionContainer sc, COOSFullDetailResponse coosFullDetailResponse,
			OrderSearchDetailFormBean osDetailFormBean) throws AtWinXSException {

		OrderSearchDetailFormBeanImpl osDetailFormBeanImpl = (OrderSearchDetailFormBeanImpl) osDetailFormBean;

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();

		IOEManageOrdersComponent oeManageOrdersComponent = oeManageOrdersComponentLocatorService
				.locate(asb.getCustomToken());
		OrderShippingVO shippingVO = oeManageOrdersComponent
				.getOrderShipping(osDetailFormBean.getHeaderVo().getXsOrderID());
		
		//CAP-48488 - Get Expedited Order Date from XST081, 
		//CAP-48488 - Expedite fee from EXPEDITE_ORDER_STATUS_WCSS_SERVICE_NUMBER(11806)
		if (shippingVO != null && shippingVO.getExpeditedOrderDate() != null) {

			coosFullDetailResponse.setExpedited(true);
			coosFullDetailResponse.setExpeditedDate(osDetailFormBeanImpl.getExpediteDueDate());
			coosFullDetailResponse.setExpeditedFee(osDetailFormBeanImpl.getExpediteFee());
		}
	}

	//CAP-45812 seperated this as method to handle profile null
	public COOSFullDetailResponse setOrginatorValues(COOSFullDetailResponse objCOOSFullDetailResponse,
			OrderHeaderVO header, SessionContainer sc) throws AtWinXSException {
		if (header.getOriginatorProfileNum() > 0 && !header.getLoginID().equalsIgnoreCase(header.getOriginatorLoginID())
				&& header.getProfileNum() != header.getOriginatorProfileNum()) {
			Profile prfl = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class,
					sc.getApplicationSession().getAppSessionBean().getCustomToken());
			prfl.populate(header.getOriginatorProfileNum());
			if (null != prfl.getContactFirstName() && null != prfl.getContactLastName()) {
				objCOOSFullDetailResponse
						.setOrderOriginator(Util.nullToEmpty(prfl.getContactFirstName()) + " " + Util.nullToEmpty(prfl.getContactLastName()));
				objCOOSFullDetailResponse.setOrderOriginatorLoginID(Util.nullToEmpty(header.getOriginatorLoginID()));
				objCOOSFullDetailResponse.setOrderOriginatorProfileID(Util.nullToEmpty(String.valueOf(prfl.getProfileID())));//CAP-46677
			} else {
				objCOOSFullDetailResponse.setOrderOriginatorLoginID(Util.nullToEmpty(header.getOriginatorLoginID()));
				objCOOSFullDetailResponse.setOrderOriginatorProfileID(Util.nullToEmpty(String.valueOf(prfl.getProfileID())));//CAP-46677
			}
		}

		if (header.getOriginatorProfileNum() == -1
				&& !header.getLoginID().equalsIgnoreCase(header.getOriginatorLoginID())
				&& header.getProfileNum() != header.getOriginatorProfileNum()) {
			Profile prfl = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class,
					sc.getApplicationSession().getAppSessionBean().getCustomToken());
			
			User user = objectMapFactoryService.getEntityObjectMap().getEntity(User.class,
					sc.getApplicationSession().getAppSessionBean().getCustomToken());
			user.populate(header.getSiteID(), header.getOriginatorLoginID());
			objCOOSFullDetailResponse.setOrderOriginator(Util.nullToEmpty(user.getSharedIDDesc())+" ("+header.getOriginatorLoginID()+")");
			objCOOSFullDetailResponse.setOrderOriginatorLoginID(Util.nullToEmpty(header.getOriginatorLoginID()));
			objCOOSFullDetailResponse.setOrderOriginatorProfileID(Util.nullToEmpty(String.valueOf(null==prfl.getProfileID()?"":prfl.getProfileID())));//CAP-46677
		}
		return objCOOSFullDetailResponse;
	}

	//CAP-36557 - Copied and use logic from  OrderSearchDetailController->prepopulateCommandObject()
	public OrderSearchDetailFormBean getOrderDetail(SessionContainer sc,
			COOSFullDetailRequest request) throws AtWinXSException {

		OrderStatusSessionBean osBean = ((OrderStatusSession) sc.getModuleSession()).getOrderStatusSettingsBean();

		((OrderStatusSession) sc.getModuleSession()).setClearParameters(false);

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		ApplicationVolatileSession volatileSession =  sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();


		// Create our form bean to hold information for the page
		OrderSearchDetailFormBean formBean = objectMapFactoryService.getEntityObjectMap().getEntity(OrderSearchDetailFormBean.class, appSessionBean.getCustomToken());

		// Process the request, grabbing the parameters out of it -> use logic from processPopulateRequest(request, formBean)
		formBean.setReqSearchResultNumber(Util.nullToEmpty(request.getSearchResultNumber()));
		formBean.setReqCampSearchResultNbr(AtWinXSConstant.EMPTY_STRING);
		formBean.setReqSortBy(AtWinXSConstant.EMPTY_STRING);
		formBean.setReqBatchOrderSearch("false");
		formBean.setReqUseCache("N");


		// Initialize the form bean for the search
		formBean.initialize(appSessionBean, osBean);
		formBean.createActions(appSessionBean, volatileSessionBean.getShoppingCartCount(),osBean);//CAP-309 EIQ

		formBean.buildBackToLinkUrl(appSessionBean.getEncodedSessionId());

		//CP-12465 RAR - Set the OSBatchOrderStatusBean.
		osBean.setBatchOrderStatusBean(formBean.getOSBatchOrderStatusBean());

		return formBean;
	}


	//CAP-36557 -  method copied from CP = com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl->createActionOrderFile() due to protected scope
	protected void setActionOrderFile(SessionContainer sc, COOSFullDetailResponse objCOOSFullDetailResponse, OrderSearchDetailFormBean orderSearchDetailFormBean)
			throws AtWinXSException
	{

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		OrderStatusSearch osSearch = objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusSearch.class,appSessionBean.getCustomToken());
		osSearch.initialize(appSessionBean, null);

		OrderStatusHeaderVO headerVo = orderSearchDetailFormBean.getHeaderVo();
		// CAP-41681 - slightly alter the checks since email visibility is almost always true
		boolean allowEmailVisibility = (appSessionBean.isShowFeature(LookAndFeelFeature.FeatureNames.UXEV.toString(), Util.ContextPath.Usability.getContextPath()));
		objCOOSFullDetailResponse.setEmailVisibility(allowEmailVisibility);
		boolean displayXSFiles = ((headerVo.getXsOrderID() > 0) &&
				isDisplayOrderFiles(objCOOSFullDetailResponse, appSessionBean, headerVo) || allowEmailVisibility);
		if (displayXSFiles)
		{
			OrderStatusAction orderFileAction = osSearch.createViewOrdFilesAction(headerVo.getXsOrderID());
			if (orderFileAction != null) {

				objCOOSFullDetailResponse.setViewEmailOrderfilesLabel(orderFileAction.getLabel(ActionEnum.VIEW_ORDER_FILES, appSessionBean.getDefaultLocale()));
				objCOOSFullDetailResponse.setViewEmailOrderfilesImg(ModelConstants.ORDER_COOS_EMAILSORDER_FILE_IMG);
			}
		}
	}

	//CAP-36557 -  method copied from CP = com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl due to protected scope
	// CAP-41681 - most of the parameters were not needed
	protected boolean isDisplayOrderFiles(COOSFullDetailResponse objCOOSFullDetailResponse,
			AppSessionBean appSessionBean, OrderStatusHeaderVO headerVo)	throws AtWinXSException	{

		if (objCOOSFullDetailResponse.getIsDisplayOrderFiles() == null)
		{
			objCOOSFullDetailResponse.setIsDisplayOrderFiles(false);

			// If we have a custompoint order, we need to see if we should display the order files icon
			if ((headerVo != null) && (headerVo.getXsOrderID() > 0)) {
				try
				{ // this hasOrderFiles flag is never set appropriately, so checking it is useless
					objCOOSFullDetailResponse.setIsDisplayOrderFiles(orderHasOrderFilesContent(appSessionBean, headerVo.getXsOrderID()));
				}
				catch(Exception ex)	{
					objCOOSFullDetailResponse.setIsDisplayOrderFiles(false);
				}
			}
		}

		return objCOOSFullDetailResponse.getIsDisplayOrderFiles();
	}


	//CAP-36557 -  method copied from CP = com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl due to protected scope
	protected boolean orderHasOrderFilesContent(AppSessionBean appSessionBean, int orderID)	throws AtWinXSException	{
		// CAP-41681 - fixed efficiency problems
		boolean hasFiles = false;
		try { // do not blow this up if any checks fail - just return false
			EFDTracking efdTracking = getEFDTracking(appSessionBean, orderID);
			if (efdTracking!=null && efdTracking.getTrackingID()>0)	{
				hasFiles = true;
			} else {
				OrderHeaderVO header = getOrderHeader(orderID);
				if (header.getListID() > 0)
				{
					ListVO list = getOrderHeaderList(appSessionBean.getSiteID(), header.getBusinessUnitID(), header.getListID());
					if ((list != null) && (list.getLoginID().equals(appSessionBean.getLoginID()) || !list.isPrivate()))	{
						hasFiles = true;
					}
				}
			}

			if (!hasFiles)	{
				hasFiles = checkOrderCustomDocsForFiles(appSessionBean, orderID);
			}
		} catch(Exception e)
		{
			logger.error(e.toString());
			hasFiles = false;
		}
		return hasFiles;
	}

	// CAP-41681 - added this method to look up cust doc lines for order and loop over checking those
	protected boolean checkOrderCustomDocsForFiles(AppSessionBean appSessionBean, int orderID) throws AtWinXSException {
		boolean hasFiles = false;
		Collection<Integer> cdLines = getCustomDocOrderLinesForOrder(orderID, appSessionBean.getCustomToken());
		if ((cdLines != null) && (!cdLines.isEmpty()))
		{
			CDFilesInformation cdFilesInformation = ObjectMapFactory.getEntityObjectMap().getEntity(CDFilesInformation.class, appSessionBean.getCustomToken());
			IOECustomDocumentComponent iOECustomDocumentComponent = OECustomDocLocator.locate(appSessionBean.getCustomToken());
			CustomDocumentOrderLine cdOrderLine = ObjectMapFactory.getEntityObjectMap().getEntity(CustomDocumentOrderLine.class, null);
			for (Integer custDocOrderLineID : cdLines)	{
				hasFiles = checkSingleCustomDocForFiles(appSessionBean, orderID, custDocOrderLineID, cdFilesInformation,
						iOECustomDocumentComponent, cdOrderLine);
    			if (hasFiles) {
    				break;
    			}
			}
		}
		return hasFiles;
	}

	// CAP-41681 - Added this method to check if a single custom document line has files
	protected boolean checkSingleCustomDocForFiles(AppSessionBean appSessionBean, int orderID, int custDocOrderLineID, CDFilesInformation cdFilesInformation, IOECustomDocumentComponent iOECustomDocumentComponent, CustomDocumentOrderLine cdOrderLine) throws AtWinXSException {
		boolean hasFiles = false;
   		cdOrderLine.populate(custDocOrderLineID);
		hasFiles = OrderDetailsUtils.checkForProofs(custDocOrderLineID, appSessionBean.getCurrentEnvCd(), appSessionBean.getCustomToken());
		if (!hasFiles) {
			OrderLineVO lineVO = iOECustomDocumentComponent.getOrderLineVO(custDocOrderLineID);
			cdFilesInformation.initialize(orderID, (lineVO != null) ? lineVO.getLineNum() : AtWinXSConstant.INVALID_ID, cdOrderLine, appSessionBean.getCurrentEnvCd());
   			hasFiles = checkForPrintFiles(cdFilesInformation.getPrintFiles(), orderID, custDocOrderLineID, appSessionBean.getCurrentEnvCd());

   			if (!hasFiles) {
   				hasFiles = checkForEFDFiles(cdFilesInformation.getEfdFiles(), orderID, custDocOrderLineID, appSessionBean.getCurrentEnvCd());
   			}
		}
 		return hasFiles;
	}

	// CAP-41681 - looking up list for order without instantiating order object
	protected ListVO getOrderHeaderList(int site, int bu, int listID) throws AtWinXSException {
		IManageList manageList = ManageListsLocator.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		return manageList.retrieveAList(new ListVOKey(site, bu, listID));
	}

   // CAP-41681 - looking up EFD Tracking for order without instantiating order object
	protected EFDTracking getEFDTracking(AppSessionBean appSessionBean, int orderID) {
		EFDTracking efdTracking = objectMapFactoryService.getEntityObjectMap().getEntity(EFDTracking.class, appSessionBean.getCustomToken());
		efdTracking.populateByOrderID(orderID);
		return efdTracking;
	}

	// CAP-41681 - looking up custom document order lines for the order ID
	public Collection<Integer> getCustomDocOrderLinesForOrder(int orderID, CustomizationToken token ) throws AtWinXSException
	{
		CustomDocumentOrderLine custDocOrdLn = ObjectMapFactory.getEntityObjectMap().getEntity(CustomDocumentOrderLine.class, token);
		return custDocOrdLn.getCustomDocOrderLinesForOrder(orderID);
	}


	//CAP-36557 -  	method copied from CP = com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl as method called from protected scope
	public boolean checkForPrintFiles(Collection<String> printFiles, int orderID, int custDocOrderLineID, String currentEnv)
			throws AtWinXSException
	{
		boolean hasFiles = false;
		for (String fileName: printFiles) {

			hasFiles = OrderDetailsUtils.checkForFile(orderID, custDocOrderLineID, currentEnv, fileName, OrderEntryConstants.EFD_SOURCES_PRINT);
			if (hasFiles)	{
				break;
			}
		}

		return hasFiles;
	}

	//CAP-36557 -  method copied from CP = com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl as method called from protected scope
	public boolean checkForEFDFiles(Map<String, Collection<String>> efdFiles,
			int orderID, int custDocOrderLineID, String currentEnv)	throws AtWinXSException	{

		boolean hasFiles = false;
	    for (Map.Entry<String,Collection<String>> entry : efdFiles.entrySet()) {

	    	Collection<String> files = entry.getValue();

	    	for (String fileName: files)
			{
				hasFiles = OrderDetailsUtils.checkForFile(orderID, custDocOrderLineID, currentEnv, fileName, entry.getKey());
				if (hasFiles) {

					break;
				}
			}
			if (hasFiles) {

				break;
			}
	    }

		return hasFiles;
	}




	//CAP-36557 - method Copied from CustomPointWeb OSDetailsItemsOrderedServiceImpl->getItemsOrdered()
	/**
	 * Method to getItemsOrdered for a order number, sales ref. number, wcss order number and sortLinesBy order
	 *
	 * @param SessionContainer
	 * @param COOSFullDetailRequest
	 * @return List<COOSDetailsItemsOrderedData>
	 */
	@Override
	public List<COOSDetailsItemsOrderedData> getItemsOrdered(SessionContainer sc, COOSFullDetailRequest request)
			throws AtWinXSException
	{
		int orderID = request.getOrderID(); // CAP-41840
		String orderNum = request.getOrderNum();
		String salesReferenceNumber = request.getSalesReferenceNumber();
		String wcssOrderNumber = request.getOrderNum();
		String sortLinesBy = request.getSortLinesBy();

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		List<COOSDetailsItemsOrderedData> orderedItemsData = new ArrayList<>();

		// CP-12008 [PDN] instantiate the Entity class to get the collection of Items Ordered
		OSDetailsItemsOrdered orderedItems = objectMapFactoryService.getEntityObjectMap().getEntity(OSDetailsItemsOrdered.class, appSessionBean.getCustomToken());
		orderedItems.populate(orderID, appSessionBean.getSessionID(), orderNum, salesReferenceNumber, wcssOrderNumber, appSessionBean.getEncodedSessionId(), sortLinesBy); // CAP-2951

		Collection <OSDetailsItemOrdered>  listOrderedItems = orderedItems.getOsItemsOrdered();

		// CP-12008 [PDN] Convert the collection of Item Ordered from backend to a collection of Items Ordered - GWT bound.
		if (null != listOrderedItems) {

			for (OSDetailsItemOrdered itemOrdered : listOrderedItems) {

				COOSDetailsItemsOrderedData orderedItemData = new COOSDetailsItemsOrderedData();
				orderedItemData.setLineNumber(String.valueOf(itemOrdered.getLineNumber()));
				orderedItemData.setTextLineNumber(itemOrdered.getTextLineNumber());
				orderedItemData.setOrderID(itemOrdered.getOrderID());
				orderedItemData.setCustomerItemNumber(itemOrdered.getCustomerItemNumber());
				orderedItemData.setVendorItemNumber(itemOrdered.getVendorItemNumber());
				orderedItemData.setItemDesc(itemOrdered.getItemDesc());
				orderedItemData.setItemStatus(itemOrdered.getItemStatusDesc());
				orderedItemData.setItemUOM(itemOrdered.getItemUOM());
				orderedItemData.setItemQty(itemOrdered.getItemQty());
				orderedItemData.setItemShippedQty(itemOrdered.getItemShippedQty());
				orderedItemData.setItemUnitPrice(itemOrdered.getItemUnitPrice());
				orderedItemData.setItemExtPrice(itemOrdered.getItemExtPrice());
				orderedItemData.setItemBackordered(String.valueOf(itemOrdered.getItemBackordered()));
				orderedItemData.setHoldCodes(itemOrdered.getHoldCodes());
				orderedItemData.setUomFactor(itemOrdered.getUomFactor());
				//CAP-40284 C1UX BE - API Fix - Item UOM translation was incorrect with getItemUOM, so replaced with getUomFactor for getorderdetails-forsalesref
				orderedItemData.setItemUOMFactor(ItemUtility.getUOMAcronyms(orderedItemData.getUomFactor().replace(
                        ModelConstants.UOM_FACTOR_FORMAT_WITHSLASH, " "+TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"ofLbl")+" "), true,appSessionBean));
				orderedItemData.setKitLineTypeCode(itemOrdered.getKitLineTypeCode());
				orderedItemData.setItemClassification(itemOrdered.getItemClassification());

				List<Map<String, String>> lineMessages = getOrderLineMessages(itemOrdered.getLineMessages()); // CAP-5774
				orderedItemData.setLineMessages(lineMessages);

				List<Map<String, String>> lineCustRefs = getOrderLineCustRefs(itemOrdered.getLineCustomerReferences());
				orderedItemData.setLineCustRefs(lineCustRefs);
				//CAP-39159 - Item Image path must reference C1UX no image instead of CP no image
				String itemImagePath = itemOrdered.getItemImage();
				if (Util.isBlankOrNull(itemImagePath) || ModelConstants.CP_NO_IMAGE.equalsIgnoreCase(itemImagePath)) {
					itemImagePath = ModelConstants.C1UX_NO_IMAGE_MEDIUM;
				}
				orderedItemData.setItemImage(itemImagePath);
				orderedItemsData.add(orderedItemData);
			}
		}

		return orderedItemsData;
	}


	//CAP-36557 - method Copied from CustomPointWeb OSDetailsItemsOrderedServiceImpl->getOrderLineMessages()
	/**
	 * Method to convert Collection of OrderMessage to List of map of line messages
	 *
	 * @param lineMessages
	 * @return
	 */
	protected List<Map<String, String>> getOrderLineMessages(Collection<OrderMessage> lineMessages) {

		List<Map<String, String>> lineMessagesList = null;

		if (null != lineMessages && !lineMessages.isEmpty()) {

			lineMessagesList = new ArrayList<>();

			for (OrderMessage lineMessage : lineMessages)
			{
				if (!lineMessage.isWinXSOnlyMessage()) {
					Map<String, String> lineMessagesMap = new HashMap<>();
					lineMessagesMap.put(lineMessage.getLabel(), lineMessage.getValue());
					lineMessagesList.add(lineMessagesMap);
				}
			}
		}

		return lineMessagesList;
	}


	//CAP-36557 - method Copied from CustomPointWeb OSDetailsItemsOrderedServiceImpl->getOrderLineCustRefs()
	/**
	 * Method to convert Collection of CustomerReference to List of map of line cust refs
	 *
	 * @param lineCustomerReferences
	 * @return
	 */
	protected List<Map<String, String>> getOrderLineCustRefs(Collection<CustomerReference> lineCustomerReferences) {

		List<Map<String, String>> lineCustRefsList = null;

		if (null != lineCustomerReferences && !lineCustomerReferences.isEmpty()) {
			lineCustRefsList = new ArrayList<Map<String, String>>();

			for (CustomerReference lineCustRef : lineCustomerReferences) {
				Map<String, String> lineCustRefsMap = new HashMap<>();
				lineCustRefsMap.put(lineCustRef.getLabel(), lineCustRef.getValue());
				lineCustRefsList.add(lineCustRefsMap);
			}
		}

		return lineCustRefsList;
	}

	//CAP-37295 - Order search - Order Status Dropdown Menu values
	/* Existing CP code using here
	 * Package : com.rrd.custompoint.service.orderstatus
	 * Class   : OrderSearchServiceImpl
	 * Method  : getOrderStatusCodes(Chaw chawSession, String type) */

	@Override
	public OrderStatusCodeResponse populateOrderStatus(SessionContainer sc, OrderStatusCodeRequest request) throws AtWinXSException// CAP-2951
	{
			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			OrderStatusCodeResponse response=new OrderStatusCodeResponse();

			OrderStatusCodes osc = ObjectMapFactory.getEntityObjectMap().getEntity(OrderStatusCodes.class, appSessionBean.getCustomToken());
			Collection<OrderStatusCode> statusCodes = null;
			try
			{
				osc.populateOrderStatusCodes();
				statusCodes = osc.getOrderStatusCodes();
			} catch (AtWinXSException e)
			{
				// CAP-16460 call to logger.
				logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
			}

			List<COSharedOrderStatusCode> sharedOSCList = new ArrayList<>();
			if (statusCodes != null && !statusCodes.isEmpty())
			{
				String selectAStatus = getTranslatedDescription("ordersearch_orderstatus_select", appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken());
				COSharedOrderStatusCode initial = new COSharedOrderStatusCode();
				initial.setSimpleDescriptionTag(selectAStatus);
				initial.setSimpleTagTranslation(selectAStatus);
				initial.setAdvancedDescriptionTag(selectAStatus);
				initial.setAdvancedTagTranslation(selectAStatus);
				initial.setCode(AtWinXSConstant.EMPTY_STRING);
				sharedOSCList.add(initial);

        // Commented as per review comments
		/*		if ("A".equalsIgnoreCase(request.getType()))
				{
					// CP-11879 Start
					boolean showBatch = false;
					OrderStatusSession oss = (OrderStatusSession) sc.getModuleSession();
					if (oss != null && oss.getOrderStatusSettingsBean() != null)
					{
						showBatch = oss.getOrderStatusSettingsBean().isShowBatchOrderTab();
					}

					boolean isAllowRMS = true; //CAP-5779 8.1.21 [SRN] Change the default value to TRUE.
				//	SessionContainer scon = initOESession(ttSession); // CAP-2951
					OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
					//CAP-5779 8.1.21 [SRN] Added a null checking
					if (oeSessionBean.getUserSettings() != null)
					{
						isAllowRMS = oeSessionBean.getUserSettings().isAllowRMS();
					}

					Set<String> codesToCheck = new HashSet<String>();
					codesToCheck.add(SystemCode.DSA.toString());
					codesToCheck.add(SystemCode.TRK.toString());
					codesToCheck.add(SystemCode.RMS.toString()); // CP-11879 TH -
																	// Fixed code
																	// here

					COSharedOrderStatusCode sosc = null;
					boolean addOrderStatusCode = false;
					for (OrderStatusCode scode : statusCodes)
					{
						// Add batch codes only when batch is enabled
						if ((scode.getSystemCode().toString().equalsIgnoreCase(SystemCode.DSA.toString()) || scode.getSystemCode().toString().equalsIgnoreCase(SystemCode.TRK.toString()))
								&& showBatch)
						{
							addOrderStatusCode = true;
						}
						// CP-11879 TH - Fixed this here
						// Add RMS codes only when RMS is enabled
						else if (scode.getSystemCode().toString().equalsIgnoreCase(SystemCode.RMS.toString()) && isAllowRMS)
						{
							addOrderStatusCode = true;
						} else if (scode.getSystemCode().toString().equalsIgnoreCase(SystemCode.RTD.toString()))
						{
							// Never add the RTD status codes here
							addOrderStatusCode = false;
						}
						// If it's not a batch code or RMS code then Add
						else if (!codesToCheck.contains(scode.getSystemCode().toString()))
						{
							addOrderStatusCode = true;
						} else
						{
							addOrderStatusCode = false;
						}

						if (addOrderStatusCode)
						{
							sosc = new COSharedOrderStatusCode(scode.getCode(), scode.getSimpleDescription(), scode.getAdvancedDescription(), scode.getDefinition(), scode.getSimpleDefinition(),
									scode.isWebToOne(), scode.getWeight(), scode.getSimpleDescriptionTag(), scode.getAdvancedDescriptionTag(), scode.isDisplayTracking(), scode.getWcssGreaterSearch(),
									scode.getWcssLessSearch(), scode.getWcssEqualSearch());

							String translation = getTranslatedDescription(scode.getAdvancedDescriptionTag(), appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken());
							sosc.setAdvancedTagTranslation(translation);

							sharedOSCList.add(sosc);
						}
						// CP-11879 End
					}
				} else
				{
				 */


					Map<String, COSharedOrderStatusCode> soscMap = new HashMap<>();
					Iterator<OrderStatusCode> it = statusCodes.iterator();
					COSharedOrderStatusCode sosc = null;
					OrderStatusCodeImpl temp = null;
					while (it.hasNext())
					{
						temp = (OrderStatusCodeImpl) it.next();

						sosc = new COSharedOrderStatusCode(temp.getCode(), temp.getSimpleDescription(), temp.getAdvancedDescription(), temp.getDefinition(), temp.getSimpleDefinition(),
								temp.isWebToOne(), temp.getWeight(), temp.getSimpleDescriptionTag(), temp.getAdvancedDescriptionTag(), temp.isDisplayTracking(),
								temp.getWcssGreaterSearch(), temp.getWcssLessSearch(), temp.getWcssEqualSearch());

						String translation = getTranslatedDescription(temp.getSimpleDescriptionTag(), appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken());
						sosc.setSimpleTagTranslation(translation);

						soscMap.put(temp.getSimpleDescriptionTag(), sosc);
					}
					sharedOSCList.addAll(soscMap.values());
				}
//			}

			if(!sharedOSCList.isEmpty())
			  response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
			else
			  response.setStatus(RouteConstants.REST_RESPONSE_FAIL);

			response.setCosharedOrderStatusCodeList(sharedOSCList);

			//CAP-38737 - API Standardization - Search Navigation in Order Search conversion to standards
			response.setSuccess(true);

			return response;
		}


	public String getTranslatedDescription(String tag, Locale locale, CustomizationToken token)
	{
		String text =  AtWinXSConstant.EMPTY_STRING;

		if (!Util.isBlankOrNull(tag))
		{
			if (locale == null)
			{
				locale = Locale.getDefault();
			}

			try
			{
				text = TranslationTextTag.processMessage(locale, token, tag);
			} catch (Exception ex)
			{
				// For this exception, do nothing
			}
		}

		// return text
		return text;
	}



	/**
	 * Method to get Shipments & Tracking details for a Order Number /Sales Ref Number
	 *
	 * @param SessionContainer
	 * @param COOSShipmentAndTrackingRequest
	 * @return COOSShipmentAndTrackingResponse
	 */
	//CAP-36916 -	Get Shipments and Items under Shipping and tracking information
	@Override
	public COOSShipmentAndTrackingResponse getOSShipmentAndTracking(SessionContainer sc, COOSFullDetailRequest request)
			throws AtWinXSException {

		return getTrackingDetails(sc, request);
	}


	/**
	 * Method to get Items in Shipment details for a Order Number /Sales Ref Number
	 *
	 * @param SessionContainer
	 * @param COOSItemInShipmentTrackingRequest
	 * @return COOSItemInShipmentTrackingResponse
	 */
	//CAP-36916 -	Get Items and tracking Information under Shipments
	@Override
	public COOSShipmentDetail getOSItemsInShipment(SessionContainer sc, COOSItemInShipmentTrackingRequest request)
			throws AtWinXSException {

		return getItemsInShipmentDetails(sc, request.getWcssOrderNumber(), request.getSeqNo(), request.getSalesRefNum(), request.getOrderLineNum());
	}

	//CP-11757 [PVT] - reduced fields for tracking details only
	/**
	 * CP-11757-OSOD-[PVT]
	 * this method builds a collection of tracking details that contains unique tracking details and its individual collection of shipment details
	 * to be called by the GWT method for building the panel
	 * @param sessionID
	 * @param wcssOrderNumber
	 * @return
	 * @throws AtWinXSException
	 */
	//CAP-36916 - CP method copied from CustomPointWeb com.rrd.custompoint.service.orderstatus.OSShipmentAndTrackingServiceImpl->getTrackingDetails(), ignored GWT dependency
	public COOSShipmentAndTrackingResponse getTrackingDetails(SessionContainer sc, COOSFullDetailRequest request) throws AtWinXSException // CAP-2951
	{
		//CAP-36916 - Replaced CP code to retrieve AppSessionBean from SessionContainer derived from Rest controller
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		OrderStatusSessionBean osBean = ((OrderStatusSession) sc.getModuleSession()).getOrderStatusSettingsBean();
		((OrderStatusSession) sc.getModuleSession()).setClearParameters(false);

		COOSShipmentAndTrackingResponse osShipmentAndTrackingDetail = new COOSShipmentAndTrackingResponse();
		osShipmentAndTrackingDetail.setOrderNo(Util.nullToEmpty(request.getOrderNum()));
		osShipmentAndTrackingDetail.setSalesRefNum(Util.nullToEmpty(request.getSalesReferenceNumber()));
		osShipmentAndTrackingDetail.setShowPrice(osBean.isShowPrice());
		
		//CAP-45429
		if (osShipmentAndTrackingDetail.getSalesRefNum().equals("(Unavailable)")) {
			osShipmentAndTrackingDetail.setSalesRefNum(AtWinXSConstant.EMPTY_STRING);
		}

		// CAP-36916 - initialize to copy the cache based of previous search
		OrderSearchDetailFormBean formBean = objectMapFactoryService.getEntityObjectMap().getEntity(OrderSearchDetailFormBean.class, appSessionBean.getCustomToken());
		formBean.setReqSearchResultNumber(Util.nullToEmpty(request.getSearchResultNumber()));
		formBean.setReqCampSearchResultNbr(AtWinXSConstant.EMPTY_STRING);
		formBean.setReqSortBy(AtWinXSConstant.EMPTY_STRING);
		formBean.setReqBatchOrderSearch("false");
		formBean.setReqUseCache("N");

		try
		{
			formBean.initialize(appSessionBean, osBean);

		}
		catch (AtWinXSException e)
		{
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
			osShipmentAndTrackingDetail.setShipmentAvailabilityFlag(false);
			osShipmentAndTrackingDetail.setShipmentAvailabilityMessage(e.getMessage());
			return osShipmentAndTrackingDetail;
		}

		// Build the collection of Tracking Info
		Collection<OrderStatusTrackingInfo> orderStatusTrackingInfos = null;
		Collection<COOSShipmentDetail> trackingDetails = null;
		OrderStatusTrackingInfo osStatusTrackingInfo = objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusTrackingInfo.class, appSessionBean.getCustomToken());
		// CP-11757 [PVT] - removed declared Collection<OSShipmentDetail>

		String encodedSessionId = sc.getApplicationSession().getAppSessionBean().getEncodedSessionId();
		orderStatusTrackingInfos = osStatusTrackingInfo.getOrderStatusShipmentAndTrackingInfo(encodedSessionId, request.getOrderNum(), request.getSalesReferenceNumber());//CAP-708 EIQ // CAP-2951
		//CAP-39049 : Add null check for headerVo
		if (formBean.getHeaderVo() != null) {
			//CP-11757 AC -  Check if we have tracking infos
			if (osShipmentAndTrackingDetail.getOrderNo().equals(formBean.getHeaderVo().getOrderNum())
					&& osShipmentAndTrackingDetail.getSalesRefNum().equals(formBean.getHeaderVo().getSalesOrderNum())
					&& orderStatusTrackingInfos != null && !orderStatusTrackingInfos.isEmpty()) {
				//CP-11757 [PVT] - instantiate tracking details if shipment information exists
				trackingDetails = new ArrayList<>();
				Map<String, Object> keyStore = new HashMap<>();

				// Loop through the tracking infos
				setShipmentAndTrackingDetails(appSessionBean, orderStatusTrackingInfos, trackingDetails, keyStore);
				osShipmentAndTrackingDetail.setShipmentAvailabilityFlag(true);
				osShipmentAndTrackingDetail.setShipmentAvailabilityMessage(ModelConstants.SHIPMENT_INFO_AVAILABLE);
			} else {
				osShipmentAndTrackingDetail.setShipmentAvailabilityFlag(false);
				osShipmentAndTrackingDetail.setShipmentAvailabilityMessage(ModelConstants.NO_SHIPMENT_INFO_AVAILABLE);
			}
		}
		osShipmentAndTrackingDetail.setTrackingDetails(trackingDetails);
		return osShipmentAndTrackingDetail;
	}
	//CAP-39049: Refactored method to eliminate complexity
	protected void setShipmentAndTrackingDetails(AppSessionBean appSessionBean,
			Collection<OrderStatusTrackingInfo> orderStatusTrackingInfos,
			Collection<COOSShipmentDetail> trackingDetails, Map<String, Object> keyStore) {
		XSCurrency currencyFormatter;
		for (OrderStatusTrackingInfo currentInfo : orderStatusTrackingInfos) {
			//CP-13030 RAR - Make sure to display only unique sequence and tracking combination.
			String currentKey = currentInfo.getSequenceNumberStr() + "|" + currentInfo.getTrackingNum();

			COOSShipmentDetail trackingDetail = new COOSShipmentDetail();
			trackingDetail.setCarrier(currentInfo.getCarrier()); // Carrier
			trackingDetail.setShipMethod(currentInfo.getShipMethod()); // Service
			trackingDetail.setTrackingNum(currentInfo.getTrackingNum()); // Tracking No.
			trackingDetail.setTrackingURL(currentInfo.getTrackingURL()); // Tracking URL.
			trackingDetail.setSequenceNumberStr(currentInfo.getSequenceNumberStr());
			trackingDetail.setShipQty(currentInfo.getShipQty()); // "Shipped" items in shipment
			trackingDetail.setSequenceNumber(currentInfo.getSequenceNumber()); // Ship No.
			trackingDetail.setStatus(currentInfo.getStatus()); // Status
			trackingDetail.setWarehouse(currentInfo.getWarehouse()); // Warehouse
			trackingDetail.setShipDate(orderStatusTrackingInfos != null
					? Util.getStringFromDate(currentInfo.getShipDate(), appSessionBean.getDefaultLocale())
					: AtWinXSConstant.EMPTY_STRING);

			//CP-13328 - Set shipDate as empty string if date format is "01/01/1601"
			String strShipDate = Util.getStringFromDate(currentInfo.getShipDate(),
					appSessionBean.getDefaultLocale());
			if (orderStatusTrackingInfos != null && "01/01/1601".equals(strShipDate)) {
				strShipDate = AtWinXSConstant.EMPTY_STRING;
			}
			trackingDetail.setShipDate(strShipDate);

			currencyFormatter = Util.getStringFromCurrency(currentInfo.getPrice(),
					appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
			trackingDetail.setPrice((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING
					: currencyFormatter.getAmountText()); // Price
			trackingDetail.setOrderLineNum(currentInfo.getOrderLineNum()); //CAP-708 EIQ set orderlinenumber, only place where we set the order line number fot tracking
			//check for sequence matches
			if (!keyStore.containsKey(currentKey)) {
				trackingDetails.add(trackingDetail);
				keyStore.put(currentKey, null);
			}
		}
	}


	/**
	 * CP-11757-OSOD-[PVT]
	 * method to retrieve remaining tracking details and items in shipment details per sequence number from a specific tracking information
	 */
	//CAP-36916 - CP method copied from CustomPointWeb com.rrd.custompoint.service.orderstatus.OSShipmentAndTrackingServiceImpl->getItemsInShipmentDetails(), ignored GWT dependency
	public COOSShipmentDetail getItemsInShipmentDetails(SessionContainer sc, String wcssOrderNumber, String seqNo, String salesRefNum, int orderLineNum) throws AtWinXSException//CAP-708 EIQ // CAP-2951
	{
		//CAP-36916 - Replaced CP code to retrieve AppSessionBean from SessionContainer derived from Rest controller
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		String appSessionID = String.valueOf(sc.getApplicationSession().getAppSessionBean().getTtSession().getId());
		String encodedSessionId = sc.getApplicationSession().getAppSessionBean().getEncodedSessionId();

		// Build the collection of Tracking Info
		Collection<OrderStatusTrackingInfo> orderStatusTrackingInfos = null;

		// CP-12281 8.1.4 [VEC] Create a flag that would determine if Vendor Item Number should be displayed or not
		// The Admin Setting used is the Show Vendor Item # on the User Group Detail
		boolean isShowVendorItemNumber = false;

		//CAP-38766 - Get rid of the load of OE Session on OS retrieve, removing code changes made under CAP-36916 under this code
		isShowVendorItemNumber = appSessionBean.isShowWCSSItemNumber();

		// CP-12180 8.1.4 [VEC] Created a collection of LineNumbers that would get the correct Shipment Details of the Shipped Items
		Collection<Integer> sequenceNumbers = new ArrayList<>();
		COOSShipmentDetail trackingDetail = new COOSShipmentDetail();

		//CAP-38766 - Replacing ObjectMapFactory to objectMapFactoryService
		OrderStatusTrackingInfo osStatusTrackingInfoCall = objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusTrackingInfo.class, appSessionBean.getCustomToken());
	
		try
		{
			// CP-11757 8.1.4 [VEC] Call the new method that would get the tracking details based on the Sequence Number
			orderStatusTrackingInfos = osStatusTrackingInfoCall.getOrderStatusShipmentAndTrackingInfoBySeqNo(encodedSessionId, wcssOrderNumber, seqNo , salesRefNum , orderLineNum);//CAP-708 EIQ // CAP-2951
		}
		catch (NullPointerException e)
		{
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
			trackingDetail.setItemInShipmentAvailabilityFlag(false);
			trackingDetail.setItemInShipmentAvailabilityMessage(ModelConstants.NO_ITEM_UNDERSHIPMENT_INFO_AVAILABLE);
		}

		// CP-11757 AC -  Check if we have tracking infos
		if (orderStatusTrackingInfos != null && !orderStatusTrackingInfos.isEmpty())
		{
			// We only need to populate the Tracking Details once except for the Shipment Details
			boolean isInitialTrackingSet = false;
					
			for(OrderStatusTrackingInfo orderStatusTrackingInfo : orderStatusTrackingInfos )
			{
				// Get the Line Numbers of the Line Item and store it on the collection
				sequenceNumbers.add(orderStatusTrackingInfo.getSequenceNumber());
				// If we've ran through this once, we don't need to update the Tracking Details anymore
				if(!isInitialTrackingSet)
				{
					//CAP-38766 Refactored method to eliminate complexity
					setTrackingDetailFields(orderLineNum, trackingDetail, orderStatusTrackingInfo, appSessionBean);

					// We do this in order to get the correct Unit for Kit Master
					osStatusTrackingInfoCall = orderStatusTrackingInfo;

					// Update value of the flag
					isInitialTrackingSet = true;
				}
			}
			
			// Go through the shipment details based on the sequence numbers of the Shipped Item's sequence number
			// Update parameters to include isShowVendorItemNumber flag
			// CP-12613 [PVT] - pass wcssOrder wcssOrderNumber
			Collection<COOSItemInShipmentDetail> itemInShipmentDetail = buildShipmentDetailsPerTrackingDetail(appSessionID, appSessionBean, sequenceNumbers, osStatusTrackingInfoCall, seqNo, isShowVendorItemNumber, wcssOrderNumber , orderLineNum, sc.getApplicationSession().getAppSessionBean().getTtSession());
			// Then add the shipment details on our Tracking Detail which goes to GWT
			trackingDetail.setItemInShipmentDetails(itemInShipmentDetail);

			//CP-12203 EIQ - get the value of isShowPrice in Admin Settings.
			OrderStatusSession sess = ((OrderStatusSession) sc.getModuleSession());
			OrderStatusSessionBean osBean = sess.getOrderStatusSettingsBean();
			trackingDetail.setShowPrice(osBean.isShowPrice());
			trackingDetail.setItemInShipmentAvailabilityFlag(true);
			trackingDetail.setItemInShipmentAvailabilityMessage(ModelConstants.ITEM_UNDERSHIPMENT_INFO_AVAILABLE);
		}
		else {
			trackingDetail.setItemInShipmentAvailabilityFlag(false);
			trackingDetail.setItemInShipmentAvailabilityMessage(ModelConstants.NO_ITEM_UNDERSHIPMENT_INFO_AVAILABLE);
		}
		return trackingDetail;

	}
	
	//CAP-38766 Refactored to eliminate complexity
	protected void setTrackingDetailFields(int orderLineNum, COOSShipmentDetail trackingDetail, OrderStatusTrackingInfo orderStatusTrackingInfo, AppSessionBean appSessionBean)
	{
		trackingDetail.setSequenceNumber(Util.safeStringToInt(orderStatusTrackingInfo.getSequenceNumberStr().trim()));
		trackingDetail.setSequenceNumberStr(orderStatusTrackingInfo.getSequenceNumberStr());
		trackingDetail.setShipQty(orderStatusTrackingInfo.getShipQty());
		trackingDetail.setFreightTerms(orderStatusTrackingInfo.getFreightTerms());
		trackingDetail.setInvoiceNumber(orderStatusTrackingInfo.getInvoiceNumber());
		trackingDetail.setSummaryInvoiceNumber(orderStatusTrackingInfo.getSummaryInvoiceNumber()); //Summary Invoice Number
		
		//CAP-38766 - ItemDesc and ItemNum should only be populate when orderlineNum is passed, otherwise empty string
		if (orderLineNum == -1)
		{
			trackingDetail.setItemDesc(orderStatusTrackingInfo.getItemDesc());
			trackingDetail.setItemNum(orderStatusTrackingInfo.getItemNum());
		} 
		else 
		{
			trackingDetail.setItemDesc(AtWinXSConstant.EMPTY_STRING);
			trackingDetail.setItemNum(AtWinXSConstant.EMPTY_STRING);
		}
		
		XSCurrency currencyFormatter = Util.getStringFromCurrency(orderStatusTrackingInfo.getSalesTax(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
		
		trackingDetail.setSalesTaxAmt((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText()); // Sales Tax

		currencyFormatter = Util.getStringFromCurrency(orderStatusTrackingInfo.getOrderAndLineCharges(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
		trackingDetail.setOrderAndLineChargesAmt((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText()); // Order and Line Charges

		currencyFormatter = Util.getStringFromCurrency(orderStatusTrackingInfo.getTotalSale(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
		trackingDetail.setTotalSaleAmt((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText()); // Total Sale

		currencyFormatter = Util.getStringFromCurrency(orderStatusTrackingInfo.getShipping(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
		trackingDetail.setShippingAmt((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText()); // Shipping

		currencyFormatter = Util.getStringFromCurrency(orderStatusTrackingInfo.getServiceCharges(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
		trackingDetail.setServiceChargesAmt((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText()); // Service Charges
	}
	
	//CP-12613 [PVT] - add wcssOrderNumber as parameter
	//CP-11757 [PVT] - changed invoked setter method names
	/**
	 * CP-11757-OSOD-[PVT]
	 * this method builds a collection of shipment details for every unique tracking detail
	 * @param appSessionID
	 * @param appSessionBean
	 * @param sequenceNumbers
	 * @param osStatusTrackingInfoCall
	 * @param seqNo
	 * @param isShowVendorItemNumber
	 * @return
	 * @throws AtWinXSException
	 */
	//CAP-36916 - CP method copied from CustomPointWeb com.rrd.custompoint.service.orderstatus.OSShipmentAndTrackingServiceImpl->buildShipmentDetailsPerTrackingDetail()
	@SuppressWarnings("removal")
	protected Collection<COOSItemInShipmentDetail> buildShipmentDetailsPerTrackingDetail(String appSessionID, AppSessionBean appSessionBean, Collection<Integer> sequenceNumbers, OrderStatusTrackingInfo osStatusTrackingInfoCall, String seqNo, boolean isShowVendorItemNumber, String wcssOrderNumber , int orderLineNum, TTSession ttSession) throws AtWinXSException//CAP-708 EIQ added orderlinenum
	{
		// CP-11757 [PVT] - handle empty string for appSessionID
		if(Util.isBlank(Util.nullToEmpty(appSessionID)))//CAP-19456
		{
			appSessionID = "0";
		}
		XSCurrency currencyFormatter;
		Collection<OrderStatusFulfillmentLineVO> fulfillmentLinesVOs = null;
		Collection<COOSItemInShipmentDetail> shipmentDetails = null;
		IOrderStatus component = OrderStatusLocator.locate(appSessionBean.getCustomToken());

		// CP-12613 [PVT]
		IOEShoppingCartComponent scComponent = OEShoppingCartComponentLocator.locate(appSessionBean.getCustomToken());
		IOEManageOrdersComponent oeComponent = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());

		// CP-11757 [PVT] - local variables for determining qty shipped for kits and kit component numbering
		int masterQtyShipped = -1;
		Date shipDate = null;
		boolean fulFilmentLineTypeMaster;
		boolean isJobNoAvail = true;
		int kitComponentCounter = 1;

		// CP-12180 8.1.4 [VEC] Get the Fulfillment Lines based on the Sequence Numbers of the Items ready for Shipping
		//CAP-708 if details came from externalOs, call getLinesFromVoPerTrackingInfo()
		fulfillmentLinesVOs = osStatusTrackingInfoCall.isFromExternalOS()? osStatusTrackingInfoCall.getLinesFromVoPerTrackingInfo(appSessionID, appSessionBean, seqNo , orderLineNum) :
																			osStatusTrackingInfoCall.getFulfillmentLinesPerTrackingInfo(appSessionID, appSessionBean, sequenceNumbers, seqNo);

		// CP-11757 [PVT] Added condition to check if Collection<fulfillmentLinesVOs> is populated
		// populate a collection of OSShipmentDetail to be added for every OSSTrackingDetail object
		if(fulfillmentLinesVOs != null && !fulfillmentLinesVOs.isEmpty())
		{
			shipmentDetails = new ArrayList<>();
			for(OrderStatusFulfillmentLineVO fulfillmentLineVO: fulfillmentLinesVOs)
			{
				COOSItemInShipmentDetail shipmentDetail = new COOSItemInShipmentDetail();

				// CP-12613 - START - [PVT] - get orderID from header object to aid in retrieving custOrderLineID via OrderLineVO
				OrderStatusHeaderVO header = component.getOrderStatusHeaderVO(ttSession.getId(), wcssOrderNumber); // CAP-2951
				// get ORD_LN_NR from XST082 to get custdocOrderLineID if any
				int xsLineNumber = oeComponent.getOrderLineNumber(header.getXsOrderID(), fulfillmentLineVO.getCustItemNr(), fulfillmentLineVO.getVendorItemNr(),
						// CAP-26078 - need to add parameters to uniquely establish which line if there are multiple
						fulfillmentLineVO.getKey().getLineNum(), fulfillmentLineVO.getEDISubLine(), fulfillmentLineVO.getEDISubSubLine());
				int custDocLineID = 0;
				OrderLineVO t = scComponent.getBasicCartLine(new OrderLineVOKey(header.getXsOrderID(), xsLineNumber));
				if(null != t)
				{
					if(null != t.getCustomDocLineKey())
					{
						IOECustomDocumentComponent custDocComponent = OECustomDocLocator.locate(appSessionBean.getCustomToken());
						custDocLineID = t.getCustomDocLineKey().getCustomDocLineID();
						CustomDocumentOrderLineVO cdLineVo = custDocComponent.getCustomDocOrderLine(new CustomDocumentOrderLineVOKey(custDocLineID));
						String custDocInstanceName = Util.nullToEmpty(cdLineVo.getFirstValue());
						String instanceNameLbl = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "instanceNameLbl"));
						shipmentDetail.setItemDesc("[" + instanceNameLbl + ": " + custDocInstanceName + "] " + fulfillmentLineVO.getCustItemDesc());
					}
					else
					{
						shipmentDetail.setItemDesc(fulfillmentLineVO.getCustItemDesc());
					}
				}
				// set default item description if shipped item is not a cust doc type
				else
				{
					shipmentDetail.setItemDesc(fulfillmentLineVO.getCustItemDesc());
				}
				// CP-12613 - END - [PVT]

				// CP-12281 [VEC] Added isShowVendorItemNumber field on the Shipment Detail object
				shipmentDetail.setShowVendorItemNumber(isShowVendorItemNumber);

				// CP-11757 [PVT] - Added ship no field
				shipmentDetail.setShipNo(fulfillmentLineVO.getFulfillmentSequenceNum());
				shipmentDetail.setOrderLineNo(fulfillmentLineVO.getLineNum());
				shipmentDetail.setCustItemNo(fulfillmentLineVO.getCustItemNr());
				shipmentDetail.setVendorItemNo(fulfillmentLineVO.getVendorItemNr());
				//CAP-40615
				shipmentDetail.setItemUOM(fulfillmentLineVO.getLinePriceUomCode() + "/" + (int) Math.round(fulfillmentLineVO.getLinePricingConversionFactor()));

				//CAP-36916 - reformat item UOM and Factor in C1 format, set image for the item under shipment
				shipmentDetail.setItemUOMFactor(ItemUtility.getUOMAcronyms(shipmentDetail.getItemUOM().replace(
						ModelConstants.UOM_FACTOR_FORMAT_WITHSLASH, " "+TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"ofLbl")+" "), true,appSessionBean));

				ICatalog catalog = CMCatalogComponentLocator.locate(appSessionBean.getCustomToken());

				ItemImagesVOKey imageKey =
						new ItemImagesVOKey(appSessionBean.getSiteID(),
							Util.htmlUnencodeQuotes(Util.nullToEmpty(fulfillmentLineVO.getCustItemNr())),
							Util.htmlUnencodeQuotes(Util.nullToEmpty(fulfillmentLineVO.getVendorItemNr())));

				ItemImagesVO imageVO = catalog.getImagesForItem(imageKey);

				String medImage = null;

				if (imageVO != null)
				{
					ItemImagesVOFilter image = new ItemImagesVOFilter(imageVO);
					//CAP-39159 - Item Image path must reference C1UX no image instead of CP no image
					medImage = Util.isBlankOrNull(image.getQualifiedItemMedImgLocURL(appSessionBean)) ?
							ModelConstants.C1UX_NO_IMAGE_MEDIUM : image.getQualifiedItemMedImgLocURL(appSessionBean);
				}
				else
				{
					// extends RemoteServiceServlet to get the context path
					//CAP-39159 - Item Image path must reference C1UX no image instead of CP no image
					medImage = ModelConstants.C1UX_NO_IMAGE_MEDIUM;
				}

				shipmentDetail.setItemImage(medImage);

				//CAP-1639 RAR - If from External, then show N/A for shipped quantity.
				if(osStatusTrackingInfoCall.isFromExternalOS())
				{
					//CAP-1639 RAR - Use translation for 'Yes'
					shipmentDetail.setLineShippedQty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "yesLbl"));
				}
				else
				{
					// If line pricing conversion factor is available, divide the Shipped Item Quantity from the VO to it in order to get the correct Shipped Item Quantity
					if (fulfillmentLineVO.getLinePricingConversionFactor() != 0)
					{
						shipmentDetail.setLineShippedQty(Util.getStringFromLong(fulfillmentLineVO.getLineShippedQty()/(int) fulfillmentLineVO.getLinePricingConversionFactor(), appSessionBean.getDefaultLocale()));
					}

					// Else, just use the one being set on the VO
					else
					{
						shipmentDetail.setLineShippedQty(Util.getStringFromLong(fulfillmentLineVO.getLineShippedQty(), appSessionBean.getDefaultLocale()));
					}
				}

				// boolean variable to determine if KIT is a master
				fulFilmentLineTypeMaster = OrderStatusConstants.COMP_DEC_FULLFILLMENT_LINE_MASTER.equals(fulfillmentLineVO.getFulfillmentTypeCd());

				// check if there is no available job # and set JOB # as text
				if(fulFilmentLineTypeMaster || "0000".equals(fulfillmentLineVO.getLinePlantNum() +
						fulfillmentLineVO.getLineJobNum() +
						OrderStatusUtil.jobComboNumberFormatter.format(fulfillmentLineVO.getLineJobComboNum())))
				{
					isJobNoAvail = false;
					shipmentDetail.setJobNumberDisplayText(fulfillmentLineVO.getLinePlantNum() +
							fulfillmentLineVO.getLineJobNum() +
							OrderStatusUtil.jobComboNumberFormatter.format(fulfillmentLineVO.getLineJobComboNum()));
					shipmentDetail.setJobNumberLinkURL(AtWinXSConstant.EMPTY_STRING);
				}
				else
				{
					isJobNoAvail = true;
				}

				// check if there is an available job # and set job # as link
				if(isJobNoAvail && appSessionBean.hasService(AtWinXSConstant.ITEM_SERVICE_ID))
				{
					// CP-12798 [PVT] - add two new arguments: component and wcssOrderNumber
					shipmentDetail.setJobNumberLinkURL(buildJobNoURL(appSessionBean, fulfillmentLineVO.getCustItemNr(), fulfillmentLineVO.getVendorItemNr(), fulfillmentLineVO.getCustItemDesc(), component, wcssOrderNumber, ttSession));
					shipmentDetail.setJobNumberDisplayText(fulfillmentLineVO.getLinePlantNum() +
							fulfillmentLineVO.getLineJobNum() +
							OrderStatusUtil.jobComboNumberFormatter.format(fulfillmentLineVO.getLineJobComboNum()));
				}

				currencyFormatter = Util.getStringFromCurrency(fulfillmentLineVO.getPrice(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
				shipmentDetail.setUnitPrice((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText());
				currencyFormatter = Util.getStringFromCurrency(fulfillmentLineVO.getItemExtendedSellAmt(), appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate());
				shipmentDetail.setExtendedPrice((currencyFormatter.getAmountValue() == 0.0) ? AtWinXSConstant.EMPTY_STRING:currencyFormatter.getAmountText());


				// CP-11757 [PVT] - added field to set for fulfillment type code to check for Kit Master and Kit Components
				shipmentDetail.setFulfillmentLineCd(fulfillmentLineVO.getFulfillmentTypeCd());
				// CAP-38538 - added to set 'N/A' if fulfillment type code is M(Kit Virtual Master)
				shipmentDetail.setJobNumberDisplayText(shipmentDetail.getFulfillmentLineCd().equals("M")?"N/A":shipmentDetail.getJobNumberDisplayText());
				shipmentDetail.setKitComponentItemSeqNr(fulfillmentLineVO.getKitComponentItemSeqNr());
				shipmentDetail.setEDISubLine(fulfillmentLineVO.getEDISubLine());
				shipmentDetail.setEDISubSubLine(fulfillmentLineVO.getEDISubSubLine());

				// CP-12950 TH - Fixed issue with showing tracking for kits
				//CP-11757 [PVT] - added validation for determining KIT values and KIT component numbering
				if (OrderStatusConstants.COMP_DEC_FULLFILLMENT_LINE_MASTER.equals(fulfillmentLineVO.getFulfillmentTypeCd()) &&
						fulfillmentLineVO.getEDISubLine() == 0)
				{
					kitComponentCounter = 0;
					masterQtyShipped = fulfillmentLineVO.getLineShippedQty();
					//CP-13030 RAR - Use the lineNum instead of autonumber.
					shipmentDetail.setFulfillmentComponentDispLineNum(shipmentDetail.getOrderLineNo()+"."+kitComponentCounter);

				}
				else if (OrderStatusConstants.COMP_DEC_FULLFILLMENT_LINE_MASTER.equals(fulfillmentLineVO.getFulfillmentTypeCd()) &&
						fulfillmentLineVO.getEDISubLine() != 0)
				{
					// CP-12950 TH - Fixed issue with showing tracking for kits
					kitComponentCounter++;
					int compPerKit = masterQtyShipped<1?0:(fulfillmentLineVO.getLineShippedQty()/masterQtyShipped);
					shipmentDetail.setQtyShippedPerKit(compPerKit);
					//CP-13030 RAR - Use the lineNum instead of autonumber.
					shipmentDetail.setFulfillmentComponentDispLineNum(shipmentDetail.getOrderLineNo()+"."+kitComponentCounter);
					shipmentDetail.setFulfillmentLineCd("C");
				}
				else if(OrderStatusConstants.COMP_DEC_FULLFILLMENT_LINE_COMP.equals(fulfillmentLineVO.getFulfillmentTypeCd())
						|| ((OrderStatusConstants.COMP_DEC_FULLFILLMENT_LINE_COMP.equals(fulfillmentLineVO.getFulfillmentTypeCd()) && (fulfillmentLineVO.getEDISubSubLine() > 0))))
				{
					kitComponentCounter++;
					int compPerKit = masterQtyShipped<1?0:(fulfillmentLineVO.getLineShippedQty()/masterQtyShipped);
					shipmentDetail.setQtyShippedPerKit(compPerKit);
					//CP-13030 RAR - Use the lineNum instead of autonumber.
					shipmentDetail.setFulfillmentComponentDispLineNum(shipmentDetail.getOrderLineNo()+"."+kitComponentCounter);

				} else
				{
					kitComponentCounter = 0;
					//CP-13030 RAR - Use the lineNum instead of autonumber.
					shipmentDetail.setFulfillmentComponentDispLineNum(shipmentDetail.getOrderLineNo()+"."+kitComponentCounter);
				}


				//CP-11757 [PVT] added validation for date same as with classic build of FulfillmentVOs for ship date
				OrderStatusShipmentVO shipvo =
						component.getOrderShipment(
								new OrderStatusShipmentVOKey(
										Util.safeStringToDefaultInt(appSessionID, 0),
										osStatusTrackingInfoCall.getWcssOrderNumber(),
										fulfillmentLineVO.getFulfillmentSequenceNum()));
				if ((shipvo != null) && (shipvo.getActualShipDate() != null) && (shipvo.getActualShipDate().after(new Date(0))))
				{
					shipDate = (shipDate == null) ? shipvo.getActualShipDate() : (shipvo.getActualShipDate().before(shipDate) ? shipvo.getActualShipDate() : shipDate);
					//i18n
					shipmentDetail.setShipDate(
							shipvo != null ? Util.getStringFromDate(shipvo.getActualShipDate(), appSessionBean.getDefaultLocale()) : AtWinXSConstant.EMPTY_STRING);
				}
				else
				{
					shipmentDetail.setShipDate(AtWinXSConstant.EMPTY_STRING);
				}
				//CAP-39049 - Set default value for SimplifiedView to false
				shipmentDetail.setSimplifiedView(false);

				shipmentDetails.add(shipmentDetail);
			}
		}

		return shipmentDetails;
	}

	// CP-12798 [PVT] - add two new parameters to retrieve ORD_STAT_SRCH_RSLT_NR from DB
	// CP-11757 [PVT] - method for JOB # link creation
	//CAP-36916 - CP method copied from CustomPointWeb com.rrd.custompoint.service.orderstatus.OSShipmentAndTrackingServiceImpl->buildJobNoURL()
	public String buildJobNoURL(AppSessionBean appSession, String custNo, String vendNo, String itemDs, IOrderStatus component, String wcssOrderNumber, TTSession ttSession) throws AtWinXSException {

		String itemCUST_ITEM = ItemConstants.CUST_ITEM;
		String itemWALLACE_ITEM = ItemConstants.WALLACE_ITEM;
		String itemPARAM_ITEM_NUM = ItemConstants.PARAM_ITEM_NUM;
		String itemPARAM_WCSS_ITEM_NUM = ItemConstants.PARAM_WCSS_ITEM_NUM;
		String itemITEM_DS =ItemConstants.ITEM_DS;

		//CP-12181
		CPUrlBuilder urlBuilder = new CPUrlBuilder(appSession.encodeURL(Util.getContextPath(ContextPath.Classic) + ItemConstants.SERVLET_ITEM_DETAILS));
		urlBuilder.addParameter(AtWinXSConstant.ACTION_ID, ItemConstants.BROKER_ITEM_INV);
		urlBuilder.addParameter(AtWinXSConstant.EVENT_ID, ItemConstants.ITEM_CHK_INVENTORY_FIRST_EVT);
		urlBuilder.addParameter(itemCUST_ITEM, custNo);
		urlBuilder.addParameter(itemWALLACE_ITEM, vendNo);
		urlBuilder.addParameter(itemPARAM_ITEM_NUM, custNo);
		urlBuilder.addParameter(itemPARAM_WCSS_ITEM_NUM, vendNo);
		urlBuilder.addParameter(itemITEM_DS, itemDs);
		urlBuilder.addParameter(OrderStatusConstants.BACK_TO_ORDER_LINE_DTL, "true");

		// CP-12798 [PVT] - retrieve order search results number to add as parameter in building Back To Order Line Detail url
		OrderStatusSearchResultVO osSearchResultVO = component.getSearchResult(ttSession.getId(), wcssOrderNumber); // CAP-2951
		urlBuilder.addParameter(OrderStatusAction.ORD_STAT_SRCH_RSLT_NR, osSearchResultVO.getKey().getNumber());

		return urlBuilder.getUrl();
	}

	//CAP-38651 : method to validate input search criteria request
	public void validateCoOrderSearchRequest(COOrderSearchRequest request, COOrderSearchResultResponse response,SessionContainer sc, OrderStatusSession orderStatusSession) throws AtWinXSException{
		logger.debug("In OrderSearchServiceImpl to calling validateCoOrderSearchRequest()");

		//Checking the user is able to see a recent order or not. If not have permission then throw new AccessForbiddenException
		if(request.isRecentOrderSearch() && !orderStatusSession.getOrderStatusSettingsBean().isShowRecentOrders())
			throw new AccessForbiddenException("Access to this service is not allowed",AtWinXSConstant.EMPTY_STRING);

		if(request.isRecentOrderSearch())
			return;//if search request recent search no further validation required

		validateCoOrderSearchRequestV1(request, response, sc);
		validateCoOrderSearchRequestV2(request, response, sc);
		validateCoOrderSearchRequestV3(request, response, sc);
	}

	public void validateCoOrderSearchRequestV1(COOrderSearchRequest request, COOrderSearchResultResponse response,SessionContainer sc) throws AtWinXSException{

		Date fromdate=null;
		Date todate=null;
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		final String dateFormat="{dateFormat}";
		Map<String, Object> replaceMap=new HashMap<>();
		replaceMap.put(dateFormat, DATE_FORMAT);
		OrderStatusSearch orderStatusSearch = objectMapFactoryService.getEntityObjectMap().getEntity(
				OrderStatusSearch.class, sc.getApplicationSession().getAppSessionBean().getCustomToken());
		   int maxDateRange = orderStatusSearch.getOrderStatusMaxDateRange(appSessionBean.getSiteID());
	    String regex = "^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$";
	    Pattern pattern = Pattern.compile(regex);
	    Matcher matcherFromDate = pattern.matcher(request.getFromDate());
	    Matcher matcherToDate = pattern.matcher(request.getToDate());
		try {
			checkDateFormat(matcherFromDate,matcherToDate,request.getFromDate(),request.getToDate(),response,sc);
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown in checkDateFormat call to checkDateFormat() :- %s",ex));
		}

		//FromDate is not in the required format or is not present
		if(AtWinXSConstant.EMPTY_STRING.equals(request.getFromDate())) {
		try {
			sdfUSLocale.parse(request.getFromDate());
		}catch (ParseException ex) {
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage("fromDate", TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"from_date_lbl_error"));
		}
		}

		//ToDate is not in the required format or is not present
		if(AtWinXSConstant.EMPTY_STRING.equals(request.getToDate())) {
		try {
			sdfUSLocale.parse(request.getToDate());
		}catch (ParseException ex) {
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage("toDate", TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"to_date_lbl_error"));
		}
		}

		//check To date cannot be before the From date
		//And also check FromDate to ToDate is more than the max number of days allowed
		try {
			fromdate= sdfUSLocale.parse(request.getFromDate());
			todate= sdfUSLocale.parse(request.getToDate());
			long diffInMillies = Math.abs(fromdate.getTime() - todate.getTime());
		    long dayDiff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		    if(fromdate.compareTo(todate)>0 && matcherFromDate.matches() && matcherToDate.matches()) {
		    	response.setSuccess(false);
		    	response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"ordersearch_msg_date_invalid"));
		    	response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		    }else if(dayDiff > maxDateRange && matcherFromDate.matches() && matcherToDate.matches()) {
		    	response.setSuccess(false);
		    	response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"ordersearch_msg_date_max"));
		    	response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		    }
		}catch (ParseException ex) {
			logger.error(String.format("Exception thrown in OrderSearchServiceImpl call to validateCoOrderSearchRequest() :- %s",ex));
		}
		//Scope value passed is empty or not a valid option
		if(Util.isBlankOrNull(request.getScope())||!(request.getScope().equals("A")||request.getScope().equals("M"))) {
		response.setSuccess(false);
		response.setMessage(AtWinXSConstant.EMPTY_STRING);
		response.setFieldMessage("scope",
				TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
						"ordersearch_lbl_scope") + RouteConstants.SINGLE_SPACE
						+ TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
								appSessionBean.getCustomToken(), "isReqFldLbl"));
		}

	}

	public void validateCoOrderSearchRequestV2(COOrderSearchRequest request, COOrderSearchResultResponse response,SessionContainer sc) throws AtWinXSException{

		String criteriaName=request.getSearchCriteriaRequest().get(0).getCriteriaName();
		String criteriaModifer=request.getSearchCriteriaRequest().get(0).getCriteriaFieldModifier();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		//Checking scope value passed is not an option for the user (i.e. they cannot see outside their own orders and A is passed.)
		OrderStatusSessionBean osBean = ((OrderStatusSession)sc.getModuleSession()).getOrderStatusSettingsBean();
		if(!Util.isBlankOrNull(request.getScope()) && (request.getScope().equalsIgnoreCase("A") && osBean.getOrderStatusRestriction().equalsIgnoreCase("Y"))) {
		response.setSuccess(false);
		response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"invalidErrMsg")+ RouteConstants. SINGLE_SPACE +TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"visibilityScopeLbl"));
		response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
		//check criteria name is missing, then return
		if(Util.isBlankOrNull(criteriaName))
		{
		response.setSuccess(false);
		response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"search_criteria_lbl_error"));
		response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
		
		//CAP-45055
		if(criteriaName.equalsIgnoreCase(SharedCriteriaEnum.INVNBR.getCriteriaName()) && request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue().length()!=9) {
			
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.INVOICE_NO_LENGH_ERR_MSG));
			response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
		
		if(criteriaName.equalsIgnoreCase(SharedCriteriaEnum.ORDERTITLE.getCriteriaName()) && request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue().length()>150) {
			
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.ORDER_NAME_LENGH_ERR_MSG));
			response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
		
		//CAP-45057
		if(criteriaName.equalsIgnoreCase(SharedCriteriaEnum.SHIPNAME.getCriteriaName()) && request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue().length()>140) {
					
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.SHIP_NAME_LENGH_ERR_MSG));
			response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
				
		if(criteriaName.equalsIgnoreCase(SharedCriteriaEnum.SHIPATTN.getCriteriaName()) && request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue().length()>35) {
					
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.SHIP_ATTN_LENGH_ERR_MSG));
			response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
		
		//check search criteria name is not a valid option(i .e "CriteriaPONumber", "CriteriaSalesRef", "CriteriaStatusCode", "CriteriaItemNumber")
		ArrayList<String> validSearchCriteriList=new ArrayList<>();
		validSearchCriteriList.add(SharedCriteriaEnum.SALESREF.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.PONBR.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.ITEMNBR.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.STATUSCD.getCriteriaName());
		//CAP-45054 added CriteriaOrderNumber
		validSearchCriteriList.add(SharedCriteriaEnum.ORDERNBR.getCriteriaName());
		// CAP-45040
		validSearchCriteriList.add(SharedCriteriaEnum.CUSTREF1.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.CUSTREF2.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.CUSTREF3.getCriteriaName());
		//CAP-45055
		validSearchCriteriList.add(SharedCriteriaEnum.ORDERTITLE.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.INVNBR.getCriteriaName());
		
		//CAP-45057
		validSearchCriteriList.add(SharedCriteriaEnum.SHIPNAME.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.SHIPATTN.getCriteriaName());
		// CAP-45133
		validSearchCriteriList.add(SharedCriteriaEnum.SHIPCNTRY.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.SHIPSTATE.getCriteriaName());
		validSearchCriteriList.add(SharedCriteriaEnum.SHIPZIP.getCriteriaName());
		
		if(!Util.isBlankOrNull(criteriaName) && !validSearchCriteriList.contains(criteriaName))
		{
		response.setSuccess(false);
		response.setMessage(AtWinXSConstant.EMPTY_STRING);
		response.setFieldMessage("criteriaName", TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"invalidErrMsg")+RouteConstants.SINGLE_SPACE +TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"searchCriteriaLbl"));
		}

		//Search Criteria value is missing
		if(Util.isBlankOrNull(request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue()))
		{
		response.setSuccess(false);
		response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"valRequired"));
		response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}

		//Search criteria modifier is too long
		final String numConst="{num}";
		criteriaModifer=Util.isBlankOrNull(criteriaModifer) ? ">=" : criteriaModifer;
		if(!Util.isBlankOrNull(criteriaName) && criteriaName.equals(SharedCriteriaEnum.STATUSCD.getCriteriaName()) && criteriaModifer.length() > RouteConstants.VALID_STATUSMODIFIER_LENGTH){
			Map<String, Object> replaceMap=new HashMap<>();
			replaceMap.put(numConst, RouteConstants.VALID_STATUSMODIFIER_LENGTH);
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage("criteriaFieldModifier", TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),RouteConstants.SF_PREFIX+"charsMax",replaceMap));
		}

		//CAP-39972 added validation for status code check in XST156
		OrderStatusCodes osc = objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusCodes.class, appSessionBean.getCustomToken());
		Collection<OrderStatusCode> statusCodes = null;
		osc.populateOrderStatusCodes();
		statusCodes = osc.getOrderStatusCodes();
		boolean chkstatuscode=false;
		chkstatuscode=request.getSearchCriteriaRequest().get(0).getCriteriaName().equals(SharedCriteriaEnum.STATUSCD.getCriteriaName());
		boolean validateCode=false;
		List<String> checkcode=new ArrayList<>();
		for (OrderStatusCode scs : statusCodes) {
			checkcode.add(scs.getCode());
		}
		if(checkcode.contains(request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue())){
			validateCode=true;
		}


		if(chkstatuscode && !validateCode)
		{
		response.setSuccess(false);
		response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"valRequired"));
		response.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}


	}
	public void validateCoOrderSearchRequestV3(COOrderSearchRequest request, COOrderSearchResultResponse response,
			SessionContainer sc){
		String criteriaName = request.getSearchCriteriaRequest().get(0).getCriteriaName();
		String criteriaValue = String.valueOf(request.getSearchCriteriaRequest().get(0).getCriteriaFieldValue());
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		// Search Criteria value is too long for the specific criteria (eg. Sales ref #
		// is ?? characters) or not in the valid list for the specific Criteria (like
		// passing 8003132622 for an order status code)
		if (!Util.isBlankOrNull(criteriaName)) {
			// CAP-45040 Refactor criteria value length validation
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.SALESREF.getCriteriaName(),
					criteriaValue, RouteConstants.VALID_SALESREF_LENGTH, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.PONBR.getCriteriaName(),
					criteriaValue, RouteConstants.VALID_PONUMBER_LENGTH, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.ITEMNBR.getCriteriaName(),
					criteriaValue, RouteConstants.VALID_ITEMNUMBER_LENGTH, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.STATUSCD.getCriteriaName(),
					criteriaValue, RouteConstants.VALID_STATUS_LENGTH, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.CUSTREF1.getCriteriaName(),
					criteriaValue, ModelConstants.NUMERIC_25, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.CUSTREF2.getCriteriaName(),
					criteriaValue, ModelConstants.NUMERIC_25, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.CUSTREF3.getCriteriaName(),
					criteriaValue, ModelConstants.NUMERIC_25, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.SHIPCNTRY.getCriteriaName(),
					criteriaValue, ModelConstants.NUMERIC_3, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.SHIPSTATE.getCriteriaName(),
					criteriaValue, ModelConstants.NUMERIC_4, response);
			validateCriteriaValueForMaxLength(appSessionBean, criteriaName, SharedCriteriaEnum.SHIPZIP.getCriteriaName(),
					criteriaValue, ModelConstants.NUMERIC_9, response);
			validateCriteriaValueForInvalidLength(appSessionBean, criteriaName, SharedCriteriaEnum.ORDERNBR.getCriteriaName(), 
					criteriaValue, ModelConstants.VALID_WCSS_ORDERNUMBER_LENGTH, response);	
			validateCriteriaValueForInvalidLength(appSessionBean, criteriaName, SharedCriteriaEnum.INVNBR.getCriteriaName(), 
					criteriaValue, ModelConstants.VALID_INVOICE_NBR_LENGTH, response);	
		}
	}
	
	// CAP-45040
	private void validateCriteriaValueForMaxLength(AppSessionBean appSessionBean, String criteriaName,
			String expectedCriteriaName, String criteriaValue, int expectedCriteriaValue,
			COOrderSearchResultResponse response) {
		if (expectedCriteriaName.equals(criteriaName) && criteriaValue.length() > expectedCriteriaValue) {
			String numConst = SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG;
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(numConst, expectedCriteriaValue);
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage(ModelConstants.CRITERIAFIELDVALUE,
					getTranslation(appSessionBean, SFTranslationTextConstants.MAX_CHARS_ERR,
							SFTranslationTextConstants.MAX_CHARS_DEF_ERR, replaceMap));
		}
	}
	// CAP-45054
	private void validateCriteriaValueForInvalidLength(AppSessionBean appSessionBean, String criteriaName,
			String expectedCriteriaName, String criteriaValue, int expectedCriteriaValue,
			COOrderSearchResultResponse response) {

		String numConst = SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG;
		Map<String, Object> replaceMap = new HashMap<>();
		if (expectedCriteriaName.equals(criteriaName) && !Util.isNumeric(criteriaValue)) {
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage(ModelConstants.CRITERIAFIELDVALUE,
					getTranslation(appSessionBean, SFTranslationTextConstants.NUMERIC_ERROR,
							SFTranslationTextConstants.NUMERIC_ERROR_DEF));
			
		}
		else if (expectedCriteriaName.equals(criteriaName) && criteriaValue.length() != expectedCriteriaValue){
			replaceMap.put(numConst, expectedCriteriaValue);
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage(ModelConstants.CRITERIAFIELDVALUE,
					getTranslation(appSessionBean, SFTranslationTextConstants.CHARS_MIN_ERROR,
							SFTranslationTextConstants.CHARS_MIN_ERROR_DEF,replaceMap));
		}
	}
	public void validateDates(COOrderSearchRequest coOrderSearchRequest, SimpleDateFormat sdf, String dateFormat) {
		if(coOrderSearchRequest.getFromDate() != null && coOrderSearchRequest.getToDate() != null) {
			try {
				Date fromdate= sdfUSLocale.parse(coOrderSearchRequest.getFromDate());
				Date todate= sdfUSLocale.parse(coOrderSearchRequest.getToDate());

				coOrderSearchRequest.setFromDate(sdf.format(fromdate));
				coOrderSearchRequest.setToDate(sdf.format(todate));

				if ( logger.isDebugEnabled() )
				{
					logger.debug(String.format("user locale date format :  :- %s",  dateFormat));
					logger.debug(String.format("fromDate in user locale format :- %s", coOrderSearchRequest.getFromDate()));
					logger.debug(String.format("toDate in user locale format :- %s",  coOrderSearchRequest.getToDate()));

				}

			} catch (ParseException ex) {
				logger.error(String.format("Exception thrown in OrderSearchServiceImpl call to getSearchOrdersDetail() :- %s",ex));			}
		}
	}

	public boolean isValidSearchResults(Collection<OrderStatusSearchResult> searchResults) {
		return null != searchResults && !searchResults.isEmpty();
	}

	public void validateRecordCounts(OrderStatusSearch osSearch, COOrderSearchResultResponse coOrderSearchResultResponse, ApplicationSession appSession)throws AtWinXSException {
		if(osSearch!=null && osSearch.isLimitExceeded()) {
			Map<String,Object> replaceMap=new HashMap<>();
			replaceMap.put("{records}", osSearch.getLimitNumber());
			coOrderSearchResultResponse.setSuccess(true);
			coOrderSearchResultResponse.setMessage(TranslationTextTag.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(),"maxSearchResultErrMsg", replaceMap));
			coOrderSearchResultResponse.setFieldMessage(AtWinXSConstant.EMPTY_STRING,AtWinXSConstant.EMPTY_STRING);
		}
	}

	//CAP-39972 updated method due to complexity
	public void checkDateFormat(Matcher fromdtformatmatch, Matcher todtformatmatch, String reqfromdate,
			String reqtodate, COOrderSearchResultResponse response, SessionContainer sc)
			throws AtWinXSException, ParseException {

		final String dateFormat = "{dateFormat}";
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(dateFormat, DATE_FORMAT);
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderStatusSearch orderStatusSearch = objectMapFactoryService.getEntityObjectMap().getEntity(OrderStatusSearch.class,
				sc.getApplicationSession().getAppSessionBean().getCustomToken());
		int maxDateRange = orderStatusSearch.getOrderStatusMaxDateRange(appSessionBean.getSiteID());
		String regex = "^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$";

		if (!fromdtformatmatch.matches() && !AtWinXSConstant.EMPTY_STRING.equals(reqfromdate)) {
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage("fromDate", TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), "dateformat", replaceMap));
		}

		if (!todtformatmatch.matches() && !AtWinXSConstant.EMPTY_STRING.equals(reqtodate)) {
			response.setSuccess(false);
			response.setMessage(AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage("toDate", TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), "dateformat", replaceMap));
			throw new ParseException(regex, maxDateRange);
		}

	}

	//CAP-41713
	protected void setShowBillToInfoAndAttn(COOSFullDetailResponse objCOOSFullDetailResponse, SessionContainer sc) throws AtWinXSException
	{
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEResolvedUserSettingsSessionBean userSettings = AdminUtil.getUserSettings(new LoginVOKey(appSessionBean.getSiteID(), appSessionBean.getLoginID()), -1, appSessionBean.getCustomToken());

		boolean showBillToAttn = true;

		if(!userSettings.isShowBillToInfo() || (userSettings.isShowBillToInfo() && userSettings.getBillToAttnOption().equalsIgnoreCase(OrderEntryConstants.BILL_TO_ATTN_HIDDEN)))
		{
			showBillToAttn = false;
		}

		objCOOSFullDetailResponse.setShowBillToInfo(userSettings.isShowBillToInfo());
		objCOOSFullDetailResponse.setShowBillToAttn(showBillToAttn);
	}
	
	// CAP-43234 - to get Order Routing Information For Order Search
	// copied from CP=>OSDetailsRoutingInfoServiceImpl=>getOSDetailsRoutingInfos()
	/**
	 * Method to get OS Routing Infos
	 *
	 * @param SessionContainer
	 * @param OrderRoutingInformationRequest
	 * @return OrderRoutingInformationResponse
	 */
	@Override
	public OrderRoutingInformationResponse getOSRoutingInfos(SessionContainer sc,
			OrderRoutingInformationRequest request) throws AtWinXSException {

		OrderRoutingInformationResponse response = new OrderRoutingInformationResponse();

		try {

			ApplicationSession appSession = sc.getApplicationSession();
			AppSessionBean appSessionBean = appSession.getAppSessionBean();

			Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class,
					appSessionBean.getCustomToken());
			order.populate(request.getOrderID());

			// CAP-2978 RAR - Pass the Feature Settings of Routing and Approval.
			Collection<RAOrderInformation> routingInformation = order.getRoutingInformation(
					appSessionBean.getFeatureSetting(LookAndFeelFeature.FeatureNames.UXRA.toString()));

			//CAP-43283
			OEOrderSessionBean oeSession = ((OrderEntrySession)sc.getModuleSession()).getOESessionBean();
			OEResolvedUserSettingsSessionBean userSettings = oeSession.getUserSettings();
			boolean showApprover = userSettings.isShowAssignedApprover();
			
			Collection<OSDetailsRoutingInfoBean> routingInfosCollection = buildOSDetailsRoutingInfosBean(
					routingInformation, appSessionBean.getDefaultLocale(), showApprover);

			response.setRoutingDetails(routingInfosCollection);
			response.setSuccess(true);

		} catch (AtWinXSException ae) {
			logger.error(this.getClass().getName() + " - " + ae.getMessage(), ae);
			response.setMessage(ae.getMessage());
			response.setSuccess(false);
		}

		return response;
	}

	// CAP-43234 - Method used to buildOSDetailsRoutingInfosBean copied from
	// CP=>OSDetailsRoutingInfoServiceImpl=>getOSDetailsRoutingInfos()
	private Collection<OSDetailsRoutingInfoBean> buildOSDetailsRoutingInfosBean(
			Collection<RAOrderInformation> routingInformation, Locale defaultLocale, boolean showApprover)
			throws AtWinXSException {

		Collection<OSDetailsRoutingInfoBean> routingInfosCollection = new LinkedList<>();

		if (null != routingInformation && !routingInformation.isEmpty()) {
			int counter = 1;

			for (RAOrderInformation routingInfo : routingInformation) {
				Collection<RAOrderDetailBean> routingInfoBeans = routingInfo.getRoutingDetailBean(defaultLocale);

				for (RAOrderDetailBean routingInfoBean : routingInfoBeans) {
					OSDetailsRoutingInfoBean bean = buildBean(routingInfoBean, defaultLocale, routingInfo, counter,
							showApprover);
					routingInfosCollection.add(bean);
				}

				counter++;
			}
		}

		return routingInfosCollection;
	}

	// CAP-43234 - Method used to buildOSDetailsRoutingInfosBean copied from
	// CP=>OSDetailsRoutingInfoServiceImpl=>getOSDetailsRoutingInfos()
	private OSDetailsRoutingInfoBean buildBean(RAOrderDetailBean routingInfoBean, Locale locale,
			RAOrderInformation routingInfo, int counter, boolean showApprover) {
		OSDetailsRoutingInfoBean bean = new OSDetailsRoutingInfoBean();

		String approvalDate = Util.getStringFromDate(routingInfoBean.getApprovalDate(), locale);
		String approvalMessage = routingInfoBean.getAppoverMessage();
		String approver = routingInfoBean.getApprover();

		if (routingInfoBean.getResult().equals(routingInfo.getTranslatedLabel(locale,
				routingInfo.getStatusFromCode("N").getTag(), routingInfo.getStatusFromCode("N").getDefaultText()))) {

			approvalDate = "";
			approvalMessage = "";
			approver = "";
		}

		bean.setNumber(counter);
		bean.setApprovalDate(approvalDate);
		bean.setApprovalQueueName(showApprover ? routingInfoBean.getApprovalQueueOwner() : "");
		bean.setApproverMessage(approvalMessage);
		bean.setApproverName(approver);
		bean.setQueueDate(Util.getStringFromDate(routingInfoBean.getQueueDate(), locale));
		bean.setResult(routingInfoBean.getResult());
		bean.setYourItem(routingInfoBean.getItemNumber());
		bean.setRoutingReasons(routingInfoBean.getRoutingReasons());
		return bean;
	}
	
	//CAP-45481- Check the Limited search 
	public boolean validateLimitedSearch(boolean limitedSearchOptions,COOrderSearchRequest coOrderSearchRequest ) {
		boolean resp=false;
		//CAP-46011 added a condition to evaluate the request object is not null and empty
		if (coOrderSearchRequest.getSearchCriteriaRequest() != null
				&& !coOrderSearchRequest.getSearchCriteriaRequest().isEmpty() && limitedSearchOptions
				&& !(coOrderSearchRequest.getSearchCriteriaRequest().get(0).getCriteriaName()
						.equalsIgnoreCase(SharedCriteriaEnum.SALESREF.getCriteriaName())
						|| coOrderSearchRequest.getSearchCriteriaRequest().get(0).getCriteriaName()
								.equalsIgnoreCase(SharedCriteriaEnum.PONBR.getCriteriaName())
						|| coOrderSearchRequest.getSearchCriteriaRequest().get(0).getCriteriaName()
								.equalsIgnoreCase(SharedCriteriaEnum.ORDERNBR.getCriteriaName())
						|| coOrderSearchRequest.getSearchCriteriaRequest().get(0).getCriteriaName()
								.equalsIgnoreCase(SharedCriteriaEnum.ITEMNBR.getCriteriaName()))) {
			resp = true;
		}
			return resp;
	}
}
