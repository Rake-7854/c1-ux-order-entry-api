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
 */

package com.rrd.c1ux.api.models.settingsandprefs;

public class CatalogUtilityNavigationInfo {

	 SettingsandPrefs SettingsandPrefsObject;

	 // Getter Methods 

	 public SettingsandPrefs getSettingsandPrefs() {
	  return SettingsandPrefsObject;
	 }

	 // Setter Methods 

	 public void setSettingsandPrefs(SettingsandPrefs settingsandPrefsObject) {
	  this.SettingsandPrefsObject = settingsandPrefsObject;
	 }
	
}
