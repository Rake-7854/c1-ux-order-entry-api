/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/07/24	N Caceres			CAP-50006				Initial Version
 *	06/20/24	C Codina			CAP-50033				Modify /api/kittemplate/addcomponent method to add more information about the added component for the front-end to build panel correctly
 */
package com.rrd.c1ux.api.models.kittemplate;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name ="KitTemplateAddCompResponse", description = "Response Class for Kit Template Add Component", type = "object")
public class KitTemplateAddCompResponse extends BaseResponse {
	
	@Schema(name = "kitLineNumber", description = "This will be the kit line number that is used to add it from kit session.", type = "Integer", example = "1234567890")
	protected Integer kitLineNumber;
	
	@Schema(name = "vendorItemNumber", description = "A String holding the vendor item number.  This may be blank based on the vendor item number configuration to show this.", type = "String")
	private String vendorItemNumber;
	
	@Schema(name = "customerItemNumber", description = "A String holding the customer item number.  Should never be blank.  Must lookup based on vendor item number.", type = "String")
	private String customerItemNumber;

	@Schema(name = "itemDescription", description = "A String holding the item description.  Must lookup based on vendor item number.", type = "String")
	private String itemDescription;
	
	@Schema(name = "requiredItem", description = "A boolean indicating if the item is required.", type = "Boolean")
	private boolean requiredItem;
	
	@Schema(name = "optionalItem", description = "A boolean indicating if the item is optional.", type = "Boolean")
	private boolean optionalItem;
	
	@Schema(name = "suggestedItem", description = "A boolean indicating if the item is suggested.", type = "Boolean")
	private boolean suggestedItem;
	
	@Schema(name = "sequenceAvailable", description = "A boolean indicating if the item can be sequenced.", type = "Boolean")
	private boolean sequenceAvailable;
	
	@Schema(name = "location", description = "A integer holding the location value.  Will start at 0 can goto 2", type = "Integer")
	private int location;
	
	@Schema(name = "imageUrl", description = "The image URL for the item.  This should be a URL that will work for SF.  Must lookup based on vendor item number.", type = "String")
	private String imageUrl;
	
	@Schema(name = "criticalItem", description = "A boolean indicating if the item is critical.", type = "Boolean")
	private boolean criticalItem;
	
	@Schema(name = "nonCriticalItem", description = "A boolean indicating if item is non-critical and can be canceled.", type = "Boolean")
	private boolean nonCriticalItem;
	
	@Schema(name = "shipLaterBackorder", description = "A boolean indicating if item can be shipped later/backordered.", type = "Boolean")
	private boolean shipLaterBackorder;
	
	@Schema(name = "canModifyCritical", description = "A boolean indicating if the user can modify critical, optional.", type = "Boolean")
	private boolean canModifyCritical;
	
	@Schema(name = "uomOptions", description = "A Collection of UOMOption that can be used for the item.", type = "Array")
	Collection<UOMOption> uomOptions;
	
	@Schema(name = "selectedQuantity", description = "An integer holding the selected quantity for the item.", type = "Integer")
	private int selectedQuantity;
	
	@Schema(name = "selectedUOM", description = "A String holding the selected code for the UOM for the item.", type = "String")
	private String selectedUOM;
	
	@Schema(name = "needsContent", description = "A boolean indicating if the item needs custom doc content if a custom document.", type = "Boolean")
	private boolean needsContent;
	
}
