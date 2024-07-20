/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 * 	07/09/24	A Boomker			CAP-46538		Initial version
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBasicImprintHistorySearchRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocImprintHistorySearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
class CustomDocsImprintHistoryControllerTests  extends BaseMvcTest {

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

		@Mock
		private C1UXCustDocLoadImprintHistoryRequest mockRequest;

		@InjectMocks
		protected CustDocsImprintHistoryController controller;



		private static final String GENERIC_ERROR_MSG = "Failure message generic";
	private String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";

	@BeforeEach
	public void setUp() throws Exception {
		controller = new CustDocsImprintHistoryController(mockTokenReader, mockSessionReader, mockCustDocsImprintHistoryService);
		controller = Mockito.spy(controller);
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


	private C1UXCustDocBaseResponse getImprintHistoryResponseSuccess() {
		C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
		response.setSuccess(true);
		return response;
	}

	private C1UXCustDocBaseResponse getImprintHistoryResponseFailure() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setMessage(GENERIC_ERROR_MSG);
		return baseResponse;
	}


	@Test
	void that_loadSelectedImprintHistory_returnsExpected() throws Exception, AtWinXSException {
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
	    C1UXCustDocLoadImprintHistoryRequest req = new C1UXCustDocLoadImprintHistoryRequest();

		when(mockCustDocsImprintHistoryService.getSelectedImprintHistory(any(SessionContainer.class), any()))
			.thenReturn(getImprintHistoryResponseFailure());
        when(mockCustDocsImprintHistoryService.getItemClassFromSession(mockSessionContainer)).thenReturn(null);
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.loadImprintHistory(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

		when(mockCustDocsImprintHistoryService.getSelectedImprintHistory(any(SessionContainer.class), any()))
			.thenReturn(getImprintHistoryResponseSuccess());
	       assertEquals(HttpStatus.OK, controller.loadImprintHistory(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());
	}

	@Test
	void that_basicImprintHistorySearch_returnsExpected() throws Exception, AtWinXSException {
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
	    C1UXCustDocBasicImprintHistorySearchRequest req = new C1UXCustDocBasicImprintHistorySearchRequest();
		String requestString = getJsonRequest(req);

		when(mockCustDocsImprintHistoryService.basicImprintHistorySearch(any(SessionContainer.class), any()))
			.thenReturn(getSearchResponseSuccess());
		checkStandard200Response(RouteConstants.CUST_DOCS_IMPRINT_HISTORY_BASIC_SEARCH, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

		when(mockCustDocsImprintHistoryService.basicImprintHistorySearch(any(SessionContainer.class), any()))
			.thenReturn(getSearchResponseFailure());
	    check422Response(RouteConstants.CUST_DOCS_IMPRINT_HISTORY_BASIC_SEARCH, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
	}

	private C1UXCustDocImprintHistorySearchResponse getSearchResponseSuccess() {
		 C1UXCustDocImprintHistorySearchResponse response = new C1UXCustDocImprintHistorySearchResponse();
		 response.setSuccess(true);
		 return response;
	}

	private C1UXCustDocImprintHistorySearchResponse getSearchResponseFailure() {
		 C1UXCustDocImprintHistorySearchResponse response = new C1UXCustDocImprintHistorySearchResponse();
		 response.setMessage(GENERIC_ERROR_MSG);
		 return response;
	}


}


