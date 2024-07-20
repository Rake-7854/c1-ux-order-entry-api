/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------------
 *  11/23/22	S Ramachandran  	CAP-36557   	Get Order details page - Order info, customer info, Items ordered 
 *  												and Ordered cost sections 
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class COOSShipmentAndTrackingRequest {
	private String orderNum;
	private String salesReferenceNumber;
}
