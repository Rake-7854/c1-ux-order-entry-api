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
 * 	06/28/23	Satishkumar A      CAP-41594	   C1UX API - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 * 	07/11/23	Satishkumar A      CAP-41970		C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 */

package com.rrd.c1ux.api.controllers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.util.CountriesAndStatesResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.util.CountriesAndStatesService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Satishkumar A
 *
 */
@RestController("CountriesAndStatesController")
@Tag(name = "Get State/Country List")
public class CountriesAndStatesController extends BaseCPApiController{

	private static final Logger logger = LoggerFactory.getLogger(CountriesAndStatesController.class);
	
	@Autowired
	CountriesAndStatesService countriesAndStatesService;
	
	@GetMapping(value = RouteConstants.COUNTRY_STATE_LIST, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Get State/Country List ")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    public ResponseEntity<CountriesAndStatesResponse> getCountriesAndStatesList(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

        logger.debug("In CountriesAndStatesController - getCountriesAndStatesList() ");
        
        SessionContainer sc = getSessionContainer(ttsession);
        
        CountriesAndStatesResponse response = countriesAndStatesService.getCountriesAndStatesOrProvincesList(sc);
        
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
        
	}
        
        protected CountriesAndStatesController(TokenReader tokenReader, CPSessionReader sessionReader,CountriesAndStatesService mCountriesAndStatesService) {
    		super(tokenReader, sessionReader);
    		countriesAndStatesService = mCountriesAndStatesService;

    	}

    	@Override
    	protected int getServiceID() {

    		return AtWinXSConstant.HOMEPAGE_SERVICE_ID;
    	}

}
