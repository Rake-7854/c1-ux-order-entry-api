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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class CancelPunchoutOrderControllerTest extends BaseMvcTest  {
	
    private static final String EXCEPTION_RESPONSE_MESSAGE = "$.message";
    private static final String EXCEPTION_RESPONSE_TITLE = "$.title";
    private static final String CANCEL_PUNCHOUT_ORDER_RETURN_URL = "https://localhost/cp/orders/cancelorder.cp?ttsessionid=ZldUc2lMZFdKMWs2ekZnNS82dWw5YVR4QUZsZEVyamtnc1RQY1l5Q3dGST0=&cancelAction=CANCEL_ORDER";
    private static final String CANCEL_PUNCHOUT_ORDER_EDIT_RETURN_URL = "https://localhost/cp/orders/cancelorder.cp?ttsessionid=ZldUc2lMZFdKMWs2ekZnNS82dWw5YVR4QUZsZEVyamtnc1RQY1l5Q3dGST0=&cancelAction=CANCEL_PUNCHOUT_EDIT";
    
    @BeforeEach
    void setUp() throws Exception {
        
    	when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);
    	when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    	when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        doReturn("").when(mockAppSessionBean). getEncodedSessionId();
       
    }
    
    
	  @Test 
	  void that_cancelPunchoutOrder_returns_expected() throws Exception { 

				when(mockSessionReader.getSessionContainer(mockAppSessionBean.getEncodedSessionId(), AtWinXSConstant.APPSESSIONSERVICEID)).thenReturn(mockSessionContainer);

				when(mockAppSessionBean.getEncodedSessionId()).thenReturn("ZldUc2lMZFdKMWs2ekZnNS82dWw5YVR4QUZsZEVyamtnc1RQY1l5Q3dGST0=");
				
				String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
				try(MockedStatic<AppProperties> mockAppProperties = Mockito.mockStatic(AppProperties.class)	){
					mockAppProperties.when(() -> AppProperties.getServerName()).thenReturn("localhost");

		        mockMvc.perform(
		            MockMvcRequestBuilders.get(RouteConstants.CANCEL_PUNCHOUT_QUOTE)
		            .accept(MediaType.APPLICATION_JSON)
		            .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
		            .contentType(MediaType.APPLICATION_JSON)
		            )
		        	.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		            .andExpect(redirectedUrl(CANCEL_PUNCHOUT_ORDER_RETURN_URL));
				}
	  }
	  
	    
	  
		  @Test
		    void that_cancelPunchoutOrder_returns_BAD_REQUEST() throws Exception {
		        
		        AtWinXSException e = new AtWinXSException("cancelPunchoutOrder error", String.class.getName());
		        
		        when(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).thenThrow(e);
		        String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		        
		        mockMvc.perform(
		        		MockMvcRequestBuilders.get(RouteConstants.CANCEL_PUNCHOUT_QUOTE)
		        		.header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
			            .contentType(MediaType.APPLICATION_JSON)
			     )
		            .andExpect(status().isBadRequest())
		            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("cancelPunchoutOrder")))
		            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, emptyString()));
		        
		    }
		  
		  @Test 
		  void that_cancelPunchoutOrderEdit_returns_expected() throws Exception { 

					when(mockSessionReader.getSessionContainer(mockAppSessionBean.getEncodedSessionId(), AtWinXSConstant.APPSESSIONSERVICEID)).thenReturn(mockSessionContainer);

					when(mockAppSessionBean.getEncodedSessionId()).thenReturn("ZldUc2lMZFdKMWs2ekZnNS82dWw5YVR4QUZsZEVyamtnc1RQY1l5Q3dGST0=");
					
					String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
					try(MockedStatic<AppProperties> mockAppProperties = Mockito.mockStatic(AppProperties.class)	){
						mockAppProperties.when(() -> AppProperties.getServerName()).thenReturn("localhost");

			        mockMvc.perform(
			            MockMvcRequestBuilders.get(RouteConstants.CANCEL_PUNCHOUT_EDITS)
			            .accept(MediaType.APPLICATION_JSON)
			            .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
			            .contentType(MediaType.APPLICATION_JSON)
			            )
			        	.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
			            .andExpect(redirectedUrl(CANCEL_PUNCHOUT_ORDER_EDIT_RETURN_URL));
					}
		  }
		  
		    
		  
			  @Test
			    void that_cancelPunchoutOrderEdit_returns_BAD_REQUEST() throws Exception {
			        
			        AtWinXSException e = new AtWinXSException("cancelPunchoutOrderEdit error", String.class.getName());
			        
			        when(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).thenThrow(e);
			        String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
			        
			        mockMvc.perform(
			        		MockMvcRequestBuilders.get(RouteConstants.CANCEL_PUNCHOUT_EDITS)
			        		.header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
				            .contentType(MediaType.APPLICATION_JSON)
				     )
			            .andExpect(status().isBadRequest())
			            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("cancelPunchoutOrderEdit")))
			            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, emptyString()));
			        
			    }
		    
}
