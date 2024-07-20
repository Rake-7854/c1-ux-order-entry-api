/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/19/23				A Boomker				CAP-42295					Initial Version
 */
package com.rrd.c1ux.api.models.custdocs;

public class C1UXDisplayControl {
	private String displayVariableInputTypeCode;
	private String displayVariableTextValue;
	private String displayVariableListValue;
	private boolean forceEquality;
	private String value; // this is the textValue for the option
	private int intValue; // this is the value saved in the database

	public String getDisplayVariableInputTypeCode() {
		return displayVariableInputTypeCode;
	}
	public void setDisplayVariableInputTypeCode(String displayVariableInputTypeCode) {
		this.displayVariableInputTypeCode = displayVariableInputTypeCode;
	}
	public String getDisplayVariableTextValue() {
		return displayVariableTextValue;
	}
	public void setDisplayVariableTextValue(String displayVariableTextValue) {
		this.displayVariableTextValue = displayVariableTextValue;
	}
	public String getDisplayVariableListValue() {
		return displayVariableListValue;
	}
	public void setDisplayVariableListValue(String displayVariableListValue) {
		this.displayVariableListValue = displayVariableListValue;
	}
	public boolean isForceEquality() {
		return forceEquality;
	}
	public void setForceEquality(boolean forceEquality) {
		this.forceEquality = forceEquality;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getIntValue() {
		return intValue;
	}
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
}
