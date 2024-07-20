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
 */
package com.rrd.c1ux.api.models.custdocs;

import org.springframework.web.multipart.MultipartFile;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocUploadListFileRequest", description = "Request Class for cust docs UI to upload a file for a list within the UI.", type = "object")
public class C1UXCustDocUploadListFileRequest {

	@Schema(name ="file", description = "File being uploaded for a merge list through cust doc order entry.", type = "MultipartFile", example="test.jpg")
	private MultipartFile file = null;

	// these fields are specific to lists
	// max length of ufListName is 25
	@Schema(name = "listName", description = "Unique name to be shown for the list file being uploaded. Max length should be 25.", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String listName = AtWinXSConstant.EMPTY_STRING;
	// max length of ufListDescription is 2000
	@Schema(name = "listDescription", description = "Longer description to be shown for the list file being uploaded. Max length should be 25.", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String listDescription = AtWinXSConstant.EMPTY_STRING;
	// acceptable values for privacy are "shared","private" and "" (empty)
	@Schema(name = "uploadListSettingsPrivacy", description = "Privacy setting to be assigned for the list file being uploaded - only applicable if the user is allowed to select it.", type = "String", example = AtWinXSConstant.EMPTY_STRING,
			allowableValues = {	"","shared","private"})
	private String uploadListSettingsPrivacy = AtWinXSConstant.EMPTY_STRING;
	// overwrite is only true if we're selecting a specific worksheet for an excel list
	@Schema(name = "listOverwrite", description = "Value indicating this request is making a choice of a specific worksheet on a multi-sheet Excel file already uploaded. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean listOverwrite = false;
	// firstline indicates the first line is header row or not
	@Schema(name = "headerFirstLine", description = "Value indicating this list has the first row contents containing headers for the rest of the list. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean headerFirstLine = false;

}
