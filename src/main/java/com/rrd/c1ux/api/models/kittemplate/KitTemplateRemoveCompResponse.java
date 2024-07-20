/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/11/24	Satishkumar A		CAP-50007				Initial Version
 *
 */
package com.rrd.c1ux.api.models.kittemplate;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name ="KitTemplateRemoveCompResponse", description = "Response Class for Kit Template Remove Component", type = "object")
public class KitTemplateRemoveCompResponse extends BaseResponse {
	
}
