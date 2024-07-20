/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/20/24	T Harmon			CAP-46543				Added code for EOO
 */


package com.rrd.c1ux.api.models.shoppingcart;

import java.io.Serializable;

import com.rrd.c1ux.api.models.admin.C1SiteAttribute;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SaveSelectedAttributesRequest", description = "Request Class for updating eoo attributes.", type = "object")
public class SaveSelectedAttributesRequest implements Serializable 
{
	@Schema(name ="c1SiteAttributes", description = "List of Attributes and their current value on the popup", type = "array")
	private C1SiteAttribute[] c1SiteAttributes;
	@Schema(name="changedAttributeID", description = "The actual attribute ID that was changed.", type="int")
	private int changedAttributeID;	
}
