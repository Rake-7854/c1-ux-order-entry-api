/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.controllers.catalog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.rrd.c1ux.api.models.catalog.CatalogRequest;
import com.rrd.c1ux.api.models.catalog.CatalogTreeResponse;
import com.rrd.custompoint.gwt.catalog.entity.CatalogTree;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockUser
class CatalogMenuControllerTests extends BaseMvcTest{
	
    private CatalogRequest request;
  
	private CatalogTreeResponse mCatalogMenu;
	
	@BeforeEach
	void setUp() throws Exception {

	    request = new CatalogRequest("123", "foo", true);
	    mCatalogMenu = getCatalogMenuTest();
	    
		when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		when(mockSessionReader.getSessionContainer(anyString(), anyInt())).thenReturn(mockSessionContainer);// mandy
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getEncodedSessionId()).thenReturn("");
	}
	
	@Test
	void that_getCatalogMenu_returnsExpected() throws Exception, AtWinXSException {

		String TEST_ENCRYPTED_SESSIONID =  mockAppSessionBean.getEncodedSessionId();
		
		when(mockCatalogMenuService.retrieveCatalogMenuDetails(any(SessionContainer.class), any(CatalogRequest.class)))
				.thenReturn(mCatalogMenu);
		when(mockCatalogMenuMapper.getCatalogMenu(mCatalogMenu)).thenReturn(mCatalogMenu);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String catalogRequestString = ow.writeValueAsString(request);
		
		// when getDetail is called, expect 200 status and item numbers in JSON
		mockMvc.perform(
				MockMvcRequestBuilders.post(RouteConstants.CATALOG_MENU)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", TEST_ENCRYPTED_SESSIONID)
				.contentType(MediaType.APPLICATION_JSON).content(catalogRequestString)
				.characterEncoding("utf-8")
				)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.categoryTreeBranch.nodeName").value("AEM Items"))
			    .andExpect(MockMvcResultMatchers.jsonPath("$.categoryTreeBranch.nodeType").value("CATEGORY"));

	}
	
	private CatalogTreeResponse getCatalogMenuTest() {
		CatalogTreeResponse resData=new CatalogTreeResponse();
		CatalogTree tree = new CatalogTree();
		tree.setNodeName("AEM Items");
		tree.setNodeType("CATEGORY");
		resData.setCategoryTreeBranch(tree);
		return resData; 
	}

}

