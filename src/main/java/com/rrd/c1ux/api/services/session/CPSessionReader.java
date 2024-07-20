/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		Jira#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  11/8/2022	T Harmon		CAP-35710					Added code to allow session timeout validation and to update timestamp for CP session.
 *  03/17/2023  C Porter        CAP-39295                   Handle "Could not load session" Exception
 *  04/26/23	A Boomker		CAP-40080					Added getSession(SessionContainer)
 *  03/14/24	A Boomker		CAP-46526					Cust Docs needs to parse and compare ttsessionID in URL to user's session
 *  05/31/24	Satishkumar A	CAP-49731					create timeout API call to logout the current user session - update session status on table
 */

package com.rrd.c1ux.api.services.session;

import com.rrd.c1ux.api.models.users.UserContext;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.tt.arch.TTException;
import com.wallace.tt.vo.TTSession;

/**
 * Interface that defines methods for reading session related informtation.
 */
public interface CPSessionReader {

    SiteBUGroupLoginProfileVO getSession(UserContext userContext) throws AtWinXSException;

    UserContext getUserContext();

    // CAP-35710 TH - Modified session container to use TTSession better for validation and update of timestamp
    public SessionContainer getSessionContainer(int serviceID, TTSession ttSession) throws AtWinXSException;
    public SessionContainer getSessionContainer(String encryptedSessionID, int serviceID) throws AtWinXSException;
    public boolean updateTTSession(SessionContainer sc) throws TTException;//CAP-36418
    public boolean ttSessionTimeout(SessionContainer sc) throws TTException,AtWinXSException;//CAP-37029

    public SiteBUGroupLoginProfileVO getSession(SessionContainer sc) throws AtWinXSException; // CAP-40080

    public TTSession getTTSession(String encryptedSessionID) throws AtWinXSException; // CAP-46526

    //CAP-49731
    public boolean ttSessionTimeoutLogout(SessionContainer sc) throws TTException, AtWinXSException; //CAP-49731

}
