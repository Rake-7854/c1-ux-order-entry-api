/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------
 *  12/21/22	S Ramachandran  	CAP-36557   	Copied and altered  OSDetailsItemsOrderedDatafrom CP 
 *  												 
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="COOSDetailsItemsOrderedData", description = "Class to view 'Items Ordered' in an Order", type = "object")
public class COOSDetailsItemsOrderedData {
	
	@Schema(name ="orderID", description = "Order Id. '0' means order was not placed through Custom Point", type = "int", example="581863")
	@Min(0)
	@Max(2147483647)
	private int orderID;
	
	@Schema(name ="lineNumber", description = "Order Line Number", type = "string", example="939933")
	@Size(min=0, max=10)
	private String lineNumber;
	
	@Schema(name ="textLineNumber", description = "EDI Line Number", type = "string", example="0001")
	@Size(max=4)
	private String textLineNumber;
	
	@Schema(name ="itemImage", description = "Item Image URL relative path", type = "string", example="/cp/images/global/NoImageAvailable.png")
	@Size(min=0,max=100)
	private String itemImage;
	
	@Schema(name ="itemDesc", description = "Item Description", type = "string", example="BIG KIT")
	@Size(min=0,max=120)
	private String itemDesc;
	
	@Schema(name ="customerItemNumber", description = "Customer Item Number", type = "string", example="22587MSTBOM")
	@Size(min=0,max=30)
	private String customerItemNumber;
	
	@Schema(name ="vendorItemNumber", description = "Vendor Item Number", type = "string", example="22587MSTBOM")
	@Size(min=0,max=15)
	private String vendorItemNumber;
	
	@Schema(name ="itemStatus", description = "Item Status within the Order process", type = "string", example="Vendor System: Shipped, Pre-Rated")
	private String itemStatus;
	
	@Schema(name ="itemUOMFactor", description = "Item UOM Factor in full text", type = "string", example="Carton of 400")
	@Size(min=0,max=20)
	private String itemUOMFactor;
	
	@Schema(name ="itemQty", description = "Item Quantity Ordered", type = "string", example="15")
	@Size(min=0,max=10)
	private String itemQty;
	
	@Schema(name ="itemShippedQty", description = "Item Shipped Quantity", type = "string", example="15")
	@Size(min=0,max=10)
	private String itemShippedQty;
	
	@Schema(name ="itemExtPrice", description = "Item Extended Sell Price", type = "string", example="$10,040.00")
	@Size(min=0,max=20)
	private String itemExtPrice; // this should follow user's currency locale
	
	@Schema(name ="itemUnitPrice", description = "Item Unit Price per Each", type = "string")
	@Size(min=0,max=20)
	private String itemUnitPrice; // this should follow user's currency locale
	
	@Schema(name ="requestedShipDate", description = "Ship Date requested by User", type = "string")
	private String requestedShipDate; // formatted and should follow user's
	
	@Schema(name ="itemBackordered", description = "Item Back Ordered", type = "string")
	private String itemBackordered;
	
	@Schema(name ="holdCodes", description = "Full text description of any Custom Point Hold Codes on that Order Line", type = "string")
	private String holdCodes;

	@Schema(name ="lineMessages", description = "Order Line Messages", type = "string")
	List<Map<String, String>> lineMessages;
	
	@Schema(name ="itemsAndURlsFilesAndData", description = "Items and URLs of Files and Data", type = "string")
	private COOSDetailsItemFilesAndUrlsData itemsAndURlsFilesAndData; 
	
	@Schema(name ="itemUOM", description = "Item UOM Code", type = "string")
	@Size(min=0,max=2)
	private String itemUOM;
	
	@Schema(name ="uomFactor", description = "UOM Factor Number", type = "string")
	@Size(min=0,max=20)
	private String uomFactor;
	
	@Schema(name ="assemblyInstrctn", description = "Kit Assembly Instruction", type = "string")
	private String assemblyInstrctn;
	
	@Schema(name ="userInstrctn", description = "Kit User Instruction", type = "string")
	private String userInstrctn;
	
	@Schema(name ="kitLineTypeCode", description = "Kit Line Type Code", type = "string", allowableValues = {"","M","P","C"} )
	private String kitLineTypeCode;
		
	@Schema(name ="kitComponentsData", description = "Components Data", type = "array")
	private List<Object> kitComponentsData;
	
	@Schema(name ="itemClassification", description = "Item Classification", type = "string")
	private String itemClassification;
	
	@Schema(name ="customVarAlpha", description = "Alphabetical List of Custom Document variable data", type = "array")
	private List<Map<String, String>> customVarAlpha;
	
	@Schema(name ="customVarHier", description = "List of Custom Document variable data in the Order Entered within the UI", type = "array")
	private List<Map<String, String>> customVarHier;
		
	@Schema(name ="lineCustRefs", description = "List of 'Line Customer Refs' map object", type = "array")
	private List<Map<String, String>> lineCustRefs;

	public String getTextLineNumber()
	{
		return textLineNumber;
	}

	public void setTextLineNumber(String textLineNumber)
	{
		this.textLineNumber = textLineNumber;
	}

	
	public String getLineNumber()
	{
		return lineNumber;
	}

	public String getItemQty()
	{
		return itemQty;
	}

	public void setItemQty(String itemQty)
	{
		this.itemQty = itemQty;
	}

	public String getItemShippedQty()
	{
		return itemShippedQty;
	}

	public void setItemShippedQty(String itemShippedQty)
	{
		this.itemShippedQty = itemShippedQty;
	}

	public String getItemUnitPrice()
	{
		return itemUnitPrice;
	}

	public void setItemUnitPrice(String itemUnitPrice)
	{
		this.itemUnitPrice = itemUnitPrice;
	}

	public String getItemExtPrice()
	{
		return itemExtPrice;
	}

	public void setItemExtPrice(String itemExtPrice)
	{
		this.itemExtPrice = itemExtPrice;
	}

	public void setLineNumber(String lineNumber)
	{
		this.lineNumber = lineNumber;
	}

	public int getOrderID()
	{
		return orderID;
	}

	public void setOrderID(int orderID)
	{
		this.orderID = orderID;
	}

	public String getCustomerItemNumber()
	{
		return customerItemNumber;
	}

	public void setCustomerItemNumber(String customerItemNumber)
	{
		this.customerItemNumber = customerItemNumber;
	}

	public String getVendorItemNumber()
	{
		return vendorItemNumber;
	}

	public void setVendorItemNumber(String vendorItemNumber)
	{
		this.vendorItemNumber = vendorItemNumber;
	}

	public String getItemDesc()
	{
		return itemDesc;
	}

	public void setItemDesc(String itemDesc)
	{
		this.itemDesc = itemDesc;
	}

	public String getItemStatus()
	{
		return itemStatus;
	}

	public void setItemStatus(String itemStatus)
	{
		this.itemStatus = itemStatus;
	}

	public String getItemUOM()
	{
		return itemUOM;
	}

	public void setItemUOM(String itemUOM)
	{
		this.itemUOM = itemUOM;
	}

	public String getRequestedShipDate()
	{
		return requestedShipDate;
	}

	public void setRequestedShipDate(String requestedShipDate)
	{
		this.requestedShipDate = requestedShipDate;
	}

	public String getItemBackordered()
	{
		return itemBackordered;
	}

	public void setItemBackordered(String itemBackordered)
	{
		this.itemBackordered = itemBackordered;
	}

	public String getHoldCodes()
	{
		return holdCodes;
	}

	public void setHoldCodes(String holdCodes)
	{
		this.holdCodes = holdCodes;
	}

	public COOSDetailsItemFilesAndUrlsData getItemsAndURlsFilesAndData()
	{
		return itemsAndURlsFilesAndData;
	}

	public void setItemsAndURlsFilesAndData(COOSDetailsItemFilesAndUrlsData itemsAndURlsFilesAndData)
	{
		this.itemsAndURlsFilesAndData = itemsAndURlsFilesAndData;
	}

	public String getItemImage()
	{
		return itemImage;
	}

	public void setItemImage(String itemImage)
	{
		this.itemImage = itemImage;
	}

	public String getUomFactor()
	{
		return uomFactor;
	}

	public void setUomFactor(String uomFactor)
	{
		this.uomFactor = uomFactor;
	}

	public List<Map<String, String>> getLineMessages()
	{
		return lineMessages;
	}

	public void setLineMessages(List<Map<String, String>> lineMessages)
	{
		this.lineMessages = lineMessages;
	}
	
	// CP-8.1.5 [PVT] new getters and setters for new fields 
	public String getKitLineTypeCode()
	{
		return kitLineTypeCode;
	}

	public void setKitLineTypeCode(String kitLineTypeCode)
	{
		this.kitLineTypeCode = kitLineTypeCode;
	}

	
	public String getAssemblyInstrctn()
	{
		return assemblyInstrctn;
	}

	public void setAssemblyInstrctn(String assemblyInstrctn)
	{
		this.assemblyInstrctn = assemblyInstrctn;
	}

	public String getUserInstrctn()
	{
		return userInstrctn;
	}

	public void setUserInstrctn(String userInstrctn)
	{
		this.userInstrctn = userInstrctn;
	}

	public List<Object> getKitComponentsData()
	{
		return kitComponentsData;
	}
	public void setKitComponentsData(List<Object> kitComponentsData)
	{
		this.kitComponentsData = kitComponentsData;
	}

	public String getItemClassification()
	{
		return itemClassification;
	}

	public void setItemClassification(String itemClassification)
	{
		this.itemClassification = itemClassification;
	}
	//CP-12381 EIQ setter and getter for custom variables
	public List<Map<String, String>> getCustomVarHier()
	{
		return customVarHier;
	}

	public void setCustomVarHier(List<Map<String, String>> customVarHier)
	{
		this.customVarHier = customVarHier;
	}

	public List<Map<String, String>> getCustomVarAlpha()
	{
		return customVarAlpha;
	}

	public void setCustomVarAlpha(List<Map<String, String>> customVarAlpha)
	{
		this.customVarAlpha = customVarAlpha;
	}
	
	public List<Map<String, String>> getLineCustRefs()
	{
		return lineCustRefs;
	}

	public void setLineCustRefs(List<Map<String, String>> lineCustRefs)
	{
		this.lineCustRefs = lineCustRefs;
	}

	public String getItemUOMFactor() {
		return itemUOMFactor;
	}

	public void setItemUOMFactor(String itemUOMFactor) {
		this.itemUOMFactor = itemUOMFactor;
	}

}
