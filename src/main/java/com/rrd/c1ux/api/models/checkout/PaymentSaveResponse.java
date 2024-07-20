/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	--------------		----------		-------------------------------------------------
 *	09/20/23	S Ramachandran		CAP-43668		Response for Payment Information Save in checkout
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
@Schema(name = "PaymentSaveResponse", description = "Response Class for Save Payment Information", type = "object")
public class PaymentSaveResponse extends BaseResponse {

	
}