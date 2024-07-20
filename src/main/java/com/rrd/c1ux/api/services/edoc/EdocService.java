/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  10/17/23	Satishkumar A	CAP-44664	C1UX BE - Create api to retrieve edoc for Storefront
 */
package com.rrd.c1ux.api.services.edoc;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rrd.c1ux.api.models.edoc.EdocUrlResponse;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface EdocService {

	public EdocUrlResponse getEdocUrl(AppSessionBean appSessionBean, HttpServletRequest request, HttpServletResponse httpServletResponse) throws AtWinXSException , UnsupportedEncodingException;
}
