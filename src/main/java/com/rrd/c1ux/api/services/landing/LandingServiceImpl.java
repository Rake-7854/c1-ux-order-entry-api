/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By			DTS#		Description
 *	--------	-----------			----------	-----------------------------------------------------------
 *  09/13/22    S Ramachandran  	CAP-35424   Get Landing page information 
 *  01/19/23    Sumit kumar			CAP-37862   Change LandingResponse object and add on it list of Widgets
 *  08/16/23	Krishna Natarajan 	CAP-42819	updated the HOMEPAGE_SERVICE_ID to STOREFRONT_SERVICE_ID
 *  10/16/23	Krishna Natarajan	CAP-44685	updated code to get the flag for grouping of widgets
 *  01/22/24	Krishna Natarajan	CAP-46645	Added a method to check and remove "pw_oob" if Allow Order On Behalf is disabled at the UG/user level 
 */

package com.rrd.c1ux.api.services.landing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.addtocart.landing.LandingResponse;
import com.rrd.custompoint.admin.entity.PluggableWidgetAssignment;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.wallace.atwinxs.admin.ao.CustomizationAssembler;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ProfileParserUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.Util.ContextPath;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;


@Service
public class LandingServiceImpl implements LandingService {

	private final Logger logger = LoggerFactory.getLogger(LandingServiceImpl.class);
	
	private static final String PW_FAVE_ITEMS= "pw_fave_items";
	private static final String PW_FEAT_ITEMS= "pw_feat_items";
	private static final String PW_FAVE_FEAT_ITEMS= "pw_fave_feat_items";
	private static final String PW_FEAT_FAVE_ITEMS= "pw_feat_fave_items";
	
	/**
 	 * 
 	 * @param sc - {@link SessionContainer} 
 	 * @return - {@link LandingResponse} This will return LandingResponse 
 	 * @throws AtWinXSException 
	 */
	//CAP-35424 - method to Get Landing page information
	public LandingResponse loadLanding(SessionContainer sc) throws AtWinXSException {
	
		AppSessionBean appSessionBean = null;
		
		LandingResponse objLandingResponse = new LandingResponse();

		try 
		{
		
			appSessionBean = sc.getApplicationSession().getAppSessionBean();
			
			CustomizationAssembler cstmAssembler = new CustomizationAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale()) ;
			String html = cstmAssembler.getLandingHTML( appSessionBean, AtWinXSConstant.STOREFRONT_SERVICE_ID );//CAP-42819	updated the HOMEPAGE_SERVICE_ID to STOREFRONT_SERVICE_ID
			
			ProfileParserUtil parser = new ProfileParserUtil();
			String landingHTML = parser.parseHTML(Util.getContextPath(ContextPath.Classic), appSessionBean, html);
			
			//CAP-37862 : add list of widgets to LandingResponse object
			UserGroup ug = ObjectMapFactory.getEntityObjectMap().getEntity(UserGroup.class,
					sc.getApplicationSession().getAppSessionBean().getCustomToken());
			ug.setKey(new UserGroupVOKey(sc.getApplicationSession().getAppSessionBean().getSiteID(),
					sc.getApplicationSession().getAppSessionBean().getBuID(),
					sc.getApplicationSession().getAppSessionBean().getGroupName()), false);
			Collection<PluggableWidgetAssignment> pwdg = ug.getHomePageWidgets();
			List<String> widgetList=new ArrayList<>();
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();//CAP-46645
			//CAP-44685 - variables created
			int faveIndex=0;
			boolean isFaveGroup=false;
			int featIndex=0;
			boolean isFeatGroup=false;
			
			for (PluggableWidgetAssignment assigndWidget : pwdg) {
				if(assigndWidget.getWidgetName().equals(PW_FAVE_ITEMS)) {//CAP-44685
					faveIndex= assigndWidget.getSequenceNumber();
					isFaveGroup= assigndWidget.isGroup();
				}
				if(assigndWidget.getWidgetName().equals(PW_FEAT_ITEMS)) {//CAP-44685
					featIndex= assigndWidget.getSequenceNumber();
					isFeatGroup= assigndWidget.isGroup();
				}
				widgetList.add(assigndWidget.getWidgetName());
			}
			groupFeatFav(faveIndex,isFaveGroup,featIndex,isFeatGroup,widgetList);//CAP-44685
			checkOOBWidget(oeSession, widgetList);//CAP-46645
			objLandingResponse.setAssignedWidgets(widgetList);
			objLandingResponse.setLandingHTML(landingHTML);
			objLandingResponse.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
			
		} catch (AtWinXSException ex)
		{
			logger.error(ex.getMessage());
			objLandingResponse.setStatus(RouteConstants.REST_RESPONSE_FAIL);
			objLandingResponse.setLandingHTML("");
		}

		return objLandingResponse;
	}
	
	//CAP-44685
	public void groupFeatFav(int faveIndex, boolean isFaveGroup, int featIndex, boolean isFeatGroup, List<String> widgetList) {
		if (isFaveGroup && isFeatGroup) {
			if (faveIndex < featIndex) {
				widgetList.set(faveIndex - 1, PW_FAVE_FEAT_ITEMS);
				widgetList.remove(featIndex - 1);
			} else {
				widgetList.set(featIndex - 1, PW_FEAT_FAVE_ITEMS);
				widgetList.remove(faveIndex - 1);
			}
		}
	}
	
	// CAP-46645
	public void checkOOBWidget(OrderEntrySession oeSession, List<String> widgetList) {
		if (!oeSession.getOESessionBean().getUserSettings().isAllowOrderOnBehalf() && widgetList.contains("pw_oob")) {
			widgetList.remove("pw_oob");
		}
	}
	
}
