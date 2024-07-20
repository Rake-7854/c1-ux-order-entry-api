/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	07/09/24		A Boomker			CAP-46538				Initialize request
 */

package com.rrd.c1ux.api.models.custdocs;

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
@Schema(name ="C1UXCustDocBasicImprintHistorySearchRequest", description = "Request for updated imprint history display changing either the flag for my orders only or this item only.", type = "object")
public class C1UXCustDocBasicImprintHistorySearchRequest {
	@Schema(name = "myOrdersOnly", description = "Value indicating the imprint history list should be for this profile only. Default value is true.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean myOrdersOnly = true;
	@Schema(name = "thisItemOnly", description = "Value indicating the imprint history list should be for this item only. Default value is true.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean thisItemOnly = true;

}
