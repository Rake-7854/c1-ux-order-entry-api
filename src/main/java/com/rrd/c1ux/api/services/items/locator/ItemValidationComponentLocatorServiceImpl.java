/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  02/19/24	S Ramachandran	CAP-47145 	Initial version
 */

package com.rrd.c1ux.api.services.items.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IItemValidation;
import com.wallace.atwinxs.items.locator.ItemValidationComponentLocator;

@Service
public class ItemValidationComponentLocatorServiceImpl implements ItemValidationComponentLocatorService {

	@Override
	public IItemValidation locate(CustomizationToken token) throws AtWinXSException {

		return ItemValidationComponentLocator.locate(token);
	}
}
