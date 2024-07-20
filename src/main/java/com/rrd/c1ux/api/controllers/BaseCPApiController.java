/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/11/22				A Boomker			      CAP-35766 				Modification for Punchout flags
 *	10/25/22				A Boomker				CAP-36153					Make session reader visible
 *  12/20/22    			M Sakthi        		CAP-37794					Update User Profile Controller (User state service) with common permission flags needed across application
 *  02/24/23                C Porter                CAP-38897                   HTTP 403 response handling
 *	04/26/23				A Boomker				CAP-40080					Changed getOrderEntrySession() to take session ID
 *	06/01/23				A Salcedo				CAP-39210					Added checkRedirectAllowed().
 *	07/25/23				A Boomker				CAP-42223					Changed visibility so one API can call another
 *  01/05/24				Krishna Natarajan		CAP-46263					To use settings for order originator for both Order For Another User and Admin settings
 *  04/25/24				Krishna Natarajan		CAP-48805					Added a new method getSessionContainerNoService() to get the session container without service
 */
package com.rrd.c1ux.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.users.UserContext;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.admin.entity.MenuGroups;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public abstract class BaseCPApiController extends BaseApiController{

	private static final Logger logger = LoggerFactory.getLogger(BaseCPApiController.class);

    protected final TokenReader mTokenReader;
    protected final CPSessionReader mSessionReader;

	protected BaseCPApiController(
        TokenReader tokenReader,
        CPSessionReader sessionReader) {

        mTokenReader = tokenReader;
        mSessionReader = sessionReader;
    }

    protected CustomizationToken getToken() {

        return mTokenReader.getToken();
    }

    protected SiteBUGroupLoginProfileVO getSession() throws AtWinXSException {

        UserContext userContext = mSessionReader.getUserContext();

        return mSessionReader.getSession(userContext);
    }

    protected UserContext getUserContext() {

        return mSessionReader.getUserContext();
    }

    public SessionContainer getSessionContainer(String encryptedSessionID) throws AtWinXSException {

        SessionContainer sc = mSessionReader.getSessionContainer(encryptedSessionID, this.getServiceID());

        if (!this.checkAccessAllowed(sc.getApplicationSession().getAppSessionBean())) {
          throw new AccessForbiddenException("Access to this service is not allowed", "");
        }

    	return sc;
    }

    /**
     * Check if session can access the services of implementing controller. Default behavior is to
     * always allow access. If a controller that extends this class needs to check for access to a service,
     * then this method can be overridden to supply a new implementation.
     *
     * As an example, a basic implementation could be:
     *
     *   <pre>return asb.hasService(getServiceID());</pre>
     *
     * @param asb - ApplicationSessionBean
     * @return true, if the session is allowed access
     */
    protected boolean checkAccessAllowed(AppSessionBean asb) {

      return true;

    }

    // CAP-35766
    protected ApplicationSession getApplicationSession() throws AtWinXSException {
    	SessionContainer sc = mSessionReader.getSessionContainer(AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.APPSESSIONSERVICEID);
    	if (sc != null)
    	{
    		return sc.getApplicationSession();
    	}
    	return null;
    }

   //CAP-37794
    protected OrderEntrySession getOrderEntrySession(String ttSession) throws AtWinXSException {
		try {
			SessionContainer sc = mSessionReader.getSessionContainer(ttSession ,AtWinXSConstant.ORDERS_SERVICE_ID);
			if (sc != null)
	    	{
				return (OrderEntrySession) sc.getModuleSession();
	    	}
			 return null;
		} catch (AtWinXSException ae) {
			logger.error(this.getClass().getName() + " - " + ae.getMessage(),ae);
			return null;
		}
    }

    //CAP-39210
    protected boolean checkAdminRedirectAllowed(SessionContainer sc) throws AtWinXSException
    {
    	AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		MenuGroups menuGroups = ObjectMapFactory.getEntityObjectMap().getEntity(MenuGroups.class, asb.getCustomToken());
		OEResolvedUserSettingsSessionBean userSettings = AdminUtil.getUserSettings(
				new LoginVOKey(asb.getSiteID(), asb.getOriginatorProfile().getLoginID()), asb.getSessionID(),
				asb.getCustomToken()); // CAP-46263

    	return (asb.hasService(AtWinXSConstant.ORDER_SEARCH_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.INVENTORY_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.CATALOGS_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.ITEM_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.KITS_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.LISTS_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.CAMPAIGNS_SERVICE_ID)
    			|| asb.hasService(AtWinXSConstant.UPLOAD_IMAGES_SERVICE_ID)
    			|| (asb.isAccountAdmin() && asb.hasService(AtWinXSConstant.ADMIN_SERVICE_ID))
    			|| (asb.isAccountAdmin() && asb.isCanAdminCustomDoc())
    			|| (asb.getAllowLoginLinking().equalsIgnoreCase(AdminConstant.LOGIN_LINKING_ADMIN_USER))
    			|| menuGroups.allowConfigureMenu(asb, userSettings)
    			|| menuGroups.allowResourcesMenu(asb, userSettings));
    }

    protected abstract int getServiceID();
    
    //CAP-48805
	public SessionContainer getSessionContainerNoService(String encryptedSessionID) throws AtWinXSException {
		return mSessionReader.getSessionContainer(encryptedSessionID, this.getServiceID());
	}
}
