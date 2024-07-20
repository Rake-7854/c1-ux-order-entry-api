/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/09/23				C Codina				CAP-41549					Initial Version
 *	10/20/23				C Codina				CAP-44742					Modified getCarouselMessages() to call the service to retrieve carousel messages
 *	10/31/23				Krishna Natarajan		CAP-44742					Set required = false for ttsession request field
 */
package com.rrd.c1ux.api.controllers.messages;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.messages.CarouselResponse;
import com.rrd.c1ux.api.services.messages.CarouselMessagesService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CarouselMessagesController")
@Tag(name = "Carousel Messages Controller")
public class CarouselMessagesController extends BaseCPApiController{
	
	//CAP-44742
	private final CarouselMessagesService carouselMessagesService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CarouselMessagesController.class);
	
	protected CarouselMessagesController(TokenReader tokenReader, CPSessionReader sessionReader, CarouselMessagesService carouselMessagesService) {
		super(tokenReader, sessionReader);
		this.carouselMessagesService = carouselMessagesService;
	}
	@Override
	protected int getServiceID() {
		return AtWinXSConstant.INVALID_ID;
	}
	
	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return true;
	}
	
	@GetMapping(value = RouteConstants.GET_CAROUSEL_MESSAGES, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve Carousel Messages")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	public ResponseEntity<CarouselResponse>  getCarouselMessages(@RequestHeader(value = "ttsession", required = false) String ttsession,
			HttpServletResponse httServletResponse) throws AtWinXSException {
		
		LOGGER.debug("In getCarouselMessages()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		CarouselResponse response = carouselMessagesService.getCarouselMessages(sc);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));

	}

}
