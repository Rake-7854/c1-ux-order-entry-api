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
 * 	08/24/23	A Boomker			CAP-43223		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocUploadedFileDetailsRequest", description = "Request Class for cust docs UI page when the detail is needed for an already uploaded file within the UI.", type = "object")
public class C1UXCustDocUploadedFileDetailsRequest {
	@Schema(name = "hdnVarValue", description = "Value on CP request indicating the value of a variable triggering the specific request action.", type = "String")
	private String hdnVarValue = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnVarValue" name="hdnVarValue" value=""> // used for dynamic list dependent variables
	@Schema(name = "hdnVar", description = "Value on CP request indicating the number of a variable triggering the specific request action.", type = "String")
	private String hdnVar = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnVar" name="hdnVar" value=""> // used for dynamic list dependent variables - dependent var name
}
