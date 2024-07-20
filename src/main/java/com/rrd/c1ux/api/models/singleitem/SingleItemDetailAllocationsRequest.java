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

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="singleItemDetailAllocationsRequest", description = "Request Class for retrieving orders of Single Item Details Allocations", type = "object")
public class SingleItemDetailAllocationsRequest {

	@Schema(name ="customerItemNumber", description = "Customer Item Number", type = "string")
	@Size(min=0, max=30)
	private String customerItemNumber = "";
	
	@Schema(name ="vendorItemNumber", description = "Vendor Item Number", type = "string")
	@Size(min=0, max=15)
	private String vendorItemNumber = "";
}