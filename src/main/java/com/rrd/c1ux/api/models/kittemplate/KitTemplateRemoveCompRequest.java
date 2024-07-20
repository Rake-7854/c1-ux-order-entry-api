/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/11/24	Satishkumar A		CAP-50007				Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "KitTemplateRemoveCompRequest", description = "Request Class for Kit Template Remove Component", type = "object")
public class KitTemplateRemoveCompRequest {
	
	@Schema(name = "kitLineNumber", description = "This will be the kit line number that is used to remove it from kit session.", type = "Integer", example = "1234")
	protected Integer kitLineNumber;
	
}
