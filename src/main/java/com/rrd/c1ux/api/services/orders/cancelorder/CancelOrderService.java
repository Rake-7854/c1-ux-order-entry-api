/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	07/28/23	Sakthi M			 CAP-42545	C1UX BE - Create API to cancel the current order (cancelOrderInProgress)
 */

package com.rrd.c1ux.api.services.orders.cancelorder;

import com.rrd.c1ux.api.models.cancelorder.CancelOrderResponse;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECancelOrderAssembler;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public interface CancelOrderService {
	public CancelOrderResponse cancelOrder(SessionContainer sc,ApplicationSession appSession,AppSessionBean appSessionBean,OrderEntrySession oeSession,OECancelOrderAssembler oeCancelOrderAssembler) throws AtWinXSException;
}
