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
 *  10/10/23	Satishkumar A	CAP-44196	C1UX API - Create api to retrieve edoc for Storefront
 */

package com.rrd.c1ux.api.models.edoc;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="EdocUrlResponse", description = "Response Class to retrive edoc Url", type = "object")
public class EdocUrlResponse extends BaseResponse{
	
	@Schema(name ="edocUrl", description = "This field returns edoc Url if its external link or empty if the edoc is in /xs2files/CustomerFiles/[SITE_LOGIN_ID]/edocs folder", type = "String")
	private String edocUrl ="" ; 
}
