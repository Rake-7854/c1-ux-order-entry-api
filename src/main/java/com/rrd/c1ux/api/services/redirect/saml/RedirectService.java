/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	5/19/23		A Salcedo		CAP-39210					Initial creation
 *	10/23/23	A Salcedo		CAP-44686					New CP redirect API w params.
 */
package com.rrd.c1ux.api.services.redirect.saml;

import java.util.Map;

import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface RedirectService 
{
	public CPRedirectResponse getSamlUrl(AppSessionBean asb, String entryPoint) throws AtWinXSException;
	public CPRedirectResponse getSamlUrl(AppSessionBean asb, Map<String, String> parameters) throws AtWinXSException;//CAP-44686
}
