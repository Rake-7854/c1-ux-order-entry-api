/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		 Modified By		Jira#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	2022.08.16	 T Harmon		 CAP-35537					 Initial creation
 *  2022.09.16   E Anderson      CAP-35362                   Add account.
 *  10/25/22	A Boomker		CAP-36153					Add entry point
 *  2022.10.25   M Sakthi	     CAP-36418 					 create keepAlive API call to help front end implement
 *  														 session timeout - update session timestamp
 *  10/26/2022	Krishna Natarajan CAP-36844					Checking if profileVO.getKey() is null to execute
 *  11/8/2022	T Harmon		CAP-35710					Added code to allow session timeout validation and to update timestamp for CP session.
 *  11/10/2022  M Sakthi        CAP-37029                   create timeout API call to help front end implement session timeout - update session status on table
 *  03/17/2023  C Porter        CAP-39295                   Handle "Could not load session" Exception
 *  04/05/2023  C Porter        CAP-39674                   Intermittent Session Timeout fix
 *  04/18/2023  C Porter        CAP-39954                   Occasional session timeout fix
 *  04/26/23	A Boomker		CAP-40080					Added getSession(SessionContainer)
 *  05/31/24	Satishkumar A	CAP-49731					create timeout API call to logout the current user session - update session status on table
 */


package com.rrd.c1ux.api.services.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.config.properties.ClaimsProperties;
import com.rrd.c1ux.api.exceptions.AtWinXSSessionException;
import com.rrd.c1ux.api.exceptions.ErrorCodeHandler;
import com.rrd.c1ux.api.models.users.UserContext;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.wallace.atwinxs.admin.vo.BusinessUnitVO;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.admin.vo.UserGroupVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IBusinessUnit;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.interfaces.IUserGroup;
import com.wallace.tt.arch.TTConstants;
import com.wallace.tt.arch.TTException;
import com.wallace.tt.arch.web.TTPerformanceTracker;
import com.wallace.tt.vo.TTSession;

@Service
public class CPSessionReaderImpl implements CPSessionReader {

    private static final String MSG_INVALID_SESSION_CONTAINER_SPECIFIED = "Invalid session container specified";
    // CAP-35710
    private static final String MSG_INVALID_SESSION_EXPIRED	= "Session expired or invalid";

	private static final Logger logger = LoggerFactory.getLogger(CPSessionReaderImpl.class);

    private final ClaimsProperties claimsProps;

    private final SiteReader mSiteReader;
    private final LoginReader mLoginReader;
    private final ProfileReader mProfileReader;
    private final TokenReader mTokenReader;
    private final IAdminComponentLocator mAdminComponentLocator;
    private final SessionHandlerService sessionHandlerService;

    public CPSessionReaderImpl(
        SiteReader siteReader,
        LoginReader loginReader,
        ProfileReader profileReader,
        TokenReader tokenReader,
        IAdminComponentLocator adminComponentLocator,
        SessionHandlerService sessionHandlerService,
        ClaimsProperties claimsProperties
    ) {
        super();
        mSiteReader = siteReader;
        mLoginReader = loginReader;
        mProfileReader = profileReader;
        mTokenReader = tokenReader;
        mAdminComponentLocator = adminComponentLocator;
        this.sessionHandlerService = sessionHandlerService;
        this.claimsProps = claimsProperties;
    }

    public Optional<Saml2AuthenticatedPrincipal> getSamlAuthenticatedPrincipal() {

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication != null && authentication.isAuthenticated()) {

        Object principal = authentication.getPrincipal();

        if (principal instanceof Saml2AuthenticatedPrincipal) {
          return Optional.of((Saml2AuthenticatedPrincipal) principal);
        }
      }

      return Optional.empty();
    }

    /**
     * Builds the session object from the user's site, business, group, profile info in the DB.
     *
     * NOTE: should be cached BUT DOES NOT APPEAR TO BE WORKING!! - see ehcacheOEUX.xml
     * @throws AtWinXSException
     */
    @Override
    @Cacheable("sessions")
    public SiteBUGroupLoginProfileVO getSession(UserContext userContext) throws AtWinXSException {

        //TODO: change this to 'debug' after fixing the cache issue
        logger.info("In getSession for {}", userContext);

        ErrorCode errorCode = null;

        // Attempt to retrieve the site here - if site doesn't exist, method
        // throws an error
        SiteVO siteVO = mSiteReader.getSiteForAccount(userContext.getAccount());

        // Get login vo here
        LoginVO loginVO = mLoginReader.getLoginVO(siteVO.getSiteID(), userContext.getUserName());

        // Get the profile vo here (this will always return the profile attached to the login).
        ProfileVO profileVO = mProfileReader.getProfileVO(loginVO, userContext.getProfileId());

        // Finally, check passed in profile against login
        CustomizationToken token = mTokenReader.getToken();
        if (loginVO.isSharedID())
        {
            // Get profile interface
            IProfileInterface iProfile = mAdminComponentLocator.locateProfileComponent(token);

			if (profileVO != null) {// CAP-36844
				// If shared, profile should be unattached
				if (!iProfile.isProfileUnattached(profileVO.getKey())) {
					// Create an error message with the appropriate error code.
					Map<String, Object> replaceMap = new HashMap<>();
					replaceMap.put(ErrorCodeConstants.REP_TAG_USER_NAME, loginVO.getLoginID());
					errorCode = new ErrorCode(ErrorCodeConstants.CTX_BASE_SERVICES_PROCESSOR,
							ErrorCodeConstants.ERR_NAME_ATTACHED_PROFILE_FOR_SHARED_USER, replaceMap);
				}
			}
        }
        else
        {
            // For a named user, if the profile passed in is not blank,
            // make sure the passed in profile ID is the profile ID attached to the user.
            if (!StringUtils.isBlank(userContext.getProfileId())
                && !profileVO.getProfileID().equals(userContext.getProfileId()))
            {
                // Create an error message with the appropriate error code.
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(ErrorCodeConstants.REP_TAG_USER_NAME, loginVO.getLoginID());
                replaceMap.put(ErrorCodeConstants.REP_TAG_PROFILE_ID, userContext.getProfileId());
                errorCode = new ErrorCode(ErrorCodeConstants.CTX_BASE_SERVICES_PROCESSOR,
                            ErrorCodeConstants.ERR_NAME_INVALID_PROFILE_FOR_USER,
                            replaceMap);
            }
        }

        // If the error code isn't null, throw an error here
        if (errorCode != null)
        {
            ErrorCodeHandler.throwFromErrorCode(errorCode, getClass());
        }

        // Get the user group information here
        IUserGroup iUserGroup = mAdminComponentLocator.locateUserGroupComponent(token);
        UserGroupVO userGroupVO = iUserGroup.getUserGroup(new UserGroupVOKey(loginVO.getSiteID(), loginVO.getBusinessUnitID(), loginVO.getUserGroupName()));

        // Get the business unit information here
        IBusinessUnit iBusinessUnit = mAdminComponentLocator.locateBusinessUnitComponent(token);
        BusinessUnitVO buVO = iBusinessUnit.getBusinessUnitByLogin(loginVO.getKey());

        // Finally, get the session settings for this user here
        return new SiteBUGroupLoginProfileVO(siteVO, buVO, userGroupVO, loginVO, profileVO);
    }

    /**
     * Parse the authentication object in the security context for the username and CustomPoint account
     * @return The username and CP account in a UserContext object
     */
    @Override
    public UserContext getUserContext() {

      String username = AtWinXSConstant.EMPTY_STRING;
      String org = AtWinXSConstant.EMPTY_STRING;
      String cpSessionId = AtWinXSConstant.EMPTY_STRING;
      String firstName = AtWinXSConstant.EMPTY_STRING;
      String lastName = AtWinXSConstant.EMPTY_STRING;
      String email = AtWinXSConstant.EMPTY_STRING;
      String account = AtWinXSConstant.EMPTY_STRING; //CAP-35362
      String entryPt = AtWinXSConstant.EMPTY_STRING;

      Optional<Saml2AuthenticatedPrincipal> authentication = getSamlAuthenticatedPrincipal();

      if (authentication.isPresent()) {
        Saml2AuthenticatedPrincipal samlPrinc = authentication.get();

        username = samlPrinc.getName();
        org = samlPrinc.getFirstAttribute(claimsProps.getAccountClaimUri());
        cpSessionId = samlPrinc.getFirstAttribute(claimsProps.getCpSessionId());
        firstName = samlPrinc.getFirstAttribute(claimsProps.getFirstName());
        lastName = samlPrinc.getFirstAttribute(claimsProps.getLastName());
        email = samlPrinc.getFirstAttribute(claimsProps.getEmail());
        account = samlPrinc.getFirstAttribute(claimsProps.getAccount()); // CAP-35362
        entryPt = samlPrinc.getFirstAttribute(claimsProps.getEntryPoint());
      }

      return new UserContext(username, org, null, cpSessionId, firstName, lastName, email, account, entryPt);
    }

    @Override
    public SessionContainer getSessionContainer(String encryptedSessionID, int serviceID) throws AtWinXSException
    {
    	try
    	{
    		// CAP-35710 TH - Modified method to handle session better and to update timestamp
	    	if (Util.isBlankOrNull(encryptedSessionID))
	    	{
              // Try and lookup in spring session
              encryptedSessionID = getSamlAuthenticatedPrincipal().map(p -> p.getFirstAttribute(claimsProps.getCpSessionId()).toString())
                  .orElse(encryptedSessionID);
	    	}

	    	TTSession ttSession = getTTSession(encryptedSessionID);

			SessionContainer sc= getSessionContainer(serviceID, ttSession);
			sc.getApplicationSession().getAppSessionBean().setTTSession(ttSession);

			updateTTSession(sc);

			return sc;
    	}
    	catch(TTException ex)
    	{
    		throw new AtWinXSSessionException(new NullPointerException(MSG_INVALID_SESSION_CONTAINER_SPECIFIED), this.getClass().getName());
    	}
    }


    @Override
    public SessionContainer getSessionContainer(int serviceID, TTSession ttSession) throws AtWinXSException
    {
        SessionContainer sc = null;
        try {
          // CAP-35710 TH - Modified session handling a bit
          sc = sessionHandlerService.getFullSessionInfo(ttSession.getId(), serviceID);
        } catch (AtWinXSException ex) {
          throw new AtWinXSSessionException(ex, this.getClass().getName());
        }
		if(sc == null)
		{
			throw new AtWinXSWrpException(new NullPointerException(MSG_INVALID_SESSION_CONTAINER_SPECIFIED), this.getClass().getName());
		}

		// Validate session now, to make sure it is still valid once we have it loaded
		validateSession(sc, ttSession);

		// return the session container object
		return sc;
    }

    // CAP-35710 TH - Added code to validate session
    protected void validateSession(SessionContainer sc, TTSession ttSession) throws AtWinXSException
    {
    	double timeoutValue = ttSession.getTimeOutValue();
    	SiteVO siteVO = mSiteReader.getSiteForAccount(sc.getApplicationSession().getAppSessionBean().getSiteLoginID());

    	if (siteVO != null && siteVO.getTimeoutMinutes() > 0)
    	{
    		timeoutValue = (double) siteVO.getTimeoutMinutes() * 60;
    	}

    	if ((!ttSession.getStatus().equals(TTSession.ACTIVE))
   				|| (timeoutValue > 0 && ttSession.getElapsedTime() > timeoutValue))
   		{
    		TTException tte = new TTException(this, TTConstants.MF_ERROR, MSG_INVALID_SESSION_EXPIRED );

    		// Set status to expired
    		ttSession.setStatus(TTSession.ENDED_DUE_TO_TIMEOUT);
    		boolean isSuccessful = ttSession.update();

    		if (!isSuccessful)
    		{
    			logger.error("Update failure - Could not update TTSession to expired.", tte);
    		}

   			throw new AtWinXSSessionException(tte, this.getClass().getName());
		}
    }

    public TTSession getTTSession(String encryptedSessionID) throws AtWinXSException
	{
    	TTSession ttSession =  new TTSession();
		@SuppressWarnings("unused")
		TTPerformanceTracker ptracker =  new TTPerformanceTracker();
		String ttSessionID = Util.nullToEmpty(encryptedSessionID);
		ttSession.setTokenPassedIn(ttSession.decodeURL(ttSessionID));
		try {
			ttSession.setId(ttSession.parseIdFromToken(ttSession.getTokenPassedIn()));

	       	if (ttSession.select(ttSession.parseIdFromToken(ttSession.getTokenPassedIn()), ptracker)
	       			|| ttSession.select(ttSession.getId(), ptracker))
	       	{
	       		// retrieve session info
	       	}
		}
		catch (TTException te)
		{
			throw new AtWinXSSessionException(new NullPointerException(MSG_INVALID_SESSION_CONTAINER_SPECIFIED), this.getClass().getName());
		}
		return ttSession;
	}

  //CAP-36418 create keepAlive API call to help front end implement session timeout - update session timestamp
    @Override
    public boolean updateTTSession(SessionContainer sc) throws TTException
    {

    	TTSession ttSession =  new TTSession();
    	boolean updateTTSessionResp=false;
		@SuppressWarnings("unused")
		TTPerformanceTracker ptracker =  new TTPerformanceTracker();
		ttSession.setTokenPassedIn(sc.getApplicationSession().getAppSessionBean().getTtSession().getTokenPassedIn());
		ttSession.setTokenExpected(sc.getApplicationSession().getAppSessionBean().getTtSession().getTokenExpected());
		ttSession.setStatus(sc.getApplicationSession().getAppSessionBean().getTtSession().getStatus());
		ttSession.setId(sc.getApplicationSession().getAppSessionBean().getTtSession().getId());
		ttSession.setElapsedTime(sc.getApplicationSession().getAppSessionBean().getTtSession().getElapsedTime());
		ttSession.setTokenNext(sc.getApplicationSession().getAppSessionBean().getTtSession().getTokenNext());
		ttSession.setSite(sc.getApplicationSession().getAppSessionBean().getTtSession().getSite());
		ttSession.setTransactionCount(sc.getApplicationSession().getAppSessionBean().getTtSession().getTransactionCount());
		try {
			ttSession.standardSessionUpdate(ttSession.getTokenPassedIn());
			updateTTSessionResp=true;
		}catch (TTException e) {
			logger.debug("Update failure", e);
		}

		return updateTTSessionResp;
    }

    //CAP-37029 -create timeout API call to help front end implement session timeout - update session status on table
    @Override
    public boolean ttSessionTimeout(SessionContainer sc) throws TTException, AtWinXSException
    {
    	sc.getApplicationSession().getAppSessionBean().getTtSession().setStatus(TTSession.ENDED_DUE_TO_TIMEOUT);
    	return (sc.getApplicationSession().getAppSessionBean().getTtSession().update());
    }
    
    //CAP-49731 -create timeout API call to logout the current user session - update session status on table
    @Override
    public boolean ttSessionTimeoutLogout(SessionContainer sc) throws TTException, AtWinXSException
    {
    	sc.getApplicationSession().getAppSessionBean().getTtSession().setStatus(TTSession.ENDED_DUE_TO_LOGOUT);
    	return (sc.getApplicationSession().getAppSessionBean().getTtSession().update());
    }

    // CAP-40080 - this needs to be able to be overridden with encrypted session
	@Override
	public SiteBUGroupLoginProfileVO getSession(SessionContainer sc) throws AtWinXSException {
         AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
        // Attempt to retrieve the site here - if site doesn't exist, method
        // throws an error
        SiteVO siteVO = mSiteReader.getSiteForAccount(asb.getSiteLoginID());

        // Get login vo here
        LoginVO loginVO = mLoginReader.getLoginVO(siteVO.getSiteID(), asb.getLoginID());

        // Get the profile vo here (this will always return the profile attached to the login).
        ProfileVO profileVO = mProfileReader.getProfileVO(loginVO, asb.getProfileID());

        // Finally, check passed in profile against login
        CustomizationToken token = asb.getCustomToken();

        // Get the user group information here
        IUserGroup iUserGroup = mAdminComponentLocator.locateUserGroupComponent(token);
        UserGroupVO userGroupVO = iUserGroup.getUserGroup(new UserGroupVOKey(loginVO.getSiteID(), loginVO.getBusinessUnitID(), loginVO.getUserGroupName()));

        // Get the business unit information here
        IBusinessUnit iBusinessUnit = mAdminComponentLocator.locateBusinessUnitComponent(token);
        BusinessUnitVO buVO = iBusinessUnit.getBusinessUnitByLogin(loginVO.getKey());

        // Finally, get the session settings for this user here
        return new SiteBUGroupLoginProfileVO(siteVO, buVO, userGroupVO, loginVO, profileVO);
	}
}
