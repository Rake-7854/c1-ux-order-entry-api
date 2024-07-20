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
 * 	08/07/23	A Boomker			CAP-42223		Initial version
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.models.custdocs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocProofLinkResponse", description = "Response Class for cust docs proof page to get the link to a single transaction ID and page number if applicable", type = "object")
public class C1UXCustDocProofLinkResponse extends C1UXCustDocBaseResponse {

	@Schema(name = "proofUrl", description = "URL for the proof requested. If success is true, then this should not be empty.", type = "String", example = "")
	private String proofUrl = "";

}
