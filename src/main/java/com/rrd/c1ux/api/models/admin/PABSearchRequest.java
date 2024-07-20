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
 *  09/01/23    S Ramachandran		CAP-41590		API Build - Request Object to get  PAB all or a search 
 *  02/08/24	M Sakthi			CAP-46964		C1UX BE - Code Fix - OOB - during checkout - PAB to bring requestor PAB address
 */

package com.rrd.c1ux.api.models.admin;

import java.io.Serializable;
import java.util.List;

import com.rrd.c1ux.api.models.common.GenericSearchCriteria;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PABSearchRequest", description = "Request Class to retrieve Personal Address Book based on search criteria in Delivery Section", type = "object")
public class PABSearchRequest implements Serializable {

	@Schema(name = "genericSearchCriteria", description = "List of search criteria for Personal Address Book - current scope is a single search term. "
			+ "Allowable criteriaFieldKey are \"shiptoname1\",\"shiptoattention\",\"city\",\"country\",\"state\",\"zip\"", type = "array")
	private List<GenericSearchCriteria> genericSearchCriteria;
	
	@Schema(name = "callingFrom", description = "Calling from Admin or Checkout", type = "String", example = "")
	private String callingFrom = AtWinXSConstant.EMPTY_STRING;

}
