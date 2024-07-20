/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			JIRA #			Description
 *	--------	-----------			----------		-----------------------------------------------------------
 *	09/26/23	M Sakthi			CAP-43996		Initial Version
 */

package com.rrd.c1ux.api.models.admin;

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
@Schema(name ="PABImportRequest", description = "Request Class for import addresses in Personal Address Book", type = "object")
public class PABImportRequest {
	@Schema(name ="file", description = "File", type = "MultipartFile", example="test.xls")
	private MultipartFile file;
	@Schema(name ="uploadType", description = "Upload type", type = "String", example="A")
	private String uploadType;

}

