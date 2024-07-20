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
@Schema(name ="COOSShipmentDetail", description = "Response Class to view 'Shipments' and 'Items' under a 'Shipment'")
public class COOSShipmentDetail 
{
	@Schema(name ="sequenceNumberStr", description = "SHIPMENT sequence number", type = "string", example="0001")
	@Size(min=4, max=4) //XRT045
	private String sequenceNumberStr;
	
	@Schema(name ="orderLineNum", description = "CustomPoint Order Line Number. "
			+ "Only to be used, when the shipment is from External order status information, not WCSS", type = "int", example="-1")
	@Min(-1)
	@Max(2147483647)
	private int orderLineNum;//CAP-708
	
	@Schema(name ="warehouse", description = "Warehouse Name", type = "string", example="ST CHARLES LABEL")
	@Size(min=0, max=30) //XRT045
	private String warehouse;
	
	@Schema(name ="shipDate", description = "Ship Date", type = "string", example="04/15/2022")
	@Size(min=0, max=10)
	private String shipDate;
	
	@Schema(name ="carrier", description = "Carrier Name", type = "string", example="UPS")
	@Size(min=0, max=30) //XRT039
	private String carrier;
	
	@Schema(name ="trackingNum", description = "Carrier Tracking Number", type = "string", example="1Z42W7A20260689105")
	@Size(min=0, max=32) //XST099
    private String trackingNum;
    
	@Schema(name ="trackingURL", description = "Carrier Tracking URL", type = "string", example="http://wwwapps.ups.com/etracking/tracking.cgi?TypeOfInquiryNumber=T&InquiryNumber1=1Z42W7A20260689105")
	@Size(min=0, max=200) //orderstatus.properties
    private String trackingURL;
    
    @Schema(name ="itemInShipmentAvailabilityFlag", description = "Item(s) in Shipment Available/Fail Flag", type = "boolean", example="true")
    private boolean itemInShipmentAvailabilityFlag;
    
    @Schema(name ="itemInShipmentAvailabilityMessage", description = "Item(s) in Shipment Available/Fail Message", type = "string", example="Item information available")
    @Size(min=0, max=200)
    private String itemInShipmentAvailabilityMessage;
    
    
    @Schema(name ="COOSItemInShipmentDetail", description = "'List of Items' in a Shipment", type = "array")
    private Collection<COOSItemInShipmentDetail> itemInShipmentDetails;
	
    @Schema(name ="sequenceNumber", description = "Sequence Number", type = "int", example="2")
    @Min(-1)
	@Max(2147483647)
    private int sequenceNumber;
    
    @Schema(name ="shipMethod", description = "Ship Method", type = "string", example="GROUND SERVICE")
    @Size(min=0, max=30) //XRT040
    private String shipMethod;
	
    @Schema(name ="itemDesc", description = "Item Description", type = "string", example="PROPERTY OF")
    @Size(min=0, max=120)
    private String itemDesc;
	
    @Schema(name ="itemNum", description = "Customer Item Number", type = "string", example="")
    @Size(min=0, max=30)
    private String itemNum;
	
    @Schema(name ="shipQty", description = "Ship Quantity", type = "string", example="3")
    @Size(min=0, max=10)
    private String shipQty; 
	
    @Schema(name ="status", description = "Ship Status", type = "string", example="Vendor System: Shipped, Pre-Rated")
    @Size(min=0, max=100)
    private String status; 
	
    @Schema(name ="freightTerms", description = "Freight Terms", type = "string", example="Prepaid")
    @Size(min=0, max=50)
    private String freightTerms; 
	
    
    @Schema(name ="invoiceNumber", description = "Invoice Number", type = "string", example="")
    @Size(min=0, max=50)
	private String invoiceNumber; 
	
    @Schema(name ="summaryInvoiceNumber", description = "Summary Invoice Number", type = "string", example="")
    @Size(min=0, max=50)
	private String summaryInvoiceNumber; 
	
    @Schema(name ="salesTaxAmt", description = "Sales Tax Amount", type = "string", example="$9.24")
    @Size(min=0,max=20)
	private String salesTaxAmt; 
	
    @Schema(name ="orderAndLineChargesAmt", description = "Order And Line Charges Amt", type = "string", example="$164.95")
    @Size(min=0,max=20)
	private String orderAndLineChargesAmt;  
	
    @Schema(name ="totalSaleAmt", description = "Total Sale Amount", type = "string", example="$174.19")
    @Size(min=0,max=20)
	private String totalSaleAmt; 
	
    @Schema(name ="shippingAmt", description = "Shipping Amount", type = "string", example="")
    @Size(min=0,max=20)
	private String shippingAmt; 
	
	
    @Schema(name ="billableFreightAmt", description = "Billable Freight Amount", type = "string", example="")
    @Size(min=0,max=20)
	private String billableFreightAmt;
	
    @Schema(name ="serviceChargesAmt", description = "Service Charges Amount", type = "string", example="")
    @Size(min=0,max=20)
	private String serviceChargesAmt;  
	
    @Schema(name ="price", description = "Price", type = "string", example="$174.19")
    @Size(min=0,max=20)
	private String price;
	
    @Schema(name ="isShowPrice", description = "Show Price Flag", type = "boolean", example="true")
	private boolean isShowPrice;
}
