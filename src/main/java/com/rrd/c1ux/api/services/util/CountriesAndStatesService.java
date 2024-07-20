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
 * 	07/11/23	Satishkumar A      CAP-41970		C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 */
package com.rrd.c1ux.api.services.util;

import com.rrd.c1ux.api.models.util.CountriesAndStatesResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CountriesAndStatesService {

	public CountriesAndStatesResponse getCountriesAndStatesOrProvincesList(SessionContainer sc) throws AtWinXSException;
}
