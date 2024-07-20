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
 *  09/13/22    S Ramachandran  CAP-35424   Get Landing page Information service Interface  
 */

package com.rrd.c1ux.api.services.landing;

import com.rrd.c1ux.api.models.addtocart.landing.LandingResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface LandingService {
		
	public LandingResponse loadLanding(SessionContainer sc) throws AtWinXSException;
	
}
