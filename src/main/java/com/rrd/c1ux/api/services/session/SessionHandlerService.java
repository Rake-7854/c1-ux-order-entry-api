/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/17/2023  C Porter        CAP-39295                   Handle "Could not load session" Exception
 *  07/25/2023	A Boomker		CAP-42223					Added session save here
 */
package com.rrd.c1ux.api.services.session;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;

import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SessionHandlerService {

  public SessionContainer getFullSessionInfo(int sessionID, int serviceID) throws AtWinXSException;
  public void saveFullSessionInfo(BaseSession session, int sessionID, int serviceID) throws AtWinXSException;
  public BaseSession loadSession(int sessionID, int serviceID)throws AtWinXSException;
	public boolean sessionExists(int sessionID, int serviceID) throws AtWinXSException;
	public void deleteSession(int sessionID, int serviceID) throws AtWinXSException;
	public void initNewModuleSession(AppSessionBean asb, HttpServletRequest request, boolean useRequestorForAutoLogin,
			BaseSession bs, int serviceID, Logger log) throws AtWinXSException;
}
