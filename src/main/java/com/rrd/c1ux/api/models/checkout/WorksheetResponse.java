/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/15/24				S Ramachandran			CAP-47387					Initial Version.
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="WorksheetResponse", description = "Respnse Class to return Worksheet names from Upload Dist List file", type = "object")
public class WorksheetResponse extends BaseResponse {
	
	@Schema(name ="workSheetNames", description = "Worksheet Name(s)", type = "array")
	private List<String> workSheetNames;
}
