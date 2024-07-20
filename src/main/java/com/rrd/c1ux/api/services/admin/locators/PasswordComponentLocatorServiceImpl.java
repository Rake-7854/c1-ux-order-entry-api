/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  02/28/24	N Caceres		CAP-47449 	Initial version
 */
package com.rrd.c1ux.api.services.admin.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.admin.locator.PasswordComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IPassword;

@Service
public class PasswordComponentLocatorServiceImpl implements PasswordComponentLocatorService {
	
	public IPassword locate(CustomizationToken token) throws AtWinXSException {
		return PasswordComponentLocator.locate(token);
	}

}
