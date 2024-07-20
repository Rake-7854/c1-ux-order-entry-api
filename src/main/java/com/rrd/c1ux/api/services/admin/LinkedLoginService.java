/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/27/24	L De Leon			CAP-49609				Initial Version
 *	05/28/24	Satishkumar A		CAP-49610				C1UX API - Create API to login as a linked login ID/user
 *	05/31/24	Satishkumar A		CAP-49731				C1UX BE - Create API to login as a linked login ID/user
 */
package com.rrd.c1ux.api.services.admin;

import java.util.Map;

import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.c1ux.api.models.admin.LinkedLoginResponse;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserRequest;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.tt.arch.TTException;

public interface LinkedLoginService {

	public LinkedLoginResponse getLinkedLogins(SessionContainer sc) throws AtWinXSException;
	
	//CAP-49731
	public LoginLinkedUserResponse loginLinkedUser(SessionContainer sc, LoginLinkedUserRequest loginLinkedUserRequest) throws AtWinXSException, TTException;
	public CPRedirectResponse getSamlUrl(AppSessionBean asb, Map<String, String> parameters, String targetLoginID) throws AtWinXSException;

}