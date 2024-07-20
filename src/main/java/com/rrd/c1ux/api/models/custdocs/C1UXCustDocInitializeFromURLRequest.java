/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	03/14/24		A Boomker			CAP-46526				Initialize request
 */

package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1UXCustDocInitializeFromURLRequest", description = "Request to initialize a User Interface from a CustomPoint style URL", type = "object")
public class C1UXCustDocInitializeFromURLRequest {

	@Schema(name = "url", description = "The URL that points to CP or was otherwise populated from custom html/tags. It must contain a ui.cp and then be followed by the parameters. The parsing of the URL parameters will begin after the first question mark.", type = "String",
			example = "https://dev.storefront.connectone.rrd.com/cp/orders/customdocs/ui.cp?ttsessionid=WVorc1RneXRBQXBHd3NkZ0JzRS81K3JOVWRmY0pBamdYS2h5ZW5JRkplNTdhQVFLbVo1RDZnPT0=&cdEntryPoint=NEWREQ&txtProjectId=12354&hdnUINumber=3&hdnUIVersion=1")
	protected String url = AtWinXSConstant.EMPTY_STRING;
}
