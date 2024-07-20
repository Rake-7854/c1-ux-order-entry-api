/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/03/23				A Salcedo				CAP-44686					Initial Version
 */
package com.rrd.c1ux.api.controllers.redirect;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.CPUrlBuilder;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;
import com.wallace.atwinxs.framework.util.Util;

@WithMockUser
class CPRedirectControllerTest extends BaseMvcTest {

	String TEST_ENCRYPTED_SESSIONID;
	private CPRedirectResponse cpRedirectResponseSuccess;
	
	@MockBean
	private EntityObjectMap mockEntity;
	
	@Mock
	protected CompositeProfileBean mockCompositeProfileBean;

	@BeforeEach
	void setUp() throws Exception 
	{
		setupBaseMockSessions();

		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		when(mockAppSessionBean.isC1uxSession()).thenReturn(true);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		
		cpRedirectResponseSuccess = getCPRedirectResponseSuccessTest();
		TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
	}

	@Test
	void that_redirectToCP_returnsExpected() throws Exception 
	{
		try (MockedStatic<ObjectMapFactory> mockedStatic = Mockito.mockStatic(ObjectMapFactory.class);
				MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)) 
		{
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			
			when(mockCompositeProfileBean.getLoginID()).thenReturn("USER-RRD");
			
			mockedStatic.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntity);
			
			when(mockRedirectService.getSamlUrl(mockAppSessionBean, AtWinXSConstant.EMPTY_STRING)).thenReturn(cpRedirectResponseSuccess);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
			
			mockedAdminUtil.when(() -> AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
			.thenReturn(mockUserSettings);
			
			when(mockOrderEntrySession.getOESessionBean()).thenReturn(mockOEOrderSessionBean);
			
			mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.CP_REDIRECT).accept(MediaType.APPLICATION_JSON)
					.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
					.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		}
	}
	
	@Test
	void that_redirectToCP_EP_returnsExpected() throws Exception 
	{
		when(mockRedirectService.getSamlUrl(mockAppSessionBean, "RPT")).thenReturn(cpRedirectResponseSuccess);

		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.CP_REDIRECT + "/" + Util.encryptString(RouteConstants.CP_REDIRECT_RPT))
				.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}
	
	@Test
	void that_redirectionToCP_returnsExpected() throws Exception 
	{
		Map<String, String> parameters = new HashMap<>();
		parameters.put(RouteConstants.SAML_ENTRY_POINT, RouteConstants.SAML_PARAM_VALUE_APPROVAL_ALERT);
		parameters.put(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID, String.valueOf(1));
		
		CPUrlBuilder builder = new CPUrlBuilder();
		builder.addParameter(RouteConstants.SAML_ENTRY_POINT, RouteConstants.SAML_PARAM_VALUE_APPROVAL_ALERT);
		builder.addParameter(RouteConstants.SAML_PARAM_APPROVAL_QUEUE_ID, String.valueOf(1));
		
		when(mockRedirectService.getSamlUrl(mockAppSessionBean, parameters)).thenReturn(cpRedirectResponseSuccess);

		mockMvc.perform(MockMvcRequestBuilders.get(RouteConstants.CP_REDIRECT_PARAMS).accept(MediaType.APPLICATION_JSON)
				.param("a", builder.getUrl(true).substring(2))
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
	}

	private CPRedirectResponse getCPRedirectResponseSuccessTest() 
	{
		cpRedirectResponseSuccess = new CPRedirectResponse();
		cpRedirectResponseSuccess.setRedirectURL("https://samltest.sso.rrd.com/idp/startSSO.ping");
		cpRedirectResponseSuccess.setSuccess(true);
		cpRedirectResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return cpRedirectResponseSuccess;
	}
}
