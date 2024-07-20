/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/25/24	Satishkumar A		CAP-48716				Initial Version
 */
package com.rrd.c1ux.api.models.orders.ordertemplate;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name ="UseOrderTemplateResponse", description = "Response Class for Load Template Order", type = "object")
public class UseOrderTemplateResponse extends BaseResponse {
	
	@Schema(name = "orderLineMessages", description = "A list of order line messages.", type = "List")
	private List<ErrorCode> orderLineMessages;

}
