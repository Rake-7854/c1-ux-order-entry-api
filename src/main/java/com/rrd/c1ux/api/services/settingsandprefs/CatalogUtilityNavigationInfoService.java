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
 * 	04/27/22	Krishna Natarajan	CAP-34022	    Created interface as per the requirement to fetch the settings and preferences for catalog navigation
 *  10/10/22	Krishna Natarajan	CAP-36438/36448 Change the return type to Object Modify the API to get the required JSON response for FE
 */

package com.rrd.c1ux.api.services.settingsandprefs;

import com.rrd.c1ux.api.models.settingsandprefs.CatalogUtilityNavigationInfo;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

/**
 * @author Krishna Natarajan
 *
 */
public interface CatalogUtilityNavigationInfoService {
	/**
	 * @param sc {@link SessionContainer}
	 * @return expected an ArrayList 
	 * @throws AtWinXSException
	 */
	CatalogUtilityNavigationInfo getCatalogUtilityNavigationInfoService(SessionContainer sc) throws AtWinXSException;

}
