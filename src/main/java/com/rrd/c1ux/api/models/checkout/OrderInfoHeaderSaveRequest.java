/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date				Modified By				JIRA#						Description
 * 	--------			-----------				-----------------------		--------------------------------
 *	05/08/23			A Boomker				CAP-38153					Initial Version
 *	08/17/23			N Caceres				CAP-41551					Save OrderDetailsShippingInfoImpl bean info on save call for order info
 *	11/03/2023			N Caceres				CAP-44840					Add requested ship date to the get order header info service
 *	01/09/24			S Ramachandran			CAP-46294					Add order due date to Checkout pages
 *	04/15/24			Satishkumar A			CAP-48437					C1UX BE - Modify /api/user/saveorderheaderinfo method to save information for DTD
 *	05/13/24			C Codina				CAP-49122					Added a variable for specialInstructions
 */
package com.rrd.c1ux.api.models.checkout;

import java.io.Serializable;

import javax.validation.constraints.Size;

import com.rrd.c1ux.api.models.common.GenericNameValuePair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OrderInfoHeaderSaveRequest", description = "Response Class for saving the order info header section", type = "object")
public class OrderInfoHeaderSaveRequest implements Serializable
{


	/**
	 *
	 */
	private static final long serialVersionUID = 4322777100082045916L;
	@Schema(name = "orderTitle", description = "Order title to be saved for the order - may be required based on admin", type = "string", example = "MOORE WALLACE INC")
	@Size(min = 0, max = 150)
	private String orderTitle;
	@Schema(name = "poNumber", description = "PO Number to be saved for the order - may be required based on admin", type = "string", example = "123ABC")
	@Size(min = 0, max = 20)
	private String poNumber;

	@Schema(name = "headerCustRefs", description = "Header level customer reference fields to be saved for the order. Name for each should contain the cust ref code and number. These may be required based on admin", type = "array",
			example = "[ { \"name\" : \"CR1\", \"value\" : \"My CR1 Value\" }, { \"name\" : \"CR5\", \"value\" : \"My CR5 Value\" } ]")
	private GenericNameValuePair[] headerCustRefs;
	@Schema(name = "headerMessages", description = "Header level order messages to be saved for the order. Name for each should contain the prefix OHM and number. These may be required based on admin", type = "array",
			example = "[ { \"name\" : \"OHM1\", \"value\" : \"I am an order header level message. Please pass me on.\" }, { \"name\" : \"OHM5\", \"value\" : \"This order message is for you.\" } ]")
	private GenericNameValuePair[] headerMessages;

	//CP Order Object fields
	@Schema(name = "contactName", description = "Contact Name to be saved for the order - may be required based on admin", type = "string", example = "James Buchanan")
	@Size(min = 0, max = 50)
	private String contactName;
	@Schema(name = "contactPhone", description = "Contact Phone to be saved for the order - may be required based on admin", type = "string", example = "800-CALL-NOW")
	@Size(min = 0, max = 20)
	private String contactPhone;
	@Schema(name = "contactEmail", description = "Contact Email to be saved for the order - may be required based on admin", type = "string", example = "joeshmoe@gmail.com")
	@Size(min = 0, max = 128)
	private String contactEmail;
	
	@Schema(name = "carrierServiceLevel", description = "Carrier or Service level", type = "string", example = "GS;FEDXR;C")
	@Size(min = 0, max = 10)
	private String carrierServiceLevel;
	
	@Schema(name = "thirdPartyAccount", description = "Third Party Account Number", type = "string", example = "ABC123456789")
	@Size(min = 0, max = 20)
	private String thirdPartyAccount;
	
	@Schema(name ="requestedShipDate", description = "Requested ship date. Date should be in MM/dd/yyyy format only.", type = "string", example="01/25/2023")
	@Size(min=0, max=10)
	String requestedShipDate;
	
	@Schema(name ="orderDueDate", description = "Order Due Date. Date should be in MM/DD/YYYY format only.", type = "string", example="01/27/2023")
	@Size(min=0, max=10)
	private String orderDueDate;
	//CAP-48437
	@Schema(name ="expediteOrder", description = "A boolean indicating true/false if the order is expedited", type = "boolean")
	private boolean expediteOrder;
	//CAP-48437
	@Schema(name = "expediteOrderFee", description = "A double value with the expedite order fee", type = "double")
	private double expediteOrderFee;
	
	@Schema(name = "specialInstruction", description = "A String value for Special Instruction", type = "String")
	private String specialInstruction;

}
