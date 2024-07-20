/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/13/24    A Boomker       CAP-46490                   Initial
 * 	03/29/24	A Boomker		CAP-46493/CAP-46494	fixes for navigation
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
 *  04/08/24	A Boomker		CAP-48464					Fixes for saving
 *  06/03/24	A Boomker		CAP-46501					Add testing for alternate profile handling
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.custompoint.customdocs.bundle.entity.BundleComponent;
import com.rrd.custompoint.customdocs.bundle.ui.BundleUserInterfaceImpl;
import com.rrd.custompoint.orderentry.bundle.BundleUserInterface;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofView;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.UIEvent;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocProofFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.BundleItem;
import com.rrd.custompoint.orderentry.entity.BundleItemImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

class BundleServiceImplTests extends BaseCustomDocsServiceTests {


	  private static final String TEST_USER = "BUNDLEAMY";
	@InjectMocks
	  protected BundleServiceImpl bundleService;
	  @Mock
	  protected BundleItemImpl mockBundleItem;
	  @Mock
	  protected BundleUserInterfaceImpl mockBundleUI;
	  @Mock
	  protected BundleComponent mockBundleComponent;

	  protected void setupMockComponentMap(boolean addUI, boolean addVendorItemNumbers) {
		    Map<Integer, CustomDocumentItem> itemMap = new HashMap<>();
		    Integer compNum = Integer.valueOf(1);
		    itemMap.put(compNum, mockItem);
		    when(mockBundleUI.getConvertedCustDocItemsMap()).thenReturn(itemMap);
		    when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(compNum);
		    if (addVendorItemNumbers) {
			    when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
		    	when(mockItem.getVendorItemNumber()).thenReturn(CUSTOMER_ITEM_NUM);
		    	when(mockBundleComponent.getWalVendorRrdItem()).thenReturn(CUSTOMER_ITEM_NUM);
		    }
		    if (addUI) {
		    	when(mockItem.getUserInterface()).thenReturn(mockUI);
		    }
	  }


	@Test
	void setProofFields() throws AtWinXSException {
		C1UXCustDocPageBean pageResponse = new C1UXCustDocPageBean();
		bundleService.setProofFields(mockBundleUI, pageResponse, 2, mockAppSessionBean);
		assertNull(pageResponse.getPagesToProof());
		when(mockBundleUI.getComponentProofID(any(), any())).thenReturn(FAKE_UPLOAD_LIST_VALUE);
//		when(mockUI.getProofID(any(ProofType.class))).thenReturn(CUSTOMER_ITEM_NUM);
		when(mockBundleUI.getProofURL(any(ProofType.class), any(), anyBoolean(), anyString(), anyString())).thenReturn(CLASSIC_CONTEXT_PATH);
		setupMockComponentMap(true, false);
	    pageResponse = new C1UXCustDocPageBean();
		bundleService.setProofFields(mockBundleUI, pageResponse, 99998, mockAppSessionBean);
		assertNull(pageResponse.getPagesToProof());

		HashSet<Integer> pagesToProof = new HashSet<>();
		pagesToProof.add(25);
		pagesToProof.add(5);
		pagesToProof.add(35);
		when(mockUI.getPagesToProof()).thenReturn(pagesToProof);
	    pageResponse = new C1UXCustDocPageBean();
		bundleService.setProofFields(mockBundleUI, pageResponse, 99998, mockAppSessionBean);
		assertNotNull(pageResponse.getPagesToProof());
		assertEquals(3,pageResponse.getPagesToProof().size());

//		when(mockUI.getProofID(any(ProofType.class))).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    pageResponse = new C1UXCustDocPageBean();
		bundleService.setProofFields(mockBundleUI, pageResponse, 99998, mockAppSessionBean);
		assertNotNull(pageResponse.getPagesToProof());
		assertEquals(3,pageResponse.getPagesToProof().size());

	}

/*	@Test
	void generateNextProof() throws Exception
	{
		when(mockBundleUI.getProofID(ProofType.IMAGE)).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockBundleUI.getProofID(ProofType.XERT)).thenReturn(DEFAULT_VIEW);
		when(mockBundleUI.getProofID(ProofType.PRECISIONDIALOGUE)).thenReturn(CUSTOMER_ITEM_NUM);
		when(mockBundleUI.getImageProofJobName()).thenReturn(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG);
		when(mockBundleUI.isExternalESP()).thenReturn(true);

		proofBean = new CustDocProofFormBean();
		bundleService.generateNextProof(mockBundleUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(DEFAULT_VIEW, proofBean.getHiddenTransactionID());

		when(mockBundleUI.isExternalESP()).thenReturn(false);
		when(mockBundleUI.isPrecisionDialogue()).thenReturn(true);

		proofBean = new CustDocProofFormBean();
		bundleService.generateNextProof(mockBundleUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(CUSTOMER_ITEM_NUM, proofBean.getHiddenTransactionID());

		when(mockBundleUI.isPrecisionDialogue()).thenReturn(false);
		when(mockBundleUI.generateNewProof(eq(ProofType.IMAGE), eq(mockAppSessionBean),
				eq(mockVolatileSessionBean), anyList())).thenReturn(false);
		bundleService.generateNextProof(mockBundleUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(AtWinXSConstant.EMPTY_STRING, proofBean.getHiddenTransactionID());

		when(mockBundleUI.getProofID(ProofType.IMAGE)).thenReturn(ITEM_DESC);
		proofBean = new CustDocProofFormBean();
		bundleService.generateNextProof(mockBundleUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(ITEM_DESC, proofBean.getHiddenTransactionID());

		String error = "blew up";
		when(mockBundleUI.getProofID(ProofType.IMAGE)).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockBundleUI.generateNewProof(eq(ProofType.IMAGE), eq(mockAppSessionBean),
				eq(mockVolatileSessionBean), anyList()))
				.thenThrow(new AtWinXSException(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING + error, "anyclass"));
		proofBean = new CustDocProofFormBean();
		bundleService.generateNextProof(mockBundleUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertTrue(proofBean.isHardStopFailure());
		assertEquals(error, proofBean.getError());

	} */

	@Test
	void finalizeProofModelDetails() throws Exception
	{
//		  setUpModuleSession();
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      bundleService = Mockito.spy(bundleService);
//	      doNothing().when(bundleService).generateNextProof(eq(mockBundleUI), any(CustDocProofFormBean.class), eq(mockAppSessionBean), eq(mockVolatileSessionBean));
	      doReturn(mockUIInterface).when(bundleService).getCDInterface(mockAppSessionBean);
	      when(mockBundleUI.getUiKey()).thenReturn(mockUIKey);
	      when(mockUIKey.getVOKey()).thenReturn(mockUIVersionKey);
	      when(mockUIInterface.getTempUIVersionVO(any())).thenReturn(mockUIVersionVO);
	      when(mockUIVersionVO.isXertCompositionEnabled()).thenReturn(false);
	      when(mockUIVersionVO.isPrecisionDialogueEnabled()).thenReturn(false);
	      when(mockBundleUI.isExternalESP()).thenReturn(false);
	      when(mockBundleUI.isVariablePage()).thenReturn(false);
//	      when(mockBundleUI.renderSpecialtyProofButtons(mockBundleItem)).thenReturn(CLASSIC_CONTEXT_PATH);
	      when(mockAppSessionBean.getEmailAddress()).thenReturn("dapres@whitehouse.gov");
//	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      when(mockBundleUI.getDefaultProofView()).thenReturn(ProofView.None);
		CustDocProofFormBean formBean = new CustDocProofFormBean();
		bundleService.finalizeProofModelDetails(mockBundleItem, mockBundleUI, formBean, mockAppSessionBean,
				mockVolatileSessionBean, mockOESession);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, formBean.getTermsConditionsText());
		assertEquals(UserInterface.ProofView.Medium.toString(), formBean.getProofView());
	}

	@Test
	void initializeUIOnly() throws Exception {
		setUpModuleSession();
		uiRequest = new HashMap<>();
	    bundleService = Mockito.spy(bundleService);
		when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	    doReturn(mockBundleItem).when(bundleService).initializeItem(any(), any(), any(), any(), any(), any(), any());
		doNothing().when(bundleService).saveFullSessionInfo(any(), anyInt(), anyInt());
	    doReturn("Initialized Successfully!").when(bundleService).getCombinedMessage(any());
		List<Page> pages = new ArrayList<>();
		pages.add(mockPage2);

		baseResponse = bundleService.initializeUIOnly(mockSessionContainer, uiRequest);
		assertTrue(baseResponse.isSuccess());

	    doThrow(new AtWinXSException("message", "classname")).when(bundleService).saveFullSessionInfo(any(), anyInt(), anyInt());
		doReturn(GENERIC_ERROR_MSG).when(bundleService).getTranslation(any(Locale.class), any(), anyString());
		baseResponse = bundleService.initializeUIOnly(mockSessionContainer, uiRequest);
		assertFalse(baseResponse.isSuccess());
	}

	@Test
	void initializeItem() throws Exception {
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
		      bundleService = Mockito.spy(bundleService);
		      doReturn(mockBundleItem).when(bundleService).createAndInitItem(any(), any(), any(), any(), any(), any());
		      when(mockBundleUI.getProfileSelector()).thenReturn(mockUserProfileSelector);
		      when(mockUserProfileSelector.isShown()).thenReturn(true);
		      when(mockAppSessionBean.getProfileNumber()).thenReturn(250);
		      when(mockBundleUI.isSkipDynamicDataCalls()).thenReturn(false);
		      CustDocUIFormBean uiBean = new CustDocUIFormBean();
		      bundleService.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

		      when(mockUserProfileSelector.isShown()).thenReturn(false);
		      uiBean = new CustDocUIFormBean();
		      bundleService.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

		      when(mockBundleUI.getProfileSelector()).thenReturn(null);
		      uiBean = new CustDocUIFormBean();
		      bundleService.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

	}

	@Test
	void performPageSubmitAction() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
	      bundleService = Mockito.spy(bundleService);
	      doNothing().when(bundleService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockBundleItem), any());
	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      uiRequest = new HashMap<>();
	      uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NEXT.toString());
	      C1UXCustDocBaseResponse baseResponse = bundleService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertTrue(baseResponse instanceof C1UXCustDocBaseResponse);

	      doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(bundleService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockBundleItem), any());
			doReturn(GENERIC_ERROR_MSG).when(bundleService).getTranslation(any(Locale.class), any(), anyString());
	      baseResponse = bundleService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new NullPointerException("I blew up!")).when(bundleService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockBundleItem), any());
	      baseResponse = bundleService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.STAY.toString());
	      doNothing().when(bundleService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockBundleItem), any());
	      doNothing().when(bundleService).convertToC1UXObject(eq(mockSessionContainer), eq(mockBundleItem), any(CustDocUIFormBean.class), any());
	      baseResponse = bundleService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertTrue(baseResponse instanceof C1UXCustDocPageBean);
	}

	@Test
	void handleUnsupportedEvents() {
		// CAP-46524
		//assertTrue(bundleService.handleUnsupportedEvents(UIEvent.REFRESH));
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.APPLY_COLOR_CHANGE)); // applying color to all of UI from font/color set
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.PROFILE_SEARCH));
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.HISTORY_SEARCH));
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.PROFILE_SELECT));
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.ALTERNATE_PROFILE_SELECT));
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.KEY_PROFILE_SELECT));

		assertFalse(bundleService.handleUnsupportedEvents(UIEvent.STAY));
		assertTrue(bundleService.handleUnsupportedEvents(UIEvent.HISTORY_SELECT));
	}

	@Test
	void handleLeavingEvents() throws Exception {
		  setUpModuleSession();
	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);

	      bundleService = Mockito.spy(bundleService);
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();
	      doNothing().when(bundleService).resetUIAndItem(any(), any());
	      doNothing().when(bundleService).saveAllUpdatedSessions(any(), any(), any(), anyInt());
	      doNothing().when(bundleService).performCancelOrCancelOrder(eq(mockOESession), eq(mockBundleUI), eq(mockBundleItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any(C1UXCustDocPageBean.class));
	      pageResponse = new C1UXCustDocPageBean();

	      assertTrue(bundleService.handleLeavingEvents(UIEvent.CANCEL_ORDER, mockBundleItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      pageResponse = new C1UXCustDocPageBean();

	      assertTrue(bundleService.handleLeavingEvents(UIEvent.CANCEL, mockBundleItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      when(mockBundleUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      when(mockBundleUI.saveMasterBundleItem(any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt()))
	      	.thenReturn(mockOrderLineVOKey);

	      pageResponse = new C1UXCustDocPageBean();
	      assertTrue(bundleService.handleLeavingEvents(UIEvent.SAVE_AND_EXIT, mockBundleItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	}

	@Test
	void navigateForward() throws Exception
	{
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
	      bundleService = Mockito.spy(bundleService);
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();

	      doNothing().when(bundleService).readSubmittedData(any(UIEvent.class), eq(mockUIBean), eq(mockBundleUI), eq(uiRequest), eq(errorStrings));
	      pageResponse = new C1UXCustDocPageBean();
	      doReturn(false).when(bundleService).isStaySituation(any(UIEvent.class), eq(mockBundleUI), eq(errorStrings));
	      when(mockBundleUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      when(mockBundleUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(1);
	      doNothing().when(bundleService).determineNavigatePages(eq(uiRequest), any(UIEvent.class), anyInt(), anyInt(), eq(pageResponse));
	      doNothing().when(bundleService).showProofErrors(errorStrings, mockUIBean);
	      when(mockUIBean.getError()).thenReturn(GENERIC_ERROR_MSG);

	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockBundleItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockBundleUI.getCurrentPageC1UX(uiRequest)).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockBundleItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockBundleUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(Integer.MAX_VALUE);
	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockBundleItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockBundleUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      pageResponse = new C1UXCustDocPageBean();
	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockBundleItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockUIBean.getError()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      when(mockBundleUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(UserInterface.NEXT_PAGE_NUMBER_EXIT);
	      pageResponse = new C1UXCustDocPageBean();
	      doNothing().when(bundleService).handleExitNonPF(anyInt(), eq(mockSessionContainer), eq(mockBundleItem), eq(errorStrings), eq(pageResponse));
	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockBundleItem, pageResponse, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      doReturn(true).when(bundleService).isStaySituation(any(UIEvent.class), eq(mockBundleUI), eq(errorStrings));
	      pageResponse = new C1UXCustDocPageBean();
	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockBundleItem, pageResponse, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

//	      doNothing().when(bundleService).performCancelOrCancelOrder(any(), any(UserInterface.class), any(), any(UIEvent.class), eq(mockSessionContainer),
//	    		  any(C1UXCustDocPageBean.class));

	      doReturn(false).when(bundleService).isStaySituation(any(UIEvent.class), eq(mockBundleUI), eq(errorStrings));
	      doReturn(45).when(bundleService).navigateProofs(any(), any(), any(), any(), any(), anyInt(), any());
	    when(mockBundleUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
	      pageResponse = new C1UXCustDocPageBean();
	      bundleService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NEXT, mockBundleItem, pageResponse, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	}

	@Test
	void handleExitNonPF() throws Exception {
		bundleService = Mockito.spy(bundleService);
		doNothing().when(bundleService).doAddToCart(any(), any(), any());
		errorStrings = new ArrayList<>();
		bundleService.handleExitNonPF(DEVTEST_SITE_ID, mockSessionContainer, mockBundleItem, errorStrings, pageResponse);
		assertTrue(errorStrings.isEmpty());
	}

	@Test
	void determineNavigatePages() {
		Map<String, String> uiRequest = new HashMap<>();
		pageResponse = new C1UXCustDocPageBean();
		bundleService.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 5, 5, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		uiRequest.put("navPage", "5");
		bundleService.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 2, 5, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		uiRequest.put("navPage", "5");
		bundleService.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 2, 3, pageResponse);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());
	}

	@Test
	void performCancelOrCancelOrder() throws Exception
		{
	    setupBaseMockSessions();
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	      when(mockBundleItem.getOrderID()).thenReturn(256);
	        bundleService = Mockito.spy(bundleService);

	        doReturn(RouteConstants.CART_ENTRY_ROUTING_URL).when(bundleService).getCancelURL(mockBundleItem, mockBundleUI, mockSessionContainer);
	        doNothing().when(bundleService).setupDirectionCancel(any(), any(), any(), any(), any(), any());
	        doNothing().when(bundleService).resetUIAndItem(any(), any());

	        pageResponse = new C1UXCustDocPageBean();
	        bundleService.performCancelOrCancelOrder(mockOESession, mockBundleUI, mockBundleItem,
	        		UserInterface.UIEvent.CANCEL_ORDER, mockSessionContainer, pageResponse);
	        assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, pageResponse.getRedirectRouting());

	        pageResponse = new C1UXCustDocPageBean();
	        bundleService.performCancelOrCancelOrder(mockOESession, mockBundleUI, mockBundleItem,
	        		UserInterface.UIEvent.CANCEL, mockSessionContainer, pageResponse);
	        assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, pageResponse.getRedirectRouting());
		}

	@Test
	void addToCart() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		setUpModuleSessionNoBase();
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	        bundleService = Mockito.spy(bundleService);
	      baseResponse = bundleService.addToCart(mockSessionContainer);
	      assertNotNull(baseResponse);
		}

	@Test
	void doAddToCart() throws Exception {
	      setUpModuleSession();
//	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
//	      when(mockBundleUI.getOrderDeliveryOptionCD()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
	      when(mockBundleUI.getEntryPoint()).thenReturn(UserInterface.EntryPoint.CATALOG_EXPRESS);
//	      when(mockBundleUI.isTryUI()).thenReturn(false);
//	      when(mockBundleUI.isFieldsEditable()).thenReturn(true);
	      errorStrings = new ArrayList<>();
	      when(mockBundleUI.saveMasterBundleItem(any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt()))
	      		.thenReturn(mockOrderLineVOKey);
	      when(mockBundleUI.getCurrentPageC1UX(anyMap())).thenReturn(247);
	      when(mockOrderLineVOKey.getLineNum()).thenReturn(22);
//	      when(mockBundleItem.saveOrderLine(any(), any(), any(), anyBoolean(), any(), any(), anyInt(), any(), any()))
//	    		  .thenReturn(mockOrderLineVOKey);
			when(mockOEOrderSession.getCurrentCampaignUserInterface()).thenReturn(mockCampaignUI);
			when(mockBundleUI.getEntryPoint()).thenReturn(UserInterface.EntryPoint.CART_ADD_FROM_STUB);
	        bundleService = Mockito.spy(bundleService);
			doNothing().when(bundleService).saveFullSessionInfo(any(), anyInt(), anyInt());
//			when(mockBundleItem.isInNewKit()).thenReturn(false);

//			when(mockBundleItem.getOrderLineNumber()).thenReturn(25);
			baseResponse = new C1UXCustDocBaseResponse();
			bundleService.doAddToCart(mockSessionContainer, mockBundleItem, baseResponse);
			assertTrue(baseResponse.isSuccess());

			when(mockOrderLineVOKey.getLineNum()).thenReturn(-1);
			baseResponse = new C1UXCustDocBaseResponse();
			bundleService.doAddToCart(mockSessionContainer, mockBundleItem, baseResponse);
			assertFalse(baseResponse.isSuccess());

		}

	// CAP-42298
	@Test
	void cancelAction() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      bundleService = Mockito.spy(bundleService);
	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      doNothing().when(bundleService).performCancelOrCancelOrder(eq(mockOESession), eq(mockBundleUI), eq(mockBundleItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      baseResponse = bundleService.cancelAction(mockSessionContainer, false);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = bundleService.cancelAction(mockSessionContainer, true);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new AtWinXSException("myError", "Aclassname")).when(bundleService).performCancelOrCancelOrder(eq(mockOESession), eq(mockBundleUI), eq(mockBundleItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      doReturn(GENERIC_ERROR_MSG).when(bundleService).getTranslation(any(Locale.class), any(), anyString());

	      baseResponse = bundleService.cancelAction(mockSessionContainer, false);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = bundleService.cancelAction(mockSessionContainer, true);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new NullPointerException("I blew up!")).when(bundleService).performCancelOrCancelOrder(eq(mockOESession), eq(mockBundleUI), eq(mockBundleItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      baseResponse = bundleService.cancelAction(mockSessionContainer, false);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = bundleService.cancelAction(mockSessionContainer, true);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	}

	@Test
	void buildCustDocUIFormBean() throws AtWinXSException {
		bundleService = Mockito.spy(bundleService);
		when(mockBundleUI.getRefreshListTriggerVariable()).thenReturn(null);
		when(mockVolatileSessionBean.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.ORDER_SCENARIO_NUMBER_DEFAULT_VALUE);
		when(mockPage.getPageNumber()).thenReturn(2);
		doNothing().when(bundleService).setBlankFields(any());
		doNothing().when(bundleService).setFieldsNoLogic(any(), any(), any(), any());
		doReturn(true).when(bundleService).getAllowFailedProofs();
		when(mockBundleUI.isTryUI()).thenReturn(false);
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		bundleService.buildCustDocUIFormBean(mockBundleUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertEquals(Boolean.FALSE.toString(), formBean.getOrderFromFile());

		when(mockVolatileSessionBean.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE);
		formBean = new CustDocUIFormBean();
		bundleService.buildCustDocUIFormBean(mockBundleUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertEquals(Boolean.TRUE.toString(), formBean.getOrderFromFile());
		assertTrue(formBean.isAllowFailedProof());

		doReturn(false).when(bundleService).getAllowFailedProofs();
		formBean = new CustDocUIFormBean();
		bundleService.buildCustDocUIFormBean(mockBundleUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(formBean.isAllowFailedProof());

		when(mockPage.getPageNumber()).thenReturn(99998);
		when(mockBundleUI.isTryUI()).thenReturn(true);
		formBean = new CustDocUIFormBean();
		bundleService.buildCustDocUIFormBean(mockBundleUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertTrue(formBean.isAllowFailedProof());
	}

	@Test
	void getNewRequestLink() {
		bundleService = Mockito.spy(bundleService);
//		doReturn(false).when(bundleService).isVarValueUploadOrSearch(any());
		when(mockVar.getListValue()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		assertEquals(AtWinXSConstant.EMPTY_STRING, bundleService.getNewRequestLink(mockVar, mockAppSessionBean));

		when(mockVar.getListValue()).thenReturn(CLASSIC_CONTEXT_PATH);
		assertEquals(AtWinXSConstant.EMPTY_STRING, bundleService.getNewRequestLink(mockVar, mockAppSessionBean));

		when(mockVar.getListValue()).thenReturn("U12345^UPLOAD: MyFile.pdf");
		doReturn(mockUploadFileUpload).when(bundleService).getUploadFileObject(any(), any(), any());
		when(mockUploadFileUpload.generateGetAFileLinkURLC1UX(anyInt())).thenReturn(GENERIC_SAVE_FAILED_ERR_ENGLISH);
		assertEquals(GENERIC_SAVE_FAILED_ERR_ENGLISH, bundleService.getNewRequestLink(mockVar, mockAppSessionBean));
	}

	protected void setupObjMapItem() {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(BundleItem.class, mockCustomizationToken)).thenReturn(mockBundleItem);
	}

	@Test
	void createAndInitItem() throws AtWinXSException {
		setupObjMapItem();
		assertEquals(mockBundleItem, bundleService.createAndInitItem(uiRequest, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings));
	}

	@Test
	void confirmProofSuccess() {
		C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
		bundleService.confirmProofSuccess(formBean, null, false);
		assertFalse(formBean.isSuccess());

		bundleService.confirmProofSuccess(formBean, null, true);
		assertTrue(formBean.isSuccess());

		bundleService.confirmProofSuccess(formBean, CUSTOMER_ITEM_NUM, true);
		assertTrue(formBean.isSuccess());
	}

	@Test
	void doneProofingAllComponents() {
		assertTrue(bundleService.doneProofingAllComponents(mockBundleUI, false));
		when(mockBundleUI.getConvertedCustDocItemsMap()).thenReturn(new HashMap<>());
		when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
		assertFalse(bundleService.doneProofingAllComponents(mockBundleUI, true));
		setupMockComponentMap(false, false);
		assertTrue(bundleService.doneProofingAllComponents(mockBundleUI, true));
	}

	@Test
	void generateComponentProof_finishedProofing() throws Exception {
//		  setUpModuleSessionNoBase();
//			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
//			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      bundleService = Mockito.spy(bundleService);
//	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());

	      setupMockComponentMap(false, true);
	      doReturn(ProofType.IMAGE).when(bundleService).getProofType(any());
	      when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
	      when(mockBundleUI.getComponentProofID(ProofType.IMAGE, mockBundleComponent)).thenReturn(CLASSIC_CONTEXT_PATH);
			C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
			bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
			assertTrue(formBean.isSuccess());

		  when(mockBundleUI.getComponentProofID(ProofType.IMAGE, mockBundleComponent)).thenReturn(AtWinXSConstant.EMPTY_STRING);
		  doReturn(CLASSIC_CONTEXT_PATH).when(bundleService).generateNewComponentProofID(any(), any(), any());
		  formBean = new C1UXCustDocBaseResponse();
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertTrue(formBean.isSuccess());
	}

	@Test
	void generateComponentProof_notfinishedProofing() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//	      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
	      bundleService = Mockito.spy(bundleService);
//	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

	      setupMockComponentMap(false, true);
	      doReturn(ProofType.IMAGE).when(bundleService).getProofType(any());
	      doReturn(AtWinXSConstant.EMPTY_STRING).when(bundleService).getCurrentBundleComponentProofID(mockBundleUI);
	      when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
//	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
	      when(mockBundleUI.getComponentProofID(ProofType.IMAGE, mockBundleComponent)).thenReturn(AtWinXSConstant.EMPTY_STRING);

	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(0);
//	      when(mockBundleUI.getProofSeqFromRequest()).thenReturn(0);
	      when(mockBundleUI.generateNewProof(any(), any(), any(), any(), any(), any(), any())).thenReturn(ProofType.IMAGE);
	      C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertFalse(formBean.isSuccess());

	      when(mockBundleUI.generateNewProof(any(), any(), any(), any(), any(), any(), any())).thenThrow(
	    		  makeMsgException(true));
	      formBean = new C1UXCustDocBaseResponse();
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertTrue(formBean.isSuccess());
		   assertEquals(BundleServiceImpl.ROUTING_WAIT, formBean.getRedirectRouting());
	}

	@Test
	void generateComponentProof_notfinishedProofing2() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//	      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
	      bundleService = Mockito.spy(bundleService);
//	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());

	      setupMockComponentMap(true, true);
	      doReturn(ProofType.IMAGE).when(bundleService).getProofType(any());
	      when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
	      when(mockBundleUI.getComponentProofID(ProofType.IMAGE, mockBundleComponent)).thenReturn(null);

	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(0);
	      when(mockBundleUI.getProofSeqFromRequest()).thenReturn(0);

		      C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
		      when(mockBundleUI.generateNewProof(any(), any(), any(), any(), any(), any(), any())).thenThrow(new AtWinXSException("npe", "anyclass"));
			  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
			  assertFalse(formBean.isSuccess());
			  assertFalse(formBean.isHardStopFailure());
	}

	@Test
	void generateComponentProof_notfinishedProofing3() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//	      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
	      bundleService = Mockito.spy(bundleService);
//	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());

	      setupMockComponentMap(true, true);
	      doReturn(ProofType.IMAGE).when(bundleService).getProofType(any());
	      when(mockBundleUI.getCurrentBundleComponent()).thenReturn(mockBundleComponent);
	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
	      when(mockBundleUI.getComponentProofID(ProofType.IMAGE, mockBundleComponent)).thenReturn(null);

	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(0);
	      when(mockBundleUI.getProofSeqFromRequest()).thenReturn(0);
			AtWinXSMsgException e = makeMsgException(false);
			e.getMsg().setErrGeneralMsg("FATALFaking this");

		      C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
	      when(mockBundleUI.generateNewProof(any(), any(), any(), any(), any(), any(), any())).thenThrow(e);
	      when(mockBundleUI.isEceHardStop()).thenReturn(false);
	      when(mockBundleUI.isDlPagerHardStop()).thenReturn(false);
	      formBean = new C1UXCustDocBaseResponse();
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertFalse(formBean.isSuccess());
		  assertTrue(formBean.isHardStopFailure());

	      when(mockBundleUI.isDlPagerHardStop()).thenReturn(true);
		  formBean = new C1UXCustDocBaseResponse();
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertFalse(formBean.isSuccess());
		  assertTrue(formBean.isHardStopFailure());

	      when(mockBundleUI.isEceHardStop()).thenReturn(true);
		  formBean = new C1UXCustDocBaseResponse();
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertFalse(formBean.isSuccess());
		  assertTrue(formBean.isHardStopFailure());

		  formBean = new C1UXCustDocBaseResponse();
	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(3);
		  bundleService.generateComponentProof(mockBundleUI, formBean, mockSessionContainer);
		  assertFalse(formBean.isSuccess());
		  assertFalse(formBean.isHardStopFailure());

	}

	@Test
	void generateNextProof() throws AtWinXSException {
		bundleService.generateNextProof(mockBundleUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertNull(null);
	}

	AtWinXSMsgException makeMsgException(boolean preprocessing) {
		Message msg = new Message();
		msg.setErrGeneralMsg(preprocessing ? "PREPROCESSING" : BLANK_NOT_ALLOWED_ERR_ENGLISH);
		return new AtWinXSMsgException(msg, CURRENT_CART_ERR_ENGLISH);
	}

	@Test
	void handeProofMsgException() throws AtWinXSMsgException {
			C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
			AtWinXSMsgException e = makeMsgException(false);
			e.getMsg().setErrGeneralMsg("FATALFaking this");
		      when(mockBundleUI.isEceHardStop()).thenReturn(false);
		      when(mockBundleUI.isDlPagerHardStop()).thenReturn(false);
			  formBean = new C1UXCustDocBaseResponse();
				assertFalse(bundleService.handleProofMsgException(mockBundleUI, e, formBean));
			  assertTrue(formBean.isHardStopFailure());

		      when(mockBundleUI.isDlPagerHardStop()).thenReturn(true);
			  formBean = new C1UXCustDocBaseResponse();
				assertFalse(bundleService.handleProofMsgException(mockBundleUI, e, formBean));
			  assertTrue(formBean.isHardStopFailure());

		      when(mockBundleUI.isEceHardStop()).thenReturn(true);
			  formBean = new C1UXCustDocBaseResponse();
				assertFalse(bundleService.handleProofMsgException(mockBundleUI, e, formBean));
			  assertTrue(formBean.isHardStopFailure());

			  e = makeMsgException(false);
			   formBean = new C1UXCustDocBaseResponse();
			   assertFalse(bundleService.handleProofMsgException(mockBundleUI, e, formBean));
			   assertNotEquals(BundleServiceImpl.ROUTING_WAIT, formBean.getRedirectRouting());
			   assertNotEquals(AtWinXSConstant.EMPTY_STRING, formBean.getMessage());
				  assertTrue(formBean.isHardStopFailure());

				   formBean = new C1UXCustDocBaseResponse();
			      when(mockBundleUI.isEceHardStop()).thenReturn(false);
			      when(mockBundleUI.isDlPagerHardStop()).thenReturn(false);
					assertFalse(bundleService.handleProofMsgException(mockBundleUI, e, formBean));
					  assertFalse(formBean.isHardStopFailure());


			   formBean = new C1UXCustDocBaseResponse();
			   assertTrue(bundleService.handleProofMsgException(mockBundleUI, makeMsgException(true), formBean));
			   assertEquals(BundleServiceImpl.ROUTING_WAIT, formBean.getRedirectRouting());
			  assertFalse(formBean.isHardStopFailure());
	}

	@Test
	void navigateProofs() throws Exception {
		  setUpModuleSession();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			UIEvent event = UIEvent.NEXT;
//	      when(mockBundleItem.getUserInterface()).thenReturn(mockBundleUI);
//	      when(mockOESessionBean.getCurrentCustomDocumentItem()).thenReturn(mockBundleItem);
//	      when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      bundleService = Mockito.spy(bundleService);
//	      doNothing().when(bundleService).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();
			C1UXCustDocPageBean pageResponse = new C1UXCustDocPageBean();
			when(mockBundleUI.getOrderedBundleItemComponentsC1UX()).thenReturn(null);
		  assertEquals(DEVTEST_SITE_ID,
		    		  bundleService.navigateProofs(uiRequest, pageResponse, mockBundleUI,
		    				  errorStrings, mockSessionContainer, DEVTEST_SITE_ID, event));
		  assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

			List<BundleComponent> orderedItems = new ArrayList<>();
		  pageResponse = new C1UXCustDocPageBean();
			when(mockBundleUI.getOrderedBundleItemComponentsC1UX()).thenReturn(orderedItems);
		  assertEquals(DEVTEST_SITE_ID,
		    		  bundleService.navigateProofs(uiRequest, pageResponse, mockBundleUI,
		    				  errorStrings, mockSessionContainer, DEVTEST_SITE_ID, event));
		  assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		  errorStrings.clear();
		  pageResponse = new C1UXCustDocPageBean();
			orderedItems.add(mockBundleComponent);
//			when(mockBundleUI.getOrderedBundleItemComponentsC1UX()).thenReturn(orderedItems);
//			when(mockBundleUI.initializeOrderedBundleComponents(any(), any(), any(), any(), any())).thenReturn(false);
	      when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(0);
//	      when(mockBundleUI.getComponentProofID(any(), any())).thenReturn(FAKE_UPLOAD_LIST_VALUE);
	      assertEquals(UserInterface.NEXT_PAGE_NUMBER_EXIT,
	    		  bundleService.navigateProofs(uiRequest, pageResponse, mockBundleUI,
	    				  errorStrings, mockSessionContainer, DEVTEST_SITE_ID, event));

	      setupMockComponentMap(false, false);
		  pageResponse = new C1UXCustDocPageBean();
			when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
	      assertEquals(UserInterface.NEXT_PAGE_NUMBER_EXIT,
	    		  bundleService.navigateProofs(uiRequest, pageResponse, mockBundleUI,
	    				  errorStrings, mockSessionContainer, UserInterface.NEXT_PAGE_NUMBER_PROOF, event));

		  pageResponse = new C1UXCustDocPageBean();
			when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(0);
			doNothing().when(bundleService).generateComponentProof(mockBundleUI, pageResponse, mockSessionContainer);
		      assertEquals(UserInterface.NEXT_PAGE_NUMBER_PROOF,
		    		  bundleService.navigateProofs(uiRequest, pageResponse, mockBundleUI,
		    				  errorStrings, mockSessionContainer, UserInterface.NEXT_PAGE_NUMBER_PROOF, event));

			  pageResponse = new C1UXCustDocPageBean();
				AtWinXSMsgException e = makeMsgException(false);
				e.getMsg().setErrGeneralMsg("who knows");

		      when(mockBundleUI.getOrderedBundleItemComponentsC1UX()).thenThrow(e);
			  assertEquals(DEVTEST_SITE_ID,
		    		  bundleService.navigateProofs(uiRequest, pageResponse, mockBundleUI,
		    				  errorStrings, mockSessionContainer, DEVTEST_SITE_ID, event));
			  assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());


	}

	  protected void setupComponentMapOnRealBundle(BundleUserInterface ui) {
		    Map<Integer, CustomDocumentItem> itemMap = new HashMap<>();
		    Integer compNum = Integer.valueOf(1);
		    itemMap.put(compNum, mockItem);
		    ui.setConvertedCustDocItemsMap(itemMap);
	  }

	@Test
	void setNewSequenceNumberFromProofPage() {
		BundleUserInterface ui = new BundleUserInterfaceImpl();
		Map<String, String> uiRequest = new HashMap<>();
		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.NAVIGATE_PAGES, ui);
		assertEquals(0, ui.getBundleComponentSequenceCtr());

		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.PREVIOUS, ui);
		assertEquals(0, ui.getBundleComponentSequenceCtr());

		ui.setBundleComponentSequenceCtr(1);
		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.PREVIOUS, ui);
		assertEquals(0, ui.getBundleComponentSequenceCtr());
		assertEquals(0, ui.getProofSeqFromRequest());

		String param = "eventSeqNum";
		uiRequest.put(param, "2");
		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.PREVIOUS, ui);
		assertEquals(1, ui.getProofSeqFromRequest());

		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.NEXT, ui);
		assertEquals(1, ui.getBundleComponentSequenceCtr());
		assertEquals(2, ui.getProofSeqFromRequest());

		setupComponentMapOnRealBundle(ui);
		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.NEXT, ui);
		assertEquals(1, ui.getBundleComponentSequenceCtr());
		assertEquals(2, ui.getProofSeqFromRequest());

		ui.getConvertedCustDocItemsMap().clear();
		bundleService.setNewSequenceNumberFromProofPage(uiRequest, UIEvent.NEXT, ui);
		assertEquals(1, ui.getBundleComponentSequenceCtr());
		assertEquals(2, ui.getProofSeqFromRequest());
	}

	@Test
	void getPageAllowsAddToCartButton() {
		assertFalse(bundleService.getPageAllowsAddToCartButton(null, null, null));

		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST);
		assertFalse(bundleService.getPageAllowsAddToCartButton(mockPage, mockUI, mockItem));

		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
		when(mockBundleUI.getConvertedCustDocItemsMap()).thenReturn(new HashMap<>());
		when(mockBundleUI.getBundleComponentSequenceCtr()).thenReturn(1);
		assertFalse(bundleService.getPageAllowsAddToCartButton(mockPage, mockBundleUI, mockItem));

		setupMockComponentMap(false, false);
		assertTrue(bundleService.getPageAllowsAddToCartButton(mockPage, mockBundleUI, mockItem));
	}

	private ListVO getListVO() {
		return new ListVO(DEVTEST_SITE_ID, DEVTEST_UX_BU_ID, 45, CLASSIC_CONTEXT_PATH, AtWinXSConstant.EMPTY_STRING, true,
				new Date(), new Date(),
           		25, TEST_USER, 236236236, "another list name", true,
           		"filea.txt", "fileb.txt", true, "sheet 1", ManageListsConstants.UPLOAD_LIST_STAT_CD_ACTIVE,
           		TEST_USER, GENERIC_ERROR_MSG, TEST_USER, new Date(),
           		ManageListsConstants.LIST_FORMAT_TEXT, ManageListsConstants.UPLOAD_LIST_DELIMITER_COMMA, false,
				false,false, //CP-1253
				TEST_USER,//CP-1557	Store uploaded lists using profileID
				TEST_USER, //CP-2316 paf id // CP-2363 TH - Added to make sure we add to list when creating file
				new Date(), //CP-2316 paf expiration  // CP-2363 TH - Added to make sure we add to list when creating file
				AtWinXSConstant.EMPTY_STRING,//CP-4282 Convert Excel (no original excel here)
				null,
				false); //CAP-29070 IS_ENCRPT_IN
	}

	@Test
	void populateSFSpecificFields() throws AtWinXSException {
		when(mockBundleUI.buildStepOptionsC1UX(any(), any(), any())).thenReturn(null);
		when(mockVolatileSessionBean.getCustDocPromptValue()).thenReturn(CustomDocsServiceImpl.N);
	      bundleService = Mockito.spy(bundleService);
		    doReturn(true).when(bundleService).getPageAllowsSave(any(), any(), any());
		    doReturn(true).when(bundleService).getPageAllowsBackButton(any(), any());
		    doReturn(true).when(bundleService).getPageAllowsAddToCartButton(any(),  any(), any());
		    doReturn(true).when(bundleService).isUIEditing(any(), any());
			doNothing().when(bundleService).setProofFields(any(), any(), anyInt(), any());
			doNothing().when(bundleService).createJSFile(any(), any());
			doReturn(null).when(bundleService).loadImprintHistory(any(), anyInt(), any(), anyBoolean());
			doReturn(null).when(bundleService).loadUserProfileSearch(any(), anyInt());
			doReturn(mockManageList).when(bundleService).getManageListComponent(any());
			when(mockManageList.retrieveAList(any())).thenReturn(getListVO());
		      doReturn(mockBUListSettings).when(bundleService).getBUListAdminSettings(mockAppSessionBean);
		      when(mockBUListSettings.isDoNotShareListsInd()).thenReturn(false);
				doReturn("02/27/2019").when(bundleService).getFormattedDate(any(), any());

		pageResponse = new C1UXCustDocPageBean();
		bundleService.populateSFSpecificFields(pageResponse, mockPage, mockAppSessionBean,
				mockBundleUI, mockItem, mockVolatileSessionBean, mockOESessionBean);
		assertTrue(pageResponse.isSuccess());

		doThrow(new AtWinXSException("yet another fails", this.getClass().getName())).when(bundleService).setProofFields(any(), any(), anyInt(), any());
		pageResponse = new C1UXCustDocPageBean();
		bundleService.populateSFSpecificFields(pageResponse, mockPage, mockAppSessionBean,
				mockBundleUI, mockItem, mockVolatileSessionBean, mockOESessionBean);
		assertFalse(pageResponse.isSuccess());

		pageResponse = new C1UXCustDocPageBean();
		bundleService.populateSFSpecificFields(pageResponse, mockPage, mockAppSessionBean,
				mockUI, mockItem, mockVolatileSessionBean, mockOESessionBean);
		assertFalse(pageResponse.isSuccess());

	}

}


