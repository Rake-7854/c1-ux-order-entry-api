/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  06/26/2023  C Codina		CAP-40613	Initial version
 */
package com.rrd.c1ux.api.services.items.locator;

import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

public interface AdminUtilService {
	
	String getWCSSPriceClassCd(OEResolvedUserSettingsSessionBean userSettings, AppSessionBean asb) throws AtWinXSException;
}
