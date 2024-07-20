/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/07/24	N Caceres			CAP-50006				Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "KitTemplateAddCompRequest", description = "Request Class for Kit Template Add Component", type = "object")
public class KitTemplateAddCompRequest {
	
	@Schema(name = "locationCode", description = "This will hold the location code of where to add the item.", type = "Integer", example = "1234567890")
	protected Integer locationCode;
	
	@Schema(name = "compVendorItemNumber", description = "This will be the vendor item of the component being added.", type = "String", example = "ABC123456789")
	protected String compVendorItemNumber;
	
	@Schema(name = "compCustomerItemNumber", description = "This will be the customer item number of the component being added.", type = "String", example = "ABC123456789")
	protected String compCustomerItemNumber;

}
