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
 * 	11/13/23	A Boomker			CAP-44426		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.Collection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocWorkingProofResponse", description = "Response Class for cust docs working proof updates", type = "object")
public class C1UXCustDocWorkingProofResponse extends C1UXCustDocBaseResponse {
	@Schema(name ="workingProofURLs", description = "List of working proof URLs that should be called to display the working proof for this page.", type = "array")
	Collection<String> workingProofURLs = null;

	@Schema(name ="workingProofLabels", description = "List of working proof Labels that should be shown for the working proof URLs for this page.", type = "array")
	Collection<String> workingProofLabels = null;
}
