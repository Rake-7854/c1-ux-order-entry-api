/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/22/24				R Ruth			    	CAP-42226 					Controller for lists
 *	05/28/24				A Boomker				CAP-48604					Add save new list API
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXSaveListResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
class CustDocListControllerTests extends BaseMvcTest {

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
	String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";
	@InjectMocks
	protected CustDocListController controller;

	@BeforeEach
	public void setUpAll() throws Exception {
		controller = new CustDocListController(mockTokenReader, mockSessionReader, mockCustomDocsListService);
		controller = Mockito.spy(controller);
		doReturn(mockCustomDocsListService).when(controller).getClassSpecificService(mockCustomDocsListService, mockSessionContainer);
		setupBaseMockSessions();
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
		TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
//	    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
//	    when(mockItem.getUserInterface()).thenReturn(mockUI);
	    when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(true);
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
	}

	@Test
	void that_getLists_returnsExpected() throws Exception, AtWinXSException {
        when(mockCustomDocsListService.getListsApi(any(SessionContainer.class)))
				.thenReturn(getCustDocListResponseSuccess());
        assertEquals(HttpStatus.OK, controller.getLists(AtWinXSConstant.EMPTY_STRING).getStatusCode());


        when(mockCustomDocsListService.getListsApi(any(SessionContainer.class)))
				.thenReturn(new C1UXCustDocListResponse());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.getLists(AtWinXSConstant.EMPTY_STRING).getStatusCode());
	}

	@Test
	void that_saveNewList_returnsExpected() throws Exception, AtWinXSException {
       when(mockCustomDocsListService.saveNewList(any(SessionContainer.class)))
				.thenReturn(getSaveListResponseSuccess());
        assertEquals(HttpStatus.OK, controller.saveNewList(AtWinXSConstant.EMPTY_STRING).getStatusCode());


        when(mockCustomDocsListService.saveNewList(any(SessionContainer.class)))
				.thenReturn(new C1UXSaveListResponse());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.saveNewList(AtWinXSConstant.EMPTY_STRING).getStatusCode());

        when(mockCustomDocsListService.saveNewList(any(SessionContainer.class)))
 				.thenThrow(new AccessForbiddenException(TEST_ENCRYPTED_SESSIONID));
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.saveNewList(AtWinXSConstant.EMPTY_STRING);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	private C1UXCustDocListResponse getCustDocListResponseSuccess() {
		C1UXCustDocListResponse response = new C1UXCustDocListResponse();
		response.setSuccess(true);
		return response;
	}

	private C1UXSaveListResponse getSaveListResponseSuccess() {
		C1UXSaveListResponse response = new C1UXSaveListResponse();
		response.setSuccess(true);
		return response;
	}

}
