package com.rrd.c1ux.api.models.orders.ordersearch;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderSearchResult", description = "Class for an individual search result order", type = "object")
public class COOrderSearchResult {
	@Schema(name ="salesRefNumber", description = "Sales Reference Number in WCSS and/or CustomPoint - may be empty", type = "string", example="80031326")
	@Size(max=20)
	String salesRefNumber;
	@Schema(name ="purchaseOrderNumber", description = "Purchase Order Number in WCSS and/or CustomPoint", type = "string", example="PO Cat 15362")
	@Size(max=20)
	String purchaseOrderNumber;
	@Schema(name ="orderDateDisplay", description = "Date present in formatted for User's locale", type = "string", example="01/25/2023")
	String orderDateDisplay;
	//CAP-38344
	@Schema(name ="orderTime", description = "Date/Time of order submitted, in the format yyyy-MM-dd hh:mm:ss", type = "string", example="2023-25-01 04:30:00")
	String orderTime;
	@Schema(name ="orderStatusCode", description = "Displayable translation text corresponding to the order status code and applicable admin setting within the XST156_ORD_STAT_CD table", type = "string", example="Invoiced")
	String orderStatusCode;
	@Schema(name ="trackingLinks", description = "Boolean flag for indicating Tracking link availability  ", type = "boolean", example="false")
	boolean trackingLinks;
	@Schema(name ="reqSearchResultNumber", description = "Index of order in cached search results - whole number of 1 or more", type = "string", example="2")
	String reqSearchResultNumber;
	@Schema(name ="orderId", description = "Numeric value corresponding to the key for this order in XST076_ORD_HDR table if CustomPoint sent it and the order can be found - 0 if it is not found", type = "string", example="0")
	@Min(0)
	int orderId;
	@Schema(name ="orderNum", description = "WCSS Order Number in WCSS - may be empty if order is not in WCSS", type = "string", example="27889425")
	String orderNum;
	
	//CAP-41553
	@Schema(name ="allowedToCopy", description = "Flag indicating if the user allowed to copy particular order or not. Default is false.", type = "boolean")
	boolean allowedToCopy = false;	



}
