/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/10/23				A Boomker				CAP-42225					Initial Version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXUIList", description = "Response Class equivalent to UIList in CP - info to display a single list", type = "object")
public class C1UXUIList {
	@Schema(name="listId", description = "Unique number for this data type of list.", type="string")
	protected String listId;
	@Schema(name ="dataTypeCode", description = "For input types that use lists, this is a code corresponding to the data in the list. Values are T - text, I - image, A - area templates, S - inserts. ", type = "string", allowableValues = {"A","I","S","T"})
	protected String dataTypeCode;
	@Schema(name ="listTypeCode", description = "For input types that use lists, this is a code corresponding to how the list was generated and whether refreshes are needed. Values are S - static values, P - profile image repository values, D - dynamic list with sql, '' (empty value) for external source.", type = "string", allowableValues = {"","D","P","S"})
	protected String listTypeCode;
	@Schema(name="listOptions", description="List options available when this is the list for the input type.", type="array")
	protected List<C1UXUIListOption> listOptions = new ArrayList<>();
}
