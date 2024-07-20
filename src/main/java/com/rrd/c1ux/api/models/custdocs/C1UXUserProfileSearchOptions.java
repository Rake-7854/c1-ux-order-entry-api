/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/06/23				A Boomker				CAP-44463					Initial Version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;

import com.rrd.custompoint.customdocs.ui.SimpleSearchOption;
import com.rrd.custompoint.orderentry.customdocs.ProfileOption;
import com.rrd.custompoint.orderentry.customdocs.ProfileSelector;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXUserProfileSearchOptions", description = "Bean in a page response used to pass info to show the user profile search section if it should be shown.", type = "object")
public class C1UXUserProfileSearchOptions {

	@Schema(name = "profileIDLabel", description = "Label for profile ID for the User profile type", type = "String", example = "Profile ID")
	protected String profileIDLabel = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "currentProfileNumber", description = "Number of the profile currently populating defaults so it can be flagged in the profile list options.", type = "number")
	protected int currentProfileNumber = -1;
	@Schema(name = "searchTerms", description = "List of search term dropdown options. Each option contains string term and string displayText", type = "array")
	protected List<SimpleSearchOption> searchTerms;

	@Schema(name = "profileOptions", description = "List of profile dropdown options. Each option contains int profile number and string historyDisplayText", type = "array")
	protected List<ProfileOption> profileOptions;

	public C1UXUserProfileSearchOptions(ProfileSelector selector) {
		this.profileOptions = selector.getProfileOptions();
		this.currentProfileNumber = selector.getSelectedProfile();
		this.searchTerms = selector.getSimpleSearchTerms();
		this.profileIDLabel = selector.getProfileIDLabel();
	}

	public String getProfileIDLabel() {
		return profileIDLabel;
	}

	public void setProfileIDLabel(String profileIDLabel) {
		this.profileIDLabel = profileIDLabel;
	}

	public int getCurrentProfileNumber() {
		return currentProfileNumber;
	}

	public void setCurrentProfileNumber(int currentProfileNumber) {
		this.currentProfileNumber = currentProfileNumber;
	}

	public List<SimpleSearchOption> getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(List<SimpleSearchOption> searchTerms) {
		this.searchTerms = searchTerms;
	}

	public List<ProfileOption> getProfileOptions() {
		return profileOptions;
	}

	public void setProfileOptions(List<ProfileOption> profileOptions) {
		this.profileOptions = profileOptions;
	}
}
