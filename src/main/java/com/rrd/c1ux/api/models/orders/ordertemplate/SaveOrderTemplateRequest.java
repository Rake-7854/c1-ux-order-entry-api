/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/19/24	N Caceres			CAP-48584				Initial Version
 */
package com.rrd.c1ux.api.models.orders.ordertemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SaveOrderTemplateRequest", description = "Request Class for Save Order Template", type = "object")
public class SaveOrderTemplateRequest {
	
	@Schema(name = "orderTemplateID", description = "Encrypted Order Template ID", type = "String", example = "640b1e9a0a4a47fb8d25b04b")
	public String orderTemplateID;
	
	@Schema(name = "orderTemplateName", description = "Order Template Name", type = "String", example = "Test")
	public String orderTemplateName;
	
	@Schema(name ="shared", description = "Flag indiciating the template is shared", type = "boolean", example="true", allowableValues = {"false", "true"})
	public boolean shared = false;

}
