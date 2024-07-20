/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  10/18/2023  C Porter        CAP-44260                   Allow for custom Content Security Policies by site
 */

package com.rrd.c1ux.api.services.util;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.XSProperties;

public interface PropertyUtilService {

	XSProperties getProperties(String bundleName) throws AtWinXSException;

}