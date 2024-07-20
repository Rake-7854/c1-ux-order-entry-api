/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/18/24	Sakthi M			CAP-50145				Initial Version
 */

package com.rrd.c1ux.api.models.kittemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ComponentItems", description = "Request Class for ComponentItems", type = "object")
public class ComponentItems {
	private String kitLineNumber;
	private String uomCode;
	private int quantity;
	private int sequenceLocationId;
	private int itemSequenceNumber;
	private String criticalIndicator;

}
