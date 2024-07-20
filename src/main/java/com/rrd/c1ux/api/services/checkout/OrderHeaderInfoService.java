/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				A Salcedo				CAP-39396					Initial Version
 *	05/16/23				A Boomker				CAP-40687					Add saveOrderHeaderInfo(), change validate to use BaseResponse
 *	04/04/24				L De Leon				CAP-48274					Added doDateToDestination() method
 */
package com.rrd.c1ux.api.services.checkout;

import java.lang.reflect.InvocationTargetException;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.checkout.DateToDestinationRequest;
import com.rrd.c1ux.api.models.checkout.DateToDestinationResponse;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoRequest;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoResponse;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveRequest;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveResponse;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderInfo;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;

public interface OrderHeaderInfoService
{
	public OrderHeaderInfoResponse getOrderHeaderInfo(SessionContainer sc, OrderHeaderInfoRequest orderHeaderInfoRequest) throws AtWinXSException, IllegalAccessException, InvocationTargetException;
	public boolean validateOrderHeaderInfo(OrderDetailsHeaderInfo headerInfo, Message msg, OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, BaseResponse response) throws AtWinXSException;
	public OrderInfoHeaderSaveResponse saveOrderHeaderInfo(SessionContainer sc, OrderInfoHeaderSaveRequest request) throws AtWinXSException;
	public DateToDestinationResponse doDateToDestination(SessionContainer sc, DateToDestinationRequest request) throws AtWinXSException; // CAP-48274
}
