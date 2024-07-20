/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	07/04/24	C Codina			CAP-46486				Initial Version
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
@Schema(name = "KitTemplateEditCustomDocResponse", description = "Response Class for Kit Template Edit Custom Doc", type = "object")
public class KitTemplateEditCustomDocResponse extends BaseResponse {
	
	@Schema(name = "index", description = "An integer that will hold the value of kit index", type = "Integer")
	int index;

}
