/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	JIRA #            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 * 	04/29/24	R Ruth				CAP-42226		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocListResponse", description = "Response Class for cust docs get list api", type = "object")
public class C1UXCustDocListResponse extends C1UXCustDocBaseResponse {
	@Schema(name = "listOfLists", description = "list of Manage List defined lists visible to the user", type = "object")
	private List<C1uxCustDocListDetails> listOfLists=null;
}
