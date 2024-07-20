/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.orderentry.customdocs.ProfileOption;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocProfileSearchResponse", description = "Response Class for cust docs profile search containing the list of valid profiles and the selected number", type = "object")
public class C1UXCustDocProfileSearchResponse extends BaseResponse {

	@Schema(name = "currentProfileNumber", description = "Number of the profile currently populating defaults so it can be flagged in the profile list options.", type = "number")
	protected int currentProfileNumber = -1;
	@Schema(name = "profileOptions", description = "List of profile dropdown options. Each option contains int profile number and string historyDisplayText", type = "array")
	protected List<ProfileOption> profileOptions;

}
