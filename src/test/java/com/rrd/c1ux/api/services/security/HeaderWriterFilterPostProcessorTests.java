/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  07/18/2023  C Porter        CAP-39073                   Address Invicti security issue for X-XSS-Protection header.  
 */

package com.rrd.c1ux.api.services.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.HstsHeaderWriter;
import org.springframework.util.ReflectionUtils;

@ExtendWith(MockitoExtension.class)
class HeaderWriterFilterPostProcessorTests {

  private HeaderWriterFilter filter;
  
  private HeaderWriterFilterPostProcessor serviceToTest;
  
  @BeforeEach
  void setup() {
    
    serviceToTest = new HeaderWriterFilterPostProcessor();
    filter = new HeaderWriterFilter(List.of(new HstsHeaderWriter()));
    
  }
  
  @Test
  void that_postProcess_converts_to_ErrorDispatchHeaderWriterFilter() {
        
    assertEquals(ErrorDispatchHeaderWriterFilter.class, serviceToTest.postProcess(filter).getClass());
    
  }
  
  @Test
  void that_fieldNotFound_converts_to_original_object() {
    
    try (MockedStatic<ReflectionUtils> mockedStatic = Mockito.mockStatic(ReflectionUtils.class)) {
     
      mockedStatic.when(() -> ReflectionUtils.findField(eq(HeaderWriterFilter.class), anyString())).thenReturn(null);
      
      assertSame(filter, serviceToTest.postProcess(filter));
      
    }
    
  }
  
}
