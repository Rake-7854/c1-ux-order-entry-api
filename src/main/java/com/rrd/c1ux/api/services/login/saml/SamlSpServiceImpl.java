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
 *	CAP-36153	A Boomker		CAP-36153					Convert entry point code into routing URL
 *  2023.07.18  C Porter        CAP-39073                   Address Invicti security issue for X-XSS-Protection header. 
 */


package com.rrd.c1ux.api.services.login.saml;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.pingidentity.opentoken.Agent;
import com.pingidentity.opentoken.AgentConfiguration;
import com.pingidentity.opentoken.TokenException;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class SamlSpServiceImpl implements SamlSpService 
{
	private final Logger logger = LoggerFactory.getLogger(SamlSpServiceImpl.class);
	    	
	@Value("${login.use-verbose-error-messages}")
	private String pingVerboseErrorMessage;
	@Value("${login.password}")
	private String pingPassword;
	@Value("${login.token-name}")
	private String pingTokenName;
	@Value("${login.use-cookie}")
	private String pingUseCookie;
	@Value("${login.cookie-path}")
	private String pingCookiePath;
	@Value("${login.cipher-suite}")
	private String pingCipherSuite;
	@Value("${login.use-sunjce}")
	private String pingUseSunJce;
	@Value("${login.token-lifetime}")
	private String pingTokenLifetime;
	@Value("${login.token-renewuntil}")
	private String pingTokenRenewUntil;
	@Value("${login.obfuscate-password}")
	private String pingObfuscatePassword;
	@Value("${login.token-notbefore-tolerance}")
	private String pingTokenNotbeforeTolerance;
	@Value("${login.url}")
	private String pingLoginUrl;
	@Value("${login.default-host}")
	private String pingDefaultHost;
	
	@Value("${saml.use-verbose-error-messages}")
	private String samlVerboseErrorMessage;
	@Value("${saml.password}")
	private String samlPassword;
	@Value("${saml.token-name}")
	private String samlTokenName;
	@Value("${saml.use-cookie}")
	private String samlUseCookie;
	@Value("${saml.cookie-path}")
	private String samlCookiePath;
	@Value("${saml.cipher-suite}")
	private String samlCipherSuite;
	@Value("${saml.use-sunjce}")
	private String samlUseSunJce;
	@Value("${saml.token-lifetime}")
	private String samlTokenLifetime;
	@Value("${saml.token-renewuntil}")
	private String samlTokenRenewUntil;
	@Value("${saml.obfuscate-password}")
	private String samlObfuscatePassword;
	@Value("${saml.token-notbefore-tolerance}")
	private String samlTokenNotbeforeTolerance;
	@Value("${saml.http-only}")
	private String samlHttpOnly;
	
					
	@Override
	public String doSamlLogin(HttpServletRequest request, HttpServletResponse httpServletResponse, Map<String, String> params) throws AtWinXSException
	{				
		// First, process the SAML request coming in here
		// retrieve the user information
		Map<String, String> userInformationMap = parseSPToken(request);
				
		// Create the actual login here
		String redirectUrl = createSamlLogin(userInformationMap, httpServletResponse);
			
		return redirectUrl;
	}		
	
	/**
	 * Method that reads the user information from the token
	 * 
	 * @param request
	 * @return
	 * @throws AtWinXSException
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String> parseSPToken(HttpServletRequest request) throws AtWinXSException
	{
		logger.info("Token Name:" + samlTokenName);
		
		// Add the Agent Configuration for the Open Token Agent
		// Use the properties saved in XST331 to populate this
		AgentConfiguration ag = new AgentConfiguration();
		ag.setUseVerboseErrorMessages(Boolean.getBoolean(samlVerboseErrorMessage));
		ag.setHttpOnly(Boolean.getBoolean(samlHttpOnly));
		ag.setPassword(samlPassword);
		ag.setTokenName(samlTokenName);
		ag.setUseCookie(Boolean.getBoolean(samlUseCookie));
		ag.setCookiePath(samlCookiePath);
		ag.setCipherSuite(Integer.parseInt(samlCipherSuite));
		ag.setUseSunJCE(Boolean.getBoolean(samlUseSunJce));
		ag.setTokenLifetime(Integer.parseInt(samlTokenLifetime));
		ag.setRenewUntilLifetime(Integer.parseInt(samlTokenRenewUntil));
		ag.setObfuscatePassword(Boolean.getBoolean(samlObfuscatePassword));
		ag.setNotBeforeTolerance(Integer.parseInt(samlTokenNotbeforeTolerance));			
		
		Agent spAgent = new Agent(ag);
		
		Map<String, String> userInformation = null;

		try
		{			
			logger.info("Token Value:" + request.getParameter(samlTokenName));
			
			// read the user information from the token
			userInformation = spAgent.readToken(request);		
			
			if (userInformation != null)
			{
				logger.info("User Information: " + userInformation.toString());
			}
		}
		catch (TokenException te)
		{
			// Do error here
			logger.error("Token Read Error:  ", te);
		}

		return userInformation;
	}
	
	protected String createSamlLogin(Map<String, String> parameters, HttpServletResponse httpServletResponse)
	{
		// Add the Agent Configuration for the Open Token Agent
		// Use the properties saved in XST331 to populate this
		AgentConfiguration ag = new AgentConfiguration();
		ag.setUseVerboseErrorMessages(Boolean.getBoolean(pingVerboseErrorMessage));
		ag.setPassword(pingPassword);
		ag.setTokenName(pingTokenName);
		ag.setUseCookie(Boolean.getBoolean(pingUseCookie));
		ag.setCookiePath(pingCookiePath);
		ag.setCipherSuite(Integer.parseInt(pingCipherSuite));
		ag.setUseSunJCE(Boolean.getBoolean(pingUseSunJce));
		ag.setTokenLifetime(Integer.parseInt(pingTokenLifetime));
		ag.setRenewUntilLifetime(Integer.parseInt(pingTokenRenewUntil));
		ag.setObfuscatePassword(Boolean.getBoolean(pingObfuscatePassword));
		ag.setNotBeforeTolerance(Integer.parseInt(pingTokenNotbeforeTolerance));				
		
		Agent spAgent = new Agent(ag);
		
		// Populate user information map needed for login
		// These are the link parameters we set on the Admin side 
		Map<String, String> userInformation = populateUserInfoMap(parameters, httpServletResponse);
	    	
		// Set/Write the token here			
		StringBuilder builder = null;
		try
		{						
			if (Util.isBlankOrNull(userInformation.get("HOST_NAME"))) 
			{				
				builder = new StringBuilder(Util.replace(pingLoginUrl, "[HOST_NAME]", pingDefaultHost));
			}
			else
			{
				builder = new StringBuilder(Util.replace(pingLoginUrl, "[HOST_NAME]", userInformation.get("HOST_NAME")));
			}
			
			String token = spAgent.writeToken(userInformation);
			builder.append("&").append(pingTokenName).append("=").append(token);			
		} 
		catch (TokenException e)
		{
			logger.error("Token Creation Error: ", e);
		}
				
		return Util.nullToEmpty(builder.toString());
	}
	
	/**
	 * This method populates the user information map. The value comes from the link parameters on the Admin side
	 * We can also process the new Replacement tags here
	 */
	protected Map<String, String> populateUserInfoMap(Map<String, String> userInformation, HttpServletResponse response)
	{		
		Map<String, String> params = new HashMap<String, String>();
		
		// SAML_SUBJECT - should be login ID
		params.put(Agent.TOKEN_SUBJECT, userInformation.get("subject"));
		
		// PROFILE_ID
		String sessionId = userInformation.get("TTSessionId");		
		params.put("cpSessionId", sessionId);				
		
		// First Name
		params.put("firstName", userInformation.get("firstName"));
		
		// First Name
		params.put("lastName", userInformation.get("lastName"));
		
		// Email
		params.put("email", userInformation.get("email"));
		
		// Account
		params.put("account", userInformation.get("account")); //CAP-35362
		
		// HOST_NAME
		params.put("HOST_NAME", Util.nullToEmpty(userInformation.get("HOST_NAME")));
		
		// POUT_ENTRY_PT
		params.put("POUT_ENTRY_PT", Util.nullToEmpty(userInformation.get("POUT_ENTRY_PT")));		
		
		logger.info("Outgoing Parms: " + params.toString());		
				
		return params;
	}			
	
	// CAP-36153
	@Override
  public String getEntryPointRouting(String entryPoint, CPSessionReader mSessionReader) 
	{
		if (LoginConstants.C1UX_ENTRY_CODE_SELECTED_CATEGORY.equals(entryPoint))
		{
	    	try {
				SessionContainer sc = mSessionReader.getSessionContainer(AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.ORDERS_SERVICE_ID);
				OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean(); 
				if (oeOrderSessionBean.getSearchCriteriaBean() != null)
				{
					return RouteConstants.RETURN_TO_CATALOG_URL + RouteConstants.FOWARD_SLASH 
							+ oeOrderSessionBean.getSearchCriteriaBean().getSelectedCategoryId();
				}
			} catch (Exception e) {
				logger.error("Tried to generate entry point url for selected category but failed to retrieve OE session:" + e.getMessage(), e);
			}   
		}
		else if ((LoginConstants.C1UX_ENTRY_CODE_CART.equals(entryPoint)) 
				|| ("/orders/shoppingcart.cp".equals(entryPoint)))
		{
			return RouteConstants.CART_ENTRY_ROUTING_URL;
		}
		else if (LoginConstants.C1UX_ENTRY_CODE_CUST_DOC_ORDER_ENTRY.equals(entryPoint))
		{
			// TODO - this is not Phase 1a, so we don't have routing for it yet
		}
		else if (LoginConstants.C1UX_ENTRY_CODE_KIT_TEMPLATE_ORDER_ENTRY.equals(entryPoint))
		{
			// TODO - this is not Phase 1a, so we don't have routing for it yet
		}
		else if (LoginConstants.C1UX_ENTRY_CODE_SPECIAL_ITEM_ORDER_ENTRY.equals(entryPoint))
		{
			// TODO - this is not Phase 1a, so we don't have routing for it yet
		}
		return AtWinXSConstant.EMPTY_STRING;
	}
}
