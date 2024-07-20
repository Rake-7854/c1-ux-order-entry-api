/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	CAP-35537	T Harmon									Initial creation
 *	CAP-36153	A Boomker		CAP-36153					Convert entry point code into routing URL
 */


package com.rrd.c1ux.api.services.login.saml;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.wallace.atwinxs.framework.util.AtWinXSException;


public interface SamlSpService 
{
	public String doSamlLogin(HttpServletRequest request, HttpServletResponse httpServletResponse, Map<String, String> params) throws AtWinXSException;
	public String getEntryPointRouting(String entryPoint, CPSessionReader mSessionReader); // CAP-36153
}
