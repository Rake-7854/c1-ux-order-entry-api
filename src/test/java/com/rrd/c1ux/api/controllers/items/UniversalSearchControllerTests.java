/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  06/08/22    S Ramachandran  CAP-34498   Added unit test cases for Universal Search Controller
 *  10/03/22	A Boomker		CAP-35542	Change response object
 *  03/07/23    C Porter        CAP-38715   Refactor
 *  05/31/23    C Porter        CAP-40530   JUnit cleanup  
 *
 */

package com.rrd.c1ux.api.controllers.items;

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
import com.rrd.c1ux.api.models.items.UniversalSearch;
import com.rrd.c1ux.api.models.items.UniversalSearchRequest;
import com.rrd.c1ux.api.models.items.UniversalSearchResponse;
import com.rrd.c1ux.api.models.users.UserContext;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;



	@WithMockUser
	class UniversalSearchControllerTests extends BaseMvcTest {

	    private static final String TEST_USER_STATUS = "Success";
	    private static final String TEST_DESCRIPTION = "Bundle test item";
	    private static final String TEST_ITEM_NUM = "LAVA ITEM";
	    private static final String TEST_WCS_ITEM_NUM = "wcsItemNum";
	    private static final String TEST_CAT_LINE_NUM = "311872";
	    private static final String TEST_IMAGE_URL = "/images/global/NoImageAvailable.png";
	    private static final String TEST_DEFAULT_QTY = "1";
	  //  private static final String TEST_ADDTOCART_ANCHORANDICON = "<a href='javascript: ir_addToCart(311872, \"LAVA ITEM\", \"\", \"Bundle test item\", \"errorMsgUniversalSearch\", true, false)' id='311872_imageAnchor' title='Customize'><img src='/cp/images/icons/png/Icon_CartCustomize.png' class='icon' id='311872_image'></a>";
	    private static final String TEST_ICONPLUS_UDF = "";

	    private UniversalSearchResponse testUniversalSearchResponse = getTestUniversalSearchResponse();


	    @BeforeEach
	    void setUp() throws Exception {

	        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
	        when(mockSessionReader.getUserContext()).thenReturn(new UserContext());
	        when(mockSessionContainer.getApplicationSession()).thenReturn(new ApplicationSession());

	        when(mockSessionReader.getSessionContainer(anyString(),anyInt())).thenReturn(mockSessionContainer);
	        doReturn("").when(mockAppSessionBean).getEncodedSessionId();

	    }


	    @Test
	    void getUniversalSearch_returnsExpected(
	        ) throws Exception, AtWinXSException {

	    	//get encrypted Session Id from mock AppSessionBean
	    	String TEST_ENCRYPTED_SESSION_ID = mockAppSessionBean.getEncodedSessionId();

	    	//given processUniversalSearch returns a valid object
	    	when(mockUniversalSearchService.processUniversalSearch(any(SessionContainer.class),
	    			any(),any(UniversalSearchRequest.class))).thenReturn(testUniversalSearchResponse);

    		UniversalSearchRequest  tempUniversalSearchRequest =  new UniversalSearchRequest();
    		tempUniversalSearchRequest.setTerm(TEST_ITEM_NUM);

    		ObjectMapper mapper = new ObjectMapper();
    	    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    	    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
    	    String requestJsonUniversalSearch=ow.writeValueAsString(tempUniversalSearchRequest);

    	    // when getUniversalSearch is called, expect UniversalSearchResponse - valid request
			mockMvc.perform(
		        MockMvcRequestBuilders.post(RouteConstants.ITEMS_UNIVERSAL_SEARCH)
			        .header("sessionID",TEST_ENCRYPTED_SESSION_ID)
					.contentType(MediaType.APPLICATION_JSON).content(requestJsonUniversalSearch))
			        .andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			    	.andExpect(MockMvcResultMatchers.jsonPath("$.status").value( TEST_USER_STATUS))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].description").value(TEST_DESCRIPTION))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].itemNum").value(TEST_ITEM_NUM))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].wcsItemNum").value(TEST_WCS_ITEM_NUM))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].catLineNum").value(TEST_CAT_LINE_NUM))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].imageUrl").value(TEST_IMAGE_URL))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].defaultQty").value(TEST_DEFAULT_QTY))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].addToCartAllowed").value("Y"))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].itemInCart").value("N"))
					.andExpect(MockMvcResultMatchers.jsonPath("$.lineItemsVO[0].iconPlusUDF").value(TEST_ICONPLUS_UDF));
	    }

	    public static String asJsonString(final Object obj) {
	        try {
	            return new ObjectMapper().writeValueAsString(obj);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }


	    private static UniversalSearchResponse getTestUniversalSearchResponse() {

	    	UniversalSearchResponse  testUniversalSearchResponse= new UniversalSearchResponse();
	    	testUniversalSearchResponse.setStatus("Success");

	    	UniversalSearch[] arrLineItemsVO = new UniversalSearch[1];
	        arrLineItemsVO[0] = new UniversalSearch();
	        arrLineItemsVO[0].setDescription("Bundle test item");
	        arrLineItemsVO[0].setItemNum("LAVA ITEM");
	        arrLineItemsVO[0].setWcsItemNum("wcsItemNum");
	        arrLineItemsVO[0].setCatLineNum("311872");
	        arrLineItemsVO[0].setImageUrl("/images/global/NoImageAvailable.png");
	        arrLineItemsVO[0].setDefaultQty("1");
	        arrLineItemsVO[0].setAddToCartAllowed("Y");
	        arrLineItemsVO[0].setItemInCart("N");
	        arrLineItemsVO[0].setIconPlusUDF("");
	    	testUniversalSearchResponse.setLineItemsVO(arrLineItemsVO);

	        return testUniversalSearchResponse;
	    }
}
