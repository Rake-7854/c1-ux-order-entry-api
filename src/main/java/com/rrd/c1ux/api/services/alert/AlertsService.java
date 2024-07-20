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
 *  10/16/23    M Sakthi			CAP-44468		Initial version
 *  10/31/23	Satishkumar A		CAP-44996		C1UX BE - Create service to show if there are any alerts for the logged in user 
 */

package com.rrd.c1ux.api.services.alert;

import com.rrd.c1ux.api.models.alert.AlertsResponse;
import com.rrd.c1ux.api.models.alert.CheckAlertsResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface AlertsService {
	
	public AlertsResponse getAlerts(SessionContainer sc) throws AtWinXSException; 
	//CAP-44996
	public CheckAlertsResponse checkAlerts(SessionContainer sc) throws AtWinXSException; 
}
