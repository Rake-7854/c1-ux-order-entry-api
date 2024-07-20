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
 * 	07/25/23	A Boomker			CAP-42223		Initial version
 * 	07/26/23	A Boomker			CAP-42225		Added redirect routing to be used when the result of this call should indicate routing
 * 	03/29/24	A Boomker			CAP-46493/CAP-46494	fixes for navigation
 */
package com.rrd.c1ux.api.models.custdocs;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocBaseResponse", description = "Response Class for cust docs when no additional fields need to be passed", type = "object")
public class C1UXCustDocBaseResponse extends BaseResponse {
	// Routing if required
	@Schema(name = "redirectRouting", description = "Angular routing to redirect to when exiting the user interface component.", type = "String", example = "")
	private String redirectRouting = "";
	@Schema(name = "hardStopFailure", description = "Flag indicating cust doc cannot proceed. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean hardStopFailure = false;

}
