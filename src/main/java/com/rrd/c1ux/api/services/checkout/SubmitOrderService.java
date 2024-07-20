
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *	06/08/23	S Ramachandran		CAP-41235		Review order - Submit order
 *	09/05/23	Satishkumar A      	CAP-42763		C1UX BE - Order Routing Justification Text Submit Order 
 */

package com.rrd.c1ux.api.services.checkout;

import com.rrd.c1ux.api.models.checkout.SubmitOrderResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SubmitOrderService {

	//CAP-38157
	public SubmitOrderResponse submitOrder(SessionContainer sc,String remoteIPAddr) throws AtWinXSException;

	/**
	 * This method should submit order 
	 *
	 * @param SessionContainer sc
	 * @param String remoteIPAddr
	 * @param OrderEntrySession oeSession
	 * @return SubmitOrderResponse
	 * @throws AtWinXSException
	 */
	//CAP-42763
	SubmitOrderResponse submitOrder(SessionContainer sc, String remoteIPAddr, String justificationText)
			throws AtWinXSException;

}
