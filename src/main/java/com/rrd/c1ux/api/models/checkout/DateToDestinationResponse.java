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

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.orderentry.validators.entity.DueDateValidator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "DateToDestinationResponse", description = "Response Class for performing date to destination WCSS call", type = "object")
public class DateToDestinationResponse extends BaseResponse {

	@Schema(name = "dueDateValidator", description = "The DueDateValidator object, which contains all the information for the DueDateValidtor.", type = "object")
	private DueDateValidator dueDateValidator;

	@Schema(name = "dateToDestionationAvailable", description = "A boolean indicating if Date To Destination is available.", type = "boolean", example = "true")
	private boolean dateToDestionationAvailable;

	@Schema(name = "statusCode", description = "This will be a value that indicates if the DTD call resulted in the following:  Success, Error, Warning, NoMessage.  Success = 0, Error = 1, Warning = 2, NoMessage = 3.", type = "string", example = "0")
	private String statusCode;

	@Schema(name = "informMessage", description = "A String holding an information message. This will be used for any inform messages.", type = "string", example = "Your earliest Order Arrival Date is 04/01/2024")
	private String informMessage;

	@Schema(name = "shipNowOrLater", description = "A boolean indicating if this is ship now or later. This will be true if we want to show ship now or later section.", type = "boolean", example = "true")
	private boolean shipNowOrLater;

	@Schema(name = "promptExpedite", description = "A boolean indicating if we should prompt for expedite.", type = "boolean", example = "true")
	private boolean promptExpedite;

	@Schema(name = "expediteMessage", description = "A String holding the expedite message for the popup.  Should only be used with isPromptExpedite.", type = "string")
	private String expediteMessage;

	@Schema(name = "expediteDate", description = "A String date holding the requested order due date", type = "string")
	private String expediteDate;

	@Schema(name = "expediteOriginalDateMessage", description = "A String holding the message for indicating we should use earliest due date.", type = "string")
	private String expediteOriginalDateMessage;

	@Schema(name = "expediteServiceMessage", description = "A String holding the bullet header for expedite order", type = "string")
	private String expediteServiceMessage;

	@Schema(name = "expediteServiceFeeMessage", description = "A String holding the expedite service fee message.", type = "string")
	private String expediteServiceFeeMessage;

	@Schema(name = "expediteServiceCharge", description = "A double value with the expedite service charge", type = "double")
	private double expediteServiceCharge;
}