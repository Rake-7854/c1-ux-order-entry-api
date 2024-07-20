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

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name = "SaveManagedFieldsResponse", description = "Response Class for Save manage session list Information", type = "object")
public class SaveManagedFieldsResponse extends BaseResponse {

}