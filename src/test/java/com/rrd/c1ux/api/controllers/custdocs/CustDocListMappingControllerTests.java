/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/16/24				A Boomker		    	CAP-42228 					Controller for lists
 * 	06/04/24				A Boomker				CAP-42231					Adding get mapped data page
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
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
class CustDocListMappingControllerTests extends BaseMvcTest {

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
	protected CustDocListMappingController controller;

	@BeforeEach
	public void setUpAll() throws Exception {
		controller = new CustDocListMappingController(mockTokenReader, mockSessionReader, mockCustomDocsListMappingService);
		controller = Mockito.spy(controller);
		doReturn(mockCustomDocsListMappingService).when(controller).getClassSpecificService(mockCustomDocsListMappingService, mockSessionContainer);
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
	void that_getListMappingPreview_returnsExpected() throws Exception, AtWinXSException {
		C1UXCustDocListForMappingRequest req = new C1UXCustDocListForMappingRequest();
        when(mockCustomDocsListMappingService.getListMappings(any(SessionContainer.class), anyInt()))
				.thenReturn(getC1UXCustDocListForMappingResponseSuccess());
        assertEquals(HttpStatus.OK, controller.getListMappingPreview(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());


        when(mockCustomDocsListMappingService.getListMappings(any(SessionContainer.class), anyInt()))
				.thenReturn(new C1UXCustDocListForMappingResponse());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.getListMappingPreview(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.getListMappingPreview(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}

	private C1UXCustDocListForMappingResponse getC1UXCustDocListForMappingResponseSuccess() {
		C1UXCustDocListForMappingResponse response = new C1UXCustDocListForMappingResponse();
		response.setSuccess(true);
		return response;
	}

	@Test
	void that_saveListMapping_returnsExpected() throws Exception, AtWinXSException {
		C1UXCustDocSaveListMappingRequest req = new C1UXCustDocSaveListMappingRequest();
        when(mockCustomDocsListMappingService.saveListMapping(any(SessionContainer.class), any()))
				.thenReturn(getC1UXCustDocSaveListMappingResponseSuccess());
        assertEquals(HttpStatus.OK, controller.saveListMapping(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());


        when(mockCustomDocsListMappingService.saveListMapping(any(SessionContainer.class), any()))
				.thenReturn(new C1UXCustDocSaveListMappingResponse());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.saveListMapping(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.saveListMapping(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	private C1UXCustDocSaveListMappingResponse getC1UXCustDocSaveListMappingResponseSuccess() {
		C1UXCustDocSaveListMappingResponse response = new C1UXCustDocSaveListMappingResponse();
		response.setSuccess(true);
		return response;
	}

	@Test
	void that_getMappedDataPage_returnsExpected() throws Exception, AtWinXSException {
		C1UXCustDocMappedDataRequest req = new C1UXCustDocMappedDataRequest();
        when(mockCustomDocsListMappingService.getMappedDataPage(any(SessionContainer.class), any()))
				.thenReturn(getC1UXCustDocMappedDataResponseSuccess());
        assertEquals(HttpStatus.OK, controller.getMappedDataPage(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());


        when(mockCustomDocsListMappingService.getMappedDataPage(any(SessionContainer.class), any()))
				.thenReturn(new C1UXCustDocMappedDataResponse());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.getMappedDataPage(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.getMappedDataPage(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	private C1UXCustDocMappedDataResponse getC1UXCustDocMappedDataResponseSuccess() {
		C1UXCustDocMappedDataResponse response = new C1UXCustDocMappedDataResponse();
		response.setSuccess(true);
		return response;
	}

}
