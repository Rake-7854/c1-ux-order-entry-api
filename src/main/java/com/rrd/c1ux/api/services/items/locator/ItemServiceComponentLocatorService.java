/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  02/19/24	S Ramachandran	CAP-47145 	Initial version
 */

package com.rrd.c1ux.api.services.items.locator;

import com.rrd.custompoint.services.interfaces.IItemServicesComponent;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

public interface ItemServiceComponentLocatorService {

		IItemServicesComponent locate(CustomizationToken token) throws AtWinXSException;
}