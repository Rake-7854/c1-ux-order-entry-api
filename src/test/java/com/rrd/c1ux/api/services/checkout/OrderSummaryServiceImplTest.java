/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/01/24				L De Leon				CAP-48972					Added test method for populateSummaryFromOrder()
 */
package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.checkout.OrderSummaryResponse;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderLines;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.vo.OrderDueDateVO;

class OrderSummaryServiceImplTest extends BaseOEServiceTest{

	private static final String EXPEDITED_DISCLAIMER = "Expedite fee will only be invoiced if the Expedited Date is successfully met.";
	
	@InjectMocks
	private OrderSummaryServiceImpl service;
	
	@Mock
	private OrderDueDateVO mockOrderDueDate;

	@Mock
	Order mockOrder;

	@Mock
	OrderLines mockOrderLines;
	
	@ParameterizedTest
    @MethodSource("getExpediteFeeArgument")
	void that_populateExpeditedFee_success(boolean isExpeditedOrder, double expeditedOrderFee) throws AtWinXSException {
		OrderSummaryResponse response = new OrderSummaryResponse();
		
		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDate);
		when(mockOrderDueDate.isExpeditedOrder()).thenReturn(isExpeditedOrder);
		
		if (isExpeditedOrder) {
			when(mockOrderDueDate.getExpeditedOrderFee()).thenReturn(expeditedOrderFee);
			if (expeditedOrderFee > 0) {
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				when(mockAppSessionBean.getCurrencyLocale()).thenReturn(Locale.US);
				when(mockTranslationService.processMessage(any(), any(), anyString())).thenReturn(EXPEDITED_DISCLAIMER);
			}
		}
		
		service.populateExpeditedFee(mockAppSessionBean, response, 1);
		
		assertNotNull(response);
	}
	
	private static Stream<Arguments> getExpediteFeeArgument() {
		return Stream.of(
				Arguments.of(true, 100d), 
				Arguments.of(true, 0d),
				Arguments.of(false, 100d),
				Arguments.of(false, 0d));
	}

	// CAP-48972
	@Test
	void that_populateSummaryFromOrder_returns_success() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		doReturn(true).when(service).validateOrder(any(), eq(mockSessionContainer), eq(mockAppSessionBean));
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
		doNothing().when(service).populateOrderSummaryTranslation(eq(mockAppSessionBean), any());
		doReturn(mockOrderLines).when(mockOrder).getOrderLines();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockOrderLines).getNonKitComponentCount();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		OrderSummaryResponse response = service.populateSummaryFromOrder(mockOrder, false, mockSessionContainer);

		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertFalse(response.isAllowOrderTemplate());
	}

}
