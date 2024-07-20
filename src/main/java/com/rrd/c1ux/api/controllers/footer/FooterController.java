/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/14/22	Sumit kumar		CAP-35547		Create API service to return footer text, links, and text for those linked pages
 */
package com.rrd.c1ux.api.controllers.footer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.footer.FooterResponse;
import com.rrd.c1ux.api.services.footer.FooterService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController("Footer")
@RequestMapping(RouteConstants.LOAD_FOOTER)
@Tag(name = "api/footertext")
public class FooterController extends BaseCPApiController{
	
	private static final Logger logger = LoggerFactory.getLogger(FooterController.class);
	@Autowired
	FooterService footerService;
	/**
	 * @param tokenReader {@link TokenReader}
	 * @param sessionReader {@link FooterController}
	 */
	protected FooterController(TokenReader tokenReader, CPSessionReader sessionReader) {
		super(tokenReader, sessionReader);
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	/**
	 * @param ttsession {@link String}
	 * @return the footerResponse object {@link FooterResponse}
	 * @throws AtWinXSException
	 */
		@GetMapping(
		        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
		    @Operation(
		        summary = "Get footer text, links, and text for those linked pages")
		//CAP-35547		Create API service to return footer text, links, and text for those linked pages
		public FooterResponse getFooterText(@RequestHeader(value = "ttsession", required=false) String ttsession) throws AtWinXSException {
			SessionContainer sc = null;
				sc = getSessionContainer(ttsession);
			return footerService.loadFooter(sc);
		}
	
	

}
