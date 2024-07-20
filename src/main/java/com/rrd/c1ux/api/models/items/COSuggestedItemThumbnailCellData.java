/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#										Description
 * 	--------	-----------		----------------------------------------	------------------------------
 *	06/05/2024	Sakthi M		CAP-49782	               					 Initial creation
 *
 */

package com.rrd.c1ux.api.models.items;

import com.rrd.custompoint.gwt.common.cell.ItemThumbnailCell.ItemThumbnailCellData;

public interface COSuggestedItemThumbnailCellData extends ItemThumbnailCellData
{
	String getSuggestedBecauseCol();
	String getInStockCol();
}
