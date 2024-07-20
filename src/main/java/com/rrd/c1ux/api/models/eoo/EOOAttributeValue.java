/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	01/22/24	Satishkumar A		CAP-46407				C1UX API - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 *  02/20/24	T Harmon			CAP-46543				Added code for saving eoo attributes
 */
package com.rrd.c1ux.api.models.eoo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "EOOAttributeValue", description = "Response Class for EOO attribute values", type = "object")
public class EOOAttributeValue {
	
	@Schema(name ="attributeValID", description = "The ID of the EOO Attribute Value", type = "int", example="456")
	int attributeValID;
	
	@Schema(name ="attributeValDesc", description = "The string holding the attribute value description", type = "string", example="blue")
	String attributeValDesc;
	
	@Schema(name="selected", description = "A boolean indicating if the value is selected", type = "string", example="true")
	boolean isSelected = false;
}
