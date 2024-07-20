/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  09/12/2023  C Codina		CAP-42170	Initial version
 */

package com.rrd.c1ux.api.services.items.locator;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IOrderAdmin;

public interface OAOrderAdminLocatorService {
	
	IOrderAdmin locate(CustomizationToken token) throws AtWinXSException;

}
