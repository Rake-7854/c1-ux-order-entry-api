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
 *  05/05/22    S Ramachandran  CAP-34048   Initial Creation, Get User Full Profile 
 *	08/29/22	A Boomker		CAP-35537	Make session optional on all API calls
 *	05/03/23	Sakthi M		CAP-39612   Schema changes
 *  05/11/23    Sakthi M        CAP-40524	C1UX BE - API Change - Convert Full Profile API to only return the user's own information
 *  12/07/23    S Ramachandran  CAP-45485   Fix code to only search for the originator profile when doing self admin in OOB
 */

package com.rrd.c1ux.api.controllers.users;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.users.UserFullProfileResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.users.UserFullProfileProcessor;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("UserFullProfileController")
@Tag(name = "User Full Profile")
public class UserFullProfileController extends BaseCPApiController {

    private static final Logger logger = LoggerFactory.getLogger(UserFullProfileController.class);

	@Autowired
	private UserFullProfileProcessor mService;
	
	
    
	protected UserFullProfileController(TokenReader tokenReader, CPSessionReader sessionReader) {
		
		super(tokenReader, sessionReader);
	}
	
	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ADMIN_SERVICE_ID;
	}

    @GetMapping(value = RouteConstants.USERS_FULL_PROFILE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve User full profile")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<UserFullProfileResponse> retrieveUserFullProfile(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID) throws AtWinXSException, IllegalAccessException, InvocationTargetException {

		logger.debug("In User Full Profile Controller");
		SessionContainer mainSession = getSessionContainer(sessionID);
		//CAP-45485 - Force to use actual profile(Requestor/Orginator) from session for any other action except SelfAdmin    
		boolean useOriginatorProfile = false;
		UserFullProfileResponse response=mService.processUserFullProfile(mainSession, useOriginatorProfile);
		return new ResponseEntity<>(response,(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
    
    //CAP-45485 - 	Method to retrieve Full User Profile of Originator
    @GetMapping(value = RouteConstants.FULL_USER_PROFILE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Retrieve full user profile originator for Self Admin")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<UserFullProfileResponse> retrieveFullUserProfileOriginator(
    		@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID) throws AtWinXSException, IllegalAccessException, InvocationTargetException {

    	logger.debug("In User Full Profile(originator) Controller");
    	SessionContainer mainSession = getSessionContainer(sessionID);
    	//CAP-45485 - Force to use originator profile in Self Admin for OOB Mode 
    	boolean useOriginatorProfile = true;
    	UserFullProfileResponse response=mService.processUserFullProfile(mainSession, useOriginatorProfile);
    	return new ResponseEntity<>(response,(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }
    
    @Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
    	return asb.hasService(getServiceID());
	}
  
}