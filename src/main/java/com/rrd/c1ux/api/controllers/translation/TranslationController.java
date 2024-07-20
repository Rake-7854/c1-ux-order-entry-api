/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  12/23/2022	E Anderson		CAP-36154					Initial.  BE Updates for translation.
 *  01/10/2023  E Anderson      CAP-36154                   Derive the appNamePrefix from XST522.
 */
package com.rrd.c1ux.api.controllers.translation;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("TranslationController")
public class TranslationController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranslationController.class);
	
	@Autowired
	private TranslationService translationService;

	protected TranslationController(TokenReader tokenReader, CPSessionReader sessionReader) {
		super(tokenReader, sessionReader);
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.HOMEPAGE_SERVICE_ID;
	}
	
	@Tag(name = "gettranslationforview")
	@GetMapping(value = RouteConstants.GET_TRANSLATION_FOR_VIEW, produces = { MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "Get Translation for a view")
	public Map<String, String> getTranslationForAppPrefixViewName(@PathVariable (value= "viewName", required=true) String viewName, @RequestHeader(value = "ttsession", required=false) String ttsession) throws AtWinXSException {
		Map<String, String> translation = new HashMap<String, String>();
		
		SessionContainer sc = getSessionContainer(ttsession);
		Properties resourceBundleProps = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(), viewName);
		if(null != resourceBundleProps) {
			translation = translationService.convertResourceBundlePropsToMap(resourceBundleProps);
		}
		
		return translation;
	}

}
