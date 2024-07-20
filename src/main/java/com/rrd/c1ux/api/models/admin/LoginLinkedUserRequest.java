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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="LoginLinkedUserRequest", description = "Request class to get the linked login user", type = "object")
public class LoginLinkedUserRequest {
	
	@Schema(name ="loginLinkedUserID", description = "A String representing the linked user loginID.", type = "String", example = "user1")
	private String loginLinkedUserID;

}
