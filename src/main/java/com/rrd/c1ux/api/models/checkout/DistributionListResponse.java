/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	03/12/24	M Sakthi			CAP-47386				Initial Version
 */

package com.rrd.c1ux.api.models.checkout;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.listscommon.lists.ManagedList;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="DistributionListResponse", description = "Response Class for Distribution Info to load the Checkout", type = "object")
public class DistributionListResponse extends BaseResponse{
	
	@Schema(name = "managedList", description = "Class that contains the list of distribution Info in checkout", type = "object")
	private Collection<ManagedList> managedList;
}
