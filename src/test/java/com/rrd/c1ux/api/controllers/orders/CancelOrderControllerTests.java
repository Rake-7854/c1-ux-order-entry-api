package com.rrd.c1ux.api.controllers.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.cancelorder.CancelOrderResponse;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

class CancelOrderControllerTests extends BaseMvcTest{

	CancelOrderResponse response=getCancelOrderTest();
	
	@BeforeEach
	void setUp() throws Exception {

		when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);// mandy
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(mockPunchoutSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOrderEntrySession);
		when(mockOrderEntrySession.getOESessionBean()).thenReturn(mockOEOrderSessionBean);
		when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		doReturn("").when(mockAppSessionBean).getEncodedSessionId();
	}
	
	@Test
	void that_logoutCancelOrder_returnsExpected() throws Exception, AtWinXSException {
		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		when(mockCancelOrderService.cancelOrder(any(SessionContainer.class), any(ApplicationSession.class), any(AppSessionBean.class), any(OrderEntrySession.class), any()))
				.thenReturn(response);

		// when getCartCount is called, expect 200 status and item numbers in JSON
				mockMvc.perform(
						MockMvcRequestBuilders.post(RouteConstants.CANCEL_ORDER).contentType(MediaType.APPLICATION_JSON)
								.header("ttsessionid", TEST_ENCRIPTED_SESSIONID)
								.characterEncoding("utf-8").accept(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
	}
	
	private static CancelOrderResponse getCancelOrderTest() {
		CancelOrderResponse resp=new CancelOrderResponse();
		resp.setSuccess(true);
		return resp;
	}	
	

}

