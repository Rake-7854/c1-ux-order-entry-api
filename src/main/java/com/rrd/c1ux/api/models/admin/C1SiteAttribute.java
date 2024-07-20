/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/20/24	T Harmon			CAP-46543				Added code for EOO
 */

package com.rrd.c1ux.api.models.admin;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="C1SiteAttribute", description = "Class for update a site attribute with 1 value only", type = "object")
public class C1SiteAttribute implements Serializable {
	@Schema(name ="attributeID", description = "Selected Attribute ID", type = "int", example="1")
	private int attributeID;
	@Schema(name ="attributeValueID", description = "Selected Attribute Value ID", type = "int", example="1")
	private int attributeValueID;
}
