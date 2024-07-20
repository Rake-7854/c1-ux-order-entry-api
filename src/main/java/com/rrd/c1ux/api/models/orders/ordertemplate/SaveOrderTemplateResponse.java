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

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name ="SaveOrderTemplateResponse", description = "Response Class for Save Order Template", type = "object")
public class SaveOrderTemplateResponse extends BaseResponse{
}
