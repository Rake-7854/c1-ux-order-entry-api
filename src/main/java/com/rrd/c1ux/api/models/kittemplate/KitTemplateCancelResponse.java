/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#				Description
 *	----------	-----------			------------		--------------------------------
 *	06/26/2024	L De Leon			CAP-50359			Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "KitTemplateCancelResponse", description = "Response Class for canceling kit template editing process", type = "object")
public class KitTemplateCancelResponse extends BaseResponse {

	@Schema(name = "kitCustomerItemNumber", description = "A String holding the kit customer item number.", type = "String")
	private String kitCustomerItemNumber;
}