/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 *  11/10/23	A Boomker			CAP-44487		Added handling for load user profile
 *  07/01/24	R Ruth				CAP-46503		Added handling for alt profiles
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUserProfileSearchRequest;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
class CustomDocsProfileControllerTests  extends BaseMvcTest {

	@Mock
	protected OrderEntrySession mockOESession;
	@Mock
	protected OEOrderSessionBean mockOEOrderSession;
	  @Mock
	  private UserInterfaceImpl mockUI;
	  @Mock
	  private CustomDocumentItemImpl mockItem;
		@Mock
		protected OEResolvedUserSettingsSessionBean mockUserSettings;


		private static final String GENERIC_ERROR_MSG = "Failure message generic";
	private String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";

	@BeforeEach
	public void setUp() throws Exception {
	    setupBaseMockSessions();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	    when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(true);
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
	}

		@Test
		void that_searchUserProfiles_returnsExpected() throws Exception, AtWinXSException {
	        String requestString = getJsonRequest(new C1UXCustDocUserProfileSearchRequest());

	        when(mockCustDocsProfileService.searchUserProfiles(any(SessionContainer.class), any(C1UXCustDocUserProfileSearchRequest.class)))
					.thenReturn(getEmptyProfileSearchResponse());
		    checkStandard200Response(RouteConstants.CUST_DOCS_USER_PROFILE_SEARCH, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
		}

		private C1UXCustDocProfileSearchResponse getEmptyProfileSearchResponse() {
			C1UXCustDocProfileSearchResponse response = new C1UXCustDocProfileSearchResponse();
			response.setSuccess(true);
			response.setProfileOptions(new ArrayList<>());
			return response;
		}

		@Test
		void that_loadUserProfile_returnsExpected() throws Exception, AtWinXSException {
		    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
		    C1UXCustDocLoadProfileRequest req = new C1UXCustDocLoadProfileRequest();
			String requestString = getJsonRequest(req);

			when(mockCustDocsProfileService.loadUserProfile(any(SessionContainer.class), any()))
				.thenReturn(getLoadResponseFailure());
		    check422Response(RouteConstants.CUST_DOCS_USER_PROFILE_LOAD, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);

			when(mockCustDocsProfileService.loadUserProfile(any(SessionContainer.class), any()))
				.thenReturn(getLoadResponseSuccess());
		    checkStandard200Response(RouteConstants.CUST_DOCS_USER_PROFILE_LOAD, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

			requestString = getJsonRequest(req);
			when(mockCustDocsProfileService.loadUserProfile(any(SessionContainer.class), any()))
				.thenReturn(getLoadResponseSuccess());
			checkStandard200Response(RouteConstants.CUST_DOCS_USER_PROFILE_LOAD, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
		}

		C1UXCustDocLoadProfileResponse getLoadResponseFailure() {
			C1UXCustDocLoadProfileResponse resp = new C1UXCustDocLoadProfileResponse();
			resp.setMessage(GENERIC_ERROR_MSG);
			return resp;
		}

		C1UXCustDocLoadProfileResponse getLoadResponseSuccess() {
			C1UXCustDocLoadProfileResponse resp = new C1UXCustDocLoadProfileResponse();
			resp.setSuccess(true);
			return resp;
		}

		@Test
		void that_loadAltProfile_returnsExpected() throws Exception, AtWinXSException {
			when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
			C1UXCustDocLoadAltProfileRequest req = new C1UXCustDocLoadAltProfileRequest();
			String requestString = getJsonRequest(req);
			
			when(mockCustDocsProfileService.loadAltProfile(any(SessionContainer.class), any())).thenReturn(getLoadAltResponseFailure());
			check422Response(RouteConstants.CUST_DOCS_LOAD_ALT_PROFILE_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);

			when(mockCustDocsProfileService.loadAltProfile(any(SessionContainer.class), any())).thenReturn(getLoadAltResponseSuccess());
			checkStandard200Response(RouteConstants.CUST_DOCS_LOAD_ALT_PROFILE_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

		}
		
		C1UXCustDocLoadAltProfileResponse getLoadAltResponseFailure() {
			C1UXCustDocLoadAltProfileResponse resp = new C1UXCustDocLoadAltProfileResponse();
			resp.setMessage(GENERIC_ERROR_MSG);
			return resp;
		}

		C1UXCustDocLoadAltProfileResponse getLoadAltResponseSuccess() {
			C1UXCustDocLoadAltProfileResponse resp = new C1UXCustDocLoadAltProfileResponse();
			resp.setSuccess(true);
			return resp;
		}
	}


