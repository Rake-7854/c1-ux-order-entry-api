
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
 * 	04/19/23	Satishkumar A      CAP-39934	   Saved Order – Getting the list of saved orders into the saved order page
 * 	04/24/23	A Boomker			CAP-40002		Added createExpansionDetails() for saved order expansion
 *	04/26/23	A Boomker			CAP-39340		Add API to delete saved order
 *	04/26/23	A Boomker			CAP-39341		Add API to resume saved order
 *	05/04/23    Satishkumar A   	CAP-37503       API Build - Save Order assuming all data already saved
 */
package com.rrd.c1ux.api.services.orders.savedorders;

import com.rrd.c1ux.api.models.orders.savedorders.SaveOrderRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SaveOrderResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderDeleteRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderDeleteResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrdersResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SavedOrderService {

	//CAP-39934
	public SavedOrdersResponse getSavedOrdersList(SessionContainer sc) throws AtWinXSException;
	// CAP-40002
	public SavedOrderExpansionResponse createExpansionDetails(SavedOrderExpansionRequest expandRequest,
			SessionContainer sc) throws AtWinXSException;
	// CAP-39340
	public SavedOrderDeleteResponse deleteOrder(SavedOrderDeleteRequest request, SessionContainer sc) throws AtWinXSException;
	// CAP-39341
	public SavedOrderResumeResponse resumeOrder(SavedOrderResumeRequest request, SessionContainer sc) throws AtWinXSException;
	//CAP-37503
	public SaveOrderResponse saveOrder(SaveOrderRequest request, SessionContainer sc) throws AtWinXSException;
}
