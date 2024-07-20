/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		-------------------------------------------------------------------------------
 *	02/22/24	S Ramachandran		CAP-47198				Initial Version. Added  basic user profile response class for save SelfReg user 
 *	03/07/24	Krishna Natarajan	CAP-47671				Added a new variable loginUrl
 */

package com.rrd.c1ux.api.models.selfreg;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "SelfRegistrationSaveResponse", description = "Response Class for saving the User's Profile in self-Reg", type = "object")
public class SelfRegistrationSaveResponse extends BaseResponse {
	@Schema(name = "loginUrl", description = "Login URL", type = "String", example = "")
	public String loginUrl = AtWinXSConstant.EMPTY_STRING;
}