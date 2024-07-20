/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/14/23				L De Leon				CAP-38151					Initial Version
 *  07/25/23				Krishna Natarajan		CAP-42241					Added state/city/zip fields to Update USPS correct state/city/zip code back into request and response objects
 *  11/20/23				Satishkumar A			CAP-38135					C1UX BE - Modification of Manual Enter Address to use new USPS validation	
 */
package com.rrd.c1ux.api.models.checkout;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name ="CODeliveryInformationSaveResponse", description = "Response Class for saving the selected address in delivery information", type = "object")
public class CODeliveryInformationSaveResponse extends BaseResponse {
	@Schema(name = "city", description = "City of the address", type = "String", example = "ELK GROVE VILLAGE")
	private String city;

	@Schema(name = "state", description = "State code of the address", type = "String", example = "IL")
	private String state;

	@Schema(name = "postalCode", description = "ZIP code of the address", type = "String", example = "600075118")
	private String zip;
	//CAP-38135
	@Schema(name = "showSuggestedAddress", description = "true if we should show SuggestedAddress, false if not", type = "boolean", example = "true")
	private boolean showSuggestedAddress = false; //CAP-38135
	//CAP-38135
	@Schema(name = "suggestedAddress1", description = "Address line 1 for suggested address", type = "String", example = "Suggested Address1")
	private String suggestedAddress1; //CAP-38135
	//CAP-38135
	@Schema(name = "suggestedAddress2", description = "Address line 2 for suggested address", type = "String", example = "Suggested Address2")
	private String suggestedAddress2; //CAP-38135
	//CAP-38135
	@Schema(name = "suggestedCity", description = "The city for suggested address", type = "String", example = "Suggested City")
	private String suggestedCity; //CAP-38135
	//CAP-38135
	@Schema(name = "suggestedState", description = "The state to use for suggested address", type = "String", example = "Suggested State")
	private String suggestedState; //CAP-38135
	//CAP-38135
	@Schema(name = "suggestedZip", description = "The zip to use for suggested address", type = "String", example = "Suggested Zip")
	private String suggestedZip; //CAP-38135
}
