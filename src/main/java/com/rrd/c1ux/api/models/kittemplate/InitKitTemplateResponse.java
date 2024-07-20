/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#				Description
 *	----------	-----------			------------		--------------------------------
 *	06/10/2024	L De Leon			CAP-49882			Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.orderentry.ao.KitFormBean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "InitKitTemplateResponse", description = "Response Class for initializing Kit template", type = "object")
public class InitKitTemplateResponse extends BaseResponse {

	@Schema(name = "kitFormBean", description = "A KitFormBean object which will contain kit information.", type = "object")
	private KitFormBean kitFormBean;

	@Schema(name = "allowCategoryViewSelection", description = "A boolean which indicates if the user can use browse for kit items.", type = "boolean", example = "true")
	private boolean allowCategoryViewSelection;

	@Schema(name = "kitMaxCharacters", description = "An integer which gives max size for the assembly instructions.", type = "integer")
	private int kitMaxCharacters;

	@Schema(name = "containsOptionalItems", description = "A boolean which indicates if the kit has optional items.", type = "boolean", example = "true")
	private boolean containsOptionalItems;

	@Schema(name = "allowDuplicateCustomDocs", description = "A boolean which indicates if the kit allows duplicate custom documents.", type = "boolean", example = "false")
	private boolean allowDuplicateCustomDocs;
}