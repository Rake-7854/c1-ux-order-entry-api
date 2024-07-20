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

import java.lang.reflect.Field;
import java.util.List;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.util.ReflectionUtils;

/*
 * Spring bean post-processor to convert a HeaderWriterFilter to ErrorDispatchHeaderWriterFilter
 */
public class HeaderWriterFilterPostProcessor implements ObjectPostProcessor<HeaderWriterFilter> {

  @Override
  @SuppressWarnings("unchecked")
  public <O extends HeaderWriterFilter> O postProcess(O headerWriterFilter) {

    try {
      Field headerWritersField = ReflectionUtils.findField(HeaderWriterFilter.class, "headerWriters");
      if (headerWritersField == null) {
        throw new IllegalStateException("headerWritersField cannot be null");
      }
      ReflectionUtils.makeAccessible(headerWritersField);
      List<HeaderWriter> headerWriters = (List<HeaderWriter>) ReflectionUtils.getField(headerWritersField, headerWriterFilter);
      
      return (O) new ErrorDispatchHeaderWriterFilter(headerWriters);
    } catch (Exception e) {
      return headerWriterFilter;
    }
    
  }

}
