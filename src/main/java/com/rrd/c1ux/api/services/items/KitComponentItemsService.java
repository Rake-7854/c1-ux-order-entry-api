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
 *  06/05/24    S Ramachandran  CAP-49887   Return components in SingleItemDetailsResponse if the item is a kit template
 */

package com.rrd.c1ux.api.services.items;

import java.util.List;

import com.rrd.custompoint.gwt.common.cell.ItemThumbnailCell.ItemThumbnailCellData;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface KitComponentItemsService {

	public List<ItemThumbnailCellData> getKitComponents(SessionContainer sc, String kitCustItemNum,
			String kitVendorItemNum, String kitClassification) throws AtWinXSException;
}
