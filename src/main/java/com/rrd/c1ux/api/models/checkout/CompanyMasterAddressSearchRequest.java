/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		-------------------------------------------------------------
 *	04/10/23				S Ramachandran			CAP-38159					Initial Version, response class for Company Master Addresses
 */
package com.rrd.c1ux.api.models.checkout;

import java.io.Serializable;
import java.util.List;

import com.rrd.c1ux.api.models.common.GenericSearchCriteria;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CompanyMasterAddressSearchRequest", description = "Request class to retrieve Company Master Addresses based on search criteria in Delivery Section", type = "object")
public class CompanyMasterAddressSearchRequest  implements Serializable {
	
	@Schema(name ="genericSearchCriteria", description = "List of search criteria for company master address search - current scope is a single search term. "
			+ "Allowable criteriaFieldKey are \"country\",\"state\",\"zip\",\"shiptoname1\",\"shiptoname2\",\"billToCode\",\"loc1\",\"loc2\",\"loc3\"", type = "array")
	private List<GenericSearchCriteria> genericSearchCriteria;

}


