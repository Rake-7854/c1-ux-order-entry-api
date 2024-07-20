/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     	DTS#                        Description
 *  --------    -----------     	-----------------------     --------------------------------
 *  06/12/23   	Satishkumar A      	CAP-40835                   Address High Priority Security Hotspots Identified by SonarQube - Dev Only
 */
package com.rrd.c1ux.api.controllers.punchout;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.custompoint.ui.common.HeaderTag;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;

@WithMockUser
class LogoutControllerTest extends BaseMvcTest{
	
    private static final String EXCEPTION_RESPONSE_MESSAGE = "$.message";
    private static final String EXCEPTION_RESPONSE_TITLE = "$.title";
    private static final String CANCEL_LOGOUT_LINK_RETURN_URL = "https://localhost/xs2/logout?ttsessionid=MERyc0RBeXBQTExST1JodS95Vk1HZ25mSnVzUkIwWGdDQ3FpbitNUXo1bz0=&actionID=logout&clearProcess=true";
 
	@Mock
	private PunchoutSessionBean mockPunchoutSessionBean;
	
    @BeforeEach
    void setUp() throws Exception {
        
    	when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);
    	when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    	when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        doReturn("").when(mockAppSessionBean). getEncodedSessionId();
       
    }
	  @Test 
	  void that_createLogoutLink_returns_expected() throws Exception { 

				when(mockSessionReader.getSessionContainer(mockAppSessionBean.getEncodedSessionId(), AtWinXSConstant.APPSESSIONSERVICEID)).thenReturn(mockSessionContainer);

				when(mockAppSessionBean.getEncodedSessionId()).thenReturn("MERyc0RBeXBQTExST1JodS95Vk1HZ25mSnVzUkIwWGdDQ3FpbitNUXo1bz0=");
				
				String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
				try(MockedStatic<AppProperties> mockAppProperties = Mockito.mockStatic(AppProperties.class); MockedStatic<HeaderTag> mockHeaderTag = Mockito.mockStatic(HeaderTag.class)){
					mockAppProperties.when(() -> AppProperties.getServerName()).thenReturn("localhost");
					
					String linkURL= "/xs2/logout?ttsessionid="+ TEST_ENCRIPTED_SESSIONID +"&actionID=logout&clearProcess=true";
					
					mockHeaderTag.when(() -> HeaderTag.getLogoutLink(any(),any())).thenReturn(linkURL);
					
		        mockMvc.perform(
		            MockMvcRequestBuilders.get(RouteConstants.PUNCHOUT_SIGN_OUT)
		            .accept(MediaType.APPLICATION_JSON)
		            .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
		            .contentType(MediaType.APPLICATION_JSON)
		            )
		        	.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		            .andExpect(redirectedUrl(CANCEL_LOGOUT_LINK_RETURN_URL));
				}
	  }

		  @Test
		    void that_createLogoutLink_returns_BAD_REQUEST() throws Exception {
		        
		        AtWinXSException e = new AtWinXSException("createLogoutLink error", String.class.getName());
		        
		        when(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).thenThrow(e);
		        String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		        
		        mockMvc.perform(
		        		MockMvcRequestBuilders.get(RouteConstants.PUNCHOUT_SIGN_OUT)
		        		.header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
			            .contentType(MediaType.APPLICATION_JSON)
			     )
		            .andExpect(status().isBadRequest())
		            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("createLogoutLink")))
		            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, emptyString()));
		        
		    }
}
