/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				JIRA#			Description
 * 	--------	-----------				----------		--------------------------------
 *	05/13/24	Ramachandran S			CAP-49326		Initial Version
 */

package com.rrd.c1ux.api.services.orderentry.locator;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICustomEmail;

public interface OECustomEmailComponentLocatorService {

	ICustomEmail locate(CustomizationToken token) throws AtWinXSException;
}