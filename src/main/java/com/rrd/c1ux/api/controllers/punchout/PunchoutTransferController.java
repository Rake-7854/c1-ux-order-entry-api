
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				DTS#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	09/30/2022 				Sumit kumar		        CAP-35440  				Controller API to get Transfer Cart Url
 *	10/12/22				A Boomker				CAP-36437				Fix redirect handling
 *	06/08/23				Satishkumar A			CAP-40835				Address High Priority Security Hotspots Identified by SonarQube - Dev Only
 */

package com.rrd.c1ux.api.controllers.punchout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.services.punchout.PunchoutService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("PunchoutTransferCartController")
public class PunchoutTransferController{

	private static final Logger logger = LoggerFactory.getLogger(PunchoutTransferController.class);
	
	@Autowired
	PunchoutService punchoutService;

	private CPSessionReader mSessionReader;
	
	protected PunchoutTransferController(CPSessionReader sessionReader) {
		super();
		this.mSessionReader = sessionReader;
	}

	
	/**
	 * @param ttsessionid
	 * @return This will return ( @link String) 
	 * @throws AtWinXSException
	 */
	//CAP-40835
	@GetMapping(value = RouteConstants.PUNCHOUT_TRANSFER_CART)
	@Tag(name = "/punchout/punchoutTransferCart/")
	@Operation(summary = "Controller API to get Transfer Cart Url")
	// CAP-35440 - Controller API to get Transfer Cart Url
	public ModelAndView transferPunchoutCart(@RequestHeader(value = "ttsession", required = false) String ttsession)
			throws AtWinXSException {
		
		SessionContainer sc = null;
		String punchoutTransferUrl="";
		try {
			
			sc=mSessionReader.getSessionContainer(ttsession, AtWinXSConstant.APPSESSIONSERVICEID);
			punchoutTransferUrl = punchoutService.transferPunchoutCart(sc);
		}
		catch (Exception ex) {
		
			logger.error(ex.getMessage());
		}
		logger.info("About to redirect cancel edit to: " + punchoutTransferUrl);

		return new ModelAndView(new RedirectView(punchoutTransferUrl));	
	}

}