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
 *  09/01/23    S Ramachandran		CAP-41590		API Build - Response Object for PAB all or a search 
 */

package com.rrd.c1ux.api.models.admin;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="PABResponse", description = "Request Class for Personal Address Book", type = "object")
public class PABSearchResponse  extends BaseResponse { 

	@Schema(name ="c1uxAddresses", description = "List of Personal Address", type = "array")
	Collection<C1UXAddress> c1uxAddresses;
	
	@Schema(name ="pabCount", description = "PAB Count excluding Extended and Corporate addresses", type = "int")
	private int pabCount;
}	
		