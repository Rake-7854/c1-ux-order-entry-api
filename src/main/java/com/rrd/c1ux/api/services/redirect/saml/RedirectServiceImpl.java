/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	5/19/23		A Salcedo		CAP-39210					Initial creation
 *	6/14/23		A Salcedo		CAP-41347					Updated for shared users w/o profile.
 *	6/14/23		A Salcedo		CAP-41344					Updated to pass locale.
 *	6/27/23		A Salcedo		CAP-41693					Updated CP Redirect to close window.
 *	10/23/23	A Salcedo		CAP-44686					New CP redirect API w params.
 *	12/05/23	A Salcedo		CAP-45725					Fix AlertType bug.
 *	01/05/24	Krishna Natarajan CAP-46263					Fix with code to get the Originator login ID and profile ID
 */
package com.rrd.c1ux.api.services.redirect.saml;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pingidentity.opentoken.Agent;
import com.pingidentity.opentoken.AgentConfiguration;
import com.pingidentity.opentoken.TokenException;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PingFederationConstants;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;

@Service
public class RedirectServiceImpl extends BaseOEService implements RedirectService
{
	protected RedirectServiceImpl(TranslationService translationService, ObjectMapFactoryService objService) 
	{
		super(translationService, objService);
	}

	private static final Logger logger = LoggerFactory.getLogger(RedirectServiceImpl.class);
	
	@Override
	public CPRedirectResponse getSamlUrl(AppSessionBean asb, String entryPoint) throws AtWinXSException 
	{
		Map<String, String> parameters = new HashMap<>();
		parameters.put(RouteConstants.SAML_LOGIN_ID, asb.getOriginatorProfile().getLoginID());//CAP-46263
		parameters.put(RouteConstants.SAML_ACCOUNT, asb.getSiteLoginID());
		parameters.put(RouteConstants.SAML_ENTRY_POINT, entryPoint);
		parameters.put(RouteConstants.SAML_PROFILE_ID, asb.getOriginatorProfile().getProfileID());//CAP-46263
		parameters.put(RouteConstants.SAML_LOCALE, asb.getDefaultLocale().toString());//CAP-41344
		parameters.put(LoginConstants.QS_PARM_RETURN_TXT, translationService.processMessage(asb.getDefaultLocale(),
				asb.getCustomToken(), SFTranslationTextConstants.CLOSE));//CAP-41693
		
		return createSamlLogin(parameters);
	}
	
	@Override
	public CPRedirectResponse getSamlUrl(AppSessionBean asb, Map<String, String> parameters) throws AtWinXSException 
	{
		parameters.put(RouteConstants.SAML_LOGIN_ID, asb.getOriginatorProfile().getLoginID());//CAP-46263
		parameters.put(RouteConstants.SAML_ACCOUNT, asb.getSiteLoginID());
		parameters.put(RouteConstants.SAML_PROFILE_ID, asb.getOriginatorProfile().getProfileID());//CAP-46263
		parameters.put(RouteConstants.SAML_LOCALE, asb.getDefaultLocale().toString());//CAP-41344
		parameters.put(LoginConstants.QS_PARM_RETURN_TXT, translationService.processMessage(asb.getDefaultLocale(),
				asb.getCustomToken(), SFTranslationTextConstants.CLOSE));//CAP-41693
		
		return createSamlLogin(parameters);
	}

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
				+ Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID)) + "&AlertType=" + Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_VALUE_ALERT_TYPE)) 
				+ "&fromStorefront=Y");
		}
		else
		{
			params.put(Agent.TOKEN_SUBJECT, userInformation.get(RouteConstants.SAML_PROFILE_ID));
			params.put("userType", PingFederationConstants.SAML_USER_TYPE_PROFILE);
			//CAP-41693 Added url and returnText.
			params.put("otherParameters", "&username=" + userInformation.get(RouteConstants.SAML_LOGIN_ID) + "&URL=" + RouteConstants.CP_REDIRECT_URL_CD + "&ReturnText=" 
				+ userInformation.get(LoginConstants.QS_PARM_RETURN_TXT) + "&Module=" + userInformation.get(RouteConstants.SAML_ENTRY_POINT) + "&locale=" 
				+ userInformation.get(RouteConstants.SAML_LOCALE) + "&approvalQueueID=" + Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID)) + "&AlertType=" 
				+ Util.nullToEmpty(userInformation.get(RouteConstants.SAML_PARAM_VALUE_ALERT_TYPE)) + "&fromStorefront=Y");
		}
		
		params.put("account", Util.nullToEmpty(userInformation.get(RouteConstants.SAML_ACCOUNT)));
				
		return params;
	}
}
