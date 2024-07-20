
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	04/27/22	Sakthi	 			CP-33098	Initial creation
 *	05/29/23    Sakthi M			CAP-39048   Address Concurrency issues in ShoppingCartItemIndicator Service
*/

package com.rrd.c1ux.api.services.shoppingcartitemindicator;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.ShoppingCartItemIndicatorResponse;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@Service
public class ShoppingCartItemIndicatorServicesImpl implements ShoppingCartItemIndicatorServices{

	/**
	 * parameter SessionContainer
	 * return count
	 */
	@Override
	public ShoppingCartItemIndicatorResponse getItemCount(SessionContainer sc) throws AtWinXSException {
		//CAP-39048
		ApplicationVolatileSession volatileSession=sc.getApplicationVolatileSession();
		ShoppingCartItemIndicatorResponse itemCountRes=new ShoppingCartItemIndicatorResponse();
		itemCountRes.setItemCountInShopingCart(volatileSession.getVolatileSessionBean().getShoppingCartCount());
		itemCountRes.setStatus(volatileSession.getVolatileSessionBean().getShoppingCartCount() >= 0 ? RouteConstants.REST_RESPONSE_SUCCESS:RouteConstants.REST_RESPONSE_FAIL);
		return itemCountRes;
	}

}
