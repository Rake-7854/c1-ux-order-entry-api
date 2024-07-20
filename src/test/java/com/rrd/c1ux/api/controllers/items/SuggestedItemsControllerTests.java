/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#										Description
 * 	--------	-----------		----------------------------------------	------------------------------
 *	06/05/2024	Sakthi M		CAP-49782	               					 Initial creation
 *
 */

package com.rrd.c1ux.api.controllers.items;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.SuggestedItemsRequest;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

@WithMockUser
class SuggestedItemsControllerTests extends BaseMvcTest {
	String TEST_ENCRYPTED_SESSIONID;
	
	private CatalogItemsResponse catalogItemsResponseSuccess;
	private CatalogItemsResponse catalogItemsResponseFailed;
	
	 @BeforeEach
	 void setup() throws Exception {
		    setupBaseMockSessions();
		    when(mockSessionReader.getSessionContainer(any(), anyInt())).thenReturn(mockSessionContainer);
			TEST_ENCRYPTED_SESSIONID = mockAppSessionBean.getEncodedSessionId();
			catalogItemsResponseSuccess=getCatalogItemsResponseSuccess();
			catalogItemsResponseFailed=getCatalogItemsResponseFailedTest();
       }
	 
	 
	 @Test
	 void that_getsuggesteditemorderlevel_expected() throws Exception {

			when(mockSuggestedItemsService.getSuggestedItems(any(), any(), any(), any(),
					anyBoolean())).thenReturn(catalogItemsResponseSuccess);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(suggestedItemRequestTest());

			// when getSearchordersDetail is called, expect 200 status
			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_SUGGESTED_ITEMS)
					.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		}
	 
	 
	 
	 @Test
	 void that_getsuggesteditem_failed() throws Exception {

			when(mockSuggestedItemsService.getSuggestedItems(any(SessionContainer.class),
					any(),any(),any(),anyBoolean())).thenReturn(catalogItemsResponseFailed);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(suggestedItemRequestTest());

			// when getSearchordersDetail is called, expect 400 status
			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_SUGGESTED_ITEMS)
					.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
					.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
		}
	 
	 
	 @Test
	 void that_getsuggesteditem_itemlevel_expected() throws Exception {

			when(mockSuggestedItemsService.getSuggestedItems(any(), any(), any(), any(),
					anyBoolean())).thenReturn(catalogItemsResponseSuccess);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestString = ow.writeValueAsString(suggestedItemRequestwithvalueTest());

			// when getSearchordersDetail is called, expect 200 status
			mockMvc.perform(MockMvcRequestBuilders.post(RouteConstants.GET_SUGGESTED_ITEMS)
					.accept(MediaType.APPLICATION_JSON).header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
					.contentType(MediaType.APPLICATION_JSON).content(requestString).characterEncoding("utf-8"))
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
					.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(AtWinXSConstant.EMPTY_STRING));
		}
	 	
	 	
	 	private CatalogItemsResponse getCatalogItemsResponseSuccess() {
			
			catalogItemsResponseSuccess = new CatalogItemsResponse();
			catalogItemsResponseSuccess.setSuccess(true);
			catalogItemsResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
			return catalogItemsResponseSuccess;
		}
		
		//CAP-47841
		private CatalogItemsResponse getCatalogItemsResponseFailedTest() {
			
			catalogItemsResponseFailed = new CatalogItemsResponse();
			catalogItemsResponseFailed.setSuccess(false);
			return catalogItemsResponseFailed;
		}
		
		private SuggestedItemsRequest suggestedItemRequestTest() {
			SuggestedItemsRequest req = new SuggestedItemsRequest();
			req.setItemNumber("");
			req.setVendorItemNumber("");
			req.setOrderLineNumber("");
			return req;
		}
	
		private SuggestedItemsRequest suggestedItemRequestwithvalueTest() {
			SuggestedItemsRequest req = new SuggestedItemsRequest();
			req.setItemNumber("123");
			req.setVendorItemNumber("112");
			req.setOrderLineNumber("333");
			return req;
		}

	 	

}
