/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	11/27/23	L De Leon			CAP-44467				Initial Version
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
@Schema(name = "OrderOnBehalfSearchRequest", description = "Request Class for retrieving Order On Behalf information of logged in user", type = "object")
public class OrderOnBehalfSearchRequest {

	@Schema(name = "search", description = "Indicates if a search should be performed. "
			+ "If this is true, one of the fields has to have value. If false, no search criteria validation.", type = "boolean")
	private boolean search;

	@Schema(name = "userID", description = "Order On Behalf user citeria for 'User ID'"
			+ " that uses starts with operator. Case insensitive", type = "string", example = "johndoe")
	@Size(min = 0, max = 16)
	private String userID;

	@Schema(name = "profileID", description = "Order On Behalf user citeria for 'Profile ID'"
			+ " that uses contains operator. Case insensitive", type = "string", example = "johndoe")
	@Size(min = 0, max = 128)
	private String profileID;

	@Schema(name = "firstName", description = "Order On Behalf user citeria for 'First Name'"
			+ " that uses contains operator. Case insensitive", type = "string", example = "John")
	@Size(min = 0, max = 25)
	private String firstName;

	@Schema(name = "lastName", description = "Order On Behalf user citeria for 'Last Name'"
			+ " that uses contains operator. Case insensitive", type = "string", example = "Doe")
	@Size(min = 0, max = 25)
	private String lastName;
}