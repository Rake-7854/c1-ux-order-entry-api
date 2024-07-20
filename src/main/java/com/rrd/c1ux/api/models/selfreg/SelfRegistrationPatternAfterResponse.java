/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/27/23	L De Leon			CAP-45907				Initial Version
 *	01/04/24	Satishkumar A		CAP-45908				C1UX API - Create api to retrieve initial user/profile information
 *	02/28/24	N Caceres			CAP-47449				C1UX BE - Retrieve password rules
 *  03/20/24	T Harmon			CAP-48077				Added new value for user id if coming in via failureAction=USR
 */
package com.rrd.c1ux.api.models.selfreg;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.users.C1UXProfile;
import com.wallace.atwinxs.admin.vo.LoginVOKey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "SelfRegistrationPatternAfterResponse", description = "Response Class for retrieving Pattern After Users for self registration", type = "object")
public class SelfRegistrationPatternAfterResponse extends BaseResponse {

	@Schema(name = "patternAfterUsers", description = "List of Pattern After Users", type = "array")
	private List<LoginVOKey> patternAfterUsers;
	
	@Schema(name ="c1uxProfile", description = "Display the Profile", type = "object")
	private C1UXProfile c1uxProfile;
	
	// CAP-48077
	@Schema(name ="loginID", description = "A String holding the login ID for the new user (may be blank)", type = "string")
	private String loginID;
	
	@Schema(name ="editableUser", description = "A boolean indicating true/false if user is editable or not", type = "boolean")
	private boolean editableUser;

	@Schema(name ="editablePassword", description = "A boolean indicating true/false if user is editable or not", type = "boolean")
	private boolean editablePassword;
	
	@Schema(name ="minimumPasswordChars", description = "Display the Minimum password characters", type = "int", example="6")
	private int minimumPasswordChars;
	
	@Schema(name ="minimumPasswordNumericChars", description = "Display the Minimum password numeric characters", type = "int", example="0")
	private int minimumPasswordNumericChars;
	
	@Schema(name ="minimumUpperCaseChars", description = "Display the Minimum Upper case characters", type = "int", example="0")
	private int minimumUpperCaseChars;
		
	@Schema(name ="minimumSpecialChars", description = "Display the Minimum Special characters", type = "int", example="0")
	private int minimumSpecialChars;

}