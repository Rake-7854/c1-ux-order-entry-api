/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 *  12/05/22    M Sakthi			CAP-37387       Fix Unauthorized API Exception Handling - item add to cart, navigation menu, get cart items
 */
package com.rrd.c1ux.api.controllers.navimenu;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.services.navimenu.NavigationMenuService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.ui.menu.MenuGroup;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.tt.arch.TTException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Krishna Natarajan
 *
 */
@RestController("NavigationMenu")
@RequestMapping(RouteConstants.GET_NAVIGATION_MENU)
@Tag(name = "api/navigationmenu")
public class NavigationMenuController extends BaseCPApiController{

NavigationMenuService navMenuService;
private static final Logger logger = LoggerFactory.getLogger(NavigationMenuController.class);

/**
 * @param tokenReader
 * @param sessionReader
 */
protected NavigationMenuController(TokenReader tokenReader, CPSessionReader sessionReader, NavigationMenuService snavMenuService) {
	super(tokenReader, sessionReader);
	navMenuService=snavMenuService;
}

@Override
protected int getServiceID() {
	return AtWinXSConstant.ORDERS_SERVICE_ID;	
}

/**
 * @param ttsession {@link String}
 * @return the collection of menu groups Collection<MenuGroups>
 * @throws AtWinXSException
 * @throws TTException 
 */
	@GetMapping(
	        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	    @Operation(
	        summary = "Get navigation menu items for the navigation tab")
	public Collection<MenuGroup> getNavigationMenuList(@RequestHeader(value = "ttsession", required=false) String ttsession) throws AtWinXSException {
			SessionContainer sc = getSessionContainer(ttsession);
			return navMenuService.getNavigationMenu(sc);
	}
}
