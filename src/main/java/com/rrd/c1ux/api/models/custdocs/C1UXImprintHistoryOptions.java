/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/28/23				A Boomker				CAP-43022					Initial Version
 *	07/09/24				A Boomker				CAP-46538					Adding handling for search results
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;

import com.rrd.custompoint.customdocs.ui.SimpleSearchOption;
import com.rrd.custompoint.orderentry.customdocs.HistoryOption;
import com.rrd.custompoint.orderentry.customdocs.ImprintHistorySelector;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXImprintHistoryOptions", description = "Bean in a page response used to pass info to show the imprint history section if it should be shown.", type = "object")
public class C1UXImprintHistoryOptions {
	@Schema(name = "itemSearchOption", description = "Indicator of the limitation for the history list.", type = "String", example = AtWinXSConstant.EMPTY_STRING,
			allowableValues = {	"N", // ItemSearchOption CUR_ITEM_ONLY
					"Y", // ItemSearchOption ASSC_TO_SAME_UI
					"P", // ItemSearchOption ASSC_TO_SAME_PROJ
					"A", // ItemSearchOption ACROSS_PROJECTS
					""}) // ItemSearchOption NONE
	protected String itemSearchOption = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "profileSearchOption", description = "Indicator of the limitation for the history list.", type = "String", example = AtWinXSConstant.EMPTY_STRING,
			allowableValues = {	"N", // ProfileSearchOption MY_PROFILE_ONLY
					"Y", // ProfileSearchOption SPECIFIC_PROFILES
					"B", // ProfileSearchOption ALL_PROFILES_IN_BU
					""}) // ProfileSearchOption NONE
	protected String profileSearchOption = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "thisItemOnly", description = "Value indicating the displayed values are limited to this item only. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean thisItemOnly;
	@Schema(name = "myOrdersOnly", description = "Value indicating the displayed values are limited to the user's current profile only. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean myOrdersOnly;
	@Schema(name = "currentOrderID", description = "Number of the current order so it can be flagged in the imprint history options.", type = "number")
	protected int currentOrderID = -1;
	@Schema(name = "numOrders", description = "Number of imprint orders in the imprint history options.", type = "number")
	protected int numOrders;

	@Schema(name = "historyOptions", description = "List of imprint history dropdown options. Each option contains int custDocOrderLineNum and string historyDisplayText", type = "array")
	protected List<HistoryOption> historyOptions;

	@Schema(name = "allowEnhancedBasicHistorySearch", description = "Value indicating the imprint history section shows enhanced instead of basic. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean allowEnhancedBasicHistorySearch = false;

	@Schema(name = "historyOptions", description = "List of enhanced basic imprint history search options. Each option contains a variable search term.", type = "array")
	protected List<SimpleSearchOption> enhancedBasicSearchOptions = null;

	@Schema(name = "allowAdvancedSearch", description = "Value indicating the user group permissions allow advanced search. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean allowAdvancedSearch = false;

	@Schema(name = "showThisItemOnlyCheckbox", description = "Value indicating the basic imprint history search would allow the user to search across items. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showThisItemOnlyCheckbox = false;

	@Schema(name = "showMyOrdersOnlyCheckbox", description = "Value indicating the basic imprint history search would allow the user to search across profiles. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showMyOrdersOnlyCheckbox = false;

	public String getItemSearchOption() {
		return itemSearchOption;
	}

	public void setItemSearchOption(String itemSearchOption) {
		this.itemSearchOption = itemSearchOption;
	}

	public String getProfileSearchOption() {
		return profileSearchOption;
	}

	public void setProfileSearchOption(String profileSearchOption) {
		this.profileSearchOption = profileSearchOption;
	}

	public boolean isThisItemOnly() {
		return thisItemOnly;
	}

	public void setThisItemOnly(boolean thisItemOnly) {
		this.thisItemOnly = thisItemOnly;
	}

	public boolean isMyOrdersOnly() {
		return myOrdersOnly;
	}

	public void setMyOrdersOnly(boolean myOrdersOnly) {
		this.myOrdersOnly = myOrdersOnly;
	}

	public int getCurrentOrderID() {
		return currentOrderID;
	}

	public void setCurrentOrderID(int currentOrderID) {
		this.currentOrderID = currentOrderID;
	}

	public int getNumOrders() {
		return numOrders;
	}

	public void setNumOrders(int numOrders) {
		this.numOrders = numOrders;
	}

	public List<HistoryOption> getHistoryOptions() {
		return historyOptions;
	}

	public void setHistoryOptions(List<HistoryOption> historyOptions) {
		this.historyOptions = historyOptions;
	}

	public C1UXImprintHistoryOptions(ImprintHistorySelector selector) {
		this.historyOptions = selector.getHistoryOptions();
		this.currentOrderID = selector.getCurrentOrderID();
		this.itemSearchOption = selector.getItemSearchOption();
		this.profileSearchOption = selector.getProfileSearchOption();
		this.myOrdersOnly = selector.isMyOrdersOnly();
		this.thisItemOnly = selector.isThisItemOnly();
		this.numOrders = selector.getNumOrders();
	}

	public boolean isAllowEnhancedBasicHistorySearch() {
		return allowEnhancedBasicHistorySearch;
	}

	public void setAllowEnhancedBasicHistorySearch(boolean allowEnhancedBasicHistorySearch) {
		this.allowEnhancedBasicHistorySearch = allowEnhancedBasicHistorySearch;
	}

	public List<SimpleSearchOption> getEnhancedBasicSearchOptions() {
		return enhancedBasicSearchOptions;
	}

	public void setEnhancedBasicSearchOptions(List<SimpleSearchOption> enhancedBasicSearchOptions) {
		this.enhancedBasicSearchOptions = enhancedBasicSearchOptions;
	}

	public boolean isAllowAdvancedSearch() {
		return allowAdvancedSearch;
	}

	public void setAllowAdvancedSearch(boolean allowAdvancedSearch) {
		this.allowAdvancedSearch = allowAdvancedSearch;
	}

	public boolean isShowThisItemOnlyCheckbox() {
		return showThisItemOnlyCheckbox;
	}

	public void setShowThisItemOnlyCheckbox(boolean showThisItemOnlyCheckbox) {
		this.showThisItemOnlyCheckbox = showThisItemOnlyCheckbox;
	}

	public boolean isShowMyOrdersOnlyCheckbox() {
		return showMyOrdersOnlyCheckbox;
	}

	public void setShowMyOrdersOnlyCheckbox(boolean showMyOrdersOnlyCheckbox) {
		this.showMyOrdersOnlyCheckbox = showMyOrdersOnlyCheckbox;
	}
}
