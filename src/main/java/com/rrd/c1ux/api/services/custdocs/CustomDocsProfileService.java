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
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 * 	11/10/23	A Boomker			CAP-44487		Added load user profile
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 *	06/26/24	R Ruth				CAP-46503		Added loadAltProfiles
 */
package com.rrd.c1ux.api.services.custdocs;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUserProfileSearchRequest;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CustomDocsProfileService extends CustomDocsBaseService {
	public C1UXCustDocProfileSearchResponse searchUserProfiles(SessionContainer sc, C1UXCustDocUserProfileSearchRequest request) throws AtWinXSException;
	public C1UXCustDocLoadProfileResponse loadUserProfile(SessionContainer sc, C1UXCustDocLoadProfileRequest request) throws AtWinXSException;
	public C1UXCustDocLoadAltProfileResponse loadAltProfile(SessionContainer sc, C1UXCustDocLoadAltProfileRequest request) throws AtWinXSException;
}
