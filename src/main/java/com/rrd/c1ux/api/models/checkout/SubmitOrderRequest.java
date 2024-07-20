/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By				JIRA#				Description
 * 	--------		-----------				---------------- 	--------------------------------
 *	09/05/23		Satishkumar A      		CAP-42763			C1UX BE - Order Routing Justification Text Submit Order
 */
package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SubmitOrderRequest", description = "Request Class for Submit Order", type = "object")
public class SubmitOrderRequest
{
	@Schema(name = "justificationText", description = "Justification text for Routing order.", type = "String", example = "Justification Text")
	public String justificationText ;
}
