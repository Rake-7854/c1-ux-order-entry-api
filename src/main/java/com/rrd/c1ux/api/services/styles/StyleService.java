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
 *  09/01/22    S Ramachandran  CAP-35358   Get URL for components to load for standard style sheet
 */

package com.rrd.c1ux.api.services.styles;

import com.rrd.c1ux.api.models.styles.StandardStyleIncludeResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface StyleService {

	public StandardStyleIncludeResponse getStandardStyleIncludeURL(SessionContainer mainSession) throws AtWinXSException;
}
