/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	06/03/24				A Boomker				CAP-46501					Initial Version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;

import com.rrd.custompoint.orderentry.customdocs.ProfileOption;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="C1UXAlternateProfileOptions", description = "Bean in a page response used to pass info to show the alternate profile section if it should be shown.", type = "object")
public class C1UXAlternateProfileOptions {

	@Schema(name = "label", description = "Label for alternate profile type", type = "String", example = "Company")
	protected String label = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "definitionID", description = "Profile Definition ID number for this alternate profile type", type = "number", example = "2352")
	protected int definitionID = AtWinXSConstant.INVALID_ID;
	@Schema(name = "currentProfileNumber", description = "Number of the profile currently populating defaults so it can be flagged in the profile list options.", type = "number")
	protected int currentProfileNumber = AtWinXSConstant.INVALID_ID;
	@Schema(name = "currentProfileID", description = "String ID of the profile currently populating defaults so it can be displayed in the profile list options combobox.", type = "string")
	protected String currentProfileID = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "profileOptions", description = "List of profile type dropdown options. Each option contains int profile number and string profileDisplayText", type = "array")
	protected List<ProfileOption> profileOptions = null;
}
