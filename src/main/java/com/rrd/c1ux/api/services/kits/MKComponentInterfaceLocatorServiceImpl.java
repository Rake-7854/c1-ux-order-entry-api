/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	06/14/24				S Ramachandran			CAP-50031					Initial Version
 */

package com.rrd.c1ux.api.services.kits;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.MKComponentInterface;
import com.wallace.atwinxs.kits.locator.MKComponentInterfaceLocator;

@Service
public class MKComponentInterfaceLocatorServiceImpl implements MKComponentInterfaceLocatorService {

	@Override
	public MKComponentInterface locate(CustomizationToken token) throws AtWinXSException {
		return MKComponentInterfaceLocator.locate(token);
	}
}
	