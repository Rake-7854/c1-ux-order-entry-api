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
 * 	03/27/23	A Boomker			CAP-37891		Initial version
 */
package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="UpdatePasswordRequest", description = "Request Class for self-admin updating the password", type = "object")
public class UpdatePasswordRequest {
	@Schema(name ="oldPassword", description = "Current Password", type = "String", example="S0meth!ng$ecur3")
	@Size(min=0, max=32)
	public String oldPassword;
	@Schema(name ="newPassword", description = "New Password Desired", type = "String", example="B3tt3rbEM0re!")
	@Size(min=0, max=32)
	public String newPassword;
	@Schema(name ="confirmPassword", description = "Confirmation of New Password Desired", type = "String", example="B3tt3rbEM0re!")
	@Size(min=0, max=32)
	public String confirmPassword;

}
