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
 *  04/21/22    S Ramachandran  CAP-33763   Initial Creation, added to get PNATiered Item 
 */

package com.rrd.c1ux.api.services.items;

import com.rrd.c1ux.api.models.items.PNATieredPriceRequest;
import com.rrd.c1ux.api.models.items.PNATieredPriceResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface PNATieredProcessor {

 public PNATieredPriceResponse processPNATierPrice(PNATieredPriceRequest request, SessionContainer mainSession) 
		 throws AtWinXSException;

}
