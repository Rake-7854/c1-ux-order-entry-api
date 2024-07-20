/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  08/23/23    A Boomker       CAP-43223                   Initial
 *	09/18/23	A Boomker		CAP-42298					Add cancel methods direct access
 *  10/26/23    AKJ Omisol		CAP-43024					Add test cases for getImprintHistory
 *	03/14/24	A Boomker		CAP-46526					Adding handling for initialize from URL
 *	07/01/24	A Boomker		CAP-46488 					Add entry for kit template custom docs
 */
package com.rrd.c1ux.api.controllers.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocEditKTOERequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeFromURLRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUIPageSubmitRequest;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.tt.vo.TTSession;

@WithMockUser
public class CustomDocsUserInterfaceControllerTests  extends BaseMvcTest {

	Map<String, String> uiRequest = null;
	Collection<String> errorStrings = null;
	public static final String NEW_LINE_CHAR = "\n";

	private C1UXCustDocUIPageSubmitRequest saveRequest;
//	private C1UXCustDocBaseResponse baseResponse;
	private C1UXCustDocInitializeRequest initializeRequest;
//	private C1UXCustDocPageBean pageResponse;

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

	@Mock
	private C1UXCustDocLoadImprintHistoryRequest mockRequest;

	@InjectMocks
	protected CustomDocsUserInterfaceController controller;

	@BeforeEach
		public void setUp() throws Exception {
		controller = new CustomDocsUserInterfaceController(mockTokenReader, mockSessionReader, mockCustDocsUIService);
		controller = Mockito.spy(controller);
		    setupBaseMockSessions();
			when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
			TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		    when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(true);
		}

	private C1UXCustDocUIPageSubmitRequest getSaveRequest() {
		saveRequest = new C1UXCustDocUIPageSubmitRequest();
		return saveRequest;
	}

	private void populateSaveRequest(C1UXCustDocUIPageSubmitRequest req) {
		req.setEventAction(UserInterface.UIEvent.NAVIGATE_PAGES.toString());
		req.setForm1(TEST_ENCRYPTED_SESSIONID);
		req.setOtherSerializedForm(GENERIC_ERROR_MSG);
		req.setValidate(false);
	}


	private C1UXCustDocBaseResponse getSaveResponseSuccess() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setSuccess(true);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getSaveResponseFailure() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setMessage(GENERIC_ERROR_MSG);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getAddToCartResponseSuccess() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setSuccess(true);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getAddToCartResponseFailure() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setMessage(GENERIC_ERROR_MSG);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getInitializeResponseSuccess() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setSuccess(true);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getInitializeResponseFailure() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setMessage(GENERIC_ERROR_MSG);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getCancelResponseSuccess() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setSuccess(true);
		return baseResponse;
	}

	private C1UXCustDocBaseResponse getCancelResponseFailure() {
		C1UXCustDocBaseResponse baseResponse = new C1UXCustDocBaseResponse();
		baseResponse.setMessage(GENERIC_ERROR_MSG);
		return baseResponse;
	}

	private C1UXCustDocInitializeRequest getInitializeRequest() {
		initializeRequest = new C1UXCustDocInitializeRequest();
		return initializeRequest;
	}

	private void populateInitializeRequest(C1UXCustDocInitializeRequest req) {
		req.setEntryPoint(UserInterface.EntryPoint.CATALOG_NORMAL.toString());
	}

	private C1UXCustDocPageBean getPageResponseSuccess() {
		C1UXCustDocPageBean pageResponse = new C1UXCustDocPageBean();
		pageResponse.setSuccess(true);
		return pageResponse;
	}

	private C1UXCustDocPageBean getPageResponseFailure() {
		C1UXCustDocPageBean pageResponse = new C1UXCustDocPageBean();
		pageResponse.setMessage(GENERIC_ERROR_MSG);
		return pageResponse;
	}

	@Test
	void that_initialize_returnsExpected() throws Exception, AtWinXSException {
		C1UXCustDocInitializeRequest req = getInitializeRequest();
//	    String requestString = getJsonRequest(req);

		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseFailure());
//	    check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.initializeUI(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseSuccess());
//	    checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.OK, controller.initializeUI(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	    populateInitializeRequest(req);
//	    requestString = getJsonRequest(req);
//	    checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.OK, controller.initializeUI(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

/*	    when(mockCustDocsUIService.getItemClassFromRequest(any(SessionContainer.class), anyMap())).thenReturn(ItemConstants.ITEM_CLASS_BUNDLE);
	    populateInitializeRequest(req);
	    requestString = getJsonRequest(req);
	    checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

	    when(mockCustDocsUIService.getItemClassFromRequest(any(SessionContainer.class), anyMap())).thenReturn(ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST);
	    populateInitializeRequest(req);
	    requestString = getJsonRequest(req);
	    checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
*/
	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(false);
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.initializeUI(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));


		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
		exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.initializeUI(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	// CAP-46526 - new cases for initialize from URL
	private static final String PREFIX_INITIALIZE_ITEM_URL = "https://dev.custompoint.rrd.com/cp/orders/customdocs/ui.cp?catalogLnNbr=-1&itemNumber=10.1875+WIDTH&vendorItemNumber=74101875X1000&ORDER_LINE_NUM=&PARAM_CUST_DOC_ORDER_LINE_ID=&cdEntryPoint=CATEXPR&PARAM_ITEM_INX=&UOM=&PARAM_ORDER_QTY=0&ttsessionid=";
	private static final String PREFIX_INITIALIZE_NEW_REQUEST_URL = "https://dev.custompoint.rrd.com/cp/orders/customdocs/ui.cp?cdEntryPoint=NEWREQ&txtProjectId=12354&hdnUINumber=5&hdnUIVersion=1&ttsessionid=";
	private static final String OLD_ENCODED_SESSION_ID = "VjM2cVptd0tpYytMZzRNK2tkVmg3VTNuM1A4ZHZMbkVEQ0hRUUFDL09kOD0=";
	private static final String NEW_ENCODED_SESSION_ID = "OGxQdzZFdXNqamFXTHpNRzFxRmdlRkZvQWREVFlhbnp5U1AvVlNCa1BFdz0=";
	private static final int NEW_ENCODED_SESSION_ID_INTEGER = 1483647;
	private C1UXCustDocInitializeFromURLRequest getInitializeURLRequestNewRequest() {
		C1UXCustDocInitializeFromURLRequest initializeRequest = new C1UXCustDocInitializeFromURLRequest();
		initializeRequest.setUrl(PREFIX_INITIALIZE_NEW_REQUEST_URL + NEW_ENCODED_SESSION_ID);
		return initializeRequest;
	}


	private C1UXCustDocInitializeFromURLRequest getInitializeURLRequestCustomDocument() {
		C1UXCustDocInitializeFromURLRequest initializeRequest = new C1UXCustDocInitializeFromURLRequest();
		initializeRequest.setUrl(PREFIX_INITIALIZE_ITEM_URL + NEW_ENCODED_SESSION_ID);
		return initializeRequest;
	}

	private C1UXCustDocInitializeFromURLRequest getInitializeURLRequestMissingPrefix() {
		C1UXCustDocInitializeFromURLRequest initializeRequest = new C1UXCustDocInitializeFromURLRequest();
		initializeRequest.setUrl(NEW_ENCODED_SESSION_ID);
		return initializeRequest;
	}

	private C1UXCustDocInitializeFromURLRequest getInitializeURLRequestMissingSession() {
		C1UXCustDocInitializeFromURLRequest initializeRequest = new C1UXCustDocInitializeFromURLRequest();
		initializeRequest.setUrl(PREFIX_INITIALIZE_ITEM_URL);
		return initializeRequest;
	}

	private C1UXCustDocInitializeFromURLRequest getInitializeURLRequestBadSession() { // this one is not parseable
		C1UXCustDocInitializeFromURLRequest initializeRequest = new C1UXCustDocInitializeFromURLRequest();
		initializeRequest.setUrl(PREFIX_INITIALIZE_ITEM_URL + "abc123");
		return initializeRequest;
	}

	private C1UXCustDocInitializeFromURLRequest getInitializeURLRequestNonMatchingSession() {
		C1UXCustDocInitializeFromURLRequest initializeRequest = new C1UXCustDocInitializeFromURLRequest();
		initializeRequest.setUrl(PREFIX_INITIALIZE_ITEM_URL + OLD_ENCODED_SESSION_ID);
		return initializeRequest;
	}

	private TTSession getTTSessionForOldID() {
	   	TTSession ttSession =  new TTSession();
	   	ttSession.setId(55555); // this doesn't match
	   	ttSession.setTokenPassedIn(OLD_ENCODED_SESSION_ID);
	   	return ttSession;
	}

	private TTSession getTTSessionForNewID() {
	   	TTSession ttSession =  new TTSession();
	   	ttSession.setId(NEW_ENCODED_SESSION_ID_INTEGER); // this doesn't match
	   	ttSession.setTokenPassedIn(NEW_ENCODED_SESSION_ID);
	   	ttSession.setStatus("A");
	   	return ttSession;
	}


	@Test
	void that_initializeFromURL_returnsExpected_goodFormat() throws Exception, AtWinXSException {
		C1UXCustDocInitializeFromURLRequest req = getInitializeURLRequestCustomDocument();
//		C1UXCustDocInitializeFromURLRequest req2 = getInitializeURLRequestNewRequest();
//	    String requestString = getJsonRequest(req);
//	    String requestString2 = getJsonRequest(req2);
	    TTSession newSession = getTTSessionForNewID();

	    when(mockAppSessionBean.getEncodedSessionId()).thenReturn(NEW_ENCODED_SESSION_ID);
	    when(mockAppSessionBean.getSessionID()).thenReturn(NEW_ENCODED_SESSION_ID_INTEGER); // this was looked up in sysfunc and goes with the new encoded session ID
	    when(mockSessionReader.getTTSession(any())).thenReturn(newSession);
		when(mockCustDocsUIService.getTranslation(any(), any(), anyString())).thenReturn(GENERIC_ERROR_MSG);
		doReturn(mockCustDocsUIService).when(controller).makeCustomDocsServiceImpl(mockCustDocsUIService, mockSessionContainer);

	    when(mockCustDocsUIService.getItemClassFromRequest(any(SessionContainer.class), anyMap())).thenReturn(ItemConstants.ITEM_CLASS_CUSTOM_DOC);
		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseFailure());
//		check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, req).getStatusCode());

		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseSuccess());
//	    checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString, AtWinXSConstant.EMPTY_STRING, NEW_ENCODED_SESSION_ID);
	       assertEquals(HttpStatus.OK, controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, req).getStatusCode());

		/*
 * Not sure how to mock it when it creates its own service that extends the original in order to make these calls.
	    when(mockCustDocsUIService.getItemClassFromRequest(any(SessionContainer.class), anyMap())).thenReturn(ItemConstants.ITEM_CLASS_BUNDLE);
		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseFailure());
		check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);

		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseSuccess());
		checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString, AtWinXSConstant.EMPTY_STRING, NEW_ENCODED_SESSION_ID);

	    when(mockCustDocsUIService.getItemClassFromRequest(any(SessionContainer.class), anyMap())).thenReturn(ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST);
		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseFailure());
		check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString2, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);

		when(mockCustDocsUIService.initializeUIOnly(any(SessionContainer.class), anyMap()))
			.thenReturn(getInitializeResponseSuccess());
		checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString2, AtWinXSConstant.EMPTY_STRING, NEW_ENCODED_SESSION_ID);
*/
	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(false);
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
	    Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestString, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
		exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	    // these tests have badFormat
		C1UXCustDocInitializeFromURLRequest reqMissingPrefix = getInitializeURLRequestMissingPrefix();
		C1UXCustDocInitializeFromURLRequest reqMissingSession = getInitializeURLRequestMissingSession();
		C1UXCustDocInitializeFromURLRequest reqBadSession = getInitializeURLRequestBadSession();
		C1UXCustDocInitializeFromURLRequest reqNonMatchingSession = getInitializeURLRequestNonMatchingSession();
//	    String requestStringEmpty = getJsonRequest(new C1UXCustDocInitializeFromURLRequest());
//	    String requestStringMissingPrefix = getJsonRequest(reqMissingPrefix);
//	    String requestStringMissingSession = getJsonRequest(reqMissingSession);
//	    String requestStringBadSession = getJsonRequest(reqBadSession);
//	    String requestStringNonMatchingSession = getJsonRequest(reqNonMatchingSession);
	    TTSession oldSession = getTTSessionForOldID();

	    when(mockSessionReader.getTTSession(any())).thenReturn(newSession);
	    when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer); // we must get a valid session

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);

//	    check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestStringMissingPrefix, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
//	    check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestStringEmpty, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, reqMissingPrefix).getStatusCode());
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, new C1UXCustDocInitializeFromURLRequest()).getStatusCode());

	    // the last three cases should throw 403 errors
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestStringMissingSession, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
	       exception = assertThrows(AccessForbiddenException.class, () -> {
				controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, reqMissingSession);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	    when(mockSessionReader.getTTSession(any())).thenReturn(oldSession);
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestStringNonMatchingSession, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
	       exception = assertThrows(AccessForbiddenException.class, () -> {
				controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, reqNonMatchingSession);
			});
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	    when(mockSessionReader.getTTSession(any())).thenThrow(new AtWinXSException("I blew up here", this.getClass().getName()));
//	    check403Response(RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, requestStringBadSession, GENERIC_ERROR_MSG, NEW_ENCODED_SESSION_ID);
	    exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.initializeUIFromURL(NEW_ENCODED_SESSION_ID, reqBadSession);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}


	@Test
	void that_savePage_returnsExpected() throws Exception, AtWinXSException {
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
	    C1UXCustDocUIPageSubmitRequest req = getSaveRequest();
//		String requestString = getJsonRequest(req);
		doReturn(mockCustDocsUIService).when(controller).getClassSpecificService(mockCustDocsUIService, mockSessionContainer);

		when(mockCustDocsUIService.performPageSubmitAction(any(SessionContainer.class), anyMap()))
			.thenReturn(getSaveResponseFailure());
//	    check422Response(RouteConstants.CUST_DOCS_UI_PAGE_SUBMIT_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.submitUIPage(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

		when(mockCustDocsUIService.performPageSubmitAction(any(SessionContainer.class), anyMap()))
			.thenReturn(getSaveResponseSuccess());
//	    checkStandard200Response(RouteConstants.CUST_DOCS_UI_PAGE_SUBMIT_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
	       assertEquals(HttpStatus.OK, controller.submitUIPage(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	    populateSaveRequest(req);
//		requestString = getJsonRequest(req);
		when(mockCustDocsUIService.performPageSubmitAction(any(SessionContainer.class), anyMap()))
			.thenReturn(getSaveResponseSuccess());
//		checkStandard200Response(RouteConstants.CUST_DOCS_UI_PAGE_SUBMIT_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
	       assertEquals(HttpStatus.OK, controller.submitUIPage(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	}

	@Test
	void that_getUIPage_returnsExpected() throws Exception, AtWinXSException {
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
     //   String requestString = AtWinXSConstant.EMPTY_STRING;
		doReturn(mockCustDocsUIService).when(controller).getClassSpecificService(mockCustDocsUIService, mockSessionContainer);

        when(mockCustDocsUIService.getCurrentPageUI(any(SessionContainer.class)))
				.thenReturn(getPageResponseSuccess());
//	    checkStandard200Response(RouteConstants.CUST_DOCS_UI_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.OK, controller.getUIPage(AtWinXSConstant.EMPTY_STRING).getStatusCode());


        when(mockCustDocsUIService.getCurrentPageUI(any(SessionContainer.class)))
				.thenReturn(getPageResponseFailure());
//        check422Response(RouteConstants.CUST_DOCS_UI_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.getUIPage(AtWinXSConstant.EMPTY_STRING).getStatusCode());
	}

	@Test
	void that_addToCart_returnsExpected() throws Exception, AtWinXSException {
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
     //   String requestString = AtWinXSConstant.EMPTY_STRING;
		doReturn(mockCustDocsUIService).when(controller).getClassSpecificService(mockCustDocsUIService, mockSessionContainer);

        when(mockCustDocsUIService.addToCart(any(SessionContainer.class)))
				.thenReturn(getAddToCartResponseSuccess());
//	    checkStandard200Response(RouteConstants.CUST_DOCS_ADD_TO_CART_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.OK, controller.addToCart(AtWinXSConstant.EMPTY_STRING).getStatusCode());


        when(mockCustDocsUIService.addToCart(any(SessionContainer.class)))
				.thenReturn(getAddToCartResponseFailure());
 //       check422Response(RouteConstants.CUST_DOCS_ADD_TO_CART_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.addToCart(AtWinXSConstant.EMPTY_STRING).getStatusCode());
	}

	@Test
	void that_cancelActions_returnsExpected() throws Exception, AtWinXSException {
	    when(mockOEOrderSession.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
   //     String requestString = AtWinXSConstant.EMPTY_STRING;
		doReturn(mockCustDocsUIService).when(controller).getClassSpecificService(mockCustDocsUIService, mockSessionContainer);

        when(mockCustDocsUIService.cancelAction(any(SessionContainer.class), anyBoolean()))
				.thenReturn(getCancelResponseSuccess());
        when(mockCustDocsUIService.getItemClassFromSession(mockSessionContainer)).thenReturn(null);
//	    checkStandard200Response(RouteConstants.CUST_DOCS_CANCEL_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
//	    checkStandard200Response(RouteConstants.CUST_DOCS_CANCEL_ORDER_API, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);
	       assertEquals(HttpStatus.OK, controller.cancel(AtWinXSConstant.EMPTY_STRING).getStatusCode());
	       assertEquals(HttpStatus.OK, controller.cancelOrder(AtWinXSConstant.EMPTY_STRING).getStatusCode());


        when(mockCustDocsUIService.cancelAction(any(SessionContainer.class), anyBoolean()))
				.thenReturn(getCancelResponseFailure());
 //       check422Response(RouteConstants.CUST_DOCS_CANCEL_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
 //       check422Response(RouteConstants.CUST_DOCS_CANCEL_ORDER_API, requestString, GENERIC_ERROR_MSG, TEST_ENCRYPTED_SESSIONID);
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.cancel(AtWinXSConstant.EMPTY_STRING).getStatusCode());
	       assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.cancelOrder(AtWinXSConstant.EMPTY_STRING).getStatusCode());
	}

	// CAP-46488 - add entry for kit template custom docs
	@Test
	void that_editCustDocInKit_returnsExpected() throws Exception, AtWinXSException {
		C1UXCustDocEditKTOERequest req = new C1UXCustDocEditKTOERequest();

		when(mockCustDocsUIService.initializeFromKitComponent(any(SessionContainer.class), anyInt()))
			.thenReturn(getInitializeResponseFailure());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.editCustDocInKit(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

		when(mockCustDocsUIService.initializeFromKitComponent(any(SessionContainer.class), anyInt()))
			.thenReturn(getInitializeResponseSuccess());
        assertEquals(HttpStatus.OK, controller.editCustDocInKit(AtWinXSConstant.EMPTY_STRING, req).getStatusCode());

	    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(false);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.editCustDocInKit(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);
		exception = assertThrows(AccessForbiddenException.class, () -> {
			controller.editCustDocInKit(AtWinXSConstant.EMPTY_STRING, req);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}



}
