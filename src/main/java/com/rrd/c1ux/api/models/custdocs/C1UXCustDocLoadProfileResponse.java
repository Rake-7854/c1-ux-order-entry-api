/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	11/10/23	A Boomker			CAP-44487		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocLoadProfileResponse", description = "Response Class for cust docs profile load indicating success or failure", type = "object")
public class C1UXCustDocLoadProfileResponse extends BaseResponse {
}