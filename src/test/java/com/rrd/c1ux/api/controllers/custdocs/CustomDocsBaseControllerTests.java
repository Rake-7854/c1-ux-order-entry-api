/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/14/24    A Boomker       CAP-46490                   Initial
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUIPageSubmitRequest;
import com.rrd.c1ux.api.services.custdocs.BundleProofingServiceImpl;
import com.rrd.c1ux.api.services.custdocs.BundleServiceImpl;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProofingServiceImpl;
import com.rrd.c1ux.api.services.custdocs.CustomDocsServiceImpl;
import com.rrd.c1ux.api.services.custdocs.NewRequestServiceImpl;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.EntryPoint;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.UIEvent;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
@WithMockUser
class CustomDocsBaseControllerTests  {


	@Mock
	protected TranslationService mockTranslationService = mock(TranslationService.class);

	@Mock
	protected ObjectMapFactoryService mockObjectMapFactoryService = mock( ObjectMapFactoryService.class);

	@Mock
	protected SessionHandlerService mockSessionHandlerService = mock(SessionHandlerService.class);

	  protected CustomDocsServiceImpl mockService = new CustomDocsServiceImpl(mockTranslationService, mockObjectMapFactoryService,
				mockSessionHandlerService);

	  protected CustomDocsProofingServiceImpl mockProofService = new CustomDocsProofingServiceImpl(mockTranslationService, mockObjectMapFactoryService,
				mockSessionHandlerService);

	  @Mock
	  protected SessionContainer mockSessionContainer = mock(SessionContainer.class);

	  @Mock
	  protected ApplicationSession mockApplicationSession = mock(ApplicationSession.class);

	  @Mock
	  protected AppSessionBean mockAppSessionBean = mock(AppSessionBean.class);

	  @Mock
	  protected EntityObjectMap mockEntityObjectMap = mock(EntityObjectMap.class);

	  @Mock
	  protected OEOrderSessionBean mockOESessionBean = mock(OEOrderSessionBean.class);

	  @Mock
	  protected ApplicationVolatileSession mockApplicationVolatileSession = mock(ApplicationVolatileSession.class);

	  @Mock
	  protected VolatileSessionBean mockVolatileSessionBean = mock(VolatileSessionBean.class);

	  @Mock
	  protected PunchoutSessionBean mockPunchoutSessionBean = mock(PunchoutSessionBean.class);

	  @Mock
	  protected CPSessionReader mockCPSessionReader = mock(CPSessionReader.class);

	  @Mock
	  protected TokenReader mockTokenReader = mock(TokenReader.class);

		@Mock
		protected OrderEntrySession mockOESession = mock(OrderEntrySession.class);

		@Mock
		protected OEOrderSessionBean mockOEOrderSession = mock(OEOrderSessionBean.class);

		@Mock
		protected OEResolvedUserSettingsSessionBean mockUserSettings = mock(OEResolvedUserSettingsSessionBean.class);

		@Mock
		protected UserInterface mockUI = mock(UserInterface.class);

		@Mock
		protected CustomDocumentItem mockItem = mock(CustomDocumentItem.class);

		 @Mock
		  protected IManageItemsInterface mockItemInterface = mock(IManageItemsInterface.class);

		 @Mock
		 protected ManageItemsInterfaceLocatorService mockLocator = mock(ManageItemsInterfaceLocatorService.class);

			@Mock
			private CustomizationToken mockCustomizationToken = mock(CustomizationToken.class);

	  protected String TEST_ENCRYPTED_SESSIONID;

	  protected CustomDocsUserInterfaceController controller;
	  protected CustDocsProofController proofController;

	  protected void setupItemInterface() throws AtWinXSException {
		  	mockService.setItemLocator(mockLocator);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
			when(mockLocator.locate(mockCustomizationToken)).thenReturn(mockItemInterface);
	  }

	  @BeforeEach
	  public void setUp() throws AtWinXSException {
		  controller = new CustomDocsUserInterfaceController(mockTokenReader, mockCPSessionReader, mockService);

			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockCPSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
			TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(true);
			when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
			when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
			Mockito.spy(mockService);
	  }

	  @Test
	  void checkSessionForClassInitializeBundle() throws AtWinXSException {
		  setupItemInterface();
		  when(mockAppSessionBean.getSiteID()).thenReturn(25);
		  when(mockItemInterface.checkItemClassification(anyInt(), any(), any())).thenReturn(ItemConstants.ITEM_CLASS_BUNDLE);
		  controller.checkSessionForClassInitialize(mockSessionContainer, new HashMap<String, String>());
		  assertTrue(controller.getService() instanceof BundleServiceImpl);
	  }

	  @Test
	  void checkSessionForClassInitializeNewRequest() throws AtWinXSException {
		  Map<String, String> uiRequest = new HashMap<>();
		  uiRequest.put(OrderEntryConstants.CUST_DOC_ENTRY_POINT, ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST);
		  controller.checkSessionForClassInitialize(mockSessionContainer, uiRequest);
		  assertTrue(controller.getService() instanceof NewRequestServiceImpl);
	  }

	  @Test
	  void checkSessionForClassBundle() throws AtWinXSException {
		  when(mockItem.getEntryPoint()).thenReturn(EntryPoint.CATALOG_EXPRESS);
		  when(mockItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_BUNDLE);
		  controller.checkSessionForClass(mockSessionContainer);
		  assertTrue(controller.getService() instanceof BundleServiceImpl);
		  proofController = new CustDocsProofController(mockTokenReader, mockCPSessionReader, mockProofService, mockService);
		  proofController.checkSessionForClass(mockSessionContainer);
		  assertTrue(proofController.getService() instanceof BundleServiceImpl);
		  assertTrue(proofController.getProofingService() instanceof BundleProofingServiceImpl);

	  }

	  @Test
	  void checkSessionForClassNewRequest() throws AtWinXSException {
		  when(mockItem.getEntryPoint()).thenReturn(EntryPoint.NEW_REQUEST);
		  controller.checkSessionForClass(mockSessionContainer);
		  assertTrue(controller.getService() instanceof NewRequestServiceImpl);
	  }

	  @Test
	  void getParametersFromCustDocAPICall() {
		  C1UXCustDocUIPageSubmitRequest  req = new C1UXCustDocUIPageSubmitRequest();
		  req.setEventAction(UIEvent.NEXT.toString());
		  Map<String, String> result = controller.getParametersFromCustDocAPICall(req);
		  assertFalse(result.isEmpty());
		  req.setForm1("fakeForm1var=amy");
		  req.setOtherSerializedForm("fakeotherformvar=cat");
		  req.setEventAction(AtWinXSConstant.EMPTY_STRING);
		  result = controller.getParametersFromCustDocAPICall(req);
		  assertFalse(result.isEmpty());
	  }

	  @Test
	  void testMakeMethods() {
		  assertTrue(controller.makeCustomDocsServiceImpl(mockService, mockSessionContainer) instanceof CustomDocsServiceImpl);
		  assertTrue(controller.makeCustomDocsProofingServiceImpl(mockProofService, mockSessionContainer) instanceof CustomDocsProofingServiceImpl);
	  }
}


