/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	02/08/23	A Boomker			CAP-38574		Create demo of new standard response handling
 */

package com.rrd.c1ux.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.models.FakeRequest;
import com.rrd.c1ux.api.models.FakeResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("TempController")
@RequestMapping("/api/temp/")
@Tag(name = "Demo")
public class TempController extends BaseCPApiController {

	private static final String HTTP_OK = "200";
	private static final String HTTP_UNPROCESSABLE_ENTITY = "422";
	
	protected TempController(TokenReader tokenReader, CPSessionReader sessionReader) {
		super(tokenReader, sessionReader);
		// TODO Auto-generated constructor stub
	}

	// CAP-38574 - demo of the new standard object and handling
	@PostMapping(value = "demostandard", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get base response object and status requested")
	@ApiResponses({@ApiResponse(responseCode = HTTP_OK), @ApiResponse(responseCode = HTTP_UNPROCESSABLE_ENTITY)})
	public ResponseEntity<FakeResponse> getTempMethod(@RequestBody FakeRequest request) throws AtWinXSException {
		FakeResponse response = new FakeResponse();
		response.setSuccess(!request.isFailThis());
		response.setMessage(!request.isFailThis() ? "I am the fake success message" : "I am the fake bad request message");
		if (request.isFailThis())
		{
			response.setFieldMessage("yourField", "This field was bad. You need more validation.");
			response.setFieldMessage("yourSecondField", "This field was ALSO bad. You need WAY more validation.");
		}
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@Override
	protected int getServiceID() {
		// TODO Auto-generated method stub
		return 0;
	}

}
