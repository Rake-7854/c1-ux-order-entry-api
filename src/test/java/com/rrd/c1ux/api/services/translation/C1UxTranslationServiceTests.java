/*
 * Copyright (c) RR Donnelley. All Rights Reserved. This software is the confidential and
 * proprietary information of RR Donnelley. You shall not disclose such confidential information.
 *
 * Revision Date Modified By JIRA# Description ---------- ----------- ----------
 * ----------------------------------------- 03/20/2023 C Porter CP-39295 Handle
 * "Could not load session" Exception
 */
package com.rrd.c1ux.api.services.translation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.C1UXResourceBundleMessageSource;
import com.wallace.atwinxs.framework.util.AppSessionBean;

public class C1UxTranslationServiceTests extends BaseServiceTest {

  @Mock
  private C1UXResourceBundleMessageSource mockMessageSource;
  
  private Properties props = new Properties();
  
  private C1UXTranslationService serviceToTest;

  public static final int DEVTEST_SITE_ID = 4366;
  public static final int DEVTEST_UX_BU_ID = 7125;
  public static final String DEFAULT_UG_NM = "C1UX";
  public static final String DEFAULT_VIEW = "orderSearch";

  @BeforeEach
  void setUp() throws Exception {
    serviceToTest = new C1UXTranslationService();
    ReflectionTestUtils.setField(serviceToTest, "translationBasenameRoot", "/root/");
    ReflectionTestUtils.setField(serviceToTest, "translationResourceBundles", Map.of("sf", mockMessageSource));
  }

  @Test
  void test_getResourceBundle_returnsBundle() throws Exception {

      when(mockMessageSource.getMergedBasenameProperties(anyString(), anyString(), any(AppSessionBean.class))).thenReturn(props);

      Properties result = serviceToTest.getResourceBundle(mockAppSessionBean, DEFAULT_VIEW);
      Assertions.assertNotNull(result);

  }

}
