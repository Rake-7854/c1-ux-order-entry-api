/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/23/24				C Codina				CAP-41549					Initial Version
 *	04/23/24				N Caceres				CAP-48821					Add method for saving Order Template
 *	04/25/24				S Ramachandran			CAP-48889					Added service method to delete an order template
 */

package com.rrd.c1ux.api.services.orders.ordertemplate;

import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.TemplateOrderListResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface OrderTemplateService {
	
	public TemplateOrderListResponse getTemplateOrderList(SessionContainer sc)
			throws AtWinXSException;
	public LoadSaveOrderTemplateResponse getOrderTemplateDetails(SessionContainer sc, LoadSaveOrderTemplateRequest request) throws AtWinXSException;
	
	public DeleteTemplateResponse deleteOrderTemplate(SessionContainer sc, DeleteTemplateRequest request)
			throws AtWinXSException;

	public UseOrderTemplateResponse loadTemplateOrder(SessionContainer sc, UseOrderTemplateRequest request)
			throws AtWinXSException;
	
	public SaveOrderTemplateResponse saveOrderTemplate(SessionContainer sc, SaveOrderTemplateRequest saveOrderTemplateRequest) throws AtWinXSException;

}
