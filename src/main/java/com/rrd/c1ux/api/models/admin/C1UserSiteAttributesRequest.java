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

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="C1UserSiteAttributesRequest", description = "Request class to save site attributes for a user profile", type = "object")
public class C1UserSiteAttributesRequest {

	@Schema(name ="c1UserSiteAttributes", description = "List of site attribute being modified to the user profile", type = "array")
	private List<C1UserSiteAttribute> c1UserSiteAttributes;
	
}