/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/16/2023  N Caceres		CAP-39045	Initial version
 */
package com.rrd.c1ux.api.services.orderentry.util;

import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public interface OrderEntryUtilService {
	
	boolean currentlyWithinKit(OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean);

}
