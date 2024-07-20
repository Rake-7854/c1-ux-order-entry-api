/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	01/22/24	Satishkumar A		CAP-46407				C1UX API - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 */
package com.rrd.c1ux.api.models.eoo;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "ValidateCheckoutResponse", description = "Response Class for checking EOO attributes validation", type = "object")
public class ValidateCheckoutResponse extends BaseResponse{

	@Schema(name ="attributes", description = "The List holding the attribute details", type = "object")
	List<EOOAttribute> attributes;

}
