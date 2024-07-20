/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	05/16/24		R Ruth				CAP-42228				Initial request
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="C1UXCustDocListForMappingRequest", description = "Request for a get list for mapping", type = "object")
public class C1UXCustDocListForMappingRequest {
	@Schema(name = "listId", description = "Provie the list ID", type = "number", example = "11111")
   	private int listId = AtWinXSConstant.INVALID_ID;
}
