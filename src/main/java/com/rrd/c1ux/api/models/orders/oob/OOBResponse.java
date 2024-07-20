/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/04/23	Satishkumar A		CAP-45280				C1UX API - Set OOB Mode for CustomPoint session
 */
package com.rrd.c1ux.api.models.orders.oob;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "OOBResponse", description = "Request Class for retrieving Order On Behalf or Self information of logged in user", type = "object")
public class OOBResponse extends BaseResponse {

}