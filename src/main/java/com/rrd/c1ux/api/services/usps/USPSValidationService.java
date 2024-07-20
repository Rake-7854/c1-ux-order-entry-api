/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By					JIRA#			Description
 * 	--------	---------------------		-----------		------------------------------------------------
 *	12/22/23	S Ramachandran				CAP-46081		Service for USPS validation
 */

package com.rrd.c1ux.api.services.usps;

import com.rrd.c1ux.api.models.usps.USPSValidationRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.custompoint.gwt.common.entity.Address;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

public interface USPSValidationService {
	
	public USPSValidationResponse validateUSPS(SessionContainer sc, USPSValidationRequest request) throws AtWinXSException;
	
	public USPSValidationResponse validateUSAddressV1(Address newAddress, AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, OECheckoutAssembler checkoutAssembler,
			boolean isOverrideUSPSErrors) throws AtWinXSException;
}