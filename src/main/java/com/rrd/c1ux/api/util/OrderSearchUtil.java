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
 *  11/25/22    Sumit Kumar  	CAP-36559   Create utility helper class to retrieve order search result
 *  12/19/22    Sumit Kumar  	CAP-37854   add addition 3 field in order search response
 *  02/03/23    Sumit Kumar  	CAP-38344   add addition orderTime field in order search response
 *  02/14/23    Sumit Kumar  	CAP-38497   set default modifier => in case of empty on CriteriaStatusCode condition
 *  03/07/23    Sumit Kumar  	CAP-39067   API Fix - change trackingLinks data type string to boolean in order search response
 *  03/07/23	A Boomker		CAP-39126	Add item number search term handling
 *  03/08/23    A Boomker  		CAP-39132   In convertSearchResults, populate trackingLinks with the boolean from the result
 *  07/19/23	Satishkumar A	CAP-41553	C1UX BE - API Change - Order Search Results Listing changes needed for new copy order flags
 *  11/10/23	M Sakthi		CAP-44979	C1UX BE - Modify order search to return credit card fields and carrier service fields
 *  11/14/23	N Caceres		CAP-45040	Add customer reference search criteria (cust ref 1, 2, 3) to Order Search
 *  11/15/23	Krishna Natarajan CAP-45241 Added a method and a condition to handle the allowed to copy flag
 *  11/15/23	C Codina		CAP-45054	C1UX BE - Add WCSS Order Number as search criteria for Order Search
 *  11/27/23	M Sakthi		CAP-45057	C1UX BE - Add Ship To Name and Ship To Attention as search criteria for Order Search
 *  11/28/23	N Caceres		CAP-45133	Add Ship To Country, Ship To State, Ship To Zip to Order Search Criteria
 */

package com.rrd.c1ux.api.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.controllers.orders.OrderSearchController;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResult;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResultResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COSharedSearchCriteriaRequest;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearch;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchCriteria;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchResult;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearchResult.StatusView;
import com.rrd.custompoint.orderstatus.entity.SharedCriteriaEnum;
import com.wallace.atwinxs.admin.locator.AdministrationLocator;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IAdministration;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOrderAdmin;
import com.wallace.atwinxs.interfaces.IOrderSweepInterface;
import com.wallace.atwinxs.orderentry.admin.locator.OAOrderAdminLocator;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.locator.OrderSweepLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

public class OrderSearchUtil {

	// copied logic from com.rrd.custompoint.service.orders.util.OrderSearchUtil
	// method getOrderStatusSearchCriteria()
	// CAP-36559 method for retrieve correct order search criteria(conditions)
	private OrderSearchUtil()
	{}
	static final Logger logger = LoggerFactory.getLogger(OrderSearchUtil.class);
	
	// CAP-45040
	public static final String CRITERIA_CUST_REF = "CriteriaCustRef";
	public static final String CRITERIA_SALES_REF = "CriteriaSalesRef";
	public static final String CRITERIA_PO_NUMBER = "CriteriaPONumber";
	public static final String CRITERIA_ORD_NUMBER = "CriteriaOrderNumber";

	// CAP-45040 - Refactor method to reduce complexity
	public static OrderStatusSearchCriteria getOrderStatusSearchCriteria(COOrderSearchRequest searchData,
			AppSessionBean asb, SimpleDateFormat sdf){
		OrderStatusSearchCriteria searchCriteria = ObjectMapFactory.getEntityObjectMap()
				.getEntity(OrderStatusSearchCriteria.class, asb.getCustomToken());
		logger.debug("in OrderSearchUtil to calling getOrderStatusSearchCriteria()");
		try {
			searchCriteria.setFromDate(sdf.parse(searchData.getFromDate()));
			searchCriteria.setToDate(sdf.parse(searchData.getToDate()));
			searchCriteria.setScope(searchData.getScope());
			searchCriteria.setRecentOrderSearch(false);

			Map<String, Integer> ordering = searchCriteria.getCriteriaOrdering();
			int idx = 0;
			for (COSharedSearchCriteriaRequest currentCriteria : searchData.getSearchCriteriaRequest()) {
				// CAP-38540 - Records should not display for new searches with an empty
				// criteria OR empty field value
				if (!Util.isBlankOrNull(currentCriteria.getCriteriaFieldValue())
						&& !Util.isBlankOrNull(currentCriteria.getCriteriaName())) {
					idx = validateCriteriaName(searchCriteria, ordering, idx, currentCriteria);
				}
			}
		} catch (Exception ex) {
			logger.error(String.format("Exception in OrderSearchUtil to calling getOrderStatusSearchCriteria():-%s", ex));
		}
		return searchCriteria;
	}
	
	protected static int validateCriteriaName(OrderStatusSearchCriteria searchCriteria, Map<String, Integer> ordering, int idx,
			COSharedSearchCriteriaRequest currentCriteria) {
		ordering.put(currentCriteria.getCriteriaName(), idx++);
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.SALESREF.getCriteriaName())) {
			searchCriteria.setSalesReferenceNbr(currentCriteria.getCriteriaFieldValue());
		}

		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.PONBR.getCriteriaName())) {
			searchCriteria.setPurchaseOrdNbr(currentCriteria.getCriteriaFieldValue());
		}
		// CAP-39126 - add item number handling
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.ITEMNBR.getCriteriaName())) {
			searchCriteria.setCustomerItemNbr(currentCriteria.getCriteriaFieldValue());
		}

		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.ORDERNBR.getCriteriaName())) {
			searchCriteria.setOrderNbr(currentCriteria.getCriteriaFieldValue());
		}

		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.STATUSCD.getCriteriaName())) {
			searchCriteria.setOrderStatusCode(currentCriteria.getCriteriaFieldValue());
			// CAP-38497 API Fix - Searching by order status with a blank modifier defaults
			// to = instead of >=
			if (Util.isBlankOrNull(currentCriteria.getCriteriaFieldModifier()))
				searchCriteria.setActionCode(">=");
			else
				searchCriteria.setActionCode(currentCriteria.getCriteriaFieldModifier());
		}

		// CAP-45040
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.CUSTREF1.getCriteriaName())) {
			searchCriteria.setCustomerReference1(currentCriteria.getCriteriaFieldValue());
		}
		
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.CUSTREF2.getCriteriaName())) {
			searchCriteria.setCustomerReference2(currentCriteria.getCriteriaFieldValue());
		}
		
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.CUSTREF3.getCriteriaName())) {
			searchCriteria.setCustomerReference3(currentCriteria.getCriteriaFieldValue());
		}
		
		//CAP-45055-Add Invoice Number
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.INVNBR.getCriteriaName())) {
			searchCriteria.setInvoiceNbr(currentCriteria.getCriteriaFieldValue());
		}
		
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.ORDERTITLE.getCriteriaName())) {
			searchCriteria.setOrdTitle(currentCriteria.getCriteriaFieldValue());
		}
		
		validateShippingCriteria(searchCriteria, currentCriteria);
		
		return idx;
	}

	// CAP-45133
	private static void validateShippingCriteria(OrderStatusSearchCriteria searchCriteria,
			COSharedSearchCriteriaRequest currentCriteria) {
		//CAP-45057-Add Ship to Name and Attention
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.SHIPNAME.getCriteriaName())) {
			searchCriteria.setShipToName(currentCriteria.getCriteriaFieldValue());
		}
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.SHIPATTN.getCriteriaName())) {
			searchCriteria.setShipToAttention(currentCriteria.getCriteriaFieldValue());
		}
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.SHIPCNTRY.getCriteriaName())) {
			searchCriteria.setShipToCountry(currentCriteria.getCriteriaFieldValue());
		}
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.SHIPSTATE.getCriteriaName())) {
			searchCriteria.setShipToState(currentCriteria.getCriteriaFieldValue());
		}
		if (currentCriteria.getCriteriaName().equals(SharedCriteriaEnum.SHIPZIP.getCriteriaName())) {
			searchCriteria.setShipToZip(currentCriteria.getCriteriaFieldValue());
		}
	}
	
	//CAP-41553
	// copied logic from com.rrd.custompoint.service.orders.util.OrderSearchUtil method convertSearchResults()
	// CAP-36559 converts orders search result into List of COOrderSearchResult object
	public static List<COOrderSearchResult> convertSearchResults(Collection<OrderStatusSearchResult> searchResults,
			boolean isSimpleView, AppSessionBean asb, SimpleDateFormat sdf,boolean isSearchForWidgetDisplay, COOrderSearchResultResponse coOrderSearchResultResponse
			) throws AtWinXSException {
		List<COOrderSearchResult> convertedResults = new ArrayList<>();
		Locale locale = asb.getDefaultLocale();
		int displayRecordsCount = 0;
		if(isSearchForWidgetDisplay && (searchResults.size()>10)) displayRecordsCount = 10;
		else displayRecordsCount = searchResults.size();
		logger.debug("in OrderSearchUtil to calling convertSearchResults()");
		Iterator<OrderStatusSearchResult> searchResultsIterator = searchResults.iterator();
		//CAP-37980 : change date format for UI(CAP-38344)
		SimpleDateFormat sdfOrdTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		for(int i=0;i<displayRecordsCount;i++) {
			OrderStatusSearchResult result = searchResultsIterator.next();
			COOrderSearchResult convertedResult = new COOrderSearchResult();

			convertedResult.setSalesRefNumber(result.getSalesReferenceNbr());
			convertedResult.setPurchaseOrderNumber(result.getPurchaseOrdNbr());
			convertedResult.setOrderDateDisplay(sdf.format(result.getOrderDate()));
			convertedResult.setOrderTime(sdfOrdTime.format(result.getOrderDate()));

			if (isSimpleView) {
				convertedResult.setOrderStatusCode(result.getOrderStatusText(StatusView.Simple, locale));
			} else {
				if (!Util.isBlankOrNull(result.getOrderStatusAdvncDefnTag())) {
					convertedResult.setOrderStatusCode(result.getOrderStatusText(StatusView.Advanced, locale));
				} else {
					convertedResult.setOrderStatusCode(TranslationTextTag.processMessage(locale, asb.getCustomToken(),
							"OSAdvncPending", null, false));
				}
			}
			//CAP-39067 : setting the tracking link boolean (type changed from string to boolean)
			convertedResult.setTrackingLinks(result.isTrackingShown()); // CAP-39132
			//CAP-37854 : add addition 3 field in order search response (orderID, wcssOrderNumber, searchSequenceNumber)
			convertedResult.setOrderId(result.getOrderId());
			convertedResult.setReqSearchResultNumber(String.valueOf(result.getSearchResultSequenceNumber()));
			convertedResult.setOrderNum(result.getWcssOrdNbr());
			
			//CAP-45241
			if (coOrderSearchResultResponse.isShowOrderOptions() && (result.getOrderId() > 0)
					&& ((result.getProfileNumber() == asb.getProfileNumber())
							&& result.getLoginID().equalsIgnoreCase(asb.getLoginID())
							&& !isCopyOrderDisabled(result, asb))) {

				convertedResult.setAllowedToCopy(true);
			}

			convertedResults.add(convertedResult);
		}

		return convertedResults;
	}
	
	/**
	 * 
	 * @param results
	 * @throws AtWinXSException
	 */
	public static void createActions(Collection<OrderStatusSearchResult> searchResults, AppSessionBean appSessionBean, OrderStatusSearch osSearch) throws AtWinXSException
	{


	    	IAdministration admin = AdministrationLocator.locate(appSessionBean.getCustomToken());
			LoginVOKey key = new LoginVOKey(appSessionBean.getSiteID(), appSessionBean.getLoginID());
			SiteBUGroupLoginProfileVO sessionSettings = admin.getSessionSettings(key);
			IOrderAdmin orderAdmin= OAOrderAdminLocator.locate(appSessionBean.getCustomToken());
			IOrderSweepInterface ioSweep = OrderSweepLocator.locate(appSessionBean.getCustomToken());

			for (OrderStatusSearchResult currResult : searchResults)
			{
				//CAP-17983 - validate the budget... (if it isn't valid, dont show copy order as an option)
			    boolean validateBudget = true;

			    try
			    {

			    	OEResolvedUserSettingsSessionBean userSettings = ioSweep.getUserSettings(sessionSettings, key, admin, orderAdmin, currResult.getOrderId());
			    	OEOrderSessionBean oeSessionBean = new OEOrderSessionBean(appSessionBean.getSiteID(),
																  appSessionBean.getSiteLoginID(),
																  currResult.getProfileNumber(),
																  appSessionBean.getBuID(),
																  appSessionBean.getGroupName(),
																  currResult.getApplicationSessionId());
			    
			    	validateBudgetAllocation(appSessionBean, 
					userSettings,  
					oeSessionBean.getOrderScenarioNumber());
			    }
			    catch(AtWinXSMsgException msg)
			    {
			    	validateBudget=false;
			    }
	    
				//CAP-8642
				//CAP-17983 - added conditional to prevent ordering without a budget
				if (currResult.getScenarioNumber() != OrderEntryConstants.SCENARIO_SUBSCRIPTION_ONLY && 
					(!currResult.isCampaignEvent()) &&
					currResult.getScenarioNumber() != OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE &&
					validateBudget)
				{
					
					currResult.addAction(osSearch.createCopyOrderAction(currResult));
				}
			}
	}
	

	// CAP-17983 - Create local validateBudgetAllocation Method 
	public static void validateBudgetAllocation(
			AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings,
			int orderScenarioNr) throws AtWinXSException
	{

    	IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());
    	boolean isInvalidBudgetScenario = ordersService.isInvalidBudgetOrderScenario(orderScenarioNr);

		if((userSettings.isAllowAllocationsInd() && userSettings.isAllowBudgetAllocations()) &&
		        !isInvalidBudgetScenario && Util.isBlankOrNull(userSettings.getBudgetAllocationGrp())
                && !userSettings.isAllowOrderingWithoutBudget())
		{
       		Message errMsg = new Message();
        		throw new AtWinXSMsgException(errMsg, OrderSearchController.class.getClass().getName());
		}
		
	}
	
	//CAP-44979
	public static String populateCardNumber(String cardType, String cardLastFourDigits) {
		StringBuilder cardNumber = new StringBuilder();
		if (!Util.isBlankOrNull(cardLastFourDigits)) {
			cardNumber.append("***********");
			if (OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD.equals(cardType)
					|| OrderEntryConstants.CREDIT_CARD_TYPE_VISA.equals(cardType)) {
				cardNumber.append("*");
			}
			cardNumber.append(cardLastFourDigits);
		}
		return cardNumber.toString();
	}

	public static String populateSavedExpirationDate(String expMonth,String expYear) {
        String expirationDate = AtWinXSConstant.EMPTY_STRING;
        if (!Util.isBlankOrNull(expMonth)
                && !Util.isBlankOrNull(expYear)) {            
            expirationDate = new StringBuilder(expMonth)
                    .append(AtWinXSConstant.FORWARD_SLASH).append(expYear).toString();
        }
        return expirationDate;
    }
	
	//CAP-45241 added a method to set the allowed to copy flag
	private static boolean isCopyOrderDisabled(OrderStatusSearchResult result, AppSessionBean appSessionBean)
    {
        boolean copyOrderDisabled = true;
        boolean hasPlaceAndManage = (appSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID)); // shin

        if ((hasPlaceAndManage)
                && (result.getOrderId() > 0 && ((result.getProfileNumber() == appSessionBean.getProfileNumber()) || result.isValidTeamSharedOrder(
                        appSessionBean.getProfileAttributes(), appSessionBean.getSiteID()))) && result.getScenarioNumber() != OrderEntryConstants.SCENARIO_SUBSCRIPTION_ONLY
                && !result.isCampaignEvent()
                && !appSessionBean.isFromStorefront()) //CAP-44614
        {
            copyOrderDisabled = false;
        }

        return copyOrderDisabled;
    }
	
}
