/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	03/29/24	Krishna Natarajan	CAP-47391				Created Initial Version
 */
package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor

@Schema(name = "DistListUpdateRequest", description = "Request Class to update Dist List info", type = "object")
public class DistListUpdateRequest {

	@Schema(name = "distListID", description = "String that holds the distribution List ID ", type = "string")
	String distListID;

	@Schema(name = "shipToAttention", description = "String that holds the shipToAttention ", type = "string")
	String shipToAttention;

}
