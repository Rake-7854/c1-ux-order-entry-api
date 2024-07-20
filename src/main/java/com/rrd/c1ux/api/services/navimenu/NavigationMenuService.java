/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#						Description
 * 	--------	-----------				-----------------------		--------------------------------
 *  08/23/23	Krishna Natarajan		CAP-42607 					Added a parameter 'catalogs' in getMenuOrder to perform null check and build menu accordingly
 */

package com.rrd.c1ux.api.services.navimenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rrd.c1ux.api.models.catalog.ItemsForNaviMenu;
import com.rrd.custompoint.ui.menu.MenuGroup;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;
/**
 * @author Krishna Natarajan
 *
 */
public interface NavigationMenuService {

	/**
	 * @param sc {@link SessionContainer}
	 * @return Collection object
	 * @throws AtWinXSException
	 */
	public Collection<MenuGroup> getNavigationMenu(SessionContainer sc) throws AtWinXSException;
	
	/**
	 * @param catalogs
	 * @return array list {@link ArrayList} of menu 
	 */
	//CAP-39245
	public List<CatalogMenuWithNavigationMenu> buildMenu(List<Object> catalogs,SessionContainer sc,boolean aMenuTools) throws AtWinXSException;
	public List<String> getMenuOrder(boolean isAllowCatalogBrowse,boolean hasOrdersService,boolean hasOrderSearchService,boolean showSaveOrdersSubMenu,boolean aMenuTools,boolean hasReport,List<Object> catalogs);
	public ItemsForNaviMenu buildSubMenuList(SessionContainer sc, int id, String lable, String routerLink)
			throws AtWinXSException;
	
}
