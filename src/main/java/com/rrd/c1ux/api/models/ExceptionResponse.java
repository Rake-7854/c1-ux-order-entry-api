/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *  Date        Modified By     Issue #     Description
 *  --------    -----------     ----------  -----------------------------------------------------------
 *  10/24/22	C Porter 		CAP-36720	Initial.
 * 
 */

package com.rrd.c1ux.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {

	private String title;
	private String message;
	
}
