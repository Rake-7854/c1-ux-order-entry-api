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
 *  09/27/22    S Ramachandran  CAP-35439   Get Punchout Transfer Cart validation
 */

package com.rrd.c1ux.api.controllers.punchout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.rrd.c1ux.api.services.punchout.PunchoutService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("PunchoutController")
public class PunchoutController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(PunchoutController.class);
	
	private final PunchoutService mPunchoutService;
	
	private final ShoppingCartService mShoppingCartService;
	
	protected PunchoutController(TokenReader tokenReader, CPSessionReader sessionReader, PunchoutService punchoutService,
			ShoppingCartService shoppingCartService) {

		super(tokenReader, sessionReader);
		this.mPunchoutService = punchoutService;
		this.mShoppingCartService = shoppingCartService;
	}

	protected int getServiceID() {
		
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	
	/**
	 * 
	 * @param sessionID - {@link String}
	 * @return - This will return ( @link COShoppingCartResponse) 
	 * @throws AtWinXSException
	 */	
	//CAP-35439  - controller to get punchout transfer cart validation flag
	@Tag(name = "punchout/validatepunchouttransfercart")
	@PostMapping(value = RouteConstants.PUNCHOUT_TRANSFER_CART_VALIDATION, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get punchout transfer cart validation flag for given punchout items")
	public COShoppingCartResponse validatePunchoutTrasferCart(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession,
			@RequestBody COShoppingCartRequest scRequest) throws AtWinXSException {

		logger.debug("In PunchoutController - validatePunchoutTrasferCart()");

		SessionContainer mainSession = getSessionContainer(ttsession);
		COShoppingCartResponse objCOShoppingCartResponse = mShoppingCartService.processUpdateShoppingCart(mainSession, scRequest);

		//CAP-35439 - Get validation flag(Y/N) for transfer cart after validating punchout item added to shopping cart 
		return mPunchoutService.validatePunchoutTransferCart(mainSession,objCOShoppingCartResponse);

	}
	
	
}	