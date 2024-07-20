/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/09/24	Satishkumar A		CAP-49204				C1UX API - Create new API to save EFD information
 */
package com.rrd.c1ux.api.models.checkout;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name ="SaveEfdInformationResponse", description = "Response Class to Save EFD Line Information", type = "object")
public class SaveEfdInformationResponse extends BaseResponse {

}
