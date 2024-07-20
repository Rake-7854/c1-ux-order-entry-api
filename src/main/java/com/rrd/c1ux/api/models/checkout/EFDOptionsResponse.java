/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/13/24	N Caceres			CAP-49151				Response object for EFD Options
 *	05/29/24	Krishna Natarajan	CAP-49748				Added new fields to the getEFDOptions
 *	05/31/24	Krishna Natarajan	CAP-49814 				Added new field to the emailSourceTypes
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.Map;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.orderentry.ao.EFDDestinationsFormBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="EFDOptionsResponse", description = "Response Class which return EFD Options", type = "object")
public class EFDOptionsResponse extends BaseResponse {
	
	@Schema(name = "efdLines", description = "Object that will contain the lines and options for the EFD lines", type = "object")
	private EFDDestinationsFormBean efdLines;
	
	@Schema(name = "efdLinesExists", description = "A boolean which indicates if there is at least 1 EFD line", type = "boolean", allowableValues = {"false", "true"})
	boolean efdLinesExists = false;
	
	@Schema(name = "ftpOnly", description = "A boolean which indicates if EFD lines options is FTP only", type = "boolean", allowableValues = {"false", "true"})
	boolean ftpOnly = false;
	
	@Schema(name = "allowEditEmail", description = "flag that says if allow Edit Email", type = "boolean", allowableValues = {"false", "true"})
	boolean allowEditEmail = false;
	
	@Schema(name = "allowMultipleEmails", description = "flag that says if allow Multiple Emails", type = "boolean", allowableValues = {"false", "true"})
	boolean allowMultipleEmails = false;
	
	@Schema(name = "defaultEmailAddress", description = "A default Email Address", type = "string")
	String defaultEmailAddress ="";

	@Schema(name = "emailSourceTypes", description = "Options having the emailSourceTypes", type = "object", allowableValues = {"Digital", "DigiMag"})
	Map<String,String> emailSourceTypes;
}
