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
 *  05/19/22    S Ramachandran  CAP-34140   Initial Creation 
 */

package com.rrd.c1ux.api.services.items;

import com.rrd.c1ux.api.models.items.PNARequest;
import com.rrd.c1ux.api.models.items.PNAResponse;
import com.rrd.c1ux.api.services.common.exception.CORPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;

public interface PNAProcessor {

 public PNAResponse processPNA(PNARequest pnaRequest, SessionContainer mainSession) 
		 throws CORPCException;
 
}
