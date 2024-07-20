/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	CAP-35537	T Harmon									Initial creation
 */


package com.rrd.c1ux.api.controllers.login.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.services.login.saml.SamlSpService;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@Controller
public class SamlLoginController 
{
    private final Logger logger = LoggerFactory.getLogger(SamlLoginController.class);
   	
	@Autowired SamlSpService samlSpService;
	
	 public SamlLoginController() 
	 {
		 super();	
	 }		
	
	@PostMapping(RouteConstants.SSO_LOGIN_SAML2)
	public void getSamlLoginPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AtWinXSException
	{
		// Send the request to do the actual SAML login here
		try
		{
			String returnUrl = samlSpService.doSamlLogin(httpServletRequest, httpServletResponse, null);	
			httpServletResponse.sendRedirect(returnUrl);
		}
		catch(Exception ex)
		{
			logger.error("Login failed - Please contact your Administrator", ex);
			throw new AtWinXSException("Login failed - Please contact your Administrator.", this.getClass().getName());
		}
	}
}
