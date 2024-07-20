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

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;

@Service
public class OEManageOrdersComponentLocatorServiceImpl implements OEManageOrdersComponentLocatorService{

	@Override
	public IOEManageOrdersComponent locate(CustomizationToken token) throws AtWinXSException {
		return OEManageOrdersComponentLocator.locate(token);
	}
	

}
