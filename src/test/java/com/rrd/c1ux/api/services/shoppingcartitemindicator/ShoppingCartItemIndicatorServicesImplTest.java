/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		    Description
 * 	--------	-----------		     ----------  	-----------------------------------------------------------
 *	06/01/22	Sakthi M	 		 CP-39048		Initial creation
 */
package com.rrd.c1ux.api.services.shoppingcartitemindicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.items.ShoppingCartItemIndicatorResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

class ShoppingCartItemIndicatorServicesImplTest extends BaseServiceTest {
	public static final String SUCCESS = "Success";
	public static final String FAIL="Failed";
	
	@InjectMocks
	private ShoppingCartItemIndicatorServicesImpl serviceToTest;
	
	ShoppingCartItemIndicatorResponse countResp=new ShoppingCartItemIndicatorResponse();
	
	@BeforeEach
	public void setUp() throws Exception {
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
	}
	
	@Test
	void that_getShoppingCartItemIndicator_returns_success() throws AtWinXSException {
			countResp = serviceToTest.getItemCount(mockSessionContainer);
			assertEquals(SUCCESS, countResp.getStatus());
	}
	
	@Test
	void that_getShoppingCartItemIndicator_returns_fail() throws AtWinXSException {
			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(-1);
			countResp = serviceToTest.getItemCount(mockSessionContainer);
			assertEquals(FAIL, countResp.getStatus());
	}
}
