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
 * 	01/17/24	A Boomker			CAP-44835		Initial version
 * 	02/01/24	A Boomker			CAP-46337		Added downloadURL and list option
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
@Schema(name ="C1UXCustDocUploadVariableFileResponse", description = "Response Class for cust docs UI page when a variable uploaded a file within the UI.", type = "object")
public class C1UXCustDocUploadVariableFileResponse extends C1UXCustDocBaseResponse {
	@Schema(name = "uploadFileName", description = "Name of the file uploaded.", type = "String", example = "myFile.jpg")
	private String uploadFileName = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "uploadId", description = "Number assigned to the uploaded file", type = "number", example = "-1")
	protected int uploadId = AtWinXSConstant.INVALID_ID;
	@Schema(name= "listOption", description = "List Option to add/replace on existing list to represent this newly uploaded file.", type="object")
	protected C1UXUIListOption listOption = null;
	@Schema(name = "downloadLinkURL", description = "If the API should not be called to download this file, then there should be a downloadLinkURL here to allow the user to download it.", type="String")
	protected String downloadLinkURL = AtWinXSConstant.EMPTY_STRING;
}
