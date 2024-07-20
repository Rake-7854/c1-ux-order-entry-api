/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  10/16/23  	S Ramachandran	CAP-44515	Initial version
 */

package com.rrd.c1ux.api.services.admin.locators;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IServiceInterface;

public interface ServiceComponentLocatorService {
	
	IServiceInterface locate(CustomizationToken token) throws AtWinXSException;
}

