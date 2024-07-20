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
 *  10/03/23	Satishkumar A	CAP-43282	C1UX BE - API Build - Get OE Item Filter Options - including favorites, featured types
 */
package com.rrd.c1ux.api.services.standardattributes;

import java.lang.reflect.InvocationTargetException;

import com.rrd.c1ux.api.models.standardattributes.StandardAttributesResponse;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface StandardAttributesService {

	public StandardAttributesResponse getStandardAttributeList(SessionContainer sc) throws CPRPCException, AtWinXSException, IllegalAccessException, InvocationTargetException;
}
