/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 *  10/16/23    M Sakthi	 		CAP-44468		Initial version 
 *  10/16/23    S Ramachandran		CAP-44515		Retrieve Order Approval Alerts 
 *  10/16/23    M Sakthi 			CAP-44743		Retrieve Inventory Alerts
 *  10/31/23	Satishkumar A		CAP-44996		C1UX BE - Create service to show if there are any alerts for the logged in user
 *  10/30/23    S Ramachandran		CAP-44469		Retrieve Catalog Alerts
 *  10/30/23	M Sakthi			CAP-44514		Retrieve Item Alerts
 */

package com.rrd.c1ux.api.services.alert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.alert.AlertCategorySubType;
import com.rrd.c1ux.api.models.alert.AlertsCategory;
import com.rrd.c1ux.api.models.alert.AlertsResponse;
import com.rrd.c1ux.api.models.alert.CheckAlertsResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.admin.locators.ServiceComponentLocatorService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OAOrderAdminLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.entity.RoutedOrders;
import com.rrd.custompoint.orderentry.entity.RoutingApprovalQueue;
import com.wallace.atwinxs.admin.vo.ServiceVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.framework.ao.HomePageAssembler;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CPUrlBuilder;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOrderAdmin;
import com.wallace.atwinxs.interfaces.IServiceInterface;
import com.wallace.atwinxs.orderentry.admin.vo.LoginOrderPropertiesVO;
import com.wallace.atwinxs.orderentry.admin.vo.LoginOrderPropertiesVOKey;
import com.wallace.atwinxs.orderentry.admin.vo.UserGroupOrderPropertiesVO;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class AlertsServiceImpl extends BaseService implements AlertsService {

	private final Logger logger = LoggerFactory.getLogger(AlertsServiceImpl.class);

	protected final OAOrderAdminLocatorService oaOrderAdminLocatorService;
	protected final ServiceComponentLocatorService serviceComponentLocatorService;

	protected AlertsServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			OAOrderAdminLocatorService oaOrderAdminLocatorService,
			ServiceComponentLocatorService serviceComponentLocatorService) {

		super(translationService, objectMapFactoryService);
		this.oaOrderAdminLocatorService = oaOrderAdminLocatorService;
		this.serviceComponentLocatorService = serviceComponentLocatorService;
	}

	// CAP-44515 : getAlerts service starts here
	public AlertsResponse getAlerts(SessionContainer sc) throws AtWinXSException {

		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();

		PunchoutSessionBean psb = appSession.getPunchoutSessionBean();

		UserGroupVOKey userGroupVOKey = new UserGroupVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(),
				appSessionBean.getGroupName());

		IOrderAdmin orderAdmin = oaOrderAdminLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		LoginOrderPropertiesVOKey loginKey = new LoginOrderPropertiesVOKey(appSessionBean.getSiteID(),
				appSessionBean.getLoginID());
		LoginOrderPropertiesVO userSettingsLoginOrderDetails = orderAdmin.getLoginOrderDetails(loginKey);
		UserGroupOrderPropertiesVO ugProp = orderAdmin.getGroupPlaceOrderDetails(userGroupVOKey);

		IServiceInterface serviceComponent = serviceComponentLocatorService
				.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		Collection<ServiceVO> servicesCollection = serviceComponent.getSelectedUserGroupServices(userGroupVOKey);
		ServiceVO[] services = servicesCollection.toArray(new ServiceVO[servicesCollection.size()]);

		// Retrieve all alerts using CP method getAlertsForHomePage
		HomePageAssembler hpa = new HomePageAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		HashMap<String, List<AlertCountResponseBean>> hashMapAlertsBean = hpa.getAlertsForHomePage(appSessionBean,
				userSettingsLoginOrderDetails, services, ugProp, psb);

		AlertsResponse alertResponse = new AlertsResponse();

		Collection<AlertsCategory> arrLstAlertCategory = new ArrayList<>();

		if (hashMapAlertsBean != null && !hashMapAlertsBean.isEmpty()) {

			// Iterate Each Alert and build SF Category and Sub Category details
			for (Map.Entry<String, List<AlertCountResponseBean>> lstAlertsBean : hashMapAlertsBean.entrySet()) {

				String key = lstAlertsBean.getKey();

				// Use CP title for Category alert name
				String alertTitleTxt = key + "AlertLbl";
				String title = translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), alertTitleTxt);

				// Category alert description & translation with respect to SF
				String alertDescTranslationTxt = RouteConstants.SF_PREFIX + key + "AlertDescLbl";
				String description = translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), alertDescTranslationTxt);

				AlertsCategory alertCategory = new AlertsCategory();
				alertCategory.setCategoryName(title);
				alertCategory.setCategoryDesc(description);
				alertCategory.setCategoryIcon(key);

				ArrayList<AlertCategorySubType> arrLstAlertCategorySubType = new ArrayList<>();

				
				// do not show approval orders for punchout user
				if (null == psb 
						//  user has approve-review level access, alerts type Order, access to Order Service 
						&& ((null != userSettingsLoginOrderDetails.getApproverReviewLevelCode() 
								&& !OrderEntryConstants.NON_APPROVER_USER.equals(userSettingsLoginOrderDetails.getApproverReviewLevelCode()) 
								&& hasROAlert(appSessionBean,key)) 
								// Or has access to other allowed alert services and alert types 
								|| hasSFAllowedAlert(appSessionBean, key))) {

					addSubCategoryDetails(key, hashMapAlertsBean, appSessionBean, arrLstAlertCategorySubType);

					alertCategory.setCategoryCount(getCatalogTotalCount(arrLstAlertCategorySubType));
					alertCategory.setAlertCategorySubType(arrLstAlertCategorySubType);
					arrLstAlertCategory.add(alertCategory);
				}
			}
		}

		// Add list of alerts category to AlertResponse object
		alertResponse.setAlertsCategory(arrLstAlertCategory);

		alertResponse.setSuccess(true);
		return alertResponse;
	}

	
	//CAP-44996 : checkAlerts service starts here
	public CheckAlertsResponse checkAlerts(SessionContainer sc) throws AtWinXSException {
		CheckAlertsResponse checkAlertsResponse = new CheckAlertsResponse();
		int count = 0;
		 AlertsResponse response = getAlerts(sc);
		 if(response.getAlertsCategory()!=null && !response.getAlertsCategory().isEmpty()) {
			 for(AlertsCategory alertCategory :response.getAlertsCategory()) {
				 count += alertCategory.getCategoryCount();
			 }
			 checkAlertsResponse.setCount(count);
			 checkAlertsResponse.setAlertsExist(true);
		 }

		 checkAlertsResponse.setSuccess(true);
		 return checkAlertsResponse;
	}

	// CAP-44515 : add SF specific Sub category Details from CP alertCount object
	protected void addSubCategoryDetails(String key, HashMap<String, List<AlertCountResponseBean>> hashMapAlertsBean,
			AppSessionBean appSessionBean, ArrayList<AlertCategorySubType> arrLstAlertCategorySubType)
			throws AtWinXSException {

		List<AlertCountResponseBean> lstAlertsBean = hashMapAlertsBean.get(key);

		for (AlertCountResponseBean alertsBean : lstAlertsBean) {

			for (AlertCounts alert : alertsBean.getAlertCounts()) {

				if (Util.nullToEmpty(alert.getAlertName()).equals(OrderEntryConstants.TITLE_APPROVAL_ORDER_LIST_PAGE)) {

					// For routing and approval, build it using the method below.
					buildRASubCategory(appSessionBean, arrLstAlertCategorySubType);
				}
				else {
					
					//For Other SF allowed Alerts & Event alerts
					getEntryAlertElement(alert, arrLstAlertCategorySubType, key);
				}
				
			}
		}
	}

	// CAP-44515 : get total catalog alert count from respective sub catalogs count
	protected int getCatalogTotalCount(ArrayList<AlertCategorySubType> arrLstAlertCategorySubType) {

		int totalCatalogCount = 0;
		for (AlertCategorySubType alertCategorySubType : arrLstAlertCategorySubType) {

			totalCatalogCount += alertCategorySubType.getCategorySubTypeCount();
		}
		return totalCatalogCount;
	}

	// CAP-44515 : build list of individual alert category sub type for RoutedOrders
	protected void buildRASubCategory(AppSessionBean appSessionBean,
			ArrayList<AlertCategorySubType> arrLstAlertCategorySubType) throws AtWinXSException {

		Collection<RoutingApprovalQueue> queue = null;

		String noOrderDisplayText = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), SFTranslationTextConstants.NO_ORDERS_APPROVAL_LBL);
		try {

			RoutedOrders routedOrders = objectMapFactoryService.getEntityObjectMap().getEntity(RoutedOrders.class,
					appSessionBean.getCustomToken());
			queue = routedOrders.populateRoutingOrderQueue(appSessionBean.getSiteID(), appSessionBean.getLoginID());

		} catch (AtWinXSException ex) {
			logger.error(ex.getMessage());

			AlertCategorySubType alertCategorySubType = new AlertCategorySubType();
			alertCategorySubType.setCategorySubTypeDesc(noOrderDisplayText);
			arrLstAlertCategorySubType.add(alertCategorySubType);
		}

		if (null != queue) {

			boolean hasNoAppQueue = true;
			for (RoutingApprovalQueue appQueue : queue) {

				if (appQueue.getApprovalQueueCount() > 0) {

					AlertCategorySubType alertCategorySubType = new AlertCategorySubType();

					alertCategorySubType.setCategorySubTypeDesc(appQueue.getApprovalQueueName());
					alertCategorySubType.setCategorySubTypeCount(appQueue.getApprovalQueueCount());

					CPUrlBuilder builder = new CPUrlBuilder();
					builder.setBaseUrl(RouteConstants.CP_REDIRECT_FORALERTS);
					builder.addParameter(RouteConstants.SAML_ENTRY_POINT,
							RouteConstants.SAML_PARAM_VALUE_APPROVAL_ALERT);
					builder.addParameter(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID,
							String.valueOf(appQueue.getApprovalQueueID()));

					// Build CP redirection url with decoded param(set true)/plain param(set false)
					alertCategorySubType.setCategorySubTypelink(builder.getUrl(true));

					arrLstAlertCategorySubType.add(alertCategorySubType);
					hasNoAppQueue = false;
				}
			}

			if (hasNoAppQueue) {

				AlertCategorySubType alertCategorySubType = new AlertCategorySubType();
				alertCategorySubType.setCategorySubTypeDesc(noOrderDisplayText);
				alertCategorySubType.setCategorySubTypelink(AtWinXSConstant.EMPTY_STRING);

				arrLstAlertCategorySubType.add(alertCategorySubType);
			}
		}
	}

	private void getEntryAlertElement(AlertCounts alertCount,
			ArrayList<AlertCategorySubType> arrLstAlertCategorySubType,
			String key) {

		AlertCategorySubType alertCategorySubType = new AlertCategorySubType();

		if (!Util.isBlank(Util.nullToEmpty(alertCount.getCountLevel())) || alertCount.getAlertCount() > 0) {

			alertCategorySubType.setCategorySubTypeDesc(alertCount.getAlertName());
			alertCategorySubType.setCategorySubTypeCount(alertCount.getAlertCount());

			CPUrlBuilder builder = new CPUrlBuilder();
			builder.setBaseUrl(RouteConstants.CP_REDIRECT_FORALERTS);
			if (alertCount.getSiteEventID() == 0) {

				builder.addParameter(RouteConstants.SAML_ENTRY_POINT, getAlertEntryParam(key));
				builder.addParameter(RouteConstants.SAML_PARAM_VALUE_ALERT_TYPE, alertCount.getAlertCd());
			} else {

				builder.addParameter(RouteConstants.SAML_ENTRY_POINT, RouteConstants.SAML_PARAM_VALUE_INVENTORY_EVENT);
				builder.addParameter(RouteConstants.SAML_PARAM_VALUE_ALERT_TYPE,
						alertCount.getAlertCd() + "_" + alertCount.getSiteEventID());
			}
			
			// Build CP redirection url with decoded param(set true)/plain param(set false)
			alertCategorySubType.setCategorySubTypelink(builder.getUrl(true));
			arrLstAlertCategorySubType.add(alertCategorySubType);
		} else {

			alertCategorySubType.setCategorySubTypeDesc(alertCount.getAlertName());
			alertCategorySubType.setCategorySubTypeCount(alertCount.getAlertCount());
			alertCategorySubType.setCategorySubTypelink(AtWinXSConstant.EMPTY_STRING);
			arrLstAlertCategorySubType.add(alertCategorySubType);
		}
	}

	
	// get SAML Alert Param value      
	public String getAlertEntryParam(String key) {
        
		String alertEntryParamValue = "";
		if(key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_INVENTORY)) {    
            
			alertEntryParamValue = RouteConstants.SAML_PARAM_VALUE_INVENTORY_ALERT;
        }
		else if(key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_CATALOG)) {
            
        	alertEntryParamValue =  RouteConstants.SAML_PARAM_VALUE_CATALOG_ALERT;
        }
		else if(key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_ITEM)) {
            
        	alertEntryParamValue =  RouteConstants.SAML_PARAM_VALUE_ITEM_ALERT;
        }
        return alertEntryParamValue;
    }
	
	
	// check access to SF allowed alert service and alert type      
	public boolean hasSFAllowedAlert(AppSessionBean appSessionBean, String key) {
        
		boolean sfAllowedAlertFlag = false;
		// has access to Inventory service and alerts available for Inventory  
		if ((appSessionBean.hasService(AtWinXSConstant.INVENTORY_SERVICE_ID) 
              		&& key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_INVENTORY))
        	// has access to Catalog service and alerts available for Catalog
        	|| (appSessionBean.hasService(AtWinXSConstant.CATALOGS_SERVICE_ID)	
            	&& key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_CATALOG))
        	// has access to Item service and alerts available for Item
        	|| (appSessionBean.hasService(AtWinXSConstant.ITEM_SERVICE_ID) 
                    && key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_ITEM))) {

			sfAllowedAlertFlag = true;
        }
        return sfAllowedAlertFlag;
    }
	
	// check access to SF allowed RO alert service and RO alert type
	public boolean hasROAlert(AppSessionBean appSessionBean, String key) {
        
		boolean sfROAlertFlag = false;
		// has access to Order service and alerts available for Routed Order
		if ((appSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID) 
        		&& key.equalsIgnoreCase(AtWinXSConstant.ALERT_CATEGORY_ORDER))) {

			sfROAlertFlag = true;
        }
        return sfROAlertFlag;
    }
	
}
