/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	10/17/23		AKJ Omisol			CAP-43024				Initialize request
 *	07/09/24		A Boomker			CAP-46538				Rename object to avoid confusion
 */

package com.rrd.c1ux.api.models.custdocs;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="C1UXCustDocLoadImprintHistoryRequest", description = "Request during a User Interface where the UI page form has been serialized and sent for a specific action", type = "object")
public class C1UXCustDocLoadImprintHistoryRequest {
	@Schema(name = "customDocOrderLineID", description = "'Order ID' of an Order that is already submitted", type = "int", example = "581863")
	@Min(0)
	@Max(2147483647)
	private int customDocOrderLineID;
}
