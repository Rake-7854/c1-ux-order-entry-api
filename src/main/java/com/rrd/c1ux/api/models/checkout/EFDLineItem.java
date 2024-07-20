/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/09/24	Satishkumar A		CAP-49204				C1UX API - Create new API to save EFD information
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="EFDLineItem", description = "Request Class to Save EFD Line Information", type = "object")
public class EFDLineItem {

	@Schema(name ="lineNumber", description = "An integer representing the line number of the item", type = "int", example = "123456")
	private int lineNumber;
	
	@Schema(name ="ftpID", description = "A string representing the FTP address", type = "string", example = "127.0.0.1")
	private String ftpID;

	@Schema(name ="efdDestinations", description = "List representing EFD destinations", type = "PDF")
	private List<String> efdDestinations;
	
}
