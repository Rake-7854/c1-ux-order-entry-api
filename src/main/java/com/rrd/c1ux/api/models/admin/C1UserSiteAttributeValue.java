/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By		JIRA #			Description
 *	--------	--------------	--------		-----------------------------------------------------------
 * 	01/29/24	S Ramachandran	CAP-46635		Initial version. C1UX API - save site attribute information for a user
 * 	02/01/24	S Ramachandran	CAP-46801		Corrected attribute value ID name  
 */

package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C1UserSiteAttributeValue
{
	@Schema(name ="attributeValueID", description = "Attribute Value ID", type = "int", example="254523")
	@Min(0)
	@Max(2147483647)
	private int attributeValueID;
}
