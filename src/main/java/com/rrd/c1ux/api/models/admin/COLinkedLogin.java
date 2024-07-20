/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/27/24	L De Leon			CAP-49609				Initial Version
 */
package com.rrd.c1ux.api.models.admin;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "COLinkedLogin", description = "Class for retrieving the information of the logged in user or linked user", type = "object")
public class COLinkedLogin {

	@Schema(name ="businessUnitName", description = "A String holding the business unit name the logged in user or linked user is tied to.", type = "string", example = "Business Unit")
	private String businessUnitName = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="userGroup", description = "A String holding the user group the logged in user or linked user is tied to.", type = "string", example = "User Group")
	private String userGroup = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="userID", description = "A String holding the user ID of the logged in user or linked user.", type = "string", example = "USER")
	private String userID = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="profileID", description = "A String holding the profile ID of the logged in user or linked user.", type = "string", example = "USER")
	private String profileID = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="firstName", description = "A String holding the first name of the logged in user or linked user.", type = "string", example = "John")
	private String firstName = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="lastName", description = "A String holding the last name of the logged in user or linked user.", type = "string", example = "Doe")
	private String lastName = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="email", description = "A String holding the email of the logged in user or linked user.", type = "string", example = "john.doe@rrd.com")
	private String email = AtWinXSConstant.EMPTY_STRING;
}