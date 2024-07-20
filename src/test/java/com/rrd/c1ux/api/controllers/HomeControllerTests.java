/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  05/31/23   C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.WithMockCustomUser;

@WithMockCustomUser
class HomeControllerTests extends BaseMvcTest {

    @Test
    void that_index_returnsRedirectToIndexHtml() throws Exception {
        
        // when getDetail is called, expect 302 status and item numbers in JSON
        mockMvc.perform(
            MockMvcRequestBuilders.get(RouteConstants.HOME)
                .accept(MediaType.ALL))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/index.html"));
    }
}
