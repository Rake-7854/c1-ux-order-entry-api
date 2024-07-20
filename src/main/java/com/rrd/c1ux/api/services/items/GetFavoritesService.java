/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      ---------------------------
 * 	05/04/22	Krishna Natarajan	CAP-33867	    Created service interface 
 */

package com.rrd.c1ux.api.services.items;

import javax.servlet.ServletContext;

import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.items.FavouriteItems;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

/**
 * @author Krishna Natarajan
 *
 */
public interface GetFavoritesService {
	
	/** Method getFavoriteItemsProcessed(), to process and get the favorite items
	 * @param searchCriteriaBean {@link OEItemSearchCriteriaSessionBean}
	 * @param mfavoriteItems {@link FavoriteItemsMapper}
	 * @param appSessBean {@link AppSessionBean}
	 * @param volatilesSssnBean {@link VolatileSessionBean}
	 * @param userSettings {@link OEResolvedUserSettingsSessionBean}
	 * @param appSession {@link ApplicationSession}
	 * @param volatileSession {@link ApplicationVolatileSession}
	 * @param punchoutSessionBean {@link PunchoutSessionBean}
	 * @return List of Favorite items {@link FavouriteItems}
	 * @throws AtWinXSException
	 */
	public CatalogItemsResponse getFavoriteItemsProcessed(SessionContainer sc,  FavoriteItemsMapper mfavoriteItems, ServletContext servletContext) throws AtWinXSException;
}
