/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/22/24				A Boomker				CAP-48496					Initial version
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
@Schema(name ="C1UXCustDocMappedDataPage", description = "Mapped column headers and row data for a single page worth of rows for the front end.", type = "object")
public class C1UXCustDocMappedDataPage {
	@Schema(name = "mergeVarDisplayLabels", description = "List of header strings to display", type = "array")
	private List<String> mergeVarDisplayLabels = new ArrayList<>();
	@Schema(name = "mergeData", description = "List of rows. Each row is a list of string values to display as mapped merge data.", type = "array")
	private ArrayList<ArrayList<String>> mergeData = new ArrayList<>();

}
