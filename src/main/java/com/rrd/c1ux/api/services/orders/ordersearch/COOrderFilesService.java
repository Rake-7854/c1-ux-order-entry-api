/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            	Description
 *	--------    -----------         ----------      	------------------------------------
 * 	03/13/24	S Ramachandran		CAP-47841			Added service method downloadOrderFileFromOS
*/

package com.rrd.c1ux.api.services.orders.ordersearch;

import javax.servlet.http.HttpServletResponse;

import com.rrd.c1ux.api.controllers.orders.DownloadOrderFileResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderFileResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.GenericEncodedRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderFileEmailDetailResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface COOrderFilesService
{
	public COOrderFileResponse getOrderFilesContent(SessionContainer sc, String salesRefNum, String orderNumber) throws AtWinXSException;

	public OrderFileEmailDetailResponse buildEmail(SessionContainer sc, GenericEncodedRequest request) throws AtWinXSException;
	
	public DownloadOrderFileResponse downloadOrderFileFromOS(SessionContainer sc, HttpServletResponse httpServletResponse, String encryptedParms) 
			throws AtWinXSException;
}

