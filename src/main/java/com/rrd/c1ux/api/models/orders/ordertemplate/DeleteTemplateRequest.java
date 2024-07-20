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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class DeleteTemplateRequest {

	@Schema(name = "orderTemplateID", description = "order Template ID", type = "string")
	String orderTemplateID;
}