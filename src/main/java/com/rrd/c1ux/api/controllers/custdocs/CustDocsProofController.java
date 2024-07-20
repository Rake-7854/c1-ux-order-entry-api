/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/18/23				A Boomker				CAP-42295					Initial Version
 *	07/25/23				A Boomker				CAP-42225					More changes for actual init
 *	07/27/23				A Boomker				CAP-42223					Changes for save
 *	11/13/23				A Boomker				CAP-44426					Added handling for update working proof
 *	12/04/23				A Boomker				CAP-45654					Force save when event calls for always save
 *	03/12/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 *  04/03/24				A Boomker				CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.controllers.custdocs;

import java.util.Map;

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

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUIPageSubmitRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocWorkingProofResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseServiceImpl;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProofingService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CustDocsProofController")
@Tag(name = "Cust Docs Proof API")
public class CustDocsProofController  extends CustDocsBaseController {
	private static final Logger logger = LoggerFactory.getLogger(CustDocsProofController.class);

	protected CustomDocsProofingService custDocsProofService;
	protected CustomDocsService custDocsUIService;

	@Override
	protected CustomDocsBaseService getService() {
		return custDocsUIService;
	}

	public CustDocsProofController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsProofingService proofService,
			CustomDocsService uiService) {
		super(tokenReader, sessionReader);
		custDocsProofService = proofService;
		custDocsUIService = uiService;
	}

	@GetMapping(value = RouteConstants.CUST_DOCS_CHECK_IMAGE_PROOF_STATUS, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Checks status for current image proof transaction ID in session")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocProofStatusResponse> checkImageProofStatus(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocProofStatusResponse response = custDocsProofService.getCurrentImageProofStatus(sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@PostMapping(value = RouteConstants.CUST_DOCS_GET_PROOF_URL, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Return the URL to look the proof image or other proof type for the current UI in session")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocProofLinkResponse> getProofUrl(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocProofLinkRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocProofLinkResponse response = custDocsProofService.getProofLink(sc, request);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@PostMapping(value = RouteConstants.CUST_DOCS_UPDATE_WORKING_PROOF, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Submit current UI Page for to save without validation for the working proof custom document already initialized in session. Action will be to generate a new working proof and send that back.")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocWorkingProofResponse> updateWorkingProof(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocUIPageSubmitRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        Map<String, String> uiRequest = getParametersFromCustDocAPICall(request);
        uiRequest.put(CustomDocsBaseServiceImpl.EVENT_ACTION_PARAM, UserInterface.UIEvent.WORKING_PROOF.toString());
        custDocsUIService.setDirtyFlag(uiRequest);

        custDocsUIService.performPageSubmitAction(sc, uiRequest);
        C1UXCustDocWorkingProofResponse response = custDocsProofService.getWorkingProof(sc, uiRequest);

         return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        custDocsProofService = getClassSpecificService(custDocsProofService, sc);
        custDocsUIService = getClassSpecificService(custDocsUIService, sc);
	}

	public CustomDocsProofingService getProofingService() {
		return this.custDocsProofService;
	}

}
