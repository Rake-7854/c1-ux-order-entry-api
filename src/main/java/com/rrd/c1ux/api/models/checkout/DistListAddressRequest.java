/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	03/21/24	Satishkumar A		CAP-47389				Initial Version
 *	04/01/24	Satishkumar A		CAP-48123				C1UX BE - Create API to retrieve Dist List addresses.
 */
package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class DistListAddressRequest {

	@Schema(name = "distListID", description = "the requested distribution list address id", type = "string")
	String distListID;
}
