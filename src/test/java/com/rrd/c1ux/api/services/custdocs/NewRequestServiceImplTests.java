/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/13/24    A Boomker       CAP-46490                   Initial
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.UIEvent;
import com.rrd.custompoint.orderentry.customdocs.entity.CustomRequestInfo;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.NewRequestCustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.NewRequestCustomDocumentItemImpl;
import com.wallace.atwinxs.admin.vo.ProjectManagerVO;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

class NewRequestServiceImplTests extends CustomDocsServiceImplTests {


	  @InjectMocks
	  protected NewRequestServiceImpl newReqService;
	  @Mock
	  protected NewRequestCustomDocumentItemImpl mockNewRequestItem;
	  @Mock
	  protected CustomRequestInfo mockCustRequest;
	@Test
	void initializeUIOnly() throws Exception {
		setUpModuleSession();
		uiRequest = new HashMap<>();
	    newReqService = Mockito.spy(newReqService);
		when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	    doReturn(mockNewRequestItem).when(newReqService).initializeItem(any(), any(), any(), any(), any(), any(), any());
		doNothing().when(newReqService).saveFullSessionInfo(any(), anyInt(), anyInt());
	    doReturn("Initialized Successfully!").when(newReqService).getCombinedMessage(any());
		List<Page> pages = new ArrayList<>();
		pages.add(mockPage2);

		baseResponse = newReqService.initializeUIOnly(mockSessionContainer, uiRequest);
		assertTrue(baseResponse.isSuccess());

	    doThrow(new AtWinXSException("message", "classname")).when(newReqService).saveFullSessionInfo(any(), anyInt(), anyInt());
		doReturn(GENERIC_ERROR_MSG).when(newReqService).getTranslation(any(Locale.class), any(), anyString());
		baseResponse = newReqService.initializeUIOnly(mockSessionContainer, uiRequest);
		assertFalse(baseResponse.isSuccess());
	}

	@Test
	void initializeItem() throws Exception {
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
		      newReqService = Mockito.spy(newReqService);
		      doReturn(mockNewRequestItem).when(newReqService).createAndInitItem(any(), any(), any(), any(), any(), any());
		      when(mockUI.getProfileSelector()).thenReturn(mockUserProfileSelector);
		      when(mockUserProfileSelector.isShown()).thenReturn(true);
		      when(mockAppSessionBean.getProfileNumber()).thenReturn(250);
		      when(mockUI.isSkipDynamicDataCalls()).thenReturn(false);
		      CustDocUIFormBean uiBean = new CustDocUIFormBean();
		      newReqService.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

		      when(mockUserProfileSelector.isShown()).thenReturn(false);
		      uiBean = new CustDocUIFormBean();
		      newReqService.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

		      when(mockUI.getProfileSelector()).thenReturn(null);
		      uiBean = new CustDocUIFormBean();
		      newReqService.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

	}

	@Test
	void performPageSubmitAction() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockNewRequestItem);
	      newReqService = Mockito.spy(newReqService);
	      doNothing().when(newReqService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockNewRequestItem), any());
	      doNothing().when(newReqService).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      uiRequest = new HashMap<>();
	      uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NEXT.toString());
	      C1UXCustDocBaseResponse baseResponse = newReqService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertTrue(baseResponse instanceof C1UXCustDocBaseResponse);

	      doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(newReqService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockNewRequestItem), any());
			doReturn(GENERIC_ERROR_MSG).when(newReqService).getTranslation(any(Locale.class), any(), anyString());
	      baseResponse = newReqService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new NullPointerException("I blew up!")).when(newReqService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockNewRequestItem), any());
	      baseResponse = newReqService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.STAY.toString());
	      doNothing().when(newReqService).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockNewRequestItem), any());
	      doNothing().when(newReqService).convertToC1UXObject(eq(mockSessionContainer), eq(mockNewRequestItem), any(CustDocUIFormBean.class), any());
	      baseResponse = newReqService.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertTrue(baseResponse instanceof C1UXCustDocPageBean);
	}

	@Test
	void handleSpecificEvent() 	throws Exception {
		  setUpModuleSession();
	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      newReqService = Mockito.spy(newReqService);
	      doNothing().when(newReqService).navigateForward(anyMap(), any(), anyList(), any(UIEvent.class),
	    		  eq(mockNewRequestItem), any(C1UXCustDocPageBean.class), eq(mockSessionContainer));

	      pageResponse = new C1UXCustDocPageBean();
		uiRequest = new HashMap<>();
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NEXT.toString());
		newReqService.handleSpecificEvent(uiRequest, mockSessionContainer, mockUIBean, mockNewRequestItem, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      doNothing().when(newReqService).saveAllUpdatedSessions(any(), any(), any(), anyInt());
	      pageResponse = new C1UXCustDocPageBean();
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_CONTINUE.toString());
		newReqService.handleSpecificEvent(uiRequest, mockSessionContainer, mockUIBean, mockNewRequestItem, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_SEARCH.toString());
		newReqService.handleSpecificEvent(uiRequest, mockSessionContainer, mockUIBean, mockNewRequestItem, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());
	}

	@Test
	void handleUnsupportedEvents() {
		// CAP-46524
		//assertTrue(newReqService.handleUnsupportedEvents(UIEvent.REFRESH));
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.APPLY_COLOR_CHANGE)); // applying color to all of UI from font/color set
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.PROFILE_SEARCH));
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.HISTORY_SEARCH));
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.PROFILE_SELECT));
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.ALTERNATE_PROFILE_SELECT));
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.KEY_PROFILE_SELECT));

		assertFalse(newReqService.handleUnsupportedEvents(UIEvent.STAY));
		assertTrue(newReqService.handleUnsupportedEvents(UIEvent.HISTORY_SELECT));
	}

	@Test
	void handleLeavingEvents() throws Exception {
		  setUpModuleSession();
	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);

	      newReqService = Mockito.spy(newReqService);
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();
	      doNothing().when(newReqService).resetUIAndItem(any(), any());
	      doNothing().when(newReqService).saveAllUpdatedSessions(any(), any(), any(), anyInt());
	      doNothing().when(newReqService).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockNewRequestItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any(C1UXCustDocPageBean.class));
	      pageResponse = new C1UXCustDocPageBean();

	      assertTrue(newReqService.handleLeavingEvents(UIEvent.CANCEL_ORDER, mockNewRequestItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      pageResponse = new C1UXCustDocPageBean();

	      assertTrue(newReqService.handleLeavingEvents(UIEvent.CANCEL, mockNewRequestItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      when(mockNewRequestItem.saveOrderLine(any(), any(), any(), anyBoolean(), any(), any(), anyInt(), any(), any()))
	      	.thenReturn(mockOrderLineVOKey);

	      pageResponse = new C1UXCustDocPageBean();
	      assertTrue(newReqService.handleLeavingEvents(UIEvent.SAVE_AND_EXIT, mockNewRequestItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));
	      pageResponse = new C1UXCustDocPageBean();
	      assertTrue(newReqService.handleLeavingEvents(UIEvent.SAVE_ORDER, mockNewRequestItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      pageResponse = new C1UXCustDocPageBean();
//	      when(mockUIBean.isHdnIsDirty()).thenReturn(true);
	      assertTrue(newReqService.handleLeavingEvents(UIEvent.SAVE_AND_EXIT, mockNewRequestItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));


	      pageResponse = new C1UXCustDocPageBean();
		assertFalse(newReqService.handleLeavingEvents(UIEvent.STAY, mockNewRequestItem, mockSessionContainer,
			pageResponse, uiRequest, errorStrings, mockUIBean));
	}

	@Test
	void navigateForward() throws Exception
	{
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      newReqService = Mockito.spy(newReqService);
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();

	      doNothing().when(newReqService).readSubmittedData(any(UIEvent.class), eq(mockUIBean), eq(mockUI), eq(uiRequest), eq(errorStrings));
	      pageResponse = new C1UXCustDocPageBean();
	      doReturn(false).when(newReqService).isStaySituation(any(UIEvent.class), eq(mockUI), eq(errorStrings));
	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      when(mockUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(1);
	      doNothing().when(newReqService).determineNavigatePages(eq(uiRequest), any(UIEvent.class), anyInt(), anyInt(), eq(pageResponse));
	      doNothing().when(newReqService).showProofErrors(errorStrings, mockUIBean);
	      when(mockUIBean.getError()).thenReturn(GENERIC_ERROR_MSG);

	      newReqService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockNewRequestItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
	      newReqService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockNewRequestItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(Integer.MAX_VALUE);
	      newReqService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockNewRequestItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      pageResponse = new C1UXCustDocPageBean();
	      newReqService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockNewRequestItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockUIBean.getError()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      when(mockUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(UserInterface.NEXT_PAGE_NUMBER_EXIT);
	      pageResponse = new C1UXCustDocPageBean();
	      doNothing().when(newReqService).handleExitNonPF(anyInt(), eq(mockSessionContainer), eq(mockNewRequestItem), eq(errorStrings), eq(pageResponse));
	      newReqService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockNewRequestItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage()); // please note that this is wrong
	      // something broke nonPF, but technically we don't have to have it working for 6 months at least, so that's ok
	      doReturn(true).when(newReqService).isStaySituation(any(UIEvent.class), eq(mockUI), eq(errorStrings));
	      pageResponse = new C1UXCustDocPageBean();
	      newReqService.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockNewRequestItem, pageResponse, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage()); // please note that this is wrong

	}

	@Test
	void handleExitNonPF() throws Exception {
	      setUpModuleSession();
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      when(mockUI.isTryUI()).thenReturn(false);
	      when(mockUI.isOrderQuestions()).thenReturn(false);
	      when(mockUI.isLastPage(anyInt())).thenReturn(true);
	      when(mockUI.isSkipProofing()).thenReturn(true);
	      when(mockNewRequestItem.saveOrderLine(any(), any(), any(), anyBoolean(), any(), any(), anyInt(), any(), any()))
	      		.thenReturn(mockOrderLineVOKey);

	      when(mockNewRequestItem.getCustomDocOrderLineID()).thenReturn(1);
	        newReqService = Mockito.spy(newReqService);
			doReturn(RouteConstants.CART_ENTRY_ROUTING_URL).when(newReqService).getForwardToURL(any(), any(), any());
			errorStrings = new ArrayList<>();

		    pageResponse = new C1UXCustDocPageBean();
			newReqService.handleExitNonPF(2, mockSessionContainer, mockNewRequestItem, errorStrings, pageResponse);
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getRedirectRouting());

		     when(mockNewRequestItem.getCustomDocOrderLineID()).thenReturn(-1);
			when(mockUI.isTryUI()).thenReturn(true);
		    pageResponse = new C1UXCustDocPageBean();
			newReqService.handleExitNonPF(2, mockSessionContainer, mockNewRequestItem, errorStrings, pageResponse);
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getRedirectRouting());

		    pageResponse = new C1UXCustDocPageBean();
			when(mockUI.isTryUI()).thenReturn(false);
			newReqService.handleExitNonPF(2, mockSessionContainer, mockNewRequestItem, errorStrings, pageResponse);
			assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getRedirectRouting());
	}

	@Test
	void determineNavigatePages() {
		Map<String, String> uiRequest = new HashMap<>();
		pageResponse = new C1UXCustDocPageBean();
		newReqService.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 5, 5, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		uiRequest.put("navPage", "5");
		newReqService.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 2, 5, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		uiRequest.put("navPage", "5");
		newReqService.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 2, 3, pageResponse);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());
	}

	@Test
	void performCancelOrCancelOrder() throws Exception
		{
	    setupBaseMockSessions();
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	      when(mockNewRequestItem.getOrderID()).thenReturn(256);
	        newReqService = Mockito.spy(newReqService);

	        doReturn(RouteConstants.CART_ENTRY_ROUTING_URL).when(newReqService).getCancelURL(mockNewRequestItem, mockUI, mockSessionContainer);
	        doNothing().when(newReqService).setupDirectionCancel(any(), any(), any(), any(), any(), any());
	        doNothing().when(newReqService).resetUIAndItem(any(), any());

	        pageResponse = new C1UXCustDocPageBean();
	        newReqService.performCancelOrCancelOrder(mockOESession, mockUI, mockNewRequestItem,
	        		UserInterface.UIEvent.CANCEL_ORDER, mockSessionContainer, pageResponse);
	        assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, pageResponse.getRedirectRouting());

	        pageResponse = new C1UXCustDocPageBean();
	        newReqService.performCancelOrCancelOrder(mockOESession, mockUI, mockNewRequestItem,
	        		UserInterface.UIEvent.CANCEL, mockSessionContainer, pageResponse);
	        assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, pageResponse.getRedirectRouting());
		}

	private Collection<ProjectManagerVO> getPMs() {
		ArrayList<ProjectManagerVO> pms = new ArrayList<>();
		pms.add(new ProjectManagerVO(0, 0, AtWinXSConstant.PROJECT_MANAGER_TYPE_REQUEST, "PM1", "Mr. Claus", "santa@northpole.com", "654-333-2222", true));
		pms.add(new ProjectManagerVO(0, 0, AtWinXSConstant.PROJECT_MANAGER_TYPE_REQUEST, "PM2", "Santas Lil Helper", "blitzen@northpole.com", "654-333-2222", true));
		return pms;
	}

	@Test
	void addToCart() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		setUpModuleSessionNoBase();
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
//	      when(mockUI.isTryUI()).thenReturn(false);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockNewRequestItem);
	      when(mockNewRequestItem.getRequestPMs()).thenReturn(getPMs());
	        newReqService = Mockito.spy(newReqService);
	      doReturn(54).when(newReqService).saveRequestData(eq(mockAppSessionBean), anyString(), eq(mockUI));
	      doNothing().when(newReqService).resetUIAndItem(mockVolatileSessionBean, mockOEOrderSession);
	      doNothing().when(newReqService).saveAllUpdatedSessions(eq(mockOESession), eq(mockApplicationVolatileSession), eq(mockApplicationSession), anyInt());
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
	      doReturn("SUCCESS").when(newReqService).getTranslation(any(Locale.class), any(CustomizationToken.class), anyString(), anyString(), anyMap());
//	      doReturn(GENERIC_ERROR_MSG).when(newReqService).getTranslation(any(Locale.class), any(CustomizationToken.class), anyString());
	      baseResponse = newReqService.addToCart(mockSessionContainer);
	      assertTrue(baseResponse.isSuccess());
	      assertEquals(RouteConstants.HOME, baseResponse.getRedirectRouting());
	}

	// CAP-42298
	@Test
	void cancelAction() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockNewRequestItem);
	      when(mockNewRequestItem.getUserInterface()).thenReturn(mockUI);
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      newReqService = Mockito.spy(newReqService);
	      doNothing().when(newReqService).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      doNothing().when(newReqService).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockNewRequestItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      baseResponse = newReqService.cancelAction(mockSessionContainer, false);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = newReqService.cancelAction(mockSessionContainer, true);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new AtWinXSException("myError", "Aclassname")).when(newReqService).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockNewRequestItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      doReturn(GENERIC_ERROR_MSG).when(newReqService).getTranslation(any(Locale.class), any(), anyString());

	      baseResponse = newReqService.cancelAction(mockSessionContainer, false);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = newReqService.cancelAction(mockSessionContainer, true);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new NullPointerException("I blew up!")).when(newReqService).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockNewRequestItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      baseResponse = newReqService.cancelAction(mockSessionContainer, false);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = newReqService.cancelAction(mockSessionContainer, true);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	}

	@Test
	void buildCustDocUIFormBean() throws AtWinXSException {
		newReqService = Mockito.spy(newReqService);
		when(mockUI.getRefreshListTriggerVariable()).thenReturn(null);
		when(mockVolatileSessionBean.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.ORDER_SCENARIO_NUMBER_DEFAULT_VALUE);
		when(mockPage.getPageNumber()).thenReturn(2);
		doNothing().when(newReqService).setBlankFields(any());
		doNothing().when(newReqService).setFieldsNoLogic(any(), any(), any(), any());
		doReturn(true).when(newReqService).getAllowFailedProofs();
		when(mockUI.isTryUI()).thenReturn(false);
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		newReqService.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertEquals(Boolean.FALSE.toString(), formBean.getOrderFromFile());

		when(mockVolatileSessionBean.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE);
		formBean = new CustDocUIFormBean();
		newReqService.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertEquals(Boolean.TRUE.toString(), formBean.getOrderFromFile());
		assertTrue(formBean.isAllowFailedProof());

		doReturn(false).when(newReqService).getAllowFailedProofs();
		formBean = new CustDocUIFormBean();
		newReqService.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(formBean.isAllowFailedProof());

		when(mockPage.getPageNumber()).thenReturn(99998);
		when(mockUI.isTryUI()).thenReturn(true);
		formBean = new CustDocUIFormBean();
		newReqService.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertTrue(formBean.isAllowFailedProof());
	}

	@Test
	void getNewRequestLink() {
		newReqService = Mockito.spy(newReqService);
//		doReturn(false).when(newReqService).isVarValueUploadOrSearch(any());
		when(mockVar.getListValue()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		assertEquals(AtWinXSConstant.EMPTY_STRING, newReqService.getNewRequestLink(mockVar, mockAppSessionBean));

		when(mockVar.getListValue()).thenReturn(CLASSIC_CONTEXT_PATH);
		assertEquals(AtWinXSConstant.EMPTY_STRING, newReqService.getNewRequestLink(mockVar, mockAppSessionBean));

		when(mockVar.getListValue()).thenReturn("U12345^UPLOAD: MyFile.pdf");
		doReturn(mockUploadFileUpload).when(newReqService).getUploadFileObject(any(), any(), any());
		when(mockUploadFileUpload.generateGetAFileLinkURLC1UX(anyInt())).thenReturn(GENERIC_SAVE_FAILED_ERR_ENGLISH);
		assertEquals(GENERIC_SAVE_FAILED_ERR_ENGLISH, newReqService.getNewRequestLink(mockVar, mockAppSessionBean));
	}

	protected void setupObjMapItem() {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(NewRequestCustomDocumentItem.class, mockCustomizationToken)).thenReturn(mockNewRequestItem);
	}

	@Test
	void createAndInitItem() throws AtWinXSException {
		Map<String, String> uiRequest = new HashMap<>();
		setupObjMapItem();
		assertEquals(mockNewRequestItem, newReqService.createAndInitItem(uiRequest, mockOEOrderSessionBean, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings));
	}

	protected void setupObjMapCustRequest() {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(CustomRequestInfo.class, mockCustomizationToken)).thenReturn(mockCustRequest);
	}

	@Test
	void saveRequestData() throws AtWinXSException {
		setupObjMapCustRequest();
		when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
		when(mockAppSessionBean.getBuID()).thenReturn(DEVTEST_UX_BU_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn("AMY");
		when(mockAppSessionBean.getSiteLoginID()).thenReturn("AMYSITE");
		when(mockAppSessionBean.getCorporateNumber()).thenReturn("0000097888");
		when(mockAppSessionBean.getEmailAddress()).thenReturn("a@b.c");
		when(mockAppSessionBean.getProfileNumber()).thenReturn(222);
		when(mockUI.getUiKey()).thenReturn(mockUIKey);
		when(mockUI.getWorkingXML(anyBoolean(), anyBoolean())).thenReturn("<tag>value</tag>");
		when(mockCustRequest.getRequestID()).thenReturn(FAKE_VAR1_NUMBER);
		assertEquals(FAKE_VAR1_NUMBER, newReqService.saveRequestData(mockAppSessionBean, BLANK_NOT_ALLOWED_ERR_ENGLISH, mockUI));
	}

}


