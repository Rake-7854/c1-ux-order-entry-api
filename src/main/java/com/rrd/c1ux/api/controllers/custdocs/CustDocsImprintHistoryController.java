/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By			JIRA#						Description
 * 	--------				-----------			----------		--------------------------------
 * 	07/09/24				A Boomker			CAP-46538		Initial version
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
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBasicImprintHistorySearchRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocImprintHistorySearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsImprintHistoryService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CustDocsImprintHistoryController")
@Tag(name = "Cust Docs Imprint History APIs")
public class CustDocsImprintHistoryController  extends CustDocsBaseController {
	private static final Logger logger = LoggerFactory.getLogger(CustDocsImprintHistoryController.class);

	protected CustomDocsImprintHistoryService custDocsImprintHistoryService;

	@Override
	protected CustomDocsBaseService getService() {
		return custDocsImprintHistoryService;
	}

	public CustDocsImprintHistoryController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsImprintHistoryService service) {
		super(tokenReader, sessionReader);
		custDocsImprintHistoryService = service;
	}

	@PostMapping(value = RouteConstants.CUST_DOCS_IMPRINT_HISTORY_BASIC_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Returns list of qualifying imprint history options for the UI search terms")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocImprintHistorySearchResponse> basicImprintHistorySearch(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocBasicImprintHistorySearchRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocImprintHistorySearchResponse response = custDocsImprintHistoryService.basicImprintHistorySearch(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        custDocsImprintHistoryService = getClassSpecificService(custDocsImprintHistoryService, sc);
 	}

	// moved here from UI controller to keep all history APIs together
	@PostMapping(value = RouteConstants.CUST_DOCS_IMPRINT_HISTORY, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Get imprint history")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> loadImprintHistory(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocLoadImprintHistoryRequest request) throws AtWinXSException {
		SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocBaseResponse response = custDocsImprintHistoryService.getSelectedImprintHistory(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

}
