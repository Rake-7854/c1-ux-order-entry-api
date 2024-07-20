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
 * 	05/20/24	R Ruth				CAP-42228		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocSaveListMappingResponse", description = "Response Class for cust docs save list mapping api", type = "object")
public class C1UXCustDocSaveListMappingResponse extends C1UXCustDocBaseResponse{
	@Schema(name = "totalColumns", description = "Number of columns existing in the data file", type = "number")
	private int totalColumns = 0;
	
	@Schema(name = "numColumnsMapped", description = "Number of columns mapped", type = "number")
	private int numColumnsMapped = 0;
	
	@Schema(name = "numColumnsNotMapped", description = "Number of columns not mapped", type = "number")
	private int numColumnsNotMapped = 0;
	
	@Schema(name = "totalNumDataRows", description = "Total number of data rows", type = "number")
	private int totalNumDataRows = 0;
}
