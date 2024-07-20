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
 * 	07/09/24	A Boomker			CAP-46538		Initial version - moved load imprint history here from regular
 */
package com.rrd.c1ux.api.services.custdocs;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBasicImprintHistorySearchRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocImprintHistorySearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CustomDocsImprintHistoryService extends CustomDocsBaseService {
	// moved here from regular service for consistency
	public C1UXCustDocBaseResponse getSelectedImprintHistory(SessionContainer sc, C1UXCustDocLoadImprintHistoryRequest request) throws AtWinXSException;
	public C1UXCustDocImprintHistorySearchResponse basicImprintHistorySearch(SessionContainer sc, C1UXCustDocBasicImprintHistorySearchRequest request) throws AtWinXSException;
}
