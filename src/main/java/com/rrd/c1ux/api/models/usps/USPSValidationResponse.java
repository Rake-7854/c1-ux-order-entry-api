/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      ----------------------------------------------------------------------------
 *  12/20/23	S Ramachandran		CAP-44961 		Initial Version. Response class of Suggested Address after USPS validation 
 */

package com.rrd.c1ux.api.models.usps;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name ="USPSValidationResponse", description = "Response Class with USPS Suggested Address after USPS Validation", type = "object")
public class USPSValidationResponse extends BaseResponse {

	@Schema(name = "showSuggestedAddress", description = "Boolean flag indicating availability of USPS Suggested Address", type = "boolean", 
	allowableValues = {"false", "true" })
	private boolean showSuggestedAddress;
	
	@Schema(name = "suggestedAddress1", description = "USPS Suggested Address1", type = "string", example = "JosephR k")
	private String suggestedAddress1;

	@Schema(name = "suggestedAddress2", description = "USPS Suggested Address2", type = "string", example = "Biden")
	private String suggestedAddress2;

	@Schema(name = "suggestedCity", description = "USPS Suggested City", type = "string", example = "Chicago")
	private String suggestedCity;

	@Schema(name = "suggestedState", description = "USPS Suggested State", type = "string", example = "AL")
	private String suggestedState;

	@Schema(name = "suggestedZip", description = "USPS Suggested Zip", type = "string", example = "123245")
	private String suggestedZip;
}