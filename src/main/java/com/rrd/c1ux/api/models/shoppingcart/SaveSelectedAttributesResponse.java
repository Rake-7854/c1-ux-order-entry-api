/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/20/24	T Harmon			CAP-46543				Added code for EOO
 */

package com.rrd.c1ux.api.models.shoppingcart;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.eoo.EOOAttribute;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="SaveSelectedAttributesResponse", description = "Response Class for saving eoo attributes", type = "object")
public class SaveSelectedAttributesResponse extends BaseResponse {	
	@Schema(name ="attributes", description = "The List holding the attribute details", type = "object")
	List<EOOAttribute> attributes;	
}
