/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#														Description
 * 	--------	-----------		-------------------------------------------------------		---------------------
 *	08/02/22	Sakthi M		C1UX-BE - Remove specific item from shopping cart detail	Initial creation
 *	08/10/22	A Salcedo		CAP-35024													Added userIPAddress and backOrderWarned.
 */
package com.rrd.c1ux.api.models.shoppingcart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CORemoveSpecificItemRequest {
	String lineNumber;
	String userIPAddress = null;
	boolean backOrderWarned = false;
}
