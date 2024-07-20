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
 *  05/19/22    S Ramachandran  CAP-34140   Initial Creation
 */

package com.rrd.c1ux.api.models.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PNARequest {
	
	private String itemNumber;
	private String vendItemNum;
	private int quantity;
	private String selectedUOM;
	private int catalogLineNumber;
	private boolean isCustomDoc;
	
}
