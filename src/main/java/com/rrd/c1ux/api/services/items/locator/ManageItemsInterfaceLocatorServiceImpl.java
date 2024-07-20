/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/25/2023  N Caceres		CAP-39046	Initial version
 */
package com.rrd.c1ux.api.services.items.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.items.locator.ManageItemsInterfaceLocator;

@Service
public class ManageItemsInterfaceLocatorServiceImpl implements ManageItemsInterfaceLocatorService {

	@Override
	public IManageItemsInterface locate(CustomizationToken token) throws AtWinXSException {
		return ManageItemsInterfaceLocator.locate(token);
	}
}
