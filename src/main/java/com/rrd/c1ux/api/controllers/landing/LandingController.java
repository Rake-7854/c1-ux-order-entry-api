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
 *  09/13/22    S Ramachandran  CAP-35424   Added API Controller, method to Get Landing page information 
 */

package com.rrd.c1ux.api.controllers.landing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.addtocart.landing.LandingResponse;
import com.rrd.c1ux.api.services.landing.LandingService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("LandingController")
public class LandingController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(LandingController.class);
	
	@Autowired
	private LandingService mService;

	protected LandingController(TokenReader tokenReader, CPSessionReader sessionReader) {

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
	//CAP-35424 - API Controller end point to get landing page information
	@Tag(name = "landing/getlandinginfo")
	@GetMapping(value = RouteConstants.GET_LANDING_INFO, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get LandingResponse for a SiteID, buID of logged-In user")
	public LandingResponse getStandardStylefileIncludeURL(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession
			) throws AtWinXSException {

		logger.debug("In LandingController");

		SessionContainer mainSession = getSessionContainer(ttsession);

		return mService.loadLanding(mainSession);

	}
	
}	