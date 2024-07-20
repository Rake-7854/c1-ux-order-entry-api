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
@Schema(name ="SaveEfdInformationRequest", description = "Request Class to Save EFD Information", type = "object")
public class SaveEfdInformationRequest {

	@Schema(name ="efdLineItems", description = "List of EFD line items", type = "object")
	List<EFDLineItem> efdLineItems;
	
	@Schema(name ="emailStyleID", description = "A String representing the encripted email style id.", type = "String", example = "EQLb6uIB8f3Q9pOtoM4EbA==")
	private String emailStyleID;
	
	@Schema(name ="emailMessage", description = "A string representing the email message.", type = "String", example = "")
	private String emailMessage;
	
	@Schema(name ="emailAddresses", description = "List of email addresses.", type = "list", example = "")
	private List<String> emailAddresses;

}
