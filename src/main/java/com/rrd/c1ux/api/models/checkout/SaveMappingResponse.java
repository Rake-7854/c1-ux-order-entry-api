package com.rrd.c1ux.api.models.checkout;

import java.util.HashMap;
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	03/25/24	M Sakthi			CAP-47390				Initial Version
 */

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name = "SaveMappingResponse", description = "Response Class for Save mapping Information", type = "object")
public class SaveMappingResponse extends BaseResponse {

	private HashMap<String, String> map;
}