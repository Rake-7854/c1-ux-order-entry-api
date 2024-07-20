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

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "EOOAttribute", description = "Response Class for EOO attributes", type = "object")
public class EOOAttribute {
	
	@Schema(name ="attributeID", description = "The ID of the EOO attribute", type = "int", example="123")
	int attributeID;
	
	@Schema(name ="attributeDesc", description = "The string holding the attribute display value", type = "string", example="colour")
	String attributeDesc;

	@Schema(name ="attributeValues", description = "The List holding the attribute values", type = "object")
	List<EOOAttributeValue> attributeValues;	
}
