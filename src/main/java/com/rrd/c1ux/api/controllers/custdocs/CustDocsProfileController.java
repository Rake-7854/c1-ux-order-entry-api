/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 * 	11/08/23				A Boomker			CAP-44486		Initial version
 *  11/10/23				A Boomker			CAP-44487		Added handling for load user profile
 *	03/12/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 * 	06/24/24				R Ruth					CAP-46503					Added code for load alternate profile
 */
package com.rrd.c1ux.api.controllers.custdocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUserProfileSearchRequest;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProfileService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CustDocsProfileController")
@Tag(name = "Cust Docs Profile APIs")
public class CustDocsProfileController  extends CustDocsBaseController {
	private static final Logger logger = LoggerFactory.getLogger(CustDocsProfileController.class);

	protected CustomDocsProfileService custDocsProfileService;

	@Override
	protected CustomDocsBaseService getService() {
		return custDocsProfileService;
	}

	public CustDocsProfileController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsProfileService service) {
		super(tokenReader, sessionReader);
		custDocsProfileService = service;
	}

	@PostMapping(value = RouteConstants.CUST_DOCS_USER_PROFILE_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Returns list of qualifying User type profiles for the UI search terms")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocProfileSearchResponse> searchUserProfiles(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocUserProfileSearchRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocProfileSearchResponse response = custDocsProfileService.searchUserProfiles(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@PostMapping(value = RouteConstants.CUST_DOCS_USER_PROFILE_LOAD, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Load user profile for custom document already initialized in session.")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocLoadProfileResponse> loadUserProfile(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocLoadProfileRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocLoadProfileResponse response = custDocsProfileService.loadUserProfile(sc, request);
         return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        custDocsProfileService = getClassSpecificService(custDocsProfileService, sc);
 	}

	// CAP-46503
	@PostMapping(value = RouteConstants.CUST_DOCS_LOAD_ALT_PROFILE_API, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Load alternate profile(s) for custom document already initialized in session.")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	 public ResponseEntity<C1UXCustDocLoadAltProfileResponse> getListProfileDefinitionIDs(@RequestHeader(value = "ttsession", required = false) String ttsession,
	    		@RequestBody C1UXCustDocLoadAltProfileRequest request) throws AtWinXSException {
		SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocLoadAltProfileResponse response = custDocsProfileService.loadAltProfile(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

}
