/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		 Modified By			Jira#						Description
 * 	--------	-----------			-----------------------		--------------------------------
 *  2022.09.16   E Anderson      	CAP-35362                   Add account.
 *  10/11/22	A Boomker			CAP-35766					User stateful service needs to contain stateful info for session
 *  								So must be manually constructed. Removed Lombok references.
 *  10/25/22	A Boomker			CAP-36153					Add entry point
 *  11/23/22    S Ramachandran  	CAP-37370   				Determine if the BU stylesheet exists for user
 *  12/20/22    M Sakthi        	CAP-37794					Update User Profile Controller (User state service) with common permission flags needed across application
 *  01/10/23    M Sakthi        	CAP-38122					Update User Profile Controller (User state service) with common locale flags needed across application
 *  04/03/23    Satishkumar A   	CAP-39182                   User Profile (User state service) needs to add shared ID description and admin service flag in response
 *	04/26/23	A Boomker			CAP-40080					Added returnText
 *	07/12/23	C Codina			CAP-41589					Added boolean AllowPAB to use in UserProfileController
 * 	08/30/23	Krishna Natarajan	CAP-43371					Added translation for use of translation for 'profileMenu'
 * 	02/09/24 	Krishna Natarajan	CAP-47091					Added field Allow Catalog Search boolean
 * 	05/30/24	C Codina			CAP-49744					Added field for suggestedItemSetting
 * 	06/03/24	Satishkumar A   	CAP-49851					Added field for allowLinkedLogins flag
 *	06/19/24	Krishna Natarajan	CAP-50338					Added Allow Budget Allocations flag
 *  06/24/24    Rakesh K M          CAP-50368                   Added Allow Order On Behalf flag
 *  07/03/24	Krishna Natarajan	CAP-50754					Added Allow Order Wizard flag
 */
package com.rrd.c1ux.api.models.users;

import java.util.Map;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="UserStateProfile", description = "Response Class for User State Profile", type = "object")
public class UserStateProfile {

	@Schema(name ="firstName", description = "First name for the profile", type = "String", example="Shin")
    private String firstName;
	@Schema(name ="lastName", description = "Last name for the profile", type = "String", example="Nebres" )
 	private String lastName;
	@Schema(name ="phoneNumber", description = "Phone number for the profile", type = "String", example="9900990098" )
    private String phoneNumber;
	@Schema(name ="emailAddress", description = "Email address for the profile", type = "String", example="test123@rrd.com" )
    private String emailAddress;
	@Schema(name ="profileID", description = "ProfileID for the user", type = "String", example="user-rrd" )
    private String profileID;
	@Schema(name ="siteID", description = "SiteID for the user", type = "int", example="4366" )
    private int siteID; //CAP-35362
	@Schema(name ="siteLoginID", description = "Site Login ID for the user", type = "String", example="DEVTEST" )
    private String siteLoginID; //CAP-35362
	@Schema(name ="siteName", description = "Site Name for the user", type = "String", example="DEV_TEST" )
    private String siteName;
	@Schema(name ="timeoutMinutes", description = "Timeout minutes for the user", type = "int", example="99999" )
    private int timeoutMinutes; //CAP-35362
	@Schema(name ="buID", description = "Business Unit for the user", type = "int", example="7125" )
    private int buID; //CAP-35362
	@Schema(name ="userGroupName", description = "User Group Name for the user", type = "String", example="C1UX" )
    private String userGroupName; //CAP-35362
	@Schema(name ="loginID", description = "Login ID for the user", type = "String", example="USER-RRD" )
    private String loginID; //CAP-35362
	@Schema(name ="punchoutOperation", description = "Punchout Operation for the user", type = "String", example="C" )
    private String punchoutOperation = AtWinXSConstant.EMPTY_STRING; // CAP-35766
	@Schema(name ="entryPoint", description = "Entry Point for the user", type = "String",  example="Nebres")
    private String entryPoint = AtWinXSConstant.EMPTY_STRING; // CAP-36153
	@Schema(name ="buStyleSheetExists", description = "Business Unit StyleSheet Exists for the user", type = "boolean", example="false" )
    private boolean buStyleSheetExists = false; //CAP-37370

	@Schema(name ="showVendorItemNumber", description = "Flag to check whether the VendorItemNumber can show or not", type = "boolean",  example="false" )
    private boolean showVendorItemNumber = false; //CAP-37794
	@Schema(name ="showPricing", description = "Flag to check whether the Pricing can show or not", type = "boolean",  example="")
    private boolean showPricing = false; //CAP-37794
	@Schema(name ="showAvailability", description = "Flag to check whether the Availability can show or not", type = "boolean",  example="false")
    private boolean showAvailability = false; //CAP-37794

	@Schema(name ="defaultLocale", description = "Default Locale for the user", type = "String",  example="en_US")
    private String defaultLocale;//CAP-38122
	@Schema(name ="currencyLocale", description = "Currency Locale for the user", type = "String",  example="en_US" )
    private String currencyLocale;//CAP-38122
	@Schema(name ="defaultTimezone", description = "Default Timezone for the user", type = "String",  example="CDT")
    private String defaultTimezone;//CAP-38122


	@Schema(name ="adminService", description = "Flag to check whether the user has access to Admin Service or not", type = "boolean",  example="true")
    private boolean adminService = false;//CAP-39182
	@Schema(name ="sharedID", description = "Flag to check whether the userID is a sharedID or not", type = "boolean",  example="false" )
	private boolean sharedID;//CAP-39182
	@Schema(name ="sharedIDDesc", description = "Description for the sharedID", type = "String",  example="" )
	private String sharedIDDesc;//CAP-39182
	@Schema(name ="allowPAB", description = "Flag to check whether the user has a a profile number and has a shared ID", type = "boolean",  example="false")
	private boolean allowPAB; //CAP-41589

	@Schema(name="returnText", description="Text for option to log out of C1UX", type="String", example="Sign Out")
	private String returnText; // CAP-40080
	
	@Schema(name ="translation", description = "Translation messages for cart page retrieved from \'profile\' file. ", type = "object",  example="\"translation\": { \"myProfile\": \"logOut\"}")
	private Map<String, String> translation;//CAP-43371
	
	@Schema(name ="allowCatalogSearch", description = "Flag to check whether the user allowed for Catalog Search", type = "boolean",  example="false")
	private boolean allowCatalogSearch=false;//CAP-47091
	
	@Schema(name ="suggestedItemSetting", description = "A String value to check if the suggested item is set to At Line Level/Group at bottom of shopping cart", type = "String",  example="B")
	private String suggestedItemSetting;
	
	@Schema(name ="allowLinkedLogins", description = "A boolean which will be true if we allow linked logins or false if not.", type = "boolean",  example="false")
	private boolean allowLinkedLogins = false;//CAP-49851
	
	@Schema(name ="allowBudgetAllocations", description = "A boolean which will be true if allow budget allocations is turned on.", type = "boolean",  example="false")
	private boolean allowBudgetAllocations=false;
	
	@Schema(name = "allowOrderOnBehalf", description = "A boolean which will be true if allow Order On Behalf if allowed true default will be false", type="boolean", example="false")
	private boolean allowOrderOnBehalf = false;//CAP-50368
	
	@Schema(name = "allowOrderWizard", description = "A boolean which will be true if allow Order wizard is true, default will be false", type="boolean", example="false")	
	private boolean allowOrderWizard = false;//CAP-50754
	
	public boolean isAllowOrderWizard() {
		return allowOrderWizard;
	}
	public void setAllowOrderWizard(boolean allowOrderWizard) {
		this.allowOrderWizard = allowOrderWizard;
	}
	public boolean isAllowOrderOnBehalf() {
		return allowOrderOnBehalf;
	}
	public void setAllowOrderOnBehalf(boolean allowOrderOnBehalf) {
		this.allowOrderOnBehalf = allowOrderOnBehalf;
	}

	public boolean isAllowBudgetAllocations() {
		return allowBudgetAllocations;
	}
	public void setAllowBudgetAllocations(boolean allowBudgetAllocations) {
		this.allowBudgetAllocations = allowBudgetAllocations;
	}
	public String getSuggestedItemSetting() {
		return suggestedItemSetting;
	}
	public void setSuggestedItemSetting(String suggestedItemSetting) {
		this.suggestedItemSetting = suggestedItemSetting;
	}
	public boolean isAllowCatalogSearch() {
		return allowCatalogSearch;
	}
	public void setAllowCatalogSearch(boolean allowCatalogSearch) {
		this.allowCatalogSearch = allowCatalogSearch;
	}
	public Map<String, String> getTranslation() {
		return translation;
	}
	public void setTranslation(Map<String, String> translation) {
		this.translation = translation;
	}

	public boolean isAdminService() {
		return adminService;
	}
	public void setAdminService(boolean adminService) {
		this.adminService = adminService;
	}

	public boolean isSharedID() {
		return sharedID;
	}
	public void setSharedID(boolean sharedID) {
		this.sharedID = sharedID;
	}
	public String getSharedIDDesc() {
		return sharedIDDesc;
	}
	public void setSharedIDDesc(String sharedIDDesc) {
		this.sharedIDDesc = sharedIDDesc;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getProfileID() {
		return profileID;
	}
	public void setProfileID(String profileID) {
		this.profileID = profileID;
	}
	public int getSiteID() {
		return siteID;
	}
	public void setSiteID(int siteID) {
		this.siteID = siteID;
	}
	public String getSiteLoginID() {
		return siteLoginID;
	}
	public void setSiteLoginID(String siteLoginID) {
		this.siteLoginID = siteLoginID;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public int getTimeoutMinutes() {
		return timeoutMinutes;
	}
	public void setTimeoutMinutes(int timeoutMinutes) {
		this.timeoutMinutes = timeoutMinutes;
	}
	public int getBuID() {
		return buID;
	}
	public void setBuID(int buID) {
		this.buID = buID;
	}
	public String getUserGroupName() {
		return userGroupName;
	}
	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
	}
	public String getLoginID() {
		return loginID;
	}
	public void setLoginID(String loginID) {
		this.loginID = loginID;
	}
	public String getPunchoutOperation() {
		return punchoutOperation;
	}
	public void setPunchoutOperation(String punchoutOperation) {
		this.punchoutOperation = punchoutOperation;
	}
	// CAP-36153
	public String getEntryPoint() {
		return entryPoint;
	}
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}
	//CAP-37370
	public boolean getBuStyleSheetExists() {
		return buStyleSheetExists;
	}
	public void setBuStyleSheetExists(boolean buStyleSheetExists) {
		this.buStyleSheetExists = buStyleSheetExists;
	}
	//CAP-37794
	public boolean isShowVendorItemNumber() {
		return showVendorItemNumber;
	}
	public void setShowVendorItemNumber(boolean showVendorItemNumber) {
		this.showVendorItemNumber = showVendorItemNumber;
	}
	public boolean isShowPricing() {
		return showPricing;
	}
	public void setShowPricing(boolean showPricing) {
		this.showPricing = showPricing;
	}
	public boolean isShowAvailability() {
		return showAvailability;
	}
	public void setShowAvailability(boolean showAvailability) {
		this.showAvailability = showAvailability;
	}

	//CAP-38122
	public String getDefaultLocale() {
		return defaultLocale;
	}
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
	public String getDefaultTimezone() {
		return defaultTimezone;
	}
	public void setDefaultTimezone(String defaultTimezone) {
		this.defaultTimezone = defaultTimezone;
	}
	public String getCurrencyLocale() {
		return currencyLocale;
	}
	public void setCurrencyLocale(String currencyLocale) {
		this.currencyLocale = currencyLocale;
	}
	// CAP-40080
	public String getReturnText() {
		return returnText;
	}
	public void setReturnText(String returnText) {
		this.returnText = returnText;
	}
	//CAP-41589
	public void setAllowPAB(boolean newAllowPab){
		this.allowPAB = newAllowPab;
	}
	public boolean isAllowPab(){
		return allowPAB;
	}
	//CAP-49851
	public boolean isAllowLinkedLogins() {
		return allowLinkedLogins;
	}
	public void setAllowLinkedLogins(boolean allowLinkedLogins) {
		this.allowLinkedLogins = allowLinkedLogins;
	}
}
