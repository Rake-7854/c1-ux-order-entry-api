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
 *  11/22/22	Sakthi M        	CAP-36560 	       Created class for the CAP-36560 
 */

package com.rrd.c1ux.api.models.orders.ordersearch;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderSearchUtilityNavigationInfo {
	public List<Map<String, String>> sortByOptions = new ArrayList<Map<String, String>>();
	public List<Map<String, Object>> noOfItemsPerPage = new ArrayList<Map<String, Object>>();
	public String defaultSortByOptions="";
	public Integer defaultNoOfItemsPerPage;
	
	public List<Map<String, String>> getSortByOptions() {
		return sortByOptions;
	}
	public void setSortByOptions(List<Map<String, String>> sortByOptions) {
		this.sortByOptions = sortByOptions;
	}
	public List<Map<String, Object>> getNoOfItemsPerPage() {
		return noOfItemsPerPage;
	}
	public void setNoOfItemsPerPage(List<Map<String, Object>> noOfItemsPerPage) {
		this.noOfItemsPerPage = noOfItemsPerPage;
	}
	public String getDefaultSortByOptions() {
		return defaultSortByOptions;
	}
	public void setDefaultSortByOptions(String defaultSortByOptions) {
		this.defaultSortByOptions = defaultSortByOptions;
	}
	public Integer getDefaultNoOfItemsPerPage() {
		return defaultNoOfItemsPerPage;
	}
	public void setDefaultNoOfItemsPerPage(Integer defaultNoOfItemsPerPage) {
		this.defaultNoOfItemsPerPage = defaultNoOfItemsPerPage;
	}
	
}