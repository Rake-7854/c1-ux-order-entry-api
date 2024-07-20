/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  04/19/22    S Ramachandran  CAP-33763   Initial Creation
 */

package com.rrd.c1ux.api.models.items;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.orderentry.vo.TieredPriceVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "PNATieredPriceResponse", description = "Response Class for retrieving PNA Tiered Price", type = "object")
public class PNATieredPriceResponse extends BaseResponse{

	@Schema(name = "tieredPrice", description = "List of Tiered Price", type = "List")
	private TieredPriceVO[] tieredPrice;

}
	