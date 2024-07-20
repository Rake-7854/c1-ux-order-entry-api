/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date        Modified By     JIRA#       Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  03/20/2023  C Porter        CP-39295    Handle "Could not load session" Exception
 *  05/31/23    C Porter        CAP-40530   JUnit cleanup  
 */
package com.rrd.c1ux.api.services.session;

import static org.mockito.ArgumentMatchers.anyInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.framework.session.SessionHandler;

class SessionHandlerServiceImplTests extends BaseServiceTest {

  SessionHandlerService serviceToTest = new SessionHandlerServiceImpl();

  @Test
  void test_getFullSessionInfo_returns_session_container() throws Exception {

    try (MockedStatic<SessionHandler> mockedSessionHandler = Mockito.mockStatic(SessionHandler.class)) {

      mockedSessionHandler.when(() -> SessionHandler.getFullSessionInfo(anyInt(), anyInt())).thenReturn(mockSessionContainer);

      Assertions.assertNotNull(serviceToTest.getFullSessionInfo(1, 1));

    }

  }

}
