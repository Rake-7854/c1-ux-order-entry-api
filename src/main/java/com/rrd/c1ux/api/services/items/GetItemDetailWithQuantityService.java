/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	05/11/22	Krishna Natarajan	CAP-34136	    Created a service interface as per the requirement to fetch the items detail
 */
package com.rrd.c1ux.api.services.items;

import com.rrd.c1ux.api.models.items.ItemNameToGetItemDetailWithQuantityRequest;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

/**
 * @author Krishna Natarajan
 *
 */
public interface GetItemDetailWithQuantityService {
	public ItemRptVO getItemDetailWithQuantity(SessionContainer sc, ItemNameToGetItemDetailWithQuantityRequest request) throws AtWinXSException;
}
