/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.controllers.shoppingcartitemindicator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.ShoppingCartItemIndicatorResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class ShoppingCartItemIndicatorControllerTest extends BaseMvcTest {
		
	ShoppingCartItemIndicatorResponse countResp=getCartCountTest();
	
	@BeforeEach
	void setUp() throws Exception {

		when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);// mandy
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}
	
	
	@Test
	void that_getCartCount_returnsExpected() throws Exception, AtWinXSException {
		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mShoppingCartItemIndicatorServices.getItemCount(any(SessionContainer.class)))
				.thenReturn(countResp);

		// when getCartCount is called, expect 200 status and item numbers in JSON
				mockMvc.perform(
						MockMvcRequestBuilders.get(RouteConstants.ITEM_INDICATOR).contentType(MediaType.APPLICATION_JSON)
								.header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
								.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.status().isOk())
						.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.jsonPath("$.status").value(RouteConstants.REST_RESPONSE_SUCCESS));
		
	}

	private static ShoppingCartItemIndicatorResponse getCartCountTest() {
		return new ShoppingCartItemIndicatorResponse(0, RouteConstants.REST_RESPONSE_SUCCESS);
	}
	
}
