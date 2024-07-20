/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  09/14/23	A Boomker		CAP-42843					initial
 */
package com.rrd.c1ux.api.controllers.shoppingcart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.custdocs.CustomDocsUserInterfaceController;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@WithMockUser
class ShoppingCartControllerTests extends BaseMvcTest {

	private static final String GENERIC_ERROR_MSG = "Failure message generic";
	String TEST_ENCRYPTED_SESSIONID="tbyuyduydyu";
	  @Mock
	  protected TranslationService mockTranslationService;

	@InjectMocks
	ShoppingCartController controller;

	@Mock
	protected OrderEntrySession mockOESession;
	@Mock
	protected OEOrderSessionBean mockOEOrderSession;
		@Mock
		protected OEResolvedUserSettingsSessionBean mockUserSettings;
		@Mock
		private ShoppingCartService mockService;
		@Mock
		private CustomDocsService mockCDService;
		@Mock
		private CustomDocsUserInterfaceController mockCDController;
		@Mock
		private ResponseEntity<C1UXCustDocBaseResponse> mockBaseResponseEntity;

	@BeforeEach
		public void setUp() throws Exception {
		    setupBaseMockSessions();
//		    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString(), any());
			when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
			TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
//			when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
//		    when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
//		    when(mockUserSettings.isAllowCustomDocumentsInd()).thenReturn(true);
		}

	private CustDocEditCartResponse getInitializeResponseSuccess() {
		CustDocEditCartResponse baseResponse = new CustDocEditCartResponse();
		baseResponse.setSuccess(true);
		return baseResponse;
	}

	private CustDocEditCartResponse getInitializeResponseFailure() {
		CustDocEditCartResponse baseResponse = new CustDocEditCartResponse();
		baseResponse.setMessage(GENERIC_ERROR_MSG);
		return baseResponse;
	}

	private C1UXCustDocInitializeRequest getInitializeRequest() {
		C1UXCustDocInitializeRequest initializeRequest = new C1UXCustDocInitializeRequest();
		return initializeRequest;
	}

	@Test
	void that_editCustDoc_returnsExpected() throws Exception, AtWinXSException {
	    String requestString = getJsonRequest(getEditCustDocRequest());
		controller = Mockito.spy(controller);
	    check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_EDIT_CART, requestString, null, TEST_ENCRYPTED_SESSIONID);

	    doReturn(getInitializeResponseFailure()).when(controller).callCustomDocumentInitializeAPI(any(), any());
//	    when(mockCDService.getTranslation(any(), any(), anyString(), anyString())).thenReturn(GENERIC_ERROR_MSG);
	    doNothing().when(controller).defaultGenericFailedMessage(any(), any());
	    doReturn(mockSessionContainer).when(controller).getSessionContainer(any());
	    doReturn(null).when(controller).doCartUpdate(any(), any());
	    ResponseEntity<CustDocEditCartResponse> response = controller.editCustDocInCart(null, getEditCustDocRequest());
	    assertFalse(response.getBody().isSuccess());

	    doReturn(getUpdateCartFailure()).when(controller).doCartUpdate(any(), any());
	    response = controller.editCustDocInCart(null, getEditCustDocRequest());
	    assertFalse(response.getBody().isSuccess());

	    doReturn(getUpdateCartSuccess()).when(controller).doCartUpdate(any(), any());
	    response = controller.editCustDocInCart(null, getEditCustDocRequest());
	    assertFalse(response.getBody().isSuccess());

	    doReturn(getInitializeResponseSuccess()).when(controller).callCustomDocumentInitializeAPI(any(), any());
	    response = controller.editCustDocInCart(null, getEditCustDocRequest());
	    assertTrue(response.getBody().isSuccess());

	    doThrow(new AtWinXSException("Error", "class")).when(controller).doCartUpdate(any(), any());
	    response = controller.editCustDocInCart(null, getEditCustDocRequest());
	    assertFalse(response.getBody().isSuccess());

//	    check422Response(RouteConstants.CUST_DOCS_INITIALIZE_API_EDIT_CART, requestString, null, TEST_ENCRYPTED_SESSIONID);

//	    doReturn(getInitializeResponseSuccess()).when(controller).callCustomDocumentInitializeAPI(any(), any());
//	    checkStandard200Response(RouteConstants.CUST_DOCS_INITIALIZE_API_EDIT_CART, requestString, AtWinXSConstant.EMPTY_STRING, TEST_ENCRYPTED_SESSIONID);

//	    doReturn(getInitializeResponseFailure()).when(controller).callCustomDocumentInitializeAPI(any(), any());
//	    doReturn(getUpdateCartFailure()).when(controller).doCartUpdate(any(), any());
//	    when(mockCDService.getTranslation(any(), any(), anyString(), anyString())).thenReturn(GENERIC_ERROR_MSG);
//	    doNothing().when(controller).defaultGenericFailedMessage(any(), any());

	}

	private COShoppingCartResponse getUpdateCartFailure() {
		COShoppingCartResponse response = new COShoppingCartResponse();
		response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
		response.setStatusMessage(RouteConstants.UPDATE_CART_VALIDATION_ERROR);
		return response;
	}

	private COShoppingCartResponse getUpdateCartSuccess() {
		COShoppingCartResponse response = new COShoppingCartResponse();
		response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
		response.setStatusMessage(RouteConstants.UPDATE_CART_VALIDATION_NOERROR);
		return response;
	}

	private CustDocEditCartRequest getEditCustDocRequest() {
		CustDocEditCartRequest req = new CustDocEditCartRequest();
		return req;
	}

	@Test
	void callCustDocAPI() throws AtWinXSException {
		controller = Mockito.spy(controller);
		CustDocEditCartRequest itemInputs = new CustDocEditCartRequest();
		doReturn(mockCDController).when(controller).getCDController();
		when(mockCDController.initializeUI(any(), any())).thenReturn(mockBaseResponseEntity);
		when(mockBaseResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
	    doReturn(GENERIC_ERROR_MSG).when(controller).getMessage(any(), anyString(), anyString());
		CustDocEditCartResponse response = controller.callCustomDocumentInitializeAPI(mockSessionContainer, itemInputs);
		assertNotNull(response);
		assertTrue(response.isSuccess());

		when(mockBaseResponseEntity.getStatusCode()).thenReturn(HttpStatus.UNPROCESSABLE_ENTITY);
		C1UXCustDocBaseResponse base = new C1UXCustDocBaseResponse();
		base.setMessage(TEST_ENCRYPTED_SESSIONID);
		when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockBaseResponseEntity.getBody()).thenReturn(base);

		response = controller.callCustomDocumentInitializeAPI(mockSessionContainer, itemInputs);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertEquals(TEST_ENCRYPTED_SESSIONID, response.getMessage());

		when(mockCDController.initializeUI(any(), any())).thenThrow(new AtWinXSException(GENERIC_ERROR_MSG, "MyClass"));

		response = controller.callCustomDocumentInitializeAPI(mockSessionContainer, itemInputs);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		assertEquals(GENERIC_ERROR_MSG, response.getMessage());
	}
}
