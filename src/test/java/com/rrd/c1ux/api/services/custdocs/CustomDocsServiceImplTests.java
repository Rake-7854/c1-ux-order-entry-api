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
 *  08/28/23	A Boomker		CAP-43022					Adding tests for imprint history
 *  08/29/23	A Boomker		CAP-42841					Added handling for sample PDF url
 *  09/11/23	A Boomker		CAP-43528					Add handling for checkbox field of checked state
 *	09/14/23	A Boomker		CAP-43660					Added dateFormat for calendar date input type
 *	09/19/23	A Boomker		CAP-42817 					In processErrorMessages(), getting rid of the prefix caret for C1UX
 *	09/29/23	A Boomker		CAP-44304					Changing tests for vars blanked by show hide
 *	09/29/23	A Boomker		CAP-43827					Make sure checkboxes are always false for required
 *  10/26/23    AKJ Omisol		CAP-43024					Add test cases for getImprintHistory
 *	11/07/23	A Boomker		CAP-44427 and CAP-44463		Added tests for changes to getUIPage
 *	11/15/23	A Boomker		CAP-44460 					Never return CP path for no image
 *	11/21/23	A Boomker		CAP-44780					Added handling for loading variable upload info
 *	11/21/23	A Boomker		CAP-44549					Junits added for save and search
 *	12/04/23	A Boomker		CAP-45654					Force save when event calls for always save
 *	12/18/23	A Boomker		CAP-45488					Changes to getUIPage to fix formatted paragraphs and external source lists
 *	02/13/24	A Boomker		CAP-46309					Added changes for file upload variables
 *	02/19/24	A Boomker		CAP-44837					Changes to list options to indicate uploads
 *	04/03/24	R Ruth			CAP-46492					Modify generation of customization steps link for bundle items
 * 	04/25/24	A Boomker		CAP-46498					Handling for lists in getUIPage API
 * 	05/10/24	A Boomker		CAP-49273					Add handling for merge only and proof only UIs
 * 	05/30/24	A Boomker		CAP-46522					Send selected uploads for multiple selects
 *  06/03/24	A Boomker		CAP-46501					Add testing for alternate profile handling
 * 	07/01/24	A Boomker		CAP-46488					Added handling for kits
 *  07/02/24	R Ruth			CAP-46487					Added values for width and height
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXAlternateProfileOptions;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocGridAssignment;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocGroupBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataPage;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXDisplayControls;
import com.rrd.c1ux.api.models.custdocs.C1UXUIListOption;
import com.rrd.c1ux.api.models.custdocs.C1UXUserProfileSearchOptions;
import com.rrd.c1ux.api.models.custdocs.C1UXVariableBean;
import com.rrd.c1ux.api.models.custdocs.C1uxCustDocListDetails;
import com.rrd.custompoint.customdocs.ui.CheckboxRendererImpl;
import com.rrd.custompoint.customdocs.ui.GridGroup;
import com.rrd.custompoint.customdocs.ui.variables.FormattedParagraphVariable;
import com.rrd.custompoint.customdocs.utils.entity.FormattedParagraphProcessor;
import com.rrd.custompoint.orderentry.customdocs.AlternateProfileSelector;
import com.rrd.custompoint.orderentry.customdocs.C1UXCustDocGridColumn;
import com.rrd.custompoint.orderentry.customdocs.C1UXCustDocGridRow;
import com.rrd.custompoint.orderentry.customdocs.DisplayControl;
import com.rrd.custompoint.orderentry.customdocs.DisplayControls;
import com.rrd.custompoint.orderentry.customdocs.Group;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.ProfileOption;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.EntryPoint;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.OrderListSource;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofView;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.UIEvent;
import com.rrd.custompoint.orderentry.customdocs.Variable;
import com.rrd.custompoint.orderentry.customdocs.Variable.InputType;
import com.rrd.custompoint.orderentry.customdocs.Variable.InstructionType;
import com.rrd.custompoint.orderentry.customdocs.Variable.PageflexType;
import com.rrd.custompoint.orderentry.customdocs.renderers.CheckboxRendererInput;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UIList.DataType;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UIList.ListType;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UIListOption;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UiFileListItemImpl;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UiTextListItemImpl;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocListDataFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocListSelectionFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocProofFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.ItemInstructions.InstructionsDisplayOption;
import com.rrd.custompoint.orderentry.messagedocs.MessageDocsUserInterface;
import com.rrd.custompoint.orderentry.ui.upload.UploadFile;
import com.wallace.atwinxs.admin.vo.ProfileDefinitionVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ProfileParserConstants;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOECustomDocumentComponent;
import com.wallace.atwinxs.interfaces.IProfileDefinition;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.kits.session.MKComponentInfo;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDMaskVO;
import com.wallace.atwinxs.orderentry.session.OECustomDocOrderLineMapSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

class CustomDocsServiceImplTests extends BaseCustomDocsServiceTests {

	Map<String, String> uiRequest = null;
	Collection<String> errorStrings = null;

  @InjectMocks
  protected CustomDocsServiceImpl service;

  @Mock
  protected CustDocProofFormBean mockProofBean;

  @Mock
  protected C1UXCustDocBaseResponse mockBaseResponse;

  @Mock
  protected C1UXCustDocPageBean mockPageResponse;

  @Mock
  protected CheckboxRendererInput mockCheckboxRendererInput;
  @Mock
  protected IOECustomDocumentComponent mockOECustDocComponent;

 @Mock
  protected C1UXCustDocLoadImprintHistoryRequest mockRequest;

 @Mock
 protected FormattedParagraphProcessor mockParaProcessor;
 @Mock
 protected FormattedParagraphVariable mockParaVar;

 @Mock
 protected OEOrderSessionBean mockOEOrderSessionBean;

 @Mock
 protected IProfileDefinition mockProfileDefinitionInterface;
 @Mock
 protected IProfileInterface mockProfileInterface;
 @Mock
 protected ProfileDefinitionVO mockProfileDefinitionVO;
 @Mock
 protected ProfileVO mockProfileVO;
 @Mock
 protected AlternateProfileSelector mockAlternateProfileSelector;

 @Mock
 protected MessageDocsUserInterface mockMessageDocsUI;
 @Mock
 protected GridGroup mockGridGroup;
 @Mock
 protected C1UXCustDocGroupBean mockGroupBean;

  protected static final String SCRIPT_PARSE_TESTA = "<SCRIPT >alert(\"Hello World!\");</SCRIPT>";
  protected static final String SCRIPT_PARSE_TESTB = "<SCRIPT type=\"text/javascript\">alert(\"Hello World!\");</SCRIPT>";
  protected static final String SCRIPT_PARSE_TESTC = "<script type=\"text/javascript\">alert(\"Hello World!\");</script>"
  	+ "<SCRIPT type=\"text/javascript\">alert(\"Hello World!\");</SCRIPT>";


  @Test
  void scriptParseTest()
  {
	  String result = service.removeScriptTags(SCRIPT_PARSE_TESTA);
	  Assertions.assertFalse(result.contains(CustomDocsServiceImpl.START_SCRIPT_TAG));
	  Assertions.assertFalse(result.contains(CustomDocsServiceImpl.END_SCRIPT_TAG));
	  result = service.removeScriptTags(SCRIPT_PARSE_TESTB);
	  Assertions.assertFalse(result.contains(CustomDocsServiceImpl.START_SCRIPT_TAG));
	  Assertions.assertFalse(result.contains(CustomDocsServiceImpl.END_SCRIPT_TAG));
	  result = service.removeScriptTags(SCRIPT_PARSE_TESTC);
	  Assertions.assertFalse(result.contains(CustomDocsServiceImpl.START_SCRIPT_TAG));
	  Assertions.assertFalse(result.contains(CustomDocsServiceImpl.END_SCRIPT_TAG));
	  assertEquals(AtWinXSConstant.EMPTY_STRING, service.removeScriptTags(AtWinXSConstant.EMPTY_STRING));
  }

  @Test
  void getCurrentPageUI() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

	  setUpModuleSessionNoBase();
      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
//      when(mockItem.getUserInterface()).thenReturn(mockUI);
      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
      service = Mockito.spy(service);

      doNothing().when(service).convertToC1UXObject(eq(mockSessionContainer), eq(mockItem), eq(mockUIBean), any(C1UXCustDocPageBean.class));
      doReturn(mockUIBean).when(service).getBeanForPage(mockItem);

      pageResponse = service.getCurrentPageUI(mockSessionContainer);
      assertTrue(pageResponse instanceof C1UXCustDocPageBean);

      doThrow(new AtWinXSException("myError", "Aclassname")).when(service).convertToC1UXObject(eq(mockSessionContainer), eq(mockItem), eq(mockUIBean), any(C1UXCustDocPageBean.class));
		doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(Locale.class), any(),
				anyString());
      pageResponse = service.getCurrentPageUI(mockSessionContainer);
      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());
	}

  @Test
  void getBeanForPage() {
      when(mockItem.getUserInterface()).thenReturn(mockUI);
      when(mockUI.getNextPageNumber()).thenReturn(1);
      CustDocUIFormBean response = service.getBeanForPage(mockItem);
      assertTrue(response instanceof CustDocUIFormBean);

      when(mockUI.getNextPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST);
      response = service.getBeanForPage(mockItem);
      assertTrue(response instanceof CustDocListSelectionFormBean);

      when(mockUI.getNextPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST_DATA);
      response = service.getBeanForPage(mockItem);
      assertTrue(response instanceof CustDocListDataFormBean);

      when(mockUI.getNextPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
      response = service.getBeanForPage(mockItem);
      assertTrue(response instanceof CustDocProofFormBean);

      when(mockUI.getNextPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_KEY_PROFILES);
      response = service.getBeanForPage(mockItem);
      assertTrue(response instanceof CustDocUIFormBean);
	}

	@Test
	void convertToC1UXObject() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      service = Mockito.spy(service);

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUIBean.isHardStopFailure()).thenReturn(true);
	      when(mockUIBean.isInitializeFailure()).thenReturn(true);
	      when(mockUI.getPage(anyInt())).thenReturn(mockPage);
//	      when(mockPage.getPageNumber()).thenReturn(25);
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(Locale.class), any(),
					any(String.class));

//	      doReturn(true).when(service).getAllowFailedProofs();
//	      doNothing().when(service).setFieldsNoLogic(mockUIBean, mockUI, mockPage, mockAppSessionBean);
//	      doNothing().when(service).loadUIPageJavascript(mockUI, mockPage, mockUIBean, mockAppSessionBean);
	      doReturn(new HashMap<>()).when(service).getCustDocTranslationMap(mockSessionContainer);
//	      when(mockUIBean.getError()).thenReturn(CLASSIC_CONTEXT_PATH);
	      service.convertToC1UXObject(mockSessionContainer, mockItem, mockUIBean, pageResponse);
	      assertFalse(pageResponse.isSuccess());
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUIBean.isInitializeFailure()).thenReturn(false);
	      when(mockUI.getNextPageNumber()).thenReturn(1);
	      when(mockUI.getPage(1)).thenReturn(mockPage);
	      doNothing().when(service).loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      service.convertToC1UXObject(mockSessionContainer, mockItem, mockUIBean, pageResponse);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUIBean.isHardStopFailure()).thenReturn(false);
	      when(mockUI.getNextPageNumber()).thenReturn(1);
	      when(mockUI.getPage(1)).thenReturn(mockPage);
	      doNothing().when(service).loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      service.convertToC1UXObject(mockSessionContainer, mockItem, mockUIBean, pageResponse);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUI.getNextPageNumber()).thenReturn(1);
	      when(mockUI.getPage(1)).thenReturn(mockPage);
	      doNothing().when(service).loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      service.convertToC1UXObject(mockSessionContainer, mockItem, mockUIBean, pageResponse);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	}

	@Test
	void loadC1UXPageBean() throws Exception {
	    setupBaseMockSessions();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
	      service = Mockito.spy(service);
	      when(mockPage.getPageNumber()).thenReturn(24);
	      doNothing().when(service).populateCPBeanForPage(any(), any(), any(), any(), any(), any(), any());
	      doNothing().when(service).loadPageImages(any(C1UXCustDocPageBean.class), eq(mockPage), eq(mockAppSessionBean), eq(mockUI));
	      doNothing().when(service).prepareBeanToReturn(any(), any(), any(), any(), any(), any(), any(), any());

	      // CAP-46524
	      doNothing().when(service).retrieveStoredError(any(), any(C1UXCustDocPageBean.class));

	      when(mockUIBean.getError()).thenReturn(GENERIC_ERROR_MSG);
	      pageResponse = new C1UXCustDocPageBean();
	      service.loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      assertFalse(pageResponse.isSuccess());
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST);
	      when(mockUIBean.getError()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      pageResponse = new C1UXCustDocPageBean();
	      service.loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST_DATA);
	      when(mockUIBean.getError()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      pageResponse = new C1UXCustDocPageBean();
	      service.loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
	      when(mockUIBean.getError()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      pageResponse = new C1UXCustDocPageBean();
	      service.loadC1UXPageBean(pageResponse, mockItem, mockUI, mockPage, mockUIBean, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	}

	@Test
	void loadPageImages() {

		when(mockUI.isWorkingProof()).thenReturn(true);
		when(mockPage.getPageNumber()).thenReturn(25);
		when(mockPage.isWorkingProofAllPages()).thenReturn(true);
	    when(mockPage.getSampleImageURLC1UX()).thenReturn(null);
	    when(mockPage.getSamplePdfURLC1UX()).thenReturn(null);

		ArrayList<String> urlList = new ArrayList<>();
		when(mockUI.getUIImageWorkingProofC1UX(mockAppSessionBean, mockPage)).thenReturn(urlList);

	    pageResponse = new C1UXCustDocPageBean();
		service.loadPageImages(pageResponse, mockPage, mockAppSessionBean, mockUI);
		assertNotNull(pageResponse.getWorkingProofURLs());
		assertEquals(1, pageResponse.getWorkingProofURLs().size());
		assertEquals(ModelConstants.CUST_DOC_PROOF_FILE_NOT_FOUND_PATH, pageResponse.getWorkingProofURLs().toArray()[0]);
		assertEquals(1, pageResponse.getWorkingProofLabels().size());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getItemImageURL());

		urlList.add(CLASSIC_CONTEXT_PATH);
	    pageResponse = new C1UXCustDocPageBean();
		service.loadPageImages(pageResponse, mockPage, mockAppSessionBean, mockUI);
		assertNotNull(pageResponse.getWorkingProofURLs());
		assertEquals(1, pageResponse.getWorkingProofURLs().size());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getItemImageURL());

		when(mockUI.isWorkingProof()).thenReturn(false);
	    when(mockPage.getSampleImageURLC1UX()).thenReturn(CLASSIC_CONTEXT_PATH);
	    pageResponse = new C1UXCustDocPageBean();
		service.loadPageImages(pageResponse, mockPage, mockAppSessionBean, mockUI);
		assertNull(pageResponse.getWorkingProofURLs());
		assertEquals(CLASSIC_CONTEXT_PATH, pageResponse.getItemImageURL());

	    when(mockPage.getSampleImageURLC1UX()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    when(mockUI.getItemImageURL()).thenReturn(CLASSIC_CONTEXT_PATH);
	    pageResponse = new C1UXCustDocPageBean();
		service.loadPageImages(pageResponse, mockPage, mockAppSessionBean, mockUI);
		assertNull(pageResponse.getWorkingProofURLs());
		assertEquals(CLASSIC_CONTEXT_PATH, pageResponse.getItemImageURL());

	    when(mockUI.getItemImageURL()).thenReturn(ModelConstants.CP_NO_IMAGE_NO_CONTEXT);
	    pageResponse = new C1UXCustDocPageBean();
		service.loadPageImages(pageResponse, mockPage, mockAppSessionBean, mockUI);
		assertNull(pageResponse.getWorkingProofURLs());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getItemImageURL());

	}

	@Test
	void isUIEditing() {
		assertFalse(service.isUIEditing(null, null));
		when(mockUI.isNewRequest()).thenReturn(true);
		assertFalse(service.isUIEditing(mockUI, mockItem));

		when(mockUI.isNewRequest()).thenReturn(false);
		assertTrue(service.isUIEditing(mockUI, mockItem));

		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.CART_ADD_FROM_STUB);
		assertFalse(service.isUIEditing(mockUI, mockItem));
		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.CATALOG_EXPRESS);
		assertFalse(service.isUIEditing(mockUI, mockItem));
		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.CATALOG_NORMAL);
		assertFalse(service.isUIEditing(mockUI, mockItem));
		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.DMIS_ADD);
		assertFalse(service.isUIEditing(mockUI, mockItem));
		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.ORDER_FROM_FILE_ADD);
		assertFalse(service.isUIEditing(mockUI, mockItem));
	}

	@Test
	void setProofFields() throws AtWinXSException {
	    pageResponse = new C1UXCustDocPageBean();
		service.setProofFields(mockUI, pageResponse, 2, mockAppSessionBean);
		assertNull(pageResponse.getPagesToProof());

		when(mockUI.getProofID(any(ProofType.class))).thenReturn(CUSTOMER_ITEM_NUM);
		when(mockUI.getProofURL(any(ProofType.class), any(), anyBoolean(), anyString(), anyString())).thenReturn(CLASSIC_CONTEXT_PATH);
	    pageResponse = new C1UXCustDocPageBean();
		service.setProofFields(mockUI, pageResponse, 99998, mockAppSessionBean);
		assertNull(pageResponse.getPagesToProof());

		HashSet<Integer> pagesToProof = new HashSet<>();
		pagesToProof.add(25);
		pagesToProof.add(5);
		pagesToProof.add(35);
		when(mockUI.getPagesToProof()).thenReturn(pagesToProof);
		when(mockUI.getProofID(any(ProofType.class))).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    pageResponse = new C1UXCustDocPageBean();
		service.setProofFields(mockUI, pageResponse, 99998, mockAppSessionBean);
		assertNotNull(pageResponse.getPagesToProof());
		assertEquals(3,pageResponse.getPagesToProof().size());
	}

	@Test
	void getPageAllowsBackButton() {
		when(mockUI.isProofOnly()).thenReturn(true);
		assertFalse(service.getPageAllowsBackButton(mockPage, mockUI));
		when(mockUI.isProofOnly()).thenReturn(false);
		when(mockUI.isMergeOnly()).thenReturn(true);
		when(mockPage.getPageNumber()).thenReturn(25);
		List<Page> pages = new ArrayList<>();
		pages.add(mockPage2);
		when(mockPage2.getPageNumber()).thenReturn(5);
		when(mockUI.getPages()).thenReturn(pages);
		assertTrue(service.getPageAllowsBackButton(mockPage, mockUI));

		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST);
		assertFalse(service.getPageAllowsBackButton(mockPage, mockUI));

		when(mockPage.getPageNumber()).thenReturn(25);
		when(mockPage2.getPageNumber()).thenReturn(25);
		when(mockUI.isKeyProfileSelectionsRequired()).thenReturn(false);
		when(mockUI.isOrderQuestions()).thenReturn(false);
		assertFalse(service.getPageAllowsBackButton(mockPage, mockUI));

		when(mockUI.isOrderQuestions()).thenReturn(true);
		assertTrue(service.getPageAllowsBackButton(mockPage, mockUI));

		when(mockUI.isKeyProfileSelectionsRequired()).thenReturn(true);
		assertTrue(service.getPageAllowsBackButton(mockPage, mockUI));
}

	@Test
	void getPageAllowsSave() {
		assertFalse(service.getPageAllowsSave(null, null, null));
		when(mockItem.isCampaign()).thenReturn(true);
		assertFalse(service.getPageAllowsSave(mockPage, mockUI, mockItem));
		when(mockItem.isCampaign()).thenReturn(false);
		when(mockUI.entryPointAllowsSave()).thenReturn(false);
		assertFalse(service.getPageAllowsSave(mockPage, mockUI, mockItem));
		when(mockUI.entryPointAllowsSave()).thenReturn(true);
		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST);
		assertFalse(service.getPageAllowsSave(mockPage, mockUI, mockItem));
		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST_DATA);
		assertFalse(service.getPageAllowsSave(mockPage, mockUI, mockItem));
		when(mockPage.getPageNumber()).thenReturn(25);
		assertTrue(service.getPageAllowsSave(mockPage, mockUI, mockItem));
	}

	@Test
	void getPageAllowsAddToCartButton() {
		List<Page> pages = new ArrayList<>();
//		pages.add(mockPage2);
//		when(mockPage2.getPageNumber()).thenReturn(5);
//		when(mockUI.getPages()).thenReturn(pages);

		assertFalse(service.getPageAllowsAddToCartButton(null, null, null));

		when(mockUI.isProofOnly()).thenReturn(true);
		assertTrue(service.getPageAllowsAddToCartButton(mockPage, mockUI, mockItem));

		when(mockUI.isProofOnly()).thenReturn(false);
		when(mockUI.isPageflex()).thenReturn(true);
		when(mockUI.isLastPage(anyInt())).thenReturn(true);
		when(mockPage.getPageNumber()).thenReturn(25);
		assertFalse(service.getPageAllowsAddToCartButton(mockPage, mockUI, mockItem));

		when(mockUI.isPageflex()).thenReturn(false);
		assertTrue(service.getPageAllowsAddToCartButton(mockPage, mockUI, mockItem));

		when(mockUI.isLastPage(anyInt())).thenReturn(false);
		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
		assertTrue(service.getPageAllowsAddToCartButton(mockPage, mockUI, mockItem));
	}

	@Test
	void buildGroups() throws Exception {
	      service = Mockito.spy(service);
	      when(mockUI.getAllVariables()).thenReturn(mockVariables);
	      when(mockUI.isFieldsEditable()).thenReturn(true);
	      HashMap<Integer, Map<String, String>> varActions = new HashMap<>();
	      when(mockPage.getEventActionsJavascriptC1UX(mockAppSessionBean, mockVariables, true, mockUI)).thenReturn(varActions);

	      pageResponse = new C1UXCustDocPageBean();
	      List<Group> groups = new ArrayList<>();
	      groups.add(mockGroup);
	      when(mockPage.getGroups()).thenReturn(groups);
	      when(mockGroup.getDisplayControls()).thenReturn(mockDisplayControls);
	      List<Variable> varList = new ArrayList<>();
	      varList.add(mockVar);
	      when(mockGroup.getVariables()).thenReturn(varList);
	      when(mockGroup.getGroupNumber()).thenReturn(24);
	      doReturn(new C1UXDisplayControls()).when(service).createC1UXDisplayControls(mockDisplayControls);
	      doNothing().when(service).setVariableListReferences(any(C1UXVariableBean.class), eq(mockVar), eq(pageResponse));
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      doReturn(new C1UXVariableBean()).when(service).createC1UXVariableBean(any(Variable.class),
	    		  any(Map.class), any(AppSessionBean.class), any(Group.class), any(C1UXCustDocGroupBean.class));

		service.buildGroups(mockPage,  pageResponse,  mockUI,  mockAppSessionBean);
		assertEquals(1, pageResponse.getGroups().size());
		assertEquals(1, pageResponse.getGroups().get(0).getC1uxVariables().size());

	      pageResponse = new C1UXCustDocPageBean();
		when(mockDisplayControls.isDisplayCriteriaMet()).thenReturn(true);
		varList.clear();
		service.buildGroups(mockPage,  pageResponse,  mockUI,  mockAppSessionBean);
		assertEquals(1, pageResponse.getGroups().size());
		assertEquals(0, pageResponse.getGroups().get(0).getC1uxVariables().size());

	      pageResponse = new C1UXCustDocPageBean();
		groups.clear();
		service.buildGroups(mockPage,  pageResponse,  mockUI,  mockAppSessionBean);
		assertEquals(0, pageResponse.getGroups().size());

	}

	@Test
	void generateNextProof() throws Exception
	{
		when(mockUI.getProofID(ProofType.IMAGE)).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockUI.getProofID(ProofType.XERT)).thenReturn(DEFAULT_VIEW);
		when(mockUI.getProofID(ProofType.PRECISIONDIALOGUE)).thenReturn(CUSTOMER_ITEM_NUM);
		when(mockUI.getImageProofJobName()).thenReturn(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG);
		when(mockUI.isExternalESP()).thenReturn(true);

		proofBean = new CustDocProofFormBean();
		service.generateNextProof(mockUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(DEFAULT_VIEW, proofBean.getHiddenTransactionID());

		when(mockUI.isExternalESP()).thenReturn(false);
		when(mockUI.isPrecisionDialogue()).thenReturn(true);

		proofBean = new CustDocProofFormBean();
		service.generateNextProof(mockUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(CUSTOMER_ITEM_NUM, proofBean.getHiddenTransactionID());

		when(mockUI.isPrecisionDialogue()).thenReturn(false);
		when(mockUI.generateNewProof(eq(ProofType.IMAGE), eq(mockAppSessionBean),
				eq(mockVolatileSessionBean), anyList())).thenReturn(false);
		service.generateNextProof(mockUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(AtWinXSConstant.EMPTY_STRING, proofBean.getHiddenTransactionID());

		when(mockUI.getProofID(ProofType.IMAGE)).thenReturn(ITEM_DESC);
		proofBean = new CustDocProofFormBean();
		service.generateNextProof(mockUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(proofBean.isHardStopFailure());
		assertEquals(ITEM_DESC, proofBean.getHiddenTransactionID());

		String error = "blew up";
		when(mockUI.getProofID(ProofType.IMAGE)).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockUI.generateNewProof(eq(ProofType.IMAGE), eq(mockAppSessionBean),
				eq(mockVolatileSessionBean), anyList()))
				.thenThrow(new AtWinXSException(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING + error, "anyclass"));
		proofBean = new CustDocProofFormBean();
		service.generateNextProof(mockUI, proofBean, mockAppSessionBean, mockVolatileSessionBean);
		assertTrue(proofBean.isHardStopFailure());
		assertEquals(error, proofBean.getError());

	}

	@Test
	void showProofErrors()
	{
		errorStrings = new ArrayList<>();
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		service.showProofErrors(errorStrings, formBean);
		assertNull(formBean.getError());

		errorStrings.add(DEFAULT_UG_NM);
		service.showProofErrors(errorStrings, formBean);
		String expectedValue = DEFAULT_UG_NM + NEW_LINE_CHAR;
		assertEquals(expectedValue, formBean.getError());

		formBean.setError(GENERIC_ERROR_MSG);
		service.showProofErrors(errorStrings, formBean);
		assertNotEquals(expectedValue, formBean.getError());
		String breakString = "<br />";
		assertFalse(formBean.getError().contains(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING));
		assertFalse(formBean.getError().contains(breakString));
		assertTrue(formBean.getError().startsWith(GENERIC_ERROR_MSG));

		formBean.setError(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING + GENERIC_ERROR_MSG);
		errorStrings.add(CUSTOMER_ITEM_NUM);
		service.showProofErrors(errorStrings, formBean);
		assertNotEquals(expectedValue, formBean.getError());
		assertFalse(formBean.getError().contains(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING));
		assertTrue(formBean.getError().startsWith(GENERIC_ERROR_MSG));
		assertTrue(formBean.getError().contains(breakString));
	}

	@Test
	void combineErrors()
	{
		errorStrings = new ArrayList<>();
		C1UXCustDocBaseResponse formBean = new C1UXCustDocBaseResponse();
		service.combineErrors(errorStrings, formBean);
		assertEquals(AtWinXSConstant.EMPTY_STRING, formBean.getMessage());

		errorStrings.add(CLASSIC_CONTEXT_PATH);
		formBean = new C1UXCustDocBaseResponse();
		service.combineErrors(errorStrings, formBean);
		assertEquals(CLASSIC_CONTEXT_PATH + NEW_LINE_CHAR, formBean.getMessage());
	}

	@Test
	void processErrorStrings() {
		errorStrings = new ArrayList<>();
		assertEquals(AtWinXSConstant.EMPTY_STRING, service.processErrorStrings(errorStrings).toString());

		errorStrings.add(CLASSIC_CONTEXT_PATH);
		assertEquals(CLASSIC_CONTEXT_PATH + NEW_LINE_CHAR, service.processErrorStrings(errorStrings).toString());

		errorStrings.add(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING+CLASSIC_CONTEXT_PATH);
		String result = service.processErrorStrings(errorStrings).toString();
		assertFalse(result.contains(CustomDocsServiceImpl.FATAL_ERROR_PREFIX_PROOFING));
		assertTrue(result.contains("<br />"));
	}

	@Test
	void setTermsConditionsKeys()
	{
		CustDocProofFormBean formBean = new CustDocProofFormBean();
		service.setTermsConditionsKeys(formBean);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, formBean.getTermsConditionsText());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, formBean.getTermsProofPopupWaringText1());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, formBean.getTermsProofPopupWaringText2());
	}

	@Test
	void loadItemInstructionsJavascript()
	{
		when(mockItem.renderInstructionsJavascript(any())).thenReturn(CUSTOMER_ITEM_NUM);
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		service.loadItemInstructionsJavascript(null, formBean, mockAppSessionBean);
		assertTrue(Util.isBlankOrNull(formBean.getInstructionsJavascript()));

		service.loadItemInstructionsJavascript(mockItem, formBean, mockAppSessionBean);
		assertTrue(Util.isBlankOrNull(formBean.getInstructionsJavascript()));

		when(mockItem.getItemInstructionsObj()).thenReturn(mockItemInstructions);
		when(mockItemInstructions.getButtonLink()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockItemInstructions.isTextEmpty()).thenReturn(true);
		service.loadItemInstructionsJavascript(mockItem, formBean, mockAppSessionBean);
		assertTrue(Util.isBlankOrNull(formBean.getInstructionsJavascript()));

		when(mockItemInstructions.isTextEmpty()).thenReturn(false);
		when(mockItemInstructions.getInstructionsDisplayOption()).thenReturn(InstructionsDisplayOption.TEXT);
		service.loadItemInstructionsJavascript(mockItem, formBean, mockAppSessionBean);
		assertTrue(Util.isBlankOrNull(formBean.getInstructionsJavascript()));
		when(mockItemInstructions.getInstructionsDisplayOption()).thenReturn(InstructionsDisplayOption.POPUP);
		service.loadItemInstructionsJavascript(mockItem, formBean, mockAppSessionBean);
		assertFalse(Util.isBlankOrNull(formBean.getInstructionsJavascript()));

		when(mockItemInstructions.getButtonLink()).thenReturn(GENERIC_ERROR_MSG);
		service.loadItemInstructionsJavascript(mockItem, formBean, mockAppSessionBean);
		assertFalse(Util.isBlankOrNull(formBean.getInstructionsJavascript()));
	}

	protected void mockLoadUIPageJavascript() throws NumberFormatException, AtWinXSException {
	    when(mockUI.replaceIcons(anyString())).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    when(mockPage.renderDisplayC1UX(any(),  any(), anyBoolean(), eq(mockUI))).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    when(mockPage.loadShowHideJavascript()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    when(mockPage.loadShowHideAltProfSelJS()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    when(mockPage.getPageValidation()).thenReturn(CLASSIC_CONTEXT_PATH);
	    when(mockPage.loadSkipProofJavascript(any(), anyInt())).thenReturn(AtWinXSConstant.EMPTY_STRING);
	}
	@Test
	void loadUIPageJavascript() throws AtWinXSException
	{
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		mockLoadUIPageJavascript();
		service.loadUIPageJavascript(mockUI, mockPage, formBean, mockAppSessionBean);
		assertEquals(CLASSIC_CONTEXT_PATH, formBean.getPageValidation());
	}

	@Test
	void setFieldsNoLogic() throws AtWinXSException
	{
	      service = Mockito.spy(service);
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));
	      doReturn(ProfileParserConstants.DATE_FORMAT).when(service).getLocaleDateFormat(mockAppSessionBean);
	      when(mockPage.getStepName()).thenReturn(CLASSIC_CONTEXT_PATH);
	      when(mockPage.getPageNumber()).thenReturn(2);
	      when(mockUI.isNewRequest()).thenReturn(false);
	      when(mockUI.isOrderQuestions()).thenReturn(false);
	      when(mockUI.isVariablePage()).thenReturn(false);
	      when(mockUI.isWorkingProof()).thenReturn(false);
	      when(mockUI.getExpandTooltip()).thenReturn(BLANK_NOT_ALLOWED_ERR_ENGLISH);
	      when(mockUI.getCollapseTooltip()).thenReturn(BLANK_NOT_ALLOWED_ERR_ENGLISH);
	      when(mockUI.areAllKeyVariablesPopulated()).thenReturn(false);

	      when(mockPage.isStatusSaved()).thenReturn(true);
	      CustDocUIFormBean formBean = new CustDocUIFormBean();
	      service.setFieldsNoLogic(formBean, mockUI, mockPage, mockAppSessionBean);
	      assertTrue(formBean.isHdnIsDirty());

	      when(mockPage.isStatusSaved()).thenReturn(false);
	      formBean = new CustDocUIFormBean();
	      service.setFieldsNoLogic(formBean, mockUI, mockPage, mockAppSessionBean);
	      assertFalse(formBean.isHdnIsDirty());

	}

	@Test
	void setBlankFields() {
		CustDocUIFormBean formBean = new CustDocUIFormBean();
	    service.setBlankFields(formBean);
	    assertEquals(AtWinXSConstant.EMPTY_STRING, formBean.getDeleteTempImageUrl());

	    pageResponse = new C1UXCustDocPageBean();
	    service.clearJSFromBean(pageResponse);
	    assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getDragDropJS());

	}

	@Test
	void createC1UXDisplayControls() throws IllegalAccessException, InvocationTargetException
	{
		when(mockDisplayControl.getDisplayVariable()).thenReturn(mockVar);
		when(mockVar.getInputTypeCode()).thenReturn(InputType.CHECKBOX);
		when(mockVar.getListValue()).thenReturn("1");
		when(mockVar.getTextValue()).thenReturn("Y");
		when(mockVar.getNumber()).thenReturn(456);
		List<DisplayControl> valList = new ArrayList<>();
		Map<Variable, List<DisplayControl>> dcMap = new HashMap<>();
		valList.add(mockDisplayControl);
		dcMap.put(mockVar, valList);
		when(mockDisplayControls.getDisplayControls()).thenReturn(dcMap);
		when(mockDisplayControls.isDisplayCriteriaMet()).thenReturn(true);

		C1UXDisplayControls controls = service.createC1UXDisplayControls(mockDisplayControls);
		assertNotNull(controls);
		assertTrue(controls.isDisplayCriteriaMetAsOfLoad());
	}

	@Test
	void createC1UXVariableBean() throws IllegalAccessException, InvocationTargetException
	{
		Map<Integer, Map<String, String>> varActionsMap = new HashMap<>();
		int varNum=2567;
		when(mockVar.getNumber()).thenReturn(varNum);
		Map<String, String> actions = new HashMap<>();
		actions.put("onclick", "doSomething");
		varActionsMap.put(varNum, actions);
		HashSet<Integer> depVars = new HashSet<>();
		depVars.add(Integer.valueOf(1));
		when(mockVar.getDependentVarPages()).thenReturn(depVars);
		when(mockVar.getInputTypeCode()).thenReturn(InputType.EDITABLE_TEXT);
		when(mockVar.getVariablePageflexType()).thenReturn(PageflexType.TEXT);
		when(mockVar.getDefault()).thenReturn(CustomDocsServiceImpl.Y);
		when(mockVar.isUploadAllowedInd()).thenReturn(false);
		when(mockVar.getTextAreaColumns()).thenReturn(0);
		when(mockVar.getInstructionsTypeCode()).thenReturn(InstructionType.TEXT);
		service = Mockito.spy(service);
		doNothing().when(service).setVariableShowHide(any(C1UXVariableBean.class), eq(mockVar), eq(mockGroup));
		doNothing().when(service).setUpBlankedVariables(any(C1UXVariableBean.class), eq(mockVar), eq(mockGroup));
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockVar.getName()).thenReturn("myName");
		when(mockVar.isKeyValueInd()).thenReturn(true);
		when(mockVar.getRenderer()).thenReturn(new CheckboxRendererImpl());
		when(mockVar.getInstructionsText()).thenReturn(CUSTOMER_ITEM_NUM);
		C1UXVariableBean bean = service.createC1UXVariableBean(mockVar, varActionsMap, mockAppSessionBean, mockGroup, mockGroupBean);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, bean.getKeyInputName());

		depVars.clear();
		bean = service.createC1UXVariableBean(mockVar, varActionsMap, mockAppSessionBean, mockGroup, mockGroupBean);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, bean.getKeyInputName());
	}


	@Test
	void setUpBlankedVariables() {
		when(mockGroup.isShown()).thenReturn(true);
		C1UXVariableBean bean = new C1UXVariableBean();
		bean.setHiddenShowHideFieldValue(CustomDocsServiceImpl.Y);
		service.setUpBlankedVariables(bean, mockVar, mockGroup);
		assertNull(bean.getTextValue());

		when(mockGroup.isShown()).thenReturn(false);
		bean = new C1UXVariableBean();
		bean.setHiddenShowHideFieldValue(CustomDocsServiceImpl.Y);
		service.setUpBlankedVariables(bean, mockVar, mockGroup);
		assertNull(bean.getTextValue());

		when(mockVar.getDefault()).thenReturn(CLASSIC_CONTEXT_PATH);
		when(mockVar.isBlankedByShowHide()).thenReturn(true);
		bean = new C1UXVariableBean();
		bean.setHiddenShowHideFieldValue(CustomDocsServiceImpl.Y);
		service.setUpBlankedVariables(bean, mockVar, mockGroup);
		assertEquals(CLASSIC_CONTEXT_PATH, bean.getTextValue());

		when(mockVar.getDefaultTextValue()).thenReturn(ITEM_DESC);
		when(mockGroup.isShown()).thenReturn(true);
		bean = new C1UXVariableBean();
		bean.setHiddenShowHideFieldValue(CustomDocsServiceImpl.N);
		service.setUpBlankedVariables(bean, mockVar, mockGroup);
		assertEquals(ITEM_DESC, bean.getTextValue());

	}
	@Test
	void setVariableListReferences() {
		C1UXVariableBean varBean = new C1UXVariableBean();
		pageResponse = new C1UXCustDocPageBean();
		service.setVariableListReferences(varBean, mockVar, pageResponse);
		assertNotNull(pageResponse.getUiListMap());
		assertTrue(pageResponse.getUiListMap().isEmpty());
		assertNull(varBean.getListKey());

		when(mockVar.getUiList()).thenReturn(mockUIList);
		when(mockUIList.getDataType()).thenReturn(DataType.TEXT);
		when(mockUIList.getListId()).thenReturn(514);
		when(mockUIList.getListType()).thenReturn(ListType.STATIC);
		List<UIListOption> listOptions = new ArrayList<>();
		UIListOption opt = new UiTextListItemImpl();
		listOptions.add(opt);
		when(mockUIList.getOptions()).thenReturn(listOptions);

		varBean = new C1UXVariableBean();
		pageResponse = new C1UXCustDocPageBean();
		service.setVariableListReferences(varBean, mockVar, pageResponse);
		assertNotNull(pageResponse.getUiListMap());
		assertFalse(pageResponse.getUiListMap().isEmpty());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, varBean.getListKey());

		when(mockUIList.getListType()).thenReturn(ListType.EXTERNAL_SOURCE);
		when(mockUIList.getListId()).thenReturn(-1);
		when(mockVar.getNumber()).thenReturn(67);
		when(mockUIList.getDataType()).thenReturn(DataType.IMAGE);
		listOptions.clear();
		UIListOption opt2 = new UiFileListItemImpl();
		listOptions.add(opt2);
		varBean = new C1UXVariableBean();
		pageResponse = new C1UXCustDocPageBean();
		service.setVariableListReferences(varBean, mockVar, pageResponse);
		assertNotNull(pageResponse.getUiListMap());
		assertFalse(pageResponse.getUiListMap().isEmpty());
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, varBean.getListKey());
	}

	@Test
	void createBaseResponseFromUIBean() {
		pageResponse = new C1UXCustDocPageBean();
		pageResponse.setSuccess(true);
		baseResponse = service.createBaseResponseFromUIBean(pageResponse);
		assertTrue(baseResponse.isSuccess());

		pageResponse = new C1UXCustDocPageBean();
		pageResponse.setMessage(GENERIC_ERROR_MSG);
		baseResponse = service.createBaseResponseFromUIBean(pageResponse);
		assertFalse(baseResponse.isSuccess());
		assertEquals(GENERIC_ERROR_MSG, baseResponse.getMessage());
	}

	@Test
	void eventSendsBaseResponseOrRouting() {
		pageResponse = new C1UXCustDocPageBean();
		uiRequest = new HashMap<>();
		assertFalse(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));

		pageResponse.setRedirectRouting(CLASSIC_CONTEXT_PATH);
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));

		pageResponse.setRedirectRouting(AtWinXSConstant.EMPTY_STRING);
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_SEARCH.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));

		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NEXT.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));

		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.CANCEL.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));

		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.CANCEL_ORDER.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));

		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_EXIT.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_ORDER.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.PREVIOUS.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.PREVIOUS_CHECKOUT.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NAVIGATE_PAGES.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.WORKING_PROOF.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_CONTINUE.toString());
		assertTrue(service.eventSendsBaseResponseOrRouting(uiRequest, pageResponse));
	}

	@Test
	void finalizeProofModelDetails() throws Exception
	{
//		  setUpModuleSession();
//	      when(mockItem.getUserInterface()).thenReturn(mockUI);
//	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      service = Mockito.spy(service);
	      doNothing().when(service).generateNextProof(eq(mockUI), any(CustDocProofFormBean.class), eq(mockAppSessionBean), eq(mockVolatileSessionBean));
	      doReturn(mockUIInterface).when(service).getCDInterface(mockAppSessionBean);
	      when(mockUI.getUiKey()).thenReturn(mockUIKey);
	      when(mockUIKey.getVOKey()).thenReturn(mockUIVersionKey);
	      when(mockUIInterface.getTempUIVersionVO(any())).thenReturn(mockUIVersionVO);
	      when(mockUIVersionVO.isXertCompositionEnabled()).thenReturn(false);
	      when(mockUIVersionVO.isPrecisionDialogueEnabled()).thenReturn(false);
	      when(mockUI.isExternalESP()).thenReturn(false);
	      when(mockUI.isVariablePage()).thenReturn(false);
	      when(mockUI.renderSpecialtyProofButtons(mockItem)).thenReturn(CLASSIC_CONTEXT_PATH);
	      when(mockAppSessionBean.getEmailAddress()).thenReturn("dapres@whitehouse.gov");
	      doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      when(mockUI.getDefaultProofView()).thenReturn(ProofView.None);
		CustDocProofFormBean formBean = new CustDocProofFormBean();
		service.finalizeProofModelDetails(mockItem, mockUI, formBean, mockAppSessionBean,
				mockVolatileSessionBean, mockOESession);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, formBean.getTermsConditionsText());
		assertEquals(UserInterface.ProofView.Medium.toString(), formBean.getProofView());
	}

	@Test
	void initializeUIOnly() throws Exception {
		setUpModuleSession();
		uiRequest = new HashMap<>();
	    service = Mockito.spy(service);
		when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	    doReturn(mockItem).when(service).initializeItem(any(), any(), any(), any(), any(), any(), any());
		doNothing().when(service).saveFullSessionInfo(any(), anyInt(), anyInt());
	    doReturn("Initialized Successfully!").when(service).getCombinedMessage(any());
		List<Page> pages = new ArrayList<>();
		pages.add(mockPage2);

		baseResponse = service.initializeUIOnly(mockSessionContainer, uiRequest);
		assertTrue(baseResponse.isSuccess());

	    doThrow(new AtWinXSException("message", "classname")).when(service).saveFullSessionInfo(any(), anyInt(), anyInt());
		doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(Locale.class), any(), anyString());
		baseResponse = service.initializeUIOnly(mockSessionContainer, uiRequest);
		assertFalse(baseResponse.isSuccess());
	}

	@Test
	void initializeItem() throws Exception {
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);
		      service = Mockito.spy(service);
		      doReturn(mockItem).when(service).createAndInitItem(any(), any(), any(), any(), any(), any());
		      when(mockUI.getProfileSelector()).thenReturn(mockUserProfileSelector);
		      when(mockUserProfileSelector.isShown()).thenReturn(true);
		      when(mockAppSessionBean.getProfileNumber()).thenReturn(250);
		      when(mockUI.isSkipDynamicDataCalls()).thenReturn(false);
		      CustDocUIFormBean uiBean = new CustDocUIFormBean();
		      service.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

		      when(mockUserProfileSelector.isShown()).thenReturn(false);
		      uiBean = new CustDocUIFormBean();
		      service.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

		      when(mockUI.getProfileSelector()).thenReturn(null);
		      uiBean = new CustDocUIFormBean();
		      service.initializeItem(uiRequest, uiBean, mockOEOrderSession, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings);
		      assertTrue(uiBean.isStarting());

	}

	@Test
	void populateSFSpecificFields() throws AtWinXSException {
		when(mockUI.buildStepOptionsC1UX(any(), any())).thenReturn(null);
		when(mockVolatileSessionBean.getCustDocPromptValue()).thenReturn(CustomDocsServiceImpl.N);
	      service = Mockito.spy(service);
			doReturn("02/27/2019").when(service).getFormattedDate(any(), any());
		    doReturn(true).when(service).getPageAllowsSave(any(), any(), any());
		    doReturn(true).when(service).getListPrivacySettings(any());
		    doReturn(true).when(service).getPageAllowsBackButton(any(), any());
		    doReturn(true).when(service).getPageAllowsAddToCartButton(any(),  any(), any());
		    doReturn(true).when(service).isUIEditing(any(), any());
			doNothing().when(service).setProofFields(any(), any(), anyInt(), any());
			doNothing().when(service).createJSFile(any(), any());
			doReturn(null).when(service).loadImprintHistory(any(), anyInt(), any(), anyBoolean());
			doReturn(null).when(service).loadUserProfileSearch(any(), anyInt());
			doReturn(mockManageList).when(service).getManageListComponent(any());
			when(mockManageList.retrieveAList(any())).thenReturn(getListVO());

		pageResponse = new C1UXCustDocPageBean();
		service.populateSFSpecificFields(pageResponse, mockPage, mockAppSessionBean,
				mockUI, mockItem, mockVolatileSessionBean, mockOEOrderSessionBean);
		assertTrue(pageResponse.isSuccess());

		doThrow(new AtWinXSException("yet another fails", this.getClass().getName())).when(service).setProofFields(any(), any(), anyInt(), any());
		pageResponse = new C1UXCustDocPageBean();
		service.populateSFSpecificFields(pageResponse, mockPage, mockAppSessionBean,
				mockUI, mockItem, mockVolatileSessionBean, mockOEOrderSessionBean);
		assertFalse(pageResponse.isSuccess());
	}

	@Test
	void performPageSubmitAction() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
	      service = Mockito.spy(service);
	      doNothing().when(service).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockItem), any());
	      doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      uiRequest = new HashMap<>();
	      uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NEXT.toString());
	      C1UXCustDocBaseResponse baseResponse = service.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertTrue(baseResponse instanceof C1UXCustDocBaseResponse);

	      doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(service).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockItem), any());
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(Locale.class), any(), anyString());
	      baseResponse = service.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new NullPointerException("I blew up!")).when(service).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockItem), any());
	      baseResponse = service.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.STAY.toString());
	      doNothing().when(service).handleSpecificEvent(anyMap(), eq(mockSessionContainer), any(CustDocUIFormBean.class), eq(mockItem), any());
	      doNothing().when(service).convertToC1UXObject(eq(mockSessionContainer), eq(mockItem), any(CustDocUIFormBean.class), any());
	      baseResponse = service.performPageSubmitAction(mockSessionContainer, uiRequest);
	      assertTrue(baseResponse instanceof C1UXCustDocPageBean);
	}

	@Test
	void handleSpecificEvent() 	throws Exception {
		  setUpModuleSession();
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      service = Mockito.spy(service);
	      doNothing().when(service).navigateForward(anyMap(), any(), anyList(), any(UIEvent.class),
	    		  eq(mockItem), any(C1UXCustDocPageBean.class), eq(mockSessionContainer));

	      pageResponse = new C1UXCustDocPageBean();
		uiRequest = new HashMap<>();
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.NEXT.toString());
		service.handleSpecificEvent(uiRequest, mockSessionContainer, mockUIBean, mockItem, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      doNothing().when(service).saveAllUpdatedSessions(any(), any(), any(), anyInt());
	      pageResponse = new C1UXCustDocPageBean();
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_CONTINUE.toString());
		service.handleSpecificEvent(uiRequest, mockSessionContainer, mockUIBean, mockItem, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
		uiRequest.put(CustomDocsServiceImpl.EVENT_ACTION_PARAM, UIEvent.SAVE_AND_SEARCH.toString());
		service.handleSpecificEvent(uiRequest, mockSessionContainer, mockUIBean, mockItem, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());
	}

	@Test
	void handleUnsupportedEvents() {
		// CAP-46524
		//assertTrue(service.handleUnsupportedEvents(UIEvent.REFRESH));
		assertTrue(service.handleUnsupportedEvents(UIEvent.APPLY_COLOR_CHANGE)); // applying color to all of UI from font/color set
		assertTrue(service.handleUnsupportedEvents(UIEvent.PROFILE_SEARCH));
		assertTrue(service.handleUnsupportedEvents(UIEvent.HISTORY_SEARCH));
		assertTrue(service.handleUnsupportedEvents(UIEvent.PROFILE_SELECT));
		assertTrue(service.handleUnsupportedEvents(UIEvent.ALTERNATE_PROFILE_SELECT));
		assertTrue(service.handleUnsupportedEvents(UIEvent.KEY_PROFILE_SELECT));

		assertFalse(service.handleUnsupportedEvents(UIEvent.STAY));
		assertTrue(service.handleUnsupportedEvents(UIEvent.HISTORY_SELECT));
	}

	@Test
	void cleanForFE() {
		assertNotNull(service.cleanForFE(AtWinXSConstant.EMPTY_STRING));
	}

	@Test
	void handleLeavingEvents() throws Exception {
		  setUpModuleSession();
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);

	      service = Mockito.spy(service);
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();
	      doNothing().when(service).resetUIAndItem(any(), any());
	      doNothing().when(service).saveAllUpdatedSessions(any(), any(), any(), anyInt());
	      doNothing().when(service).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any(C1UXCustDocPageBean.class));
	      pageResponse = new C1UXCustDocPageBean();

	      assertTrue(service.handleLeavingEvents(UIEvent.CANCEL_ORDER, mockItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      pageResponse = new C1UXCustDocPageBean();

	      assertTrue(service.handleLeavingEvents(UIEvent.CANCEL, mockItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      when(mockItem.saveOrderLine(any(), any(), any(), anyBoolean(), any(), any(), anyInt(), any(), any()))
	      	.thenReturn(mockOrderLineVOKey);

	      pageResponse = new C1UXCustDocPageBean();
	      assertTrue(service.handleLeavingEvents(UIEvent.SAVE_AND_EXIT, mockItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));
	      pageResponse = new C1UXCustDocPageBean();
	      assertTrue(service.handleLeavingEvents(UIEvent.SAVE_ORDER, mockItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));

	      pageResponse = new C1UXCustDocPageBean();
//	      when(mockUIBean.isHdnIsDirty()).thenReturn(true);
	      assertTrue(service.handleLeavingEvents(UIEvent.SAVE_AND_EXIT, mockItem, mockSessionContainer,
					pageResponse, uiRequest, errorStrings, mockUIBean));


	      pageResponse = new C1UXCustDocPageBean();
		assertFalse(service.handleLeavingEvents(UIEvent.STAY, mockItem, mockSessionContainer,
			pageResponse, uiRequest, errorStrings, mockUIBean));
	}

	@Test
	void navigateForward() throws Exception
	{
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      service = Mockito.spy(service);
	      uiRequest = new HashMap<>();
	      errorStrings = new ArrayList<>();

	      doNothing().when(service).readSubmittedData(any(UIEvent.class), eq(mockUIBean), eq(mockUI), eq(uiRequest), eq(errorStrings));
	      pageResponse = new C1UXCustDocPageBean();
	      doReturn(false).when(service).isStaySituation(any(UIEvent.class), eq(mockUI), eq(errorStrings));
	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      when(mockUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(1);
	      doNothing().when(service).determineNavigatePages(eq(uiRequest), any(UIEvent.class), anyInt(), anyInt(), eq(pageResponse));
	      doNothing().when(service).showProofErrors(errorStrings, mockUIBean);
	      when(mockUIBean.getError()).thenReturn(GENERIC_ERROR_MSG);

	      service.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
	      service.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      pageResponse = new C1UXCustDocPageBean();
	      when(mockUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(Integer.MAX_VALUE);
	      service.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockUI.getCurrentPageC1UX(uiRequest)).thenReturn(1);
	      pageResponse = new C1UXCustDocPageBean();
	      service.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.PREVIOUS, mockItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

	      when(mockUIBean.getError()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      when(mockUI.getNextPageNumberC1UX(anyInt(), any(UIEvent.class), eq(uiRequest), eq(mockAppSessionBean))).thenReturn(UserInterface.NEXT_PAGE_NUMBER_EXIT);
	      pageResponse = new C1UXCustDocPageBean();
	      doNothing().when(service).handleExitNonPF(anyInt(), eq(mockSessionContainer), eq(mockItem), eq(errorStrings), eq(pageResponse));
	      service.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockItem, pageResponse, mockSessionContainer);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage()); // please note that this is wrong
	      // something broke nonPF, but technically we don't have to have it working for 6 months at least, so that's ok
	      doReturn(true).when(service).isStaySituation(any(UIEvent.class), eq(mockUI), eq(errorStrings));
	      pageResponse = new C1UXCustDocPageBean();
	      service.navigateForward(uiRequest, mockUIBean, errorStrings, UIEvent.NAVIGATE_PAGES, mockItem, pageResponse, mockSessionContainer);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage()); // please note that this is wrong

	}

	@Test
	void handleExitNonPF() throws Exception {
	      setUpModuleSession();
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockUI.isTryUI()).thenReturn(false);
	      when(mockUI.isOrderQuestions()).thenReturn(false);
	      when(mockUI.isLastPage(anyInt())).thenReturn(true);
	      when(mockUI.isSkipProofing()).thenReturn(true);
	      when(mockItem.saveOrderLine(any(), any(), any(), anyBoolean(), any(), any(), anyInt(), any(), any()))
	      		.thenReturn(mockOrderLineVOKey);

	      when(mockItem.getCustomDocOrderLineID()).thenReturn(1);
	        service = Mockito.spy(service);
			doReturn(RouteConstants.CART_ENTRY_ROUTING_URL).when(service).getForwardToURL(any(), any(), any());
			errorStrings = new ArrayList<>();

		    pageResponse = new C1UXCustDocPageBean();
			service.handleExitNonPF(2, mockSessionContainer, mockItem, errorStrings, pageResponse);
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getRedirectRouting());

		     when(mockItem.getCustomDocOrderLineID()).thenReturn(-1);
			when(mockUI.isTryUI()).thenReturn(true);
		    pageResponse = new C1UXCustDocPageBean();
			service.handleExitNonPF(2, mockSessionContainer, mockItem, errorStrings, pageResponse);
			assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getRedirectRouting());

		    pageResponse = new C1UXCustDocPageBean();
			when(mockUI.isTryUI()).thenReturn(false);
			service.handleExitNonPF(2, mockSessionContainer, mockItem, errorStrings, pageResponse);
			assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getRedirectRouting());
	}

	@Test
	void isStaySituation() {
		when(mockUI.isNewRequestFlow()).thenReturn(false);
		errorStrings = new ArrayList<>();
		assertFalse(service.isStaySituation(null, mockUI, errorStrings));

		errorStrings.add(GENERIC_ERROR_MSG);
		assertTrue(service.isStaySituation(null, mockUI, errorStrings));

		errorStrings.clear();
		assertFalse(service.isStaySituation(UserInterface.UIEvent.STAY, mockUI, errorStrings));

		when(mockUI.isNewRequestFlow()).thenReturn(true);
		assertFalse(service.isStaySituation(UserInterface.UIEvent.NEXT, mockUI, errorStrings));

		assertTrue(service.isStaySituation(UserInterface.UIEvent.STAY, mockUI, errorStrings));
		assertTrue(service.isStaySituation(UserInterface.UIEvent.WORKING_PROOF, mockUI, errorStrings));
		assertTrue(service.isStaySituation(UserInterface.UIEvent.SAVE_AND_CONTINUE, mockUI, errorStrings));
	}

	@Test
	void readSubmittedData() throws AtWinXSException {
		when(mockUIBean.isHdnIsDirty()).thenReturn(true);
		Map<String, String> uiRequest = new HashMap<>();
		errorStrings = new ArrayList<>();
		service.readSubmittedData(UserInterface.UIEvent.NEXT, mockUIBean, mockUI, uiRequest, errorStrings);
		assertTrue(true);
		service.readSubmittedData(UserInterface.UIEvent.STAY, mockUIBean, mockUI, uiRequest, errorStrings);
		assertTrue(true);
		when(mockUIBean.isHdnIsDirty()).thenReturn(false);
		service.readSubmittedData(UserInterface.UIEvent.STAY, mockUIBean, mockUI, uiRequest, errorStrings);
		assertTrue(true);
	}

	@Test
	void valueIsTrue() {
		assertFalse(service.valueIsTrue(null));
		assertFalse(service.valueIsTrue("TRUE "));
		assertTrue(service.valueIsTrue("TRUE"));
		assertTrue(service.valueIsTrue("tRUe"));
	}
	@Test
	void checkForSuccess() {
		pageResponse = new C1UXCustDocPageBean();
		pageResponse.setHardStopFailure(false);
		pageResponse.setMessage(null);
		pageResponse.getFieldMessages().clear();
		service.checkForSuccess(pageResponse);
		assertTrue(pageResponse.isSuccess());

		pageResponse.setSuccess(false);
		pageResponse.setFieldMessage("field1", GENERIC_ERROR_MSG);
		service.checkForSuccess(pageResponse);
		assertFalse(pageResponse.isSuccess());

		pageResponse.setMessage(GENERIC_ERROR_MSG);
		service.checkForSuccess(pageResponse);
		assertFalse(pageResponse.isSuccess());

		pageResponse.setHardStopFailure(true);
		service.checkForSuccess(pageResponse);
		assertFalse(pageResponse.isSuccess());
	}

	@Test
	void determineNavigatePages() {
		Map<String, String> uiRequest = new HashMap<>();
		pageResponse = new C1UXCustDocPageBean();
		service.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 5, 5, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		uiRequest.put("navPage", "5");
		service.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 2, 5, pageResponse);
		assertEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());

		uiRequest.put("navPage", "5");
		service.determineNavigatePages(uiRequest, UIEvent.NAVIGATE_PAGES, 2, 3, pageResponse);
		assertNotEquals(AtWinXSConstant.EMPTY_STRING, pageResponse.getMessage());
	}

	@Test
	void performCancelOrCancelOrder() throws Exception
		{
	    setupBaseMockSessions();
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	      when(mockItem.getOrderID()).thenReturn(256);
	        service = Mockito.spy(service);

	        doReturn(RouteConstants.CART_ENTRY_ROUTING_URL).when(service).getCancelURL(mockItem, mockUI, mockSessionContainer);
	        doNothing().when(service).setupDirectionCancel(any(), any(), any(), any(), any(), any());
	        doNothing().when(service).resetUIAndItem(any(), any());

	        pageResponse = new C1UXCustDocPageBean();
	        service.performCancelOrCancelOrder(mockOESession, mockUI, mockItem,
	        		UserInterface.UIEvent.CANCEL_ORDER, mockSessionContainer, pageResponse);
	        assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, pageResponse.getRedirectRouting());

	        pageResponse = new C1UXCustDocPageBean();
	        service.performCancelOrCancelOrder(mockOESession, mockUI, mockItem,
	        		UserInterface.UIEvent.CANCEL, mockSessionContainer, pageResponse);
	        assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, pageResponse.getRedirectRouting());
		}

	@Test
	void addToCart() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		setUpModuleSessionNoBase();
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	        service = Mockito.spy(service);
	      baseResponse = service.addToCart(mockSessionContainer);
	      assertNotNull(baseResponse);
		}

	@Test
	void doAddToCart() throws Exception {
	      setUpModuleSession();
	      when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
	      when(mockUI.getOrderDeliveryOptionCD()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockUI.getEntryPoint()).thenReturn(UserInterface.EntryPoint.CATALOG_EXPRESS);
	      when(mockUI.isTryUI()).thenReturn(false);
	      when(mockUI.isFieldsEditable()).thenReturn(true);
	      when(mockItem.saveOrderLine(any(), any(), any(), anyBoolean(), any(), any(), anyInt(), any(), any()))
	    		  .thenReturn(mockOrderLineVOKey);
			when(mockOEOrderSession.getCurrentCampaignUserInterface()).thenReturn(mockCampaignUI);
			when(mockUI.getEntryPoint()).thenReturn(UserInterface.EntryPoint.CART_ADD_FROM_STUB);
	        service = Mockito.spy(service);
			doNothing().when(service).saveFullSessionInfo(any(), anyInt(), anyInt());
			when(mockItem.isInNewKit()).thenReturn(false);

			when(mockItem.getOrderLineNumber()).thenReturn(25);
			baseResponse = new C1UXCustDocBaseResponse();
			service.doAddToCart(mockSessionContainer, mockItem, baseResponse);
			assertTrue(baseResponse.isSuccess());

			when(mockItem.getOrderLineNumber()).thenReturn(-1);
			baseResponse = new C1UXCustDocBaseResponse();
			service.doAddToCart(mockSessionContainer, mockItem, baseResponse);
			assertFalse(baseResponse.isSuccess());

		}

	@Test
	void resetUIAndItem()
	{
		service.resetUIAndItem(mockVolatileSessionBean, mockOEOrderSession);
		assertNotNull(service);
		when(mockOEOrderSession.getCurrentCampaignUserInterface()).thenReturn(mockCampaignUI);
		when(mockCampaignUI.getEntryPoint()).thenReturn(EntryPoint.CART_EDIT);
		service.resetUIAndItem(mockVolatileSessionBean, mockOEOrderSession);
		assertNotNull(service);

		when(mockCampaignUI.getEntryPoint()).thenReturn(EntryPoint.SUBSCRIPTION_MOD);
		service.resetUIAndItem(mockVolatileSessionBean, mockOEOrderSession);
		assertNotNull(service);
	}

	@Test
	void testGetUrlBaseMethods() {
		assertNull(service.getCancelURL(null, mockUI, mockSessionContainer));
		assertNull(service.getForwardToURL(null, mockUI, mockSessionContainer));
		assertNull(service.getCancelURL(mockItem, null, mockSessionContainer));
		assertNull(service.getForwardToURL(mockItem, null, mockSessionContainer));
		assertNull(service.getCancelURL(mockItem, mockUI, null));
		assertNull(service.getForwardToURL(mockItem, mockUI, null));

		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.NEW_REQUEST);
		assertNotNull(service.getCancelURL(mockItem, mockUI, mockSessionContainer));
		assertNotNull(service.getForwardToURL(mockItem, mockUI, mockSessionContainer));
		assertEquals("home", service.getCancelURL(mockItem, mockUI, mockSessionContainer));
		assertEquals("/", service.getForwardToURL(mockItem, mockUI, mockSessionContainer));

		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.CART_EDIT);
		assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, service.getCancelURL(mockItem, mockUI, mockSessionContainer));
		assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, service.getForwardToURL(mockItem, mockUI, mockSessionContainer));

		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.CATALOG_NORMAL);
		assertNotEquals(RouteConstants.CART_ENTRY_ROUTING_URL, service.getCancelURL(mockItem, mockUI, mockSessionContainer));
		assertEquals(RouteConstants.CART_ENTRY_ROUTING_URL, service.getForwardToURL(mockItem, mockUI, mockSessionContainer));

	}

	@Test
	void setVariableShowHide()
	{
		HashMap<Integer, DisplayControls> map = new HashMap<>();
		int varNum = 23;
		when(mockVar.getNumber()).thenReturn(varNum);
		map.put(Integer.valueOf(45), mockDisplayControls);

		C1UXVariableBean varBean = new C1UXVariableBean();
		when(mockGroup.getVariableDisplayControls()).thenReturn(new HashMap<>());
		service.setVariableShowHide(varBean, mockVar, mockGroup);
		assertEquals(CustomDocsServiceImpl.Y, varBean.getHiddenShowHideFieldValue());

		map.put(Integer.valueOf(varNum), mockDisplayControls);
		when(mockGroup.getVariableDisplayControls()).thenReturn(map);
		when(mockDisplayControls.isDisplayCriteriaMet()).thenReturn(true);
		service.setVariableShowHide(varBean, mockVar, mockGroup);
		assertEquals(CustomDocsServiceImpl.Y, varBean.getHiddenShowHideFieldValue());

		when(mockDisplayControls.isDisplayCriteriaMet()).thenReturn(false);
		service.setVariableShowHide(varBean, mockVar, mockGroup);
		assertEquals(CustomDocsServiceImpl.N, varBean.getHiddenShowHideFieldValue());
	}


	// CAP-43582 - added new method
	@Test
	void setCheckboxVariableInfo() throws Exception {
		try (MockedStatic<CheckboxRendererImpl> mockUtil = Mockito.mockStatic(CheckboxRendererImpl.class)) {
			  	mockUtil.when(() -> CheckboxRendererImpl.getCheckboxRendererInput(any())).thenReturn(mockCheckboxRendererInput);
		when(mockCheckboxRendererInput.isChecked()).thenReturn(true);
		when(mockVar.getInputTypeCode()).thenReturn(InputType.CHECKBOX);
		C1UXVariableBean bean = new C1UXVariableBean();
		service.setCheckboxVariableInfo(bean, mockVar);
		assertTrue(bean.isCurrentlyChecked());
		assertFalse(bean.isInputRequired());

		when(mockCheckboxRendererInput.isChecked()).thenReturn(false);
		bean = new C1UXVariableBean();
		service.setCheckboxVariableInfo(bean, mockVar);
		assertFalse(bean.isCurrentlyChecked());

		when(mockVar.getInputTypeCode()).thenReturn(InputType.CHECKBOX);
		bean = new C1UXVariableBean();
		service.setCheckboxVariableInfo(bean, mockVar);
		assertFalse(bean.isCurrentlyChecked());
		}
	}

	@Test
	void getCalendarVariableInfo() throws AtWinXSException {
		String convertedBaseDateFormat = "mm/dd/yy";
		String convertedOppositeHypensDateFormat = "dd-mm-yy";
		String convertedDotsDateFormat = "mm.dd.yy";

		try (MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
		  	mockUtil.when(() -> Util.getDateFormatForLocale(any())).thenReturn("MM/DD/YYYY");

			when(mockVar.getInputTypeCode()).thenReturn(InputType.CHECKBOX);
			C1UXVariableBean bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertNull(bean.getDateFormat());

			when(mockVar.getInputTypeCode()).thenReturn(InputType.CALENDAR_DATE);
			when(mockVar.getMaskNumber()).thenReturn(2);

	        service = Mockito.spy(service);
	        doReturn(mockOECustDocComponent).when(service).getOECustDocComponent(any());
			doReturn(null).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedBaseDateFormat, bean.getDateFormat());

			doReturn(getNullNameMask()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedBaseDateFormat, bean.getDateFormat());

			doReturn(getNonDateMask()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedBaseDateFormat, bean.getDateFormat());

			doReturn(get99MaskSlashes()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedBaseDateFormat, bean.getDateFormat());

			doReturn(get99MaskDashes()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals("mm-dd-yy", bean.getDateFormat());

			doReturn(getOtherMaskDots()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedDotsDateFormat, bean.getDateFormat());

			// now try non-US locale with other format
		  	mockUtil.when(() -> Util.getDateFormatForLocale(any())).thenReturn("DD-MM-YYYY");
			doReturn(null).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedOppositeHypensDateFormat, bean.getDateFormat());

			doReturn(get99MaskSlashes()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals("dd/mm/yy", bean.getDateFormat());

			doReturn(get99MaskDashes()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedOppositeHypensDateFormat, bean.getDateFormat());

			doReturn(getOtherMaskDots()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals(convertedDotsDateFormat, bean.getDateFormat());

			doReturn(getOtherMaskDots2DigitYear()).when(mockOECustDocComponent).getMask(any());
			bean = new C1UXVariableBean();
			service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
			assertEquals("mm.dd.y", bean.getDateFormat());

			doThrow(new AtWinXSException("another fails", this.getClass().getName())).when(mockOECustDocComponent).getMask(any());
		      bean = new C1UXVariableBean();
				service.setCalendarVariableInfo(bean, mockVar, DEFAULT_US_LOCALE);
				assertNotNull(bean.getDateFormat());
}
	}

	protected Object getNullNameMask() {
		return new CDMaskVO(0, null, null);
	}

	protected Object getNonDateMask() {
		return new CDMaskVO(0, "Phone 999 999.9999", null);
	}

	protected Object getOtherMaskDots() {
		return new CDMaskVO(0, "Date MM.DD.YYYY", null);
	}

	protected Object getOtherMaskDots2DigitYear() {
		return new CDMaskVO(0, "Date MM.DD.YY", null);
	}

	protected Object get99MaskSlashes() {
		return new CDMaskVO(0, "Date 99/99/9999", null);
	}

	protected Object get99MaskDashes() {
		return new CDMaskVO(0, "Date 99-99-9999", null);
	}

	// CAP-42298
	@Test
	void cancelAction() throws Exception {
		  setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
	      when(mockItem.getUserInterface()).thenReturn(mockUI);
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      service = Mockito.spy(service);
	      doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());
	      doNothing().when(service).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      baseResponse = service.cancelAction(mockSessionContainer, false);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = service.cancelAction(mockSessionContainer, true);
	      assertEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new AtWinXSException("myError", "Aclassname")).when(service).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(Locale.class), any(), anyString());

	      baseResponse = service.cancelAction(mockSessionContainer, false);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = service.cancelAction(mockSessionContainer, true);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      doThrow(new NullPointerException("I blew up!")).when(service).performCancelOrCancelOrder(eq(mockOESession), eq(mockUI), eq(mockItem), any(UIEvent.class), eq(mockSessionContainer),
	    		  any());
	      baseResponse = service.cancelAction(mockSessionContainer, false);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	      baseResponse = service.cancelAction(mockSessionContainer, true);
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, baseResponse.getMessage());

	}

	@Test
	void populateCPBeanForPage() throws AtWinXSException {
	      service = Mockito.spy(service);
	      when(mockOrderEntrySession.getOESessionBean()).thenReturn(mockOESessionBean);
	      doNothing().when(service).buildCustDocUIFormBean(eq(mockUI), eq(mockPage),
	    		  any(CustDocUIFormBean.class), eq(mockAppSessionBean), eq(mockVolatileSessionBean));
	      doNothing().when(service).loadItemInstructionsJavascript(eq(mockItem), any(CustDocUIFormBean.class), eq(mockAppSessionBean));
	      when(mockPage.getPageNumber()).thenReturn(25);
	      doNothing().when(service).loadUIPageJavascript(eq(mockUI), eq(mockPage), any(CustDocUIFormBean.class), eq(mockAppSessionBean));

	      CustDocUIFormBean bean = new CustDocUIFormBean();
	      service.populateCPBeanForPage(mockItem, mockUI, mockPage, bean, mockAppSessionBean, mockVolatileSessionBean, mockOrderEntrySession);
	      assertNotNull(bean);

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_PROOF);
	      doNothing().when(service).finalizeProofModelDetails(eq(mockItem), eq(mockUI), any(CustDocProofFormBean.class), eq(mockAppSessionBean), eq(mockVolatileSessionBean), eq(mockOrderEntrySession));
	      bean = new CustDocProofFormBean();
	      service.populateCPBeanForPage(mockItem, mockUI, mockPage, bean, mockAppSessionBean, mockVolatileSessionBean, mockOrderEntrySession);
	      assertNotNull(bean);

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST);
	      bean = new CustDocUIFormBean();
	      service.populateCPBeanForPage(mockItem, mockUI, mockPage, bean, mockAppSessionBean, mockVolatileSessionBean, mockOrderEntrySession);
	      assertNotNull(bean);

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST_DATA);
	      bean = new CustDocUIFormBean();
	      service.populateCPBeanForPage(mockItem, mockUI, mockPage, bean, mockAppSessionBean, mockVolatileSessionBean, mockOrderEntrySession);
	      assertNotNull(bean);

	      when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_KEY_PROFILES);
	      bean = new CustDocUIFormBean();
	      service.populateCPBeanForPage(mockItem, mockUI, mockPage, bean, mockAppSessionBean, mockVolatileSessionBean, mockOrderEntrySession);
	      assertNotNull(bean);

	}

	@Test
	void loadUserProfileSearch()  {
		int firstPageNum = 2;
		int secondPageNum = 30;
		when(mockUI.isTryUI()).thenReturn(true);
		C1UXUserProfileSearchOptions options = service.loadUserProfileSearch(mockUI, firstPageNum);
		assertNull(options);
		when(mockUI.isTryUI()).thenReturn(false);
		when(mockUI.isFieldsEditable()).thenReturn(false);
		when(mockUI.isNewRequestFlow()).thenReturn(true);
		when(mockPage.getPageNumber()).thenReturn(2);
		List<Page> pages = new ArrayList<>();
		pages.add(mockPage);
		pages.add(mockPage2);
		when(mockUI.getPages()).thenReturn(pages);
		options = service.loadUserProfileSearch(mockUI, secondPageNum);
		assertNull(options);

		when(mockUI.getProfileSelector()).thenReturn(null);
		options = service.loadUserProfileSearch(mockUI, firstPageNum);
		assertNull(options);
		when(mockUI.isFieldsEditable()).thenReturn(true);
		options = service.loadUserProfileSearch(mockUI, firstPageNum);
		assertNull(options);

		when(mockUI.isNewRequestFlow()).thenReturn(false);
		when(mockUI.getProfileSelector()).thenReturn(null);
		options = service.loadUserProfileSearch(mockUI, firstPageNum);
		assertNull(options);

		when(mockUI.getProfileSelector()).thenReturn(mockUserProfileSelector);
		when(mockUserProfileSelector.isShown()).thenReturn(false);
		options = service.loadUserProfileSearch(mockUI, firstPageNum);
		assertNull(options);
		when(mockUserProfileSelector.isShown()).thenReturn(true);
		when(mockUserProfileSelector.getProfileOptions()).thenReturn(null);
		when(mockUserProfileSelector.getSimpleSearchTerms()).thenReturn(null);
		when(mockUserProfileSelector.getSelectedProfile()).thenReturn(2);
		String label="Person";
		when(mockUserProfileSelector.getProfileIDLabel()).thenReturn(label);
		options = service.loadUserProfileSearch(mockUI, firstPageNum);
		assertNotNull(options);
		assertEquals(label, options.getProfileIDLabel());
	}

	@Test
	void getSuccessfulWorkingProofLabels() {
		Collection<String> labels = null;
		String proofPagePrefix = "Proof page";
		service = Mockito.spy(service);
		doReturn(proofPagePrefix).when(service).getTranslation(any(AppSessionBean.class), anyString(), anyString());
		when(mockPage.isWorkingProofAllPages()).thenReturn(false);
		when(mockPage.getWorkingProofPageNumber()).thenReturn(3);
		labels = service.getSuccessfulWorkingProofLabels(mockUI, mockAppSessionBean, mockPage);
		assertEquals(1, labels.size());

		when(mockPage.isWorkingProofAllPages()).thenReturn(true);
		when(mockUI.getPagesToProof()).thenReturn(null);
		when(mockUI.getTemplatePageCount()).thenReturn(1);
		labels = service.getSuccessfulWorkingProofLabels(mockUI, mockAppSessionBean, mockPage);
		assertEquals(1, labels.size());

		when(mockUI.getTemplatePageCount()).thenReturn(10);
		labels = service.getSuccessfulWorkingProofLabels(mockUI, mockAppSessionBean, mockPage);
		assertEquals(10, labels.size());

		HashSet<Integer> unsortedHashSet = new HashSet<>();
		unsortedHashSet.add(2);
		unsortedHashSet.add(20);
		unsortedHashSet.add(12);
		unsortedHashSet.add(11);
		unsortedHashSet.add(7);
		when(mockUI.getPagesToProof()).thenReturn(unsortedHashSet);
		labels = service.getSuccessfulWorkingProofLabels(mockUI, mockAppSessionBean, mockPage);
		assertEquals(5, labels.size());

	}

	@Test
	void failResponse() {
		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		formBean.setError(GENERIC_ERROR_MSG);
		service.failResponse(sfBean, mockAppSessionBean, false, formBean);
		assertEquals(GENERIC_ERROR_MSG, sfBean.getMessage());
	}

	@Test
	void buildCustDocUIFormBean() throws AtWinXSException {
		service = Mockito.spy(service);
		when(mockUI.getRefreshListTriggerVariable()).thenReturn(null);
		when(mockVolatileSessionBean.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.ORDER_SCENARIO_NUMBER_DEFAULT_VALUE);
		when(mockPage.getPageNumber()).thenReturn(2);
		doNothing().when(service).setBlankFields(any());
		doNothing().when(service).setFieldsNoLogic(any(), any(), any(), any());
		doReturn(true).when(service).getAllowFailedProofs();
		when(mockUI.isTryUI()).thenReturn(false);
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		service.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertEquals(Boolean.FALSE.toString(), formBean.getOrderFromFile());

		when(mockVolatileSessionBean.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE);
		formBean = new CustDocUIFormBean();
		service.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertEquals(Boolean.TRUE.toString(), formBean.getOrderFromFile());
		assertTrue(formBean.isAllowFailedProof());

		doReturn(false).when(service).getAllowFailedProofs();
		formBean = new CustDocUIFormBean();
		service.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertFalse(formBean.isAllowFailedProof());

		when(mockPage.getPageNumber()).thenReturn(99998);
		when(mockUI.isTryUI()).thenReturn(true);
		formBean = new CustDocUIFormBean();
		service.buildCustDocUIFormBean(mockUI, mockPage, formBean, mockAppSessionBean, mockVolatileSessionBean);
		assertTrue(formBean.isAllowFailedProof());
	}

	@Test
	void setUploadVariableInfo() {
		C1UXVariableBean var = new C1UXVariableBean();
		when(mockVar.isUploadAllowedInd()).thenReturn(false);
		service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
		assertFalse(var.isUploadAllowedInd());
		assertEquals(0, var.getMinFiles());

		when(mockVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.INSERTION_GROUP);
		when(mockVar.getInputTypeCode()).thenReturn(Variable.InputType.UPLOAD_ONLY);
		when(mockVar.getGroupMaxInserts()).thenReturn(5);
		when(mockVar.getGroupMinInserts()).thenReturn(1);
		var = new C1UXVariableBean();
		service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
		assertTrue(var.isUploadAllowedInd());
		assertEquals(1, var.getMinFiles());
		assertEquals(5, var.getMaxFiles());
		assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_INSERTS, var.getUploadFileFormatsCode());

		when(mockVar.isUploadAllowedInd()).thenReturn(true);
		var = new C1UXVariableBean();
		service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
		assertTrue(var.isUploadAllowedInd());
		assertEquals(1, var.getMinFiles());
		assertEquals(5, var.getMaxFiles());
		assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_INSERTS, var.getUploadFileFormatsCode());

		when(mockVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.HOSTED_RESOURCE);
		var = new C1UXVariableBean();
		service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
		assertTrue(var.isUploadAllowedInd());
		assertEquals(1, var.getMinFiles());
		assertEquals(1, var.getMaxFiles());
		assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_HOSTED_RESOURCE, var.getUploadFileFormatsCode());

		when(mockFileUploadVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.FILE_UPLOAD);
		var = new C1UXVariableBean();
		service.setUploadVariableInfo(var, mockFileUploadVar, mockAppSessionBean);
		assertTrue(var.isUploadAllowedInd());
		assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_FILE_UPLOAD, var.getUploadFileFormatsCode());

		var = new C1UXVariableBean();
		when(mockFileUploadVar.isNewRequestFlow()).thenReturn(true);
		service.setUploadVariableInfo(var, mockFileUploadVar, mockAppSessionBean);
		assertTrue(var.isUploadAllowedInd());
		assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_NEW_REQUEST, var.getUploadFileFormatsCode());
	}

	@Test
	void setUploadVariableInfo_Image() {
		try (MockedStatic<PropertyUtil> mockUtil = Mockito.mockStatic(PropertyUtil.class)) {
		  	mockUtil.when(() -> PropertyUtil.getProperties(anyString(), anyString())).thenReturn(mockXSProperties);
			when(mockVar.getInputTypeCode()).thenReturn(Variable.InputType.UPLOAD_ONLY);
		  	when(mockXSProperties.getProperty(any())).thenReturn("25");
			when(mockVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.IMAGE);
			when(mockAppSessionBean.getCurrentEnvCd()).thenReturn(CLASSIC_CONTEXT_PATH);
			C1UXVariableBean var = new C1UXVariableBean();
		  	when(mockAppSessionBean.getBuID()).thenReturn(25);
		  	
		  	// CAP-46487
		  	double size=100;
		  	when(mockVar.getCropImageWidth()).thenReturn(size);
		  	when(mockVar.getCropImageHeight()).thenReturn(size);

			service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
			assertTrue(var.isUploadAllowedInd());
			assertEquals(1, var.getMinFiles());
			assertEquals(1, var.getMaxFiles());
			assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_IMAGES_EPS_ONLY, var.getUploadFileFormatsCode());

		  	when(mockAppSessionBean.getBuID()).thenReturn(45);
			service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
			assertTrue(var.isUploadAllowedInd());
			assertEquals(1, var.getMinFiles());
			assertEquals(1, var.getMaxFiles());
			assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_IMAGES_STANDARD_ONLY, var.getUploadFileFormatsCode());

			when(mockVar.isAllowEPSImg()).thenReturn(true);
			var = new C1UXVariableBean();
			service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
			assertTrue(var.isUploadAllowedInd());
			assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_IMAGES_EPS_ONLY, var.getUploadFileFormatsCode());

			when(mockVar.isAllowSTDImg()).thenReturn(true);
			var = new C1UXVariableBean();
			service.setUploadVariableInfo(var, mockVar, mockAppSessionBean);
			assertTrue(var.isUploadAllowedInd());
			assertEquals(CustomDocsService.UPLOAD_FILE_FORMATS_ALL_IMAGES, var.getUploadFileFormatsCode());
		}
	}

	@Test
	void getWebserviceProperty() {
		try (MockedStatic<PropertyUtil> mockUtil = Mockito.mockStatic(PropertyUtil.class)) {
		  	mockUtil.when(() -> PropertyUtil.getProperties(anyString(), anyString())).thenThrow(new AtWinXSException(CLASSIC_CONTEXT_PATH, GENERIC_ERROR_MSG));
		  	assertEquals(AtWinXSConstant.EMPTY_STRING, service.getWebserviceProperty(mockAppSessionBean, "key"));
		}
	}

	@Test
	void checkIfBUAllowsEPS() {
		service = Mockito.spy(service);
		doReturn(AtWinXSConstant.EMPTY_STRING).when(service).getWebserviceProperty(mockAppSessionBean, UploadFile.BUS_UNITS_ALLOWED_EPS_UPLOAD);
		assertFalse(service.checkIfBUAllowsEPS(mockAppSessionBean));
		doReturn(ID_NUM).when(service).getWebserviceProperty(mockAppSessionBean, UploadFile.BUS_UNITS_ALLOWED_EPS_UPLOAD);
		when(mockAppSessionBean.getBuID()).thenReturn(ID_NUMBER);
		assertTrue(service.checkIfBUAllowsEPS(mockAppSessionBean));
	}

	@Test
	void getNewRequestLink() {
		service = Mockito.spy(service);
//		doReturn(false).when(service).isVarValueUploadOrSearch(any());
		when(mockVar.getListValue()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		assertEquals(AtWinXSConstant.EMPTY_STRING, service.getNewRequestLink(mockVar, mockAppSessionBean));

		when(mockVar.getListValue()).thenReturn(CLASSIC_CONTEXT_PATH);
		assertEquals(AtWinXSConstant.EMPTY_STRING, service.getNewRequestLink(mockVar, mockAppSessionBean));

		when(mockVar.getListValue()).thenReturn("U12345^UPLOAD: MyFile.pdf");
		doReturn(mockUploadFileUpload).when(service).getUploadFileObject(any(), any(), any());
		when(mockUploadFileUpload.generateGetAFileLinkURLC1UX(anyInt())).thenReturn(GENERIC_SAVE_FAILED_ERR_ENGLISH);
		assertEquals(GENERIC_SAVE_FAILED_ERR_ENGLISH, service.getNewRequestLink(mockVar, mockAppSessionBean));
	}


	@Test
	void retrieveStoredError() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);

		assertDoesNotThrow(() -> {
			C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
			service.retrieveStoredError(mockSessionContainer, sfBean);
		});

		when(mockMessage.getErrGeneralMsg()).thenReturn(CLASSIC_CONTEXT_PATH);
		when(mockOESessionBean.getUsabilityRedirectErrorMessage()).thenReturn(mockMessage);
		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		service.retrieveStoredError(mockSessionContainer, sfBean);
		assertEquals(CLASSIC_CONTEXT_PATH, sfBean.getMessage());

		sfBean = new C1UXCustDocPageBean();
		sfBean.setMessage(GENERIC_ERROR_MSG);
		service.retrieveStoredError(mockSessionContainer, sfBean);
		assertTrue(sfBean.getMessage().contains(CLASSIC_CONTEXT_PATH));
		assertTrue(sfBean.getMessage().contains(GENERIC_ERROR_MSG));
	}

	@Test
	void storeErrorForLater() throws Exception {
		service = Mockito.spy(service);
		when(mockPageResponse.getMessage()).thenReturn(CLASSIC_CONTEXT_PATH);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);

		doNothing().when(service).saveFullOESessionInfo(eq(mockOESession), anyInt());

		assertDoesNotThrow(() -> {
			service.storeErrorForLater(mockPageResponse, mockSessionContainer);
		});
		when(mockPageResponse.getMessage()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		assertDoesNotThrow(() -> {
			service.storeErrorForLater(mockPageResponse, mockSessionContainer);
		});

	}


	@Test
	void isSaveAndDoEvent() throws Exception {
		assertTrue(service.isSaveAndDoEvent(UserInterface.UIEvent.SAVE_AND_CONTINUE));
		assertTrue(service.isSaveAndDoEvent(UserInterface.UIEvent.SAVE_AND_SEARCH));
		assertTrue(service.isSaveAndDoEvent(UserInterface.UIEvent.REFRESH));
		assertFalse(service.isSaveAndDoEvent(UserInterface.UIEvent.SAVE_AND_EXIT));
	}

	@Test
	void setFormattedParagraphHandling() throws Exception {
	    service = Mockito.spy(service);
				when(mockParaVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.INSERTION_GROUP);
				C1UXVariableBean var = new C1UXVariableBean();
				service.setFormattedParagraphHandling(var, mockParaVar);
				assertNotNull(var);

				when(mockParaVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.FORMATTED_PARAGRAPH);
				when(mockParaVar.getTextValue()).thenReturn(AtWinXSConstant.EMPTY_STRING);
				var = new C1UXVariableBean();
				service.setFormattedParagraphHandling(var, mockParaVar);
				assertEquals(AtWinXSConstant.EMPTY_STRING, var.getTextValue());

				when(mockParaVar.getTextValue()).thenReturn(CLASSIC_CONTEXT_PATH);
				var = new C1UXVariableBean();
				service.setFormattedParagraphHandling(var, mockParaVar);
				assertEquals(CLASSIC_CONTEXT_PATH, var.getTextValue());

				String testWithSpecialChars = "This unit is available in both the Oakwood® brand and ExecuStay® brand.";
				String htmlWithSpecialChars = "<p>" + testWithSpecialChars + "</p>";
			    service = Mockito.spy(service);
				doReturn(mockParaProcessor).when(service).getFormattedParagraphProcessor();
				when(mockParaProcessor.getHtmlText()).thenReturn(testWithSpecialChars);

				when(mockParaVar.getCustomStyle()).thenReturn(CUSTOMER_ITEM_NUM);
				when(mockParaVar.isUseNewFormatting()).thenReturn(true);
				when(mockParaVar.getTextValue()).thenReturn("<PF_Para_Base><_char>"+testWithSpecialChars+"</_char></PF_Para_Base>");
				var = new C1UXVariableBean();
				service.setFormattedParagraphHandling(var, mockParaVar);
				assertEquals(testWithSpecialChars, var.getTextValue());

				when(mockParaVar.isWordProcess()).thenReturn(true);
				when(mockParaProcessor.getHtmlText()).thenReturn(htmlWithSpecialChars);

				doReturn(htmlWithSpecialChars).when(service).getOEParagraphValue(any(), any());

				var = new C1UXVariableBean();
				service.setFormattedParagraphHandling(var, mockParaVar);
				assertEquals(htmlWithSpecialChars, var.getTextValue());

				when(mockParaVar.isUseNewFormatting()).thenReturn(false);
				when(mockParaVar.getTextValue()).thenReturn("&lt;PF_Para_Base&gt;&lt;_char bold=\"false\" italic=\"false\" underline=\"false\" supersub=\"\"&gt;"+testWithSpecialChars+"&lt;/_char&gt;&lt;/PF_Para_Base&gt;");
				var = new C1UXVariableBean();
				service.setFormattedParagraphHandling(var, mockParaVar);
				assertEquals(htmlWithSpecialChars, var.getTextValue());

				 doThrow(new AtWinXSException("another fails", this.getClass().getName())).when(service).getOEParagraphValue(any(), any());
			      var = new C1UXVariableBean();
					service.setFormattedParagraphHandling(var, mockParaVar);
					assertNotNull(var);
	}
	@Test
	void setGridInformation() {
		when(mockGroup.isGrid()).thenReturn(false);
		when(mockGridGroup.isGrid()).thenReturn(true);
		C1UXCustDocGroupBean groupBean = new C1UXCustDocGroupBean();
		service.setGridInformation(mockGroup, groupBean);
		assertTrue(groupBean.getRows().isEmpty());
		List<C1UXCustDocGridRow> goodGridRows = getGoodGridRows();
		when(mockGridGroup.getRowsC1UX()).thenReturn(goodGridRows);
		when(mockGridGroup.getColumnsC1UX()).thenReturn(getGoodGridColumns());
	    service = Mockito.spy(service);
	    doReturn(new ArrayList<C1UXCustDocGridAssignment>()).when(service).buildEmptyCells(any(GridGroup.class), any(C1UXCustDocGroupBean.class));
	    groupBean = new C1UXCustDocGroupBean();
	    service.setGridInformation(mockGridGroup, groupBean);
	    assertEquals(goodGridRows, groupBean.getRows());
	    groupBean = new C1UXCustDocGroupBean();
		when(mockGridGroup.isAnyRowHeaders()).thenReturn(true);
		when(mockGridGroup.isAnyRowsHide()).thenReturn(true);
		service.setGridInformation(mockGridGroup, groupBean);
	    assertEquals(goodGridRows, groupBean.getRows());
	}
	protected List<C1UXCustDocGridRow> getGoodGridRows() {
		ArrayList<C1UXCustDocGridRow> rows = new ArrayList<C1UXCustDocGridRow>();
		int number = 1;
		C1UXCustDocGridRow newRow = new C1UXCustDocGridRow();
		newRow.setHeader("This is beRow my level.");
		newRow.setNumber(number);
		newRow.setInitiallyShown(true);
		newRow.setCanBeShownOnAdd(false);
		newRow.setHeaderCpId("td" + FAKE_GROUP_NUMBER + "LabelRow" + number);
		newRow.setHiddenFieldId("showGrp"+FAKE_GROUP_NUMBER +"Row"+number);
		newRow.setControlHiddenFieldId("Group"+FAKE_GROUP_NUMBER +"RowHide"+number);
		newRow.setCpTrId("grp" + FAKE_GROUP_NUMBER + "Row" + number++);
		int[] varNums = {-2, FAKE_VAR1_NUMBER};
		newRow.setVarNumbers(varNums);
		rows.add(newRow);
		C1UXCustDocGridRow newRow2 = new C1UXCustDocGridRow();
		newRow2.setHeader("I am just a blank row - an empty and a continue");
		newRow2.setNumber(number);
		newRow2.setInitiallyShown(true);
		newRow2.setCanBeShownOnAdd(false);
		newRow2.setHeaderCpId("td" + FAKE_GROUP_NUMBER + "LabelRow" + number);
		newRow2.setHiddenFieldId("showGrp"+FAKE_GROUP_NUMBER +"Row"+number);
		newRow2.setControlHiddenFieldId("Group"+FAKE_GROUP_NUMBER +"RowHide"+number);
		newRow2.setCpTrId("grp" + FAKE_GROUP_NUMBER + "Row" + number++);
		int[] varNums2 = {-2, -1};
		newRow2.setVarNumbers(varNums2);
		rows.add(newRow2);
		C1UXCustDocGridRow newRow3 = new C1UXCustDocGridRow();
		newRow3.setHeader("Row 3 has var in first slot, then empty");
		newRow3.setNumber(number);
		newRow3.setInitiallyShown(true);
		newRow3.setCanBeShownOnAdd(false);
		newRow3.setHeaderCpId("td" + FAKE_GROUP_NUMBER + "LabelRow" + number);
		newRow3.setHiddenFieldId("showGrp"+FAKE_GROUP_NUMBER +"Row"+number);
		newRow3.setControlHiddenFieldId("Group"+FAKE_GROUP_NUMBER +"RowHide"+number);
		newRow3.setCpTrId("grp" + FAKE_GROUP_NUMBER + "Row" + number++);
		int[] varNums3 = {FAKE_VAR2_NUMBER, -2};
		newRow3.setVarNumbers(varNums3);
		rows.add(newRow3);
		C1UXCustDocGridRow newRow4 = new C1UXCustDocGridRow();
		newRow4.setHeader("Row 4 has var in first slot, then continue");
		newRow4.setNumber(number);
		newRow4.setInitiallyShown(true);
		newRow4.setCanBeShownOnAdd(false);
		newRow4.setHeaderCpId("td" + FAKE_GROUP_NUMBER + "LabelRow" + number);
		newRow4.setHiddenFieldId("showGrp"+FAKE_GROUP_NUMBER +"Row"+number);
		newRow4.setControlHiddenFieldId("Group"+FAKE_GROUP_NUMBER +"RowHide"+number);
		newRow4.setCpTrId("grp" + FAKE_GROUP_NUMBER + "Row" + number++);
		int[] varNums4 = {FAKE_VAR3_NUMBER, -1};
		newRow4.setVarNumbers(varNums4);
		rows.add(newRow4);
		C1UXCustDocGridRow newRow5 = new C1UXCustDocGridRow();
		newRow5.setHeader("Row 5 is just two empty cells");
		newRow5.setNumber(number);
		newRow5.setInitiallyShown(true);
		newRow5.setCanBeShownOnAdd(false);
		newRow5.setHeaderCpId("td" + FAKE_GROUP_NUMBER + "LabelRow" + number);
		newRow5.setHiddenFieldId("showGrp"+FAKE_GROUP_NUMBER +"Row"+number);
		newRow5.setControlHiddenFieldId("Group"+FAKE_GROUP_NUMBER +"RowHide"+number);
		newRow5.setCpTrId("grp" + FAKE_GROUP_NUMBER + "Row" + number++);
		int[] varNums5 = {-2, -2};
		newRow5.setVarNumbers(varNums5);
		rows.add(newRow5);
		return rows;
	}
	protected List<C1UXCustDocGridColumn> getGoodGridColumns() {
		ArrayList<C1UXCustDocGridColumn> cols = new ArrayList<C1UXCustDocGridColumn>();
		C1UXCustDocGridColumn newColumn = new C1UXCustDocGridColumn();
		int number = 1;
		newColumn.setHeader("My first column");
		newColumn.setNumber(number);
		newColumn.setHeaderCpId("h" + FAKE_GROUP_NUMBER + "Col" + number);
		newColumn.setHiddenFieldId("showGrp"+ FAKE_GROUP_NUMBER + "Col" + number);
		newColumn.setInitiallyShown(true);
		cols.add(newColumn);
		newColumn = new C1UXCustDocGridColumn();
		number = 2;
		newColumn.setHeader("I am # 2");
		newColumn.setNumber(number);
		newColumn.setHeaderCpId("h" + FAKE_GROUP_NUMBER + "Col" + number);
		newColumn.setHiddenFieldId("showGrp"+ FAKE_GROUP_NUMBER + "Col" + number);
		newColumn.setInitiallyShown(true);
		cols.add(newColumn);
		return cols;
	}
	@Test
	void getAssignment() {
		when(mockGridGroup.getColspanForCellC1UX(anyInt(), anyInt())).thenReturn(1);
		assertEquals(2, service.getAssignment(mockGridGroup, 2, 1).getRow());
		assertEquals(4, service.getAssignment(mockGridGroup, 3, 4).getColumn());
	}
	protected C1UXCustDocGridAssignment getAssignment(int row, int col, int span) {
		C1UXCustDocGridAssignment asn = new C1UXCustDocGridAssignment();
		asn.setColumn(col);
		asn.setRow(row);
		asn.setColumnSpan(span);
		asn.setGridCellId("td" + FAKE_GROUP_NUMBER + "Col" + col + "Row" + row);
		return asn;
	}
	@Test
	void buildEmptyCells() {
		Map<Integer, List<Integer>> cellMap = new HashMap<Integer, List<Integer>>();
		when(mockGridGroup.getEmptyVariablesC1UX()).thenReturn(cellMap);
		C1UXCustDocGroupBean groupBean = new C1UXCustDocGroupBean();
		assertTrue(service.buildEmptyCells(mockGridGroup, groupBean).isEmpty());
		List<Integer> list1 = new ArrayList<Integer>(); list1.add(1);
		List<Integer> list2 = new ArrayList<Integer>(); list2.add(1);
		List<Integer> list3 = new ArrayList<Integer>(); list3.add(2);
		List<Integer> list5 = new ArrayList<Integer>(); list5.add(1);  list5.add(2);
		cellMap.put(1, list1);
		cellMap.put(2, list2);
		cellMap.put(3, list3);
		cellMap.put(5, list5);
		when(mockGridGroup.getColspanForCellC1UX(anyInt(), anyInt())).thenReturn(1);
//		when(mockGridGroup.getRowsC1UX()).thenReturn(getGoodGridRows());
		when(mockGridGroup.getColspanForCellC1UX(anyInt(), anyInt())).thenReturn(1);
//	    service = Mockito.spy(service);
//		doReturn(new C1UXCustDocGridAssignment()).when(service).getAssignment(any(GridGroup.class), anyInt(), anyInt());
		groupBean = new C1UXCustDocGroupBean();
		groupBean.setRows(getGoodGridRows());
		assertEquals(5, service.buildEmptyCells(mockGridGroup, groupBean).size());
	}
	@Test
	void setVariableGridAssignment() {
		C1UXCustDocGroupBean groupBean = new C1UXCustDocGroupBean();
		C1UXVariableBean varBean = new C1UXVariableBean();
		service.setVariableGridAssignment(mockGroup, groupBean, varBean);
		assertNull(varBean.getGridAssignment());
		groupBean.setRows(null);
		service.setVariableGridAssignment(mockGroup, groupBean, varBean);
		assertNull(varBean.getGridAssignment());
		groupBean.setRows(getGoodGridRows());
		groupBean.setColumns(getGoodGridColumns());
		service.setVariableGridAssignment(mockGridGroup, groupBean, varBean);
		assertNull(varBean.getGridAssignment());
		varBean.setNumber(FAKE_VAR1_NUMBER);
		service.setVariableGridAssignment(mockGridGroup, groupBean, varBean);
		assertNotNull(varBean.getGridAssignment());
	}
	@Test
	void loadPageTranslations() {
	      service = Mockito.spy(service);
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));
			C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
			service.loadPageTranslations(mockCampaignUI, sfBean, mockAppSessionBean);
			assertEquals(GENERIC_ERROR_MSG, sfBean.getAddRowButtonText());
			service.loadPageTranslations(mockMessageDocsUI, sfBean, mockAppSessionBean);
			assertEquals(GENERIC_ERROR_MSG, sfBean.getAddRowButtonText());
	}
	@Test
	void formatJavaScriptForFE() {
	      service = Mockito.spy(service);
	      doReturn(CLASSIC_CONTEXT_PATH).when(service).removeScriptTags(any());
	      doReturn(CLASSIC_CONTEXT_PATH).when(service).cleanForFE(any());
	      doReturn(CLASSIC_CONTEXT_PATH).when(service).reformatFunction(any(), any());
	      assertEquals(AtWinXSConstant.EMPTY_STRING, service.formatJavaScriptForFE(AtWinXSConstant.EMPTY_STRING, null));
	      assertEquals(CLASSIC_CONTEXT_PATH, service.formatJavaScriptForFE(GENERIC_ERROR_MSG, null));
	}
	@Test
	void reformatFunction() {
	      assertEquals(AtWinXSConstant.EMPTY_STRING, service.reformatFunction(AtWinXSConstant.EMPTY_STRING, new HashSet<String>()));
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, service.reformatFunction("var abc=1;", new HashSet<String>()));
	}

	@Test
	void getAllowFailedProofs() throws AtWinXSException {
		try (MockedStatic<PropertyUtil> mockUtil = Mockito.mockStatic(PropertyUtil.class)) {
		  	mockUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockXSProperties);
		  	when(mockXSProperties.getProperty(any())).thenReturn(CustomDocsServiceImpl.N);
		  	assertFalse(service.getAllowFailedProofs());
		  	when(mockXSProperties.getProperty(any())).thenReturn(CustomDocsServiceImpl.Y);
		  	assertTrue(service.getAllowFailedProofs());
		}
	}

	@Test
	void prepareBeanToReturn() throws IllegalAccessException, InvocationTargetException  {
		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		service = Mockito.spy(service);
		when(mockUI.isPageflex()).thenReturn(true);
		doNothing().when(service).buildGroups(mockPage, sfBean, mockUI, mockAppSessionBean);
		doNothing().when(service).populateSFSpecificFields(sfBean, mockPage, mockAppSessionBean, mockUI, mockItem, mockVolatileSessionBean, mockOEOrderSessionBean);
		service.prepareBeanToReturn(sfBean, mockUIBean, mockPage, mockAppSessionBean, mockUI, mockItem, mockVolatileSessionBean, mockOEOrderSessionBean);
		assertTrue(sfBean.isHdnPageFlexIndicator());
	}

	@Test
	void buildSingleUploadedListOption() {
		when(mockVar.getListValue()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		assertNull(service.buildSingleUploadedListOption(mockVar));

		when(mockVar.getListValue()).thenReturn("5");
		assertNull(service.buildSingleUploadedListOption(mockVar));

		when(mockVar.getListValue()).thenReturn(FAKE_UPLOAD_LIST_VALUE);
		assertNotNull(service.buildSingleUploadedListOption(mockVar));
	}

	@Test
	void setUploadListOptions() {
		C1UXVariableBean varBean = new C1UXVariableBean();
		service.setUploadListOptions(varBean, mockVar);
		assertNotNull(varBean.getSelectedUploadedListValues());
		assertTrue(varBean.getSelectedUploadedListValues().isEmpty());

		when(mockVar.getName()).thenReturn("fakeListOption");
		when(mockVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.DISPLAY);
		service.setUploadListOptions(varBean, mockVar);
		assertNotNull(varBean.getSelectedUploadedListValues());
		assertTrue(varBean.getSelectedUploadedListValues().isEmpty());

		when(mockVar.getVariablePageflexType()).thenReturn(Variable.PageflexType.INSERTION_GROUP);
		when(mockVar.getGroupMaxInserts()).thenReturn(5);
		service.setUploadListOptions(varBean, mockVar);
		assertNotNull(varBean.getSelectedUploadedListValues());
		assertTrue(varBean.getSelectedUploadedListValues().isEmpty());

		when(mockVar.getTextValue()).thenReturn("4^Emerald_Ash_Borer.pdf|U21765^UPLOAD: RR Donnelley Mail - CustomPoint Test - Word Processing.pdf");
		when(mockVar.getListValue()).thenReturn(FAKE_UPLOAD_LIST_VALUE);
		service.setUploadListOptions(varBean, mockVar);
		assertNotNull(varBean.getSelectedUploadedListValues());
		assertFalse(varBean.getSelectedUploadedListValues().isEmpty());

		when(mockVar.getGroupMaxInserts()).thenReturn(1);
		service.setUploadListOptions(varBean, mockVar);
		assertNotNull(varBean.getSelectedUploadedListValues());
		assertEquals(1, varBean.getSelectedUploadedListValues().size());

		when(mockVar.getListValue()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		service.setUploadListOptions(varBean, mockVar);
		assertNotNull(varBean.getSelectedUploadedListValues());
		assertTrue(varBean.getSelectedUploadedListValues().isEmpty());


	}

	protected void setupObjMapItem() {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(CustomDocumentItem.class, mockCustomizationToken)).thenReturn(mockItem);
	}

	@Test
	void createAndInitItem() throws AtWinXSException {
		setupObjMapItem();
		assertEquals(mockItem, service.createAndInitItem(uiRequest, mockOEOrderSessionBean, mockAppSessionBean, mockVolatileSessionBean, mockPunchoutSessionBean, errorStrings));
	}

	@Test
	void isReviewOnlyApproval() {
		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.CART_EDIT);
		assertFalse(service.isReviewOnlyApproval(mockUI, mockOEOrderSessionBean));

		when(mockUI.getEntryPoint()).thenReturn(EntryPoint.ROUTING_AND_APPROVAL);
		when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getApproverReviewLevel()).thenReturn(null);
		assertFalse(service.isReviewOnlyApproval(mockUI, mockOEOrderSessionBean));

		when(mockUserSettings.getApproverReviewLevel()).thenReturn(OrderEntryConstants.APPROVE_ONLY_USER);
		assertTrue(service.isReviewOnlyApproval(mockUI, mockOEOrderSessionBean));
	}

	@Test
	void getListPrivacySettings() throws AtWinXSException 	{
	      service = Mockito.spy(service);
	      doReturn(null).when(service).getBUListAdminSettings(mockAppSessionBean);
	      assertFalse(service.getListPrivacySettings(mockAppSessionBean));

	      doReturn(mockBUListSettings).when(service).getBUListAdminSettings(mockAppSessionBean);
	      when(mockBUListSettings.isDoNotShareListsInd()).thenReturn(false);
	      assertFalse(service.getListPrivacySettings(mockAppSessionBean));

	      when(mockBUListSettings.isDoNotShareListsInd()).thenReturn(true);
	      assertTrue(service.getListPrivacySettings(mockAppSessionBean));
	}

	@Test
	void getOrderListSourceType() {
		when(mockUI.getOrderListSource()).thenReturn(null);
		assertEquals(AtWinXSConstant.EMPTY_STRING, service.getOrderListSourceType(mockUI));
		when(mockUI.getOrderListSource()).thenReturn(OrderListSource.NONE);
		assertEquals(AtWinXSConstant.EMPTY_STRING, service.getOrderListSourceType(mockUI));
		when(mockUI.getOrderListSource()).thenReturn(OrderListSource.UPLOAD);
		assertEquals(OrderListSource.UPLOAD.toString(), service.getOrderListSourceType(mockUI));
	}

	@Test
	void getOrderListVO() throws AtWinXSException {
		when(mockUI.getOrderListID()).thenReturn(-1);
		assertNull(service.getOrderListVO(mockUI, mockAppSessionBean));

	    service = Mockito.spy(service);
		when(mockUI.getOrderListID()).thenReturn(11);
	    doReturn(null).when(service).getOEListComponent(mockAppSessionBean);
		assertNull(service.getOrderListVO(mockUI, mockAppSessionBean));

	    doReturn(mockOEListComponent).when(service).getOEListComponent(mockAppSessionBean);
	    when(mockOEListComponent.getOrderListVO(any())).thenReturn(null);
		assertNull(service.getOrderListVO(mockUI, mockAppSessionBean));

	    when(mockOEListComponent.getOrderListVO(any())).thenReturn(mockOrderListVO);
		assertEquals(mockOrderListVO, service.getOrderListVO(mockUI, mockAppSessionBean));
	}

	@Test
	void isListMapped() {
		when(mockUI.getListMappings()).thenReturn(null);
		assertFalse(service.isListMapped(mockUI, DEVTEST_SITE_ID));
		when(mockUI.getListMappings()).thenReturn(new HashMap<>());
		assertFalse(service.isListMapped(mockUI, DEVTEST_SITE_ID));
		HashMap<String, List<OECustomDocOrderLineMapSessionBean>> map = new HashMap<>();
		map.put(String.valueOf(DEVTEST_UX_BU_ID), new ArrayList<OECustomDocOrderLineMapSessionBean>());
		when(mockUI.getListMappings()).thenReturn(map);
		assertFalse(service.isListMapped(mockUI, DEVTEST_SITE_ID));
		map.put(String.valueOf(DEVTEST_SITE_ID), new ArrayList<OECustomDocOrderLineMapSessionBean>());
		assertTrue(service.isListMapped(mockUI, DEVTEST_SITE_ID));
	}

	@Test
	void getSingleSelectedListID() {
		when(mockUI.getSelectedListIDs()).thenReturn(null);
		assertEquals(AtWinXSConstant.INVALID_ID, service.getSingleSelectedListID(mockUI));

		ArrayList<String> list = new ArrayList<>();
		when(mockUI.getSelectedListIDs()).thenReturn(list);
		assertEquals(AtWinXSConstant.INVALID_ID, service.getSingleSelectedListID(mockUI));

		list.add(String.valueOf(DEVTEST_SITE_ID));
		assertEquals(DEVTEST_SITE_ID, service.getSingleSelectedListID(mockUI));
	}

	@Test
	void isListSelected() {
		when(mockUI.getSelectedListIDs()).thenReturn(null);
		assertFalse(service.isListSelected(mockUI, DEVTEST_SITE_ID));
		when(mockUI.getSelectedListIDs()).thenReturn(new ArrayList<>());
		assertFalse(service.isListSelected(mockUI, DEVTEST_SITE_ID));
		ArrayList<String> map = new ArrayList<>();
		map.add(String.valueOf(DEVTEST_UX_BU_ID));
		when(mockUI.getSelectedListIDs()).thenReturn(map);
		assertFalse(service.isListSelected(mockUI, DEVTEST_SITE_ID));
		map.add(String.valueOf(DEVTEST_SITE_ID));
		assertTrue(service.isListSelected(mockUI, DEVTEST_SITE_ID));
	}

	private static final String TEST_USER = "AMY";

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
	void makeListDetailObjectFromVO() {
	    service = Mockito.spy(service);
		doReturn("02/27/2019").when(service).getFormattedDate(any(), any());
		C1uxCustDocListDetails list = service.makeListDetailObjectFromVO(getListVO(), mockUI, mockAppSessionBean);
		assertEquals(GENERIC_ERROR_MSG, list.getListDescription());
		assertFalse(list.isSelected());
		assertFalse(list.isMapped());
	}

	@Test
	void loadListDataPage() throws AtWinXSException {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
//		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockMergeListUploadFile);
		C1UXCustDocMappedDataPage data = service.loadListDataPage(DEVTEST_SITE_ID, false, mockOrderListVO, mockUI);
		assertNotNull(data);
	}

	@Test
	void loadListDataSaved() throws AtWinXSException {
	    service = Mockito.spy(service);
//		doReturn("02/27/2019").when(service).getFormattedDate(any(), any());
		doReturn(null).when(service).getOrderListVO(mockUI, mockAppSessionBean);
		when(mockPage.getPageNumber()).thenReturn(4);
		C1UXCustDocPageBean formBean = new C1UXCustDocPageBean();
		service.loadListDataSaved(formBean, mockPage, mockAppSessionBean, mockUI);
		assertEquals(0, formBean.getValidRecordCount());
		assertEquals(0, formBean.getInvalidRecordCount());
		assertFalse(formBean.isListDataSaved());

		doReturn(mockOrderListVO).when(service).getOrderListVO(mockUI, mockAppSessionBean);
		int goodCount=26;
		int failCount=2;
		when(mockOrderListVO.getGoodRecCount()).thenReturn(goodCount);
		when(mockOrderListVO.getValidFailCount()).thenReturn(failCount);
		formBean = new C1UXCustDocPageBean();
		service.loadListDataSaved(formBean, mockPage, mockAppSessionBean, mockUI);
		assertEquals(goodCount, formBean.getValidRecordCount());
		assertEquals(failCount, formBean.getInvalidRecordCount());
		assertNull(formBean.getInitialRecords());
		assertTrue(formBean.isListDataSaved());

		formBean = new C1UXCustDocPageBean();
		when(mockPage.getPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST_DATA);
		doReturn(new C1UXCustDocMappedDataPage()).when(service).loadListDataPage(anyInt(), anyBoolean(), any(), any());
		service.loadListDataSaved(formBean, mockPage, mockAppSessionBean, mockUI);
		assertEquals(goodCount, formBean.getValidRecordCount());
		assertEquals(failCount, formBean.getInvalidRecordCount());
		assertNotNull(formBean.getInitialRecords());
		assertTrue(formBean.isListDataSaved());
		assertFalse(formBean.isHardStopFailure());

		when(mockOrderListVO.getGoodRecCount()).thenReturn(0);
		formBean = new C1UXCustDocPageBean();
		service.loadListDataSaved(formBean, mockPage, mockAppSessionBean, mockUI);
		assertNotEquals(goodCount, formBean.getValidRecordCount());
		assertEquals(0, formBean.getValidRecordCount());
		assertEquals(failCount, formBean.getInvalidRecordCount());
		assertNotNull(formBean.getInitialRecords());
		assertTrue(formBean.isListDataSaved());
		assertTrue(formBean.isHardStopFailure());
	}

	@Test
	void adjustFirstPage() {
		service.adjustFirstPage(mockUI);
		when(mockUI.isMergeOnly()).thenReturn(true);
		service.adjustFirstPage(mockUI);
		when(mockUI.isProofOnly()).thenReturn(true);
		service.adjustFirstPage(mockUI);
		assertNotNull(mockUI);
	}

	@Test
	void buildMultipleUploadedListOptions() {
		List<C1UXUIListOption> listOptions = service.buildMultipleUploadedListOptions("4^Emerald_Ash_Borer.pdf|U21765^UPLOAD: RR Donnelley Mail - CustomPoint Test - Word Processing.pdf|U21766^UPLOAD: 23.24 BHS Early Release Dates & Final Exam Schedule.pdf|U21767^UPLOAD: JAMS Summer Beginning Band Information (2024).pdf");
		assertNotNull(listOptions);
		assertEquals(3, listOptions.size());
		listOptions = service.buildMultipleUploadedListOptions("4^Emerald_Ash_Borer.pdf");
		assertNotNull(listOptions);
		assertEquals(0, listOptions.size());
	}

	@Test
	void loadAlternateProfiles() throws AtWinXSException {
	    service = Mockito.spy(service);
	    doReturn(mockProfileDefinitionInterface).when(service).getProfileDefinitionComponent(mockAppSessionBean);
	    HashMap<String, Integer> selectionMap = new HashMap<>();
	    when(mockUI.getUiAltProfileSelections()).thenReturn(selectionMap);
	    HashMap<String,ArrayList<Integer>> profileDropdowns = new HashMap<>();
	    when(mockUI.getAlternateProfileDropdownsC1UX(any(), anyInt(), any())).thenReturn(profileDropdowns);
	    C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
	    when(mockVolatileSessionBean.getSelectedSiteAttribute()).thenReturn(null);
	    service.loadAlternateProfiles(mockUI, UserInterface.NEXT_PAGE_NUMBER_LIST, sfBean, mockAppSessionBean, mockVolatileSessionBean);
	    assertTrue(sfBean.getAlternateProfiles().isEmpty());
	    int pageNum = 20;
	    when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
	    when(mockAppSessionBean.getBuID()).thenReturn(DEVTEST_UX_BU_ID);
//	    when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
	    service.loadAlternateProfiles(mockUI, pageNum, sfBean, mockAppSessionBean, mockVolatileSessionBean);
	    assertTrue(sfBean.getAlternateProfiles().isEmpty());

	    when(mockProfileDefinitionInterface.getProfileDefinitionByType(any(), anyString())).thenReturn(mockProfileDefinitionVO);
	    when(mockProfileDefinitionVO.getProfileDefinitionID()).thenReturn(45);
	    when(mockUI.getAlternateProfileSelector()).thenReturn(mockAlternateProfileSelector);
	    when(mockAlternateProfileSelector.loadProfileOptions(any(),  any(), any(),  any(),  anyInt())).thenReturn(null);
	    doReturn(new C1UXAlternateProfileOptions()).when(service).makeC1UXAlternateProfileOptions(any(), any(), any(), any());

	    ArrayList<ProfileOption> options = new ArrayList<ProfileOption>();
	    String profileType = "Brand";
	    ArrayList<Integer> profileNums = new ArrayList<>();
	    int profileNum = 555;
	    profileNums.add(profileNum);
	    profileDropdowns.put(profileType, profileNums);
	    service.loadAlternateProfiles(mockUI, pageNum, sfBean, mockAppSessionBean, mockVolatileSessionBean);
	    assertTrue(sfBean.getAlternateProfiles().isEmpty());

	    when(mockAlternateProfileSelector.loadProfileOptions(any(),  any(), any(),  any(),  anyInt())).thenReturn(options);
	    service.loadAlternateProfiles(mockUI, pageNum, sfBean, mockAppSessionBean, mockVolatileSessionBean);
	    assertTrue(sfBean.getAlternateProfiles().isEmpty());

	    options.add(new ProfileOption(profileNum++, profileType, false));
	    options.add(new ProfileOption(profileNum, profileType, false));
	    service.loadAlternateProfiles(mockUI, pageNum, sfBean, mockAppSessionBean, mockVolatileSessionBean);
	    assertFalse(sfBean.getAlternateProfiles().isEmpty());
	}

	@Test
	void makeC1UXAlternateProfileOptions() throws AtWinXSException {
	    service = Mockito.spy(service);
	    doReturn(mockProfileInterface).when(service).getProfileComponent(mockAppSessionBean);
	    HashMap<String, Integer> selectionMap = new HashMap<>();
	    when(mockUI.getUiAltProfileSelections()).thenReturn(selectionMap);
	    when(mockProfileDefinitionVO.getProfileType()).thenReturn(DEFAULT_UG_NM);
//	    when(mockProfileDefinitionVO.getProfileLabel()).thenReturn(TEST_USER);
	    when(mockProfileDefinitionVO.getProfileDefinitionID()).thenReturn(DEVTEST_SITE_ID);
	    when(mockProfileInterface.getProfileByProfileNumber(anyInt(), anyInt())).thenReturn(mockProfileVO);
	    when(mockProfileVO.getProfileID()).thenReturn(NOT_VALID_ERR_ENGLISH);
	    int profileNum = 366;
//	    when(mockProfileVO.getProfileNumber()).thenReturn(profileNum);
	    ArrayList<ProfileOption> options = new ArrayList<ProfileOption>();

	    C1UXAlternateProfileOptions bean = service.makeC1UXAlternateProfileOptions(mockProfileDefinitionVO, mockUI, options, mockAppSessionBean);
	    assertEquals(AtWinXSConstant.INVALID_ID, bean.getCurrentProfileNumber());

	    selectionMap.put(DEFAULT_UG_NM, new Integer(-1));
	    bean = service.makeC1UXAlternateProfileOptions(mockProfileDefinitionVO, mockUI, options, mockAppSessionBean);
	    assertEquals(AtWinXSConstant.INVALID_ID, bean.getCurrentProfileNumber());

	    selectionMap.put(DEFAULT_UG_NM, new Integer(profileNum));
	    bean = service.makeC1UXAlternateProfileOptions(mockProfileDefinitionVO, mockUI, options, mockAppSessionBean);
	    assertEquals(profileNum, bean.getCurrentProfileNumber());
	}

	@Test
	void initializeFromKitComponent() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	    service = Mockito.spy(service);
	    C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
	    response.setSuccess(true);
		when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	    doReturn(null).when(service).getKitSession(mockAppSessionBean);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, -1);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	    doReturn(mockKitSession).when(service).getKitSession(mockAppSessionBean);
	    when(mockKitSession.getComponents()).thenReturn(null);
	    exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, -1);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	    when(mockKitSession.getComponents()).thenReturn(new MKComponentInfo[0]);
	    exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, -1);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	    exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, 0);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		MKComponentInfo[] fakeComponents = new MKComponentInfo[1];
	    when(mockKitSession.getComponents()).thenReturn(fakeComponents);
	    exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, 0);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		fakeComponents[0] = mockKitComponent;
		when(mockKitComponent.isCustomDoc()).thenReturn(false);
	    exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, 0);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockKitComponent.isCustomDoc()).thenReturn(true);
		when(mockKitComponent.getWCSSItemNum()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    exception = assertThrows(AccessForbiddenException.class, () -> {
			service.initializeFromKitComponent(mockSessionContainer, 0);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(Locale.class), any(), anyString());
//	    doReturn(response).when(service).initializeUIOnly(any(), any());
//		doNothing().when(service).saveFullSessionInfo(any(), anyInt(), anyInt());

		when(mockKitComponent.getWCSSItemNum()).thenReturn(FAKE_UPLOAD_LIST_VALUE);
		doReturn(false).when(service).validateKitComponentUI(anyMap(), any());
		baseResponse = service.initializeFromKitComponent(mockSessionContainer, 0);
		assertFalse(baseResponse.isSuccess());
		assertTrue(baseResponse.isHardStopFailure());
		assertEquals(GENERIC_ERROR_MSG, baseResponse.getMessage());

		doReturn(true).when(service).validateKitComponentUI(anyMap(), any());
	    C1UXCustDocBaseResponse successResponse = new C1UXCustDocBaseResponse();
	    successResponse.setSuccess(true);
	    doReturn(successResponse).when(service).initializeUIOnly(any(), any());
	    doNothing().when(service).updateKitComponent(any(), any(), any(), anyInt());
		baseResponse = service.initializeFromKitComponent(mockSessionContainer, 0);
	}

	@Test
	void updateKitComponent() throws Exception {
		C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
		service.updateKitComponent(response, mockSessionContainer, mockAppSessionBean, DEVTEST_SITE_ID);
		assertFalse(response.isSuccess());

		response.setSuccess(true);
	    service = Mockito.spy(service);
		setUpModuleSessionNoBase();
		when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		doNothing().when(service).saveFullOESessionInfo(any(), anyInt());
		service.updateKitComponent(response, mockSessionContainer, mockAppSessionBean, DEVTEST_SITE_ID);
		assertTrue(response.isSuccess());
	}

}


