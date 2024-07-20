/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By      	JIRA #          Description
 *	--------    -----------			----------		-------------------------------------------------------------------------------
 *  02/12/24	S Ramachandran		CAP-47062		Initial Version. C1UX API - to return list of orders used in current allocation  
 */

package com.rrd.c1ux.api.models.singleitem;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name ="singleItemDetailAllocationsResponse", description = "Response Class to retrieve orders of Single Item Details Allocations", type = "object")
public class SingleItemDetailAllocationsResponse extends BaseResponse {
	
	@Schema(name = "itemAllocationOrderList", description = "List of orders used to allocate the item and user", type = "List")
	List<ItemAllocationOrder> itemAllocationOrderList;
	
}