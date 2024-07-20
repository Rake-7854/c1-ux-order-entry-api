/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/20/23				A Boomker				CAP-42295					Initial Version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXCustDocGroupGridBean", description = "Response Class equivalent to GridGroupImpl in CP - info to display a single UI grid group", type = "object")
public class C1UXCustDocGroupGridBean extends C1UXCustDocGroupBean {

	// CAP-4316
	@Schema(name ="groupGridRowLevelDisplayControls", description = "Map of rows (keyed by row number) and the display controls that will affect that row's display", type = "array")
	protected Map<Integer, C1UXDisplayControls> groupGridRowLevelDisplayControls = new HashMap<>();

	// CAP-4864
	@Schema(name ="groupGridColLevelDisplayControls", description = "Map of columns (keyed by column number) and the display controls that will affect that column's display", type = "array")
	protected Map<Integer, C1UXDisplayControls> groupGridColLevelDisplayControls = new HashMap<>();

	public Map<Integer, C1UXDisplayControls> getGroupGridRowLevelDisplayControls() {
		return groupGridRowLevelDisplayControls;
	}

	public void setGroupGridRowLevelDisplayControls(Map<Integer, C1UXDisplayControls> groupGridRowDisplayControls) {
		this.groupGridRowLevelDisplayControls = groupGridRowDisplayControls;
	}

	public Map<Integer, C1UXDisplayControls> getGroupGridColLevelDisplayControls() {
		return groupGridColLevelDisplayControls;
	}

	public void setGroupGridColLevelDisplayControls(Map<Integer, C1UXDisplayControls> groupGridColDisplayControls) {
		this.groupGridColLevelDisplayControls = groupGridColDisplayControls;
	}

}
