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
 * 	06/04/24	A Boomker			CAP-42231		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocMappedDataResponse", description = "Response Class for cust docs mapped data page request from the list data page. This should return up to one page of information.", type = "object")
public class C1UXCustDocMappedDataResponse extends C1UXCustDocBaseResponse {
	@Schema(name = "records", description = "This object will contain the info to display one page of records. Depending on the request, it may contain valid or invalid records. Even if there are no records to display, this object should be present. It would be null if there is no list on the order or if the list source is not one that allows records to be displayed (like custom list request).", type = "object")
	protected C1UXCustDocMappedDataPage records = null;
}
