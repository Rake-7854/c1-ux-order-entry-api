/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By      	JIRA #          Description
 *	--------    -----------			----------		-----------------------------------------------------------
 *  10/13/23    M Sakthi			CAP-44468		Initial version
 *  10/13/23    S Ramachandran		CAP-44515		Retrieve Order Approval Alerts  
 *  10/25/23	Satishkumar A		CAP-44663		C1UX API - Create service to show if there are any alerts for the logged in user
 *  10/31/23	Satishkumar A		CAP-44996		C1UX BE - Create service to show if there are any alerts for the logged in user  
 */

package com.rrd.c1ux.api.controllers.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.alert.AlertsResponse;
import com.rrd.c1ux.api.models.alert.CheckAlertsResponse;
import com.rrd.c1ux.api.services.alert.AlertsService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("AlertsController")
@Tag(name = "Alerts APIs")
public class AlertsController extends BaseCPApiController {
	
	private static final Logger logger = LoggerFactory.getLogger(AlertsController.class);
	
	private final AlertsService mAlertService;
	
	protected AlertsController(TokenReader tokenReader, CPSessionReader sessionReader,AlertsService alertService) {
		
		super(tokenReader, sessionReader);
		mAlertService = alertService;
	}

	@Override
	protected int getServiceID() {
		
		return AtWinXSConstant.ADMIN_SERVICE_ID;
	}

	@GetMapping(value =RouteConstants.GET_ALERTS,
	    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	
	@Operation(summary = "Alerts with count details")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<AlertsResponse> getAlerts(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
		
		logger.debug("In getAlerts()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		AlertsResponse response= mAlertService.getAlerts(sc);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	 }
	
	
	@GetMapping(value =RouteConstants.CHECK_ALERTS,
		    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
		
		@Operation(summary = "Service to show if there are any alerts for the logged in user")
		@ApiResponse(responseCode = RouteConstants.HTTP_OK)
		@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
		@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
		@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
		public ResponseEntity<CheckAlertsResponse> checkAlerts(
				@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
			
			logger.debug("In checkAlerts()");
			SessionContainer sc = getSessionContainer(ttsession);
			CheckAlertsResponse response= mAlertService.checkAlerts(sc);
			
			return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
		 }	
}
