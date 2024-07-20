/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  04/05/2023  C Porter        CAP-39674                   Intermittent Session Timeout fix
 *  04/18/2023  C Porter        CAP-39954                   Occasional session timeout fix  
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */

package com.rrd.c1ux.api.services.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@ExtendWith(SpringExtension.class)
class C1UXSamlAuthenticationSuccessHandlerTests extends BaseServiceTest {

  @Mock
  private Authentication mockAuthentication;

  private C1UXSamlAuthenticationSuccessHandler serviceToTest;

  private MockHttpServletRequest mockRequest;
  private MockHttpServletResponse mockResponse;

  @BeforeEach
  void setup() {
    mockRequest = new MockHttpServletRequest();
    mockRequest.setSession(new MockHttpSession());
    mockResponse = new MockHttpServletResponse();

    serviceToTest = new C1UXSamlAuthenticationSuccessHandler(mockCPSessionReader);
  }

  @Test
  void testHandler_can_set_session_attributes() throws Exception {
    
    when(mockCPSessionReader.getSessionContainer(null, AtWinXSConstant.HOMEPAGE_SERVICE_ID)).thenReturn(mockSessionContainer);
    when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
    when(mockAppSessionBean.getTtSession()).thenReturn(mockTTSession);
    when(mockTTSession.getTimeOutValue()).thenReturn(1800);
    
    serviceToTest.handle(mockRequest, mockResponse, mockAuthentication);

    HttpSession sess = mockRequest.getSession();
    assertEquals(1800, sess.getMaxInactiveInterval());
  }

}
