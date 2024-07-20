/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	03/21/24	Satishkumar A		CAP-47389				Initial Version
 *	04/01/24	Satishkumar A		CAP-48123				C1UX BE - Create API to retrieve Dist List addresses.
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.entity.Address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class DistListAddressResponse extends BaseResponse {

	@Schema(name = "addresses", description = "Collection of addresses", type = "object")
	Collection<Address> addresses;
	

	
}
