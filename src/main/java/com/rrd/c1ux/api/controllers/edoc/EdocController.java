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
 *  10/10/23	Satishkumar A	CAP-44196	C1UX API - Create api to retrieve edoc for Storefront
 *  10/17/23	Satishkumar A	CAP-44664	C1UX BE - Create api to retrieve edoc for Storefront
 *  11/10/23	Krishna Natarajan		CAP-44548 			Additional changes made to indicate EDOC internal or external
 */
package com.rrd.c1ux.api.controllers.edoc;


import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.edoc.EdocUrlResponse;
import com.rrd.c1ux.api.services.edoc.EdocService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("EdocController")
@Tag(name = "EDoc APIs")
public class EdocController extends BaseCPApiController{

	private final EdocService edocService;

	protected EdocController(TokenReader tokenReader, CPSessionReader sessionReader,EdocService mEdocService) {
		super(tokenReader, sessionReader);
		edocService = mEdocService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID; 
	}
	
	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@GetMapping(value = RouteConstants.GET_EDOC_URL, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Edoc URL or File")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	public ResponseEntity<EdocUrlResponse> getEdocUrl(
				@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestParam(name = "a") String a, HttpServletRequest request, HttpServletResponse response1) throws AtWinXSException, UnsupportedEncodingException   {

		SessionContainer sc = getSessionContainer(ttsession);
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		EdocUrlResponse response = edocService.getEdocUrl(appSessionBean, request,response1);	

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-44548 additional changes
	@GetMapping(value = RouteConstants.GET_EDOC_EXTERNAL_URL, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Edoc URL or File")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	public ResponseEntity<EdocUrlResponse> getEdocUrlExternal(
			@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestParam(name = "a") String a,
			HttpServletRequest request, HttpServletResponse response1)
			throws AtWinXSException, UnsupportedEncodingException {

		SessionContainer sc = getSessionContainer(ttsession);
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		EdocUrlResponse response = edocService.getEdocUrl(appSessionBean, request,response1);	

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
