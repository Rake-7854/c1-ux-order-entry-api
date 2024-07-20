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
 *  08/30/23	Satishkumar A		CAP-43283		C1UX BE - Routing Information For Justification Section on Review Order Page
 *  09/26/23	A Salcedo		   CAP-44145	   Refactor getRoutingInformation. Don't return null in response when R&A setting off.
 *	11/10/23	L De Leon			CAP-44841		Modified getRoutingInformation() to not process routing reasons if demo user
 */
package com.rrd.c1ux.api.services.routing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.routing.RoutingInformationResponse;
import com.rrd.c1ux.api.models.routing.RoutingReason;
import com.rrd.c1ux.api.models.routing.RoutingReasons;
import com.rrd.c1ux.api.models.routing.RoutingSettings;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderLinesRoutingFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderRoutingFormBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class RoutingInformationServiceImpl extends BaseOEService implements RoutingInformationService {
	
	protected RoutingInformationServiceImpl(TranslationService translationService) {
		super(translationService);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(RoutingInformationServiceImpl.class);
	
	public RoutingInformationResponse getRoutingInformation(SessionContainer sc) throws AtWinXSException {
		
		RoutingInformationResponse response = new RoutingInformationResponse();
		
		try {
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeSession = ((OrderEntrySession)sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeSession.getUserSettings();

		if(!hasValidCart(sc)) {
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
			appSessionBean.getCustomToken(),
			SFTranslationTextConstants.ORDER_NOT_FOUND_FOR_ROUTING_INFO));
			return response;
		}

		//CAP-44145
		boolean showApprover = userSettings.isShowAssignedApprover();
		boolean showVendorItemNo = appSessionBean.isShowWCSSItemNumber();
		List<RoutingReason> list = new ArrayList<>();
		RoutingReasons reasons = new RoutingReasons();
		RoutingSettings routingSettings = new RoutingSettings();
		
		routingSettings.setOhAlwaysRouteOrder(userSettings.isAlwaysRouteOrders());
		routingSettings.setOhApprovalQueueID(userSettings.getAssignedApprovalQueue());
		routingSettings.setOhRouteByDollarAmount(userSettings.getRouteDollarAmount());
		routingSettings.setOhRouteDollar(userSettings.getRouteDollarAmount() > 0 );
		routingSettings.setOhRouteShippingChange(userSettings.isRouteOnShipMethodChange());
		routingSettings.setShowApprovalQueue(showApprover);
		routingSettings.setShowRoutingJustification(
				!appSessionBean.isDemoUser() && userSettings.isRequireJustificationTxtInd()); // CAP-44841
		routingSettings.setShowVendorItemNumber(showVendorItemNo);
		response.setRoutingSettings(routingSettings);
		
		if(!appSessionBean.isDemoUser() && userSettings.isRoutingAvailable() && userSettings.isSubjToRnA())//CAP-44145 // CAP-44841
		{
			processRoutingReasons(volatileSessionBean, appSessionBean, oeSession, list , showApprover, showVendorItemNo);
		}
		
		reasons.setRoutingReasonList(list);
		response.setRoutingReasons(reasons);
		response.setSuccess(true);
		}catch (Exception e) {
			LOGGER.error(this.getClass().getName() + " - " + e.getMessage(),e);
			response.setSuccess(false);
		}
		return response;
		
	}

	//CAP-44145
	private void processRoutingReasons(VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean, OEOrderSessionBean oeSession, List<RoutingReason> list,  boolean showApprover, boolean showVendorItemNo) throws AtWinXSException
	{
		//Ref : OECheckoutAssembler  getOrderRoutingDetails line :12774
		OECheckoutAssembler oeAssembler=  new OECheckoutAssembler(volatileSessionBean,
				appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
		OEOrderRoutingFormBean orderDetailsFormBean = oeAssembler.getOrderRoutingDetails(oeSession, appSessionBean);
		
		int lineNo = 1;
		
		if(orderDetailsFormBean.getOrderLineDetailsFormBean()!=null) 
		{
			for(OEOrderLinesRoutingFormBean bean : orderDetailsFormBean.getOrderLineDetailsFormBean()) {
				RoutingReason reason = new RoutingReason();
				reason.setApprovalQueue(showApprover ? bean.getApprvQueueOwner() : RouteConstants.EMPTY_STRING);
				reason.setItemNumber(bean.getCustItemNum());
				reason.setReasonDescription(bean.getRoutingReason());
				reason.setRoutingCounter(String.format("%03d", lineNo));
				reason.setVendorItemNumber(showVendorItemNo ? bean.getWccsItemNum() : RouteConstants.EMPTY_STRING);
				list.add(reason);
				lineNo++;
			}
		}
		
		if(orderDetailsFormBean.getOrderRoutingReason() != null && !orderDetailsFormBean.getOrderRoutingReason().isEmpty()) 
		{
			RoutingReason reasonOrderLevel = new RoutingReason();
			reasonOrderLevel.setApprovalQueue(showApprover ? orderDetailsFormBean.getApprvQueueOwner() : RouteConstants.EMPTY_STRING);
			reasonOrderLevel.setReasonDescription(orderDetailsFormBean.getOrderRoutingReason());
			reasonOrderLevel.setRoutingCounter(String.format("%03d", lineNo));
			reasonOrderLevel.setItemNumber(RouteConstants.EMPTY_STRING);
			reasonOrderLevel.setVendorItemNumber(RouteConstants.EMPTY_STRING);
			list.add(reasonOrderLevel);
		}
	}
	
}
