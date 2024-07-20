/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#				Description
 *	----------	-----------			------------		--------------------------------
 *	04/04/2024	L De Leon			CAP-48274			Initial Version
 */
package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="DateToDestinationRequest", description = "Request Class for Date To Destination", type = "object")
public class DateToDestinationRequest {

	@Schema(name = "serviceTypeCode", description = "This will be the selected service type code that is currently selected. This may also be the default value it the user cannot change it.", type = "string", example="")
	private String serviceTypeCode;

	@Schema(name = "orderDueDate", description = "The order due date, if the user typed it in. Date should be in MM/DD/YYYY format only.", type = "string", example="04/30/2024")
	private String orderDueDate;

	@Schema(name = "requestedShipDate", description = "The requested ship date, if the user typed it in. Date should be in MM/DD/YYYY format only.", type = "string", example="04/01/2024")
	private String requestedShipDate;

	@Schema(name = "checkValidation", description = "A boolean which indicates if the DTD check should be run, even if on.", type = "boolean", example="false")
	private boolean checkValidation;
}
