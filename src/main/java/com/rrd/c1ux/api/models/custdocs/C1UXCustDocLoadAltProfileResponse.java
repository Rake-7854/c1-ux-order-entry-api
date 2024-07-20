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
 * 	06/24/24	R Ruth			   CAP-46503		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocLoadAltProfileResponse", description = "Response Class for cust docs Load Alt Profile", type = "object")
public class C1UXCustDocLoadAltProfileResponse extends C1UXCustDocBaseResponse {
}
