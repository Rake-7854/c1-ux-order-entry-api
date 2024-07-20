/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	11/10/23		A Boomker			CAP-44487				Initial request
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
@Schema(name ="C1UXCustDocLoadProfileRequest", description = "Request during a User Interface where the UI page form has been serialized and sent for a specific action of loading the defaults of a specific profile type and profile number", type = "object")
public class C1UXCustDocLoadProfileRequest {
	@Schema(name = "profileNumber", description = "Profile number available to be loaded for the specified profile type", type = "int", example = "581863")
   	private int profileNumber = AtWinXSConstant.INVALID_ID;
	@Schema(name = "profileDefinitionNumber", description = "Profile definition number indicating the specified profile type. This only needs to be populated when this isn't the USER profile type.", type = "int", example = "-1")
   	private int profileDefinitionNumber = AtWinXSConstant.INVALID_ID;

}
