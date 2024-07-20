/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By				JIRA#				Description
 * 	--------		-----------				---------------- 	--------------------------------
 *	03/15/24		S Ramachandran			CAP-47387			Initial Version.
 */
package com.rrd.c1ux.api.models.checkout;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "WorksheetRequest", description = "Request Class for Upload Distribution List file to CP path and get worksheet names", type = "object")
public class WorksheetRequest
{
	@Schema(name ="distFile", description = "Distribution List File to upload", type = "MultipartFile", example="test.xls")
	private MultipartFile distFile;
}