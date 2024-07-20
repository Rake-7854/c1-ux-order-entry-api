/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/08/24				C Codina				CAP-46200					Initial Version
 *	01/18/24				C Codina 				CAP-46379					C1UX BE - Method to retrieve Attribute Filters for Order Entry
 *	01/18/24				S Ramachandran			CAP-46304					Retrieve standard options attributes to use for the Catalog Page
 *	01/31/24				N Caceres				CAP-46698					Retrieve order wizard questions
 *	02/06/24				C Codina				CAP-46723					Perform order wizard Search
 *	02/21/24				C Codina				CAP-47086					C1UX BE - Order wizard api that will perform search
 */

package com.rrd.c1ux.api.controllers.catalog;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeRequest;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeResponse;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchRequest;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchResponse;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionRequest;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionResponse;
import com.rrd.c1ux.api.services.catalog.CatalogService;
import com.rrd.c1ux.api.services.catalog.wizard.OrderWizardService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CatalogController")
@Tag(name = "Catalog Attributes Controller")
public class CatalogController extends BaseCPApiController {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CatalogController.class);

	private final CatalogService catalogService;
	// CAP-46698
	private final OrderWizardService orderWizardService;
	
	protected CatalogController(TokenReader tokenReader, CPSessionReader sessionReader, 
			CatalogService catalogService, OrderWizardService orderWizardService) {
		super(tokenReader, sessionReader);
		this.catalogService = catalogService;
		this.orderWizardService = orderWizardService;
	}
	
	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return true;
	}
	@PostMapping(value = RouteConstants.GET_ATTRIBUTE_FILTERS, produces = {MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Catalog Attributes")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	public ResponseEntity<CatalogAttributeResponse> getCatalogAttributes(
			@RequestHeader(value = "ttsession", required = false)String ttsession,
			@RequestBody CatalogAttributeRequest request)  throws AtWinXSException, IllegalAccessException, InvocationTargetException, CPRPCException{
		
		LOGGER.debug("In getCatalogAttributes");
		
		SessionContainer sc = getSessionContainer(ttsession);
		CatalogAttributeResponse response = catalogService.getCatalogAttributes(sc, request);
		
		return new ResponseEntity<>(response,(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
		
	}
	
	@PostMapping(value = RouteConstants.GET_ORDER_WIZARD, produces = {MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Order Wizard Question")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	public ResponseEntity<OrderWizardQuestionResponse> getOrderWizardQuestion(
			@RequestHeader(value = "ttsession", required = false)String ttsession,
			@RequestBody OrderWizardQuestionRequest request) throws AtWinXSException {
		
		LOGGER.debug("In getOrderWizardQuestion");
		
		SessionContainer sc = getSessionContainer(ttsession);
		OrderWizardQuestionResponse response;
		response = orderWizardService.getOrderWizardQuestion(sc, request);
		
		return new ResponseEntity<>(response,(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@PostMapping(value = RouteConstants.WIZARD_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Perform Wizard Search")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	public ResponseEntity<OrderWizardSearchResponse> performWizardSearch(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody OrderWizardSearchRequest request) throws AtWinXSException {

		LOGGER.debug("In performWizardSearch");

		SessionContainer sc = getSessionContainer(ttsession);
		OrderWizardSearchResponse response = orderWizardService.performWizardSearch(sc, request);
						
		return new ResponseEntity<>(response,(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
