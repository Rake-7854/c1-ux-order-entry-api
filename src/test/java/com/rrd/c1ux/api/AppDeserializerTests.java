/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  05/12/2023  C Porter        CAP-39738                   Update Spring Boot and Tomcat versions   
 */

package com.rrd.c1ux.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InvalidClassException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.core.ConfigurableObjectInputStream;
import com.pingidentity.opentoken.util.Base64.InputStream;
import com.rrd.c1ux.api.config.AppDeserializer;

class AppDeserializerTests {

  @Mock
  private InputStream mockInputStream;

  private AppDeserializer serviceToTest = new AppDeserializer();

  @Test
  void that_deserialize_returns_object() throws Exception {

    try (MockedConstruction<ConfigurableObjectInputStream> mocked =
        Mockito.mockConstruction(ConfigurableObjectInputStream.class, (mock, context) -> {
          when(mock.readObject()).thenReturn(new Object());
        })) {

      assertNotNull(serviceToTest.deserialize(mockInputStream));

    }

  }

  @Test
  void that_deserialize_returns_null() throws Exception {

    try (MockedConstruction<ConfigurableObjectInputStream> mocked =
        Mockito.mockConstruction(ConfigurableObjectInputStream.class, (mock, context) -> {
          when(mock.readObject()).thenThrow(new InvalidClassException("reason"));
        })) {

      assertNull(serviceToTest.deserialize(mockInputStream));

    }

  }

  @Test
  void that_deserialize_throws_ioexception() throws Exception {

    try (MockedConstruction<ConfigurableObjectInputStream> mocked =
        Mockito.mockConstruction(ConfigurableObjectInputStream.class, (mock, context) -> {
          when(mock.readObject()).thenThrow(new ClassNotFoundException());
        })) {

      assertThrows(IOException.class, () -> serviceToTest.deserialize(mockInputStream));

    }

  }

}
