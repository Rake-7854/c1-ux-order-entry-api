/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				DTS#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	12/09/2022 (Sep2022)    Krishna Natarajan        CAP-35708  				Create API service - for Message Center texts and links
 *	09/12/22				A Boomker				 CAP-35436					Adding service for returning messages for message center
 *	06/27/23				C Codina 				 CAP-40833					Address Low Priority Security Hotspots Identified by SonarQube - Dev Only
 */


package com.rrd.c1ux.api.controllers.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.messages.MessageCenterResponse;
import com.rrd.c1ux.api.models.messages.ShowMessageCenterResponse;
import com.rrd.c1ux.api.services.messages.MessageCenterService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Krishna Natarajan
 */
@RestController("MessageCenterController")
public class MessageCenterController extends BaseCPApiController {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageCenterController.class);
	
	@Autowired
	private MessageCenterService mMessageCenterService;

	/**
	 * @param tokenReader
	 * @param sessionReader
	 * @param messageCenterService
	 */
	protected MessageCenterController(TokenReader tokenReader, CPSessionReader sessionReader, MessageCenterService messageCenterService) {
		super(tokenReader, sessionReader);
		mMessageCenterService = messageCenterService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.HOMEPAGE_SERVICE_ID;
	}
	
	/**
	 * @param ttsessionid
	 * @return
	 * @throws AtWinXSException
	 */
	@PostMapping(value = RouteConstants.SHOW_MESSAGE_CENTER_LINK, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "/api/messages/showmessagecenter/")
	@Operation(summary = "Get message center info for the user group in the session in context")
	public ShowMessageCenterResponse getShowMessageCenter(@RequestHeader(required = false) String ttsessionid)
			throws AtWinXSException {

		SessionContainer sc = getSessionContainer(ttsessionid);
		ShowMessageCenterResponse response = null;
		try {
			response = mMessageCenterService.getShowResponse(sc);
		} catch (Exception e) {
			//CAP-40833 - Replaced printStrackTrace with logger
			logger.error("Exception thrown in call to getShowResponse() {} {}", e.getMessage(), e);
		}
		return response;
	}
	
	@PostMapping(value=RouteConstants.MESSAGE_CENTER_MESSAGES , produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "messages/getMessages")
	@Operation(summary = "Get messages for message center")
	public MessageCenterResponse getMessageCenter(@RequestHeader(required=false) String ttsessionid) throws AtWinXSException
	{
		SessionContainer sc = getSessionContainer(ttsessionid);
		MessageCenterResponse response=null;
		try {
			response = mMessageCenterService.getMessagesResponse(sc);
		} catch (Exception e) {
			//CAP-40833 - Replaced printStrackTrace with logger
			logger.error("Exception thrown in call to getMessagesResponse(): {} {}", e.getMessage(), e);
		}
		
		return response;
	}

}
