/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date                    Modified By             JIRA#                       Description
 *  --------                -----------             -----------------------     --------------------------------
 *  02/24/23                C Porter                CAP-38897                   HTTP 403 response handling
 *  03/29/23				A Boomker				CAP-39510					Adding constructor that just takes classname for default message
 */
package com.rrd.c1ux.api.exceptions;

import com.wallace.atwinxs.framework.util.AtWinXSException;

public class AccessForbiddenException extends AtWinXSException {

  private static final long serialVersionUID = 7132868714713244616L;

  public AccessForbiddenException(String msg, String className) {
    super(msg, className);
  }

  // CAP-39510 - adding a standardized constructor
  public AccessForbiddenException(String className)
  {
	  super("Access to this service is not allowed", className);
  }
}
