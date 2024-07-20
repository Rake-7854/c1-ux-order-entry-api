/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/23/23				C Codina				CAP-44742					Initial Version
*/
package com.rrd.c1ux.api.services.messages;

import com.rrd.c1ux.api.models.messages.CarouselResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CarouselMessagesService {

	public CarouselResponse getCarouselMessages(SessionContainer sc)
			throws AtWinXSException;

}
