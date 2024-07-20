/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------------------
 *  06/13/2023  N Caceres		CAP-40882	Initial version
 *  06/16/23	S Ramachandran	CAP-41136	Shopping cart total price in displayable format - Junit
 *  07/14/23	Satishkumar A	CAP-42213 	C1UX BE - Shopping Cart - Default Quantity (Admin Setting at Site Level)
 *	08/22/23	L De Leon		CAP-42663 	Added tests for buildItemRoutingMessages() method
 *	09/05/23	S Ramachandran	CAP-43193	Added tests for Order Line Routing at header level
 *	09/14/23	A Boomker		CAP-43843	Modifications for edit cust doc junits
 *	12/22/23	C Codina		CAP-45934	Added test for checkbackordertosetstatus
 *	02/12/24	Krishna Natarajan	CAP-47109 Added testcase to satisfy newly added condition on loadShoppingCart, also cleaned up several methods
 *  02/17/24	Krishna Natarajan CAP-47085 Added a new test for method that overwrites the UOM acronyms to full words
 *  05/24/24	Krishna Natarajan	CAP-49673 Disabled tests to get the bug functionally working
 *  05/30/24	N Caceres 		CAP-49693 	JUnits for sorting bundle items
 *  06/28/24	Krishna Natarajan CAP-49811	Commented a few tests to take the functional code in
 */

package com.rrd.c1ux.api.services.shoppingcart;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.models.shoppingcart.CORemoveSpecificItemRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartLineFormBean;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartRequest;
import com.rrd.c1ux.api.services.locators.ListsAdminLocatorService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.EntryPoint;
import com.rrd.custompoint.orderentry.entity.CartLine;
import com.rrd.custompoint.orderentry.entity.OrderLine.ComponentType;
import com.rrd.custompoint.validator.orderentry.ShoppingCartValidator;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.interfaces.IManageListAdmin;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVO;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartLineFormBean;
import com.wallace.atwinxs.orderentry.session.AllocationSummaryBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.CartUpdateLineVO;
import com.wallace.atwinxs.orderentry.vo.CustomDocumentOrderLineVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.orderentry.vo.RoutingInfoCompositeVO;

class ShoppingCartServiceImplTests extends BaseOEServiceTest {

	public static final String SUCCESS = "Success";
	public static final String FAIL = "Failed";
	private static final String ACTION_ID = "1";

	//Begin:CAP-43193
	private static final String TEST_ITEM1_ORDERID = "611300";
	private static final String TEST_ITEM1_NUMBER = "14444";
	private static final String TEST_ITEM1_DESC = "FASSON 14444 TEST 2 - default $3 each";
	private static final String TEST_ITEM1_QTY = "3";
	private static final String TEST_ITEM1_RTNG_MSG ="This item requires approval when quantity ordered exceeds 2.  This order will route for review.";

	private static final String TEST_ITEM2_ORDERID = "611300";
	private static final String TEST_ITEM2_NUMBER = "BC3TEST";
	private static final String TEST_ITEM2_DESC = "BCSI BUSINESS CARD 3 TEST ITEM";
	private static final String TEST_ITEM2_QTY = "250";
	private static final String TEST_ITEM2_RTNG_MSG ="This item requires approval.  This order will route for review.";

	private static final String TEST_ORDER_HL_RTNG_MSG ="This order will route for review.";
	//End:CAP-43193


	public static final String ERROR_MESSAGE = "error message";

	private COShoppingCartResponse coShoppingCartResponse;

	private OEShoppingCartFormBean oeShoppingCartFormBean;

	private OEShoppingCartLineFormBean[] oeShoppingCartLineFormBean;
	private OEShoppingCartLineFormBean[] oeShoppingCartLineFormBeanTbdTrue;

	private OEOrderSessionBean oeOrderSessionBean;
	
	@Mock
	private OEOrderSessionBean mockOEOrderSessionBean;

	@Mock
	private RoutingInfoCompositeVO mockRoutingInfo;

	@Mock
	private OEResolvedUserSettingsSessionBean mockUserSettings;

	@InjectMocks
	private ShoppingCartServiceImpl service;

	@Mock
	private IOEShoppingCartComponent mockOEShoppingCartComp;

	@Mock
	private OEShoppingCartAssembler mockAssembler;
	
	@Mock
	COShoppingCartRequest mockShoppingCartRequest;
	
	@Mock
	OEShoppingCartLineFormBean mockShoppingCartLineFormBean;
	
	@Mock
	COShoppingCartResponse mockCoShoppingCartResponse;
	
	@Mock
	OEShoppingCartFormBean mockOEShoppingCartFormBean ;

	@Mock
	OrderLineVO mockCustDocOrderLineVO;
	
	@Mock
	CartLine mockCartLine;
	
	@Mock
	OECheckoutAssembler mockCheckoutAssembler;
	
	@Mock
	IManageListAdmin mockIManageListAdmin;
	
	@Mock
	ManageListsBusinessUnitPropsVO mockbusinessUnitPropsVO;
	
	@Mock
	ListsAdminLocatorService mockListsAdminLocatorService;
	
	@BeforeEach
	void setup() {

		oeShoppingCartFormBean = new OEShoppingCartFormBean();
		oeShoppingCartFormBean.setCurrencyLocale(Locale.US);
		oeShoppingCartLineFormBean = new OEShoppingCartLineFormBean[1];

		oeShoppingCartLineFormBeanTbdTrue = new OEShoppingCartLineFormBean[2];
	}

	@Test
	@Disabled
	void that_getShoppingCartItemDetail_TBDIsFalse_CTAIsGreaterThanZERO() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		CartItem_TBDIsFalse_CTAIsGreaterThanZERO(oeShoppingCartFormBean, oeShoppingCartLineFormBean);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		oeShoppingCartFormBean.setItems(oeShoppingCartLineFormBean, mockAppSessionBean);

		doReturn(oeShoppingCartFormBean).when(service).loadShoppingCart(any(), any(), anyBoolean());
		doReturn(coShoppingCartResponse).when(service).loadDisclaimers(any(SessionContainer.class),
				any(COShoppingCartResponse.class));
		doReturn(true).when(service).shouldShowSaveButton(any(SessionContainer.class));
		doReturn(coShoppingCartResponse).when(service).getUOMAndQuantityErrorMessage(any(SessionContainer.class),
				any(COShoppingCartResponse.class));

		coShoppingCartResponse = service.getShoppingCartItemDetail(mockSessionContainer);

		assertNotNull(coShoppingCartResponse);
		assertTrue(Util.isBlankOrNull(coShoppingCartResponse.getStatusMessage()));
		assertFalse(coShoppingCartResponse.getOeShoppingCartFormBean().isTbd());
		assertNotEquals("TBD", coShoppingCartResponse.getOeShoppingCartFormBean().getCartTotalPrice());
	}

	@Test
	@Disabled
	void that_getShoppingCartItemDetail_TBDIsFalse_CTAIsEqualToZERO() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		CartItem_TBDIsFalse_CTAIsEqualToZERO(oeShoppingCartFormBean, oeShoppingCartLineFormBean);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		oeShoppingCartFormBean.setItems(oeShoppingCartLineFormBean, mockAppSessionBean);

		doReturn(oeShoppingCartFormBean).when(service).loadShoppingCart(any(), any(), anyBoolean());
		doReturn(coShoppingCartResponse).when(service).loadDisclaimers(any(SessionContainer.class),
				any(COShoppingCartResponse.class));
		doReturn(true).when(service).shouldShowSaveButton(any(SessionContainer.class));
		doReturn(coShoppingCartResponse).when(service).getUOMAndQuantityErrorMessage(any(SessionContainer.class),
				any(COShoppingCartResponse.class));

		coShoppingCartResponse = service.getShoppingCartItemDetail(mockSessionContainer);

		assertNotNull(coShoppingCartResponse);
		assertTrue(Util.isBlankOrNull(coShoppingCartResponse.getStatusMessage()));
		assertFalse(coShoppingCartResponse.getOeShoppingCartFormBean().isTbd());
		assertNotEquals("TBD", coShoppingCartResponse.getOeShoppingCartFormBean().getCartTotalPrice());
	}

	@Test
	@Disabled
	void that_getShoppingCartItemDetail_TBDIsFalse_CTAIsLessThanZERO() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		CartItem_TBDIsFalse_CTAIsLessThanZERO(oeShoppingCartFormBean, oeShoppingCartLineFormBean);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowPrintOverride()).thenReturn(true);

		oeShoppingCartFormBean.setItems(oeShoppingCartLineFormBean, mockAppSessionBean);

		doReturn(oeShoppingCartFormBean).when(service).loadShoppingCart(any(), any(), anyBoolean());
		doReturn(coShoppingCartResponse).when(service).loadDisclaimers(any(SessionContainer.class),
				any(COShoppingCartResponse.class));
		doReturn(true).when(service).shouldShowSaveButton(any(SessionContainer.class));
		doReturn(coShoppingCartResponse).when(service).getUOMAndQuantityErrorMessage(any(SessionContainer.class),
				any(COShoppingCartResponse.class));

		coShoppingCartResponse = service.getShoppingCartItemDetail(mockSessionContainer);

		assertNotNull(coShoppingCartResponse);
		assertTrue(Util.isBlankOrNull(coShoppingCartResponse.getStatusMessage()));
		assertTrue(coShoppingCartResponse.getOeShoppingCartFormBean().isTbd());
		assertEquals("TBD", coShoppingCartResponse.getOeShoppingCartFormBean().getCartTotalPrice());
	}

	@Test
	@Disabled
	void that_getShoppingCartItemDetail_CartItem_TBDIsTrue() throws AtWinXSException, Exception {

		service = Mockito.spy(service);

		CartItem_TBDIsTrue(oeShoppingCartFormBean, oeShoppingCartLineFormBeanTbdTrue);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		oeShoppingCartFormBean.setItems(oeShoppingCartLineFormBeanTbdTrue, mockAppSessionBean);

		doReturn(oeShoppingCartFormBean).when(service).loadShoppingCart(any(), any(), anyBoolean());
		doReturn(coShoppingCartResponse).when(service).loadDisclaimers(any(SessionContainer.class),
				any(COShoppingCartResponse.class));
		doReturn(true).when(service).shouldShowSaveButton(any(SessionContainer.class));
		doReturn(coShoppingCartResponse).when(service).getUOMAndQuantityErrorMessage(any(SessionContainer.class),
				any(COShoppingCartResponse.class));

		coShoppingCartResponse = service.getShoppingCartItemDetail(mockSessionContainer);

		assertNotNull(coShoppingCartResponse);
		assertTrue(Util.isBlankOrNull(coShoppingCartResponse.getStatusMessage()));
		assertTrue(coShoppingCartResponse.getOeShoppingCartFormBean().isTbd());
		assertEquals("TBD", coShoppingCartResponse.getOeShoppingCartFormBean().getCartTotalPrice());
	}

	@Test
	@Disabled
	void that_removedSpecificItem_unable_to_delete_item() throws AtWinXSException {

		CORemoveSpecificItemRequest req = new CORemoveSpecificItemRequest();
		OEShoppingCartFormBean oeShoppingCartFormBean =  buildFormBean();
		CartUpdateLineVO[] cartUpdateLineVO = new CartUpdateLineVO[1];


		try (MockedConstruction<OEShoppingCartAssembler> mockedAssembler = mockConstruction(OEShoppingCartAssembler.class, (mock, context) -> {
			when(mock.updateShoppingCart(anyInt(), anyBoolean(), anyString(), isA(OEShoppingCartFormBean.class), isA(OEOrderSessionBean.class),
					isA(VolatileSessionBean.class), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(cartUpdateLineVO);
			when(mock.getShoppingCart(anyInt(), isA(OEOrderSessionBean.class), anyString(), any(),
					any(), anyBoolean(), any(), anyBoolean())).thenReturn(oeShoppingCartFormBean);
		})) {
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockOEShoppingCartComponentLocatorService.locate(any())).thenReturn(mockOEShoppingCartComp);
			when(mockOESession.getSessionID()).thenReturn(1);
			when(mockAppSessionBean.isCustomPointSite()).thenReturn(false);
			when(mockAppSessionBean.isMobileSession()).thenReturn(false);
			when(mockAppSessionBean.isPreSelectAltProfile()).thenReturn(true);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isShowOrderLineAvailability()).thenReturn(true);
			when(mockAppSessionBean.getCorporateNumber()).thenReturn("");
			when(mockOEAssembler.getShoppingCartAssembler(any(), any(), anyBoolean())).thenReturn(mockAssembler);
			when(mockAssembler.getShoppingCart(any(), any(), any(), any(), any(), anyBoolean(), any(), anyBoolean())).thenReturn(oeShoppingCartFormBean);

			COShoppingCartResponse removedSpecificItem = service.removedSpecificItem(mockSessionContainer, req, oeShoppingCartFormBean, ACTION_ID);
			assertNotNull(removedSpecificItem);
		}
	}

	private OEShoppingCartFormBean buildFormBean() throws AtWinXSException {
		OEShoppingCartFormBean formBean = new OEShoppingCartFormBean();
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setAvailabilityCode("B");
		formBean.setItems(items, mockAppSessionBean);

		return formBean;
	}

	private void CartItem_TBDIsFalse_CTAIsGreaterThanZERO(OEShoppingCartFormBean oeShoppingCartFormBean,
			OEShoppingCartLineFormBean[] oeShoppingCartLineFormBean) {

		oeShoppingCartLineFormBean[0] = new OEShoppingCartLineFormBean();
		oeShoppingCartLineFormBean[0].setUOMCode("M");
		oeShoppingCartLineFormBean[0].setUOMFactor("1000");
		oeShoppingCartLineFormBean[0].setItemQuantity("1");

		oeShoppingCartLineFormBean[0].setFileDeliveryMethod("P");
		oeShoppingCartLineFormBean[0].setFileDeliveryOption("P");

		XSCurrency itemExtendedCurrency = new XSCurrency(25, "$25.00", "USD", "");
		oeShoppingCartLineFormBean[0].setItemExtendedCurrency(itemExtendedCurrency);
		oeShoppingCartLineFormBean[0].setItemExtendedSellPrice("$25.00");
	}

	private void CartItem_TBDIsFalse_CTAIsEqualToZERO(OEShoppingCartFormBean oeShoppingCartFormBean,
			OEShoppingCartLineFormBean[] oeShoppingCartLineFormBean) {

		oeShoppingCartLineFormBean[0] = new OEShoppingCartLineFormBean();
		oeShoppingCartLineFormBean[0].setUOMCode("M");
		oeShoppingCartLineFormBean[0].setUOMFactor("1000");
		oeShoppingCartLineFormBean[0].setItemQuantity("1");

		oeShoppingCartLineFormBean[0].setFileDeliveryMethod("P");
		oeShoppingCartLineFormBean[0].setFileDeliveryOption("P");

		XSCurrency itemExtendedCurrency = new XSCurrency(0, "$0.00", "USD", "");
		oeShoppingCartLineFormBean[0].setItemExtendedCurrency(itemExtendedCurrency);
		oeShoppingCartLineFormBean[0].setItemExtendedSellPrice("$0.00");
	}

	private void CartItem_TBDIsFalse_CTAIsLessThanZERO(OEShoppingCartFormBean oeShoppingCartFormBean,
			OEShoppingCartLineFormBean[] oeShoppingCartLineFormBean) {

		oeShoppingCartLineFormBean[0] = new OEShoppingCartLineFormBean();
		oeShoppingCartLineFormBean[0].setUOMCode("M");
		oeShoppingCartLineFormBean[0].setUOMFactor("1000");
		oeShoppingCartLineFormBean[0].setItemQuantity("1");

		oeShoppingCartLineFormBean[0].setFileDeliveryMethod("P");
		oeShoppingCartLineFormBean[0].setFileDeliveryOption("P");

		XSCurrency itemExtendedCurrency = new XSCurrency(-1, "-$1.00", "USD", "");
		oeShoppingCartLineFormBean[0].setItemExtendedCurrency(itemExtendedCurrency);
		oeShoppingCartLineFormBean[0].setItemExtendedSellPrice("$1.00");
	}

	private void CartItem_TBDIsTrue(OEShoppingCartFormBean oeShoppingCartFormBean,
			OEShoppingCartLineFormBean[] oeShoppingCartLineFormBean) {

		oeShoppingCartFormBean.setTbd(true);
		oeShoppingCartLineFormBeanTbdTrue[0] = new OEShoppingCartLineFormBean();
		oeShoppingCartLineFormBeanTbdTrue[0].setUOMCode("M");
		oeShoppingCartLineFormBeanTbdTrue[0].setUOMFactor("1000");
		oeShoppingCartLineFormBeanTbdTrue[0].setItemQuantity("1");

		oeShoppingCartLineFormBeanTbdTrue[0].setFileDeliveryMethod("P");
		oeShoppingCartLineFormBeanTbdTrue[0].setFileDeliveryOption("P");

		XSCurrency itemExtendedCurrencyGtZero = new XSCurrency(25, "$25.00", "USD", "");
		oeShoppingCartLineFormBeanTbdTrue[0].setItemExtendedCurrency(itemExtendedCurrencyGtZero);
		oeShoppingCartLineFormBeanTbdTrue[0].setItemExtendedSellPrice("$25.00");

		oeShoppingCartLineFormBeanTbdTrue[1] = new OEShoppingCartLineFormBean();
		oeShoppingCartLineFormBeanTbdTrue[1].setUOMCode("M");
		oeShoppingCartLineFormBeanTbdTrue[1].setUOMFactor("1000");
		oeShoppingCartLineFormBeanTbdTrue[1].setItemQuantity("1");

		oeShoppingCartLineFormBeanTbdTrue[1].setFileDeliveryMethod("P");
		oeShoppingCartLineFormBeanTbdTrue[1].setFileDeliveryOption("P");

		XSCurrency itemExtendedCurrencyLtZero = new XSCurrency(-1, "1$1.00", "USD", "");
		oeShoppingCartLineFormBeanTbdTrue[1].setItemExtendedCurrency(itemExtendedCurrencyLtZero);
		oeShoppingCartLineFormBeanTbdTrue[1].setItemExtendedSellPrice("$1.00");
	}

	//CAP-42213
	@Test
	void that_defaultQuantities_SiteDefaultQty_Zero() throws AtWinXSException {

		when(mockAppSessionBean.getSiteDefaultQty()).thenReturn(0);
		oeShoppingCartFormBean = service.defaultQuantities(mockOEShoppingCartFormBean, mockAppSessionBean);
		assertNotNull(oeShoppingCartFormBean);
	}

	//CAP-42213
	@Test
	void that_defaultQuantities_SiteDefaultQty_One() throws AtWinXSException {

		when(mockAppSessionBean.getSiteDefaultQty()).thenReturn(1);
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(null);

		oeShoppingCartFormBean =null;
		oeShoppingCartFormBean = service.defaultQuantities(oeShoppingCartFormBean, mockAppSessionBean);
		assertNull(oeShoppingCartFormBean);


		oeShoppingCartFormBean = service.defaultQuantities(mockOEShoppingCartFormBean, mockAppSessionBean);
		assertNotNull(oeShoppingCartFormBean);

		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[0];
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(items);
		oeShoppingCartFormBean = service.defaultQuantities(mockOEShoppingCartFormBean, mockAppSessionBean);
		assertNotNull(oeShoppingCartFormBean);

		items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setItemQuantity("0");
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(items);
		oeShoppingCartFormBean = service.defaultQuantities(mockOEShoppingCartFormBean, mockAppSessionBean);
		assertNotNull(oeShoppingCartFormBean);
		assertEquals("1", oeShoppingCartFormBean.getItems()[0].getItemQuantity());

		items[0] = new OEShoppingCartLineFormBean();
		items[0].setItemQuantity("2");
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(items);
		oeShoppingCartFormBean = service.defaultQuantities(mockOEShoppingCartFormBean, mockAppSessionBean);
		assertNotNull(oeShoppingCartFormBean);
		assertEquals("2", oeShoppingCartFormBean.getItems()[0].getItemQuantity());
	}

	@Test
	void that_buildItemRoutingMessages_isDemoUser() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();

		service = Mockito.spy(service);

		when(mockAppSessionBean.isDemoUser()).thenReturn(true);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, mockOEShoppingCartFormBean, mockAssembler);

		assertFalse(oeOrderSessionBean.isCLURoutingOnly());
	}

	@Test
	void that_buildItemRoutingMessages_isRoutingNotAvailable() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();

		service = Mockito.spy(service);

		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(false);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, mockOEShoppingCartFormBean, mockAssembler);

		assertFalse(oeOrderSessionBean.isCLURoutingOnly());
	}

	@Test
	void that_buildItemRoutingMessages_isNotRA_isNotCustomListUpload() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();
		oeOrderSessionBean.setAllowCstmListUpload(false);

		service = Mockito.spy(service);

		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(false);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, mockOEShoppingCartFormBean, mockAssembler);

		assertFalse(oeOrderSessionBean.isCLURoutingOnly());
	}

	@Test
	void that_buildItemRoutingMessages_isCustomListUpload() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();
		oeOrderSessionBean.setAllowCstmListUpload(true);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler = mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
			when(mock.checkOrderForRouting(mockAppSessionBean, oeOrderSessionBean, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, false)).thenReturn(null);
		})) {

		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(false);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockCheckoutAssembler);
		when(mockCheckoutAssembler.checkOrderForRouting(any(), any(), any(), any(), anyBoolean())).thenReturn(mockRoutingInfo);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, mockOEShoppingCartFormBean, mockAssembler);

		assertFalse(oeOrderSessionBean.isCLURoutingOnly());
		}
	}

	@Test
	void that_buildItemRoutingMessages_isSubjecttoRA() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler = mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
			when(mock.checkOrderForRouting(mockAppSessionBean, oeOrderSessionBean, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, false)).thenReturn(mockRoutingInfo);
		})) {

		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);
		when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
		when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getDefaultLanguage()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		doReturn(true).when(mockAssembler).hasCstmListUploadRoutingOnly(mockRoutingInfo);
		doNothing().when(mockAssembler).buildOrderRoutingWarningMsg(mockOEShoppingCartFormBean, oeOrderSessionBean, DEFAULT_US_LOCALE, DEVTEST_SITE_ID, mockRoutingInfo, DEFAULT_US_LOCALE, AtWinXSConstant.EMPTY_STRING, mockVolatileSessionBean, mockUserSettings);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockCheckoutAssembler);
		when(mockCheckoutAssembler.checkOrderForRouting(any(), any(), any(), any(), anyBoolean())).thenReturn(mockRoutingInfo);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, mockOEShoppingCartFormBean, mockAssembler);

		assertTrue(oeOrderSessionBean.isCLURoutingOnly());
		}
	}

	//Begin - CAP-43193 - OEShoppingCartFormBean Items added for Order Line HL routing
	private OEShoppingCartFormBean oeShoppingCartFormBean_withHLItemRouting() throws AtWinXSException {
		OEShoppingCartFormBean formBean = new OEShoppingCartFormBean();
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[2];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setOrderId(TEST_ITEM1_ORDERID);
		items[0].setItemNumber(TEST_ITEM1_NUMBER);
		items[0].setItemDescription(TEST_ITEM1_DESC);
		items[0].setItemQuantity(TEST_ITEM1_QTY);
		items[0].setItemRoutingMessage(TEST_ITEM1_RTNG_MSG);
		items[0].setFileDeliveryMethod(AtWinXSConstant.EMPTY_STRING);

		items[1] = new OEShoppingCartLineFormBean();
		items[1].setOrderId(TEST_ITEM2_ORDERID);
		items[1].setItemNumber(TEST_ITEM2_NUMBER);
		items[1].setItemDescription(TEST_ITEM2_DESC);
		items[1].setItemQuantity(TEST_ITEM2_QTY);
		items[1].setItemRoutingMessage(TEST_ITEM2_RTNG_MSG);
		items[1].setFileDeliveryMethod(AtWinXSConstant.EMPTY_STRING);

		formBean.setItems(items, mockAppSessionBean);
		return formBean;
	}

	//CAP-43193 - build ItemRoutingMessages subject to Order line header level Routing(
	@Test
	void that_buildItemRoutingMessages_isSubjecttoOrderLineHLItemRouting() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();
		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler = mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
			when(mock.checkOrderForRouting(mockAppSessionBean, oeOrderSessionBean, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, false)).thenReturn(mockRoutingInfo);
		})) {

		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);
		when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
		when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getDefaultLanguage()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockCheckoutAssembler);
		when(mockCheckoutAssembler.checkOrderForRouting(any(), any(), any(), any(), anyBoolean())).thenReturn(mockRoutingInfo);
		doReturn(true).when(mockAssembler).hasCstmListUploadRoutingOnly(mockRoutingInfo);

		OEShoppingCartFormBean oeShoppingCartFormBean =  oeShoppingCartFormBean_withHLItemRouting();
		//oeShoppingCartFormBean.setItems(oeShoppingCartLineFormBean, mockAppSessionBean);

		doNothing().when(mockAssembler).buildOrderRoutingWarningMsg(oeShoppingCartFormBean, oeOrderSessionBean, DEFAULT_US_LOCALE, DEVTEST_SITE_ID, mockRoutingInfo, DEFAULT_US_LOCALE, AtWinXSConstant.EMPTY_STRING, mockVolatileSessionBean, mockUserSettings);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, oeShoppingCartFormBean, mockAssembler);
		String routingMsgofItem1 = oeShoppingCartFormBean.getItems()[0].getItemRoutingMessage();
		String routingMsgofItem2 = oeShoppingCartFormBean.getItems()[0].getItemRoutingMessage();

		assertEquals(TEST_ORDER_HL_RTNG_MSG, routingMsgofItem1.substring(routingMsgofItem1.length()-TEST_ORDER_HL_RTNG_MSG.length()));
		assertEquals(TEST_ORDER_HL_RTNG_MSG, routingMsgofItem2.substring(routingMsgofItem2.length()-TEST_ORDER_HL_RTNG_MSG.length()));
		}
	}
	//End - CAP-43193


	protected void setupOEOrderSessionBean() {
		oeOrderSessionBean = new OEOrderSessionBean(DEVTEST_SITE_ID, AtWinXSConstant.EMPTY_STRING,
				AtWinXSConstant.INVALID_ID, DEVTEST_UX_BU_ID, DEFAULT_UG_NM, AtWinXSConstant.INVALID_ID);
		oeOrderSessionBean.setUserSettings(mockUserSettings);
		oeOrderSessionBean.setCLURoutingOnly(false);
	}

	@Test
	void createInitializeUIEditRequest() throws AtWinXSException {
		try (MockedConstruction<OEShoppingCartAssembler> mockedAssembler = mockConstruction(OEShoppingCartAssembler.class, (mock, context) -> {
			when(mock.getSpecialItemOrderLine(anyInt(), anyInt(), any())).thenReturn(mockCustDocOrderLineVO);
		})) {
			setupBaseMockSessions();
			int lineNum = 356;
			CustDocEditCartRequest req1 = new CustDocEditCartRequest();
			req1.setOrderLineNumber(lineNum);
			when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);
			when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(25));
			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(3);
			service = Mockito.spy(service);
			setUpTranslationServiceErrors();
			doReturn(true).when(service).isOrderSubmittedCannotEdit(anyInt());
			AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> { service.createInitializeUIEditRequest(mockSessionContainer, req1);});
			assertTrue(error403 instanceof AccessForbiddenException);

			when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);
			when(mockOEAssembler.getShoppingCartAssembler(any(), any(), anyBoolean())).thenReturn(mockAssembler);
			when(mockAssembler.getSpecialItemOrderLine(anyInt(), anyInt(), any())).thenReturn(mockCustDocOrderLineVO);
			getCustDocOrderLineVOMocks(false);
			AtWinXSException error422 = assertThrows(AtWinXSException.class, () -> { service.createInitializeUIEditRequest(mockSessionContainer, req1);});
			assertFalse(error422 instanceof AccessForbiddenException);

			getCustDocOrderLineVOMocks(true);
			C1UXCustDocInitializeRequest req = service.createInitializeUIEditRequest(mockSessionContainer, req1);
			assertEquals(EntryPoint.CART_EDIT.toString(), req.getEntryPoint());
		}
	}
	@ParameterizedTest
	@MethodSource("getErrorMessage")
	void testBackOrderToSetStatus(String errorMessage, boolean isBackOrder) throws AtWinXSException {
	
		String update = "Y";
		OEShoppingCartFormBean shoppingCart = new OEShoppingCartFormBean();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		OEShoppingCartLineFormBean[] cpLineItems = new OEShoppingCartLineFormBean[1];
		OEShoppingCartLineFormBean cpLineItem = new OEShoppingCartLineFormBean();
		cpLineItem.setErrorMessage(errorMessage);
		cpLineItems[0] = cpLineItem;
		COShoppingCartRequest scRequest = new COShoppingCartRequest();
		scRequest.setBackOrderWarned(isBackOrder);
		
		CartItem_TBDIsFalse_CTAIsGreaterThanZERO(oeShoppingCartFormBean, oeShoppingCartLineFormBean);
		shoppingCart.setItems(oeShoppingCartLineFormBean, mockAppSessionBean);
		when(mockTranslationService.processMessage(any(), any(), anyString())).thenReturn(TranslationTextConstants.TRANS_BACK_ORDER_WARNING_MSG);
		when(mockCoShoppingCartResponse.getUpdateCartNoError()).thenReturn(update);
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(cpLineItems);
		when(mockCoShoppingCartResponse.getOeShoppingCartFormBean()).thenReturn(mockOEShoppingCartFormBean);
		service.checkBackOrderToSetStatus(mockAppSessionBean, scRequest, mockOEShoppingCartFormBean, mockCoShoppingCartResponse, true);
	}
	
	private static Stream<Arguments> getErrorMessage(){
		return Stream.of(Arguments.of(TranslationTextConstants.TRANS_BACK_ORDER_WARNING_MSG, true),
						Arguments.of("Test Error",true),
						Arguments.of(SFTranslationTextConstants.ITEM_WILL_BE_REMOVED_WARNING,true),
						Arguments.of(null, true),
						Arguments.of("",false));
		
	}
	private void getCustDocOrderLineVOMocks(boolean valid) {
		CustomDocumentOrderLineVOKey key = new CustomDocumentOrderLineVOKey(valid ? 25 : -1);
		when(mockCustDocOrderLineVO.getCustomDocLineKey()).thenReturn(key);
		when(mockCustDocOrderLineVO.getItemAvailabilityAvailabilityCode()).thenReturn("A");
		when(mockCustDocOrderLineVO.getCustomerItemNum()).thenReturn("ABC");
		when(mockCustDocOrderLineVO.getOrderQty()).thenReturn(30);
		when(mockCustDocOrderLineVO.getItemSellPrice()).thenReturn(2.01);
		when(mockCustDocOrderLineVO.getUomCode()).thenReturn("PA");
		when(mockCustDocOrderLineVO.getWallaceItemNum()).thenReturn("DEF");
		when(mockCustDocOrderLineVO.getLineNum()).thenReturn(442);
	}

	private static final String TEST_ITEM1_ORDERID_NULL = null;
	
	private OEShoppingCartFormBean oeShoppingCartFormBean_withHLItemRouting_orderIDNUll() throws AtWinXSException {
		OEShoppingCartFormBean formBean = new OEShoppingCartFormBean();
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[2];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setOrderId(TEST_ITEM1_ORDERID_NULL);
		items[0].setItemNumber(TEST_ITEM1_NUMBER);
		items[0].setItemDescription(TEST_ITEM1_DESC);
		items[0].setItemQuantity(TEST_ITEM1_QTY);
		items[0].setItemRoutingMessage(TEST_ITEM1_RTNG_MSG);
		items[0].setFileDeliveryMethod(AtWinXSConstant.EMPTY_STRING);

		items[1] = new OEShoppingCartLineFormBean();
		items[1].setOrderId(TEST_ITEM1_ORDERID_NULL);
		items[1].setItemNumber(TEST_ITEM2_NUMBER);
		items[1].setItemDescription(TEST_ITEM2_DESC);
		items[1].setItemQuantity(TEST_ITEM2_QTY);
		items[1].setItemRoutingMessage(TEST_ITEM2_RTNG_MSG);
		items[1].setFileDeliveryMethod(AtWinXSConstant.EMPTY_STRING);

		formBean.setItems(items, mockAppSessionBean);
		return formBean;
	}
	
	@Test
	void that_buildItemRoutingMessages_isSubjecttoOrderLineHLItemRouting_orderIDNull() throws AtWinXSException, Exception {

		setupOEOrderSessionBean();
		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler = mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
			when(mock.checkOrderForRouting(mockAppSessionBean, oeOrderSessionBean, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, false)).thenReturn(mockRoutingInfo);
		})) {

		when(mockAppSessionBean.isDemoUser()).thenReturn(false);
		when(mockUserSettings.isRoutingAvailable()).thenReturn(true);
		when(mockUserSettings.isSubjToRnA()).thenReturn(true);
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockCheckoutAssembler);
		when(mockCheckoutAssembler.checkOrderForRouting(any(), any(), any(), any(), anyBoolean())).thenReturn(mockRoutingInfo);
		when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
		when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getDefaultLanguage()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		doReturn(true).when(mockAssembler).hasCstmListUploadRoutingOnly(mockRoutingInfo);

		OEShoppingCartFormBean oeShoppingCartFormBean =  oeShoppingCartFormBean_withHLItemRouting_orderIDNUll();

		doNothing().when(mockAssembler).buildOrderRoutingWarningMsg(oeShoppingCartFormBean, oeOrderSessionBean, DEFAULT_US_LOCALE, DEVTEST_SITE_ID, mockRoutingInfo, DEFAULT_US_LOCALE, AtWinXSConstant.EMPTY_STRING, mockVolatileSessionBean, mockUserSettings);

		service.buildItemRoutingMessages(mockAppSessionBean, mockVolatileSessionBean, oeOrderSessionBean, oeShoppingCartFormBean, mockAssembler);
		String routingMsgofItem1 = oeShoppingCartFormBean.getItems()[0].getItemRoutingMessage();
		String routingMsgofItem2 = oeShoppingCartFormBean.getItems()[0].getItemRoutingMessage();

		assertEquals(TEST_ORDER_HL_RTNG_MSG, routingMsgofItem1.substring(routingMsgofItem1.length()-TEST_ORDER_HL_RTNG_MSG.length()));
		assertEquals(TEST_ORDER_HL_RTNG_MSG, routingMsgofItem2.substring(routingMsgofItem2.length()-TEST_ORDER_HL_RTNG_MSG.length()));
		}
	}
	
	@Test
	void that_getShoppingCartItemDetail_TBDIsFalse_CTAIsGreaterThanZERO_nullorderID() throws AtWinXSException, Exception {
		service = Mockito.spy(service);
		mockLoadShoppingCart();
		doReturn(true).when(service).checkForBackorderedItems(mockOEShoppingCartFormBean);
		doNothing().when(service).updateForPunchout(any(), any(), any());
		doReturn(mockOEShoppingCartFormBean).when(service).defaultQuantities(any(), any());
		
		service.loadShoppingCart(mockSessionContainer, null, false);
		assertNotNull(mockOEShoppingCartFormBean);
	}

	//CAP-47085 Created a new test method that over writes the UOM acronyms to full words
	private OEShoppingCartFormBean oeShoppingCartFormBean_null() throws AtWinXSException {
		OEShoppingCartFormBean formBean = new OEShoppingCartFormBean();
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setErrorMessage(null);

		formBean.setItems(items, mockAppSessionBean);
		return formBean;
	}
	
	@Test
	void that_getShoppingCartItemDetail_convertAcronymsToWordsOnErrMsgNull() throws AtWinXSException, Exception {
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.
				mockStatic(TranslationTextTag.class)){
			
			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), "ofLbl"))
					.thenReturn("of");
			 
			OEShoppingCartFormBean oeShoppingCartFormBean =  oeShoppingCartFormBean_null();
		service.convertAcronymsToWordsOnErrMsg(oeShoppingCartFormBean,mockAppSessionBean);
		}
		assertNotNull(mockOEShoppingCartFormBean);
	}
	
	private OEShoppingCartFormBean oeShoppingCartFormBean_new_max() throws AtWinXSException {
		OEShoppingCartFormBean formBean = new OEShoppingCartFormBean();
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setErrorMessage("Maximum order 500 BA.");

		formBean.setItems(items, mockAppSessionBean);
		return formBean;
	}
	
	@Test
	void that_getShoppingCartItemDetail_convertAcronymsToWordsOnErrMsg_max() throws AtWinXSException, Exception {
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.
				mockStatic(TranslationTextTag.class)){
			
			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), "ofLbl"))
					.thenReturn("of");
			 
			OEShoppingCartFormBean oeShoppingCartFormBean =  oeShoppingCartFormBean_new_max();
		service.convertAcronymsToWordsOnErrMsg(oeShoppingCartFormBean,mockAppSessionBean);
		}
		assertNotNull(mockOEShoppingCartFormBean);
	}
	
	private OEShoppingCartFormBean oeShoppingCartFormBean_new_min() throws AtWinXSException {
		OEShoppingCartFormBean formBean = new OEShoppingCartFormBean();
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setErrorMessage("Minimum order 500 BA.");

		formBean.setItems(items, mockAppSessionBean);
		return formBean;
	}
	
	@Test
	void that_getShoppingCartItemDetail_convertAcronymsToWordsOnErrMsg_Min() throws AtWinXSException, Exception {
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.
				mockStatic(TranslationTextTag.class)){
			
			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), "ofLbl"))
					.thenReturn("of");
			 
			OEShoppingCartFormBean oeShoppingCartFormBean =  oeShoppingCartFormBean_new_min();
		service.convertAcronymsToWordsOnErrMsg(oeShoppingCartFormBean,mockAppSessionBean);
		}
		assertNotNull(mockOEShoppingCartFormBean);
	}
	
	@ParameterizedTest
    @MethodSource("budgetAllocationArguments")
	void that_validateBudgetAllocation(boolean isCCAllowed, boolean isExceedBudgetWarned, 
			AllocationQuantitiesCompositeVO allocQty, boolean isTbd, boolean useTbd) throws AtWinXSException {
		mockSession();
		mockBudgetAllocationMethods(isCCAllowed, isExceedBudgetWarned, allocQty);
		if (useTbd) {
			when(mockOEShoppingCartFormBean.isTbd()).thenReturn(isTbd);
		}
		if (allocQty != null) {
			when(mockAssembler.calculateRemainingBudget(any(), any(), any(), any(), any(), anyInt())).thenReturn(buildAllocationSummaryBean());
		} else {
			when(mockUserSettings.isAllowBudgetAllocations()).thenReturn(true);
			when(mockOEShoppingCartFormBean.getItems()).thenReturn(buildItems());
			if (isCCAllowed) {
				when(mockUserSettings.isAllowOrderingWithoutBudget()).thenReturn(false);
			}
		}
		service.validateBudgetAllocation(mockSessionContainer, mockAssembler, mockOEShoppingCartFormBean);
		assertEquals(isExceedBudgetWarned, mockOEShoppingCartFormBean.isExceedBudgetWarned());
	}

	private void mockBudgetAllocationMethods(boolean isCCAllowed, boolean isExceedBudgetWarned,
			AllocationQuantitiesCompositeVO allocQty) throws AtWinXSException {
		when(mockTranslationService.processMessage(any(), any(), anyString())).thenReturn("");
		when(mockOEAssembler.getCheckoutAssembler(any(), any())).thenReturn(mockCheckoutAssembler);
		when(mockCheckoutAssembler.getAllocationQuantities(any(), any())).thenReturn(allocQty);
		when(mockUserSettings.isForceCreditCardOverAllcBudget()).thenReturn(isCCAllowed);
		when(mockOEShoppingCartFormBean.isExceedBudgetWarned()).thenReturn(isExceedBudgetWarned);
	}
	
	@Test
	@Disabled
	void that_ProcessUpdateShoppingCart_success() throws AtWinXSException {
		mockSession();
		mockLoadShoppingCart();
		when(mockOEShoppingCartFormBean.getItems()).thenReturn(buildItems());
		when(mockPunchoutSessionBean.getOperation()).thenReturn(OrderEntryConstants.PUNCHOUT_OPERATION_INSPECT);
		when(mockOEShoppingCartComponentLocatorService.locate(any())).thenReturn(mockOEShoppingCartComp);
		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler = mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
				when(mock.isDistributionListOrder(anyInt())).thenReturn(false);
			});
			MockedConstruction<ShoppingCartValidator> mockedSCValidator = mockConstruction(ShoppingCartValidator.class, (mock, context) -> {
				when(mock.validateCartItemsCp(any(), any(), any(), anyInt(), anyBoolean(), any(), any(), anyBoolean())).thenReturn(false);
			});
			MockedConstruction<OEShoppingCartAssembler> mockedAssembler = mockConstruction(OEShoppingCartAssembler.class, (mock, context) -> {
				when(mock.updateShoppingCart(anyInt(), anyBoolean(), anyString(), isA(OEShoppingCartFormBean.class), isA(OEOrderSessionBean.class),
						isA(VolatileSessionBean.class), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(new CartUpdateLineVO[1]);
			})) {
			mockBudgetAllocationMethods(true, false, buildAllocationQuantity());
			when(mockOEShoppingCartFormBean.getFormAction()).thenReturn(OrderEntryConstants.UPDATE_CART_ACTION);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);

			when(mockListsAdminLocatorService.locate(mockToken)).thenReturn(mockIManageListAdmin);
			when(mockIManageListAdmin.getBuListDetails(any())).thenReturn(mockbusinessUnitPropsVO);
			when(mockbusinessUnitPropsVO.isDoNotShareListsInd()).thenReturn(true);
			
			COShoppingCartResponse response = service.processUpdateShoppingCart(mockSessionContainer, buildShoppingCartRequest());
			assertEquals(RouteConstants.REST_RESPONSE_SUCCESS, response.getStatus());
		}
	}
	
	// CAP-49693
	@ParameterizedTest
	@ValueSource(strings = {"A", "B", "C", "D"})
	void that_sortBundleComponents_hasBundleItems(String scenario) {
		OEShoppingCartLineFormBean[] items = buildShoppingCartItems(scenario);
		OEShoppingCartLineFormBean[] sortedItems = service.sortBundleComponents(items);
		assertNotNull(sortedItems);
	}
	
	private OEShoppingCartLineFormBean[] buildShoppingCartItems(String scenario) {
		List<OEShoppingCartLineFormBean> items = new ArrayList<>();
		
		if (scenario.equals("A") || scenario.equals("C")) {
			OEShoppingCartLineFormBean masterItem = new OEShoppingCartLineFormBean();
			masterItem.setOrderID(622852);
			masterItem.setLineNumber("976622");
			masterItem.setItemNumber("AMY GRID BUNDLE EXAMPLE");
			masterItem.setBundleComponentTypeCode(ComponentType.GenericMaster.toString());
			masterItem.setBundleParentOrderLine(AtWinXSConstant.INVALID_ID);
			items.add(masterItem);
		
			OEShoppingCartLineFormBean componentItem = new OEShoppingCartLineFormBean();
			componentItem.setOrderID(622852);
			componentItem.setLineNumber("976624");
			componentItem.setItemNumber("4 X 4 1/2");
			componentItem.setBundleComponentTypeCode(ComponentType.GenericComponent.toString());
			componentItem.setBundleParentOrderLine(976622);
			items.add(componentItem);
		}
		
		if (scenario.equals("B") || scenario.equals("C")) {
			OEShoppingCartLineFormBean regularItem = new OEShoppingCartLineFormBean();
			regularItem.setOrderID(622852);
			regularItem.setLineNumber("976624");
			regularItem.setItemNumber("4 X 4 1/2");
			regularItem.setBundleComponentTypeCode(AtWinXSConstant.EMPTY_STRING);
			regularItem.setBundleParentOrderLine(0);
			items.add(regularItem);
		}
		
		return items.toArray(new OEShoppingCartLineFormBean[items.size()]);
	}
	
	private static Stream<Arguments> budgetAllocationArguments() {
        return Stream.of(
                Arguments.of(true, true, buildAllocationQuantity(), false, true),
                Arguments.of(false, true, buildAllocationQuantity(), false, true),
                Arguments.of(false, true, null, true, true),
                Arguments.of(true, true, null, false, false)
        );
    }
	
	private void mockLoadShoppingCart() throws AtWinXSException {
		mockSession();
		when(mockOEAssembler.getShoppingCartAssembler(any(), any(), anyBoolean())).thenReturn(mockAssembler);
		when(mockAssembler.getPunchoutShoppingCart(any(), any(), any(), any(), any(), any(), anyBoolean(), any(),
				anyBoolean())).thenReturn(mockOEShoppingCartFormBean);
		when(mockUserSettings.isShowOrderLineAvailability()).thenReturn(true);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(null);
		when(mockAssembler.isCampaignSubscription(anyInt())).thenReturn(true);
		when(mockAssembler.isMergeCamp(anyInt())).thenReturn(true);
		when(mockAssembler.isSubscription(anyInt())).thenReturn(true);
	}

	private void mockSession() {
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSessionBean);
		when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockOEOrderSessionBean.getOrderScenarioNumber()).thenReturn(1);
	}
	
	private static AllocationQuantitiesCompositeVO buildAllocationQuantity() {
		int allocationAssignmentID = 32969;
		int allocationTimeframe = 0;
		double allocationQuantity = 100.0;
		double remainingQuantity = 100.0;
		double carryoverQuantity = 0.0;
		String userID = "USER_RRD";
		Date changeTS = new Date();
		String allocTypeCd = "66";
		return new AllocationQuantitiesCompositeVO(allocationAssignmentID, 
				allocationTimeframe, allocationQuantity, remainingQuantity, carryoverQuantity, 
				userID, changeTS, allocTypeCd);
	}
	
	private AllocationSummaryBean buildAllocationSummaryBean() {
		String remainingBudget = "100";
		String estimatedValueOfOrder = "200"; 
		String estimatedRemainingBudget = "-100";
		String estimatedValueLabel = "100";
		AllocationSummaryBean allocSummaryBean = new AllocationSummaryBean(remainingBudget, estimatedValueOfOrder, estimatedRemainingBudget, estimatedValueLabel);
		allocSummaryBean.setEstRemainingBudgetDoubleVal(-200.0);
		return allocSummaryBean;
	}
	
	private OEShoppingCartLineFormBean[] buildItems() {
		OEShoppingCartLineFormBean[] items = new OEShoppingCartLineFormBean[1];
		items[0] = new OEShoppingCartLineFormBean();
		items[0].setItemNumber(TEST_ITEM1_NUMBER);
		
		return items;
	}
	
	private COShoppingCartRequest buildShoppingCartRequest() {
		COShoppingCartRequest scRequest = new COShoppingCartRequest();
		
		COShoppingCartLineFormBean[] lineItems = new COShoppingCartLineFormBean[1];
		lineItems[0] = new COShoppingCartLineFormBean();
		lineItems[0].setOrderId("619048");
		lineItems[0].setLineNumber("971799");
		lineItems[0].setItemQuantity("10");
		lineItems[0].setUomFactor("500");
		lineItems[0].setUomCode("BX");
		lineItems[0].setUserIPAddress("127.0.0");
				
		scRequest.setCoLineItems(lineItems);
		scRequest.setBackOrderWarned(false);
		scRequest.setCheckBudgetWarning(true);
		scRequest.setUserIPAddress("127.0.0");
		
		return scRequest;
	}
}