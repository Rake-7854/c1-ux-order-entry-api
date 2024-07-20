/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#				Description
 *	----------	-----------			------------		--------------------------------
 *	06/10/2024	L De Leon			CAP-49882			Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="InitKitTemplateRequest", description = "Request Class for initializing Kit template", type = "object")
public class InitKitTemplateRequest {

	@Size(min=0, max=30)
	@Schema(name = "customerItemNumber", description = "The String holding the customer item number for the kit.", type = "string", example="KITTEMPLATE")
	private String customerItemNumber;

	@Size(min=0, max=15)
	@Schema(name = "vendorItemNumber", description = "TheA String holding the vendor item number for the kit. This is not required and can be blank.", type = "string", example="")
	private String vendorItemNumber;
}
