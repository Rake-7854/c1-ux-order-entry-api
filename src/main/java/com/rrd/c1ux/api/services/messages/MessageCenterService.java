/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#						Description
 * 	--------	-----------				-----------------------		--------------------------------
 *	09/12/22	A Boomker				CAP-35436					Adding service for returning messages for message center
 *  09/13/22	Krishna Natarajan		CAP-35708					Adding service for returning messages for message flags
 */
package com.rrd.c1ux.api.services.messages;

import com.rrd.c1ux.api.models.messages.MessageCenterResponse;
import com.rrd.c1ux.api.models.messages.ShowMessageCenterResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface MessageCenterService {
	ShowMessageCenterResponse getShowResponse(SessionContainer sc) throws AtWinXSException;

	public MessageCenterResponse getMessagesResponse(SessionContainer sc) throws AtWinXSException;

}
