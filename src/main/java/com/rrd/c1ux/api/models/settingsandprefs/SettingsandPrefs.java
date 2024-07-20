/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            	Description
 *	--------    -----------         ----------      	-----------------------------------------------------------
 *  10/10/22	Krishna Natarajan	CAP-36438/36448 	Created class for the CAP-36438/36448
 *  10/16/22    E Anderson          CAP-36438           Data type changes for defaultSortBylabel.
 *  02/16/23    Sakthi              CAP-38420           Schema addition,New variable addition and renamed variables 
 *  11/29/23	Krishna Natarajan	CAP-45483			Created new variables for VI Module and cust item number
 *  12/07/23	N Caceres			CAP-45601			Created new fields for Order Search Details params	
 */
package com.rrd.c1ux.api.models.settingsandprefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; 

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SettingsandPrefs", description = "Response class for sorting and pagination options", type = "object")
public class SettingsandPrefs {
	
	@Schema(name ="sortOptions", description = "To sort the Items/Orders based on the sort option field", type = "array", example=" [{\"label\": \"Vendor Item number\",\"value\": \"vendorItemNumber\"}, {\"label\": \"Sales Ref\",\"value\": \"salesRefNo\"}]", allowableValues = {"Vendor Item number","Item Number","Item Description","Sales Ref","PO #","Order Date","Status"})
	List<Map<String, String>> sortOptions = new ArrayList<Map<String, String>>();
	@Schema(name ="defaultSortBylabel", description = "To display the selectable Items/Orders per page", type = "array", example=" [{\"label\": \"10\",\"value\": 10}, {\"label\": \"20\",\"value\": 20}]", allowableValues = {"10","20","40","24","48","96"})
	List<Map<String, Object>> defaultSortBylabel = new ArrayList<Map<String, Object>>();
	//CAP-38420 Variable addition
	@Schema(name ="showNumberOptions", description = "To display the selectable Items/Orders per page", type = "array", example=" [{\"label\": \"10\",\"value\": 10}, {\"label\": \"20\",\"value\": 20}]", allowableValues = {"10","20","40","24","48","96"})
	List<Map<String, Object>> showNumberOptions = new ArrayList<Map<String, Object>>();
	//CAP-38420  Below variables are renamed first letter as lower case.
	@Schema(name ="expressShopping", description = "Express Shopping options", type = "string", example="Y",  allowableValues = {" ","Y","N"})
	@Size(min=0, max=1)
	String expressShopping="";
	
	@Schema(name ="promptForSavingUIPages", description = "Option to save pages during navigation", type = "string", example="Y", allowableValues = {" ","Y","N"})
	@Size(min=0, max=1)
	String promptForSavingUIPages="";
	
	@Schema(name ="defaultDisplayAs", description = "To display the Items in the format of grid/line view", type = "string", example="T",allowableValues = {" ","T","I"})
	@Size(min=0, max=1)
	String defaultDisplayAs="";
	
	@Schema(name ="defaultSortBy", description = "The default sorting of Items/Orders", type = "string", example="itemNumber")
	String defaultSortBy="";
	
	@Schema(name ="defaultNoOfItems", description = "Default No of Items/Orders to be displayed", type = "string", example="24")
	Integer defaultNoOfItems= 24;
	
	@Schema(name ="module", description = "The module to be redirected to CP", type = "string", example="VI")
	String module="";
	
	@Schema(name ="custItemNum", description = "The Items Number to be searched and land on details page", type = "string", example="1018")
	String custItemNum="";
	
	@Schema(name ="catalogRouterLink", description = "The Catalog Router Link with the catalog ID to get redirected", type = "string", example="shopbycatalog/37527")
	String catalogRouterLink="";
	
	@Schema(name ="salesRefNum", description = "The sales reference number to be searched and land on details page", type = "string", example="1018")
	String salesRefNum="";
	
	@Schema(name ="fromDate", description = "Earliest date to include in search results. Date should be in MM/DD/YYYY format only", type = "string", example="12/07/2023")
	String fromDate="";
	
	@Schema(name ="toDate", description = "Latest date to include in search results. Date should be in MM/DD/YYYY format only", type = "string", example="12/07/2023")
	String toDate="";
	
	@Schema(name ="scope", description = "Limits the orders returned to a specific visibility related to the user", type = "string", example="allorders", allowableValues = {"allorders","myorders"})
	String scope="";

}