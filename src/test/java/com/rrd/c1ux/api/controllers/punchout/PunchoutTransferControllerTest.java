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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;

@WithMockUser
class PunchoutTransferControllerTest  extends BaseMvcTest {

    private static final String TRANSFER_PUNCHOUTCART_RETURN_URL = "https://localhost/xs2/orders/orderentry?actionID=BROKER_OE_PUNCHOUT_SESSION&eventID=UX_REDIRECT&ttsessionid=WDJSWU5wR0JRSklvWGU1T2wwcm5MTnJlaCsrNzhxcUg0VnMyMXlDYkVnST0=&redirectEvent=CART_TRANSFER&freightSel=Y&hasPassedUSPSValidation=Y";
    
    @Mock
    PunchoutSessionBean mockPunchoutSessionBean;
    
    @BeforeEach
    void setUp() throws Exception {
        
    	when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);
    	when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    	when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        doReturn("").when(mockAppSessionBean). getEncodedSessionId();
       
    }
    
	  @Test 
	  void that_transferPunchoutCart_returns_expected() throws Exception { 

				when(mockSessionReader.getSessionContainer(mockAppSessionBean.getEncodedSessionId(), AtWinXSConstant.APPSESSIONSERVICEID)).thenReturn(mockSessionContainer);

				when(mockAppSessionBean.getEncodedSessionId()).thenReturn("MERyc0RBeXBQTExST1JodS95Vk1HZ25mSnVzUkIwWGdDQ3FpbitNUXo1bz0=");
				
				
				when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
				when(mockPunchoutService.transferPunchoutCart(any())).thenReturn(TRANSFER_PUNCHOUTCART_RETURN_URL);
				
				String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
				try(MockedStatic<AppProperties> mockAppProperties = Mockito.mockStatic(AppProperties.class)	){
					mockAppProperties.when(() -> AppProperties.getServerName()).thenReturn("localhost");
				
					mockMvc.perform(
		            MockMvcRequestBuilders.get(RouteConstants.PUNCHOUT_TRANSFER_CART)
		            .accept(MediaType.APPLICATION_JSON)
		            .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
		            .contentType(MediaType.APPLICATION_JSON)
		            )
		        	.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		            .andExpect(redirectedUrl(TRANSFER_PUNCHOUTCART_RETURN_URL));
				}
	  }
	  
}
