/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 */
package com.rrd.c1ux.api;

import org.bouncycastle.util.Arrays;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

/**
 * Sets the default spring profile to LOCAL, if no profile is set. This is particularly useful for
 * running JUnit tests in the local workspace.
 */
public class C1UXDefaultActiveProfilesResolver implements ActiveProfilesResolver {

  private final DefaultActiveProfilesResolver defaultActiveProfileResolver = new DefaultActiveProfilesResolver();

  @Override
  public String[] resolve(Class<?> testClass) {

    String[] profiles = defaultActiveProfileResolver.resolve(testClass);

    if (profiles.length == 0) {
      profiles = Arrays.append(profiles, "LOCAL");
    }

    return profiles;
  }

}
