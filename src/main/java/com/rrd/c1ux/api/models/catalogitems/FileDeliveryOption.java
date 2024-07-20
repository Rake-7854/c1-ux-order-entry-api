/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/13/24	L De Leon			CAP-48938/CAP-48977		Initial Version
 */
package com.rrd.c1ux.api.models.catalogitems;

import java.util.ArrayList;
import java.util.List;

import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "FileDeliveryOption", description = "Class for file delivery options", type = "object")
public class FileDeliveryOption {

	@Schema(name ="optionCode", description = "The code for the file delivery option. This can be: \"E\", \"P\", \"EP\", \"PE\", \"PO\" or \"EA\"", type = "string")
	private String optionCode = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="displayOptions", description = "An array of display options for the option code.", type = "array")
	private List<NameValuePair<String>> displayOptions = new ArrayList<>();

	@Schema(name ="displayLabel", description = "The equivalent display text for the option code. This will be used only for badges.", type = "string")
	private String displayLabel = AtWinXSConstant.EMPTY_STRING;
}