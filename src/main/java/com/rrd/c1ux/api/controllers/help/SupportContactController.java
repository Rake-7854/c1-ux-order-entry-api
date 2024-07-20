/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/15/22	Satish kumar A	CAP-35429- support page 	Initial creation
 *  09/28/22	Satish kumar A	CAP-35430- support page 	Send email with support page form data
 */

package com.rrd.c1ux.api.controllers.help;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.help.SupportContactRequest;
import com.rrd.c1ux.api.models.help.SupportContactResponse;
import com.rrd.c1ux.api.services.help.SupportContactService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

 

@RestController("SupportContactController")
public class SupportContactController extends BaseCPApiController {

    private static final Logger logger = LoggerFactory.getLogger(SupportContactController.class);

	@Autowired
	private SupportContactService supportContactService;
	
	protected SupportContactController(TokenReader tokenReader, CPSessionReader sessionReader, SupportContactService contactService) {
		super(tokenReader, sessionReader);
		supportContactService = contactService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	/**
	 * @param ttsession with the session ID
	 * @return {@link SupportContactResponse}
	 * @throws AtWinXSException
	 */
	//CAP-35429- To load support page information. 
	@PostMapping(value=RouteConstants.SUPPORT_PAGE_LOAD_INFO , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "/api/supportpage/loadinfo")
	@Operation(summary = "Service to return the support page information.")
	public SupportContactResponse getSupportContactDetails(@RequestHeader(required=false) String ttsessionid) throws AtWinXSException  {
	
		logger.debug("In SupportContactController - getSupportContactDetails() - Start");
		
		SessionContainer sc = getSessionContainer(ttsessionid);
		SupportContactResponse supportContactResponse = supportContactService.populateSupportContactDetails(sc);

		logger.debug("In SupportContactController - getSupportContactDetails() - End");

		return supportContactResponse;
	}	
	
	
	/**
	 * @param ttsession with the session ID
	 * @param scRequest {@link SupportContactRequest}
	 * @return {@link SupportContactResponse}
	 * @throws AtWinXSException
	 */
	//CAP-35430 - To submit support page form information. 
	@PostMapping(value=RouteConstants.SUPPORT_PAGE_SUBMIT_FORM , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "/api/supportpage/submitform")
	@Operation(summary = "Service to submit the support page form information and send mail.")
	public SupportContactResponse handleSupportFormSubmission(@RequestHeader(required=false) String ttsessionid, @RequestBody SupportContactRequest scRequest) throws AtWinXSException  {
	
		logger.debug("In SupportContactController - handleSupportContactFormSubmission() - Start");
		
		SessionContainer sc = getSessionContainer(ttsessionid);
		SupportContactResponse supportContactResponse = supportContactService.sendMailToSupportContactDetails(sc,scRequest);

		logger.debug("In SupportContactController - handleSupportContactFormSubmission() - End");

		return supportContactResponse;
	}		
}
