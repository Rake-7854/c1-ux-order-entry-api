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
 * 	05/11/22	Krishna Natarajan	CAP-34136	    Created as per the requirement to fetch the items detail
 */
package com.rrd.c1ux.api.services.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.items.ItemNameToGetItemDetailWithQuantityRequest;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.items.locator.ManageItemsInterfaceLocator;
import com.wallace.atwinxs.reports.vo.ItemRptVO;
import com.wallace.atwinxs.reports.vo.ItemRptVOKey;

/**
 * @author Krishna Natarajan
 *
 */
@Service
public class GetItemDetailWithQuantityServiceImpl implements GetItemDetailWithQuantityService{
	
	private static final Logger logger = LoggerFactory.getLogger(GetItemDetailWithQuantityServiceImpl.class);

	/**
	 * @param sc {@link SessionContainer}
	 * @param request {@link ItemNameToGetItemDetailWithQuantityRequest}
	 * @return details of ItemRptVO
	 * @throws AtWinXSException
	 */
	public ItemRptVO getItemDetailWithQuantity(SessionContainer sc, ItemNameToGetItemDetailWithQuantityRequest request) throws AtWinXSException {
		
		logger.debug("inside GetItemDetailWithQuantity() ");
		
		IManageItemsInterface itemInterface = ManageItemsInterfaceLocator.locate(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		ItemRptVOKey rptKey = new ItemRptVOKey(request.getWallaceItemNumber());
		ItemRptVO vo = itemInterface.getWCSSItem(rptKey, sc.getApplicationSession().getAppSessionBean().getCorporateNumber(), null, false);
		return vo;
	}

}
