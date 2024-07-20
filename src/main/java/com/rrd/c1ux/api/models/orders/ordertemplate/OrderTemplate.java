/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/23/24	C Codina			CAP-48623				Initial Version
 *	05/02/24	A Salcedo			CAP-48890				Added order template name.
 */

package com.rrd.c1ux.api.models.orders.ordertemplate;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "OrderTemplate", description = "Object containing Order Templates", type = "object")
public class OrderTemplate {
	
	@Schema(name = "orderTemplateID", description = "An encryptedString for the order template ID, that is the ID for the order template.", type = "Integer")
	private String orderTemplateID;
	
	@Schema(name = "orderTemplateName", description = "A String for the order template name.", type = "String")
	private String orderTemplateName;
	
	@Schema(name = "dateCreated", description = "A Date fields formatted using the locale of the user, which shows the date the template was created.", type = "Date")
	private Date dateCreated;
	
	@Schema(name = "accessType", description = "A String holding the access type", type = "String")
	private String accessType;
	
	@Schema(name = "itemCount", description = "An integer holding the number of items in the template.", type = "Integer")
	private int itemCount;
	
	@Schema(name = "canEditDelete", description = "A boolean which will be returned for each item if the user can delete the template.", type = "Boolean")
	private boolean canEditDelete;
	
}
