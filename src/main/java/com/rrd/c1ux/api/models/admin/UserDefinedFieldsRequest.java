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

import java.util.ArrayList;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="UserDefinedFieldsRequest", description = "Request Class for update User defined fields", type = "object")
public class UserDefinedFieldsRequest {
	
	@Schema(name ="c1UserDefinedFields", description = "List of User Denifed Field objects with values", type = "array")
	ArrayList<C1UserDefinedField> c1UserDefinedFields;
}
