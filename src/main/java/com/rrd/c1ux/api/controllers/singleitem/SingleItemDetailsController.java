/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	04/05/22	Satishkumar Abburi	 CAP-33689	Initial creation, Sonarcube changes being addressed
 *	08/29/22	A Boomker			 CAP-35537	Make session optional on all API calls
 *  09/19/22    Sakthi M             CAP-35464  Modify API loading of product details to make catalog line 
 *                                              number optional for cart call and return results fields
 *	02/12/24	S Ramachandran		 CAP-47062	Create api to return list of orders used in current allocation                                            
 *	02/14/24	S Ramachandran		 CAP-47145	Integrated service to return list of orders used in current allocation
 */
package com.rrd.c1ux.api.controllers.singleitem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsResponse;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsResponse;
import com.rrd.c1ux.api.models.singleitem.mappers.SingleItemDetailsMapper;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.singleitem.SingleItemDetailAllocationsService;
import com.rrd.c1ux.api.services.singleitem.SingleItemDetailsService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("SingleItemDetailsController")
@Tag(name = "Single Item Details Controller")
public class SingleItemDetailsController extends BaseCPApiController {

    private static final Logger logger = LoggerFactory.getLogger(SingleItemDetailsController.class);
    private SingleItemDetailsMapper mSingleItemDetailsMapper;


    @Autowired
    SingleItemDetailsService mSingleItemDetailsService;
    
    @Autowired
    CatalogItemRetriveServices mCatalogItemRetriveServices;
    
    @Autowired
    private SingleItemDetailAllocationsService mSingleItemDetailAllocationsService;
    
    protected SingleItemDetailsController(
        TokenReader tokenReader, 
        CPSessionReader sessionReader,
        SingleItemDetailsMapper singleItemDetailsMapper,
        SingleItemDetailsService singleItemDetailsService,
        CatalogItemRetriveServices catalogItemRetriveServices,
        SingleItemDetailAllocationsService singleItemDetailAllocationsService) {

        super(tokenReader, sessionReader);
        mSingleItemDetailsMapper = singleItemDetailsMapper;
        mSingleItemDetailsService = singleItemDetailsService;
        mCatalogItemRetriveServices = catalogItemRetriveServices;
        mSingleItemDetailAllocationsService = singleItemDetailAllocationsService;
    }

    /*@Override*/
    protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
    }
    
    @PostMapping(value = RouteConstants.SINGLE_ITEM_DETAILS, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
        summary = "Get single item details for the user session in context")
    public SingleItemDetailsResponse getSingleItemDetails(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid, @RequestBody SingleItemDetailsRequest itemDetailsRequest ) throws AtWinXSException {

        logger.info("In getSingleItemDetails with request:" + itemDetailsRequest);

        SessionContainer sc = getSessionContainer( ttsessionid);

      SingleItemDetailsResponse itemDetails= mSingleItemDetailsService.retrieveSingleItemDetails(sc,ttsessionid,itemDetailsRequest,mCatalogItemRetriveServices);
       
        logger.info("About to return item details response:" + itemDetails);
       return mSingleItemDetailsMapper.getSingleItemDetails(itemDetails);
    }

    @PostMapping(value = RouteConstants.GET_SINGLE_ITEM_DETAIL_ALLOCATIONS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Get Item allocation details used in an Order")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    public  ResponseEntity<SingleItemDetailAllocationsResponse> getSingleItemDetailAllocations(
    	@RequestHeader(value = "ttsessionid", required=false) String ttsessionid, 
    	@RequestBody SingleItemDetailAllocationsRequest request ) throws AtWinXSException {

    	logger.info("In getSingleItemDetailAllocations()");
    	SessionContainer sc = getSessionContainer( ttsessionid);
    	
    	SingleItemDetailAllocationsResponse response = mSingleItemDetailAllocationsService.retrieveItemAllocations(sc, request);
    	
    	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

}
