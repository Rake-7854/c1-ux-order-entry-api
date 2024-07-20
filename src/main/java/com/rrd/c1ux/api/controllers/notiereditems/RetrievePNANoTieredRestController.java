
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#																				Description
 * 	--------	-----------		-------------------------------------------------------------------------------		--------------------------------
 *	04/20/22	Sakthi M		CAP-33762- Item Detail Pricing/Availability Call (Single Item no tiered pricing)	Initial creation
 *	08/29/22	A Boomker		CAP-35537	Make session optional on all API calls
 *	06/13/22	C Codina		CAP-39052	Address Concurrency issues in RetrievePNANoTiered Rest Controller
 */
package com.rrd.c1ux.api.controllers.notiereditems;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.notiereditems.PNANoTieredPriceRequest;
import com.rrd.c1ux.api.models.notiereditems.mappers.NotieredItemMapper;
import com.rrd.c1ux.api.services.notiereditems.PNANoTieredServices;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("RetrievePNANoTieredRestController")
@RequestMapping(RouteConstants.NO_TIERED_ITEM_PRICING)
@Tag(name = "items/notieredpricingitems")
public class RetrievePNANoTieredRestController extends BaseCPApiController {

	private NotieredItemMapper mNotieredItemMapper;
	
	@Autowired
	PNANoTieredServices pNANoTieredServices;

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	protected RetrievePNANoTieredRestController(TokenReader tokenReader, CPSessionReader sessionReader, PNANoTieredServices pnANoTieredServices,
			NotieredItemMapper notieredItemMapper) {

		super(tokenReader, sessionReader);
		mNotieredItemMapper = notieredItemMapper;
		pNANoTieredServices = pnANoTieredServices;
	}
	
	@PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get No Tiered pricing items for the user session in context")
	public PriceAndAvailabilityVO retrieveNoTieredPricingValues(@RequestHeader(required=false) String ttsessionid,
			@RequestBody PNANoTieredPriceRequest request) throws AtWinXSException {
	
		SessionContainer sc = getSessionContainer(ttsessionid);
		ApplicationSession appSession = sc.getApplicationSession();
	
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		//CAP-39052 Resolved Concurrency Issue
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		
		PriceAndAvailabilityVO item = pNANoTieredServices.getPNANoTieredItemPricing(appSession, volatileSession,
				oeSession,request);
		return mNotieredItemMapper.getNoTieredItemPricing(item);
	}

}
