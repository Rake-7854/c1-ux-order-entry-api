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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOSFullDetailRequest", description = "Request Class to view a specific 'Order details' from "
		+ "previously searched 'Orders Detail' via API 'api/orders/getsearchordersdetail'", type = "object")
public class COOSFullDetailRequest {
	
	@Schema(name ="searchResultNumber", description = "'Search Result Number' of an Order retrieved in Previous 'Orders search'", 
			type = "string", example="1")
	@Size(min=0, max=10)
	private String searchResultNumber;
	
	@Schema(name ="orderID", description = "'Order ID' of an Order retrieved in Previous 'Orders search'. "
			+ "'0' means order was not placed through Custom Point", type = "int", example="581863")
	@Min(0)
	@Max(2147483647)
	private int orderID;
	
	@Schema(name ="orderNum", description = "'WCSS Order Number' of an Order retrieved in Previous 'Orders search'", 
			type = "string", example="20639722")
	@Size(min=0, max=8)
	private String orderNum;
	
	@Schema(name ="salesReferenceNumber", description = "'Sales Reference Number' of an Order retrieved in Previous 'Orders search'", 
			type = "string", example="80030653")
	@Size(min=0, max=20)
	private String salesReferenceNumber;
	
	@Schema(name ="sortLinesBy", description = "Sort option(Column in XST093) will be applied to retrieve 'Items Ordered' in an Order. "
			+ "Default/Allowable value is 'ORD_LN_NR'", type = "string", allowableValues = {"ORD_LN_NR"},   example="ORD_LN_NR")
	@Size(min=0, max=9)
	@NotBlank 
	private String sortLinesBy;
	
}

