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
 * 	07/10/23	Sakthi M			CAP-37901		Initial version
 */

package com.rrd.c1ux.api.models.admin;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="C1UserDefinedField", description = "Class for update UserDefinedFields", type = "object")
public class C1UserDefinedField {
	@Schema(name ="udfFieldNumber", description = "UDF Field number", type = "int", example="1")
	int udfFieldNumber;
	@Schema(name ="udfValueText", description = "UDF Value", type = "string", example="Test UDF")
	@Size(min=0, max=50)
	String udfValueText;
	
}


