/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  05/05/22    S Ramachandran  CAP-34048   Initial Creation,  Get User Full Profile 
 *  05/11/23    Sakthi M        CAP-40524	C1UX BE - API Change - Convert Full Profile API to only return the user's own information
 *  12/07/23    S Ramachandran  CAP-45485   Fix code to only search/use originator profile when doing self administration
 */

package com.rrd.c1ux.api.services.users;

import java.lang.reflect.InvocationTargetException;

import com.rrd.c1ux.api.models.users.UserFullProfileResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface UserFullProfileProcessor {

	public UserFullProfileResponse processUserFullProfile(SessionContainer mainSession, boolean useOriginatorProfile) 
			throws AtWinXSException,IllegalAccessException, InvocationTargetException;

}
