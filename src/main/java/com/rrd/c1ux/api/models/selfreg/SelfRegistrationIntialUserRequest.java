/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	01/04/24	Satishkumar A		CAP-45908				Initial Version
 */
package com.rrd.c1ux.api.models.selfreg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
 @AllArgsConstructor
 @NoArgsConstructor
 @Schema(name = "SelfRegistrationIntialUserRequest", description = "Self Registration Intial User Request of C1UX", type = "object") 
public class SelfRegistrationIntialUserRequest {
	
	@Schema(name = "patternAfterUser", description = "LoginID of Pattern After User", type = "string", example = "USER-RRD")
	private String patternAfterUser;


}
