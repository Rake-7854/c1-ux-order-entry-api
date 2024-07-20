/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/10/23				C Codina				CAP-41549					Initial Version
 */
package com.rrd.c1ux.api.models.messages;

import java.util.ArrayList;
import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "CarouselResponse", description = "Response Class for retrieving Carousel Items ", type = "object")
public class CarouselResponse extends BaseResponse{
	
	@Schema(name = "carouselItem", description = "List of Carousel Item class objects.", type = "List")
	protected List<CarouselItem> carouselItem = new ArrayList<>();

}
