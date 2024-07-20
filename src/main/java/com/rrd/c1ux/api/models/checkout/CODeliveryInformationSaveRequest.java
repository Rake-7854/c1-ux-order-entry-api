/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/14/23				L De Leon				CAP-38151					Initial Version
 *	05/18/23				L De Leon				CAP-40324					Modified shipToName to match translation key
 *	06/02/23				L De Leon				CAP-40324					Fixed the sample value for addressLine1
 *	06/15/23				Satishkumar A			CAP-41094					API Change - Add overrideUSPSWarning boolean flag to request in Delivery Save API to override USPS failures and save
 *	08/15/23				C Codina				CAP-41550					Added PAB ID
 *  10/05/23				T Harmon				CAP-44416					Added parameters for saving PAB from manual address
 */
package com.rrd.c1ux.api.models.checkout;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CODeliveryInformationSaveRequest", description = "Response Class for saving the selected address in delivery information", type = "object")
public class CODeliveryInformationSaveRequest {

	@Schema(name = "shipToName", description = "Ship To Name of the selected address. Split name fields should be concatenated before being sent in this field as a single name", type = "String", example = "MOORE WALLACE INC")
	@Size(min = 0, max = 35)
	private String shipToName;

	@Schema(name = "shipToName2", description = "Ship To Name 2 of the selected address", type = "String", example = "LABEL SYSTEMS DIVISION")
	@Size(min = 0, max = 35)
	private String shipToName2;

	@Schema(name = "addressLine1", description = "Address Line 1 of the selected address", type = "String", example = "925 PRATT BLVD")
	@Size(min = 0, max = 35)
	private String addressLine1;

	@Schema(name = "addressLine2", description = "Address Line 2 of the selected address", type = "String", example = "DOCK #2 NORTH SIDE")
	@Size(min = 0, max = 35)
	private String addressLine2;

	@Schema(name = "addressLine3", description = "Address Line 3 of the selected address", type = "String", example = "1ST FLOOR")
	@Size(min = 0, max = 35)
	private String addressLine3;

	@Schema(name = "city", description = "City of the selected address", type = "String", example = "ELK GROVE VILLAGE")
	@Size(min = 0, max = 30)
	private String city;

	@Schema(name = "stateOrProvince", description = "State or Province code of the selected address", type = "String", example = "IL")
	@Size(min = 0, max = 4)
	private String stateOrProvince;

	@Schema(name = "postalCode", description = "Postal or ZIP code of the selected address, for USA: minimum size should be 5", type = "String", example = "600075118")
	@Size(min = 0, max = 12)
	private String postalCode;

	@Schema(name = "country", description = "Uppercased Country code of the selected address", type = "String", example = "USA")
	@Size(min = 0, max = 3)
	private String country;

	@Schema(name = "phoneNumber", description = "Phone number for the selected address", type = "String", example = "1234567890")
	@Size(min = 0, max = 24)
	private String phoneNumber;

	@Schema(name = "shipToAttention", description = "Ship To Attention of the selected address", type = "String", example = "JOHN DOE")
	@Size(min = 0, max = 35)
	private String shipToAttention;

	@Schema(name = "billToCode", description = "Bill To code for the selected bill to address - combination of corporate and sold to numbers", type = "String", example = "0000097000:00001")
	private String billToCode;

	@Schema(name = "billToAttention", description = "Bill to attention text", type = "String", example = "JANE DOE")
	@Size(min = 0, max = 35)
	private String billToAttention;

	@Schema(name = "addressSource", description = "Selected address source code", type = "String", example = "DEF", allowableValues = {
			"DEF", "MEN", "CML" })
	@Size(min = 0, max = 3)
	private String addressSource;

	@Schema(name = "corporateNbr", description = "Corporate number, based on the selected bill to address,", type = "String", example = "0000097000")
	@Size(min = 0, max = 10)
	private String corporateNbr;

	@Schema(name = "soldToNbr", description = "Sold to number, based on the selected bill to address", type = "String", example = "00001")
	@Size(min = 0, max = 5)
	private String soldToNbr;

	@Schema(name = "wcssShipToNbr", description = "WCSS Ship To, only available when the selected address is from the Company Master List", type = "String", example = "0636")
	@Size(min = 0, max = 4)
	private String wcssShipToNbr;
	
	//CAP-41094
	@Schema(name = "overrideUSPSErrors", description = "Flag indicating the request in Delivery Save API to override USPS failures and save or not. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean overrideUSPSErrors = false;
	
	//CAP-41550
	@Schema(name = "pabId", description = "Personal Address Book ID,", type = "int", example = "-1")
	@Min(-1)
	@Max(2147483647)
	private int pabId;
	
	// CAP-44416
	@Schema(name = "addToPAB", description = "Save to PAB flag", type = "boolean", example = "true/false")
	private boolean addToPAB;
	
	@Schema(name = "personalAddressDefault", description = "Save PAB As Default", type = "boolean", example = "true/false")
	private boolean personalAddressDefault;		
}
