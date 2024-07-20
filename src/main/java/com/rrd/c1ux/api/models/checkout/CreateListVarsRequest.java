/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/15/24				M Sakthi				CAP-47777					Initial Version
 */

package com.rrd.c1ux.api.models.checkout;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CreateListVarsRequest", description = "Request Class for CreateListVarsRequest", type = "object")
public class CreateListVarsRequest {
	@Schema(name = "listName", description = "List name of the distribution list", type = "String", example = "Test11")
	@Size(min = 0, max = 25)
	private String listName;
	
	@Schema(name = "listDescription", description = "Discription  of the distribution list", type = "String", example = "Test description")
	@Size(min = 0, max = 2000)
	private String listDescription;
	
	@Schema(name = "headers", description = "Header for Distribution List. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean headers=false;
	
	@Schema(name = "sharedList", description = "Shared List for Distribution list. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean sharedList=false;

}
