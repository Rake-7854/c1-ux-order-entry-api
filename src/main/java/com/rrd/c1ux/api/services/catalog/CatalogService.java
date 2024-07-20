/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/18/24				C Codina 				CAP-46379					C1UX BE - Method to retrieve Attribute Filters for Order Entry
 *	01/18/24				S Ramachandran			CAP-46304					Retrieve standard options to use for the Catalog Page
 */

package com.rrd.c1ux.api.services.catalog;

import java.lang.reflect.InvocationTargetException;

import com.rrd.c1ux.api.models.catalog.CatalogAttributeRequest;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeResponse;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CatalogService {
	
	public CatalogAttributeResponse getCatalogAttributes(SessionContainer sc, CatalogAttributeRequest request)
		throws AtWinXSException, IllegalAccessException, InvocationTargetException, CPRPCException;

}
