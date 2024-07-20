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

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IManageListAdmin;

public interface ListsAdminLocatorService {
	IManageListAdmin locate(CustomizationToken token) throws AtWinXSException;
}
