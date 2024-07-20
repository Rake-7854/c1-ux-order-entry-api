/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	07/28/23	Sakthi M			 CAP-42545	C1UX BE - Create API to cancel the current order (cancelOrderInProgress)
 *	08/08/23	Krishna Natarajan	 CAP-42545  Changed the call from POST to GET
 */

package com.rrd.c1ux.api.controllers.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.cancelorder.CancelOrderResponse;
import com.rrd.c1ux.api.services.orders.cancelorder.CancelOrderService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECancelOrderAssembler;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController("CancelOrderController")
@Tag(name = "Cancel Order APIs")
public class CancelOrderController extends BaseCPApiController {
	
	@Autowired
	CancelOrderService cancelOrderService;

    protected CancelOrderController(TokenReader tokenReader, CPSessionReader sessionReader,CancelOrderService mCancelOrderService) {
		super(tokenReader, sessionReader);
		cancelOrderService=mCancelOrderService;
	}

    @Override
    protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
    }

    
    @GetMapping(value = RouteConstants.CANCEL_ORDER, produces = { MediaType.APPLICATION_JSON_VALUE, //changed GET
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Cancel order call")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CancelOrderResponse> logoutCancelOrder(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
    	SessionContainer sc = getSessionContainer(ttsession);
    	ApplicationSession appSession = sc.getApplicationSession();
	    AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
	    OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession(); 
	    OECancelOrderAssembler oeCancelOrderAssembler = new OECancelOrderAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
 		CancelOrderResponse response = cancelOrderService.cancelOrder(sc,appSession,appSessionBean,oeSession,oeCancelOrderAssembler);
    	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
