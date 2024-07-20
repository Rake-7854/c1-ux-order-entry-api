/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/21/24	C Codina			CAP-50033				Initial Version
 */

package com.rrd.c1ux.api.models.kittemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UOMOption", description = "UOM Option Object", type = "object")
public class UOMOption {
	
	@Schema(name ="uomCode", description = "The uomCode that will be used for the option.", type = "String")
	private String uomCode;
	
	@Schema(name ="uomDisplay", description = "The uomDisplay that should be used for the option.", type = "String")
	private String uomDisplay;
	
	@Schema(name ="minQuantity", description = "An integer holding the minimum quantity.", type = "Integer")
	private int minQuantity;
	
	@Schema(name ="maxQuantity", description = "An integer holding the maximum quantity.", type = "Integer")
	private int maxQuantity;
	
	
	

}
