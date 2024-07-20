/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/04/23	Satishkumar A		CAP-45280				C1UX API - Set OOB Mode for CustomPoint session
 */

package com.rrd.c1ux.api.models.orders.oob;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OOBRequest", description = "Request Class to toggle session between the Order on Behalf and Order for Slef", type = "object")
public class OOBRequest {
	
	@Schema(name = "loginID", description = "Order On Behalf user citeria for 'Login ID'."
			+ " Case insensitive", type = "string", example = "loginid")
	@Size(min = 0, max = 16)
	private String loginID;
	
	@Schema(name = "profileNumber", description = "Order On Behalf user citeria for 'Profile Number'"
			+ " that uses contains operator.", type = "int", example = "123")
	//@Size(min = 0, max = 128)
	private String profileNumber;

	@Schema(name = "profileID", description = "Order On Behalf user citeria for 'Profile ID'"
			+ " that uses contains operator. Case insensitive", type = "string", example = "profileID")
	@Size(min = 0, max = 128)
	private String profileID;
	
	@Schema(name ="isOrderForSelf", description = "Flag indicating if Order On Behalf user citeria for 'Profile ID' or 'Self'", type = "boolean", example = "false")
	boolean isOrderForSelf = false;


}


