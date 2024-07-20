/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/10/24	S Ramachandran		CAP-49205				Initial Version
 */

package com.rrd.c1ux.api.models.checkout;

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

@Schema(name ="EFDStyleInformationResponse", description = "Request Class which return EFD Style Information", type = "object")
public class EFDStyleInformationResponse extends BaseResponse{
	
	@Schema(name = "fromName", description = "From Name configuration for the style", type = "String", example = "Biden")
	private String fromName ="";
	
	@Schema(name = "replyTo", description = "Reply To information for the style", type = "String", example = "JKRowling@warnerbros.com")
	private String replyTo ="";
	
	@Schema(name = "emailSubject", description = "Email Subject for the style", type = "String", example = "Custom Subject")
	private String emailSubject ="";

	@Schema(name = "emailContent", description = "Email Content for the style", type = "String", example = "Custom Content")
	private String emailContent ="";

	@Schema(name = "emailStyle", description = "email style name", type = "String", example = "Custom Style Name")
	private String emailStyle ="";
	
	@Schema(name = "showEmailMessage", description = "Style Indicator to allows Email Message", type = "boolean", example = "false")
	private boolean showEmailMessage = false;
}

