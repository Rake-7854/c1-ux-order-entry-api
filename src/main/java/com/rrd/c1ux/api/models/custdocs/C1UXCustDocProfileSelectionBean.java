/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	06/24/24				R Ruth					CAP-46503					Initial Version
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
@Schema(name =" C1UXCustDocProfileSelectionBean", description = "Bean for C1UXCustDocLoadAltProfileRequest", type = "object")
public class C1UXCustDocProfileSelectionBean {
	@Schema(name = "definitionId", description = "Provide the definition ID", type = "number", example = "11111")
   	private int definitionId = AtWinXSConstant.INVALID_ID;
	
	@Schema(name = "profileNum", description = "Provide the profile Number", type = "number", example = "11111")
   	private int profileNum = AtWinXSConstant.INVALID_ID;
}
