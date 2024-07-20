/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By          DTS#            	Description
 *	--------    -----------         ----------      	------------------------------------
 *  03/21/23	A Boomker			CAP-39369			Add handling for show recent orders flag
 *  03/23/23    E Anderson          CAP-38724           Add translationOrderHeader Map
 */
package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderSearchNavigationResponse", description = "Response Class for order search - filter options", type = "object")
//CAP-38736
public class COOrderSearchNavigationResponse extends BaseResponse{

	@Schema(name ="orderSearchTitleLabel", description = "Label for Order Search page - filter options - Title", type = "string", example="Order Search")
	@Size(min=0)
	private String orderSearchTitleLabel ;
	@Schema(name ="orderSearchLabel", description = "Label for Order Search page - filter options - Search", type = "string", example="Search")
	@Size(min=0)
	private String orderSearchLabel  ;

	@Schema(name ="orderSearchForLabel", description = "Label for Order Search page - filter options - For", type = "string", example="For")
	@Size(min=0)
	private String orderSearchForLabel ;

	@Schema(name ="orderDateRangeLabel", description = "Label for Order Search page - filter options - Date Range", type = "string", example="Date Range")
	@Size(min=0)
	private String orderDateRangeLabel ;
	@Schema(name ="orderSearchScopeLabel", description = "Label for Order Search page - filter options - Scope", type = "string", example="Scope")
	@Size(min=0)
	private String orderSearchScopeLabel ;

	@Schema(name ="orderDateRangeFrom", description = "Earliest date to include in search results in format MM/DD/YYYY. The default date should be 30 days prior to the current date.", type = "string", example="01/04/2023")
	@Size(min=0,max=10)
	private String orderDateRangeFrom;
	@Schema(name ="orderDateRangeTo", description = "Latest date to include in search results in format MM/DD/YYYY. The default date should be the current date.", type = "string", example="02/03/2023")
	@Size(min=0,max=10)
	private String orderDateRangeTo;
	@Schema(name ="maxDateRange", description = "It is the maximum number of days that can be chosen between the from date and the to date. So the number of days between the from date and the to date can be (maxDateRange) number of days.", type = "string", example="90")
	@Size(min=1,max=2)
	private int maxDateRange;
	@Schema(name ="quickSearchPreference", description = "This is the defaulted search term that should be selected. Quick Search Preference - Value from PUNCHOUT settings : User group level -> Manage order admin -> order status -> Quick Search Preference.", type = "string", example="CriteriaPONumber" , allowableValues = { "CriteriaPONumber", "CriteriaSalesRef", "CriteriaOrderNumber"})
	@Size(max=20)
	private String quickSearchPreference="";

	@Schema(name ="COOrderSearchCriteria", description = "List of search criteria objects with values", type = "object")
	private List<COOrderSearchCriteria> orderSearchCriteria;

	@Schema(name ="COOrderSearchScope", description = "List of search scope objects with values", type = "object")
	private List<COOrderSearchScope> orderSearchScope;

	@Schema(name ="orderSearchMsgCriteria", description = "Error message for criteria error.", type = "string", example="Search criteria needs to be specified. Either add the missing criteria or remove the criteria.")
	@Size(min=0)
	private String orderSearchMsgCriteria  ;
	@Schema(name ="orderSearchMsgDateMissing", description = "Error message for date missing.", type = "string", example="A date field needs to be specified.")
	@Size(min=0)
	private String orderSearchMsgDateMissing  ;
	@Schema(name ="orderSearchMsgDateInvalid", description = "Error message for invalid date.", type = "string", example="The To date cannot be before the From date.")
	@Size(min=0)
	private String orderSearchMsgDateInvalid  ;
	@Schema(name ="orderSearchMsgDateMax", description = "Error message for max date.", type = "string", example="The date range is greater than the maximum number of days allowed.")
	@Size(min=0)
	private String orderSearchMsgDateMax  ;
	//CAP-38709
	@Schema(name ="translation", description = "Messages from \"orderSearchNav\" translation file will load here.", type = "string",  example="\"translation\": { \"OrigDateLbl\": \"Original Date\"}")
	private Map<String, String> translation;
	//CAP-39369
	@Schema(name ="showRecentOrders", description = "Flag for whether the User Group has show recent orders turned on", type = "boolean")
	private boolean showRecentOrders = false;
	//CAP-38724
	@Schema(name ="translationOrderHeader", description = "Translation for \"orderHeader\" ViewName.", type = "string",  example="\"translationOrderHeader\": { \"OrigDateLbl\": \"Original Date\"}")
	private Map<String, String> translationOrderHeader;
}
