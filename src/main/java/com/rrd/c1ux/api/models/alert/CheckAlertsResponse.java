/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By      	JIRA #          Description
 *	--------    -----------			----------		-----------------------------------------------------------
 *  10/25/23	Satishkumar A		CAP-44663		C1UX API - Create service to show if there are any alerts for the logged in user
 *  10/31/23	Satishkumar A		CAP-44996		C1UX BE - Create service to show if there are any alerts for the logged in user
 */
package com.rrd.c1ux.api.models.alert;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CheckAlertsResponse", description = "Response Object to show if there are any alerts for the logged in user", type = "object")
public class CheckAlertsResponse extends BaseResponse {

	@Schema(name ="alertsExist", description = "This will be true if the user has any alerts, or false if they don’t.", type = "boolean")
	private boolean alertsExist = false;
	
	@Schema(name ="count", description = "This flag will holding the alert count.", type = "int")
	private int count;
	

}
