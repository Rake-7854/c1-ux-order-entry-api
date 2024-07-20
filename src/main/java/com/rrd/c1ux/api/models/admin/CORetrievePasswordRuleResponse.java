/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	03/28/23	Sakthi M           CAP-39164	   Get Password Requirements
 * 	05/12/23	A Salcedo		   CAP-40607	   Added translation.
*/

package com.rrd.c1ux.api.models.admin;

import java.util.Map;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CORetrievePasswordRuleResponse", description = "Response Class to view Minimum Password rule", type = "object")
public class CORetrievePasswordRuleResponse extends BaseResponse{
	@Schema(name ="minimumPasswordChars", description = "Display the Minimum password characters", type = "int", example="6")
	private int minimumPasswordChars;
	
	@Schema(name ="minimumPasswordNumericChars", description = "Display the Minimum password numeric characters", type = "int", example="0")
	private int minimumPasswordNumericChars;
	
	@Schema(name ="minimumUpperCaseChars", description = "Display the Minimum Upper case characters", type = "int", example="0")
	private int minimumUpperCaseChars;
		
	@Schema(name ="minimumSpecialChars", description = "Display the Minimum Special characters", type = "int", example="0")
	private int minimumSpecialChars;
	
	//CAP-40607
	@Schema(name ="translationChangePassword", description = "Messages from \"changePassword\" translation file will load here.", type = "string",  example="\"translation\": { \"nameLabel\": \"Your Name\"}")
	private Map<String, String> translationChangePassword;
}
