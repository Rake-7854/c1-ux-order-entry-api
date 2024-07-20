/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	------------------------------------------------------------------------------------
 *	02/14/24	S Ramachandran		 CAP-47145	Initial Version. created serviceImpl to return list of orders used in current allocation
 */

package com.rrd.c1ux.api.services.singleitem;

import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SingleItemDetailAllocationsService {

	public SingleItemDetailAllocationsResponse retrieveItemAllocations(SessionContainer sc, 
			SingleItemDetailAllocationsRequest singleItemDetailAllocationsRequest) throws AtWinXSException;
}

