/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	03/26/24	S Ramachandran		CAP-47388				Initial Version
 *	04/08/24	S Ramachandran		CAP-48434				Added specific List ID attribute for Dist List mapping
 */

package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor

@Schema(name ="UploadDistListRequest", description = "Request Class to upload Dist List and return mapperData", type = "object")
public class UploadDistListRequest {

	@Schema(name = "recordCount", description = "Integer that holds the record count ", type = "Integer")
	int recordCount;
	
	@Schema(name = "listID", description = "Distribution List ID", type = "string")
	String listID;	
}

