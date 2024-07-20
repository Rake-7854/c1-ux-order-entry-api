/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision
 *  Date        Modified By     JIRA#       Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  03/20/2023  C Porter        CP-39295    Handle "Could not load session" Exception
 */
package com.rrd.c1ux.api;

import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

public abstract class BaseOEServiceTest extends BaseServiceTest {

	@Mock
	protected OEOrderSessionBean mockOEOrderSession;
	@Mock
	protected OEResolvedUserSettingsSessionBean mockUserSettings;

  public static final int DEVTEST_SITE_ID = 4366;
  public static final int DEVTEST_UX_BU_ID = 7125;
  public static final String DEFAULT_UG_NM = "C1UX";
  public static final String DEFAULT_VIEW = "orderSearch";

	public void setUpModuleSession() throws Exception {
	    setupBaseMockSessions();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	}

	public void setUpModuleSessionNoBase() throws Exception {
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	}

}
