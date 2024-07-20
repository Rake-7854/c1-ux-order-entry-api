/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  10/18/2023  C Porter        CAP-44260                   Allow for custom Content Security Policies by site
 */

package com.rrd.c1ux.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.util.PropertyUtilService;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.rrd.custompoint.services.vo.KeyValuePair;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.tt.arch.TTException;
import com.wallace.tt.arch.web.TTPerformanceTracker;
import com.wallace.tt.vo.TTSession;

@ExtendWith(MockitoExtension.class)
class CSPHeaderResolverTests {

	private static final String CSP = "default 'unsafe-inline' *";
	private static final String CSP_WITH_NONCE = "default 'self'; script-src 'self' 'nonce-${NONCE}';";
	private static final String DEFAULT_HEADER_KEY = "SF_CSP_HEADER_DEFAULT";
	private static final String DEFAULT_CSP_KEY = "SF_CSP_DEFAULT";
	private static final String SITE_SPECIFIC_HEADER_KEY = "1234_SF_CSP_HEADER";
	private static final int SITE_ID = 1234;
	private static final String SITE_SPECIFIC_CSP_KEY = "1234_SF_CSP";
	private static final String TT_SESSION_ID = "12345";
	private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

	@Mock
	private XSProperties mockProperties;
	
	@Mock
	private PropertyUtilService mockPropertyUtilService;
	
	@Mock
	private CPSessionReader mockCPSessionReader;
	
	@Mock
	private SessionContainer mockSessionContainer;
	
	@Mock
	private ApplicationSession mockApplicationSession;
	
	@Mock
	private AppSessionBean mockAppSessionBean;

	@InjectMocks
	private CSPHeaderResolver testSubject;

	private MockHttpServletRequest mockRequest;

	@BeforeEach
	void setup() throws Exception {
		mockRequest = new MockHttpServletRequest();
	}

	@Test
	void that_headers_not_present() throws Exception {
		
		testSubject = Mockito.spy(testSubject);
		
		doReturn(Optional.empty()).when(testSubject)
			.lookupCSPHeaders(any());
		
		assertTrue(testSubject.resolve(mockRequest).isEmpty());
	}
	
	@Test
	void that_headers_with_no_nonce_are_used() throws Exception {
		
		testSubject = Mockito.spy(testSubject);
		
		doReturn(Optional.of(new KeyValuePair(CONTENT_SECURITY_POLICY, CSP))).when(testSubject)
			.lookupCSPHeaders(any());
		
		Optional<KeyValuePair> result =  testSubject.resolve(mockRequest);
		
		assertTrue(result.isPresent());
		assertEquals(CONTENT_SECURITY_POLICY, result.get().getKey());
		assertEquals(CSP, result.get().getValue());
	}

	@Test
	void that_nonce_template_is_removed() throws Exception {

		testSubject = Mockito.spy(testSubject);
		
		doReturn(Optional.of(new KeyValuePair(CONTENT_SECURITY_POLICY, CSP_WITH_NONCE))).when(testSubject)
			.lookupCSPHeaders(any());
		
		Optional<KeyValuePair> result =  testSubject.resolve(mockRequest);
		
		assertTrue(result.isPresent());
		assertEquals(CONTENT_SECURITY_POLICY, result.get().getKey());
		assertEquals("default 'self'; script-src 'self' ;", result.get().getValue());
	}

	@Test
	void that_nonce_template_is_added() throws Exception {

		testSubject = Mockito.spy(testSubject);
		
		mockRequest.setAttribute(CSPHeaderResolver.CSP_NONCE, "123456");
		
		doReturn(Optional.of(new KeyValuePair(CONTENT_SECURITY_POLICY, CSP_WITH_NONCE))).when(testSubject)
			.lookupCSPHeaders(any());
		
		Optional<KeyValuePair> result =  testSubject.resolve(mockRequest);
		
		assertTrue(result.isPresent());
		assertEquals(CONTENT_SECURITY_POLICY, result.get().getKey());
		assertEquals("default 'self'; script-src 'self' 'nonce-123456';", result.get().getValue());
	}

	@Test
	void that_ttsessionid_is_used() throws Exception {

		try (MockedConstruction<TTSession> mockSession = Mockito.mockConstruction(TTSession.class, (mock, context) -> {
			when(mock.select(anyInt(), any(TTPerformanceTracker.class))).thenReturn(true);
			when(mock.getId()).thenReturn(9999999);
			when(mock.getSite()).thenReturn(SITE_ID);
		})) {

			mockRequest.addHeader("ttsession", TT_SESSION_ID);
			assertEquals(SITE_ID, testSubject.findSiteId(mockRequest));
		}
		
		try (MockedConstruction<TTSession> mockSession = Mockito.mockConstruction(TTSession.class, (mock, context) -> {
			when(mock.getId()).thenReturn(9999999);
			when(mock.select(anyInt(), any(TTPerformanceTracker.class))).thenReturn(false);
		})) {

			mockRequest.addHeader("ttsession", TT_SESSION_ID);
			assertEquals(0, testSubject.findSiteId(mockRequest));
		}
	}
	
	@Test
	void that_app_session_bean_is_used() throws Exception {

		whenAppSessionBean();
		
		when(mockAppSessionBean.getSiteID()).thenReturn(SITE_ID);
		
		assertEquals(SITE_ID, testSubject.findSiteId(mockRequest));
	}

	@Test
	void that_default_headers_are_used() throws Exception {
		
		testSubject = Mockito.spy(testSubject);
		
		when(mockPropertyUtilService.getProperties(LoginConstants.PROP_SYSTEM)).thenReturn(mockProperties);
		doReturn(SITE_ID).when(testSubject).findSiteId(mockRequest);
		
		whenSiteSpecificCSP(null, null);
		whenDefaultCSP(CONTENT_SECURITY_POLICY, CSP);
		
		Optional<KeyValuePair> result = testSubject.lookupCSPHeaders(mockRequest);
		
		assertTrue(result.isPresent());
		assertEquals(CONTENT_SECURITY_POLICY, result.get().getKey());
		assertEquals(CSP, result.get().getValue());
	}
	
	@Test
	void that_headers_are_not_used() throws Exception {
		
		testSubject = Mockito.spy(testSubject);
		
		when(mockPropertyUtilService.getProperties(LoginConstants.PROP_SYSTEM)).thenReturn(mockProperties);
		doReturn(SITE_ID).when(testSubject).findSiteId(mockRequest);
		
		whenSiteSpecificCSP(null, null);
		whenDefaultCSP(null, null);
		
		assertTrue(testSubject.lookupCSPHeaders(mockRequest).isEmpty());
	}
	
	@Test
	void that_findsiteid_throws_exception() throws Exception {
		
		testSubject = Mockito.spy(testSubject);
		
		doThrow(new TTException("src", 0)).when(testSubject).findSiteId(mockRequest);
		
		Assertions.assertThrows(AtWinXSException.class, () -> {
			testSubject.lookupCSPHeaders(mockRequest);
		});
		
	}
	
	private void whenDefaultCSP(String cspHeader, String csp) throws AtWinXSException {
		when(mockProperties.getProperty(DEFAULT_CSP_KEY)).thenReturn(csp);
		when(mockProperties.getProperty(DEFAULT_HEADER_KEY)).thenReturn(cspHeader);
	}

	private void whenSiteSpecificCSP(String cspHeader, String csp) {
		when(mockProperties.getProperty(SITE_SPECIFIC_CSP_KEY)).thenReturn(cspHeader);
		when(mockProperties.getProperty(SITE_SPECIFIC_HEADER_KEY)).thenReturn(csp);
	}

	private void whenAppSessionBean() throws Exception {
		when(mockCPSessionReader.getSessionContainer(AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.APPSESSIONSERVICEID))
			.thenReturn(mockSessionContainer);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	}

}
