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

import java.util.List;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.HeaderWriterFilter;

public class ErrorDispatchHeaderWriterFilter extends HeaderWriterFilter {
  
  public ErrorDispatchHeaderWriterFilter(List<HeaderWriter> headerWriters) {
    super(headerWriters);
  } 
  
  /*
   * override superclass so that errored requests have security headers written
   */
  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }
}
