/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	26/07/22	Sakthi M			 CAP-34855	Initial creation, Catalog Line View - Units and Qty revised validation
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 */


package com.rrd.c1ux.api.controllers.itemqtyvalidation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationRequest;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationResponse;
import com.rrd.c1ux.api.services.itemqtyvalidation.ItemQuantityValidationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("ItemQuantityValidationController")
@RequestMapping(RouteConstants.ITEM_QTY_VALIDATION)
@Tag(name = "itemqtyvalidation/itemquantityvalidation")
public class ItemQuantityValidationController extends BaseCPApiController {
	
	@Autowired
	ItemQuantityValidationService itemQuantityValidationService;

    protected ItemQuantityValidationController(TokenReader tokenReader, CPSessionReader sessionReader,ItemQuantityValidationService mItemQuantityValidationService) {
		super(tokenReader, sessionReader);
		itemQuantityValidationService=mItemQuantityValidationService;
	}

    @Override
    protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
    }
    
    
    @PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Item Qty validation")
	public ItemQtyValidationResponse getItemQtyValidation(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid, @RequestBody ItemQtyValidationRequest request) throws AtWinXSException{
        SessionContainer sc = getSessionContainer(ttsessionid);
        return itemQuantityValidationService.getQtyValidation(sc,request);
    }
}