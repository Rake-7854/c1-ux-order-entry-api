/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  09/20/2023  N Caceres		CAP-42856	Initial version
 */
package com.rrd.c1ux.api.services.items.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.catalogs.locator.CMFeaturedItemsComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IFeaturedItems;

@Service
public class CMFeaturedItemsComponentLocatorServiceImpl implements CMFeaturedItemsComponentLocatorService {

	@Override
	public IFeaturedItems locate(CustomizationToken token) throws AtWinXSException {
		return CMFeaturedItemsComponentLocator.locate(token);
	}
}
