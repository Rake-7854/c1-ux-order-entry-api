/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/05/23	N Caceres		CAP-39051					Initial version
 */
package com.rrd.c1ux.api.controllers.items;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.rrd.c1ux.api.models.users.UserContext;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class GetFavoritesControllerTests extends BaseMvcTest {

	private static final boolean TEST_SUCCESS_TRUE = true;
	@BeforeEach
	void setUp() throws Exception {
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockTokenReader.getToken()).thenReturn(mockToken);
		when(mockSessionReader.getUserContext()).thenReturn(new UserContext());
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockOrderEntrySession.getOESessionBean()).thenReturn(mockOEOrderSessionBean);
		when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockOEOrderSessionBean.getSearchCriteriaBean()).thenReturn(mockSearchCriteriaBean);
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}

	@Test
	void that_getFavoriteItems_returns_expected() throws Exception {
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockGetFavoritesService.getFavoriteItemsProcessed(any(SessionContainer.class),any(FavoriteItemsMapper.class),any(ServletContext.class)))
				.thenReturn(getFavoriteResponse());
		
		// when getDetail is called, expect 200 status and item numbers in JSON
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.GET_FAVORITE).contentType(MediaType.APPLICATION_JSON)
						.header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
						.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(TEST_SUCCESS_TRUE));
				
	}
	
	
	private CatalogItemsResponse getFavoriteResponse() {
		CatalogItemsResponse response = new CatalogItemsResponse();
		response.setSuccess(true);
		return response;
	}
}
