/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		--------------			--------------------------------
 *	01/22/24	M Sakthi		CAP-46544				C1UX BE - Modify SingleItemDetailsServiceImpl method to return Attributes for Item
 */

package com.rrd.c1ux.api.models.singleitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeList {
	String attributeValues;
	String attrValueDescription;
}
