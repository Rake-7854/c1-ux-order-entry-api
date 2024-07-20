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
 *	10/03/23	L De Leon			CAP-44059		Initial Version
 */
package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="PABExportResponse", description = "Response Class for exporting all addresses in Personal Address Book", type = "object")
public class PABExportResponse extends BaseResponse { 

}