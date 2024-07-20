/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *															Initial creation
 */
package com.rrd.c1ux.api.models.itemqtyvalidation;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemQtyValidationResponse {
	String status;
	String statusMessage;
}
