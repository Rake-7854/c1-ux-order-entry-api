/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/22/24				R Ruth			    	CAP-42226 					Controller for lists
 *	05/28/24				A Boomker				CAP-48604					Add save new list API
 */
package com.rrd.c1ux.api.controllers.custdocs;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXSaveListResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsListService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CustDocListController")
@Tag(name = "Cust Docs List")
public class CustDocListController extends CustDocsBaseController {
	private static final Logger logger = LoggerFactory.getLogger(CustDocListController.class);

	protected CustomDocsListService custDocsListService;

	protected CustDocListController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsListService service) {
		super(tokenReader, sessionReader);
		custDocsListService = service;
	}

	@Override
	protected CustomDocsBaseService getService() {
		return custDocsListService;
	}

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        custDocsListService = getClassSpecificService(custDocsListService, sc);
 	}

	@GetMapping(value = RouteConstants.CUST_DOCS_GET_LISTS_API)
	@Operation(summary = "Return a list of list beans")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<C1UXCustDocListResponse> getLists(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocListResponse response = custDocsListService.getListsApi(sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	// CAP-48604-add save list
	@GetMapping(value = RouteConstants.SAVE_CUST_DOC_LIST)
	@Operation(summary = "Upload list and save within custom docs only")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<C1UXSaveListResponse> saveNewList(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
		logger.debug("In saveNewList()");
		SessionContainer sc = getSessionContainer(ttsession);
		checkSessionForClass(sc);
		C1UXSaveListResponse response = custDocsListService.saveNewList(sc);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
