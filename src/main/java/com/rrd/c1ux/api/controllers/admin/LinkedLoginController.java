/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/27/24	L De Leon			CAP-49609				Initial Version
 *	05/28/24	Satishkumar A		CAP-49610				C1UX API - Create API to login as a linked login ID/user
 *	05/31/24	Satishkumar A		CAP-49731				C1UX BE - Create API to login as a linked login ID/user
 */
package com.rrd.c1ux.api.controllers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.admin.LinkedLoginResponse;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserRequest;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserResponse;
import com.rrd.c1ux.api.services.admin.LinkedLoginService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.tt.arch.TTException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("LinkedLoginController")
@Tag(name = "Linked Login User API")
public class LinkedLoginController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinkedLoginController.class);

	private final LinkedLoginService linkedLoginService;

	protected LinkedLoginController(TokenReader tokenReader, CPSessionReader sessionReader,
			LinkedLoginService linkedLoginService) {
		super(tokenReader, sessionReader);
		this.linkedLoginService = linkedLoginService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@GetMapping(value = RouteConstants.GET_LINKED_LOGINS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve user information and linked logins")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<LinkedLoginResponse> getLinkedLogins(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getLinkedLogins()");
		SessionContainer sc = getSessionContainer(ttsession);
		LinkedLoginResponse response = linkedLoginService.getLinkedLogins(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	//CAP-49610 CAP-49731
	@PostMapping(value=RouteConstants.LOGIN_LINKED_USER, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Linked Login User information")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<LoginLinkedUserResponse> loginLinkedUser(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID, 
			@RequestBody LoginLinkedUserRequest liginLinkedUserRequest) throws AtWinXSException, TTException {

		LOGGER.debug("In loginLinkedUser()");
		
		SessionContainer sc = getSessionContainer(sessionID);
		LoginLinkedUserResponse response = linkedLoginService.loginLinkedUser(sc, liginLinkedUserRequest);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}