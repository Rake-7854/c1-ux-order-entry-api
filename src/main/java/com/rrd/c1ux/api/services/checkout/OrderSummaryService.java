/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/03/23				A Boomker				CAP-39512					Initial Version
 */
package com.rrd.c1ux.api.services.checkout;

import com.rrd.c1ux.api.models.checkout.OrderSummaryResponse;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface OrderSummaryService {
	public Order updatePrices(SessionContainer sc, Order order, boolean review) throws AtWinXSException;
	public OrderSummaryResponse populateSummaryFromOrder(Order order, boolean review, SessionContainer sc) throws AtWinXSException;
}
