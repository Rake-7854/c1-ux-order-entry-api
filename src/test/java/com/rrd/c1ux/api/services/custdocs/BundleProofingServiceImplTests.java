/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.custompoint.customdocs.bundle.entity.BundleComponent;
import com.rrd.custompoint.customdocs.bundle.ui.BundleUserInterfaceImpl;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.customdocs.ProofControl;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.entity.ProofTrackingAudit;
import com.rrd.custompoint.orderentry.entity.BundleItemImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.customdocs.compserv.util.CSStatusResponse;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public class BundleProofingServiceImplTests extends BaseCustomDocsServiceTests {

	public static final String FAKE_TRANSACTION_ID = "7sMCu9gmfJkXJtTHc_vbg";
	public static final String FAKE_URL = "http://www.cnn.com";

	  @InjectMocks
	  private BundleProofingServiceImpl service;

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

	  @Mock
	  protected BundleItemImpl mockBundleItem;
	  @Mock
	  protected BundleUserInterfaceImpl mockBundleUI;
	  @Mock
	  protected BundleComponent mockBundleComponent;


	  @Test
	  void getCurrentImageProofStatus() throws Exception
		{
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
		    when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
		    when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(AtWinXSConstant.CURRENT_PAGEFLEX_ENV);
		    when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
		    when(mockBundleUI.getComponentProofID(any(), any())).thenReturn(FAKE_TRANSACTION_ID);
//			when(mockBundleUI.getImageProofJobName()).thenReturn(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG);
//			when(mockBundleUI.isExternalESP()).thenReturn(false);
//			when(mockBundleUI.isPrecisionDialogue()).thenReturn(false);
		    when(mockUI.getNumberOfPages(anyString(), anyString(), anyBoolean())).thenReturn(mockProofControl);
		    when(mockProofControl.getNumPages()).thenReturn(3);
		    when(mockProofControl.getStatus()).thenReturn(CSStatusResponse.StatusCodes.Success);
		    setupMockComponentMap(true, true);
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

//			when(mockBundleUI.isExternalESP()).thenReturn(true);
//			statusResponse = service.getCurrentImageProofStatus(mockSessionContainer);
//			assertFalse(statusResponse.isSuccess());
///			assertFalse(statusResponse.isAnyReady());
//			assertFalse(statusResponse.isCompleted());
//			assertEquals(0, statusResponse.getLastProofPageNbr());
		}

	  protected void setupMockComponentMap(boolean addUI, boolean addVendorItemNumbers) {
		    Map<Integer, CustomDocumentItem> itemMap = new HashMap<>();
		    Integer compNum = Integer.valueOf(1);
		    itemMap.put(compNum, mockItem);
		    when(mockBundleUI.getConvertedCustDocItemsMap()).thenReturn(itemMap);
		    when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
		    if (addVendorItemNumbers) {
			    when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(compNum);
//		    	when(mockItem.getVendorItemNumber()).thenReturn(CUSTOMER_ITEM_NUM);
//		    	when(mockBundleComponent.getWalVendorRrdItem()).thenReturn(CUSTOMER_ITEM_NUM);
		    }
		    if (addUI) {
		    	when(mockItem.getUserInterface()).thenReturn(mockUI);
		    }
	  }

	  @Test
		void getProofLink()
				throws Exception {
			C1UXCustDocProofLinkRequest request = new C1UXCustDocProofLinkRequest();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
		    when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//		    when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		    when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(AtWinXSConstant.CURRENT_PAGEFLEX_ENV);
		    when(mockBundleUI.isAlwaysSsl()).thenReturn(true);
		    when(mockBundleUI.getProofURL(any(), any(), anyBoolean(), any(), any())).thenReturn(FAKE_URL);

//			when(mockBundleUI.getImageProofJobName()).thenReturn(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG);
//			when(mockBundleUI.isExternalESP()).thenReturn(false);
//			when(mockBundleUI.isPrecisionDialogue()).thenReturn(false);
		    when(mockBundleUI.getComponentProofID(any(), any())).thenReturn(null);
			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertFalse(linkResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, linkResponse.getProofUrl());
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, linkResponse.getMessage());


		    when(mockBundleUI.getComponentProofID(any(), any())).thenReturn(FAKE_TRANSACTION_ID);
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
			doReturn(1).when(service).createPdfProofAuditRecord(any(), any(), any(), any());
		    request.setProofType(ProofType.PDF.toString());
			linkResponse = service.getProofLink(mockSessionContainer, request);
			assertTrue(linkResponse.isSuccess());
			assertEquals(FAKE_URL, linkResponse.getProofUrl());

		}

	  @Test
		void getProofLink_pdffail()
				throws Exception {
			C1UXCustDocProofLinkRequest request = new C1UXCustDocProofLinkRequest();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
		    when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//		    when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//		    when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(AtWinXSConstant.CURRENT_PAGEFLEX_ENV);
//		    when(mockBundleUI.isAlwaysSsl()).thenReturn(true);
//		    when(mockBundleUI.getProofURL(any(), any(), anyBoolean(), any(), any())).thenReturn(FAKE_URL);
		    setupMockComponentMap(false, false);
		    request.setProofType(ProofType.PDF.toString());

		      when(mockBundleUI.getComponentProofID(ProofType.PDF, mockBundleComponent)).thenReturn(AtWinXSConstant.EMPTY_STRING);
//		      when(mockBundleUI.generateNewProof(any(), any(), any(), any(), any(), any(), any())).thenReturn(ProofType.PDF);

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

}
