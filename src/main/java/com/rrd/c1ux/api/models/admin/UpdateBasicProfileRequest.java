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
 * 	04/10/23	A Boomker			CAP-37890		Initial version
 */
package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="UpdateBasicProfileRequest", description = "Request Class for self-admin updating the user's Basic Profile", type = "object")
public class UpdateBasicProfileRequest {
	@Schema(name ="firstName", description = "Profile First Name", type = "String", example="Joseph")
	@Size(min=0, max=25)
	public String firstName = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="lastName", description = "Profile Last Name", type = "String", example="Biden")
	@Size(min=0, max=25)
	public String lastName = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="phone", description = "Profile Phone Number", type = "String", example="1-800-588-2300")
	@Size(min=0, max=24)
	public String phone = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="email", description = "Profile Email Address", type = "String", example="JKRowling@warnerbros.com")
	@Size(min=0, max=128)
	public String email = AtWinXSConstant.EMPTY_STRING;

}
