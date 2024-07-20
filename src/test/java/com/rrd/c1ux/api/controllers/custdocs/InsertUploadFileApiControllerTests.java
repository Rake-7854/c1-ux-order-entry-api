package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileApiRequest;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;


@WithMockUser
class InsertUploadFileApiControllerTests extends BaseMvcTest {
	private static final String GENERIC_ERROR_MSG = "Failure message generic";
	String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";
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

	public void setUpAll() throws Exception {
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

	//@Test
	void insertUploadFile_success() throws Exception {
			setUpAll();
	        String requestString = getJsonRequest(getUploadFileDownloadRequest());

	        when(mockCustDocsFileStreamService.prepareDownload(any(InsertUploadFileApiRequest.class), any(AppSessionBean.class)))
					.thenReturn(getResponseSuccess());
	        when(mockCustDocsFileStreamService.getFailureDownloadMessage(mockAppSessionBean)).thenReturn(GENERIC_ERROR_MSG);
		    checkStreaming200Response(RouteConstants.CUST_DOCS_INSERT_UPLOAD_FILE, requestString, TEST_ENCRYPTED_SESSIONID);
	}

	@Test
	void insertUploadFile_failure() throws Exception {
			setUpAll();
	        String requestString = getJsonRequest(getUploadFileDownloadRequest());

	        when(mockCustDocsFileStreamService.getFailureDownloadMessage(mockAppSessionBean)).thenReturn(GENERIC_ERROR_MSG);
	        when(mockCustDocsFileStreamService.prepareDownload(any(InsertUploadFileApiRequest.class), any(AppSessionBean.class)))
					.thenReturn(getResponseFailure());
	        ResultActions result = doPostTest(RouteConstants.CUST_DOCS_INSERT_UPLOAD_FILE, TEST_ENCRYPTED_SESSIONID, requestString);
	        assertNotNull(result);
	}

	private InsertUploadFileApiRequest getUploadFileDownloadRequest() {
		InsertUploadFileApiRequest req = new InsertUploadFileApiRequest();
		req.setFileId("2");
		req.setFileName("amyfile.pdf");
		return req;
	}

	private InsertUploadFileResponse getResponseSuccess() {
		InsertUploadFileResponse response = new InsertUploadFileResponse();
		response.setSuccess(true);
		return response;
	}

	private InsertUploadFileResponse getResponseFailure() {
		InsertUploadFileResponse response = new InsertUploadFileResponse();
		response.setMessage(GENERIC_ERROR_MSG);
		response.setFailureMessage(GENERIC_ERROR_MSG);
		return response;
	}

}
