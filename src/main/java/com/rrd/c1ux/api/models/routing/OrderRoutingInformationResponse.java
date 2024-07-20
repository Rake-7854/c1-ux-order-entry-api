/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      ---------------------------------------------------------
 *  08/09/23	S Ramachandran		CAP-42746       Response Object of Order Routing Information for an Order  
 */

package com.rrd.c1ux.api.models.routing;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.ordersearch.orderdetails.routinginfo.widget.OSDetailsRoutingInfoBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderRoutingInformationResponse", description = "Response Class to retrieve Order Routing Information", type = "object")
public class OrderRoutingInformationResponse extends BaseResponse {
	
	@Schema(name ="routingDetails", description = "A collection of routing detail with approval queue and approver info", type = "array")
	private Collection<OSDetailsRoutingInfoBean> routingDetails;
	
}