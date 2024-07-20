/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/28/24	Satishkumar A		CAP-49610				C1UX API - Create API to login as a linked login ID/user
 */
package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name ="LoginLinkedUserResponse", description = "Response class linked login user", type = "object")
public class LoginLinkedUserResponse extends BaseResponse {
	
	@Schema(name ="redirectURL", description = "C1UX to Linked User Login Redirect URL", type = "string", example="https://samltest.sso.rrd.com/idp/startSSO.ping")
	private String redirectURL;

}
