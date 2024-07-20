/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  08/23/23    A Boomker       CAP-43223                   Initial
 * 	01/17/24	A Boomker		CAP-44835					Added handling for uploading variable files
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
@WithMockUser
class CustomDocsFileStreamControllerTests  extends BaseMvcTest {

	Map<String, String> uiRequest = null;
//	Collection<String> errorStrings = null;

	private C1UXCustDocUploadedFileDetailsRequest request;

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
	String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";

		public void setUpAll() throws Exception {
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
		void that_getUploadFileDetails_returnsExpected() throws Exception, AtWinXSException {
			setUpAll();
	        String requestString = getJsonRequest(getUploadFileDetailsRequest());

	        when(mockCustDocsFileStreamService.getUploadedFileDetails(any(SessionContainer.class), any(C1UXCustDocUploadedFileDetailsRequest.class)))
					.thenReturn(getResponseSuccess());
		    checkStandard200Response(RouteConstants.CUST_DOCS_GET_UPLOADED_FILE_INFO_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);


	        when(mockCustDocsFileStreamService.getUploadedFileDetails(any(SessionContainer.class), any(C1UXCustDocUploadedFileDetailsRequest.class)))
					.thenReturn(getResponseFailure());
	        check422Response(RouteConstants.CUST_DOCS_GET_UPLOADED_FILE_INFO_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
		}

		private C1UXCustDocUploadedFileDetailsRequest getUploadFileDetailsRequest() {
			request = new C1UXCustDocUploadedFileDetailsRequest();
			request.setHdnVar("ABC");
			return request;
		}

		private C1UXCustDocUploadedFileDetailsResponse getResponseSuccess() {
			C1UXCustDocUploadedFileDetailsResponse response = new C1UXCustDocUploadedFileDetailsResponse();
			response.setSuccess(true);
			return response;
		}

		private C1UXCustDocUploadedFileDetailsResponse getResponseFailure() {
			C1UXCustDocUploadedFileDetailsResponse response = new C1UXCustDocUploadedFileDetailsResponse();
			response.setMessage(GENERIC_ERROR_MSG);
			return response;
		}

		@Test
		void validateSession() {
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
			when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(null);
			CustomDocsUserInterfaceController controller = new CustomDocsUserInterfaceController(null, null, null);
			try {
				controller.validateSession(mockSessionContainer);
				assertTrue(false); // if we got here, it didn't throw the exception
			}
			catch(AccessForbiddenException af)
			{
				assertTrue(true);
			}
			catch(Exception e)
			{ // if we got here, it threw some OTHER exception
				assertTrue(false);
			}
		}

		@Test
		void that_uploadVariableFile_returnsExpected() throws Exception, AtWinXSException {
			setUpAll();
	        String requestString = getJsonRequest(getUploadVariableFileRequest());

	        when(mockCustDocsFileStreamService.uploadVariableFile(any(SessionContainer.class), any(C1UXCustDocUploadVariableFileRequest.class)))
					.thenReturn(getUploadVariableResponseSuccess());
		    checkStandard200Response(RouteConstants.CUST_DOCS_VAR_UPLOAD_FILE_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);


	        when(mockCustDocsFileStreamService.uploadVariableFile(any(SessionContainer.class), any(C1UXCustDocUploadVariableFileRequest.class)))
					.thenReturn(getUploadVariableResponseFailure());
	        check422Response(RouteConstants.CUST_DOCS_VAR_UPLOAD_FILE_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
		}

		private C1UXCustDocUploadVariableFileRequest getUploadVariableFileRequest() {
			C1UXCustDocUploadVariableFileRequest request = new C1UXCustDocUploadVariableFileRequest();
			request.setHdnVar("ABC");
			return request;
		}

		private C1UXCustDocUploadVariableFileResponse getUploadVariableResponseSuccess() {
			C1UXCustDocUploadVariableFileResponse response = new C1UXCustDocUploadVariableFileResponse();
			response.setSuccess(true);
			return response;
		}

		private C1UXCustDocUploadVariableFileResponse getUploadVariableResponseFailure() {
			C1UXCustDocUploadVariableFileResponse response = new C1UXCustDocUploadVariableFileResponse();
			response.setMessage(GENERIC_ERROR_MSG);
			return response;
		}

}


