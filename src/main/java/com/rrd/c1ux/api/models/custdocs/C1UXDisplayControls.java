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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class C1UXDisplayControls {
	private Map<Integer, List<C1UXDisplayControl>> displayControlsMap = new HashMap<>();
	private String hideActionCode;
	private String multipleVarActionCode;
	private String displayVarLevelCode;
	// CAP-4801 - indicate that this display control triggered the current hide of the level it applies to
	// if false, it means not currently hidden or hidden by another level
	// if true, it means currently hidden and hidden by this display control
	private boolean thisDisplayControlTriggeredHide = false;
	private boolean displayCriteriaMetAsOfLoad = false;

	public Map<Integer, List<C1UXDisplayControl>> getDisplayControlsMap() {
		return displayControlsMap;
	}
	public void setDisplayControlsMap(Map<Integer, List<C1UXDisplayControl>> displayControls) {
		this.displayControlsMap = displayControls;
	}
	public String getHideActionCode() {
		return hideActionCode;
	}
	public void setHideActionCode(String hideAction) {
		this.hideActionCode = hideAction;
	}
	public String getMultipleVarActionCode() {
		return multipleVarActionCode;
	}
	public void setMultipleVarActionCode(String multipleVarAction) {
		this.multipleVarActionCode = multipleVarAction;
	}
	public String getDisplayVarLevelCode() {
		return displayVarLevelCode;
	}
	public void setDisplayVarLevelCode(String displayVarLevel) {
		this.displayVarLevelCode = displayVarLevel;
	}
	public boolean isThisDisplayControlTriggeredHide() {
		return thisDisplayControlTriggeredHide;
	}
	public void setThisDisplayControlTriggeredHide(boolean thisDisplayControlTriggeredHide) {
		this.thisDisplayControlTriggeredHide = thisDisplayControlTriggeredHide;
	}
	public boolean isDisplayCriteriaMetAsOfLoad() {
		return displayCriteriaMetAsOfLoad;
	}
	public void setDisplayCriteriaMetAsOfLoad(boolean displayCriteriaMetAsOfLoad) {
		this.displayCriteriaMetAsOfLoad = displayCriteriaMetAsOfLoad;
	}

}
