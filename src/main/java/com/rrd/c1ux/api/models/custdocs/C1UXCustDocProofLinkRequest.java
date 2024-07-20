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
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocProofLinkRequest", description = "Request Class for cust docs proof page to get the link to a single transaction ID and page number if applicable", type = "object")
public class C1UXCustDocProofLinkRequest  {

	@Schema(name = "proofType", description = "Type of proof for which the URL is requested. If this proof has no current transaction ID, one will be started if applicable. If the type is not applicable, an error will be returned. Types are I - Image, J - Jellyvision, P - PDF, X - Xert, W - Working, E - Email, D - Precision Dialogue, B - Blaze. Empty will default to Image.", type = "String", example = "",
			allowableValues = { "I", "J", "P", "W", "E", "X", "D", "B" })
	private String proofType = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "proofPageNbr", description = "Number of the page requested for image proof. This is not applicable for any other proof types", type = "number")
	protected int proofPageNbr = 0;

}
