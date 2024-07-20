/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			JIRA #			Description
 *	--------	-----------			----------		-----------------------------------------------------------
 *	01/24/2024	R Ruth				CAP-44862		Initial Version
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 */

package com.rrd.c1ux.api.controllers.custdocs;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileApiRequest;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsFileStreamService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("InsertUploadFileApiController")
@Tag(name = "Cust Docs Insert Upload File Stream")
public class InsertUploadFileApiController extends CustDocsBaseController {

	private static final Logger logger = LoggerFactory.getLogger(InsertUploadFileApiController.class);

	protected CustomDocsFileStreamService custDocsStreamingService;
	@Override
	protected CustomDocsBaseService getService() {
		return custDocsStreamingService;
	}

	public InsertUploadFileApiController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsFileStreamService service) {
		super(tokenReader, sessionReader);
		custDocsStreamingService = service;
	}

	@PostMapping(value = RouteConstants.CUST_DOCS_INSERT_UPLOAD_FILE, produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.ALL_VALUE })
	@Operation(summary = "Stream back selected insert or uploaded file from Custom Documents")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<InsertUploadFileResponse> insertUploadFile(@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody InsertUploadFileApiRequest req) throws AtWinXSException {

		logger.debug("Inside insertUploadFile()");

		// validate session
		SessionContainer sc = getSessionContainer(ttsession);
		validateAuthorization(sc);
		validateSession(sc);

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		// setup response
		InsertUploadFileResponse response = custDocsStreamingService.prepareDownload(req, asb);
		return (response.isSuccess()) ? ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
					custDocsStreamingService.getDisposition(req.getFileName()))
		            .contentType(custDocsStreamingService.getMediaType(req.getFileName())).body(response) :
		            	new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}

}
