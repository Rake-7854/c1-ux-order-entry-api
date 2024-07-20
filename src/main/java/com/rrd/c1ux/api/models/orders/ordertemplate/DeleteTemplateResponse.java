/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/23/24	S Ramachandran		CAP-48136				Initial Version
 */

package com.rrd.c1ux.api.models.orders.ordertemplate;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)

@Schema(name = "DeleteTemplateResponse", description = "Response Class for delete Order Template", type = "object")
public class DeleteTemplateResponse extends BaseResponse {

}
