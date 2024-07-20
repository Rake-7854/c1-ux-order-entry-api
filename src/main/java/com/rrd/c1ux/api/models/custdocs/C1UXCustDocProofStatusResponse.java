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
 *  04/03/24	A Boomker			CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.models.custdocs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocProofStatusResponse", description = "Response Class for cust docs on the proof page when status of image proofs is checked", type = "object")
public class C1UXCustDocProofStatusResponse extends C1UXCustDocBaseResponse {

	@Schema(name = "lastProofPageNbr", description = "Number of the last proof page for main proof that is ready- used on the proof page only!", type = "number")
	protected int lastProofPageNbr = 0;
	@Schema(name="anyReady", description = "Flag indicating that any pages for the main proof transaction ID are ready- used on the proof page only!", type = "boolean", allowableValues = {"false", "true"})
	protected boolean anyReady = false;
	@Schema(name="completed", description = "Flag indicating that ALL pages for the main proof transaction ID are ready- used on the proof page only!", type = "boolean", allowableValues = {"false", "true"})
	protected boolean completed = false;

}
