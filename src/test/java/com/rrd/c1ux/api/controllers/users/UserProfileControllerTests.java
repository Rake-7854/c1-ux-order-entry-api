/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/11/22				A Boomker			    CAP-35766 					Modification for Punchout flags
 *  03/07/23                C Porter                CAP-38715                   Refactor
 *  05/31/23                C Porter                CAP-40530                   JUnit cleanup  
 * 	08/30/23				Krishna Natarajan		CAP-43371					modification to get over error thrown in existing test method
 * 	01/01/24				S Ramachandran			CAP-46119					Added tests for user profile to get originator profile 
 * 	05/30/24				C Codina				CAP-49744					Added test for suggestedItemDisplay
 */
package com.rrd.c1ux.api.controllers.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.users.UserContext;
import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CompositeOrdPropBean;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;

@WithMockUser
class UserProfileControllerTests extends BaseMvcTest {

    private static final int TEST_SITE_ID = 1234;
    private static final int TEST_BU_ID = 1;
    private static final int TEST_VALID_PROFILE_NUMBER = 1;
    private static final String TEST_PROFILE_ID = "testProfileId";
    private static final String TEST_ACCOUNT = "tstAccount";
    private static final String TEST_USERNAME = "testUsername";
    private final static String TEST_USERGROUP = "testUserGroup";
    private String EMPTY_TEST_ENCRYPTED_SESSIONID = "";
    private String TEST_ENCRYPTED_SESSIONID = "testEncryptedSessionId";

    private SiteBUGroupLoginProfileVO testSessionVO;
    private UserStateProfile userStateProfile;
    
	@MockBean
	protected CompositeProfileBean mockCompositeProfileBean;
	
	@MockBean
	protected CompositeOrdPropBean mockCompositeOrdPropBean;
	
	@MockBean
	protected Resource mockResource;
	
	@MockBean
	protected ResourceLoader mockResourceLoader;
 
	//CAP-46119
    @BeforeEach
    void setUp() throws Exception {
    	
      testSessionVO = getTestSessionVO();
      userStateProfile = new UserStateProfile();
      userStateProfile.setProfileID(TEST_PROFILE_ID);
      userStateProfile.setSiteName(TEST_ACCOUNT);
     
      when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
      when(mockSessionReader.getUserContext()).thenReturn(new UserContext());
      when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
      when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
      when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
      when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
      when(mockOrderEntrySession.getOESessionBean()).thenReturn(mockOEOrderSessionBean);
	  when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
      when(mockUserStateProfileMapper.fromSiteBUGroupLoginProfileVO(any(), any(ApplicationSession.class)))
        .thenReturn(userStateProfile);
      when(mockUserStateProfileService.getTranslation(userStateProfile, mockApplicationSession))
      	.thenReturn(userStateProfile);//CAP-43371
      when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
      when(mockAppSessionBean.getCurrencyLocale()).thenReturn(Locale.US);
      when(mockAppSessionBean.getDefaultTimeZone()).thenReturn("CST");
      
      //CAP-49744
      when(mockUserSettings.getSugItemsDfltDsply()).thenReturn("B");
      
      // given that getSession returns a valid session
      when(mockSessionReader.getSession(any(UserContext.class))).thenReturn(testSessionVO);
      when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
      
      when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
      when(mockAppSessionBean.getOriginatorOrdProp()).thenReturn(mockCompositeOrdPropBean);
    }
    
    //CAP-46119
    @Test
    void that_getProfileWithSessionId_returnsExpected() throws Exception, AtWinXSException {

    	// given that getProfile With SessionId Non Empty returns Valid UserStateProfile 
    	when(mockCompositeProfileBean.getProfileNumber()).thenReturn(TEST_VALID_PROFILE_NUMBER);
    	when(mockCompositeProfileBean.isSharedID()).thenReturn(true);
    	when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
    	when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(EMPTY_TEST_ENCRYPTED_SESSIONID);
    	
        try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)) {
        	
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
        
			// when getProfile is called, expect 200 status and UserStateProfile in JSON
	        mockMvc.perform(
	            MockMvcRequestBuilders.get(RouteConstants.USERS_PROFILE)
	            .accept(MediaType.APPLICATION_JSON)
	            .header("ttsession", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8"))
	            .andExpect(MockMvcResultMatchers.status().isOk());
			
		}
    }
    
    //CAP-46119
    @Test
    void that_getProfileWithSessionIdEMPTY_returnsExpected() throws Exception, AtWinXSException {

    	// given that getProfile With SessionId Empty returns Valid UserStateProfile
    	when(mockCompositeProfileBean.getProfileNumber()).thenReturn(AtWinXSConstant.INVALID_PROFILE_NUMBER);
    	when(mockCompositeProfileBean.isSharedID()).thenReturn(false);
    	when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
    	when(mockAppSessionBean.getAllowLoginLinking()).thenReturn(EMPTY_TEST_ENCRYPTED_SESSIONID);
    	
    	
        try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)) {
        	
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
        
	        // when getProfile is called, expect 200 status and UserStateProfile in JSON
	        mockMvc.perform(
	            MockMvcRequestBuilders.get(RouteConstants.USERS_PROFILE)
	            .accept(MediaType.APPLICATION_JSON)
	            .header("ttsession", EMPTY_TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8"))
	            .andExpect(MockMvcResultMatchers.status().isOk());
			
		}
    }
 
    //CAP-46119    
    private static SiteBUGroupLoginProfileVO getTestSessionVO() {

        return new SiteBUGroupLoginProfileVO(
            BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_ACCOUNT), 
            BaseServiceTest.getTestBusinessUnitVO(TEST_SITE_ID, 1), 
            BaseServiceTest.getTestUserGroupVO(TEST_SITE_ID, TEST_BU_ID, TEST_USERGROUP),
            BaseServiceTest.getTestLoginVO(TEST_USERNAME, TEST_SITE_ID), 
            BaseServiceTest.getTestProfileVO(TEST_SITE_ID, 1, TEST_PROFILE_ID));
    }
    
}
