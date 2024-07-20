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
 * 	08/07/23	A Boomker			CAP-42223		Initial version
 *  11/13/23	A Boomker			CAP-44426		Added handling for update working proof
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.Map;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocWorkingProofResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CustomDocsProofingService extends CustomDocsBaseService {
	public C1UXCustDocProofStatusResponse getCurrentImageProofStatus(SessionContainer sc) throws AtWinXSException;

	public C1UXCustDocProofLinkResponse getProofLink(SessionContainer sc, C1UXCustDocProofLinkRequest request) throws AtWinXSException;

	public C1UXCustDocWorkingProofResponse getWorkingProof(SessionContainer sc, Map<String, String> uiRequest) throws AtWinXSException;

}
