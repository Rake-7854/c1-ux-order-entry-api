/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	05/11/23	Alex Salcedo		 CAP-39210	Initial creation
 */
package com.rrd.c1ux.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CPRedirectResponse", description = "Response Class for CP Redirect", type = "object" )
public class CPRedirectResponse extends BaseResponse
{
	@Schema(name ="redirectURL", description = "C1UX to CP Redirect URL", type = "string", example="https://samltest.sso.rrd.com/idp/startSSO.ping")
	private String redirectURL;
}
