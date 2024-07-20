/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup
 */
package com.rrd.c1ux.api.controllers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;

@ExtendWith(MockitoExtension.class)
class BaseCPApiControllerTest {

  public static class ConcreteController extends BaseCPApiController {

    public ConcreteController(TokenReader tr, CPSessionReader sr) {
      super(tr, sr);
    }

    @Override
    protected int getServiceID() {
      return 0;
    }
    
    @Override
    protected boolean checkAccessAllowed(AppSessionBean asb) {
      // TODO Auto-generated method stub
      return asb.hasService(getServiceID());
    }
  }

  @Mock
  private TokenReader mockTokenReader;

  @Mock
  private CPSessionReader mockSessionReader;

  @Mock
  private SessionContainer mockSessionContainer;

  @Mock
  private ApplicationSession mockApplicationSession;

  @Mock
  private AppSessionBean mockAppSessionBean;

  @InjectMocks
  private ConcreteController testSubject;

  @BeforeEach
  void setUp() throws Exception {
  
    when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);
    when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  }


  @Test
  void testGetSessionContainer() throws Exception {

    when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
    
    assertDoesNotThrow(() -> {
      assertNotNull(testSubject.getSessionContainer("sessionID"));
    });
  }

  @Test
  void testGetSessionContainer2() {

    when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
    
    assertThrows(AccessForbiddenException.class, () -> {
      testSubject.getSessionContainer("sessionID");
    });

  }


}
