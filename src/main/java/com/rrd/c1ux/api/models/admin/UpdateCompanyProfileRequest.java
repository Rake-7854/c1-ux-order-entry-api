/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/04/23				N Caceres				CAP-37898					Initial version
 */
package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateCompanyProfileRequest", description = "Request Class for self-admin updating the user's Company Profile", type = "object")
public class UpdateCompanyProfileRequest {

	@Schema(name ="useAsdefaultPreferredShipping", description = "To use Company as Preferred Shipping Address", type = "boolean", example="false", allowableValues = {"false", "true"})
	public boolean useAsdefaultPreferredShipping = false;
	
	@Schema(name ="name1", description = "Name1", type = "String", example="Micheal")
	@Size(min=0, max=35)
	public String name1 = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="name2", description = "Name2", type = "String", example="Jordan")
	@Size(min=0, max=35)
	public String name2 = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="title", description = "Title", type = "String", example="Mr")
	@Size(min=0, max=50)
	public String title = AtWinXSConstant.EMPTY_STRING;
	
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
	
	@Schema(name ="department", description = "Department", type = "String", example="General Management")
	@Size(min=0, max=30)
	public String department = AtWinXSConstant.EMPTY_STRING;
	
	@Schema(name ="division", description = "Division", type = "String", example="Technology")
	@Size(min=0, max=30)
	public String division = AtWinXSConstant.EMPTY_STRING;
		
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
	
	@Schema(name ="zipCd", description = "Zip Code", type = "String", example="60601")
	@Size(min=0, max=12)
	public String zipCd = AtWinXSConstant.EMPTY_STRING; 
	
	@Schema(name ="countryCd", description = "Country Code", type = "String", example="USA")
	@Size(min=0, max=3)
	public String countryCd = AtWinXSConstant.EMPTY_STRING; // (country code)
}
