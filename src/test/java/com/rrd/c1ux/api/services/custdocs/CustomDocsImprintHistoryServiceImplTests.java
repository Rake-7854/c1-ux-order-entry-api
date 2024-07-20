/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 * 	07/09/24	A Boomker			CAP-46538		Initial version
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBasicImprintHistorySearchRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocImprintHistorySearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXImprintHistoryOptions;
import com.rrd.custompoint.customdocs.ui.ProfileSelectorImpl;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.customdocs.HistoryOption;
import com.rrd.custompoint.orderentry.customdocs.ImprintHistorySelector;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

class CustomDocsImprintHistoryServiceImplTests extends BaseCustomDocsServiceTests {

	  @InjectMocks
	  private CustomDocsImprintHistoryServiceImpl service;

	  @Mock
	  private VolatileSessionBean mockVolatileSessionBean;

	  @Mock
	  private OrderEntrySession mockOrderEntrySession;

	  @Mock
	  private ApplicationVolatileSession mockApplicationVolatileSession;

	  @Mock
	  private UserInterfaceImpl mockUI;

	  @Mock
	  private CustomDocumentItemImpl mockItem;

	  @Mock
	  private ProfileSelectorImpl mockProfileSelector;

	  @Mock
	  private C1UXCustDocProfileSearchResponse mockSearchResponse;

	  @Mock
	  private ProfileVO mockUserProfile;

	  @Mock
	  private C1UXCustDocLoadAltProfileResponse mockLoadAltProfileResponse;

		@Test
		void getSelectedImprintHistory() throws Exception {
		  setUpModuleSessionNoBase();
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      service = Mockito.spy(service);
	      doNothing().when(service).loadHistorySelection(any(UserInterface.class), any(), any(), any(), anyInt());

	      assertNotNull(service.getSelectedImprintHistory(mockSessionContainer, new C1UXCustDocLoadImprintHistoryRequest()));
		}

		@Test
		void check403Errors_loadHistorySelection() throws Exception {
			setUpModuleSession();
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.loadHistorySelection(mockUI, null, mockSessionContainer, null, -1);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

			when(mockImprintHistorySelector.isShown()).thenReturn(false);
			exception = assertThrows(AccessForbiddenException.class, () -> {
				service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, null, -1);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

			when(mockImprintHistorySelector.isShown()).thenReturn(true);
			when(mockUI.isTryUI()).thenReturn(true);
			exception = assertThrows(AccessForbiddenException.class, () -> {
				service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, null, -1);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

			when(mockUI.isTryUI()).thenReturn(false);
			when(mockUI.isFieldsEditable()).thenReturn(false);
			when(mockUI.isNewRequestFlow()).thenReturn(true);
			when(mockPage.getPageNumber()).thenReturn(2);
			List<Page> pages = new ArrayList<>();
			pages.add(mockPage);
			pages.add(mockPage2);
			when(mockUI.getPages()).thenReturn(pages);
			when(mockUI.getNextPageNumber()).thenReturn(3);
			exception = assertThrows(AccessForbiddenException.class, () -> {
				service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, null, -1);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

			when(mockUI.getNextPageNumber()).thenReturn(2);
			exception = assertThrows(AccessForbiddenException.class, () -> {
				service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, null, -1);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

			when(mockUI.isFieldsEditable()).thenReturn(true);
			exception = assertThrows(AccessForbiddenException.class, () -> {
				service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, null, -1);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

			when(mockUI.isNewRequestFlow()).thenReturn(false);

		    service = Mockito.spy(service);

			doReturn(true).when(service).validateHistorySelection(any(ImprintHistorySelector.class), anyInt(),
					any(C1UXCustDocBaseResponse.class), any(AppSessionBean.class));
			doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
			doReturn("a combined message").when(service).getCombinedMessage(any());
			when(mockImprintHistorySelector.isShown()).thenReturn(true);
			C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();

			assertDoesNotThrow(() -> {
				service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, response, 234);
			});


		}
		@Test
		void loadHistorySelection() throws Exception {
			setUpModuleSession();
//		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
//		    when(mockItem.getUserInterface()).thenReturn(mockUI);
			when(mockUI.isTryUI()).thenReturn(false);
			when(mockUI.isFieldsEditable()).thenReturn(true);
			when(mockUI.isNewRequestFlow()).thenReturn(false);
			when(mockPage.getPageNumber()).thenReturn(2);
			List<Page> pages = new ArrayList<>();
			pages.add(mockPage);
			pages.add(mockPage2);
			when(mockUI.getPages()).thenReturn(pages);
			when(mockUI.getNextPageNumber()).thenReturn(2);

			C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
		    service = Mockito.spy(service);
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), anyString(), anyString());
			when(mockImprintHistorySelector.isShown()).thenReturn(true);
			doReturn(false).when(service).validateHistorySelection(any(ImprintHistorySelector.class), anyInt(),
					any(C1UXCustDocBaseResponse.class), any(AppSessionBean.class));
			service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, response, -1);
			assertFalse(response.isSuccess());

			response = new C1UXCustDocBaseResponse();
			doReturn(true).when(service).validateHistorySelection(any(ImprintHistorySelector.class), anyInt(),
					any(C1UXCustDocBaseResponse.class), any(AppSessionBean.class));
			doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
			doReturn("a combined message").when(service).getCombinedMessage(any());
			service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, response, -1);
			assertTrue(response.isSuccess());

			response = new C1UXCustDocBaseResponse();
			doThrow(new AtWinXSException("cannot save session","loadhistoryfail")).when(service).saveFullOESessionInfo(any(OrderEntrySession.class), anyInt());
			service.loadHistorySelection(mockUI, mockImprintHistorySelector, mockSessionContainer, response, -1);
			assertFalse(response.isSuccess());
		}

		@Test
		void validateHistorySelection() throws AtWinXSException {
		    service = Mockito.spy(service);
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), anyString(), anyString());
			C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
			when(mockImprintHistorySelector.getHistoryOptions()).thenReturn(null);
			assertFalse(service.validateHistorySelection(mockImprintHistorySelector, -1, response, mockAppSessionBean));

			doThrow(new AtWinXSException("cannot load history","validatehistoryfail")).when(mockImprintHistorySelector).loadOrderHistoryOptions();
			assertFalse(service.validateHistorySelection(mockImprintHistorySelector, -1, response, mockAppSessionBean));

			List<HistoryOption> options = new ArrayList<>();
			int goodNum = 12345;
			options.add(new HistoryOption(goodNum, "label"));
			when(mockImprintHistorySelector.getHistoryOptions()).thenReturn(options);
			when(mockImprintHistorySelector.isOrdersFound()).thenReturn(false);
			assertFalse(service.validateHistorySelection(mockImprintHistorySelector, -1, response, mockAppSessionBean));

			when(mockImprintHistorySelector.isOrdersFound()).thenReturn(true);
			assertFalse(service.validateHistorySelection(mockImprintHistorySelector, -1, response, mockAppSessionBean));
			assertFalse(service.validateHistorySelection(mockImprintHistorySelector, 5678, response, mockAppSessionBean));

			assertTrue(service.validateHistorySelection(mockImprintHistorySelector, goodNum, response, mockAppSessionBean));
		}

		@Test
		void loadImprintHistory() throws AtWinXSException {
			int firstPageNum = 2;
			int secondPageNum = 30;
			when(mockUI.isTryUI()).thenReturn(true);
			C1UXImprintHistoryOptions options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNull(options);
			when(mockUI.isTryUI()).thenReturn(false);
			when(mockUI.isFieldsEditable()).thenReturn(false);
			when(mockUI.isNewRequestFlow()).thenReturn(true);
			when(mockPage.getPageNumber()).thenReturn(2);
			List<Page> pages = new ArrayList<>();
			pages.add(mockPage);
			pages.add(mockPage2);
			when(mockUI.getPages()).thenReturn(pages);
			options = service.loadImprintHistory(mockUI, secondPageNum, mockAppSessionBean, false);
			assertNull(options);

			when(mockUI.getImprintHistorySelector()).thenReturn(null);
			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNull(options);
			when(mockUI.isFieldsEditable()).thenReturn(true);
			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNull(options);

			when(mockUI.isNewRequestFlow()).thenReturn(false);
			when(mockUI.getImprintHistorySelector()).thenReturn(null);
			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNull(options);

			when(mockUI.getImprintHistorySelector()).thenReturn(mockImprintHistorySelector);
			when(mockImprintHistorySelector.isShown()).thenReturn(false);
			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNull(options);
			when(mockImprintHistorySelector.isShown()).thenReturn(true);
			when(mockImprintHistorySelector.getHistoryOptions()).thenReturn(null);
			when(mockImprintHistorySelector.isOrdersFound()).thenReturn(false);
			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNull(options);
			when(mockImprintHistorySelector.isOrdersFound()).thenReturn(true);

			when(mockImprintHistorySelector.getCurrentOrderID()).thenReturn(45);
			when(mockImprintHistorySelector.getItemSearchOption()).thenReturn(AtWinXSConstant.EMPTY_STRING);
			when(mockImprintHistorySelector.getProfileSearchOption()).thenReturn(AtWinXSConstant.EMPTY_STRING);
			when(mockImprintHistorySelector.isMyOrdersOnly()).thenReturn(true);
			when(mockImprintHistorySelector.isThisItemOnly()).thenReturn(true);
			when(mockImprintHistorySelector.getNumOrders()).thenReturn(2);

			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNotNull(options);
			when(mockImprintHistorySelector.getHistoryOptions()).thenReturn(new ArrayList<>());
			options = service.loadImprintHistory(mockUI, firstPageNum, mockAppSessionBean, false);
			assertNotNull(options);
		}

		@Test
		void basicImprintHistorySearch() throws Exception {
			setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.isAllowImpHistSrchInd()).thenReturn(true);
		      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);
		    service = Mockito.spy(service);
			doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), anyString(), anyString());
			doReturn(null).when(service).loadImprintHistory(any(), anyInt(), any(), anyBoolean());
			when(mockUI.getImprintHistorySelector()).thenReturn(mockImprintHistorySelector);
			when(mockImprintHistorySelector.isShown()).thenReturn(false);

			C1UXCustDocBasicImprintHistorySearchRequest request = new C1UXCustDocBasicImprintHistorySearchRequest();
			C1UXCustDocImprintHistorySearchResponse response = service.basicImprintHistorySearch(mockSessionContainer, request);
			assertTrue(response.isSuccess());

			when(mockImprintHistorySelector.isShown()).thenReturn(true);
			response = service.basicImprintHistorySearch(mockSessionContainer, request);
			assertTrue(response.isSuccess());

			when(mockUI.getImprintHistorySelector()).thenReturn(null);
			response = service.basicImprintHistorySearch(mockSessionContainer, request);
			assertTrue(response.isSuccess());

			when(mockUI.getImprintHistorySelector()).thenReturn(mockImprintHistorySelector);

			doThrow(new NullPointerException()).when(mockImprintHistorySelector).isShown();
			response = service.basicImprintHistorySearch(mockSessionContainer, request);
			assertFalse(response.isSuccess());

			when(mockAppSessionBean.isAllowImpHistSrchInd()).thenReturn(false);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				service.basicImprintHistorySearch(mockSessionContainer, request);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		}

}
