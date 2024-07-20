/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.Util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocUserProfileSearchRequest", description = "Request Class for cust docs user profile type search to get list of profiles eligible to populate the UI defaults", type = "object")
public class C1UXCustDocUserProfileSearchRequest  {

	@Schema(name = "term1", description = "First selected search term", type = "String", example = "First Name")
	private String term1 = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "value1", description = "Value for the first selected search term", type = "String", example = "amy")
	private String value1 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "term2", description = "Second selected search term", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String term2 = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "value2", description = "Value for the second selected search term", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String value2 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "term3", description = "Third selected search term", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String term3 = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "value3", description = "Value for the third selected search term", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String value3 = AtWinXSConstant.EMPTY_STRING;

	public void cleanUpRequest() {
		term1 = Util.nullToEmpty(term1).trim();
		term2 = Util.nullToEmpty(term2).trim();
		term3 = Util.nullToEmpty(term3).trim();
		value1 = Util.nullToEmpty(value1).trim();
		value2 = Util.nullToEmpty(value2).trim();
		value3 = Util.nullToEmpty(value3).trim();
		if ((Util.isBlank(term1)) || (Util.isBlank(value1))) {
			term1 = AtWinXSConstant.EMPTY_STRING;
			value1 = AtWinXSConstant.EMPTY_STRING;
		}
		if ((Util.isBlank(term2)) || (Util.isBlank(value2))) {
			term2 = AtWinXSConstant.EMPTY_STRING;
			value2 = AtWinXSConstant.EMPTY_STRING;
		}
		if ((Util.isBlank(term3)) || (Util.isBlank(value3))) {
			term3 = AtWinXSConstant.EMPTY_STRING;
			value3 = AtWinXSConstant.EMPTY_STRING;
		}

	}
}
