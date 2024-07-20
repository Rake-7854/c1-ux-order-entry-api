/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#				Description
 * 	--------	-----------			--------------		--------------------------------
 * 	12/07/23	S Ramachandran  	CAP-45485   		Added Junit for Fix code to only search/use originator profile when doing self admin
 */
package com.rrd.c1ux.api.controllers.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.users.UserContext;
import com.rrd.c1ux.api.models.users.UserFullProfileResponse;
import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class UserFullProfileControllerTest extends BaseMvcTest {

	private static final int TEST_SITE_ID = 1234;
	private static final int TEST_BU_ID = 1;
	private static final String TEST_PROFILE_ID = "testProfileId";
	private static final String TEST_ACCOUNT = "tstAccount";
	private static final String TEST_USERNAME = "testUsername";
	private final static String TEST_USERGROUP = "testUserGroup";

	private SiteBUGroupLoginProfileVO testSessionVO;
	private UserStateProfile userStateProfile;

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
		when(mockUserStateProfileMapper.fromSiteBUGroupLoginProfileVO(any(SiteBUGroupLoginProfileVO.class), 
			any(ApplicationSession.class))).thenReturn(userStateProfile);
		when(mockUserStateProfileService.getTranslation(userStateProfile, mockApplicationSession)).
			thenReturn(userStateProfile);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockAppSessionBean.getCurrencyLocale()).thenReturn(Locale.US);
		when(mockAppSessionBean.getDefaultTimeZone()).thenReturn("CST");

	}

	//CAP-45485
	@Test
	void that_retrieveUserFullProfile_returnsExpected200() throws Exception, AtWinXSException {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSession(any(UserContext.class))).thenReturn(testSessionVO);
		when(mockUserFullProfileProcessor.processUserFullProfile(any(), anyBoolean()))
			.thenReturn(getUserFullProfileResponseSuccess());

		// when retrieveUserFullProfile is called, expected 200 status
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.USERS_FULL_PROFILE)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

	}

	//CAP-45485
	@Test
	void that_retrieveUserFullProfile_returnsFailed422() throws Exception, AtWinXSException {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSession(any(UserContext.class))).thenReturn(testSessionVO);
		when(mockUserFullProfileProcessor.processUserFullProfile(any(), anyBoolean()))
			.thenReturn(getUserFullProfileResponseFailed());

		// when retrieveUserFullProfile is called, failed 422 with status
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.USERS_FULL_PROFILE)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}

	//CAP-45485
	@Test
	void that_retrieveFullUserProfileOriginator_rreturnsExpected200() throws Exception, AtWinXSException {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSession(any(UserContext.class))).thenReturn(testSessionVO);
		when(mockUserFullProfileProcessor.processUserFullProfile(any(), anyBoolean()))
			.thenReturn(getUserFullProfileResponseSuccess());

		// when retrieveFullUserProfileOriginator is called, expect 200 status
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.FULL_USER_PROFILE)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}

	//CAP-45485
	@Test
	void that_retrieveFullUserProfileOriginator_returnsFailed422() throws Exception, AtWinXSException {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionReader.getSession(any(UserContext.class))).thenReturn(testSessionVO);
		when(mockUserFullProfileProcessor.processUserFullProfile(any(), anyBoolean()))
			.thenReturn(getUserFullProfileResponseFailed());

		// when retrieveFullUserProfileOriginator is called, failed with 422 status
		mockMvc.perform(
				MockMvcRequestBuilders.get(RouteConstants.FULL_USER_PROFILE)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}

	private SiteBUGroupLoginProfileVO getTestSessionVO() {

		return new SiteBUGroupLoginProfileVO(
				BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_ACCOUNT), 
				BaseServiceTest.getTestBusinessUnitVO(TEST_SITE_ID, 1), 
				BaseServiceTest.getTestUserGroupVO(TEST_SITE_ID, TEST_BU_ID, TEST_USERGROUP),
				BaseServiceTest.getTestLoginVO(TEST_USERNAME, TEST_SITE_ID), 
				BaseServiceTest.getTestProfileVO(TEST_SITE_ID, 1, TEST_PROFILE_ID));
	}

	private UserFullProfileResponse getUserFullProfileResponseSuccess() {

		UserFullProfileResponse userFullProfileResponse = new UserFullProfileResponse();
		userFullProfileResponse.setSuccess(true);
		return userFullProfileResponse;
	}

	private UserFullProfileResponse getUserFullProfileResponseFailed() {

		UserFullProfileResponse userFullProfileResponse = new UserFullProfileResponse();
		userFullProfileResponse.setSuccess(false);
		return userFullProfileResponse;
	}
}
