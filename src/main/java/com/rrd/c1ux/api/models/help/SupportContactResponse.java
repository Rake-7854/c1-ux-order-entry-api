/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/15/22	Satish kumar A		CAP-35429- support page 	Initial creation
 *  09/28/22	Satish kumar A		CAP-35430- support page 	Send email with support page form data
 *  12/01/2022	E Anderson			CAP-36154		            BE Updates for translation.
 *  04/24/23	Nathaniel Caceres	CAP-39246					Backend updates for translation.
 */

package com.rrd.c1ux.api.models.help;

import java.util.List;
import java.util.Map;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//CAP-39246 OpenAPI3 documentation
@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SupportContactResponse", description = "Response Class for Support Contact", type = "object")
public class SupportContactResponse extends BaseResponse {

	@Schema(name ="status", description = "Support Contact Response status", type = "string", example="Success", allowableValues = {"Success","Failed"})
	private String status=RouteConstants.REST_RESPONSE_FAIL;
	@Schema(name ="errorMessage", description = "Custom error message, if fail to retrieve", type = "string", example="Error in loading the Support Contact details.")
	private String errorMessage="";
	@Schema(name ="successMessage", description = "Custom success message, if there is no error encountered", type = "string", example="Support Contact details loaded successfully.")
	private String successMessage="";
	@Schema(name ="headerSupportPhone", description = "Support Contact number", type = "string", example="1-866-362-3230")
	private String headerSupportPhone=""; 
	@Schema(name ="supportPhoneText", description = "Label for Storefront help line number", type = "string", example="The number for the Storefront help line is")
	private String supportPhoneText=""; 
	@Schema(name ="supportPhoneTextValue", description = "Storefront help line number", type = "string", example="1-866-362-3230")
	private String supportPhoneTextValue="";
	@Schema(name ="userEmailText", description = "Label for email address", type = "string", example="The email address is")
	private String userEmailText="";
	@Schema(name ="userEmailTextValue", description = "Support Contact email address", type = "string", example="CustomPointSupport@rrd.com")
	private String userEmailTextValue=""; 
	@Schema(name ="customSupportHTML", description = "Custom HTML for Support Contact", type = "string", example="<b>only</b>")
	private String customSupportHTML=""; 
	@Schema(name ="accountCustomer", description = "Customer Name", type = "string", example="UX")
	private String accountCustomer=""; 
	@Schema(name ="accountUser", description = "User Name", type = "string", example="Lindsay Lacek")
	private String accountUser=""; 
	@Schema(name ="accountUserID", description = "User ID", type = "string", example="LLACEK-RRD")
	private String accountUserID=""; 
	@Schema(name ="showContactForm", description = "Display Support Contact form", type = "string", example="Y", allowableValues = {"Y","N"})
	private String showContactForm="N";
	@Schema(name ="defaultFullName", description = "Default user full name", type = "string", example="Lindsay Lacek")
	private String defaultFullName=""; 
	@Schema(name ="defaultEmailAddress", description = "Default email address", type = "string", example="Lindsay Lacek")
	private String defaultEmailAddress="";
	@Schema(name ="defaultPhoneNumber", description = "Default phone number", type = "string", example="4013329041")
	private String defaultPhoneNumber="";
	@Schema(name ="defaultContactSelectText", description = "Label for default send to", type = "string", example="Select a send to")
	private String defaultContactSelectText=""; 
	@Schema(name ="defaultSelectedContactNumber", description = "Default selected contact number", type = "string", example="1904")
	private String defaultSelectedContactNumber="";
	
	//CAP-36154
	@Schema(name ="translation", description = "Messages from \"helpandcontact\" translation file will load here.", type = "string",  example="\"translation\": { \"nameLabel\": \"Your Name\"}")
	private Map<String, String> translation;
	@Schema(name ="contacts", description = "List of support contacts", type = "array")
	List<SupportContacts> contacts=null;
	
}
