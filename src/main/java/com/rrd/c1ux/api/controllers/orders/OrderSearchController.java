
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	11/22/22	Sakthi M        	CAP-36560	    Created controller as per the requirement to fetch the sort,Page number
 *  11/28/22    Satishkumar A       CAP-36854       Order Search page filter option - navigation
 *  11/25/22    Sumit Kumar		    CAP-36559       Controller to retrieves orders search records
 *  11/23/22	S Ramachandran  	CAP-36557   	Get Order details - Order info, customer info, Items ordered,  Ordered cost sections
 *  12/08/22    Sakthi M            CAP-37295       Order search - Order Status Dropdown Menu values
 *  12/23/22	S Ramachandran		CAP-36916 		Get Shipments and Items under Shipping and tracking information
 *  01/03/23	Sumit Kumar			CAP-37779 		Controller to retrieves repeated searched orders information
 *  01/12/23 	S Ramachandran		CAP-37781       Get Order file email details
 *  02/23/23	Sumit Kumar			CAP-38765 		removed 'api/orders/getordersearchrepeat'(CAP-37779) added its logic on 'api/orders/getsearchordersdetail'
 *  02/24/23    C Porter            CAP-38897       HTTP 403 response handling
 *  03/06/23	Sakthi M			CAP-39070		getOrderSearchPagination API needs to return common Settings and Preferences object in response and correct bad data
 *	03/10/23	A Boomker			CAP-39173 		Override session retrieve to force simple view
 *  03/15/23    Satishkumar A       CAP-38736       API Standardization - Search Navigation in Order Search conversion to standards
 *  03/15/23    Sakthi M            CAP-38737		API Standardization - Order Status Codes in Order Search conversion to standards
 *  03/17/23	S Ramachandran		CAP-38720 		API Standardization - Order Detail by Sales ref in Order Search conversion to standards
 *  03/21/23    Satishkumar A       CAP-38738       API Standardization - Order Files response in Order Search conversion to standards
 *  03/21/23    Sumit Kumar         CAP-38651		API Standardization - Search Orders in Order Search conversion to standards
 *	11/16/23	L De Leon			CAP-45180		Added getCountries() API
 *	11/16/23	Krishna Natarajan	CAP-45181		Added getStates() API
 *	12/04/23	Krishna Natarajan	CAP-45180		Added getSessionContainer(ttsession) to throw 401 for a bad/expired session
 *	03/12/24	S Ramachandran		CAP-47744		Controller handler for api to download Order file from order in Order Search
 *	03/13/24	S Ramachandran		CAP-47841		Added service call to controller handler for api to download Order file from order in Order Search
 */

package com.rrd.c1ux.api.controllers.orders;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSItemInShipmentTrackingRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentAndTrackingRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentAndTrackingResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentDetail;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderFileRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderFileResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchNavigationResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderSearchResultResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.CountriesResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.CountryCodeRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.GenericEncodedRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderFileEmailDetailResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderStatusCodeRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderStatusCodeResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.StatesResponse;
import com.rrd.c1ux.api.models.settingsandprefs.CatalogUtilityNavigationInfo;
import com.rrd.c1ux.api.services.checkout.CODeliveryInformationService;
import com.rrd.c1ux.api.services.orders.ordersearch.COOrderFilesService;
import com.rrd.c1ux.api.services.orders.ordersearch.OrderSearchService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * @author Sakthivel Mottaiyan
 *
 */
@RestController("OrderSearchController")
@Tag(name = "Order Search APIs")
public class OrderSearchController extends BaseCPApiController {

private final OrderSearchService orderSearchService;

private final COOrderFilesService orderFileService;

// CAP-45180
private final CODeliveryInformationService deliveryInformationService;

private static final Logger logger = LoggerFactory.getLogger(OrderSearchController.class);

/**
 * @param tokenReader {@link TokenReader}
 * @param sessionReader {@link CPSessionReader}
 */
protected OrderSearchController(TokenReader tokenReader, CPSessionReader sessionReader,
		OrderSearchService sOrderSearchService, COOrderFilesService sOrderFileService,
		CODeliveryInformationService deliveryInformationService) {
        super(tokenReader, sessionReader);
        orderSearchService=sOrderSearchService;
        orderFileService=sOrderFileService;
		this.deliveryInformationService = deliveryInformationService; // CAP-45180
}

/**
 *@return a constant ORDER_SEARCH_SERVICE_ID AtWinXSConstant {@link AtWinXSConstant}
 */
@Override
protected int getServiceID() {
	return AtWinXSConstant.ORDER_SEARCH_SERVICE_ID;
}




@GetMapping(value=RouteConstants.GET_ORDER_SORT_PAGINATION , produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "orders/getordersearchsortpagination")
@Operation(summary = "Get Order search sort and pagination default value for utility navication")
public CatalogUtilityNavigationInfo getOrderSearchSortPagination(@RequestHeader(value = "ttsession", required=false) String ttsession) throws AtWinXSException {
    logger.debug("In getOrderSearchSortPagination");

    SessionContainer sc = getSessionContainer(ttsession);

    return orderSearchService.getOrderSortPagination(sc);
}


/**
 * @param ttsession with the session ID
 * @return {@link COOrderSearchNavigationResponse}
 * @throws AtWinXSException
 */
//CAP-36854 - Order Search - search order navigation functionality and return objects
@PostMapping(value = RouteConstants.ORDER_SEARCH_NAVIGATION, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "/api/orders/order-search-navigation")
@Operation(summary = "Order Search - search order navigation details i.e. order search filter options")
public COOrderSearchNavigationResponse getOrderDetailforNavigation(
		@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

	logger.debug("In OrderSearchController - getOrderDetailforNavigation() ");

	SessionContainer sc = getSessionContainer(ttsession);

	return orderSearchService.populateOrderDetailforNavigation(sc);
}

//CAP-38736 -  API Standardization - Search Navigation in Order Search conversion to standards
@PostMapping(value = RouteConstants.ORDER_SEARCH_LOAD_OPTIONS, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
@Operation(summary = "Order Search - load options")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
public ResponseEntity<COOrderSearchNavigationResponse> orderSearchLoadOptions(
		@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
	logger.debug("In OrderSearchController - getOrderSearchLoadOptions() ");

	COOrderSearchNavigationResponse response = new COOrderSearchNavigationResponse();

	SessionContainer sc = getSessionContainer(ttsession);

	response = orderSearchService.populateOrderDetailforNavigation(sc);

	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));


}


/**
 * @param ttsession            {@link String}
 * @param COOrderSearchRequest {@link COOrderSearchRequest}
 * @return the COOrderSearchResultResponse object
 *         {@link COOrderSearchResultResponse}
 * @throws AtWinXSException
 */
//CAP-36559       Controller method to retrieves orders search records
//CAP-45054 Modified api to handle 422
@PostMapping(value = RouteConstants.SEARCH_ORDERS_DETAIL, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "orders/getsearchordersdetail")
@Operation(summary = "Get Order Search listing of search results")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
public ResponseEntity<COOrderSearchResultResponse> getSearchOrdersDetail(
		@RequestHeader(value = "ttsessionid", required = false) String ttsessionid,
		@RequestBody COOrderSearchRequest coorderSearchRequest) throws AtWinXSException {
	logger.debug("In getSearchOrdersDetail Controller");
	SessionContainer sc = getSessionContainer(ttsessionid);

	COOrderSearchResultResponse response = orderSearchService.getSearchOrdersDetail(sc, coorderSearchRequest);
	
	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));

}

/**
 * @param ttsession            {@link String}
 * @param COOrderSearchRequest {@link COOrderSearchRequest}
 * @return the COOrderSearchResultResponse object
 *         {@link COOrderSearchResultResponse}
 * @throws AtWinXSException
 */
//CAP-38651     New controller method to retrieves orders search records
//CAP-39972	added API response annotations
@PostMapping(value = RouteConstants.OS_DETAIL, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "/api/os/search")
@Operation(summary = "Get Order Search listing of search results")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
public ResponseEntity<COOrderSearchResultResponse >  searchOrders(
		@RequestHeader(value = "ttsessionid", required = false) String ttsessionid,
		@RequestBody COOrderSearchRequest coorderSearchRequest) throws AtWinXSException {
	logger.debug("In get Search Orders Controller");
	SessionContainer sc = getSessionContainer(ttsessionid);
	COOrderSearchResultResponse response = orderSearchService.getSearchOrdersDetail(sc, coorderSearchRequest);

	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
}


/**
 * @param ttsession with the session ID
 * @return {@link COOSFullDetailResponse}
 * @throws AtWinXSException
 */
//CAP-35559  - Get Order details - Order info, customer info, Items ordered,  Ordered cost sections
@PostMapping(value=RouteConstants.ORDER_SEARCH_DETAILS_FORSALESREF , produces = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "orders/getorderdetails-forsalesref")
@Operation(summary = "Get Order search full detail for Sales Ref")
public COOSFullDetailResponse getOrderSearchFullDetailsWithSalesRef(
		@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession,
		@RequestBody COOSFullDetailRequest request)
		throws AtWinXSException {

	logger.debug("In OrderSearchController - getOrderSearchFullDetailsWithSalesRef()");

	SessionContainer sc = getSessionContainer(ttsession);

	return orderSearchService.getOrderSearchFullDetailsWithSalesRef(sc, request);
}

/**
 * @param ttsession with the session ID
 * @return {@link COOSFullDetailResponse}
 * @throws AtWinXSException
 */
//CAP-38720  - API Standardization - load Order Detail by Sales ref in Order Search conversion to standards
//			 - with Order info, customer info, Items ordered,  Ordered cost sections
@PostMapping(value=RouteConstants.ORDER_SEARCH_LOAD_DETAIL , produces = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
@Operation(summary = "Order search - load detail")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
public ResponseEntity<COOSFullDetailResponse> orderSearchLoadDetail(
		@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession,
		@RequestBody COOSFullDetailRequest request)
		throws AtWinXSException {

	logger.debug("In OrderSearchController - orderSearchLoadDetail()");

	SessionContainer sc = getSessionContainer(ttsession);

	COOSFullDetailResponse response = orderSearchService.getOrderSearchLoadDetail(sc, request);

	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
}


/**
 * @param ttsession with the session ID
 * @return {@link COOSShipmentAndTrackingRequest}
 * @throws AtWinXSException
 */
//CAP-36916  - Get Shipments and Tracking details for a Order, Sales Ref. Number
@PostMapping(value=RouteConstants.ORDER_SEARCH_SHIPMENTS_AND_TRACKING , produces = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "orders/getorderdetails-shipmentsandtracking")
@Operation(summary = "Get Shipments and tracking information")
public COOSShipmentAndTrackingResponse getOSShipmentsAndTracking(
		@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession,
		@RequestBody COOSFullDetailRequest request)
		throws AtWinXSException {

	logger.debug("In OrderSearchController - getOSShipmentsAndTracking()");

	SessionContainer sc = getSessionContainer(ttsession);

	return orderSearchService.getOSShipmentAndTracking(sc, request);
}


/**
 * @param ttsession with the session ID
 * @param COOSItemInShipmentTrackingRequest
 * @return {@link COOSShipmentDetail}
 * @throws AtWinXSException
 */
//CAP-36916  - Get Items & Tracking Information under Shipment for a Order, Sales ref. Number
@PostMapping(value=RouteConstants.ORDER_SEARCH_ITEMS_UNDER_SHIPMENTS , produces = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "orders/getorderdetails-itemsundershipments")
@Operation(summary = "Get Items under Shipments")
public COOSShipmentDetail getOSItemsInShipment(
		@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String ttsession,
		@RequestBody COOSItemInShipmentTrackingRequest request)
		throws AtWinXSException {

	logger.debug("In OrderSearchController - getOSItemsInShipment()");

	SessionContainer sc = getSessionContainer(ttsession);

	return orderSearchService.getOSItemsInShipment(sc, request);
}


/**
 * @param ttsession with the session ID
 * @return {@link OrderStatusCodeResponse}
 * @throws AtWinXSException
 */
//CAP-37295 - Order search - Order Status Dropdown Menu values
@PostMapping(value = RouteConstants.ORDER_STATUS_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "/api/orders/getorderstatuscodeoptions")
@Operation(summary = "Order Status Dropdown Menu values")
public OrderStatusCodeResponse getOrderStatusCode(
		@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestBody OrderStatusCodeRequest request) throws AtWinXSException {

	logger.debug("In OrderSearchController - getOrderStatus() ");
	SessionContainer sc = getSessionContainer(ttsession);
	return orderSearchService.populateOrderStatus(sc,request);
}

//CAP-37757- Order Search/Details - API Creation - Order Files response for Order Search record
@PostMapping(value = RouteConstants.ORDER_FILE_CONTENT, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "/api/orders/getorderfilecontent")
@Operation(summary = "Fetch Order File content")
public COOrderFileResponse getOrderFileContent(
		@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestBody COOrderFileRequest request) throws AtWinXSException {

	logger.debug("In OrderSearchController - getOrderFileContent() ");
	SessionContainer sc = getSessionContainer(ttsession);
	return orderFileService.getOrderFilesContent(sc,request.getSalesRefNum(),request.getOrderNumber());
}


/**
 * @param ttsession {@link String}
 * @param genericEncodedRequest {@link GenericEncodedRequest}
 * @return OrderFileEmailDetailResponse object {@link COOrderSearchResultResponse}
 * @throws AtWinXSException
 */
//CAP-37781       controller method to get order file email details
@PostMapping(value = RouteConstants.ORDER_FILE_EMAIL_DETAIL, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
//@Tag(name = "api/orders/getorderfileemailDetail")
@Operation(summary = "Get Orders Search Result")
public OrderFileEmailDetailResponse getOrderFileEmailDetail(
		@RequestHeader(value = "ttsessionid", required = false) String ttsessionid,
		@RequestBody GenericEncodedRequest genericEncodedRequest) throws AtWinXSException {
	logger.debug("In OrderSearchController Controller -> getOrderFileEmailDetail()");
	SessionContainer sc = getSessionContainer(ttsessionid);
	return orderFileService.buildEmail(sc,genericEncodedRequest);

}


/**
 * @param ttsession {@link String}
 * @param genericEncodedRequest {@link GenericEncodedRequest}
 * @return OrderFileEmailDetailResponse object {@link OrderFileEmailDetailResponse}
 * @throws AtWinXSException
 */
//CAP-38738       controller method to get order file email content
@PostMapping(value = RouteConstants.ORDER_SEARCH_ORDER_FILES, produces = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.APPLICATION_XML_VALUE })
@Operation(summary = "Order Search - order files")
@ApiResponse(responseCode = RouteConstants.HTTP_OK)
@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
public ResponseEntity<OrderFileEmailDetailResponse> getOrderSearchOrderFiles(
		@RequestHeader(value = "ttsessionid", required = false) String ttsessionid,
		@RequestBody GenericEncodedRequest genericEncodedRequest) throws AtWinXSException {
	logger.debug("In OrderSearchController Controller -> getOrderSearchOrderFiles()");
	SessionContainer sc = getSessionContainer(ttsessionid);
	OrderFileEmailDetailResponse response = orderFileService.buildEmail(sc,genericEncodedRequest);
	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));

}

    @Override
    protected boolean checkAccessAllowed(AppSessionBean asb) {

      return asb.hasService(getServiceID());
    }

    // CAP-39173 - override session retrieve to force simple view
    @Override
    public SessionContainer getSessionContainer(String encryptedSessionID) throws AtWinXSException {
        SessionContainer sc = super.getSessionContainer(encryptedSessionID);
        ((OrderStatusSession) sc.getModuleSession()).getOrderStatusSettingsBean().setSimplifiedView(true);
    	return sc;
    }

    /**
     * @param ttsession with the session ID
     * @return {@link OrderStatusCodeResponse}
     * @throws AtWinXSException
     */
    //CAP-38737 - API Standardization - Search Navigation in Order Search conversion to standards
    @PostMapping(value = RouteConstants.ORDER_STATUS_CODES, produces = { MediaType.APPLICATION_JSON_VALUE,
    		MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Order Status Dropdown Menu values")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<OrderStatusCodeResponse> getStatusCodes(
    		@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestBody OrderStatusCodeRequest request) throws AtWinXSException {

    	logger.debug("In OrderSearchController - getStatusCodes() ");
    	OrderStatusCodeResponse response=new OrderStatusCodeResponse();
    	SessionContainer sc = getSessionContainer(ttsession);
    	response=orderSearchService.populateOrderStatus(sc,request);
    	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	// CAP-45180
	@GetMapping(value = RouteConstants.GET_COUNTRIES, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve list of countries with the country codes")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<CountriesResponse> getCountries(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		logger.debug("In getCountries()");
		getSessionContainer(ttsession);//added to throw 401 for a bad/expired session 
		OEOrderSessionBean oeSessionBean = getOrderEntrySession(ttsession).getOESessionBean();

		CountriesResponse response = deliveryInformationService.getCountries(oeSessionBean);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-45181
	@PostMapping(value = RouteConstants.GET_STATES, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve list of states with the names and state codes")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<StatesResponse> getStates(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody CountryCodeRequest request) throws AtWinXSException {
		logger.debug("In getStates()");
		getSessionContainer(ttsession);
		StatesResponse response = deliveryInformationService.getStates(request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-47744, CAP-47841
	/**
	 * @param ttsessionid { @link String }
	 * @param downloadOrderFileRequest { @link DownloadOrderFileRequest }
	 * @param httpServletResponse { @link HttpServletResponse }
	 * @return ResponseEntity<DownloadOrderFileResponse> object { @link ResponseEntity<DownloadOrderFileResponse> }
	 * @throws AtWinXSException
	 */
	@GetMapping(value = RouteConstants.DOWNLOAD_ORDER_FILES, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@Operation(summary = "Download Order file from order in Order Search")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<DownloadOrderFileResponse> downloadOrderFileFromOS(
			@RequestParam (value= "a", required = false) String encryptedParms,
			@RequestHeader(value = "ttsession", required = false) String ttsessionid,
			HttpServletResponse httpServletResponse) throws AtWinXSException {

		logger.debug("In OrderSearchController Controller -> downloadOrderFileinOS()");
		SessionContainer sc = getSessionContainer(ttsessionid);
		
		DownloadOrderFileResponse response = orderFileService.downloadOrderFileFromOS(sc, httpServletResponse, encryptedParms);

		HttpStatus status = HttpStatus.FORBIDDEN;
		if (response.isSuccess()) {
			status = HttpStatus.OK;
			response = null;
		}
		return new ResponseEntity<>(response, status);
  	}
}
