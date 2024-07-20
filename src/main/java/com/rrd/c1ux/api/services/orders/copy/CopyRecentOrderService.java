/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/01/23				L De Leon				CAP-42519					Initial Version
 */
package com.rrd.c1ux.api.services.orders.copy;

import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CopyRecentOrderService {

	public CopyRecentOrderResponse copyRecentOrder(SessionContainer sc, CopyRecentOrderRequest copyRecentOrderRequest)
			throws AtWinXSException;
}
