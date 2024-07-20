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
 *  04/12/24	Krishna Natarajan	CAP-48606		Added this Impl to add coverage using Junits listsAdminLocatorService 
 */
package com.rrd.c1ux.api.services.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IManageListAdmin;
import com.wallace.atwinxs.lists.locator.ListsAdminLocator;

@Service
public class ListsAdminLocatorServiceImpl implements ListsAdminLocatorService {

	@Override
	public IManageListAdmin locate(CustomizationToken token) throws AtWinXSException {
		return ListsAdminLocator.locate(token);
	}

}
