/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	7/01/24			A Boomker			CAP-46488				Initial request
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
@Schema(name ="C1UXCustDocEditKTOERequest", description = "Request for initialize CDOE for a kit template component index", type = "object")
public class C1UXCustDocEditKTOERequest {
	@Schema(name = "index", description = "Kit component index of the custom document component that needs to enter custom doc OE flow", type = "number", example = "11111")
   	private int index = AtWinXSConstant.INVALID_ID;
}
