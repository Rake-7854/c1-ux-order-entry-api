package com.rrd.c1ux.api.services.orders.cancelorder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.cancelorder.CancelOrderResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

class CancelOrderServiceImplTests extends BaseServiceTest{

	
	private CancelOrderResponse cancelResp;
	
	@InjectMocks
	private CancelOrderServiceImpl serviceToTest;

	@BeforeEach
	public void setUp() throws Exception {
		cancelResp = new CancelOrderResponse();
	}
	private void setUpCommonMocks() {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockOECancelOrderAssembler.isOrderRequest(0)).thenReturn(true);
	}
	
	@Test
	void that_getCancelOrderService_returns_success() throws AtWinXSException {
		setUpCommonMocks();
		when(mockVolatileSessionBean.getOrderId()).thenReturn(1234);
		when(mockAppSessionBean.hasService(1)).thenReturn(true);
		when(mockSessionHandlerService.loadSession(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
		serviceToTest = Mockito.spy(serviceToTest);
		doReturn(true).when(serviceToTest).isCancelNewOrderOnly(ArgumentMatchers.anyInt());

		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getProfileSelections()).thenReturn(mockProfileSelection);
		//CAP-43159
		Map<String, Integer> getProfileSelections = new HashMap<>();
		getProfileSelections.put("TestString", 1234);
		when(mockAppSessionBean.isRefreshCatalogEnabled()).thenReturn(true);
		when(mockProfileSelection.isHasEnforceOnCatalog()).thenReturn(true);
		when(mockProfileSelection.getProfileSelections()).thenReturn(getProfileSelections);
		when(mockProfileSelection.resetLoadedForCatRefresh(mockAppSessionBean, mockOESession, mockVolatileSessionBean, true)).thenReturn(true);

		cancelResp = serviceToTest.cancelOrder(mockSessionContainer, mockApplicationSession, mockAppSessionBean, mockOESession, mockOECancelOrderAssembler);
		Assertions.assertNotNull(cancelResp);
		assertTrue(cancelResp.isSuccess());
		
	}
	@Test
	void testCheckAccessForbidden(){
		
		assertThrows(AccessForbiddenException.class, () -> {
			serviceToTest.cancelOrder(mockSessionContainer, mockApplicationSession, mockAppSessionBean, mockOESession, mockOECancelOrderAssembler);
		});
	}
	
}
