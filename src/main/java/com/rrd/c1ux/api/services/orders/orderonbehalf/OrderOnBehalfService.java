/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	11/27/23	L De Leon			CAP-44467				Initial Version
 *	12/22/23 	Satishkumar A		CAP-45709				C1UX BE - Set OOB Mode for CustomPoint session
 */
package com.rrd.c1ux.api.services.orders.orderonbehalf;

import com.rrd.c1ux.api.models.orders.oob.OOBRequest;
import com.rrd.c1ux.api.models.orders.oob.OOBResponse;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchRequest;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface OrderOnBehalfService {

	public OrderOnBehalfSearchResponse getOOBInfo(SessionContainer sc, OrderOnBehalfSearchRequest request)
			throws AtWinXSException;
	//CAP-45709
	public OOBResponse setOrderForSelfOrOOBMode(SessionContainer sc, OOBRequest request)
			throws AtWinXSException;
}
