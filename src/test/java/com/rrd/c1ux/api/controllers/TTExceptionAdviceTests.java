/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  03/22/23   C Porter        CAP-39295                   Handle "Could not load session" Exception
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.controllers;

import static com.rrd.c1ux.api.controllers.RouteConstants.GET_ITEM_ADD_TO_CART;
import static com.rrd.c1ux.api.controllers.RouteConstants.SUPPORT_PAGE_LOAD_INFO;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import com.rrd.c1ux.api.BaseMvcTest;
import com.rrd.c1ux.api.WithMockCustomUser;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.exceptions.AtWinXSSessionException;
import com.rrd.c1ux.api.exceptions.BadRequestException;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@WithMockCustomUser
class TTExceptionAdviceTests extends BaseMvcTest {

    private static final String EXCEPTION_RESPONSE_MESSAGE = "$.message";

    private static final String EXCEPTION_RESPONSE_TITLE = "$.title";

    
    @Test
    void that_AtWinXSSessionException_returns_NOT_AUTHORIZED() throws Exception {
        
        AtWinXSSessionException e = new  AtWinXSSessionException(new NullPointerException("invalid session container"), this.getClass().getName());
        
        given(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).willThrow(e);
        
        mockMvc.perform(post(SUPPORT_PAGE_LOAD_INFO))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith(TTExceptionAdvice.MSG_NO_SESSION)))
            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, startsWith(TTExceptionAdvice.MSG_NO_SESSION)));
    }
    
    @Test
    void that_HttpMessageNotReadableException_returns_BAD_REQUEST() throws Exception {
      
      String invalidJson = "{ \"itemQuantity\" : \"foo\" }";
      
      mockMvc.perform(post( "/" + GET_ITEM_ADD_TO_CART)
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("JSON parse error: Cannot deserialize")))
            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, startsWith("JSON parse error: Cannot deserialize")));
    }
    
    @Test
    void that_AtWinXSException_returns_BAD_REQUEST() throws Exception {
        
        AtWinXSException e = new AtWinXSException("foo", String.class.getName());
        
        given(mockSessionReader.getSessionContainer(nullable(String.class), anyInt())).willThrow(e);
        
        mockMvc.perform(post(SUPPORT_PAGE_LOAD_INFO))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, startsWith("foo")))
            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, emptyString()));
        
    }
    
    @Test
    void that_BadRequestException_returns_BAD_REQUEST() throws Exception {
        
        BadRequestException e = new BadRequestException("foo");
        
        given(mockSessionReader.getSessionContainer(any(), anyInt())).willThrow(e);
        
        mockMvc.perform(get(RouteConstants.CATALOG_MENU_FOR_PRIME))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, is("foo")))
            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, is("foo")));
    }
    
    @Test
    void that_AccessForbiddenException_returns_FORBIDDEN() throws Exception {
      
      AccessForbiddenException e = new AccessForbiddenException("foo", "class");
      
      given(mockSessionReader.getSessionContainer(any(), anyInt())).willThrow(e);
      
      mockMvc.perform(get(RouteConstants.CATALOG_MENU_FOR_PRIME))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, is("foo")))
          .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, is("foo")));
      
    }
    
    @Test
    void that_NullPointerException_returns_INTERNAL_SERVER_ERROR() throws Exception {
        
        NullPointerException e = new NullPointerException("foo");
        
        given(mockSessionReader.getSessionContainer(any(), anyInt())).willThrow(e);
        
        mockMvc.perform(get(RouteConstants.CATALOG_MENU_FOR_PRIME))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath(EXCEPTION_RESPONSE_TITLE, is("foo")))
            .andExpect(jsonPath(EXCEPTION_RESPONSE_MESSAGE, is("foo")));
        
    }

}
