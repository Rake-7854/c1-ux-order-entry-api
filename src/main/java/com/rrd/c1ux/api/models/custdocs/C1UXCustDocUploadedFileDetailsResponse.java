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
 * 	08/24/23	A Boomker			CAP-43223		Initial version
 * 	01/09/24	A Boomker			CAP-44839		Modified for new bean in CustomPoint with download information
 */
package com.rrd.c1ux.api.models.custdocs;

import com.rrd.custompoint.orderentry.customdocs.ui.download.DownloadFileC1uxBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocUploadedFileDetailsResponse", description = "Response Class for cust docs UI page when the detail is needed for an already uploaded file within the UI.", type = "object")
public class C1UXCustDocUploadedFileDetailsResponse extends C1UXCustDocBaseResponse {
	@Schema(name = "canDownloadImmediately", description = "Value indicating the single file name is the only intended valid file and it is available for download now.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean canDownloadImmediately = false;
	@Schema(name = "downloadInfo", description = "Information about the requested download of an uploaded file in a custom document variable", type = "object")
	private DownloadFileC1uxBean downloadInfo = null;

}
