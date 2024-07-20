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
 *  09/01/22    S Ramachandran  CAP-35358   Get URL for components to load for standard style sheet
 */


package com.rrd.c1ux.api.controllers.styles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.styles.StandardStyleIncludeResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.styles.StyleService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("StandardStyleController")
public class StyleController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(StyleController.class);
	
	@Autowired
	private StyleService mService;

	protected StyleController(TokenReader tokenReader, CPSessionReader sessionReader) {

		super(tokenReader, sessionReader);
	}

	protected int getServiceID() {
		
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	/**
	 * 
	 * @param sessionID - {@link String}
	 * @return - This will return ( @link StandardStyleIncludeResponse) 
	 * @throws AtWinXSException
	 */	
	//CAP-35358 - controller end point to get Standard Style URL for C1UX based on buid, siteId, styleid, usergroup name
	@Tag(name = "styles/getstandardstylefileurl")
	@GetMapping(value = RouteConstants.GET_CSS_LINK, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get standard style url for C1UX with respect to buID for the user logged in")
	public StandardStyleIncludeResponse getStandardStylefileIncludeURL(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession
			) throws AtWinXSException {

		logger.debug("In StyleController");

		SessionContainer mainSession = getSessionContainer(ttsession);

		return mService.getStandardStyleIncludeURL(mainSession);

	}
	
}	