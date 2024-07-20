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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(name = "CarouselItem", description = "Object containing Carousel Item message content", type = "object")
public class CarouselItem {
	
	@Schema(name = "msgTitle", description = "String containing Carousel Message Title", type = "String")
	protected String msgTitle;
	@Schema(name = "msgEffDate", description = "String containing Carousel Message Effective Date", type = "String")
	String msgEffDate;
	@Schema(name = "msgContent", description = "String containing Carousel Message Content", type = "String")
	String msgContent;
 
	
}
