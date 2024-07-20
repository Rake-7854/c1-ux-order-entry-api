/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	06/30/23	S Ramachandran		CAP-37894		Initial version
 */
package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="UpdateExtendedProfileRequest", description = "Request Class for self-admin updating the user's Extended Profile", type = "object")
public class UpdateExtendedProfileRequest {
	
	@Schema(name ="useAsdefaultPreferredShipping", description = "To use Extended as Preferred Shipping Address", type = "boolean", example="false", allowableValues = {"false", "true"})
	public boolean useAsdefaultPreferredShipping = false;
	
	@Schema(name ="name2", description = "Name2", type = "String", example="Biden")
	@Size(min=0, max=35)
	public String name2 = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="middleInitial", description = "Middle Initial", type = "String", example="A")
	@Size(min=0, max=1)
	public String middleInitial = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="title", description = "Title", type = "String", example="Mr")
	@Size(min=0, max=10)
	public String title = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="suffix", description = "Suffix", type = "String", example="Jr")
	@Size(min=0, max=15)
	public String suffix = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="faxNumber", description = "Fax Number", type = "String", example="1-888-123-1234")
	@Size(min=0, max=24)
	public String faxNumber = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="phoneNumber", description = "Phone Number", type = "String", example="1-888-123-1234")
	@Size(min=0, max=24)
	public String phoneNumber = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="webUrl", description = "Web URL", type = "String", example="https://www.rrd.com")
	@Size(min=0, max=255)
	public String webUrl = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="imageUrl", description = "Image URL", type = "String", example="https://www.google.com")
	@Size(min=0, max=255)
	public String imageUrl = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="pagerNumber", description = "Pager Number", type = "String", example="1-888-123-1234")
	@Size(min=0, max=24)
	public String pagerNumber = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="tollFreeNumber", description = "Toll Free Number", type = "String", example="1-888-123-1234")
	@Size(min=0, max=24)
	public String tollFreeNumber = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="addressLine1", description = "Address Line1 ", type = "String", example="35 W Wacker Dr")
	@Size(min=0, max=35)
	public String addressLine1 = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="addressLine2", description = "Address Line2 ", type = "String", example="35 W Wacker Dr")
	@Size(min=0, max=35)
	public String addressLine2 = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="addressLine3", description = "Address Line3 ", type = "String", example="35 W Wacker Dr")
	@Size(min=0, max=35)
	public String addressLine3 = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="city", description = "City", type = "String", example="Chicago")
	@Size(min=0, max=30)
	public String city = AtWinXSConstant.EMPTY_STRING; 

	@Schema(name ="stateCd", description = "State Code", type = "String", example="IL")
	@Size(min=0, max=4)
	public String stateCd = AtWinXSConstant.EMPTY_STRING; // (state code)

	@Schema(name ="zip", description = "Zip", type = "String", example="60601")
	@Size(min=0, max=12)
	public String zip = AtWinXSConstant.EMPTY_STRING; 

	@Schema(name ="countryCd", description = "Country Code", type = "String", example="USA")
	@Size(min=0, max=3)
	public String countryCd = AtWinXSConstant.EMPTY_STRING; // (country code)
}
