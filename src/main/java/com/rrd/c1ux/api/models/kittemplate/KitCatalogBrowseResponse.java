/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/26/24	N Caceres			CAP-50309				Initial Version
 */
package com.rrd.c1ux.api.models.kittemplate;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Schema(name = "KitCatalogBrowseResponse", description = "Kit Template Browse Catalog Response object", type = "object")
public class KitCatalogBrowseResponse extends BaseResponse {
}
