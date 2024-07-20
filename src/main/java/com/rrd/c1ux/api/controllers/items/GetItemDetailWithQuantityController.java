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
 * 	05/11/22	Krishna Natarajan	CAP-34136	    Created as per the requirement to fetch the items detail
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 */

package com.rrd.c1ux.api.controllers.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.ItemNameToGetItemDetailWithQuantityRequest;
import com.rrd.c1ux.api.models.items.ItemRpt;
import com.rrd.c1ux.api.models.items.mappers.ItemRptMapper;
import com.rrd.c1ux.api.services.items.GetItemDetailWithQuantityService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Krishna Natarajan
 *
 */
@RestController("GetItemWithQuantityDetailController")
@RequestMapping(RouteConstants.ITEM_DETAIL_WITH_QUANTITY)
@Tag(name = "api/itemdetailwithquantity")
public class GetItemDetailWithQuantityController extends BaseCPApiController {
	
	GetItemDetailWithQuantityService getItemDetailWithQuantityService;
	ItemRptMapper itemMapper;

	private static final Logger logger = LoggerFactory.getLogger(GetItemDetailWithQuantityController.class);

	/**
	 * @param tokenReader
	 * @param sessionReader
	 */
	protected GetItemDetailWithQuantityController(TokenReader tokenReader, CPSessionReader sessionReader, GetItemDetailWithQuantityService sgetItemDetailWithQuantityService, ItemRptMapper sitemMapper) {
		super(tokenReader, sessionReader);
		getItemDetailWithQuantityService=sgetItemDetailWithQuantityService;
		itemMapper= sitemMapper;
	}

	@Override
	protected int getServiceID() {
		return 0;
	}

	/**
	 * @param ttsession {@link String}
	 * @param request {@link ItemNameToGetItemDetailWithQuantityRequest}
	 * @return details of ItemRptVO
	 * @throws AtWinXSException
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get item detail with quantity")
	public ItemRpt getItemDetailWithQuantity(@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody ItemNameToGetItemDetailWithQuantityRequest request)
			throws AtWinXSException {
		logger.debug("In getItemDetailWithQuantity");

		SessionContainer sc = null;


			sc = getSessionContainer(ttsession);

		return itemMapper.mapItemDetail(getItemDetailWithQuantityService.getItemDetailWithQuantity(sc, request));
	}

}
