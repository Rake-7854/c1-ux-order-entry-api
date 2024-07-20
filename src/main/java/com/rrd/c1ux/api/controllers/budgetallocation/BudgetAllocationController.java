/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/13/24	L De Leon			CAP-46960				Initial Version
 *	02/16/24	Satishkumar A		CAP-46961				Create API - Remaining Budget Allocations Order Entry
 *	02/26/24	Satishkumar A		CAP-47325				C1UX BE - Create API - Remaining Budget Allocations Order Entry
 *  04/23/24	T Harmon			CAP-48796				Added new method for summary budget allocations.				
 */
package com.rrd.c1ux.api.controllers.budgetallocation;

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
import com.rrd.c1ux.api.models.budgetallocation.AllocationSummaryResponse;
import com.rrd.c1ux.api.models.budgetallocation.BudgetAllocationResponse;
import com.rrd.c1ux.api.services.budgetallocation.BudgetAllocationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("BudgetAllocationController")
@Tag(name = "Budget Allocation API")
public class BudgetAllocationController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BudgetAllocationController.class);

	private final BudgetAllocationService budgetAllocationService;

	protected BudgetAllocationController(TokenReader tokenReader, CPSessionReader sessionReader,
			BudgetAllocationService budgetAllocationService) {
		super(tokenReader, sessionReader);
		this.budgetAllocationService = budgetAllocationService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@GetMapping(value = RouteConstants.GET_BUDGET_ALLOCATION_BANNER_MSG, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve banner message for remaining budget allocation")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<BudgetAllocationResponse> getRemainingBudgetAllocationBannerMessage(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getRemainingBudgetAllocationBannerMessage()");
		SessionContainer sc = getSessionContainer(ttsession);
		BudgetAllocationResponse response = budgetAllocationService.getBannerMessage(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	@GetMapping(value = RouteConstants.GET_REMAINING_BUDGET_ALLOCATIONS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve remaining budget allocations")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<AllocationSummaryResponse> getRemainingBudgetAllocations(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getRemainingBudgetAllocations()");
		//CAP-47325
		SessionContainer sc = getSessionContainer(ttsession);
		AllocationSummaryResponse response = budgetAllocationService.getRemainingBudgetAllocations(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-48796 TH - Added new method for summary page and budget
	@GetMapping(value = RouteConstants.GET_REMAINING_BUDGET_ALLOCATIONS_SUMMARY, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve remaining budget allocations")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<AllocationSummaryResponse> getRemainingBudgetAllocationsSummary(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getRemainingBudgetAllocations()");
		//CAP-47325
		SessionContainer sc = getSessionContainer(ttsession);
		AllocationSummaryResponse response = budgetAllocationService.getRemainingBudgetAllocationsSummary(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}