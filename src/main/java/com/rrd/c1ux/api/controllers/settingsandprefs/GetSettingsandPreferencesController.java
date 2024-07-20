/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	04/27/22	Krishna Natarajan	CAP-34022	    Created controller as per the requirement to fetch the settings and preferences for catalog navigation
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 *  10/10/22	Krishna Natarajan	CAP-36438/36448 Change the return type to Object Modify the API to get the required JSON response for FE
 */

package com.rrd.c1ux.api.controllers.settingsandprefs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.settingsandprefs.CatalogUtilityNavigationInfo;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.settingsandprefs.CatalogUtilityNavigationInfoService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * @author Krishna Natarajan
 *
 */
@RestController("GetSettingsandPreferencesController")
@RequestMapping(RouteConstants.GET_SETTINGS_AND_PREFERENCES)
@Tag(name = "api/settingsandprefs")
public class GetSettingsandPreferencesController extends BaseCPApiController {
	
private CatalogUtilityNavigationInfoService serviceSettingandPrefs;

private static final Logger logger = LoggerFactory.getLogger(GetSettingsandPreferencesController.class);

/**
 * @param tokenReader {@link TokenReader}
 * @param sessionReader {@link CPSessionReader}
 */
protected GetSettingsandPreferencesController(TokenReader tokenReader, CPSessionReader sessionReader, CatalogUtilityNavigationInfoService sSettingandPrefs) 
{
        super(tokenReader, sessionReader);
        serviceSettingandPrefs=sSettingandPrefs;
}

/**
 *@return a constant ORDERS_SERVICE_ID AtWinXSConstant {@link AtWinXSConstant}
 */
protected int getServiceID() {
	return AtWinXSConstant.ORDERS_SERVICE_ID;
}

/**
 * 
 * The method to call the service method to get the ArrayList of preferences
 * 
 * @param ttsession {@link String}
 * @return an object, list of the fields, preferences, custom references {@link Object}
 * @throws AtWinXSException
 */
@GetMapping(
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
@Operation(
    summary = "Get settings and preferences")
public CatalogUtilityNavigationInfo getSettingsandPreferences(@RequestHeader(value = "ttsession", required=false) String ttsession) throws AtWinXSException {
    logger.debug("In getSettingsandPreferences");
    
    SessionContainer sc = null;
    

		sc = getSessionContainer(ttsession);

	return serviceSettingandPrefs.getCatalogUtilityNavigationInfoService(sc);
}
}
