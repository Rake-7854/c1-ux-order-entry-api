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
 *  11/01/23	A Salcedo		   CAP-44686	   New CP redirect API w params.
 */

package com.rrd.c1ux.api.services.redirect.saml;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.pingidentity.opentoken.Agent;
import com.pingidentity.opentoken.AgentConfiguration;
import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PingFederationConstants;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.XSProperties;

@WithMockUser
class RedirectServiceImpTest extends BaseServiceTest  
{
	@InjectMocks
	private RedirectServiceImpl service;
	
	@Mock
    protected XSProperties mockXSProperty;
	
	@BeforeEach
	public void setup() throws AtWinXSException 
	{
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		/*
		 * when(mockAppSessionBean.getLoginID()).thenReturn("USER-RRD");
		 * when(mockAppSessionBean.getProfileID()).thenReturn("480902");
		 */
		when(mockAppSessionBean.getSiteLoginID()).thenReturn("DEVTEST");
		
		doReturn("Close").when(mockTranslationService).processMessage(any(), any(), anyString());
		
		service = Mockito.spy(service);
	}
	
	@Test
	void that_getURL_params_success() throws Exception 
	{
		CPRedirectResponse response;
		Map<String, String> parameters = new HashMap<>();
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		
		when(mockCompositeProfileBean.getLoginID()).thenReturn("USER-RRD");
		when(mockCompositeProfileBean.getProfileID()).thenReturn("480902");
		

		parameters.put(RouteConstants.SAML_LOGIN_ID, mockCompositeProfileBean.getLoginID());
		parameters.put(RouteConstants.SAML_ACCOUNT, mockAppSessionBean.getSiteLoginID());
		parameters.put(RouteConstants.SAML_ENTRY_POINT, "APR");
		parameters.put(RouteConstants.SAML_PROFILE_ID, mockCompositeProfileBean.getProfileID());
		parameters.put(RouteConstants.SAML_LOCALE, mockAppSessionBean.getDefaultLocale().toString());
		parameters.put(LoginConstants.QS_PARM_RETURN_TXT, mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE));
		
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class)) 
		{
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(mockXSProperty);
			
			when(mockXSProperty.getProperty("c1ux.url")).thenReturn("https://samltest.sso.rrd.com/idp/startSSO.ping");
			when(mockXSProperty.getProperty("c1ux.use-verbose-error-messages")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.password")).thenReturn("czNjdXIzQ1A=");
			when(mockXSProperty.getProperty("c1ux.token-name")).thenReturn("opentoken");
			when(mockXSProperty.getProperty("c1ux.use-cookie")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.cookie-path")).thenReturn("/");
			when(mockXSProperty.getProperty("c1ux.cipher-suite")).thenReturn("0");
			when(mockXSProperty.getProperty("c1ux.use-sunjce")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.token-lifetime")).thenReturn("300");
			when(mockXSProperty.getProperty("c1ux.token-renewuntil")).thenReturn("43200");
			when(mockXSProperty.getProperty("c1ux.obfuscate-password")).thenReturn("true");
			when(mockXSProperty.getProperty("c1ux.token-notbefore-tolerance")).thenReturn("30");
			when(mockXSProperty.getProperty("c1ux.partnerId")).thenReturn("http%3A%2F%2Fdev.saml.rrd.c1ux");

			response = service.getSamlUrl(mockAppSessionBean, parameters);
		}
		
		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getRedirectURL());
		Assertions.assertEquals(true, response.isSuccess());
	}
	
	@Test
	void that_getURL_entryPoint_success() throws Exception 
	{
		CPRedirectResponse response;
		Map<String, String> parameters = new HashMap<>();
		
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		
		when(mockCompositeProfileBean.getLoginID()).thenReturn("USER-RRD");
		when(mockCompositeProfileBean.getProfileID()).thenReturn("480902");
		
		parameters.put(RouteConstants.SAML_LOGIN_ID, mockCompositeProfileBean.getLoginID());
		parameters.put(RouteConstants.SAML_ACCOUNT, mockAppSessionBean.getSiteLoginID());
		parameters.put(RouteConstants.SAML_PROFILE_ID, mockCompositeProfileBean.getProfileID());
		parameters.put(RouteConstants.SAML_LOCALE, mockAppSessionBean.getDefaultLocale().toString());
		parameters.put(LoginConstants.QS_PARM_RETURN_TXT, mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE));
		
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class)) 
		{
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(mockXSProperty);
			
			when(mockXSProperty.getProperty("c1ux.url")).thenReturn("https://samltest.sso.rrd.com/idp/startSSO.ping");
			when(mockXSProperty.getProperty("c1ux.use-verbose-error-messages")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.password")).thenReturn("czNjdXIzQ1A=");
			when(mockXSProperty.getProperty("c1ux.token-name")).thenReturn("opentoken");
			when(mockXSProperty.getProperty("c1ux.use-cookie")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.cookie-path")).thenReturn("/");
			when(mockXSProperty.getProperty("c1ux.cipher-suite")).thenReturn("0");
			when(mockXSProperty.getProperty("c1ux.use-sunjce")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.token-lifetime")).thenReturn("300");
			when(mockXSProperty.getProperty("c1ux.token-renewuntil")).thenReturn("43200");
			when(mockXSProperty.getProperty("c1ux.obfuscate-password")).thenReturn("true");
			when(mockXSProperty.getProperty("c1ux.token-notbefore-tolerance")).thenReturn("30");
			when(mockXSProperty.getProperty("c1ux.partnerId")).thenReturn("http%3A%2F%2Fdev.saml.rrd.c1ux");

			response = service.getSamlUrl(mockAppSessionBean, AtWinXSConstant.EMPTY_STRING);
		}
		
		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getRedirectURL());
		Assertions.assertEquals(true, response.isSuccess());
	}
	
	@Test
	void that_populateUserInfoMap_success() throws Exception 
	{
		Map<String, String> parameters;
		Map<String, String> parametersMock = new HashMap<>();
		when(mockAppSessionBean.getLoginID()).thenReturn("USER-RRD");
		when(mockAppSessionBean.getProfileID()).thenReturn("480902");
		parametersMock.put(RouteConstants.SAML_LOGIN_ID, mockAppSessionBean.getLoginID());
		parametersMock.put(RouteConstants.SAML_ACCOUNT, mockAppSessionBean.getSiteLoginID());
		parametersMock.put(RouteConstants.SAML_PROFILE_ID, mockAppSessionBean.getProfileID());
		parametersMock.put(RouteConstants.SAML_LOCALE, mockAppSessionBean.getDefaultLocale().toString());
		parametersMock.put(LoginConstants.QS_PARM_RETURN_TXT, mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE));
		
		parameters = service.populateUserInfoMap(parametersMock);
		Assertions.assertNotNull(parameters);
	}
	
	@Test
	void that_populateUserInfoMap_sharednoprofile_success() throws Exception 
	{
		when(mockAppSessionBean.getProfileID()).thenReturn(null);
		when(mockAppSessionBean.getLoginID()).thenReturn("USER-RRD");
		Map<String, String> parameters;
		Map<String, String> parametersMock = new HashMap<>();
		parametersMock.put(RouteConstants.SAML_LOGIN_ID, mockAppSessionBean.getLoginID());
		parametersMock.put(RouteConstants.SAML_ACCOUNT, mockAppSessionBean.getSiteLoginID());
		parametersMock.put(RouteConstants.SAML_PROFILE_ID, mockAppSessionBean.getProfileID());
		parametersMock.put(RouteConstants.SAML_LOCALE, mockAppSessionBean.getDefaultLocale().toString());
		parametersMock.put(LoginConstants.QS_PARM_RETURN_TXT, mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE));
		
		parameters = service.populateUserInfoMap(parametersMock);
		Assertions.assertNotNull(parameters);
	}
	
	@Test
	void that_createSamlLogin_success() throws Exception 
	{
		AgentConfiguration ag = new AgentConfiguration();
		CPRedirectResponse response;
		when(mockAppSessionBean.getLoginID()).thenReturn("USER-RRD");
		when(mockAppSessionBean.getProfileID()).thenReturn("480902");
		
		Map<String, String> parametersMock = new HashMap<>();
		parametersMock.put(RouteConstants.SAML_LOGIN_ID, mockAppSessionBean.getLoginID());
		parametersMock.put(RouteConstants.SAML_ACCOUNT, mockAppSessionBean.getSiteLoginID());
		parametersMock.put(RouteConstants.SAML_PROFILE_ID, mockAppSessionBean.getProfileID());
		parametersMock.put(RouteConstants.SAML_LOCALE, mockAppSessionBean.getDefaultLocale().toString());
		parametersMock.put(LoginConstants.QS_PARM_RETURN_TXT, mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE));
		
		Map<String, String> userInformationMock = new HashMap<>();
		userInformationMock.put(Agent.TOKEN_SUBJECT, mockAppSessionBean.getProfileID());
		userInformationMock.put("userType", PingFederationConstants.SAML_USER_TYPE_PROFILE);
		userInformationMock.put("otherParameters", "&username=" + mockAppSessionBean.getLoginID() + "&URL=" + RouteConstants.CP_REDIRECT_URL_CD + "&ReturnText=" 
			+ mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE) + "&Module=" + AtWinXSConstant.EMPTY_STRING + "&locale=" 
			+ mockAppSessionBean.getDefaultLocale().toString() + "&approvalQueueID=" + AtWinXSConstant.EMPTY_STRING + "&alertType=" + AtWinXSConstant.EMPTY_STRING + "&fromStorefront=Y");
		userInformationMock.put("account", mockAppSessionBean.getSiteLoginID());
		
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class)) 
		{
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(mockXSProperty);
			
			when(mockXSProperty.getProperty("c1ux.url")).thenReturn("https://samltest.sso.rrd.com/idp/startSSO.ping");
			when(mockXSProperty.getProperty("c1ux.use-verbose-error-messages")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.password")).thenReturn("czNjdXIzQ1A=");
			when(mockXSProperty.getProperty("c1ux.token-name")).thenReturn("opentoken");
			when(mockXSProperty.getProperty("c1ux.use-cookie")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.cookie-path")).thenReturn("/");
			when(mockXSProperty.getProperty("c1ux.cipher-suite")).thenReturn("0");
			when(mockXSProperty.getProperty("c1ux.use-sunjce")).thenReturn("false");
			when(mockXSProperty.getProperty("c1ux.token-lifetime")).thenReturn("300");
			when(mockXSProperty.getProperty("c1ux.token-renewuntil")).thenReturn("43200");
			when(mockXSProperty.getProperty("c1ux.obfuscate-password")).thenReturn("true");
			when(mockXSProperty.getProperty("c1ux.token-notbefore-tolerance")).thenReturn("30");
			when(mockXSProperty.getProperty("c1ux.partnerId")).thenReturn("http%3A%2F%2Fdev.saml.rrd.c1ux");

			response = service.createSamlLogin(parametersMock);
		}
		
		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getRedirectURL());
		Assertions.assertEquals(true, response.isSuccess());
	}
	
}
