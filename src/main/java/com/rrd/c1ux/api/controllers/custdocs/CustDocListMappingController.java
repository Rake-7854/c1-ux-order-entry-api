/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/15/24				R Ruth			    	CAP-42228 					Controller for lists
 *	05/30/24				A Boomker				CAP-42230					save list mapping is now here
 * 	06/04/24				A Boomker				CAP-42231					Adding get mapped data page
 */
package com.rrd.c1ux.api.controllers.custdocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsListMappingService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CustDocListMappingController")
@Tag(name = "Cust Docs List Mapping")
public class CustDocListMappingController  extends CustDocsBaseController{
	private static final Logger logger = LoggerFactory.getLogger(CustDocListMappingController.class);

	protected CustomDocsListMappingService custDocsListMappingService;

	protected CustDocListMappingController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsListMappingService service) {
		super(tokenReader, sessionReader);
		custDocsListMappingService = service;
	}

	@Override
	protected CustomDocsBaseService getService() {
		return custDocsListMappingService;
	}

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        custDocsListMappingService = getClassSpecificService(custDocsListMappingService, sc);
 	}

	@PostMapping(value = RouteConstants.CUST_DOCS_GET_LISTS_MAPPING_API)
	@Operation(summary = "Return a list of list beans")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<C1UXCustDocListForMappingResponse> getListMappingPreview(@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody C1UXCustDocListForMappingRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocListForMappingResponse response = custDocsListMappingService.getListMappings(sc, request.getListId());
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@PostMapping(value = RouteConstants.CUST_DOCS_SAVE_LISTS_MAPPING_API)
	@Operation(summary = "Return a saved list mappings")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<C1UXCustDocSaveListMappingResponse> saveListMapping(@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody C1UXCustDocSaveListMappingRequest request) throws AtWinXSException {
		SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocSaveListMappingResponse response = custDocsListMappingService.saveListMapping(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@PostMapping(value = RouteConstants.CUST_DOCS_GET_MAPPED_DATA_PAGE_API)
	@Operation(summary = "Return up to one page of mapped list data")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<C1UXCustDocMappedDataResponse> getMappedDataPage(@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody C1UXCustDocMappedDataRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocMappedDataResponse response = custDocsListMappingService.getMappedDataPage(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

}
