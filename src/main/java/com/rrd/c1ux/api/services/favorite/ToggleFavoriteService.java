
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  08/09/23 	Satishkumar A		CAP-42720		C1UX API - API Build - Favorite Toggle Call
 */
package com.rrd.c1ux.api.services.favorite;

import com.rrd.c1ux.api.models.favorite.ToggleFavoriteResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface ToggleFavoriteService {

	public ToggleFavoriteResponse toggleFavorite(SessionContainer sc, String custItem, String vendorItem)
			throws AtWinXSException;
}
