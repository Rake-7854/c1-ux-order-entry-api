/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  06/26/2023  C Codina		CAP-40613	Initial version
 */
package com.rrd.c1ux.api.services.items.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IOEPricingAndAvailability;
import com.wallace.atwinxs.orderentry.locator.OEPricingAndAvailabilityComponentLocator;

@Service
public class OEPricingAndAvailabilityComponentLocatorServiceImpl implements OEPricingAndAvailabilityComponentLocatorService{

	@Override
	public IOEPricingAndAvailability locate(CustomizationToken token) throws AtWinXSException {
		return OEPricingAndAvailabilityComponentLocator.locate(token);
	}

}
