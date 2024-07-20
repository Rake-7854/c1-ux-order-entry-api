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
 *  03/17/23	S Ramachandran		CAP-38720 		API Standardization - Order Detail by Sales ref in Order Search conversion to standards 
 *  06/29/23	A Salcedo			CAP-41713		Added Show Bill To Info and Show Bill To Attn boolean.
 *  11/10/23	M Sakthi			CAP-44979		C1UX BE - Modify order search to return credit card fields and carrier service fields
 *  12/05/23	Krishna Natarajan	CAP-45058		Added a new collection object orderMessages
 *  12/22/23	M Sakthi			CAP-45812		C1UX BE - Add order originator to order search details
 *  04/12/23	S Ramachandran		CAP-48488 		Modify Order Search to show expedite information if an order is expedited.
 *  05/16/24	Krishna Natarajan 	CAP-49259		Added a new variable distListText
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.List;

import javax.validation.constraints.Size;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.admin.profile.entity.CustomerReferenceField;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusHeaderVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOSFullDetailResponse", description = "Response Class to view Order details including Order info, Customer info, Cost Info "
		+ "and Items Ordered Info ", type = "object")
public class COOSFullDetailResponse  extends BaseResponse {
	
	@Schema(name ="objOrderStatusHeaderVO", description = "'Order Status Header' shows  Order Info, Customer Info and  Cost Info", type = "object")
	private OrderStatusHeaderVO objOrderStatusHeaderVO;
	
	@Schema(name ="customerReferenceFields", description = "List of Customer Reference Fields", type = "array")
	private List<CustomerReferenceField> customerReferenceFields;
	
	@Schema(name ="statusDescription", description = "Order Status", type = "string", example="Invoiced")
	@Size(min=0, max=100)
	private String statusDescription;
	
	@Schema(name ="billMethodValue", description = "'Billing Method' of an order available in 'Customer Info Section", type = "string", example="Invoiced")
	@Size(min=0, max=100)
	private String billMethodValue;
	
	@Schema(name ="isDisplayOrderFiles", description = "'Emails/Order Files' Flag indicator for a User Group", type = "boolean", example="true")
	protected Boolean isDisplayOrderFiles = null;
	
	@Schema(name ="emailVisibility", description = "'Email visibility' Flag indicator for a User Group", type = "boolean", example="true")
	protected Boolean emailVisibility;
	
	@Schema(name ="viewEmailOrderfilesLabel", description = "Display value for  'Emails/Order Files'", type = "string", example="View Emails/Order Files")
	@Size(min=0, max=25)
	private String viewEmailOrderfilesLabel;
	
	@Schema(name ="viewEmailOrderfilesImg", description = "Display 'Image Icon path' parameter for 'Emails/Order Files'", type = "string", example="/images/icons/png/Icon_Files.png")
	@Size(min=0, max=100)
	private String viewEmailOrderfilesImg;
	
	@Schema(name ="COOSDetailsItemsOrderedData", description = "List of Items Ordered", type = "array")
	private List<COOSDetailsItemsOrderedData> itemsOrderedData;

	@Schema(name ="showBillToInfo", description = "Show Bill To Info Flag indicator for a User Group", type = "boolean", example="true")
	protected Boolean showBillToInfo = null;
	
	@Schema(name ="showBillToAttn", description = "Show Bill To Attention Flag indicator for a User Group", type = "boolean", example="true")
	protected Boolean showBillToAttn = null;
	
	
	//CAP-44979
	@Schema(name ="paymentName", description = "Holding the name of the person who the credit card belongs", type = "string", example="Sakthi")
	private String paymentName = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="paymentCardType", description = "Type of the Card", type = "string", example="V")
	private String paymentCardType = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="paymentCardLast4", description = "Last 4 Digit of the Card", type = "string", example="3333")
	private String paymentCardLast4 = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="paymentReceiptEmail", description = "Card holder Email", type = "string", example="test@test.com")
	private String paymentReceiptEmail = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="paymentExpirationDate", description = "Card Expiration Date", type = "string", example="08/22")
	private String paymentExpirationDate = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="carrierServiceLevel", description = "Carrier Service Level", type = "string", example="UPS")
	private String carrierServiceLevel = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="thirdPartyAccount", description = "Third Party Account", type = "string", example="AS23242")
	private String thirdPartyAccount = AtWinXSConstant.EMPTY_STRING;
	
	//CAP-45812
	@Schema(name ="orderOriginator", description = "Originator Name", type = "string", example="Sakthi")
	private String orderOriginator = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="orderOriginatorLoginID", description = "Originator Login Id", type = "string", example="USER-RRD2")
	private String orderOriginatorLoginID = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="orderOriginatorProfileID", description = "Originator Profile Id", type = "string", example="AS21344")
	private String orderOriginatorProfileID = AtWinXSConstant.EMPTY_STRING;
		
	
	@Schema(name ="orderMessages", description = "Order messages to get the message text and type of the message", type = "array", example=" {\r\n"
			+ "      \"messageText\": \"ORIGINAL INPUT EDI ROUTING ACCOUNT NR: 6543211\",\r\n"
			+ "      \"messageType\": \"AL\"\r\n"
			+ "    }")
	private List<OrderMessages> orderMessages;
	
	@Schema(name = "expedited", description = "'Expedited Status', Flag indicator if the order was expedited", type = "boolean", example = "true")
	private boolean expedited = false;
	
	@Schema(name = "expeditedFee", description = "Expedited Fee", type = "string", example = "$ 0.0")
	private String expeditedFee = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "expeditedDate", description = "Expedited Date", type = "string", example = "04/30/2024")
	private String expeditedDate = AtWinXSConstant.EMPTY_STRING;
	
	//CAP-49259
	@Schema(name = "distListText", description = "Distribution List text", type = "string", example = "Distribution List")
	private String distListText=AtWinXSConstant.EMPTY_STRING;
}
