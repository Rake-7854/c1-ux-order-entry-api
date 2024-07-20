/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 * 
 * Revisions: 
 * 	Date		Modified By			DTS#				Description
 * 	--------	-----------			----------			-----------------------------------------------------------
 * 12/23/22		S Ramachandran		CAP-36916 			Get Shipments and Items under Shipping and tracking information
 *
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.Collection;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOSShipmentAndTrackingResponse", description = "Response Class to view Shipment And Tracking "
		+ "for a specific order from previously searched 'Orders Detail'", type = "object")
public class COOSShipmentAndTrackingResponse
{
	@Schema(name ="orderNo", description = "WCSS Order Number", type = "string", example="20639722")
	@Size(min=0, max=8)
	private String orderNo;
	
	@Schema(name ="salesRefNum", description = "Sales Reference Number", type = "string", example="80030653")
	@Size(min=0, max=20)
	private String salesRefNum;
	
	@Schema(name ="isShowPrice", description = "Show Price Flag", type = "boolean", example="true")
	private boolean isShowPrice;
	
	@Schema(name ="shipmentAvailabilityFlag", description = "Shipments Info availability Flag", type = "boolean", example="true")
	private boolean shipmentAvailabilityFlag;
	
	@Schema(name ="shipmentAvailabilityMessage", description = "Shipments Info availability Message", type = "string", example="Shipment information available")
	@Size(min=0, max=200)
	private String shipmentAvailabilityMessage;
	
	@Schema(name ="trackingDetails", description = "List of Shipments", type = "collection")
	private Collection<COOSShipmentDetail> trackingDetails;
}
	
