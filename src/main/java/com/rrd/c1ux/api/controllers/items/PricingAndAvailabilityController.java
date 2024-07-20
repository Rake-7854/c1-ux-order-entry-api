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
 *  05/19/22    S Ramachandran  CAP-34140   Initial Creation
 *	08/29/22	A Boomker		CAP-35537	Make session optional on all API calls
 */

package com.rrd.c1ux.api.controllers.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.PNARequest;
import com.rrd.c1ux.api.models.items.PNAResponse;
import com.rrd.c1ux.api.services.common.exception.CORPCException;
import com.rrd.c1ux.api.services.items.PNAProcessor;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("PricingAndAvailabilityController")
@RequestMapping(RouteConstants.GET_PNA)
@Tag(name = "items/get-pna")
public class PricingAndAvailabilityController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(PricingAndAvailabilityController.class);
	
	@Autowired
	private PNAProcessor mService;

	protected int getServiceID() {
		
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}


	/**
	 * @param tokenReader {@link TokenReader}
	 * @param sessionReader {@link CPSessionReader}
	 */
	protected PricingAndAvailabilityController(TokenReader tokenReader, CPSessionReader sessionReader) {
		
		super(tokenReader, sessionReader);
	}


	/**
	 *
	 * @param sessionID - {@link String}
	 * @param request - {@link PNARequest}
	 * @return - This will return {@link PNAResponse} 
	 * @throws AtWinXSException {@link AtWinXSException}, CORPCException {@link CORPCException}
	 */
	@PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve Price and Availability of an Item")
	public PNAResponse retrievePNAController(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID,
			@RequestBody PNARequest request) throws AtWinXSException,CORPCException  {

		logger.debug("In PricingAndAvailabilityController");

		SessionContainer mainSession = getSessionContainer(sessionID);

		return mService.processPNA(request,mainSession);
	}

}
