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
 *	09/04/23	L De Leon			CAP-41595		Initial Version
 */
package com.rrd.c1ux.api.models.admin;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="PABDeleteResponse", description = "Response Class for deleting one or more addresses in Personal Address Book", type = "object")
public class PABDeleteResponse extends BaseResponse { 

	@Schema(name ="deletedCount", description = "Count of successfully deleted address/es", type = "int")
	private int deletedAddressCount = 0;
}