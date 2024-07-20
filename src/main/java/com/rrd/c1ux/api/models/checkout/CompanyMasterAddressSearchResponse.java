/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		-------------------------------------------------------------
 *	04/10/23				S ramachandran			CAP-38159					Initial Version, response class for Company Master Addresses
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.entity.Address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CompanyMasterAddressSearchResponse", description = "Response Class to retrieve Company Master Addresses based on Search Criteria in Delivery Section", type = "object")
public class CompanyMasterAddressSearchResponse extends BaseResponse {
	
	@Schema(name ="companyMasterListAddresses", description = "List of Company Master Address", type = "array")
	private Collection<Address> companyMasterAddresses;
	
	@Schema(name ="companyMasterAddressesExceedLimit", description = "Flag indicating if returned List of Company Master Address exceeds limit(100 maximum)", type = "boolean")
	boolean companyMasterAddressesExceedLimit = false;
	
}
