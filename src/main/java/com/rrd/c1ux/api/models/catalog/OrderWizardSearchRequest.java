/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By          JIRA#           Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *	02/06/24  	C Codina   			CAP-46723     	Initial Version
 */

package com.rrd.c1ux.api.models.catalog;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrderWizardSearchRequest {
	
	@Schema(name ="attributeValues", description = "An array of OrderWizardSelectedAttributes objects", type = "List")
	List<OrderWizardSelectedAttributes> attributeValues = new ArrayList<>();
	
}
