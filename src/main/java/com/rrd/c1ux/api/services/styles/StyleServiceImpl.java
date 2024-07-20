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
 *  09/01/22    S Ramachandran  CAP-35358   Get URL for components to load for standard style sheet
 *  11/22/22    S Ramachandran  CAP-37370   change stylesheet API URL returned to be a relative URL
 */

package com.rrd.c1ux.api.services.styles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.styles.StandardStyleIncludeResponse;
import com.wallace.atwinxs.admin.locator.AdministrationLocator;
import com.wallace.atwinxs.admin.vo.BusinessUnitVOKey;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.IAdministration;


@Service
public class StyleServiceImpl implements StyleService {

	private final Logger logger = LoggerFactory.getLogger(StyleServiceImpl.class);
		    	
	@Value("${apstandardstyle.baseurl}")
	private String apStandardStyleUrl;
	
	//CAP-35358 - method included to get Standard Style URL for C1UX based on buid, siteId, styleid, usergroup name 
	public StandardStyleIncludeResponse getStandardStyleIncludeURL(SessionContainer mainSession) 
			throws AtWinXSException {
		
		IAdministration admin = AdministrationLocator.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		LoginVOKey loginKey = new LoginVOKey(mainSession.getApplicationSession().getAppSessionBean().getSiteID(), 
				mainSession.getApplicationSession().getAppSessionBean().getLoginID());	
		
		//CAP-37370 - apstandardstyle.baseurl is taken from application-<DEV/LOCAL/TEST/PROD>.properties
		String BASE = apStandardStyleUrl;
		StandardStyleIncludeResponse objStandardStyleIncludeResponse = new StandardStyleIncludeResponse();

		try 
		{
			
			//CAP-37370 - code refactored to returned stylesheet api url with relative path of app server
			SiteBUGroupLoginProfileVO sessionSettings = admin.getSessionSettings(loginKey);
			UserGroupVOKey groupKey = new UserGroupVOKey(sessionSettings.getSiteID(), sessionSettings.getBuID(), 
					sessionSettings.getUserGroupName());

			int styleID = admin.getStyleID(new BusinessUnitVOKey(groupKey.getSiteID(), groupKey.getBuID()));

			objStandardStyleIncludeResponse.setCoStandardStyleUrl(BASE + "c1ux." + styleID + ".css");
			objStandardStyleIncludeResponse.setStatus("Success");
		} catch(Exception ex) {

			logger.error(ex.getMessage());

			objStandardStyleIncludeResponse.setCoStandardStyleUrl(BASE + "c1ux." + "0" + ".css");
			objStandardStyleIncludeResponse.setStatus("Failed");
		}
		
	return objStandardStyleIncludeResponse;
	}
	
}
