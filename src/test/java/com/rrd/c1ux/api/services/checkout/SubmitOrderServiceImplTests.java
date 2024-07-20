/*
 *	Copyright (c) RR Donnelley. All Rights Reserved.
 *	This software is the confidential and proprietary information of RR Donnelley.
 *	You shall not disclose such confidential information.
 *
 *	Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/08/23	S Ramachandran	CAP-41235					Review order - Submit order - Junit
 *	06/22/23	L De Leon		CAP-41373					Modified test to reflect changes to clearSessionAfterSubmit() method
 *	09/12/23 	Satishkumar A	CAP-42763					C1UX BE - Order Routing Justification Text Submit Order
 *	09/19/23	Satishkumar A	CAP-43685					C1UX - BE - Add translation messages for order submit and routed orders
 *	09/25/23	Satishkumar A	CAP-44097					C1UX - BE - Fixing Routing Justification - Submit Order - when order justification is not displayed
 *	11/10/23	L De Leon		CAP-44841					Modified test to reflect changes to loadSubmitResponse() method
 */

package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.checkout.SubmitOrderResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;

class SubmitOrderServiceImplTests extends BaseOEServiceTest {

	public static final String REMOTE_IP_ADDR = "127.0.0.1";

	public static final String GENERIC_422MESSAGE = "Order validation failed";

	public static final String SUCCESS = "Success";
	public static final String FAIL = "Failed";

	private static final String TEST_SALE_REFERENCE_NUMBER = "80031955";
	private static final int TEST_ORDER_ID = 604534;

	private SubmitOrderResponse submitOrderResponse;
	public static Locale DEFAULT_US_LOCALE = new java.util.Locale("en_US");

	public static final String MESSAGE_GREATERTHAN_1000 = "Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000. Message length grater than length 1000.";
	public static final String MESSAGE_LESSTHAN_1000 = "Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000. Message length less than length 1000.";
	private static final String JUSTIFY_TEXT = "Justification test";

	@InjectMocks
	private SubmitOrderServiceImpl service;

	@Mock
	private OrderHeaderVO mockOrderHeaderVO;

	@Mock
	private OEOrderSessionBean mockOEOrderSessionBean;

	@Mock
	private VolatileSessionBean mockVolatileSessionBean;

	@Mock
	private EntityObjectMap mockEntityObjectMap;

	@Mock
	private OrderEntrySession mockOrderEntrySession;

	@Mock
	private ApplicationVolatileSession mockApplicationVolatileSession;

	@Mock
	private Map<String, String> mockTranslationMap;

	@Mock
	private Properties mockProperties;

	@Mock
	private OECheckoutAssembler mockCheckoutAssembler;

	@Mock
	private Locale mockLocale;

	@Mock
	private SubmitOrderResponse mockSubmitOrderResponse;

	@Mock
	private OEResolvedUserSettingsSessionBean mockUserSettings;

	@BeforeEach
	void setup() {

		submitOrderResponse = new SubmitOrderResponse();
	}

	@Test
	void that_submitOrder_success() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		setUpModuleSession();

		doNothing().when(service).stopPunchoutSubmit(any(AppSessionBean.class));

		doReturn(true).when(service).validateOrder(any(SubmitOrderResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockCheckoutAssembler).when(service).getCheckoutAssembler(any(AppSessionBean.class),
				any(VolatileSessionBean.class));

		when(mockCheckoutAssembler.submitOrder(any(AppSessionBean.class), any(OEOrderSessionBean.class),
				eq(REMOTE_IP_ADDR))).thenReturn(TEST_SALE_REFERENCE_NUMBER);

		doReturn(TEST_ORDER_ID).when(service).loadSubmitResponse(any(SubmitOrderResponse.class),
				any(AppSessionBean.class), anyInt(), any(OECheckoutAssembler.class));
		doNothing().when(service).setUpQuickCopy(any(SubmitOrderResponse.class), any(OEOrderSessionBean.class),
				any(VolatileSessionBean.class), anyInt(), anyInt());
		doNothing().when(service).clearSessionAfterSubmit(any(OrderEntrySession.class), any(ApplicationVolatileSession.class),
				any(ApplicationSession.class));
		//CAP-43685
		doReturn(true).when(service).validateJustificationText(any(), any(), any(), anyString(), any());

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);//CAP-44145
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);//CAP-44145
		//CAP-44097
		doReturn(true).when(service).isOrderRoutedOrNot(any(), any(), any());
		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);

		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
		//CAP-43685
		doReturn(false).when(service).validateJustificationText(any(), any(), any(), anyString(), any());

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);
		assertNotNull(submitOrderResponse);
		assertFalse(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
	}

	@Test
	void that_submitOrder_return403Message_punchoutfail() throws Exception {

		setUpModuleSession();

		when(mockAppSessionBean.isPunchout()).thenReturn(true);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}

	@Test
	void that_submitOrder_justify_success() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		setUpModuleSession();

		doNothing().when(service).stopPunchoutSubmit(any(AppSessionBean.class));

		doReturn(true).when(service).validateOrder(any(SubmitOrderResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		doReturn(mockCheckoutAssembler).when(service).getCheckoutAssembler(any(AppSessionBean.class),
				any(VolatileSessionBean.class));

		when(mockCheckoutAssembler.submitOrder(any(AppSessionBean.class), any(OEOrderSessionBean.class),
				eq(REMOTE_IP_ADDR))).thenReturn(TEST_SALE_REFERENCE_NUMBER);

		doReturn(TEST_ORDER_ID).when(service).loadSubmitResponse(any(SubmitOrderResponse.class),
				any(AppSessionBean.class), anyInt(), any(OECheckoutAssembler.class));
		doNothing().when(service).setUpQuickCopy(any(SubmitOrderResponse.class), any(OEOrderSessionBean.class),
				any(VolatileSessionBean.class), anyInt(), anyInt());
		doNothing().when(service).clearSessionAfterSubmit(any(OrderEntrySession.class), any(ApplicationVolatileSession.class),
				any(ApplicationSession.class));

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);//CAP-44145
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);//CAP-44145

		//CAP-44097
		doReturn(false).when(service).isOrderRoutedOrNot(any(), any(), any());

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);

		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);
		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));


	}

	@Test
	void that_submitOrder_justify_salesref() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		setUpModuleSession();

		doNothing().when(service).stopPunchoutSubmit(any(AppSessionBean.class));

		doReturn(true).when(service).validateOrder(any(), any(),any());

		doReturn(mockCheckoutAssembler).when(service).getCheckoutAssembler(any(),any());

		when(mockCheckoutAssembler.submitOrder(any(AppSessionBean.class), any(OEOrderSessionBean.class),
				eq(REMOTE_IP_ADDR))).thenReturn(null);
		doNothing().when(service).setUpQuickCopy(any(SubmitOrderResponse.class), any(OEOrderSessionBean.class),
				any(VolatileSessionBean.class), anyInt(), anyInt());
		doNothing().when(service).clearSessionAfterSubmit(any(OrderEntrySession.class), any(ApplicationVolatileSession.class),
				any(ApplicationSession.class));

		when(mockCheckoutAssembler.getOrderHeader(anyInt())).thenReturn(mockOrderHeaderVO);
		when(mockOrderHeaderVO.getOrderXSStatusCode()).thenReturn("A");
		when(mockOrderHeaderVO.getSalesReferenceNum()).thenReturn(TEST_SALE_REFERENCE_NUMBER);
		submitOrderResponse.setSalesReferenceNumber(TEST_SALE_REFERENCE_NUMBER);

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);//CAP-44145
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);//CAP-44145
		//CAP-44097
		doReturn(false).when(service).isOrderRoutedOrNot(any(), any(), any());
		//CAP-43685
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null)).thenReturn("");
		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);
		}
		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null)).thenReturn("");
		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);
		}
		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

		when(mockOrderHeaderVO.getWCSSOrderNumber()).thenReturn(TEST_ORDER_ID+"");
		submitOrderResponse.setWcssOrderNumber(TEST_ORDER_ID+"");
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null)).thenReturn("");

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);
		}
		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null)).thenReturn("");

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);
		}
		assertNotNull(submitOrderResponse);
		assertTrue(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));


		when(mockOrderHeaderVO.getSalesReferenceNum()).thenReturn(null);
		//CAP-43685
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null)).thenReturn("");

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);
		}
		assertNotNull(submitOrderResponse);
		assertFalse(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null)).thenReturn("");

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);
		}
		assertNotNull(submitOrderResponse);
		assertFalse(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

	}

	@Test
	void that_submitOrder_justify_exception() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		setUpModuleSession();

		doNothing().when(service).stopPunchoutSubmit(any(AppSessionBean.class));

		doReturn(true).when(service).validateOrder(any(), any(),any());

		doReturn(mockCheckoutAssembler).when(service).getCheckoutAssembler(any(),any());

		when(mockCheckoutAssembler.submitOrder(any(AppSessionBean.class), any(OEOrderSessionBean.class),
				eq(REMOTE_IP_ADDR))).thenThrow(new AtWinXSException("testing", "MyJunitsClass"));

		doReturn(true).when(service).validateJustificationText(any(), any(), any(), anyString(), any());

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);//CAP-44145
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);//CAP-44145
		//CAP-44097
		doReturn(true).when(service).isOrderRoutedOrNot(any(), any(), any());
			submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);

			assertNotNull(submitOrderResponse);
			assertFalse(submitOrderResponse.isSuccess());
			assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
			//CAP-43685
			submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);

			assertNotNull(submitOrderResponse);
			assertFalse(submitOrderResponse.isSuccess());
			assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));

	}

	@Test
	void that_submitOrder_justify_validateOrder_success() throws AtWinXSException, Exception {

		service = Mockito.spy(service);


		setUpModuleSession();

		doNothing().when(service).stopPunchoutSubmit(any(AppSessionBean.class));

		doReturn(false).when(service).validateOrder(any(SubmitOrderResponse.class), eq(mockSessionContainer),
				eq(mockAppSessionBean));

		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR,JUSTIFY_TEXT);

		assertNotNull(submitOrderResponse);
		assertFalse(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
		//CAP-43685
		submitOrderResponse = service.submitOrder(mockSessionContainer, REMOTE_IP_ADDR);

		assertNotNull(submitOrderResponse);
		assertFalse(submitOrderResponse.isSuccess());
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
	}

	@Test
	void that_submitOrder_validateJustificationText_success() throws AtWinXSException, Exception {

		service = Mockito.spy(service);
		Boolean validText ;

		when(mockUserSettings.isRequireJustificationTxtInd()).thenReturn(false);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

				mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), null, null)).thenReturn("");
				validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, EXPECTED_403MESSAGE, mockSubmitOrderResponse);

				assertNotNull(validText);
				assertTrue(validText);
		}
		when(mockUserSettings.isRequireJustificationTxtInd()).thenReturn(true);
		when(mockAppSessionBean.isDemoUser()).thenReturn(true);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null, null)).thenReturn("");

			validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, EXPECTED_403MESSAGE, mockSubmitOrderResponse);

			assertNotNull(validText);
			assertTrue(validText);
		}
		when(mockUserSettings.isRequireJustificationTxtInd()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(false);
		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null, null)).thenReturn("");

			validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, EXPECTED_403MESSAGE, mockSubmitOrderResponse);

			assertNotNull(validText);
			assertTrue(validText);
		}
		when(mockUserSettings.isRequireJustificationTxtInd()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(false);
		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null, null)).thenReturn("");

			validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, null, mockSubmitOrderResponse);

			assertNotNull(validText);
			assertTrue(validText);
		}
		when(mockUserSettings.isRequireJustificationTxtInd()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);
		when(mockAppSessionBean.isDemoUser()).thenReturn(false);

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), null, null))
					.thenReturn("");
		validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, null, mockSubmitOrderResponse);

		assertNotNull(validText);
		assertFalse(validText);
		}
		when(mockUserSettings.isRequireJustificationTxtInd()).thenReturn(true);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
				.mockStatic(TranslationTextTag.class)) {

	mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), MESSAGE_GREATERTHAN_1000, null))
			.thenReturn("");
		validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, MESSAGE_GREATERTHAN_1000, mockSubmitOrderResponse);
		}
		assertNotNull(validText);
		assertFalse(validText);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
				mockAppSessionBean.getCustomToken(), null, null)).thenReturn("");

			validText = service.validateJustificationText(mockUserSettings, mockAppSessionBean, mockOEOrderSessionBean, MESSAGE_LESSTHAN_1000, mockSubmitOrderResponse);
			assertNotNull(validText);
			assertTrue(validText);
		}
	}

	// CAP-44841
	@Test
	void that_setOrderLabelAndHeaderDescription_for_demo_user_success() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		doReturn(SFTranslationTextConstants.SUBMIT_ORDER_DEF_TXT).when(service).getTranslation(mockAppSessionBean,
				SFTranslationTextConstants.SUBMIT_ORDER_LABEL, SFTranslationTextConstants.SUBMIT_ORDER_DEF_TXT);
		doReturn(SFTranslationTextConstants.DEMO_SUBMIT_ORDER_DEF_TXT).when(service).getTranslation(mockAppSessionBean,
				SFTranslationTextConstants.DEMO_SUBMIT_ORDER_LABEL, SFTranslationTextConstants.DEMO_SUBMIT_ORDER_DEF_TXT);
		doReturn(SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DEF_DESC).when(service).getTranslation(
				mockAppSessionBean, SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DESC,
				SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DEF_DESC);
		when(mockOrderHeaderVO.getOrderXSStatusCode()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockAppSessionBean.isDemoUser()).thenReturn(false, true);

		service.setOrderLabelAndHeaderDescription(submitOrderResponse, mockAppSessionBean, mockOrderHeaderVO);

		assertNotNull(submitOrderResponse);
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
		assertEquals(SFTranslationTextConstants.SUBMIT_ORDER_DEF_TXT, submitOrderResponse.getOrderLabel());
		assertEquals(SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DEF_DESC, submitOrderResponse.getOrderHeaderDescription());

		service.setOrderLabelAndHeaderDescription(submitOrderResponse, mockAppSessionBean, mockOrderHeaderVO);

		assertNotNull(submitOrderResponse);
		assertTrue(Util.isBlankOrNull(submitOrderResponse.getMessage()));
		assertEquals(SFTranslationTextConstants.DEMO_SUBMIT_ORDER_DEF_TXT, submitOrderResponse.getOrderLabel());
		assertEquals(SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DEF_DESC, submitOrderResponse.getOrderHeaderDescription());
	}
}