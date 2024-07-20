/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 *  09/01/23    S Ramachandran		CAP-41590		API Build - call admin service to get PAB all or a search 
 *  09/04/23    M Sakthi			CAP-41593		API Build - call admin service to Save PAB  
 *	09/04/23	L De Leon			CAP-41595		Added delete PAB address  
 *	09/07/23	L De Leon			CAP-43631		Modified deletePABAddress() to call service to delete PAB addresses
 *	09/07/23	S Ramachandran		CAP-43630		Added service to return PAB entries - all or a search
 *	10/03/23	L De Leon			CAP-44059		Added export PAB addresses
 *	10/06/23	L De Leon			CAP-44422		Modified exportPABAddresses() to call service to export PAB addresses
 *	11/07/23	S Ramachandran		CAP-44961 		USPS validation in save PAB and to show suggested address
 *	12/18/23	M Sakthi			CAP-45544		C1UX BE - Fix code to only search/edit/save for the originator PAB addresses in OOB mode
 *	02/08/24	M Sakthi			CAP-46964		C1UX BE - Code Fix - OOB - during checkout - PAB to bring requestor PAB address
 */

package com.rrd.c1ux.api.controllers.admin;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.PABDeleteRequest;
import com.rrd.c1ux.api.models.admin.PABDeleteResponse;
import com.rrd.c1ux.api.models.admin.PABExportResponse;
import com.rrd.c1ux.api.models.admin.PABImportRequest;
import com.rrd.c1ux.api.models.admin.PABImportResponse;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.admin.PABSearchRequest;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("SelfAdminPABController")
@Tag(name = "Self Admin PAB APIs")
public class SelfAdminPABController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(SelfAdminPABController.class);

	private final SelfAdminService mSelfAdminService;

	/**
	 * @param tokenReader   {@link TokenReader}
	 * @param sessionReader {@link CPSessionReader}
	 */
	protected SelfAdminPABController(TokenReader tokenReader, CPSessionReader sessionReader,
			SelfAdminService selfAdminService) {
		super(tokenReader, sessionReader);
		mSelfAdminService = selfAdminService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ADMIN_SERVICE_ID;
	}

	// CAP-41590 - Self Admin - Get PAB all or a search
	/**
	 * @param ttsession {@link String}
	 * @return PABSearchResponse {@link PABSearchResponse}
	 * @throws AtWinXSException
	 */
	@PostMapping(value = RouteConstants.SEARCH_PAB, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get list of Personal Addresses for a Search Criteria")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<PABSearchResponse> searchPAB(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody PABSearchRequest request) throws AtWinXSException {

		logger.debug("In getPAB()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		AppSessionBean appSessionBean=sc.getApplicationSession().getAppSessionBean();
		
		//CAP-45544
		boolean useOriginator=false;
		
		//CAP-46964
		OEResolvedUserSettingsSessionBean settings = AdminUtil.getUserSettings(new LoginVOKey(appSessionBean.getSiteID(), appSessionBean.getOriginatorProfile().getLoginID()), appSessionBean.getSessionID(), appSessionBean.getCustomToken());
		if (settings.isAllowOrderOnBehalf() && appSessionBean.getRequestorProfile()!=null && !request.getCallingFrom().isEmpty() &&
				request.getCallingFrom().equalsIgnoreCase(ModelConstants.CALLING_FROM_CHECKOUT)){
			
				useOriginator=true;
		}

		PABSearchResponse response = mSelfAdminService.searchPAB(sc, request,useOriginator);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-41593
	@PostMapping(value = RouteConstants.SAVE_PAB, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save the Personal address book")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<PABSaveResponse> savePAB(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody PABSaveRequest request) throws AtWinXSException {
		
		//CAP-44961 - older version without usps address validation 
		boolean uspsValidationFlag = false;
		
		//CAP-45544
		boolean useOriginator=false;
		
		logger.debug("In savePAB()");
		SessionContainer sc = getSessionContainer(ttsession);
		
		PABSaveResponse response = mSelfAdminService.savePABAddress(sc,request,uspsValidationFlag,useOriginator);	
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-44961
	@PostMapping(value = RouteConstants.SAVE_PAB_V1, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save the Personal address book")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<PABSaveResponse> savePABV1(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody PABSaveRequest request) throws AtWinXSException {

		//CAP-44961 - older version with usps address validation
		boolean uspsValidationFlag = true;
		
		//CAP-45544
		boolean useOriginator=false;
		
		logger.debug("In savePAB()");
		SessionContainer sc = getSessionContainer(ttsession);
		PABSaveResponse response = mSelfAdminService.savePABAddress(sc,request,uspsValidationFlag,useOriginator);	
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-41595
	@PostMapping(value = RouteConstants.DELETE_PAB, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Delete address/es in the Personal address book")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<PABDeleteResponse> deletePABAddress(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody PABDeleteRequest request) throws AtWinXSException {

		logger.debug("In deletePAB()");
		SessionContainer sc = getSessionContainer(ttsession);
		//CAP-45544
		boolean useOriginator=false;

		PABDeleteResponse response = mSelfAdminService.deletePABAddress(request, sc,useOriginator);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	//CAP-41593
	@PostMapping(value = RouteConstants.IMPORT_PAB, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Import the Personal address book")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<PABImportResponse> importPAB(
					@RequestHeader(value = "ttsession", required = false) String ttsession,
					@ModelAttribute PABImportRequest request) throws AtWinXSException, CPRPCException {
		logger.debug("In importPAB()");
		SessionContainer sc = getSessionContainer(ttsession);
		
		//CAP-45544
		boolean useOriginator=false;
		
		PABImportResponse response=mSelfAdminService.importAllPABAddresses(sc, request,useOriginator);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-44059
	@GetMapping(value = RouteConstants.EXPORT_PAB, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@Operation(summary = "Export all addresses in the Personal address book")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<PABExportResponse> exportPABAddresses(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			HttpServletResponse httpServletResponse) throws AtWinXSException {

		//CAP-45544
		boolean useOriginator=false;
				
		logger.debug("In exportPABAddresses()");
		SessionContainer sc = getSessionContainer(ttsession);
		PABExportResponse response = mSelfAdminService.exportPABAddresses(sc, httpServletResponse,useOriginator);

		HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
		if (response.isSuccess()) {
			status = HttpStatus.OK;
			response = null;
		}

		return new ResponseEntity<>(response, status);
	}
}