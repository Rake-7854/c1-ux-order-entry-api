/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		-------------------------------------------------------------
 *	04/10/23				S Ramachandran			CAP-38159					Initial Version, Generic criteria Field class
 */

package com.rrd.c1ux.api.models.common;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="GenericSearchCriteria", description = "Class to define search criteria in Key-Value Pairs", type = "object")
public class GenericSearchCriteria  implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Schema(name ="criteriaFieldKey", description = "Criteria Field Key", type = "string", example="country")
	private String criteriaFieldKey;
	
	@Schema(name ="criteriaFieldValue", description = "Criteria Field Value", type = "string", example="USA")
	private String criteriaFieldValue;
	
}
