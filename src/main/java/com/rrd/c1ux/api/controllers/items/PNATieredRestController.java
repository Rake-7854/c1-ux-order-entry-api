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
 *  04/19/22    S Ramachandran  CAP-33763   Initial Creation
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 *	06/03/24	C Codina		CAP-38842	C1UX BE - Unused API Standardization - Get Tiered PNA conversion to standards
 */

package com.rrd.c1ux.api.controllers.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.PNATieredPriceRequest;
import com.rrd.c1ux.api.models.items.PNATieredPriceResponse;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.rrd.c1ux.api.services.items.PNATieredProcessor;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("PNATieredRestController")
@Tag(name = "PNA Tiered Rest Controller")
public class PNATieredRestController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(PNATieredRestController.class);
	
	private final PNATieredProcessor mService;

	/**
	 * @param tokenReader {@link TokenReader}
	 * @param sessionReader {@link CPSessionReader}
	 * @param favoriteItems {@link FavoriteItemsMapper}
	 */
	protected PNATieredRestController(TokenReader tokenReader, CPSessionReader sessionReader,
			PNATieredProcessor mService) {
		
		super(tokenReader, sessionReader);
		this.mService = mService;
	}
	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return true;
	}


	/**
	 *
	 * @param sessionID - {@link String}
	 * @param request - {@link PNATieredPriceRequest}
	 * @return - This will return {@link PNATieredPriceResponse} which 
	 *           includes TieredPriceVO & Status 
	 * @throws AtWinXSException
	 */
	@PostMapping(value = RouteConstants.GET_PNATIERED, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve Price and Availability details for Tiered Items")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	public ResponseEntity<PNATieredPriceResponse> retrievePNATieredRestController(
			@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody PNATieredPriceRequest request) throws AtWinXSException {

		logger.debug("In retrievePNATieredRestController");

		SessionContainer sc = getSessionContainer(ttsession);
		PNATieredPriceResponse response = mService.processPNATierPrice(request, sc);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

}
