/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 * 	06/04/24	A Boomker			CAP-42231		Initial version
 */
package com.rrd.c1ux.api.models.custdocs;

import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="C1UXCustDocMappedDataRequest", description = "Request for the mapped data of type invalid or valid for a single pagination page made from the list data page.", type = "object")
public class C1UXCustDocMappedDataRequest {
	@Min(1)
	@Schema(name = "pageNum", description = "The page number of the request. This must be greater than 0.", type = "number", example = "11111")
   	private int pageNum = 1;
	@Schema(name="validValues", description = "Flag indicating whether the request is for the valid rows or the invalid rows.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean validValues = true;

}
