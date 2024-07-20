/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  04/11/2023  C Porter        CAP-36673                   Invicti Scan - OPTIONS method enabled
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */

package com.rrd.c1ux.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.TRACE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import com.rrd.c1ux.api.BaseMvcTest;

class HttpMethodControllerTest extends BaseMvcTest {

  @Test
  void that_GET_allowedByFirewall() throws Exception {

    mockMvc.perform(get("/")).andExpect(status().is3xxRedirection());

  }

  @Test
  void that_disallowed_methods_not_allowed_by_firewall() throws Exception {

    for (String method : List.of("CONNECT", OPTIONS.name(), TRACE.name())) {
      MvcResult result = mockMvc.perform(request(method, new URI("/favicon.ico"))).andReturn();
      assertEquals(BAD_REQUEST.value(), result.getResponse().getStatus(), String.format("HTTP Method: %s;", method));
    }

  }

}
