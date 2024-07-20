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
 * 	05/15/24	R Ruth				CAP-42228		Initial version
 * 	06/04/24	A Boomker			CAP-42231		Adding get mapped data page
 */
package com.rrd.c1ux.api.services.custdocs;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CustomDocsListMappingService extends CustomDocsBaseService {
	public C1UXCustDocSaveListMappingResponse saveListMapping(SessionContainer sc, C1UXCustDocSaveListMappingRequest request) throws AtWinXSException;
	public C1UXCustDocListForMappingResponse getListMappings(SessionContainer sc, int id) throws AtWinXSException;
	public C1UXCustDocMappedDataResponse getMappedDataPage(SessionContainer sc, C1UXCustDocMappedDataRequest request) throws AtWinXSException;
}
