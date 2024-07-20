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
 * 	07/09/24	A Boomker			CAP-44486		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocImprintHistorySearchResponse", description = "Response Class for cust docs imprint history search containing the list of valid orders", type = "object")
public class C1UXCustDocImprintHistorySearchResponse extends BaseResponse {

	@Schema(name = "imprintHistory", description = "Imprint history section that will contain results.", type = "object")
	private C1UXImprintHistoryOptions imprintHistory;

}
