/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/27/24	L De Leon			CAP-49609				Initial Version
 *	05/31/24	Satishkumar A		CAP-49731				C1UX BE - Create API to login as a linked login ID/user
 */
package com.rrd.c1ux.api.services.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pingidentity.opentoken.Agent;
import com.pingidentity.opentoken.AgentConfiguration;
import com.pingidentity.opentoken.TokenException;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.COLinkedLogin;
import com.rrd.c1ux.api.models.admin.LinkedLoginResponse;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserRequest;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.admin.entity.LinkedLogin;
import com.rrd.custompoint.admin.entity.LinkedLogins;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.wallace.atwinxs.admin.locator.LinkedLoginLocator;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PingFederationConstants;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.ILinkedLogin;
import com.wallace.tt.arch.TTException;

@Service
public class LinkedLoginServiceImpl extends BaseOEService implements LinkedLoginService {
	
	//CAP-49731
	@Autowired
	CPSessionReader cpSessionReader;

	private static final Logger logger = LoggerFactory.getLogger(LinkedLoginServiceImpl.class);

	protected LinkedLoginServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService) {
		super(translationService, objectMapFactoryService);
	}

	@Override
	public LinkedLoginResponse getLinkedLogins(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		if (appSessionBean.getAllowLoginLinking().equals(AdminConstant.LOGIN_LINKING_NO)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		LinkedLoginResponse response = new LinkedLoginResponse();
		response.setSuccess(true);

		if (hasValidCart(sc)) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.LINKED_LOGIN_SWITCH_ERR,
					SFTranslationTextConstants.LINKED_LOGIN_SWITCH_DEF_ERR));
		}

		if (response.isSuccess()) {
			populateResponse(appSessionBean, response);
		}

		return response;
	}

	protected void populateResponse(AppSessionBean appSessionBean, LinkedLoginResponse response)
			throws AtWinXSException {
		response.setLoggedInUser(populateCOLinkedLogin(appSessionBean));
		response.setSelfRegURL(retrieveSelfRegUrl(appSessionBean));
		setLinkedLogins(appSessionBean, response);
	}

	protected void setLinkedLogins(AppSessionBean appSessionBean, LinkedLoginResponse response) {
		try {
			LinkedLogins linkedLogins = populateLinkedLogins(appSessionBean);

			if (null != linkedLogins.getLinkedLogins() && !linkedLogins.getLinkedLogins().isEmpty()) {
				List<COLinkedLogin> linkedLoginList = new ArrayList<>();
				for (LinkedLogin tempLinkedLogin : linkedLogins.getLinkedLogins()) {
					COLinkedLogin linkedLogin = populateCOLinkedLogin(tempLinkedLogin);
					linkedLoginList.add(linkedLogin);
				}
				response.setLinkedLogins(linkedLoginList);
			}
		} catch (AtWinXSException e) {
			logger.error(e.getMessage());
		}
	}

	protected LinkedLogins populateLinkedLogins(AppSessionBean appSessionBean) throws AtWinXSException {
		LinkedLogin currentLogin = objectMapFactoryService.getEntityObjectMap().getEntity(LinkedLogin.class,
				appSessionBean.getCustomToken());
		currentLogin.setUserID(appSessionBean.getLoginID());
		currentLogin.setSiteID(appSessionBean.getSiteID());

		LinkedLogins linkedLogins = objectMapFactoryService.getEntityObjectMap().getEntity(LinkedLogins.class,
				appSessionBean.getCustomToken());
		linkedLogins.populateLinkedLogins(currentLogin);
		return linkedLogins;
	}

	protected String retrieveSelfRegUrl(AppSessionBean appSessionBean) throws AtWinXSException {
		ILinkedLogin admin = getLinkedLoginLocator(appSessionBean);
		int llGroupID = admin.getlinkedLoginGroupID(appSessionBean.getSiteID(), appSessionBean.getLoginID());

		String selfRegUrl = AtWinXSConstant.EMPTY_STRING;
		if (!Util.isBlankOrNull(appSessionBean.getSelfRegistrationURL())) {
			StringBuilder selfRegUrlBuffer = new StringBuilder(appSessionBean.getSelfRegistrationURL());
			selfRegUrlBuffer.append("&linkedLoginGroupID=");
			selfRegUrlBuffer.append(llGroupID);
			selfRegUrlBuffer.append("&linkedLoginInitLoginID=");
			selfRegUrlBuffer.append(appSessionBean.getLoginID());

			selfRegUrl = appSessionBean.encodeURL(selfRegUrlBuffer.toString());
		}
		return selfRegUrl;
	}

	protected ILinkedLogin getLinkedLoginLocator(AppSessionBean appSessionBean) throws AtWinXSException {
		return LinkedLoginLocator.locate(appSessionBean.getCustomToken());
	}

	protected COLinkedLogin populateCOLinkedLogin(LinkedLogin linkedLogin) {
		return getCOLinkedLoginInfo(linkedLogin.getBusinessUnitName(), linkedLogin.getUserGroup(),
				linkedLogin.getUserID(), linkedLogin.getProfileID(), linkedLogin.getFirstName(),
				linkedLogin.getLastName(), linkedLogin.getEmail());
	}

	protected COLinkedLogin populateCOLinkedLogin(AppSessionBean appSessionBean) {
		return getCOLinkedLoginInfo(appSessionBean.getBuName(), appSessionBean.getGroupName(),
				appSessionBean.getLoginID(), appSessionBean.getProfileID(), appSessionBean.getFirstName(),
				appSessionBean.getLastName(), appSessionBean.getEmailAddress());
	}

	protected COLinkedLogin getCOLinkedLoginInfo(String buName, String ugName, String userID, String profileID,
			String fName, String lName, String email) {
		COLinkedLogin coLinkedLogin = new COLinkedLogin();
		coLinkedLogin.setBusinessUnitName(buName);
		coLinkedLogin.setUserGroup(ugName);
		coLinkedLogin.setUserID(userID);
		coLinkedLogin.setProfileID(profileID);
		coLinkedLogin.setFirstName(fName);
		coLinkedLogin.setLastName(lName);
		coLinkedLogin.setEmail(email);
		return coLinkedLogin;
	}

	//CAP-49731
	@Override
	public LoginLinkedUserResponse loginLinkedUser(SessionContainer sc, LoginLinkedUserRequest loginLinkedUserRequest)
			throws AtWinXSException, TTException {
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		String targetLoginID = loginLinkedUserRequest.getLoginLinkedUserID();

		LoginLinkedUserResponse response = new LoginLinkedUserResponse();
		
		if (appSessionBean.getAllowLoginLinking().equals(AdminConstant.LOGIN_LINKING_NO)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		if (hasValidCart(sc)) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.LINKED_LOGIN_SWITCH_ERR,
					SFTranslationTextConstants.LINKED_LOGIN_SWITCH_DEF_ERR));
			return response;
		}

		if(Util.isBlankOrNull(targetLoginID)) {
			response.setSuccess(false);
			response.getFieldMessages().put(ModelConstants.TARGET_LOGINID_FIELDNAME, AtWinXSConstant.BLANK_SPACE
					+ buildTranslationMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, appSessionBean, null));
			return response;
		}
		
		if(targetLoginID.length() > 16 ) {
			response.setSuccess(false);
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, 16 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(ModelConstants.TARGET_LOGINID_FIELDNAME, buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, appSessionBean, replaceMap));
			return response;
		}

		if(!validateLinkedLoginID(appSessionBean,targetLoginID.toUpperCase())){
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.LINKED_LOGIN_INVALID_LOGIN_ERR,
					SFTranslationTextConstants.LINKED_LOGIN_INVALID_LOGIN_ERR_DEFAULT));
			return response;
		}
		
		Map<String, String> parameters = new HashMap<>();
		CPRedirectResponse cpResponse = getSamlUrl(appSessionBean, parameters, targetLoginID);
		cpSessionReader.ttSessionTimeoutLogout(sc);
		
		response.setRedirectURL(cpResponse.getRedirectURL());
		response.setSuccess(true);
		return response;
		
	}
	
	//CAP-49731
	protected boolean validateLinkedLoginID(AppSessionBean appSessionBean, String searchFor) throws AtWinXSException {
		
		boolean linkedLoginIDValid = false;
		
		try {
			LinkedLogins linkedLogins = populateLinkedLogins(appSessionBean);

			if (null != linkedLogins.getLinkedLogins() && !linkedLogins.getLinkedLogins().isEmpty()) {

				for (LinkedLogin tempLinkedLogin : linkedLogins.getLinkedLogins()) {

					if(searchFor.equalsIgnoreCase(tempLinkedLogin.getUserID())) {
						linkedLoginIDValid =true;
						break;
					}
					
				}
				
			}
		} catch (AtWinXSException e) {
			logger.error(e.getMessage());
		}		
		
		return linkedLoginIDValid;
	}
	
	//CAP-49731
	@Override
	public CPRedirectResponse getSamlUrl(AppSessionBean asb, Map<String, String> parameters, String targetLoginID) throws AtWinXSException 
	{
		parameters.put(RouteConstants.SAML_LOGIN_ID, targetLoginID);//CAP-46263
		parameters.put(RouteConstants.SAML_ACCOUNT, asb.getSiteLoginID());
		parameters.put(RouteConstants.SAML_PROFILE_ID, targetLoginID);//CAP-46263
		parameters.put(RouteConstants.SAML_LOCALE, asb.getDefaultLocale().toString());//CAP-41344
		parameters.put(LoginConstants.QS_PARM_RETURN_TXT, translationService.processMessage(asb.getDefaultLocale(),
				asb.getCustomToken(), SFTranslationTextConstants.CLOSE));//CAP-41693
		
		return createSamlLogin(parameters);
	}

	//CAP-49731
	public CPRedirectResponse createSamlLogin(Map<String, String> parameters) throws AtWinXSException
	{
		CPRedirectResponse response = new CPRedirectResponse();
		
		XSProperties systemProps = PropertyUtil.getProperties("c1ux");
		
		// Set initial URL
		StringBuilder builder = new StringBuilder(systemProps.getProperty("c1ux.url"));
		
		// Add the Agent Configuration for the Open Token Agent
		// Use the properties saved in XST331 to populate this
		AgentConfiguration ag = new AgentConfiguration();
		ag.setUseVerboseErrorMessages(Boolean.getBoolean(systemProps.getProperty("c1ux.use-verbose-error-messages")));
		ag.setPassword(systemProps.getProperty("c1ux.password"));
		ag.setTokenName(systemProps.getProperty("c1ux.token-name"));
		ag.setUseCookie(Boolean.getBoolean(systemProps.getProperty("c1ux.use-cookie")));
		ag.setCookiePath(systemProps.getProperty("c1ux.cookie-path"));
		ag.setCipherSuite(Integer.parseInt(systemProps.getProperty("c1ux.cipher-suite")));
		ag.setUseSunJCE(Boolean.getBoolean(systemProps.getProperty("c1ux.use-sunjce")));
		ag.setTokenLifetime(Integer.parseInt(systemProps.getProperty("c1ux.token-lifetime")));
		ag.setRenewUntilLifetime(Integer.parseInt(systemProps.getProperty("c1ux.token-renewuntil")));
		ag.setObfuscatePassword(Boolean.getBoolean(systemProps.getProperty("c1ux.obfuscate-password")));
		ag.setNotBeforeTolerance(Integer.parseInt(systemProps.getProperty("c1ux.token-notbefore-tolerance")));						
		
		Agent spAgent = new Agent(ag);	
		
		// Populate user information map needed for login
		// These are the link parameters we set on the Admin side 
		Map<String, String> userInformation = populateUserInfoMap(parameters);
	    	
		try
		{						
			// For local use http://local.saml.rrd.c1ux 
			builder.append("?PartnerSpId=").append(Util.nullToEmpty(systemProps.getProperty("c1ux.partnerId")));
			
			String token = spAgent.writeToken(userInformation);			
			builder.append("&").append(systemProps.getProperty("c1ux.token-name")).append("=").append(token);
			
			response.setSuccess(true);
		} 
		catch (TokenException te)
		{
			logger.error("Failed in RedirectServiceImpl.createSamlLogin " + te.getMessage(), te);
		}		
		
		response.setRedirectURL(Util.nullToEmpty(builder.toString()));
		
		return response;
	}
	
	//CAP-49731
	/**
	 * This method populates the user information map.
	 * We can also process the new Replacement tags here
	 */
	protected Map<String, String> populateUserInfoMap(Map<String, String> userInformation)
	{		
		Map<String, String> params = new HashMap<>();
		
		//CAP-41347 Special case shared user with no profile.
		if(Util.isBlankOrNull(userInformation.get(RouteConstants.SAML_PROFILE_ID)))
		{
			params.put(Agent.TOKEN_SUBJECT, userInformation.get(RouteConstants.SAML_LOGIN_ID));
			params.put("userType", PingFederationConstants.SAML_USER_TYPE_USER);
			//CAP-41693 Added url and returnText.
			params.put("otherParameters", "&URL=" + RouteConstants.CP_REDIRECT_URL_CD + "&ReturnText=" + userInformation.get(LoginConstants.QS_PARM_RETURN_TXT) 
				+ "&Module=" + userInformation.get(RouteConstants.SAML_ENTRY_POINT) + "&locale=" + userInformation.get(RouteConstants.SAML_LOCALE) + "&approvalQueueID=" 
				+ Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID)) + "&AlertType=" + Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_VALUE_ALERT_TYPE)));
		}
		else
		{
			params.put(Agent.TOKEN_SUBJECT, userInformation.get(RouteConstants.SAML_PROFILE_ID));
			params.put("userType", PingFederationConstants.SAML_USER_TYPE_PROFILE);
			//CAP-41693 Added url and returnText.
			params.put("otherParameters", "&username=" + userInformation.get(RouteConstants.SAML_LOGIN_ID) + "&URL=" + RouteConstants.CP_REDIRECT_URL_CD + "&ReturnText=" 
				+ userInformation.get(LoginConstants.QS_PARM_RETURN_TXT) + "&Module=" + userInformation.get(RouteConstants.SAML_ENTRY_POINT) + "&locale=" 
				+ userInformation.get(RouteConstants.SAML_LOCALE) + "&approvalQueueID=" + Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID)) + "&AlertType=" 
				+ Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_VALUE_ALERT_TYPE)));
		}
		
		params.put("account", Util.nullToEmpty(userInformation.get(RouteConstants.SAML_ACCOUNT)));
				
		return params;
	}
	
	//CAP-49731
	private String buildTranslationMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {

		return Util.nullToEmpty(
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), errorKey, replaceMap));
	}

}