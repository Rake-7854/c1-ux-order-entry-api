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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOSItemInShipmentTrackingRequest", description = "Request Class to view 'Items' under a 'Shipment'", type = "object")
public class COOSItemInShipmentTrackingRequest {
	
	@Schema(name ="wcssOrderNumber", description = "WCSS Order Number", type = "string", example="20639722")
	@Size(min=0, max=8)
	private String wcssOrderNumber;
	
	@Schema(name ="seqNo", description = "SHIPMENT sequence number", type = "string", example="0001")
	@Size(min=4, max=4) //XRT045
	private String seqNo;
	
	@Schema(name ="salesRefNum", description = "Sales Reference Number", type = "string", example="80030653")
	@Size(min=0, max=20)
	private String salesRefNum;
	
	@Schema(name ="orderLineNum", description = "CustomPoint Order Line Number. "
			+ "Only to be used, when the shipment is from External order status information, not WCSS", type = "int", example="-1")
	@Min(-1)
	@Max(2147483647)
	private int orderLineNum;
	
}
