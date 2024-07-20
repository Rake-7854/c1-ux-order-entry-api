/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/16/2023  N Caceres		CAP-39045	Initial version
 */
package com.rrd.c1ux.api.services.orderentry.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IOESavedOrderComponent;
import com.wallace.atwinxs.orderentry.locator.OESavedOrderComponentLocator;

@Service
public class OESavedOrderComponentLocatorServiceImpl implements OESavedOrderComponentLocatorService {

	@Override
	public IOESavedOrderComponent locate(CustomizationToken token) throws AtWinXSException {
		return OESavedOrderComponentLocator.locate(token);
	}
}
