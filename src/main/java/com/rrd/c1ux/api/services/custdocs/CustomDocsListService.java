/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	04/22/24	R Ruth				CAP-42226		Initial version
 *	05/28/24	A Boomker			CAP-48604		Add save new list API
 */
package com.rrd.c1ux.api.services.custdocs;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXSaveListResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CustomDocsListService extends CustomDocsBaseService{
	public C1UXCustDocListResponse getListsApi(SessionContainer sc) throws AtWinXSException;
	public C1UXSaveListResponse saveNewList(SessionContainer sc) throws AtWinXSException;

}
