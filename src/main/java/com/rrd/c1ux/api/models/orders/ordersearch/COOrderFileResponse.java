/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions:
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  03/11/2024	N Caceres		CAP-47732					Show a file link to the distribution list
 *	05/17/2024	L De Leon		CAP-49280					Added EFD fields
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.ArrayList;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderFileResponse", description = "Response Class to view Email/Order URL link retrieved", type = "object")
public class COOrderFileResponse {
	@Schema(name ="headerTxt", description = "Header text with sales reference number", type = "String", example="Emails/Order Files for Sales Ref 80031311")
	public String headerTxt;
	@Schema(name ="emailsHeaderTxt", description = "Emails header text", type = "String", example="Emails")
	public String emailsHeaderTxt;
	@Schema(name ="isShowEmailPanel", description = "Show Email panel in Custompoint", type = "String", example="Y")
	public String isShowEmailPanel;
	@Schema(name ="coOrderEmailDetailsList", description = "List of results for sales reference number", type = "array")
	public ArrayList<COOrderEmailDetails> coOrderEmailDetailsList=new ArrayList<COOrderEmailDetails>();
	
	@Schema(name ="orderListFileName", description = "The order list file name", type = "String", example="My List")
	public String orderListFileName;
	
	@Schema(name ="orderListLink", description = "Encrypted URL that will download the file", type = "String", example="/api/orders/getorderlist?a=[XXXXXXXX]")
	public String orderListLink;

	// CAP-49128
	@Schema(name = "orderListNoEmailMsg", description = "Error message in case there are no emails or order files", type = "String", example = "No emails or order files.")
	public String orderListNoEmailMsg = AtWinXSConstant.EMPTY_STRING;
	
	// CAP-49280
	@Schema(name ="hasEfd", description = "This indicates that the order has EFD items", type = "boolean", example="false")
	public boolean hasEfd;

	@Schema(name ="efdLandingPageLink", description = "EFD landing page link", type = "String", example="")
	public String efdLandingPageLink = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="efdEmailLink", description = "EFD email link", type = "String", example="")
	public String efdEmailLink = AtWinXSConstant.EMPTY_STRING;
}