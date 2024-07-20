/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  01/02/23    D Li	        CAP-43217             		Initial tests for App Alive Controller
 */
package com.rrd.c1ux.api.controllers;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.rrd.c1ux.api.BaseMvcTest;
import com.wallace.atwinxs.admin.ao.SiteAssembler;

@WithMockUser
class AppAliveControllerTests extends BaseMvcTest {
	
	@Test
	void databaseCallSucceedsandReturnsOk() throws Exception {
		
		try (MockedConstruction<SiteAssembler> mocked = 
				mockConstruction(SiteAssembler.class, (mock, context) -> {
					when(mock.getSite(anyInt())).thenReturn(null);
				}))
		{
			mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.API_ALIVE)
					.accept(MediaType.ALL))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.serverName").value(System.getProperty("XS.SERVER.TOKEN") + System.getProperty("XS.SERVER.ID")));
		}
	}
  
	@Test
	void databaseCallFailsAndReturnsFail() throws Exception {

		try (MockedConstruction<SiteAssembler> mocked = 
				mockConstruction(SiteAssembler.class, (mock, context) -> { 
					when(mock.getSite(anyInt())).thenThrow(new RuntimeException("test error")); 
					}))
		{
			mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.API_ALIVE)
					.accept(MediaType.ALL))
			.andExpect(MockMvcResultMatchers.status().is5xxServerError())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("test error"));
		}
	}
	
}
