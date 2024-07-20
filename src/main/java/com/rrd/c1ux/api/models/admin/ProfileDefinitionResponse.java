/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 * 
 * Revisions: 
 * 	Date		Modified By			DTS#				Description
 * 	--------	-----------			----------			-----------------------------------------------------------
 * 04/27/23		S Ramachandran		CAP-39201 			API Build - Get Profile Definition of User Type
 *
 */

package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="ProfileDefinitionResponse", description = "Response Class to view Profile Definition of Loggged-in User ", type = "object")
public class ProfileDefinitionResponse extends BaseResponse {	

	@Schema(name ="profileDefinition", description = "ProfileDefinition of Loggged-in User", type = "object")
	private  ProfileDefinition profileDefinition;  	
}
