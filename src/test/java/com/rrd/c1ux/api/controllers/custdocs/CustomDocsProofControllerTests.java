/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  08/23/23    A Boomker       CAP-43223                   Initial
 *  11/13/23	A Boomker		CAP-44426					Added handling for update working proof
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUIPageSubmitRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocWorkingProofResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
public class CustomDocsProofControllerTests  extends BaseMvcTest {

	Map<String, String> uiRequest = null;
	Collection<String> errorStrings = null;
	public static final String NEW_LINE_CHAR = "\n";
	C1UXCustDocUIPageSubmitRequest saveRequest = null;

	private C1UXCustDocProofLinkRequest proofLinkRequest;
//	private C1UXCustDocProofLinkResponse proofLinkResponse;
//	private C1UXCustDocProofStatusResponse proofStatusResponse;

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

		@InjectMocks
		protected CustDocsProofController controller;

	private static final String GENERIC_ERROR_MSG = "Failure message generic";
	String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";

	@BeforeEach
		public void setUp() throws Exception {
			controller = new CustDocsProofController(mockTokenReader, mockSessionReader, mockCustDocsProofService, mockCustDocsUIService);
			controller = Mockito.spy(controller);
			doReturn(mockCustDocsProofService).when(controller).getClassSpecificService(mockCustDocsProofService, mockSessionContainer);
			doReturn(mockCustDocsUIService).when(controller).getClassSpecificService(mockCustDocsUIService, mockSessionContainer);
		    setupBaseMockSessions();
			when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
			TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
//		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
//		    when(mockItem.getUserInterface()).thenReturn(mockUI);
		    when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(true);
		    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
		}

		@Test
		void that_getProofLink_returnsExpected() throws Exception, AtWinXSException {
			C1UXCustDocProofLinkRequest req = getProofLinkRequest();
//	        String requestString = getJsonRequest(req);

	        when(mockCustDocsProofService.getProofLink(any(SessionContainer.class), any(C1UXCustDocProofLinkRequest.class)))
					.thenReturn(getProofLinkResponseSuccess());
//		    checkStandard200Response(RouteConstants.CUST_DOCS_GET_PROOF_URL, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
	        assertEquals(HttpStatus.OK, controller.getProofUrl(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());


	        when(mockCustDocsProofService.getProofLink(any(SessionContainer.class), any(C1UXCustDocProofLinkRequest.class)))
					.thenReturn(getProofLinkResponseFailure());
//	        check422Response(RouteConstants.CUST_DOCS_GET_PROOF_URL, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
	        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.getProofUrl(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

		}

		@Test
		void that_getProofStatus_returnsExpected() throws Exception, AtWinXSException {
	        String requestString = AtWinXSConstant.EMPTY_STRING;

	        when(mockCustDocsProofService.getCurrentImageProofStatus(any()))
					.thenReturn(getProofStatusResponseSuccess());
//		    checkStandard200Response(RouteConstants.CUST_DOCS_CHECK_IMAGE_PROOF_STATUS, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
	        assertEquals(HttpStatus.OK, controller.checkImageProofStatus(requestString).getStatusCode());

	        when(mockCustDocsProofService.getCurrentImageProofStatus(any(SessionContainer.class)))
					.thenReturn(getProofStatusResponseFailure());
//	        check422Response(RouteConstants.CUST_DOCS_CHECK_IMAGE_PROOF_STATUS, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
	        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.checkImageProofStatus(requestString).getStatusCode());
		}

		private C1UXCustDocProofLinkRequest getProofLinkRequest() {
			proofLinkRequest = new C1UXCustDocProofLinkRequest();
			proofLinkRequest.setProofType(ProofType.PDF.toString());
			return proofLinkRequest;
		}

		private C1UXCustDocProofLinkResponse getProofLinkResponseSuccess() {
			C1UXCustDocProofLinkResponse proofLinkResponse = new C1UXCustDocProofLinkResponse();
			proofLinkResponse.setSuccess(true);
			return proofLinkResponse;
		}

		private C1UXCustDocProofLinkResponse getProofLinkResponseFailure() {
			C1UXCustDocProofLinkResponse proofLinkResponse = new C1UXCustDocProofLinkResponse();
			proofLinkResponse.setMessage(GENERIC_ERROR_MSG);
			return proofLinkResponse;
		}

		private C1UXCustDocProofStatusResponse getProofStatusResponseSuccess() {
			C1UXCustDocProofStatusResponse proofStatusResponse = new C1UXCustDocProofStatusResponse();
			proofStatusResponse.setSuccess(true);
			return proofStatusResponse;
		}

		private C1UXCustDocProofStatusResponse getProofStatusResponseFailure() {
			C1UXCustDocProofStatusResponse proofStatusResponse = new C1UXCustDocProofStatusResponse();
			proofStatusResponse.setMessage(GENERIC_ERROR_MSG);
			return proofStatusResponse;
		}

		private C1UXCustDocUIPageSubmitRequest getUpdateProofRequest() {
			saveRequest = new C1UXCustDocUIPageSubmitRequest();
			saveRequest.setForm1(ICustomDocsAdminConstants.HDN_UI_STEP_PAGE_NUM + "=1");
			return saveRequest;
		}

		private C1UXCustDocWorkingProofResponse getUpdateProofResponseSuccess() {
			C1UXCustDocWorkingProofResponse baseResponse = new C1UXCustDocWorkingProofResponse();
			baseResponse.setSuccess(true);
			return baseResponse;
		}

		private C1UXCustDocWorkingProofResponse getUpdateProofResponseFailure() {
			C1UXCustDocWorkingProofResponse baseResponse = new C1UXCustDocWorkingProofResponse();
			baseResponse.setSuccess(true);
			return baseResponse;
		}

		@Test
		void that_updateWorkingProof_returnsExpected() throws Exception, AtWinXSException {
		    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
		    C1UXCustDocUIPageSubmitRequest req = getUpdateProofRequest();
//			String requestString = getJsonRequest(req);

			when(mockCustDocsUIService.performPageSubmitAction(any(SessionContainer.class), anyMap()))
				.thenReturn(getSaveResponseFailure());
	        when(mockCustDocsProofService.getWorkingProof(any(SessionContainer.class), anyMap()))
					.thenReturn(getUpdateProofResponseFailure());
	        assertEquals(HttpStatus.OK, controller.updateWorkingProof(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());
//		    checkStandard200Response(RouteConstants.CUST_DOCS_UPDATE_WORKING_PROOF, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

	        when(mockCustDocsProofService.getWorkingProof(any(SessionContainer.class), anyMap()))
					.thenReturn(getUpdateProofResponseSuccess());
//		    checkStandard200Response(RouteConstants.CUST_DOCS_UPDATE_WORKING_PROOF, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
	        assertEquals(HttpStatus.OK, controller.updateWorkingProof(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

			when(mockCustDocsUIService.performPageSubmitAction(any(SessionContainer.class), anyMap()))
				.thenReturn(getSaveResponseSuccess());
	        assertEquals(HttpStatus.OK, controller.updateWorkingProof(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());
			//			checkStandard200Response(RouteConstants.CUST_DOCS_UPDATE_WORKING_PROOF, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

		}

		private C1UXCustDocBaseResponse getSaveResponseSuccess() {
			C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
			baseResponse.setSuccess(true);
			return baseResponse;
		}

		private C1UXCustDocBaseResponse getSaveResponseFailure() {
			C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
			baseResponse.setMessage(GENERIC_ERROR_MSG);
			return baseResponse;
		}

	}


