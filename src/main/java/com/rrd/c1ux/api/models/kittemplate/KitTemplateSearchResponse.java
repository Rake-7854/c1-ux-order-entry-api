/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#				Description
 *	----------	-----------			------------		--------------------------------
 *	07/03/2024	Satishkumar A		CAP-50560			Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "KitTemplateSearchResponse", description = "Response Class for Kit Template Search Response", type = "object")
public class KitTemplateSearchResponse extends BaseResponse {

	@Schema(name = "searchTerm", description = " This will be the passed in search term, which we have to return back in the response", type = "string", example="test")
	private String searchTerm;

}