/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				DTS#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	09/28/2022 				Krishna Natarajan        CAP-35767  				Create API service - for cancel punchout quote
 *  10/03/2022				Sakthi M				 CAP-35768					Added controller for cancel edit punchout quote   	
 *	10/12/22				A Boomker				CAP-36437				Fix redirect handling
 *	06/08/23				Satishkumar A			CAP-40835				Address High Priority Security Hotspots Identified by SonarQube - Dev Onlys
 */

package com.rrd.c1ux.api.controllers.punchout;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

import io.swagger.v3.oas.annotations.Operation;

/**
 * @author Krishna Natarajan
 *
 */
@RestController("CancelPunchoutOrderController")
public class CancelPunchoutOrderController {

	private static final Logger logger = LoggerFactory.getLogger(CancelPunchoutOrderController.class);

	private CPSessionReader mSessionReader;

	public CancelPunchoutOrderController(CPSessionReader sessionReader) {
		super();
		this.mSessionReader = sessionReader;
	}

	/**
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 * @throws IOException
	 */
	//CAP-40835
	@GetMapping(value = RouteConstants.CANCEL_PUNCHOUT_QUOTE)
	@Operation(summary = "Redirect the request to CustomPoint to cancel the punchout order")
	public ModelAndView cancelPunchoutOrder(@RequestHeader(required = false) String ttsessionid, HttpServletResponse response)
			throws IOException, AtWinXSException {
		logger.debug("The Cancel punchout re-directed");

		SessionContainer sc = mSessionReader.getSessionContainer(ttsessionid, AtWinXSConstant.APPSESSIONSERVICEID);

		String redirectURL =  "https://" + AppProperties.getServerName() + "/cp/orders/cancelorder.cp"
				+ RouteConstants.ADD_QUERY_PARAM + RouteConstants.TTSESSIONID_PARAM
				+ sc.getApplicationSession().getAppSessionBean().getEncodedSessionId()
				+ RouteConstants.ADD_AMP_ANOTHER_PARAM + RouteConstants.CANCEL_ACTION_PARAM
				+ OrderEntryConstants.EVT_CANCEL_ORDER;
		logger.info("About to redirect cancel order to: " + redirectURL);
		return new ModelAndView(new RedirectView(redirectURL));	
	}
	
	// CAP-35768 Create API service for cancel punchout quote EDITS
	//CAP-40835
	@GetMapping(value = RouteConstants.CANCEL_PUNCHOUT_EDITS)
	@Operation(summary = "Redirect the request to CustomPoint to canceledit the punchout order")
	public ModelAndView cancelPunchoutOrderEdit(@RequestHeader(required = false) String ttsessionid, HttpServletResponse response)
			throws IOException, AtWinXSException {
		logger.debug("The Cancel edit punchout re-directed");

		SessionContainer sc = mSessionReader.getSessionContainer(ttsessionid, AtWinXSConstant.APPSESSIONSERVICEID);
		String redirectURL = "https://" + AppProperties.getServerName() + "/cp/orders/cancelorder.cp"
		+ RouteConstants.ADD_QUERY_PARAM + RouteConstants.TTSESSIONID_PARAM
		+ sc.getApplicationSession().getAppSessionBean().getEncodedSessionId()
		+ RouteConstants.ADD_AMP_ANOTHER_PARAM + RouteConstants.CANCEL_ACTION_PARAM
		+ OrderEntryConstants.EVT_CANCEL_PUNCHOUT_EDIT;
		
		logger.info("About to redirect cancel edit to: " + redirectURL);
		return new ModelAndView(new RedirectView(redirectURL));	
	}
	
}
