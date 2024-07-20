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
 *  10/03/22	A Boomker		CAP-35542	Change response object
 *	08/30/23	A Boomker		CAP-43405	Fixing item in cart flags for customizable items
 */
package com.rrd.c1ux.api.models.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversalSearch {

    private String description;
    private String itemNum;
    private String wcsItemNum;
    private String catLineNum;
    private String imageUrl;
    private String defaultQty;
    private String addToCartAllowed;
    private String itemInCart;
    private String iconPlusUDF;
    private boolean customizable;
}
