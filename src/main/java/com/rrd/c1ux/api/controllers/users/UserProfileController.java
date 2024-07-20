/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		JIRA#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	10/11/22	A Boomker		CAP-35766 					Modification for Punchout flags
 *  10/25/22	A Boomker		CAP-36153					Add entry point
 *  11/23/22    S Ramachandran  CAP-37370   				Determine if the BU stylesheet exists for user
 *  12/20/22    M Sakthi        CAP-37794					Update User Profile Controller (User state service) with common permission flags needed across application
 *  01/10/23    M Sakthi        CAP-38122					Update User Profile Controller (User state service) with common locale flags needed across application
 *  04/03/23    Satishkumar A   CAP-39182                   User Profile (User state service) needs to add shared ID description and admin service flag in response
 *	04/04/23	A Boomker		CAP-39673					Update response for shared IDs with profile selected
 *	04/26/23	A Boomker		CAP-40080					Add ability to override ttsession on user state service call, response must include returnText
 *	07/13/23	C Codina		CAP-41589					API Change - Add response flag to User state service indicating user has access to PAB
 * 	08/30/23	Krishna Natarajan	CAP-43371				Added service to send back translation
 * 	01/01/24	S Ramachandran	CAP-46119   				Modify user profile method to get originator profile 
 * 	02/09/24 	Krishna Natarajan	CAP-47091				Allow Catalog Search flag added to the response
 * 	05/30/24	C Codina		CAP-49744					API Change - Add suggestedItemSetting variable to UserStateProfile response
 * 	06/03/24	Satishkumar A	CAP-49851					Added flag to indicate if user has linked logins or not
 *  06/19/24	Krishna Natarajan	CAP-50338				Added Allow Budget Allocations flag to indicate true/false on profile call
 *  06/24/24    Rakesh K M        CAP-50368                 Added Allow Order On Behalf flag to indicate true/false on profile call 
 *  07/03/24	Krishna Natarajan	CAP-50754				Added Allow Order Wizard flag to indicate true/false on profile call          
 */
package com.rrd.c1ux.api.controllers.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.users.UserContext;
import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.rrd.c1ux.api.models.users.mappers.UserStateProfileMapper;
import com.rrd.c1ux.api.services.login.saml.SamlSpService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.users.UserStateProfileService;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("UserProfileController")
@RequestMapping(RouteConstants.USERS_PROFILE)
@Tag(name = "User State Service - users/profile")
public class UserProfileController extends BaseCPApiController {
	@Autowired SamlSpService samlSpService;

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private final UserStateProfileMapper mUserProfileMapper;
    private final UserStateProfileService mUserStateProfileForTran; //CAP-43371 Added service to send back translation 

	protected UserProfileController(TokenReader tokenReader, CPSessionReader sessionReader,
			UserStateProfileMapper userProfileMapper,UserStateProfileService userStateProfileForTran) {
		super(tokenReader, sessionReader);
		this.mUserProfileMapper = userProfileMapper;
		this.mUserStateProfileForTran = userStateProfileForTran;
	}

    @GetMapping(
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
        summary = "Get user profile for the user session in context")
    public UserStateProfile getProfile(
    		@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        logger.debug("In getProfile");
        // CAP-40080 - need to handle session override
        SiteBUGroupLoginProfileVO userSession = null;
        ApplicationSession appSession = null;
        String entryPoint = null;
        boolean useOriginatorProfile = true;
        boolean requestorModeModified = false;
        
        if (!Util.isBlankOrNull(ttsession))
        { // when passing in an override, use it
           	SessionContainer sc = getSessionContainer(ttsession);
            appSession = sc.getApplicationSession();

            //CAP-46119 - Force to use originator profile in Self Admin for OOB Mode
    		requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(
    				useOriginatorProfile, appSession.getAppSessionBean());
            
    		userSession = getSession(sc);
            entryPoint = appSession.getAppSessionBean().getEntryPoint();
        }
        else
        { // otherwise create and store the new session from the redirect
            
        	appSession = getApplicationSession();
            //CAP-46119 - Force to use originator profile in Self Admin for OOB Mode
    		requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(
    				useOriginatorProfile, appSession.getAppSessionBean());

        	userSession = getSession();
            entryPoint = getEntryPoint(getUserContext()); // CAP-36153
        }

        UserStateProfile response = mUserProfileMapper.fromSiteBUGroupLoginProfileVO(userSession, appSession);
        
        //CAP-49851
        response.setAllowLinkedLogins(!appSession.getAppSessionBean().getAllowLoginLinking().equals(AdminConstant.LOGIN_LINKING_NO));
        
        setSessionValues(response, appSession, entryPoint);
        response = mUserStateProfileForTran.getTranslation(response, appSession);
        
        //CAP-46119 - Revert originator to Requestor profile after Self Admin process in OOB Mode 
        SelfAdminUtil.revertOriginatorProfileInSelfAdmin(appSession.getAppSessionBean(), 
        		requestorModeModified);
        
      //CAP-47091
        OrderEntrySession oeSession = getOrderEntrySession(getApplicationSession().getAppSessionBean().getEncodedSessionId());
        if(oeSession != null) {
	 	    OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		    OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		    response.setAllowCatalogSearch(userSettings.isAllowCatalogSearch());
		    response.setAllowBudgetAllocations(userSettings.isAllowBudgetAllocations() && userSettings.isAllowAllocationsInd());//CAP-50338
		    response.setAllowOrderWizard(oeSession.getOESessionBean().getUserSettings().isAllowOrderWizard());//CAP-50754
        }//CAP-47091
        
      //CAP-50368
		OEResolvedUserSettingsSessionBean settings = AdminUtil.getUserSettings(
				new LoginVOKey(appSession.getAppSessionBean().getSiteID(),
						appSession.getAppSessionBean().getOriginatorProfile().getLoginID()),
				appSession.getAppSessionBean().getSessionID(), appSession.getAppSessionBean().getCustomToken());
		response.setAllowOrderOnBehalf(settings.isAllowOrderOnBehalf());     
        return response;
    }

    private void setSessionValues(UserStateProfile response, ApplicationSession appSession, String entryPoint) throws AtWinXSException
    {
    	AppSessionBean appSessionBean = appSession.getAppSessionBean();
        setSharedProfileOverrides(response, appSessionBean); // CAP-39673
        setPunchoutValue(response, appSession);

        setBuStyleSheetExists(appSessionBean.getBuID(), response);
        
        //CAP-37794
        response.setShowVendorItemNumber(appSession.getAppSessionBean().isShowWCSSItemNumber());
        //CAP-39182
        response.setAdminService(appSessionBean.hasService(AtWinXSConstant.ADMIN_SERVICE_ID));

        OrderEntrySession oeSession = getOrderEntrySession(appSessionBean.getEncodedSessionId());
        if(oeSession != null) {
	 	    OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		    OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		    response.setShowAvailability(userSettings.isShowOrderLineAvailability());
		    response.setShowPricing(userSettings.isShowOrderLinePrice());
		  
		    //CAP-49744
		    String suggestedItemsDsply = userSettings.getSugItemsDfltDsply().toUpperCase();
		    if("L".equals(suggestedItemsDsply) || "B".equals(suggestedItemsDsply)){
		    	response.setSuggestedItemSetting(suggestedItemsDsply);
		    }
		    
        }
        
       //CAP-38122
        response.setDefaultLocale(appSessionBean.getDefaultLocale().toString());
        response.setCurrencyLocale(appSessionBean.getCurrencyLocale().toString());
        response.setDefaultTimezone(appSessionBean.getDefaultTimeZone());
        // CAP-40080
        response.setEntryPoint(entryPoint);
        response.setReturnText(appSessionBean.getReturnText());
		
        //CAP-46119 - Force to use originator profile in Self Admin for OOB Mode
        // CAP-41589
		boolean isAllowPAB = false;
		if (appSessionBean.getOriginatorProfile().getProfileNumber() > 0) {
			
			isAllowPAB = appSessionBean.getOriginatorOrdProp().isUsePersonalAddrBook();
		}
		response.setAllowPAB(isAllowPAB);
	}

    //CAP-46119 - Force to use originator profile in Self Admin for OOB Mode
	// CAP-39673 - update response if shared ID has a profile selected
    private void setSharedProfileOverrides(UserStateProfile userStateProfile, AppSessionBean appSessionBean)
    {
    	if ((appSessionBean.getOriginatorProfile().isSharedID()) 
				&& (appSessionBean.getOriginatorProfile().getProfileNumber() > AtWinXSConstant.INVALID_PROFILE_NUMBER)) {
    		
            userStateProfile.setFirstName(appSessionBean.getOriginatorProfile().getFirstName() );
            userStateProfile.setLastName(appSessionBean.getOriginatorProfile().getLastName() );
            userStateProfile.setPhoneNumber(appSessionBean.getOriginatorProfile().getPhoneNumber() );
            userStateProfile.setEmailAddress(appSessionBean.getOriginatorProfile().getEmailAddress() );
            userStateProfile.setProfileID(appSessionBean.getOriginatorProfile().getProfileID() );
		}
	}

	// CAP-36153
	private String getEntryPoint(UserContext userContext) {
		if ((userContext != null) && (!Util.isBlankOrNull(userContext.getEntryPoint())))
		{
			return samlSpService.getEntryPointRouting(userContext.getEntryPoint(), mSessionReader);
		}
		return null;
	}

	private void setPunchoutValue(UserStateProfile response, ApplicationSession appSession) {
		if (appSession.getPunchoutSessionBean() != null)
		{
			response.setPunchoutOperation(appSession.getPunchoutSessionBean().getOperation());
		}
	}

	@Value("${uri.stylesheets.path}")
	private String uriStyleSheetsPath;
	@Autowired
	ResourceLoader resourceLoader;

	private void setBuStyleSheetExists(int buId, UserStateProfile response) {
		Resource file = resourceLoader.getResource(uriStyleSheetsPath + "c1ux." + buId + ".css");
		response.setBuStyleSheetExists(file.exists());
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ADMIN_SERVICE_ID;
	}

    protected SiteBUGroupLoginProfileVO getSession(SessionContainer sc) throws AtWinXSException {
         return mSessionReader.getSession(sc);
    }

}
