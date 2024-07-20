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
 *  09/04/23    M Sakthi    		CAP-41593		API Build - Request Object to Save  PAB 
 */

package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PABSaveRequest", description = "Request Class to Save Personal Address Book", type = "object")
public class PABSaveRequest{
	@Schema(name ="addressID", description = "Address ID", type = "int", example="3455")
	private int addressID;
	@Schema(name ="name", description = "Name", type = "String", example="Joseph")
	@Size(min=0, max=35)
	private String name;
	@Schema(name ="name2", description = "Name 2", type = "String", example="Vijay")
	@Size(min=0, max=35)
	private String name2;
	@Schema(name ="address1", description = "Address 1", type = "String", example="Address 1")
	@Size(min=0, max=35)
	private String address1;
	@Schema(name ="address2", description = "Address 2", type = "String", example="Address 2")
	@Size(min=0, max=35)
	private String address2;
	@Schema(name ="address3", description = "Address 3", type = "String", example="Address 3")
	@Size(min=0, max=35)
	private String address3;
	@Schema(name ="city", description = "City", type = "String", example="NewYark")
	@Size(min=0, max=30)
	private String city;
	@Schema(name ="state", description = "State", type = "String", example="AZ")
	@Size(min=0, max=4)
	private String state;
	@Schema(name ="zip", description = "Zip", type = "String", example="906767")
	@Size(min=0, max=12)
	private String zip;
	@Schema(name ="country", description = "Country", type = "String", example="USA")
	@Size(min=0, max=12)
	private String country;
	@Schema(name ="shipToAttn", description = "Ship To Attention", type = "String", example="Test Ship To Attention")
	@Size(min=0, max=35)
	private String shipToAttn;
	@Schema(name ="phoneNumber", description = "Phone Number", type = "String", example="124-232-1234")
	@Size(min=0, max=24)
	private String phoneNumber;
	@Schema(name ="hasPassedZipValidation", description = "Has Passed Zip Validation", type = "boolean", example="true")
	private boolean hasPassedZipValidation;
	@Schema(name ="isDefaultAddress", description = "Validate Default Address", type = "boolean", example="true")
	private boolean isDefaultAddress;
	
	@Schema(name ="isCorporateAddress", description = "Is CorporateAddress Address flag", type = "boolean", example="false")
	private boolean isCorporateAddress;
	@Schema(name ="isExtendedAddress", description = "Is Extended Address flag", type = "boolean", example="false")
	private boolean isExtendedAddress;
	@Schema(name ="showShipToAtn", description = "Show ShipTo Attention flag", type = "boolean", example="false")
	private boolean showShipToAtn;

}
