/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	JIRA #            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 * 	05/15/24	R Ruth				CAP-42228		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.ArrayList;
import java.util.List;

import com.rrd.c1ux.api.models.common.GenericNameValuePair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocListForMappingResponse", description = "Response Class for cust docs get list mapping api", type = "object")
public class C1UXCustDocListForMappingResponse extends C1UXCustDocBaseResponse {
	@Schema(name = "existingMapping", description = "Current mapping for this list for this specific custom document specification. If there is no mapping, this will be empty. If there is a mapping, an entry will exist for each column, even if that entry is an empty string.", type = "array")
	private List<String> existingMapping=null;

	@Schema(name = "mergeVars", description = "List of merge vars visible to the user for the dropdown to select from.", type = "array")
	private List<GenericNameValuePair> mergeVars=null;

	@Schema(name = "sampleData", description = "Sample list of data for the selected list - includes up to the first three rows. The inner array is all the values for the same row, while the outer array is all the columns. This essentially transposes the data like a matrix transposition. A file with n rows and m columns will become a two dimentional list with m rows and n columns within those rows."
			+ "The result looks like how we need it for display - that is this: [[(column 1, row 1), (column 1, row 2), (column 1, row 3)], [(column 2, row 1), (column 2, row 2), (column 2, row 3)]]", type = "array")
	private List<ArrayList<String>> sampleData=null;

	@Schema(name = "listHeaders", description = "Headers to be shown for the columns. If the list doesn't have columns in the file, this will have numbers to display.", type = "array")
	private List<String> listHeaders=null;

	@Schema(name = "fileColumns", description = "Number of columns existing in the data file", type = "number")
	private int fileColumns = 0;

}
