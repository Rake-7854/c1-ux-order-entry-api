/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/14/23				A Salcedo				CAP-38152					Initial Version
 *	05/09/23				S Ramachandran			CAP-38156					Added Extended Item Quantity to load it in Checkout Review Order 
 *09/12/23					C Codina				CAP-42170					API Change - Get Order Header Info API needs to add ship info object and carrier fields to order of display
 *	04/08/24				C Codina				CAP-48436					Added field for DTD validation
 *	04/18/24				S Ramachandran			CAP-48719					Added variable Expedited Flag and Earliest Delivery Date
 *	04/24/24				Krishna Natarajan		CAP-48961					Added a varaiable validateDueDateSetting to get due date setting UG
 *	05/13/24				C Codina				CAP-49122					Added a variable for specialInstructions
 *	06/13/24				Krishna Natarajan		CAP-50416					Added a new flag allowMultipleEmails
 *  06/25/24				Krishna Natarajan		CAP-50471					Added a new variable for delivery options
 *  07/08/24				Krishna Natarajan		CAP-50851					Added a new variable for delivery Options List  
 *  07/09/24				Krishna Natarajan		CAP-50886					Added a new variable efdOnly to set EFD only order flag true/false 
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.catalogitems.FileDeliveryOption;
import com.rrd.c1ux.api.services.checkout.OrderDetailsHeaderInfoC1UX;
import com.rrd.c1ux.api.services.checkout.OrderDetailsShippingInfoC1UX;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderHeaderInfoResponse", description = "Response Class for Order Header Info to load the Checkout Details", type = "object")
public class OrderHeaderInfoResponse extends BaseResponse
{
	@Schema(name = "orderDetailsHeaderInfo", description = "Class that contains all information necessary for the display of the Order Header Info section in checkout", type = "object")
	private OrderDetailsHeaderInfoC1UX orderDetailsHeaderInfo;
	
	@Schema(name = "orderDetailsHeaderInfo", description = "Class that contains all information necessary for the display of the Order Details Shipping info section in checkout", type = "object")
	private OrderDetailsShippingInfoC1UX orderDetailsShippingInfo;

	@Schema(name = "validAndComplete", description = "Flag indicating the values are valid and complete so the user can proceed without modifying them when resuming a saved order.", type = "boolean")
	private boolean validAndComplete = false;
	
	@Schema(name = "orderHeaderInfoDisplayOrder", description = "Class that contains the order in which the fields will display of the Order Header Info section in checkout", type = "object")
	private List<List<String>> orderHeaderInfoDisplayOrder;
	
	@Schema(name = "extendedItemQuantity", description = "Class that contains array of Extended Item Quantity", type = "array")
	private OEExtendedItemQuantityResponseBean[] extendedItemQuantity; 
	
	@Schema(name = "callDtdService", description = "Flag indicating whether we call dtd or not", type = "boolean")
	private boolean callDtdService = false;

	@Schema(name = "expeditedOrder", description = "'Expedited Order Status', Flag indicator if the order has been selected to expedite", type = "boolean", example = "true")
	private boolean expeditedOrder = false;
	
	@Schema(name = "earliestDueDate", description = "Earliest Due Date", type = "string", example = "04/30/2024")
	private String earliestDueDate = AtWinXSConstant.EMPTY_STRING;
	
	//CAP-48961
	@Schema(name = "validateDueDateSetting", description = "Due Date Setting", type = "string", example = "N")
	private String validateDueDateSetting=AtWinXSConstant.EMPTY_STRING;
	
	//CAP-49122
	@Schema(name = "specialInstructions", description = "A string that holds special Instructions", type = "String", example = "")
	private String specialInstructions;
	
	//CAP-49278
	private Collection<String> requiredEmailAddress;
	
	//CAP-50416
	@Schema(name = "allowMultipleEmails", description = "Flag indicating allow multiple emails true/false", type = "boolean")
	private boolean allowMultipleEmails= false;
	
	// CAP-50471
	@Schema(name ="filedeliveryOptions", description = "Map of file delivery option codes and display label.", type = "array")
	private Map<String,String> filedeliveryOptions;		
	
	//CAP-50581
	@Schema(name ="deliveryOptionsList", description = "List of file delivery option codes and equivalent options and display label.", type = "array")
	private List<FileDeliveryOption> deliveryOptionsList;
	
	@Schema(name = "efdOnly", description = "Flag indicating true/false if order is EFD only order", type = "boolean")
	private boolean efdOnly = false;
}
