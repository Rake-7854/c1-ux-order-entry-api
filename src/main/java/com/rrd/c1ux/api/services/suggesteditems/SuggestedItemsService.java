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

package com.rrd.c1ux.api.services.suggesteditems;

import java.lang.reflect.InvocationTargetException;

import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SuggestedItemsService {
	public CatalogItemsResponse getSuggestedItems(SessionContainer sc, String vItemNum, String cItemNum, String lineNum, boolean isIgnoreSessionSave) throws AtWinXSException, IllegalAccessException, InvocationTargetException;

}