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

import java.util.List;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="PABDeleteRequest", description = "Request Class for deleting one or more addresses in Personal Address Book", type = "object")
public class PABDeleteRequest {

	@Schema(name ="addressIds", description = "List of address ID's to be deleted", type = "array", example = "[12345, 12346]", defaultValue = "[]")
	@Size(min = 1)
	List<Integer> addressIds;
}