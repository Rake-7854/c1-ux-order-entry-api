/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date                    Modified By             JIRA#                       Description
 *  --------                -----------             -----------------------     --------------------------------
 *  03/13/23                C Porter                CAP-39179                   Refactor setSuccess for BaseResponse
 */
package com.rrd.c1ux.api.models;

import javax.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="FakeResponse", description = "Demo class which just contains some field but extends the new base response", type = "object")
public class FakeResponse extends BaseResponse {
	@Schema(name ="someField", description = "Just some sample field with a default of YMCA", type = "string", example="YMCA")
	@Size(min=0)
	private String someField = "YMCA";
}
