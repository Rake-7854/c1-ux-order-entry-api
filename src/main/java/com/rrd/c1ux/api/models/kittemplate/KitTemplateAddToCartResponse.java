/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/18/24	Sakthi M			CAP-50145				Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "KitTemplateAddToCartResponse", description = "Kit Template Add To Cart Response object", type = "object")
public class KitTemplateAddToCartResponse extends BaseResponse{
	private Collection<ComponentItemErrors>  componentItemErrors;
}
