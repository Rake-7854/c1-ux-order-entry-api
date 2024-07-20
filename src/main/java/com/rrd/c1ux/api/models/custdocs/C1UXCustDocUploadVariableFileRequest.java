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
@Schema(name ="C1UXCustDocUploadVariableFileRequest", description = "Request Class for cust docs UI to upload a file for a variable within the UI.", type = "object")
public class C1UXCustDocUploadVariableFileRequest {

	@Schema(name ="file", description = "File being uploaded for a variable through cust doc order entry. Type is Multipart file.", type = "object", example="test.jpg")
	private MultipartFile file = null;

	@Schema(name = "hdnVar", description = "Name of a variable within the UI that this should be uploaded for.", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	private String hdnVar = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnVar" name="hdnVar" value=""> // used for dynamic list dependent variables - dependent var name

	// These are optional fields that will have to be shown and passed to CP for certain types of uploads/variables
	// max length of upload description is 50 // URL_PARM_HOSTED_RESOURCE_DESCRIPTION
	@Schema(name = "uploadDescription", description = "Description for the file being uploaded. This only applies to certain types of uploads - namely Hosted Resources. Max length should be 50.", type = "String", example = "EOB PPO Template")
	private String uploadDescription = AtWinXSConstant.EMPTY_STRING; // formerly ufUploadDescriptionTxtBx
}
