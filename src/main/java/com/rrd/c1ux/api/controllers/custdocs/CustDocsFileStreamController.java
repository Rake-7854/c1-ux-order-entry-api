/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/07/23				A Boomker			    CAP-42225 					Controller for file stream requests
 * 	01/17/24				A Boomker				CAP-44835					Added handling for uploading variable files
 *	03/12/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 */

package com.rrd.c1ux.api.controllers.custdocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsFileStreamService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("CustDocsFileStreamController")
@Tag(name = "Cust Docs File Stream")
public class CustDocsFileStreamController extends CustDocsBaseController {
	private static final Logger logger = LoggerFactory.getLogger(CustDocsFileStreamController.class);

	protected CustomDocsFileStreamService custDocsStreamingService;

	public CustDocsFileStreamController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsFileStreamService service) {
		super(tokenReader, sessionReader);
		custDocsStreamingService = service;
	}

	/**
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 */
	@PostMapping(value = RouteConstants.CUST_DOCS_GET_UPLOADED_FILE_INFO_API)
	@Operation(summary = "Get info on an uploaded file for the popup to display")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocUploadedFileDetailsResponse> getUploadedFileDetails(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocUploadedFileDetailsRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocUploadedFileDetailsResponse response = custDocsStreamingService.getUploadedFileDetails(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	/**
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 */
	@PostMapping(value = RouteConstants.CUST_DOCS_VAR_UPLOAD_FILE_API,  consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Uploads a single file for a single variable")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocUploadVariableFileResponse> uploadVariableFile(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@ModelAttribute C1UXCustDocUploadVariableFileRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocUploadVariableFileResponse response = custDocsStreamingService.uploadVariableFile(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@Override
	protected CustomDocsBaseService getService() {
		return custDocsStreamingService;
	}

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        custDocsStreamingService = getClassSpecificService(custDocsStreamingService, sc);
 	}

}
