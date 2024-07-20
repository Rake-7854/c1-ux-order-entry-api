/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	06/22/23				L De Leon				CAP-41373					Initial Version
 */
package com.rrd.c1ux.api.services.orders.copy;

import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface QuickCopyOrderService {

	public QuickCopyOrderResponse quickCopyOrder(SessionContainer sc,
			QuickCopyOrderRequest quickCopyOrderRequest) throws AtWinXSException;
}