/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 *  09/04/23    M Sakthi    		CAP-41593		API Build - Response Object to Save  PAB
 *  11/07/23	S Ramachandran		CAP-44961 		USPS validation in save PAB and to show suggested address 
 */
package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name ="PABSaveResponse", description = "Response Class for Save the Personal address book", type = "object")
public class PABSaveResponse extends BaseResponse{

	@Schema(name = "showSuggestedAddressFlag", description = "Boolean flag inticating availability of USPS Suggested Address", type = "boolean", 
	allowableValues = {"false", "true" })
	private boolean showSuggestedAddress;
	
	@Schema(name = "suggestedAddress1", description = "USPS Suggested Ship To Name1 for provided Personal Address", type = "string", example = "JosephR k")
	private String suggestedAddress1;

	@Schema(name = "suggestedAddress2", description = "USPS Suggested Ship To Name2 for provided Personal Address", type = "string", example = "Biden")
	private String suggestedAddress2;

	@Schema(name = "suggestedCity", description = "USPS Suggested City for provided Personal Address", type = "string", example = "Chicago")
	private String suggestedCity;

	@Schema(name = "suggestedState", description = "USPS Suggested State for provided Personal Address", type = "string", example = "AL")
	private String suggestedState;

	@Schema(name = "suggestedZip", description = "USPS Suggested Zip for provided Personal Addresss", type = "string", example = "123245")
	private String suggestedZip;
}

