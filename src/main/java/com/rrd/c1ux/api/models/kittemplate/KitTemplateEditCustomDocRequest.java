/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	07/04/24	C Codina			CAP-46486				Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "KitTemplateEditCustomDocRequest", description = "Request Class for Kit Template Edit Custom Doc Request", type = "object")
public class KitTemplateEditCustomDocRequest extends KitTemplateAddToCartRequest {
	
	@Schema(name = "selectedKitTemplateLineNumber", description = "An Integer holding the kit template line number for the custom doc", type = "Integer")
	int selectedKitTemplateLineNumber;
	
	

}
