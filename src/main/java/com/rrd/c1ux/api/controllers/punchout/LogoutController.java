/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				DTS#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	09/30/2022 				Satishkumar Abburi      CAP-35765  				    Create API service to sign out for punchout
 *	10/11/22				A Boomker			    CAP-35766 					Modification for Punchout flags
 *	06/08/23				Satishkumar A			CAP-40835					Address High Priority Security Hotspots Identified by SonarQube - Dev Only
 */

package com.rrd.c1ux.api.controllers.punchout;

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
import com.rrd.custompoint.ui.common.HeaderTag;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("PunchoutUserLogoutController")
public class LogoutController {

	private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

	private CPSessionReader mSessionReader;

	public LogoutController(CPSessionReader sessionReader) {
		super();
		this.mSessionReader = sessionReader;
	}

	/**
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 */
	//CAP-40835
	@GetMapping(value = RouteConstants.PUNCHOUT_SIGN_OUT)
	@Tag(name = "/api/punchout/logout")
	@Operation(summary = "Redirect the request to CustomPoint to signout the punchout user")
	public ModelAndView createLogoutLink(@RequestHeader(required = false) String ttsessionid, HttpServletResponse response)
			throws AtWinXSException {
		logger.debug("The punchout signout re-directed");

		SessionContainer sc = mSessionReader.getSessionContainer(ttsessionid, AtWinXSConstant.APPSESSIONSERVICEID);
		
		String linkURL = HeaderTag.getLogoutLink(sc.getApplicationSession().getAppSessionBean(), sc.getApplicationSession().getPunchoutSessionBean());

		String redirectURL = "https://" + AppProperties.getServerName() + linkURL;
		logger.info("About to redirect logout to: " + redirectURL);
		return new ModelAndView(new RedirectView(redirectURL));	

	}
	
}
