/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	11/27/23	L De Leon			CAP-44467				Initial Version
 *	12/19/23	L De Leon			CAP-45939				Added list of shared ID users
 */
package com.rrd.c1ux.api.models.orders.oob;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.gwt.orderonbehalf.client.OrderOnBehalfSearchResult;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "OrderOnBehalfSearchResponse", description = "Request Class for retrieving Order On Behalf information of logged in user", type = "object")
public class OrderOnBehalfSearchResponse extends BaseResponse {

	@Schema(name ="count", description = "The number of returned OOB users for the user", type = "int", example="6")
	private int count;

	@Schema(name = "oobUsers", description = "List of Order On Behalf users for the logged in user", type = "array")
	private List<OrderOnBehalfSearchResult> oobUsers;

	@Schema(name = "oobSharedIdUsers", description = "List of Order On Behalf shared ID users for the logged in user", type = "array")
	private List<NameValuePair<String>> oobSharedIdUsers;

	@Schema(name ="userID", description = "'User ID' of the selected OOB user", type = "string", example="johndoe")
	private String userID;

	@Schema(name ="profileID", description = "'Profile ID' of the selected OOB user", type = "string", example="johndoe")
	private String profileID;

	@Schema(name ="firstName", description = "'First Name' of the selected OOB user", type = "string", example="John")
	private String firstName;

	@Schema(name ="lastName", description = "'Last Name' of the selected OOB user", type = "string", example="Doe")
	private String lastName;

	@Schema(name ="emailAddr", description = "'Email Address' of the selected OOB user", type = "string", example="johndoe@rrd.com")
	private String emailAddr;

	@Schema(name ="phone", description = "'Phone Number' of the selected OOB user", type = "string", example="1234567890")
	private String phone;

	@Schema(name = "inOOBMode", description = "Indicates that user is in OOB mode", type = "boolean")
	private boolean inOOBMode;
}