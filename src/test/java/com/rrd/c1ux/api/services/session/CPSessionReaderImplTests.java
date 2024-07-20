/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	CAP-35537	T Harmon									Updated for SAML
 *  10/25/22	A Boomker		CAP-36153					Add entry point
 *  03/07/23    C Porter        CAP-38715                   refactor
 *  03/17/2023  C Porter        CAP-39295                   Handle "Could not load session" Exception
 *  04/05/2023  C Porter        CAP-39674                   Intermittent Session Timeout fix
 *  04/18/2023  C Porter        CAP-39954                   Occasional session timeout fix  
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */

package com.rrd.c1ux.api.services.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.exceptions.AtWinXSSessionException;
import com.rrd.c1ux.api.models.users.UserContext;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.ProfileVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.ModuleSessionDAO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.tt.arch.web.TTPerformanceTracker;
import com.wallace.tt.vo.TTSession;

class CPSessionReaderImplTests extends BaseServiceTest {

    private final static int TEST_SITE_ID = 1234;
    private final static int TEST_BU_ID = 4321;
    private final static String TEST_ACCOUNT = "tstAccount";
    private final static String TEST_USERNAME = "testUsername";
    private final static String TEST_USERGROUP = "testUserGroup";
    private final static String TEST_PROFILE_ID = "testProfileId";

    @Mock
    private Saml2AuthenticatedPrincipal mockSaml2AuthenticatedPrincipal;
    
    @Mock
    private Saml2Authentication mockSaml2Authentication;
    
    @InjectMocks
    private CPSessionReaderImpl serviceToTest;
  
    @Test
    void that_getSamlAuthenticatedPrincipal_returns_principal() {

      SecurityContext securityContext = new SecurityContextImpl();
      securityContext.setAuthentication(mockSaml2Authentication);

      try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {

        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(mockSaml2Authentication.isAuthenticated()).thenReturn(true);
        when(mockSaml2Authentication.getPrincipal()).thenReturn(mockSaml2AuthenticatedPrincipal);

        assertTrue(serviceToTest.getSamlAuthenticatedPrincipal().isPresent());
      }
    }
    
    @Test
    void that_getSamlAuthenticatedPrincipal_returns_empty() {

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("foo", "bar", List.of());
      SecurityContext securityContext = new SecurityContextImpl();
      securityContext.setAuthentication(authentication);

      try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {

        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        assertTrue(serviceToTest.getSamlAuthenticatedPrincipal().isEmpty());
      }

    }
    
    @Test
    void that_getSession_throws_whenLoginIsSharedAndProfileNotUnattached() throws AtWinXSException {
        
        // given login reader returns shared login VO
        LoginVO sharedLoginVO = BaseServiceTest.getTestSharedLoginVO(TEST_USERNAME, TEST_SITE_ID);
        when(mockLoginReader.getLoginVO(TEST_SITE_ID, TEST_USERNAME)).thenReturn(sharedLoginVO);
        
        // given profile component indicates an attached profile VO
        UserContext testUserContext = new UserContext(TEST_USERNAME, AtWinXSConstant.EMPTY_STRING, TEST_PROFILE_ID, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, TEST_ACCOUNT, AtWinXSConstant.EMPTY_STRING); // CAP-36153
        when(mockProfileReader.getProfileVO(sharedLoginVO, TEST_PROFILE_ID))
            .thenReturn(BaseServiceTest.getTestProfileVO(TEST_SITE_ID, 1, TEST_PROFILE_ID));
        when(mockProfileComponent.isProfileUnattached(any(ProfileVOKey.class))).thenReturn(false);

        // given site reader returns site VO
        when(mockSiteReader.getSiteForAccount(TEST_ACCOUNT))
            .thenReturn(BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_ACCOUNT));
        
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        when(mockComponentLocator.locateProfileComponent(any()))
            .thenReturn(mockProfileComponent);
        
        // when getSession is called for user context
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getSession(testUserContext);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.CPSessionReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals("attachedProfileForSharedUse", actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains the username
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue(TEST_USERNAME));
    }
    
    @Test
    void that_getSession_throws_whenLoginProfileIdDoesNotMatchSpecifiedProfileId() throws AtWinXSException {
        
        // given login reader returns non-shared login VO
        LoginVO testLoginVO = BaseServiceTest.getTestLoginVO(TEST_USERNAME, TEST_SITE_ID);
        when(mockLoginReader.getLoginVO(TEST_SITE_ID, TEST_USERNAME)).thenReturn(testLoginVO);
        
        // given user context with profile ID different than that attached to user
        String badProfileId = "differentProfileId";
        UserContext testUserContext = new UserContext(TEST_USERNAME, AtWinXSConstant.EMPTY_STRING, badProfileId, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, TEST_ACCOUNT, AtWinXSConstant.EMPTY_STRING); // CAP-36153
        when(mockProfileReader.getProfileVO(testLoginVO, badProfileId))
            .thenReturn(BaseServiceTest.getTestProfileVO(TEST_SITE_ID, 1, TEST_PROFILE_ID));

        // given site reader returns site VO
        when(mockSiteReader.getSiteForAccount(TEST_ACCOUNT))
            .thenReturn(BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_ACCOUNT));
        
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        
        // when getSession is called for user context
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getSession(testUserContext);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.CPSessionReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals("invalidProfileForUser", actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains the username
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue(TEST_USERNAME));

        // and the error map contains the bad profile ID
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue(badProfileId));
    }

    @Test
    void that_getSession_returnsExpectedForSharedLogin() throws AtWinXSException {

        // given login reader returns shared login VO
        LoginVO testSharedLoginVO = BaseServiceTest.getTestSharedLoginVO(TEST_USERNAME, TEST_SITE_ID);
        when(mockLoginReader.getLoginVO(TEST_SITE_ID, TEST_USERNAME)).thenReturn(testSharedLoginVO);

        when(mockProfileComponent.isProfileUnattached(any(ProfileVOKey.class))).thenReturn(true);
        
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        when(mockComponentLocator.locateProfileComponent(any()))
            .thenReturn(mockProfileComponent);
        when(mockComponentLocator.locateBusinessUnitComponent(any()))
            .thenReturn(mockBUComponent);
        when(mockComponentLocator.locateUserGroupComponent(any()))
            .thenReturn(mockGroupComponent);

        that_getSession_returnsExpectedForLogin(testSharedLoginVO);
    }

    @Test
    void that_getSession_returnsExpectedForNonSharedLogin() throws AtWinXSException {

        // given login reader returns non-shared login VO
        LoginVO testNonSharedLoginVO = BaseServiceTest.getTestLoginVO(TEST_USERNAME, TEST_SITE_ID);
        when(mockLoginReader.getLoginVO(TEST_SITE_ID, TEST_USERNAME)).thenReturn(testNonSharedLoginVO);

        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        when(mockComponentLocator.locateBusinessUnitComponent(any()))
            .thenReturn(mockBUComponent);
        when(mockComponentLocator.locateUserGroupComponent(any()))
            .thenReturn(mockGroupComponent);
        
        that_getSession_returnsExpectedForLogin(testNonSharedLoginVO);
    }

    private void that_getSession_returnsExpectedForLogin(LoginVO testLoginVO) throws AtWinXSException {
        
        // given user context and profile VO
        UserContext testUserContext = new UserContext(TEST_USERNAME, AtWinXSConstant.EMPTY_STRING, TEST_PROFILE_ID, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, TEST_ACCOUNT, AtWinXSConstant.EMPTY_STRING); // CAP-36153
        when(mockProfileReader.getProfileVO(testLoginVO, TEST_PROFILE_ID))
            .thenReturn(BaseServiceTest.getTestProfileVO(TEST_SITE_ID, 1, TEST_PROFILE_ID));

        // given site reader returns site VO
        when(mockSiteReader.getSiteForAccount(TEST_ACCOUNT))
            .thenReturn(BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_ACCOUNT));

        // given BU component returns BU VO
        when(mockBUComponent.getBusinessUnitByLogin(any(LoginVOKey.class)))
            .thenReturn(BaseServiceTest.getTestBusinessUnitVO(TEST_SITE_ID, TEST_BU_ID));

        // given Group component returns group VO
        when(mockGroupComponent.getUserGroup(any(UserGroupVOKey.class)))
            .thenReturn(BaseServiceTest.getTestUserGroupVO(TEST_SITE_ID, TEST_BU_ID, TEST_USERGROUP));
        
        // when getSession is called for user context
        SiteBUGroupLoginProfileVO actual = serviceToTest.getSession(testUserContext);

        // then returned VO is as expected
        assertNotNull(actual);
        assertEquals(TEST_SITE_ID, actual.getSiteID());
        assertEquals(TEST_BU_ID, actual.getBuID());
        assertEquals(TEST_USERGROUP, actual.getUserGroupName());
        assertEquals(TEST_PROFILE_ID, actual.getProfileID());
        assertEquals(TEST_USERNAME, actual.getLoginID());
    }
    
    @Test
    void that_getSessionContainer_returnsSessionContainer() throws Exception {

      SiteVO stubSiteVO = BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_BU_ID, TEST_ACCOUNT, TEST_USERNAME, TEST_USERGROUP, TEST_PROFILE_ID);
      TTSession stubTTSession = new TTSession();

      try (MockedConstruction<TTSession> mockedTTSession = Mockito.mockConstruction(TTSession.class, (mock, context) -> {

        when(mock.select(anyInt(), any(TTPerformanceTracker.class))).thenReturn(true);
        when(mock.getStatus()).thenReturn(TTSession.ACTIVE);

      })) {

        when(mockSessionHandlerService.getFullSessionInfo(0, 1)).thenReturn(mockSessionContainer);
        when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
        when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
        when(mockAppSessionBean.getSiteLoginID()).thenReturn(TEST_ACCOUNT);
        when(mockSiteReader.getSiteForAccount(TEST_ACCOUNT)).thenReturn(stubSiteVO);
        when(mockAppSessionBean.getTtSession()).thenReturn(stubTTSession);

        SessionContainer result = serviceToTest.getSessionContainer("sessId", 1);

        assertNotNull(result);
      }

    }
    
    @Test
    void that_getSessionContainer_uses_SamlAttribute() throws Exception {

      SiteVO stubSiteVO = BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_BU_ID, TEST_ACCOUNT, TEST_USERNAME, TEST_USERGROUP, TEST_PROFILE_ID);
      TTSession stubTTSession = new TTSession();

      String propCpSessionId = "CP_SESSION_ID";

      SecurityContext securityContext = new SecurityContextImpl();
      securityContext.setAuthentication(mockSaml2Authentication);

      try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class);
          MockedConstruction<TTSession> mockedTTSession = Mockito.mockConstruction(TTSession.class, (mock, context) -> {

            when(mock.select(anyInt(), any(TTPerformanceTracker.class))).thenReturn(true);
            when(mock.getStatus()).thenReturn(TTSession.ACTIVE);

          })) {

        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(mockSaml2Authentication.isAuthenticated()).thenReturn(true);
        when(mockSaml2Authentication.getPrincipal()).thenReturn(mockSaml2AuthenticatedPrincipal);
        when(mockClaimsProperties.getCpSessionId()).thenReturn(propCpSessionId);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propCpSessionId)).thenReturn("test123");

        when(mockSessionHandlerService.getFullSessionInfo(0, 1)).thenReturn(mockSessionContainer);
        when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
        when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
        when(mockAppSessionBean.getSiteLoginID()).thenReturn(TEST_ACCOUNT);
        when(mockSiteReader.getSiteForAccount(TEST_ACCOUNT)).thenReturn(stubSiteVO);
        when(mockAppSessionBean.getTtSession()).thenReturn(stubTTSession);

        assertNotNull(serviceToTest.getSessionContainer("", 1));
      }

    }
    
    @Test
    void that_getSessionContainer_throws_when_session_not_found() throws Exception {

      try (MockedConstruction<TTSession> mockedTTSession = Mockito.mockConstruction(TTSession.class, (mock, context) -> {

        when(mock.select(anyInt(), any(TTPerformanceTracker.class))).thenReturn(true);

      })) {

        when(mockSessionHandlerService.getFullSessionInfo(0, 1))
            .thenThrow(new AtWinXSException("Session information not available", ModuleSessionDAO.class.getName()));

        assertThrowsExactly(AtWinXSSessionException.class, () -> {
          serviceToTest.getSessionContainer("sessId", 1);
        });
      }

    }
    
    @Test
    void that_getUserContext_returnsExpectedUserContext() {
      
      String propCpSessionId = "cpSessionId";
      String propAccountClaimUri = "http://wso2.org/claims/organization";
      String propFirstName = "firstName";
      String propLastName = "lastName";
      String propEmail = "email";
      String propAccount = "account";
      String propEntryPoint = "POUT_ENTRY_PT";
      
      String expectedUserName = "theName";
      String expectedCpSessionId = "theCpSessionId";
      String expectedAccountClaimUri = "theAccountClaimUri";
      String expectedFirstName = "theFirstName";
      String expectedLastName = "theLastName";
      String expectedEmail = "theEmail";
      String expectedAccount = "theAccount";
      String expectedEntryPoint = "theEntryPoint";
      
      SecurityContext ctx = new SecurityContextImpl();
      Saml2Authentication saml2Auth = new Saml2Authentication(mockSaml2AuthenticatedPrincipal, "123", Collections.emptyList());
      ctx.setAuthentication(saml2Auth);
      
      try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
        
        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(mockSaml2AuthenticatedPrincipal.getName()).thenReturn(expectedUserName);
        
        when(mockClaimsProperties.getCpSessionId()).thenReturn(propCpSessionId);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propCpSessionId)).thenReturn(expectedCpSessionId);
        when(mockClaimsProperties.getAccountClaimUri()).thenReturn(propAccountClaimUri);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propAccountClaimUri)).thenReturn(expectedAccountClaimUri);
        when(mockClaimsProperties.getFirstName()).thenReturn(propFirstName);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propFirstName)).thenReturn(expectedFirstName);
        when(mockClaimsProperties.getLastName()).thenReturn(propLastName);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propLastName)).thenReturn(expectedLastName);
        when(mockClaimsProperties.getEmail()).thenReturn(propEmail);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propEmail)).thenReturn(expectedEmail);
        when(mockClaimsProperties.getAccount()).thenReturn(propAccount);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propAccount)).thenReturn(expectedAccount);
        when(mockClaimsProperties.getEntryPoint()).thenReturn(propEntryPoint);
        when(mockSaml2AuthenticatedPrincipal.getFirstAttribute(propEntryPoint)).thenReturn(expectedEntryPoint);
        
        UserContext userContext = serviceToTest.getUserContext();
        
        assertEquals(expectedUserName, userContext.getUserName());
        assertEquals(expectedAccountClaimUri, userContext.getOrg());
        assertEquals(expectedCpSessionId, userContext.getCpSessionId());
        assertEquals(expectedFirstName, userContext.getFirstName());
        assertEquals(expectedLastName, userContext.getLastName());
        assertEquals(expectedEmail, userContext.getEmail());
        assertEquals(expectedAccount, userContext.getAccount());
        assertEquals(expectedEntryPoint, userContext.getEntryPoint());
        
      }
      
    }
}
