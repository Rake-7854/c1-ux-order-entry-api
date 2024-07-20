/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/16/2023  N Caceres		CAP-39045	Initial version
 *  05/16/2023	N Caceres		CAP-39046	Add mockToken and remove getToken method
 *  02/06/24	Krishna Natarajan		CAP-46920				Added method with null value for Price 
 *  06/07/24	C Codina		CAP-49893	Added test cases for kit template
 *  06/18/24	S Ramachandran	CAP-50031	Added test for Kit Template validation error message
 */
package com.rrd.c1ux.api.services.addtocart;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartRequest;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.gwt.common.exception.CPRPCRedirectException;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.MKComponentInterface;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.ao.MKTemplateOrderAssembler;
import com.wallace.atwinxs.kits.vo.ContainerVO;
import com.wallace.atwinxs.kits.vo.KitTemplateVO;
import com.wallace.atwinxs.kits.vo.MKKitTemplateComponentVO;
import com.wallace.atwinxs.kits.vo.MKKitTemplateCompositeVO;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

class ItemAddToCartServiceImplTests extends BaseServiceTest {

	@InjectMocks
	private ItemAddToCartServiceImpl serviceToTest;
	
	@Mock
	ItemAddToCartResponse mockResponse;
	
	@Mock
	OrderLineVO mockOrderLineVO;
	
	@Mock
	MKComponentInterface mockMKComponentInterface;
	
	@Mock
	MKKitTemplateCompositeVO mockMKKitTemplateCompositeVO; 
	
	@Mock
	KitTemplateVO mockKitTemplateVO;
	
	@Mock
	MKKitTemplateComponentVO mockMKKitTemplateComponentVO; 
	
	@Mock
	ContainerVO mockContainerVO;
	
	private static final Date TEST_EARLIEST_DATE = new Date(1704067200000L);
	
	@MethodSource("getItemClassification")
	@ParameterizedTest
	void that_addItemToCart_returns_successfully(String itemClassification) throws AtWinXSException, CPRPCRedirectException, CPRPCException {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(new OrderOnBehalfVO());
		when(mockAppSessionBean.getCurrencyLocale()).thenReturn(Locale.US);
		
		
		try (MockedStatic<ItemHelper> mockedStatic = mockStatic(ItemHelper.class) ;
			MockedStatic<TranslationTextTag> mockTranslationTextTag = mockStatic(TranslationTextTag.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(MKTemplateOrderAssembler.class, (mock, context) ->{
				when(mock.validateItemWithinAvailAndExpDates(anyInt(), anyString(), any())).thenReturn(true);
			})){
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage
					(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_PUNCHOUT_NO_OLD_KIT_TEMPLATES))
			.thenReturn(TranslationTextConstants.TRANS_NM_PUNCHOUT_NO_OLD_KIT_TEMPLATES);
			mockedStatic.when(() -> ItemHelper.isCustomizableItem(anyString(), anyString(), any(), any())).thenReturn(true);

		
		when(mockCatalogItem.getItemClassification()).thenReturn(itemClassification);
		when(mockCatalogItem.getVendorItemNumber()).thenReturn("Test");
		
		// CAP-39046 Replace getToken method by mockToken
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockCatalogItem);
		when(mockOrderEntryUtilService.currentlyWithinKit(isA(OrderEntrySession.class), isA(VolatileSessionBean.class))).thenReturn(true);
		
		// CAP-50031 - KitTemplate validation tests separated    
		serviceToTest = Mockito.spy(serviceToTest);
		doNothing().when(serviceToTest).validateKitTemplate(any(),any(),any());
		
		assertNotNull(serviceToTest.addItemToCart(mockSessionContainer, createItemAddToCartRequest()));

		}
	}
	private static Stream<Arguments> getItemClassification(){
		return Stream.of(
				Arguments.of(ItemConstants.ITEM_CLASS_KIT_TEMPLATE),
				Arguments.of("Custom Document"),
				Arguments.of("Bundle Item"));
		
	}

	private ItemAddToCartRequest createItemAddToCartRequest() {
		ItemAddToCartRequest itemAddToCartRequest = new ItemAddToCartRequest();
		itemAddToCartRequest.setPrice("$1.00");
		itemAddToCartRequest.setCatalogLineNumber(0);
		itemAddToCartRequest.setItemNumber("16841");
		itemAddToCartRequest.setItemQuantity(1);
		itemAddToCartRequest.setSelectedUom("");
		itemAddToCartRequest.setVendorItemNumber("WEA18475");
		return itemAddToCartRequest;
	}
	
	private ItemAddToCartRequest createItemAddToCartRequest_priceNull() {
		ItemAddToCartRequest itemAddToCartRequest = new ItemAddToCartRequest();
		itemAddToCartRequest.setPrice(null);
		itemAddToCartRequest.setCatalogLineNumber(0);
		itemAddToCartRequest.setItemNumber("16841");
		itemAddToCartRequest.setItemQuantity(1);
		itemAddToCartRequest.setSelectedUom("");
		itemAddToCartRequest.setVendorItemNumber("WEA18475");
		return itemAddToCartRequest;
	}
	
	@Test
	void that_addItemToCart_returns_successfully_pricenull() throws AtWinXSException, CPRPCRedirectException, CPRPCException {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(new OrderOnBehalfVO());
		// CAP-39046 Replace getToken method by mockToken
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockCatalogItem);
		when(mockOrderEntryUtilService.currentlyWithinKit(isA(OrderEntrySession.class), isA(VolatileSessionBean.class))).thenReturn(true);
		when(mockOESavedOrderComponentLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockIOESavedOrderComponent);
		
		assertNotNull(serviceToTest.addItemToCart(mockSessionContainer, createItemAddToCartRequest_priceNull()));

	}

	// CAP-50031 - 	Kit Template Item validation tests for Approved, DisApprove, 
	// 				Request For Approval, Earliest Order Date, Last Possible Order Date
	@MethodSource("getKitTemplateItem")
	@ParameterizedTest
	void that_addItemToCartKitTemplate_returns_200(String itemClassificationKitTemplate, String ktOrderStatus,
			int currentDate, int currentMonth, int currentYear, String earliestOrderDate, String lastPossibleOrderDate)
			throws AtWinXSException, CPRPCRedirectException, CPRPCException, ParseException {

		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockCatalogItem.getVendorItemNumber()).thenReturn("Test");

		MKKitTemplateComponentVO[] arrayKitTemplateComponentVO = new MKKitTemplateComponentVO[1];
		arrayKitTemplateComponentVO[0] = mockMKKitTemplateComponentVO;
		when(mockKitTemplateVO.getKitTmpltApprovedCd()).thenReturn(ktOrderStatus);

		MKKitTemplateCompositeVO testMKKitTemplateCompositeVO = new MKKitTemplateCompositeVO(mockKitTemplateVO,
				arrayKitTemplateComponentVO, mockContainerVO);
		testMKKitTemplateCompositeVO.setEarliestOrderDate(new Date());
		testMKKitTemplateCompositeVO.setLastPossibleOrderDate(new Date());

		when(mockMKComponentInterfaceLocatorService.locate(any())).thenReturn(mockMKComponentInterface);
		when(mockMKComponentInterface.getKitTemplate(any())).thenReturn(testMKKitTemplateCompositeVO);

		when(serviceToTest.getCurrentDate())
				.thenReturn(new GregorianCalendar(currentYear, currentMonth, currentDate).getTime());

		try (MockedStatic<Util> mockedUtil = mockStatic(Util.class);
				MockedConstruction<MKTemplateOrderAssembler> mockedAssembler = mockConstruction(
						MKTemplateOrderAssembler.class, (mock, context) -> {
							when(mock.validateItemWithinAvailAndExpDates(anyInt(), anyString(), any()))
									.thenReturn(true);
						})) {

			mockedUtil.when(() -> Util.getStringFromDate(TEST_EARLIEST_DATE, null)).thenReturn(earliestOrderDate);
			when(mockCatalogItem.getItemClassification()).thenReturn(itemClassificationKitTemplate);

			AtWinXSMsgException actualEx = null;
			try {

				serviceToTest.validateKitTemplate(mockCatalogItem, mockAppSessionBean, mockPunchoutSessionBean);
			} catch (AtWinXSException ex) {

				actualEx = (AtWinXSMsgException) ex;
			}

			assertNotNull(actualEx, "Expected an exception");
		}
	}

	// CAP-50031 - 	Kit Template Item validation Input for Approved, DisApprove, 
	// 				Request For Approval, Earliest Order Date, Last Possible Order Date 
	private static Stream<Arguments> getKitTemplateItem() {
		
		return Stream.of(
			Arguments.of(ItemConstants.ITEM_CLASS_KIT_TEMPLATE, "A", "19", Calendar.JUNE, "2024", "20/06/2024",
					"20/06/2024"),
			Arguments.of(ItemConstants.ITEM_CLASS_KIT_TEMPLATE, "A", "21", Calendar.JUNE, "2024", "20/06/2024",
					"20/06/2024"),
			Arguments.of(ItemConstants.ITEM_CLASS_KIT_TEMPLATE, "A", "20", Calendar.JUNE, "2024", "20/06/2024",
					"20/06/2024"),
			Arguments.of(ItemConstants.ITEM_CLASS_KIT_TEMPLATE, "P", "20", Calendar.JUNE, "2024", "20/06/2024",
					"20/06/2024"),
			Arguments.of(ItemConstants.ITEM_CLASS_KIT_TEMPLATE, "R", "20", Calendar.JUNE, "2024", "20/06/2024",
					"20/06/2024"));
	}
	
	// CAP-50031 - 	Kit Template Item validation throws Error not available for ordering 
	//				if it punchout Session and No Vendor Item Number 
	@Test
	void that_addItemToCartKitTemplate_punchoutWithNoVendor_returnsError_200() throws AtWinXSException {

		when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_KIT_TEMPLATE);

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = mockStatic(TranslationTextTag.class);) {

			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(),
							TranslationTextConstants.TRANS_NM_PUNCHOUT_NO_OLD_KIT_TEMPLATES))
					.thenReturn(TranslationTextConstants.TRANS_NM_PUNCHOUT_NO_OLD_KIT_TEMPLATES);

			AtWinXSMsgException actualEx = null;
			try {

				serviceToTest.validateKitTemplate(mockCatalogItem, mockAppSessionBean, mockPunchoutSessionBean);
			} catch (AtWinXSException ex) {

				actualEx = (AtWinXSMsgException) ex;
			}

			assertNotNull(actualEx, "Expected an exception");
		}
	}
	
}