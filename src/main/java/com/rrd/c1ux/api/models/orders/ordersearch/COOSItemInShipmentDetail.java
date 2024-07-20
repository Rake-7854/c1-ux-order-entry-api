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

@Schema(name ="COOSItemInShipmentDetail", description = "Response Class to view 'Single Item' under a 'Shipment'")
public class COOSItemInShipmentDetail
{
	@Schema(name ="shipNo", description = "Shipment Number", type = "string", example="0001")
	private String shipNo;
	
	@Schema(name ="fulfillmentComponentDispLineNum", description = "Fulfillment Line Number", type = "string", example="2.0")
	@Size(min=0,max=15)
	private String fulfillmentComponentDispLineNum;
	
	@Schema(name ="orderLineNo", description = "EDI Line No", type = "string", example="2")
	@Min(0)
	@Max(2147483647)
	private int orderLineNo;
	
	@Schema(name ="itemImage", description = "Item Image with relative path", type = "string", example="/cp/images/global/NoImageAvailable.png")
	@Size(min=0,max=100)
	private String itemImage;
	
	@Schema(name ="itemDesc", description = "Item Description", type = "string", example="PROPERTY OF")
	@Size(min=0,max=120)
	private String itemDesc;
	
	@Schema(name ="custItemNo", description = "Customer Item No", type = "string", example="209929")
	@Size(min=0,max=30)
	private String custItemNo; 
	
	@Schema(name ="vendorItemNo", description = "Vendor Item No", type = "string", example="UL209929")
	@Size(min=0,max=15)
	private String vendorItemNo;
	
	@Schema(name ="itemUOMFactor", description = "Item UOM and Factor in Full text", type = "string", example="Roll of 1")
	@Size(min=0,max=20)
	private String itemUOMFactor;
	
	@Schema(name ="lineShippedQty", description = "Line Shipped Quantity", type = "string", example="1")
	@Size(min=0,max=10)
	private String lineShippedQty;
	
	@Schema(name ="unitPrice", description = "Unit Price per Each", type = "string", example="$164.95")
	@Size(min=0,max=20)
	private String unitPrice;
	
	@Schema(name ="extendedPrice", description = "Extended Price", type = "string", example="$164.95")
	@Size(min=0,max=20)
	private String extendedPrice;
	
	@Schema(name ="itemUOM", description = "Item UOM Code and Factor", type = "string", example="RL/1")
	@Size(min=0,max=2)
	private String itemUOM; 
	
	@Schema(name ="kitComponentItemSeqNr", description = "Kit Item Sequence Number", type = "int", example="0")
	@Min(0)
	@Max(2147483647)
	private int kitComponentItemSeqNr;
	
	@Schema(name ="fulfillmentLineCd", description = "Fulfillment Line Type Code", type = "string", example="N", allowableValues = {"","C","N","M"})
	@Size(min=1, max=1) 
	private String fulfillmentLineCd; 
	
	@Schema(name ="qtyShippedPerKit", description = "Quantity Shipped per Kit", type = "int", example="0")
	@Min(0)
	@Max(2147483647)
	private int qtyShippedPerKit;
	
	@Schema(name ="EDISubLine", description = "EDI Sub Line", type = "int", example="0")
	@Min(0)
	@Max(2147483647)
    private int EDISubLine;
	
	@Schema(name ="EDISubSubLine", description = "EDI Component Line Sub Line", type = "int", example="0")
	@Min(0)
	@Max(2147483647)
    private int EDISubSubLine;
	
	@Schema(name ="jobNumberDisplayText", description = "Job Number", type = "string", example="0075999960000")
	@Size(min=0, max=13)
  	private String jobNumberDisplayText;
	
	@Schema(name ="jobNumberLinkURL", description = "Link URL to view Item check inventory in Custompoint with relative path", type = "string", 
			example="/xs2/items/itemdetails?ttsessionid=cmM2WWNkVURVdVFKa21JZUlQSFN1Yi9Ubng1a2VBL25BSi9JNlpVTXIyUXRNS3B4Um91djBnPT0=&actionID=BROKER_ITEM_INV&eventID=ITEM_CHK_INVENTORY_FIRST_EVT&CUST_ITEM=209929&WALLACE_ITEM=UL209929&ItemNbr=209929&VendorItemNbr=UL209929&ITEM_DS=PROPERTY%20OF&backToOrderLineDtl=true&ORD_STAT_SRCH_RSLT_NR=1")
	@Size(min=0, max=500) 
  	private String jobNumberLinkURL;
	
	@Schema(name ="shipDate", description = "Ship Date", type = "string", example="04/15/2022")
	@Size(min=0, max=10)
	private String shipDate;
	
	@Schema(name ="isShowVendorItemNumber", description = "Show Vendor Item Number Flag", type = "boolean", example="true")
	private	boolean isShowVendorItemNumber;
	
	@Schema(name ="isSimplifiedView", description = "User Admin setting is Simplified instead of Advance Flag", type = "boolean", example="false")
	private boolean isSimplifiedView;
}
