/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  03/14/23   C Porter        CAP-37146                   Spring Session
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 *	08/29/23	L De Leon		CAP-43197					Modified getSingleItemResponse() method to add new field in response object
 *	09/07/23	Krishna Natarajan		CAP-43656			added a new parameter to the existing method
 *	02/14/24	S Ramachandran	CAP-47145					added test for SingleItemDetailAllocations API
 *	06/05/24    S Ramachandran  CAP-49887   				added value for componentItems parameter in SingleItemDetailsResponse 
 */
package com.rrd.c1ux.api.controllers.singleitem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsResponse;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsResponse;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;



@WithMockUser
class SingleItemDetailsControllerTests extends BaseMvcTest  {
	
	
    private static final boolean TEST_SHOW_ADD_TO_CART_BUTTON = false;   
    private static final boolean TEST_SHOW_DISPLAY_UOM_OPTIONS = false;  
    private static final String TEST_ITEM_QUANTITY="6";
    private static final String TEST_ITEM_EDOC_URL="edoc.jpg";
    
    //CAP-47145
    private static final String TEST_CUSTOMER_ITEM_NUMBER="14444";
    private static final String TEST_VENDOR_ITEM_NUMBER="255105";
    String TEST_ENCRYPTED_SESSIONID;
    
    private SingleItemDetailsResponse voItemDetailsResponse = getSingleItemResponse();  
    
    //CAP-47145
    private SingleItemDetailAllocationsResponse singleItemDetailAllocationsResponseSuccess; 
  	private SingleItemDetailAllocationsResponse singleItemDetailAllocationsResponseFailed;
  	private SingleItemDetailAllocationsRequest singleItemDetailAllocationsRequestValid;
    
   
    @BeforeEach
    void setUp() throws Exception {
        
    	when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);
    	when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    	when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
        doReturn("").when(mockAppSessionBean). getEncodedSessionId();
        
        //CAP-47145
        singleItemDetailAllocationsResponseSuccess = getSingleItemDetailAllocationsResponseSuccess();
        singleItemDetailAllocationsResponseFailed = getSingleItemDetailAllocationsResponseFailed();
        singleItemDetailAllocationsRequestValid = getSingleItemDetailAllocationsRequestValid();
        TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
       
    }
    
    @Test
    void that_getProfile_returnsExpected(
        ) throws Exception, AtWinXSException {

        // given that retrieveSingleItemDetails returns a valid ItemDetailsResponse value object
        when(mSingleItemDetailsService.retrieveSingleItemDetails(any(SessionContainer.class), any(), any(SingleItemDetailsRequest.class), any(CatalogItemRetriveServices.class))).thenReturn(voItemDetailsResponse);
        when(mockSingleItemDetailsMapper.getSingleItemDetails(voItemDetailsResponse)).thenReturn(voItemDetailsResponse);

		String TEST_ENCRIPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
        
        // when getDetail is called, expect 200 status and item numbers in JSON
        mockMvc.perform(
            MockMvcRequestBuilders.post(RouteConstants.SINGLE_ITEM_DETAILS)
            .accept(MediaType.APPLICATION_JSON)
            .header("ttsessionid",TEST_ENCRIPTED_SESSIONID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ \"itemNumber\": \"22279\",  \"vendorItemNumber\": \"9022279\",  \"catalogLnNbr\": -2}\n")
            )
        	.andExpect(MockMvcResultMatchers.status().isOk())
           
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.showAddToCartButton").value(TEST_SHOW_ADD_TO_CART_BUTTON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.displayUOMOptions").value(TEST_SHOW_DISPLAY_UOM_OPTIONS));
        
    }
    
    //CAP-47145
    @Test
    void that_getSingleItemDetailAllocations_returnsExpected() throws Exception {

        // given that retrieveRoutingInformation returns a valid SingleItemDetailAllocationsResponse
    	when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSingleItemDetailAllocationsService.retrieveItemAllocations(any(SessionContainer.class), any(SingleItemDetailAllocationsRequest.class)))
			.thenReturn(singleItemDetailAllocationsResponseSuccess);
        
        // when retrieveRoutingInformation is called, expect 200 status and SingleItemDetailAllocationsResponse
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owc1UserSiteAttributesRequest_Valid = ow.writeValueAsString(singleItemDetailAllocationsRequestValid);

        mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_SINGLE_ITEM_DETAIL_ALLOCATIONS)
        	.accept(MediaType.APPLICATION_JSON)
			.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
			.content(owc1UserSiteAttributesRequest_Valid).characterEncoding("utf-8"))
        	.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }
    
    //CAP-47145
  	@Test
  	void that_getSingleItemDetailAllocations_returnFailed() throws Exception {

  		// given that retrieveRoutingInformation returns 422 
    	when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockSingleItemDetailAllocationsService.retrieveItemAllocations(any(SessionContainer.class), any(SingleItemDetailAllocationsRequest.class)))
			.thenReturn(singleItemDetailAllocationsResponseFailed);
  		
		// when retrieveRoutingInformation is called, expect 422 status
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String owc1UserSiteAttributesRequest_Valid = ow.writeValueAsString(singleItemDetailAllocationsRequestValid);
        
        mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_SINGLE_ITEM_DETAIL_ALLOCATIONS)
        	.accept(MediaType.APPLICATION_JSON)
  			.header("ttsessionid", TEST_ENCRYPTED_SESSIONID).contentType(MediaType.APPLICATION_JSON)
  			.content(owc1UserSiteAttributesRequest_Valid).characterEncoding("utf-8"))
  			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
  			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
  			.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
  	}
    
    private SingleItemDetailsResponse getSingleItemResponse()  {
    		//CAP-35080
    	//CAP-43656 added the allowFavorites parameter
    		return new SingleItemDetailsResponse(null, null, false, false, false, false, false, false, false, TEST_ITEM_EDOC_URL, TEST_ITEM_QUANTITY, null, 0, 0, 0, "", false, false,"", null, null, null, null, null,null, true,null,null, null, null,null,0,0,0,0,null,null,null);
    	
    }
    
    //CAP-47145
  	private SingleItemDetailAllocationsResponse getSingleItemDetailAllocationsResponseSuccess() {
  		
  		SingleItemDetailAllocationsResponse singleItemDetailAllocationsResponse = new SingleItemDetailAllocationsResponse();
  		singleItemDetailAllocationsResponse.setSuccess(true);
  		return singleItemDetailAllocationsResponse;
  	}
  	
  	//CAP-47145
  	private SingleItemDetailAllocationsResponse getSingleItemDetailAllocationsResponseFailed() {
  		
  		SingleItemDetailAllocationsResponse singleItemDetailAllocationsResponse = new SingleItemDetailAllocationsResponse();
  		singleItemDetailAllocationsResponse.setSuccess(false);
  		return singleItemDetailAllocationsResponse;
  	}
  	
  	//CAP-47145
  	private SingleItemDetailAllocationsRequest getSingleItemDetailAllocationsRequestValid() {
  		
  		SingleItemDetailAllocationsRequest singleItemDetailAllocationsRequest = new SingleItemDetailAllocationsRequest(TEST_CUSTOMER_ITEM_NUMBER, TEST_VENDOR_ITEM_NUMBER);
  		return singleItemDetailAllocationsRequest;
  	}
}
