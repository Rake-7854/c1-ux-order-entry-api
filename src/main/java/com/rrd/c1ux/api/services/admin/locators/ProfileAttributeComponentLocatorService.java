/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  11/07/23	S Ramachandran	CAP-44961 	Initial version
 */

package com.rrd.c1ux.api.services.admin.locators;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IProfileAttribute;

public interface ProfileAttributeComponentLocatorService {

	public IProfileAttribute locate(CustomizationToken token) throws AtWinXSException;
}

