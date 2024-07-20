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
 */

package com.rrd.c1ux.api.services.session;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@Service
public class C1UXSamlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  private final CPSessionReader cpSessionReader;

  public C1UXSamlAuthenticationSuccessHandler(CPSessionReader cpSessionReader) {

    super();

    this.cpSessionReader = cpSessionReader;
  }

  @Override
  protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    try {
      SessionContainer sc = cpSessionReader.getSessionContainer(null, AtWinXSConstant.HOMEPAGE_SERVICE_ID);
      int timeoutValue = sc.getApplicationSession().getAppSessionBean().getTtSession().getTimeOutValue();
      request.getSession().setMaxInactiveInterval(timeoutValue);
    } catch (AtWinXSException ex) {
      throw new IOException("failed to load session container", ex);
    }

    super.handle(request, response, authentication);
  }
}
