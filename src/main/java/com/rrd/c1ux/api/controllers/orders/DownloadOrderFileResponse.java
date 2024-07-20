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
 *	03/12/24	S Ramachandran		CAP-47744		Initial Version
 */

package com.rrd.c1ux.api.controllers.orders;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="DownloadOrderFileResponse", description = "Response Class for download Order file from order in Order Search", type = "object")
public class DownloadOrderFileResponse extends BaseResponse { 

}