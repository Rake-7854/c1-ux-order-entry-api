/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By				JIRA #			Description
 *	--------	---------------------	---------		-----------------------------------------------------------
 * 	01/29/24	S Ramachandran			CAP-46635		Initial version. C1UX API - save site attribute information for a user
 */


package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name ="C1UserSiteAttributesResponse", description = "Response class for save site attributes for a user", type = "object")
public class C1UserSiteAttributesResponse extends BaseResponse {
	
}