/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	04/27/22	Sakthi				 CP-33098	Initial creation
 *	08/29/22	A Boomker			CAP-35537	Make session optional on all API calls
 */
package com.rrd.c1ux.api.controllers.shoppingcartitemindicator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.ShoppingCartItemIndicatorResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.shoppingcartitemindicator.ShoppingCartItemIndicatorServices;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("ShoppingCartItemIndicatorController")
@RequestMapping(RouteConstants.ITEM_INDICATOR)
@Tag(name = "itemindicator/shoppingcartitemindicator")
public class ShoppingCartItemIndicatorController extends BaseCPApiController {
	
	@Autowired
	ShoppingCartItemIndicatorServices shoppingCartItemIndicatorServices;

    protected ShoppingCartItemIndicatorController(TokenReader tokenReader, CPSessionReader sessionReader,ShoppingCartItemIndicatorServices mCartCountIndicator) {
		super(tokenReader, sessionReader);
		shoppingCartItemIndicatorServices=mCartCountIndicator;
	}

    @Override
    protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
    }
    
    @GetMapping(
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
        summary = "Get calalog menu for the user session in context")
    public ShoppingCartItemIndicatorResponse getItemCount(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid) throws AtWinXSException{
        SessionContainer sc = getSessionContainer(ttsessionid);
        return shoppingCartItemIndicatorServices.getItemCount(sc);
    }
}
