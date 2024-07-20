/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/14/23				A Salcedo				CAP-38152					Initial Version
 */
package com.rrd.c1ux.api.models.checkout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderHeaderInfoRequest", description = "Request Class for Order Header Info to load the Order Header Info modal", type = "object")
public class OrderHeaderInfoRequest 
{	
	@Schema(name = "review", description = "Flag indicating the values loaded should assume this is called from the review page and has shipping information.", type = "boolean")
	boolean review = false;
}
