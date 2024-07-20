/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  08/23/23    A Boomker       CAP-43223                   Initial
 *  09/12/23	A Boomker		CAP-42839					Added session save if new proof is generated
 *  11/13/23	A Boomker		CAP-44426					Added handling for update working proof
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocWorkingProofResponse;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.customdocs.ProofControl;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.entity.ProofTrackingAudit;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.customdocs.compserv.util.CSStatusResponse;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public class CustomDocsProofingServiceImplTests extends BaseOEServiceTest {

	public static final String FAKE_TRANSACTION_ID = "7sMCu9gmfJkXJtTHc_vbg";
	public static final String FAKE_URL = "http://www.cnn.com";

	  @InjectMocks
	  private CustomDocsProofingServiceImpl service;

	  @Mock
	  private VolatileSessionBean mockVolatileSessionBean;

	  @Mock
	  private EntityObjectMap mockEntityObjectMap;

	  @Mock
	  private ProofControl mockProofControl;

	  @Mock
	  private ProofTrackingAudit mockProofTrackingAudit;

	  @Mock
	  private CustomizationToken mockCustomizationToken;

	  @Mock
	  private OrderEntrySession mockOrderEntrySession;

	  @Mock
	  private ApplicationVolatileSession mockApplicationVolatileSession;

	  @Mock
	  private UserInterfaceImpl mockUI;

	  @Mock
	  private CustomDocumentItemImpl mockItem;

	  @Mock
	  private C1UXCustDocProofStatusResponse mockStatusResponse;
	  private C1UXCustDocProofStatusResponse statusResponse;

	  @Mock
	  private C1UXCustDocProofLinkResponse mockLinkResponse;
	  private C1UXCustDocProofLinkResponse linkResponse;

	  private C1UXCustDocProofLinkRequest linkRequest;

	  @Test
	  void getCurrentImageProofStatus() throws Exception
		{
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		    when(mockItem.getUserInterface()).thenReturn(mockUI);
		    when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(AtWinXSConstant.CURRENT_PAGEFLEX_ENV);
		    when(mockUI.getProofID(any())).thenReturn(FAKE_TRANSACTION_ID);
			when(mockUI.getImageProofJobName()).thenReturn(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG);
			when(mockUI.isExternalESP()).thenReturn(false);
			when(mockUI.isPrecisionDialogue()).thenReturn(false);
		    when(mockUI.getNumberOfPages(anyString(), anyString(), anyBoolean())).thenReturn(mockProofControl);
		    when(mockProofControl.getNumPages()).thenReturn(3);
		    when(mockProofControl.getStatus()).thenReturn(CSStatusResponse.StatusCodes.Success);

			statusResponse = service.getCurrentImageProofStatus(mockSessionContainer);
			assertTrue(statusResponse.isSuccess());
			assertTrue(statusResponse.isAnyReady());
			assertTrue(statusResponse.isCompleted());
			assertEquals(3, statusResponse.getLastProofPageNbr());

		    when(mockProofControl.getStatus()).thenReturn(CSStatusResponse.StatusCodes.Processing);

			statusResponse = service.getCurrentImageProofStatus(mockSessionContainer);
			assertTrue(statusResponse.isSuccess());
			assertTrue(statusResponse.isAnyReady());
			assertFalse(statusResponse.isCompleted());
			assertEquals(3, statusResponse.getLastProofPageNbr());

		    when(mockProofControl.getStatus()).thenReturn(CSStatusResponse.StatusCodes.Waiting);

			statusResponse = service.getCurrentImageProofStatus(mockSessionContainer);
			assertTrue(statusResponse.isSuccess());
			assertFalse(statusResponse.isAnyReady());
			assertFalse(statusResponse.isCompleted());
			assertEquals(3, statusResponse.getLastProofPageNbr());

		    when(mockProofControl.getStatus()).thenReturn(CSStatusResponse.StatusCodes.Failure);

			statusResponse = service.getCurrentImageProofStatus(mockSessionContainer);
			assertFalse(statusResponse.isSuccess());
			assertFalse(statusResponse.isAnyReady());
			assertFalse(statusResponse.isCompleted());
			assertEquals(0, statusResponse.getLastProofPageNbr());

			when(mockUI.isExternalESP()).thenReturn(true);
			statusResponse = service.getCurrentImageProofStatus(mockSessionContainer);
			assertFalse(statusResponse.isSuccess());
			assertFalse(statusResponse.isAnyReady());
			assertFalse(statusResponse.isCompleted());
			assertEquals(0, statusResponse.getLastProofPageNbr());
		}

		@Test
		void validateImageProofType()
		{
		      service = Mockito.spy(service);
		      doReturn(ProofType.BLAZE).when(service).getProofType(mockUI);
		      assertFalse(service.validateImageProofType(mockUI, false, null));
		      assertFalse(service.validateImageProofType(mockUI, true, null));
		      assertFalse(service.validateImageProofType(mockUI, false, ProofType.BLAZE));
		      assertFalse(service.validateImageProofType(mockUI, true, ProofType.BLAZE));

		      doReturn(ProofType.WORKING).when(service).getProofType(mockUI);
		      assertFalse(service.validateImageProofType(mockUI, false, null));
		      assertTrue(service.validateImageProofType(mockUI, true, null));
		      assertFalse(service.validateImageProofType(mockUI, false, ProofType.WORKING));
		      assertTrue(service.validateImageProofType(mockUI, true, ProofType.WORKING));

		      doReturn(ProofType.IMAGE).when(service).getProofType(mockUI);
		      assertTrue(service.validateImageProofType(mockUI, false, null));
		      assertTrue(service.validateImageProofType(mockUI, true, null));
		      assertTrue(service.validateImageProofType(mockUI, false, ProofType.IMAGE));
		      assertTrue(service.validateImageProofType(mockUI, true, ProofType.IMAGE));
		}

		@Test
		void getProofLink()
				throws Exception {
			C1UXCustDocProofLinkRequest request = new C1UXCustDocProofLinkRequest();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		    when(mockItem.getUserInterface()).thenReturn(mockUI);
//		    when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		    when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(AtWinXSConstant.CURRENT_PAGEFLEX_ENV);
		    when(mockUI.isAlwaysSsl()).thenReturn(true);
		    when(mockUI.getProofURL(any(), any(), anyBoolean(), any(), any())).thenReturn(FAKE_URL);

//			when(mockUI.getImageProofJobName()).thenReturn(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG);
//			when(mockUI.isExternalESP()).thenReturn(false);
//			when(mockUI.isPrecisionDialogue()).thenReturn(false);
		    when(mockUI.getProofID(any())).thenReturn(FAKE_TRANSACTION_ID);

		    // initial proof type of null or empty will default to image
 			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertTrue(linkResponse.isSuccess());
			assertEquals(FAKE_URL, linkResponse.getProofUrl());

			request.setProofPageNbr(25);
 			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertTrue(linkResponse.isSuccess());
			assertEquals(FAKE_URL, linkResponse.getProofUrl());


		    request.setProofType(ProofType.EMAIL.toString());
			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertFalse(linkResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, linkResponse.getProofUrl());
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, linkResponse.getMessage());

		    service = Mockito.spy(service);
			doReturn(1).when(service).createPdfProofAuditRecord(mockUI, FAKE_TRANSACTION_ID, mockAppSessionBean, mockOEOrderSession);
		    request.setProofType(ProofType.PDF.toString());
			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertTrue(linkResponse.isSuccess());
			assertEquals(FAKE_URL, linkResponse.getProofUrl());

		    when(mockUI.getProofID(any())).thenReturn(null);
			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertFalse(linkResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, linkResponse.getProofUrl());
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, linkResponse.getMessage());



		}

		@Test
 		void createPdfProofAuditRecord() throws AtWinXSException
		{
//			ProofTrackingAudit proofTrackingAudit = new ProofTrackingAuditImpl();
		    service = Mockito.spy(service);
			doReturn(mockProofTrackingAudit).when(service).createProofTrackingAudit(mockAppSessionBean);
			int fakeTrackingID = 345;
			when(mockProofTrackingAudit.getTrackingID()).thenReturn(fakeTrackingID);
			int id = service.createPdfProofAuditRecord(mockUI, FAKE_TRANSACTION_ID, mockAppSessionBean, mockOEOrderSession);
			assertEquals(id, fakeTrackingID);

			id = service.createPdfProofAuditRecord(null, FAKE_TRANSACTION_ID, mockAppSessionBean, null);
			assertEquals(id, fakeTrackingID);

			doReturn(null).when(service).createProofTrackingAudit(mockAppSessionBean);
			id = service.createPdfProofAuditRecord(null, FAKE_TRANSACTION_ID, mockAppSessionBean, null);
			assertEquals(id, -1);

		}

		@Test
		void getWorkingProof() throws Exception {
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		    when(mockItem.getUserInterface()).thenReturn(mockUI);
		    Map<String, String> uiRequest = new HashMap<>();
		    uiRequest.put(ICustomDocsAdminConstants.HDN_UI_STEP_PAGE_NUM, "1");
		    C1UXCustDocWorkingProofResponse response = service.getWorkingProof(mockSessionContainer, uiRequest);
		    assertTrue(response.isSuccess());

		    service = Mockito.spy(service);
		    doReturn(true).when(service).loadWorkingProof(any(), any(), any(), any());
		    response = service.getWorkingProof(mockSessionContainer, uiRequest);
		    assertTrue(response.isSuccess());

		    doThrow(new NullPointerException("myfakeError")).when(service)
		     		.loadWorkingProof(any(), any(), any(), any());
		    response = service.getWorkingProof(mockSessionContainer, uiRequest);
			assertTrue(response.isSuccess());
		}
}
