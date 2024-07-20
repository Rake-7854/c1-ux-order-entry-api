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

import java.util.ArrayList;
import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "LinkedLoginResponse", description = "Response Class for retrieving the user information and linked logins", type = "object")
public class LinkedLoginResponse extends BaseResponse {

	@Schema(name ="loggedInUser", description = "An Object holding the information of the logged in user.", type = "object")
	private COLinkedLogin loggedInUser;

	@Schema(name ="selfRegURL", description = "A String holding self registration URL.", type = "string", example = "rrd.com")
	private String selfRegURL = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="linkedLogins", description = "A Collection of linked login users", type = "array", example = "[]")
	private List<COLinkedLogin> linkedLogins = new ArrayList<>();
}