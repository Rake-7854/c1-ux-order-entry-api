/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/10/24	S Ramachandran		CAP-49205				Initial Version
 */

package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor

@Schema(name = "EFDStyleInformationRequest", description = "Request Class to retrieve EFD Style Information", type = "object")
public class EFDStyleInformationRequest {

	@Schema(name = "styleID", description = "Style ID", type = "string")
	String styleID;
}