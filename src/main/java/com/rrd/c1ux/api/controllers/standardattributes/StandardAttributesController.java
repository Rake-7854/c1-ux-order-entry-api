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
 *  08/17/23    M Sakthi		CAP-41802  	Initial version
 *  10/03/23	Satishkumar A	CAP-43282	C1UX BE - API Build - Get OE Item Filter Options - including favorites, featured types
 */

package com.rrd.c1ux.api.controllers.standardattributes;

import java.lang.reflect.InvocationTargetException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.standardattributes.StandardAttributesResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.standardattributes.StandardAttributesService;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("StandardAttributesController")
@Tag(name = "Item Filter APIs")
public class StandardAttributesController extends BaseCPApiController{
	
	StandardAttributesService mStandardAttributesService;
	
	 @Override
	    protected int getServiceID() {
			
			return AtWinXSConstant.ORDERS_SERVICE_ID;
		}
	 
		@Override
		protected boolean checkAccessAllowed(AppSessionBean asb) {
			return asb.hasService(getServiceID());
		}

		protected StandardAttributesController(TokenReader tokenReader, CPSessionReader sessionReader, StandardAttributesService standardAttributesService) {
			super(tokenReader, sessionReader);
			mStandardAttributesService=standardAttributesService;
		}
	
		@GetMapping(value = RouteConstants.GET_ITEM_FILTERS, produces = { MediaType.APPLICATION_JSON_VALUE,
				MediaType.APPLICATION_XML_VALUE })
		@Operation(summary = "Get Item Filters")
		@ApiResponse(responseCode = RouteConstants.HTTP_OK)
		@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
		@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
		@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
		public ResponseEntity<StandardAttributesResponse> getItemFilters(
				@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException, IllegalAccessException, InvocationTargetException, CPRPCException   {
			
			SessionContainer sc = getSessionContainer(ttsession);
			StandardAttributesResponse response = mStandardAttributesService.getStandardAttributeList(sc);
			return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
		}

}



