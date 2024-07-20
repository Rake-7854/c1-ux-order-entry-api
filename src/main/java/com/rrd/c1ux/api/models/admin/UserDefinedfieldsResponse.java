
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
 * 	07/10/23	Sakthi M			CAP-37901		Initial version
 */


package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name ="UserDefinefieldsResponse", description = "Response Class for update User defined", type = "object")
public class UserDefinedfieldsResponse extends BaseResponse {
}