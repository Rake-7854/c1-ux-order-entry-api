/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		--------------		--------------------------------
 *	09/28/22	A Boomker		CAP-36084			Modify API response for categories so top level cats have label
 */
package com.rrd.c1ux.api.services.singleitem;

import java.util.Collection;

import com.rrd.c1ux.api.models.singleitem.CategoryListing;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsResponse;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
public interface SingleItemDetailsService {

	/**
	 * 
	 * @param sc - {@link SessionContainer}
	 * @param ttsessionid - {@link String}
	 * @return - This is a Collection of {@link SingleItemDetailsResponse}
	 * @throws AtWinXSException 
	 */
	SingleItemDetailsResponse retrieveSingleItemDetails(SessionContainer sc, String ttsessionid, SingleItemDetailsRequest idr, CatalogItemRetriveServices catalogItemRetriveServices)
			throws AtWinXSException;

	// CAP-36083
	Collection<CategoryListing> generateCategoryTree(CatalogItem items, AppSessionBean appSessionBean)
			throws AtWinXSException;
}