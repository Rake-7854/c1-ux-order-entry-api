/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  07/25/23    A Boomker       CAP-42295                   Initial
 *  08/21/23	A Boomker		CAP-43223					Making this class comply with minimums
 * 	01/09/24	A Boomker		CAP-44839					Updates after CP side implemented c1ux code
 * 	01/17/24	A Boomker		CAP-44835					Added handling for uploading variable files
 *	02/02/24	A Boomker		CAP-46337					Added handling for file upload
 *	02/14/24	A Boomker		CAP-46309					Moved some things to new base class
 *	02/27/24	A Boomker		CAP-47446					Fixes for extensions on upload
 *	03/06/24	A Boomker		CAP-46508					Fix junits for insertion group upload
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.exceptions.BadRequestException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsResponse;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileApiRequest;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.customdocs.ui.download.DownloadFileImpl;
import com.rrd.custompoint.orderentry.customdocs.Variable.PageflexType;
import com.rrd.custompoint.orderentry.customdocs.ui.download.DownloadFileC1uxBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocProofFormBean;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileC1uxBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.interfaces.ICustomDocsCopyFiles;
import com.wallace.atwinxs.orderentry.customdocs.compserv.util.CSCopyFilesResponse;

class CustomDocsFileStreamServiceImplTests extends BaseCustomDocsServiceTests {

  @InjectMocks
  private CustomDocsFileStreamServiceImpl service;

  @Mock
  private DownloadFileImpl mockDownloadFileObject;

  @Mock
  private CustDocProofFormBean mockProofBean;
  private CustDocProofFormBean proofBean;

  @Mock
  private C1UXCustDocBaseResponse mockBaseResponse;
  private C1UXCustDocBaseResponse baseResponse;

  @Mock
  private C1UXCustDocPageBean mockPageResponse;
  private C1UXCustDocPageBean pageResponse;


  @Mock
  private ICustomDocsCopyFiles mockCopyFilesComponent;
  @Mock
  private CSCopyFilesResponse mockCopyFilesResponse;

  @Mock
  private java.io.OutputStream mockOutputStream;
  @Mock
  private java.io.File mockFile;
  @Mock
  private MultipartFile mockMultipartFile;

  protected static final String CLASSIC_CONTEXT_PATH = "/xs2";
  protected static final String SCRIPT_PARSE_TESTA = "<SCRIPT >alert(\"Hello World!\");</SCRIPT>";
  protected static final String SCRIPT_PARSE_TESTB = "<SCRIPT type=\"text/javascript\">alert(\"Hello World!\");</SCRIPT>";
  protected static final String SCRIPT_PARSE_TESTC = "<script type=\"text/javascript\">alert(\"Hello World!\");</script>"
  	+ "<SCRIPT type=\"text/javascript\">alert(\"Hello World!\");</SCRIPT>";

	public static final String LOGIN_ID="AMYC1UX";
	public static final String VENDOR_ITEM_NUM="74101875X1000";
	public static final String CUSTOMER_ITEM_NUM="10.1875 WIDTH";
	public static final String ITEM_DESC="Single Editable Textbox Cust Doc - For Cust Docs testing ONLY - do not add to cart in regular C1UX!";
	public static final String VAR_XML_MODIFIED="<VAR_VALUES><VAR external_source_code=\"P\" initial=\"N\" name=\"text1\" list_val=\"\" label=\"My Required Var Display label\" key_val=\"N\" type=\"T\">AmeliaPlusIModifiedThis</VAR><VAR external_source_code=\"C\" initial=\"Y\" name=\"DisplayVar\" list_val=\"\" label=\"\" key_val=\"N\" type=\"S\">TEST2</VAR></VAR_VALUES>";

    private static final String GENERIC_ERROR_MSG = "Failure message generic";

	@Test
	void getUploadedFileDetails() throws Exception {
		setUpModuleSessionNoBase();
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockUI.getAllVariables()).thenReturn(mockVariables);
	      C1UXCustDocUploadedFileDetailsRequest request = new C1UXCustDocUploadedFileDetailsRequest();
		C1UXCustDocUploadedFileDetailsResponse detailsResponse = service.getUploadedFileDetails(mockSessionContainer,
				request);
		assertFalse(detailsResponse.isSuccess());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, detailsResponse.getMessage());

	    request.setHdnVar(CLASSIC_CONTEXT_PATH);
		detailsResponse = service.getUploadedFileDetails(mockSessionContainer,
				request);
		assertFalse(detailsResponse.isSuccess());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, detailsResponse.getMessage());

	    when(mockVariables.find(any())).thenReturn(mockVar);
		detailsResponse = service.getUploadedFileDetails(mockSessionContainer, request);
		assertFalse(detailsResponse.isSuccess());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, detailsResponse.getMessage());

		request.setHdnVarValue(CLASSIC_CONTEXT_PATH);
	      service = Mockito.spy(service);
	      doReturn(true).when(service).isVarValueUploadOrSearch(any());
	      doReturn(true).when(service).validRequestForVar(eq(mockVar), anyBoolean());
	      doReturn(mockDownloadFileObject).when(service).getDownloadFileObject(mockAppSessionBean);
	      when(mockDownloadFileObject.isReadyToViewFile()).thenReturn(false);
	      doReturn(getSuccessfulDownloadInfo()).when(service).callCustomPointDownloadInfo(any(), any(), any(), anyString());

		detailsResponse = service.getUploadedFileDetails(mockSessionContainer, request);
		assertEquals(AtWinXSConstant.EMPTY_STRING, detailsResponse.getMessage());
		assertTrue(detailsResponse.isSuccess());

	}

	@Test
	void validRequestForVar() {
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.AREA_TEMPLATE);
		assertFalse(service.validRequestForVar(mockVar, false));
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.INSERTION_GROUP);
		assertTrue(service.validRequestForVar(mockVar, false));
		assertTrue(service.validRequestForVar(mockVar, true));
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.FILE_UPLOAD);
		assertTrue(service.validRequestForVar(mockVar, true));
		assertFalse(service.validRequestForVar(mockVar, false));

		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.IMAGE);
		when(mockVar.isSearchImages()).thenReturn(false);
		when(mockVar.isUploadAllowedInd()).thenReturn(false);
		assertFalse(service.validRequestForVar(mockVar, false));
		assertFalse(service.validRequestForVar(mockVar, true));

		when(mockVar.isSearchImages()).thenReturn(true);
		assertFalse(service.validRequestForVar(mockVar, false));
		assertTrue(service.validRequestForVar(mockVar, true));

		when(mockVar.isUploadAllowedInd()).thenReturn(true);
		assertFalse(service.validRequestForVar(mockVar, false));
		assertTrue(service.validRequestForVar(mockVar, true));
	}

	@Test
	void isVarValueUploadOrSearch() {
		assertFalse(service.isVarValueUploadOrSearch("1"));
		assertFalse(service.isVarValueUploadOrSearch("D26236")); // like a dynamic list value
		assertFalse(service.isVarValueUploadOrSearch("^"));
		assertFalse(service.isVarValueUploadOrSearch("U^"));
		assertFalse(service.isVarValueUploadOrSearch("S^"));
		assertFalse(service.isVarValueUploadOrSearch("U1234"));
		assertFalse(service.isVarValueUploadOrSearch("S1234"));
		assertFalse(service.isVarValueUploadOrSearch("UPLOAD: filename"));
		assertFalse(service.isVarValueUploadOrSearch("SEARCH: filename"));
		assertFalse(service.isVarValueUploadOrSearch("U1234UPLOAD: filename"));
		assertFalse(service.isVarValueUploadOrSearch("S1234SEARCH: filename"));
		assertFalse(service.isVarValueUploadOrSearch("U^SEARCH:"));
		assertFalse(service.isVarValueUploadOrSearch("S^UPLOAD:"));
		assertTrue(service.isVarValueUploadOrSearch("S^SEARCH: ")); // even though the number and file name are missing, that's CP only validation logic
		assertTrue(service.isVarValueUploadOrSearch("U^UPLOAD: "));// even though the number and file name are missing, that's CP only validation logic
	}

	@Test
	void getDownloadInfoFromCustomPoint() throws AtWinXSException {
	      service = Mockito.spy(service);
	      doReturn(false).when(service).isVarValueUploadOrSearch(anyString());
	      doReturn(false).when(service).validRequestForVar(mockVar, false);
	      doReturn(SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT).when(service)
	      		.getTranslation(eq(mockAppSessionBean), anyString(), anyString());

	      C1UXCustDocUploadedFileDetailsResponse sfBean = new C1UXCustDocUploadedFileDetailsResponse();
	      service.getDownloadInfoFromCustomPoint(CLASSIC_CONTEXT_PATH, sfBean, mockVar, mockAppSessionBean);
	      assertEquals(SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT, sfBean.getMessage());
	      assertFalse(sfBean.isSuccess());
	      assertFalse(sfBean.isCanDownloadImmediately());
	      assertNull(sfBean.getDownloadInfo());

	      doReturn(true).when(service).isVarValueUploadOrSearch(anyString());
	      doReturn(true).when(service).validRequestForVar(mockVar, true);
	      doReturn(mockDownloadFileObject).when(service).getDownloadFileObject(mockAppSessionBean);
	      when(mockDownloadFileObject.isReadyToViewFile()).thenReturn(false);
	      doReturn(null).when(service).callCustomPointDownloadInfo(any(), any(), any(), anyString());
	      sfBean = new C1UXCustDocUploadedFileDetailsResponse();
	      service.getDownloadInfoFromCustomPoint(CLASSIC_CONTEXT_PATH, sfBean, mockVar, mockAppSessionBean);
	      assertEquals(SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT, sfBean.getMessage());
	      assertFalse(sfBean.isSuccess());
	      assertFalse(sfBean.isCanDownloadImmediately());
	      assertNull(sfBean.getDownloadInfo());

	      DownloadFileC1uxBean info = getSuccessfulDownloadInfo();
	      doReturn(info).when(service).callCustomPointDownloadInfo(any(), any(), any(), anyString());
	      sfBean = new C1UXCustDocUploadedFileDetailsResponse();
	      service.getDownloadInfoFromCustomPoint(CLASSIC_CONTEXT_PATH, sfBean, mockVar, mockAppSessionBean);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());
	      assertTrue(sfBean.isSuccess());
	      assertFalse(sfBean.isCanDownloadImmediately());
	      assertEquals(info, sfBean.getDownloadInfo());

	      when(mockDownloadFileObject.isReadyToViewFile()).thenReturn(true);
	      sfBean = new C1UXCustDocUploadedFileDetailsResponse();
	      service.getDownloadInfoFromCustomPoint(CLASSIC_CONTEXT_PATH, sfBean, mockVar, mockAppSessionBean);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());
	      assertTrue(sfBean.isSuccess());
	      assertTrue(sfBean.isCanDownloadImmediately());
	      assertEquals(info, sfBean.getDownloadInfo());

	}

	private DownloadFileC1uxBean getSuccessfulDownloadInfo() {
		DownloadFileC1uxBean info = new DownloadFileC1uxBean();
		info.setFileName1(CLASSIC_CONTEXT_PATH);
		info.setFileName2(SCRIPT_PARSE_TESTA);
		info.setImage(true);
		info.setUpload(true);
		return info;
	}


	@Test
	void uploadVariableFile() throws Exception {
	      service = Mockito.spy(service);

	      doNothing().when(service).uploadFileThroughCustomPoint(any(), any(), any(), any(), any(), any());
			setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);
		      when(mockUI.getAllVariables()).thenReturn(mockVariables);
			C1UXCustDocUploadVariableFileRequest request = new C1UXCustDocUploadVariableFileRequest();

			C1UXCustDocUploadVariableFileResponse response = null;
			assertThrows(BadRequestException.class, () -> {
				C1UXCustDocUploadVariableFileRequest request2 = new C1UXCustDocUploadVariableFileRequest();
				service.uploadVariableFile(mockSessionContainer, request2);
			});


			assertThrows(AccessForbiddenException.class, () -> {
				C1UXCustDocUploadVariableFileRequest request2 = new C1UXCustDocUploadVariableFileRequest();
				request2.setFile(mockMultipartFile);
				service.uploadVariableFile(mockSessionContainer, request2);
			});

			assertThrows(AccessForbiddenException.class, () -> {
				C1UXCustDocUploadVariableFileRequest request2 = new C1UXCustDocUploadVariableFileRequest();
				request2.setHdnVar(CLASSIC_CONTEXT_PATH);
				request2.setFile(mockMultipartFile);
				service.uploadVariableFile(mockSessionContainer, request2);
			});

		    when(mockVariables.find(any())).thenReturn(mockVar);

			assertThrows(AccessForbiddenException.class, () -> {
				C1UXCustDocUploadVariableFileRequest request2 = new C1UXCustDocUploadVariableFileRequest();
				request2.setHdnVar(CLASSIC_CONTEXT_PATH);
				request2.setFile(mockMultipartFile);
				when(mockVar.isUploadAllowedInd()).thenReturn(false);
				service.uploadVariableFile(mockSessionContainer, request2);
			});

			when(mockVar.isUploadAllowedInd()).thenReturn(true);
		    request.setHdnVar(CLASSIC_CONTEXT_PATH);
			request.setFile(mockMultipartFile);
		    response = service.uploadVariableFile(mockSessionContainer,	request);
			assertFalse(response.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());
	}

	@Test
	void persistReuse() throws AtWinXSException {
		service = Mockito.spy(service);
		when(mockAppSessionBean.getSessionID()).thenReturn(12345);
		doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
		service.persistReuse(mockUI, mockOESession, mockAppSessionBean);
		when(mockUI.isReuseUploadFiles()).thenReturn(true);
		service.persistReuse(mockUI, mockOESession, mockAppSessionBean);
		assertTrue(true);
	}

	private HashMap<String, String> getFreshExtMap() {
		HashMap<String, String> extMap = new HashMap<String, String>();
		extMap.put(CUSTOMER_ITEM_NUM, CLASSIC_CONTEXT_PATH);
		extMap.put(VENDOR_ITEM_NUM, CLASSIC_CONTEXT_PATH);
		return extMap;
	}

	@Test
	void updateEPSExtensions()
	{
	      service = Mockito.spy(service);
	    doReturn(false).when(service).checkIfBUAllowsEPS(mockAppSessionBean);

		when(mockVar.isAllowSTDImg()).thenReturn(true);
		Map<String, String> extMap = getFreshExtMap();
		int freshMapSize = extMap.size();
		when(mockUploadFileImage.getValidExtensions()).thenReturn(extMap);
		service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
		assertEquals(freshMapSize, mockUploadFileImage.getValidExtensions().size());

		when(mockVar.isAllowSTDImg()).thenReturn(true);
		service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
		assertEquals(freshMapSize, mockUploadFileImage.getValidExtensions().size());

		when(mockVar.isAllowEPSImg()).thenReturn(true);
		service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
		assertNotEquals(freshMapSize, mockUploadFileImage.getValidExtensions().size());
		assertEquals(freshMapSize + 1, mockUploadFileImage.getValidExtensions().size());

		when(mockVar.isAllowSTDImg()).thenReturn(false);
		extMap = getFreshExtMap();
		when(mockUploadFileImage.getValidExtensions()).thenReturn(extMap);
		service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
		assertNotEquals(0, mockUploadFileImage.getValidExtensions().size());

	      when(service.checkIfBUAllowsEPS(mockAppSessionBean)).thenReturn(true);
			extMap = getFreshExtMap();
			when(mockUploadFileImage.getValidExtensions()).thenReturn(extMap);
			service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
			assertNotEquals(0, mockUploadFileImage.getValidExtensions().size());


			when(mockVar.isAllowEPSImg()).thenReturn(false);
			extMap = getFreshExtMap();
			when(mockUploadFileImage.getValidExtensions()).thenReturn(extMap);
			service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
			assertNotEquals(0, mockUploadFileImage.getValidExtensions().size());

			when(mockVar.isAllowSTDImg()).thenReturn(true);
			extMap = getFreshExtMap();
			when(mockUploadFileImage.getValidExtensions()).thenReturn(extMap);
			service.updateEPSExtensions(mockVar, mockUploadFileImage, mockAppSessionBean);
			assertEquals(freshMapSize + 1, mockUploadFileImage.getValidExtensions().size());


	}

	@Test
	void getUploadFileObjectTest() {
		setupObjMapUploads(true);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		assertEquals(mockUploadFileImage, service.getUploadFileObject(PageflexType.IMAGE, mockVar, mockAppSessionBean));
		assertEquals(mockUploadFileInsert, service.getUploadFileObject(PageflexType.INSERTION_GROUP, mockVar, mockAppSessionBean));
		assertEquals(mockUploadFileHostedResource, service.getUploadFileObject(PageflexType.HOSTED_RESOURCE, mockVar, mockAppSessionBean));
		assertEquals(mockUploadFileUpload, service.getUploadFileObject(PageflexType.FILE_UPLOAD, mockVar, mockAppSessionBean));
		assertEquals(mockUploadFileUpload, service.getUploadFileObject(PageflexType.FILE_UPLOAD, mockFileUploadVar, mockAppSessionBean));
		assertNull(service.getUploadFileObject(PageflexType.AREA_TEMPLATE, mockVar, mockAppSessionBean));
	}

	@Test
	void uploadFileThroughCustomPoint() {
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.AREA_TEMPLATE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		C1UXCustDocUploadVariableFileRequest request = getUploadVariableFileRequest();
		C1UXCustDocUploadVariableFileResponse sfBean = new C1UXCustDocUploadVariableFileResponse();
		setupObjMapUploads(false);
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertFalse(sfBean.isSuccess());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		sfBean = new C1UXCustDocUploadVariableFileResponse();
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.HOSTED_RESOURCE);
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertFalse(sfBean.isSuccess());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		sfBean = new C1UXCustDocUploadVariableFileResponse();
		when(mockUploadFileUpload.uploadFileC1UX(any(), any(), any(), any())).thenReturn(false);
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.FILE_UPLOAD);
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertFalse(sfBean.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		when(mockUploadFileUpload.uploadFileC1UX(any(), any(), any(), any())).thenReturn(true);
		sfBean = new C1UXCustDocUploadVariableFileResponse();
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertTrue(sfBean.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		when(mockUploadFileInsert.uploadFileC1UX(any(), any(), any(), any())).thenReturn(false);
		sfBean = new C1UXCustDocUploadVariableFileResponse();
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.INSERTION_GROUP);
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertFalse(sfBean.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		when(mockUploadFileInsert.uploadFileC1UX(any(), any(), any(), any())).thenReturn(true);
		sfBean = new C1UXCustDocUploadVariableFileResponse();
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertTrue(sfBean.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		sfBean = new C1UXCustDocUploadVariableFileResponse();
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.IMAGE);
		when(mockVar.isAllowSTDImg()).thenReturn(true);
		when(mockUploadFileImage.uploadFileC1UX(any(), any(), any(), any())).thenReturn(false);
	    service = Mockito.spy(service);
	    UploadFileC1uxBean cpBean = getMockCpBean();
	    cpBean.getErrors().add(CLASSIC_CONTEXT_PATH);
	    doReturn(cpBean).when(service).convertVariableRequestForCP(any(C1UXCustDocUploadVariableFileRequest.class), any(), any(UserInterfaceImpl.class));

		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertFalse(sfBean.isSuccess());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());
		assertEquals(CLASSIC_CONTEXT_PATH, sfBean.getMessage());

		sfBean = new C1UXCustDocUploadVariableFileResponse();
		when(mockUploadFileImage.uploadFileC1UX(any(), any(), any(), any())).thenReturn(true);
		service.uploadFileThroughCustomPoint(request, sfBean, mockVar, mockAppSessionBean, mockUI, mockOESession);
		assertTrue(sfBean.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());
	}

	private C1UXCustDocUploadVariableFileRequest getUploadVariableFileRequest() {
		C1UXCustDocUploadVariableFileRequest request = new C1UXCustDocUploadVariableFileRequest();
		request.setHdnVar("ABC");
		request.setFile(new MockMultipartFile("sourceFile.tmp", "Hello World".getBytes()));
		return request;
	}

	@Test
	void populateUploadResponseFromCP() {
		C1UXCustDocUploadVariableFileResponse sfBean = new C1UXCustDocUploadVariableFileResponse();
		sfBean.setSuccess(true);
		UploadFileC1uxBean cpBean = getMockCpBean();
		service.populateUploadResponseFromCP(sfBean, cpBean);
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		sfBean.setSuccess(false);
		service.populateUploadResponseFromCP(sfBean, cpBean);
		assertEquals(AtWinXSConstant.EMPTY_STRING, sfBean.getMessage());

		sfBean.setMessage(CLASSIC_CONTEXT_PATH);
		service.populateUploadResponseFromCP(sfBean, cpBean);
		assertEquals(CLASSIC_CONTEXT_PATH, sfBean.getMessage());

		sfBean.setMessage(AtWinXSConstant.EMPTY_STRING);
		cpBean.getErrors().add(CLASSIC_CONTEXT_PATH);
		service.populateUploadResponseFromCP(sfBean, cpBean);
		assertEquals(CLASSIC_CONTEXT_PATH, sfBean.getMessage());
	}

	@Test
	void convertVariableRequestForCP() {
		assertNotNull(service.convertVariableRequestForCP(new C1UXCustDocUploadVariableFileRequest(), mockVar, mockUI));
	}

	protected UploadFileC1uxBean getMockCpBean() {
		UploadFileC1uxBean cpBean = new UploadFileC1uxBean();
		cpBean.setFile(new MockMultipartFile("sourceFile.tmp", "Hello World".getBytes()));
		cpBean.setVar(mockVar);
		cpBean.setHdnVar("myVar");
		return cpBean;
	}

	@Test
	void getFailureDownloadMessage() {
	      service = Mockito.spy(service);
	      doReturn(SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT).when(service)
    		.getTranslation(eq(mockAppSessionBean), anyString(), anyString());
	      assertEquals(SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT, service.getFailureDownloadMessage(mockAppSessionBean));
	}

	@Test
	void getTempFileDirectory() {
		try (MockedStatic<PropertyUtil> mockUtil = Mockito.mockStatic(PropertyUtil.class)) {
		  	mockUtil.when(() -> PropertyUtil.getProperties(anyString(), anyString())).thenReturn(mockXSProperties);
		  	when(mockXSProperties.getProperty(any())).thenReturn(CLASSIC_CONTEXT_PATH);
			when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(CLASSIC_CONTEXT_PATH);
		  	assertEquals(CLASSIC_CONTEXT_PATH, service.getTempFileDirectory(mockAppSessionBean));
		}
	}

	@Test
	void getDisposition() {
		assertTrue(service.getDisposition(CLASSIC_CONTEXT_PATH).contains(CLASSIC_CONTEXT_PATH));
	}

	@Test
	void prepareDownload() throws AtWinXSException  {
		InsertUploadFileApiRequest req = new InsertUploadFileApiRequest();
	      service = Mockito.spy(service);
	      doReturn(mockCopyFilesComponent).when(service).getCopyFilesComponent(any());
	      when(mockAppSessionBean.getCurrentEnvCd()).thenReturn("6x");
	      when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
	      when(mockAppSessionBean.getCorporateNumber()).thenReturn("0000097000");
	      when(mockCopyFilesComponent.copyDSAFile(any(), any(), any(), anyInt())).thenReturn(null);
	      InsertUploadFileResponse response = service.prepareDownload(req, mockAppSessionBean);
	      assertFalse(response.isSuccess());

	      req.setFileName("myfile.jpg");
	      req.setFileId("1");
	      when(mockCopyFilesComponent.copyDSAFile(any(), any(), any(), anyInt())).thenReturn(mockCopyFilesResponse);
	      when(mockCopyFilesResponse.getStatus()).thenReturn("Fails");
	      response = service.prepareDownload(req, mockAppSessionBean);
	      assertFalse(response.isSuccess());

	      when(mockCopyFilesResponse.getStatus()).thenReturn("Success");
	      doReturn("myDirectoryPath").when(service).getTempFileDirectory(mockAppSessionBean);
	      doReturn(SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT).when(service)
    		.getTranslation(eq(mockAppSessionBean), anyString(), anyString());
	      response = service.prepareDownload(req, mockAppSessionBean);
	      assertFalse(response.isSuccess());
	}

	@Test
	void testStreamWriteTo() throws IOException {
	    	  InsertUploadFileResponse response2 = new InsertUploadFileResponse();
		      response2.setSuccess(true);
		      response2.setDeleteWhenDown(true);
		      response2.setExportedFile(mockFile);
		      when(mockFile.getPath()).thenReturn("1234567890");
	    	  response2.writeTo(mockOutputStream);
	    	  assertNotNull(response2);
	}

	@Test
	void testStreamOpenFile() {
		assertThrows(Exception.class, () -> {
  	  InsertUploadFileResponse response2 = new InsertUploadFileResponse();
	      response2.setSuccess(true);
	      response2.setExportedFile(mockFile);
	      response2.attemptToOpenStream();
		});
	}

	@Test
	void cleanFileName() {
		assertFalse(service.cleanFileName("D_filename.jpg").contains("D_"));
	}

	@Test
	void getMediaType() {
		assertEquals(MediaType.APPLICATION_PDF, service.getMediaType("amy.pdf"));
		assertNotEquals(MediaType.APPLICATION_OCTET_STREAM, service.getMediaType("amy.doc"));
		assertNotEquals(MediaType.APPLICATION_OCTET_STREAM, service.getMediaType("amy.docx"));
		assertNotEquals(MediaType.APPLICATION_OCTET_STREAM, service.getMediaType("amy.xls"));
		assertNotEquals(MediaType.APPLICATION_OCTET_STREAM, service.getMediaType("amy.xlsx"));
		assertEquals(MediaType.IMAGE_JPEG, service.getMediaType("amy.jpg"));
		assertEquals(MediaType.IMAGE_JPEG, service.getMediaType("amy.jpeg"));
		assertEquals(MediaType.IMAGE_GIF, service.getMediaType("amy.gif"));
		assertEquals(MediaType.IMAGE_PNG, service.getMediaType("amy.png"));
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, service.getMediaType("amy.tif"));
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, service.getMediaType("amy"));
	}
}


