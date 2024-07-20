/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	06/24/24		R Ruth				CAP-42228				Initial request
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;

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
@Schema(name ="C1UXCustDocLoadAltProfileRequest", description = "Request for a load an alt profile", type = "object")
public class C1UXCustDocLoadAltProfileRequest {
	@Schema(name = "altProfileSelections", description = "list of alternate ProfileSelections chosen on the page", type = "array")
   	private List<C1UXCustDocProfileSelectionBean> altProfileSelections = null;

}
