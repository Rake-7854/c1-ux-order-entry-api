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
package com.rrd.c1ux.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

@ExtendWith(MockitoExtension.class)
class C1UXDefaultActiveProfilesResolverTest {

  @Test
  void test_when_no_active_profiles_that_LOCAL_is_assigned() {

    try (MockedConstruction<DefaultActiveProfilesResolver> mockedResolver =
        Mockito.mockConstruction(DefaultActiveProfilesResolver.class, (mock, context) -> {
          
          when(mock.resolve(any())).thenReturn(new String[] {});
          
        })) {

      C1UXDefaultActiveProfilesResolver testSubject = new C1UXDefaultActiveProfilesResolver();

      String[] activeProfiles = testSubject.resolve(Object.class);

      Assertions.assertArrayEquals(new String[] {"LOCAL"}, activeProfiles);
    }

  }

  @Test
  void test_when_active_profiles_that_LOCAL_is_not_assigned() {

    try (MockedConstruction<DefaultActiveProfilesResolver> mockedResolver =
        Mockito.mockConstruction(DefaultActiveProfilesResolver.class, (mock, context) -> {
          
          when(mock.resolve(any())).thenReturn(new String[] {"PROD"});
          
        })) {

      C1UXDefaultActiveProfilesResolver testSubject = new C1UXDefaultActiveProfilesResolver();

      String[] activeProfiles = testSubject.resolve(Object.class);

      Assertions.assertArrayEquals(new String[] {"PROD"}, activeProfiles);
    }

  }

}
