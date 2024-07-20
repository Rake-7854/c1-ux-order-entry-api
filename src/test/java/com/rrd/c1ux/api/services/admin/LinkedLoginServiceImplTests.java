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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.LinkedLoginResponse;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserRequest;
import com.rrd.c1ux.api.models.admin.LoginLinkedUserResponse;
import com.rrd.custompoint.admin.entity.LinkedLogin;
import com.rrd.custompoint.admin.entity.LinkedLogins;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.ILinkedLogin;

class LinkedLoginServiceImplTests extends BaseOEServiceTest {

	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;

	private static final int TEST_SITE_ID = 1234;
	private static final String TEST_USER = "TEST_USER";
	private static final int TEST_LL_GROUP_ID = 123;
	private static final int TEST_CART_CT = 1;
	private static final int TEST_ORD_ID = 123456;
	private static final String TEST_STRING = "TEST_STRING";

	@Mock
	private EntityObjectMap mockEntityObjectMap;

	@Mock
	private ILinkedLogin mockILinkedLogin;

	@Mock
	private LinkedLogin mockLinkedLogin;

	@Mock
	private LinkedLogins mockLinkedLogins;

	@InjectMocks
	private LinkedLoginServiceImpl service;
    
	//CAP-49731
	@Mock
	private XSProperties mockXSProperty;
	//CAP-49731
	@Mock
	private CPRedirectResponse mockCpResponse;

	@BeforeEach
	void setup() throws Exception {

	}

	@Test
	void that_getLinkedLogins_return_success() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(AdminConstant.LOGIN_LINKING_ADMIN);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(AtWinXSConstant.INVALID_ID);
		doReturn(mockILinkedLogin).when(service).getLinkedLoginLocator(mockAppSessionBean);
		doReturn(TEST_LL_GROUP_ID).when(mockILinkedLogin).getlinkedLoginGroupID(TEST_SITE_ID, TEST_USER);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER);
		when(mockAppSessionBean.getSelfRegistrationURL()).thenReturn(TEST_STRING);
		doReturn(TEST_STRING).when(mockAppSessionBean).encodeURL(anyString());
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockLinkedLogin).when(mockEntityObjectMap).getEntity(LinkedLogin.class, null);
		doNothing().when(mockLinkedLogin).setSiteID(TEST_SITE_ID);
		doNothing().when(mockLinkedLogin).setUserID(TEST_USER);
		doReturn(mockLinkedLogins).when(mockEntityObjectMap).getEntity(LinkedLogins.class, null);
		doNothing().when(mockLinkedLogins).populateLinkedLogins(mockLinkedLogin);
		Collection<LinkedLogin> linkedLogins = new ArrayList<>();
		linkedLogins.add(mockLinkedLogin);
		when(mockLinkedLogins.getLinkedLogins()).thenReturn(linkedLogins);

		LinkedLoginResponse linkedLoginResponse = service.getLinkedLogins(mockSessionContainer);

		assertNotNull(linkedLoginResponse);
		assertTrue(linkedLoginResponse.isSuccess());
		assertTrue(linkedLoginResponse.getMessage().isEmpty());
	}

	@Test
	void that_getLinkedLogins_return_access_forbidden_exception() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(AdminConstant.LOGIN_LINKING_NO);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getLinkedLogins(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_getLinkedLogins_return_failure() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(AdminConstant.LOGIN_LINKING_ADMIN);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(TEST_ORD_ID);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(TEST_CART_CT);

		LinkedLoginResponse linkedLoginResponse = service.getLinkedLogins(mockSessionContainer);

		assertNotNull(linkedLoginResponse);
		assertFalse(linkedLoginResponse.isSuccess());
		assertFalse(linkedLoginResponse.getMessage().isEmpty());
	}
	
	//CAP-49731
	@Test
	void that_loginLinkedUser_return_422() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(AdminConstant.LOGIN_LINKING_NO);
		
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.loginLinkedUser(mockSessionContainer, getLoginLinkedUserRequest());
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		
		when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(AdminConstant.LOGIN_LINKING_ADMIN);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(TEST_ORD_ID);
		when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(TEST_CART_CT);
		
		LoginLinkedUserResponse linkedLoginUserResponse = service.loginLinkedUser(mockSessionContainer, getLoginLinkedUserRequest());
		assertNotNull(linkedLoginUserResponse);
		assertFalse(linkedLoginUserResponse.isSuccess());

		when(mockVolatileSessionBean.getOrderId()).thenReturn(AtWinXSConstant.INVALID_ID);
		linkedLoginUserResponse = service.loginLinkedUser(mockSessionContainer, getRequestWithEmptyUserID());
		assertNotNull(linkedLoginUserResponse);
		assertFalse(linkedLoginUserResponse.isSuccess());
		
		linkedLoginUserResponse = service.loginLinkedUser(mockSessionContainer, getRequestWithInvalidUserID());
		assertNotNull(linkedLoginUserResponse);
		assertFalse(linkedLoginUserResponse.isSuccess());
		
		doReturn(mockLinkedLogins).when(service).populateLinkedLogins(any());
		linkedLoginUserResponse = service.loginLinkedUser(mockSessionContainer, getLoginLinkedUserRequest());
		assertNotNull(linkedLoginUserResponse);
		assertFalse(linkedLoginUserResponse.isSuccess());
		
	}
	
	//CAP-49731
	@Test
	void that_loginLinkedUser_return_success() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(AdminConstant.LOGIN_LINKING_ADMIN);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(AtWinXSConstant.INVALID_ID);
		doReturn(mockLinkedLogins).when(service).populateLinkedLogins(any());
		doReturn(mockCpResponse).when(service).getSamlUrl(any(), any(), any());

		Collection<LinkedLogin> linkedLogins = new ArrayList<>();
		linkedLogins.add(mockLinkedLogin);
		when(mockLinkedLogins.getLinkedLogins()).thenReturn(linkedLogins);
		when(mockLinkedLogin.getUserID()).thenReturn(TEST_USER);

		LoginLinkedUserResponse linkedLoginUserResponse = service.loginLinkedUser(mockSessionContainer, getLoginLinkedUserRequest());

		assertNotNull(linkedLoginUserResponse);
		assertTrue(linkedLoginUserResponse.isSuccess());
		assertTrue(linkedLoginUserResponse.getMessage().isEmpty());

	}
	//CAP-49731
	public LoginLinkedUserRequest getLoginLinkedUserRequest() {
		
		LoginLinkedUserRequest request = new LoginLinkedUserRequest();
		request.setLoginLinkedUserID(TEST_USER);
		return request;
	}
	//CAP-49731
	public LoginLinkedUserRequest getRequestWithEmptyUserID() {
		
		LoginLinkedUserRequest request = new LoginLinkedUserRequest();
		request.setLoginLinkedUserID("");
		return request;
	}
	//CAP-49731
	public LoginLinkedUserRequest getRequestWithInvalidUserID() {
		
		LoginLinkedUserRequest request = new LoginLinkedUserRequest();
		request.setLoginLinkedUserID("UserID-1234567890");
		return request;
	}
	//CAP-49731
	@Test
	void that_getURL_params_success() throws Exception 
	{
		CPRedirectResponse response;
		Map<String, String> parameters = new HashMap<>();
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		
		when(mockCompositeProfileBean.getLoginID()).thenReturn("USER-RRD");
		when(mockCompositeProfileBean.getProfileID()).thenReturn("480902");
		
		parameters.put(RouteConstants.SAML_LOGIN_ID, mockCompositeProfileBean.getLoginID());
		parameters.put(RouteConstants.SAML_ACCOUNT, mockAppSessionBean.getSiteLoginID());
		parameters.put(RouteConstants.SAML_ENTRY_POINT, "APR");
		parameters.put(RouteConstants.SAML_PROFILE_ID, mockCompositeProfileBean.getProfileID());
		parameters.put(RouteConstants.SAML_LOCALE, mockAppSessionBean.getDefaultLocale().toString());
		parameters.put(LoginConstants.QS_PARM_RETURN_TXT, mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.CLOSE));
		
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
		
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class)) 
		{
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(mockXSProperty);
			
			response = service.getSamlUrl(mockAppSessionBean, parameters,"");
			Assertions.assertNotNull(response);
			Assertions.assertNotNull(response.getRedirectURL());
			Assertions.assertEquals(true, response.isSuccess());
			
			response = service.getSamlUrl(mockAppSessionBean, parameters,"test");
			Assertions.assertNotNull(response);
			Assertions.assertNotNull(response.getRedirectURL());
			Assertions.assertEquals(true, response.isSuccess());
		}
		

	}

}