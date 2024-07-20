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

package com.rrd.c1ux.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.Nullable;

/*
 * This class is based off of DefaultDeserializer
 * 
 * spring-security does not support serialization of the SecurityContextImpl between 
 * different versions, making upgrades of spring-security difficult. Rather than throwing
 * an exception when it fails to deserialize a class, this class returns null instead.
 * 
 * https://github.com/spring-projects/spring-session/issues/1924
 * https://github.com/spring-projects/spring-security/issues/9204#issuecomment-738853204
 * https://github.com/spring-projects/spring-session/pull/2099 
 */
public class AppDeserializer implements Deserializer<Object> {

  @Nullable
  private final ClassLoader classLoader;

  public AppDeserializer() {
    this.classLoader = null;
  }

  public AppDeserializer(@Nullable ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public Object deserialize(InputStream inputStream) throws IOException {
    ObjectInputStream objectInputStream = new ConfigurableObjectInputStream(inputStream, this.classLoader);
    try {      
      return objectInputStream.readObject(); 
    } catch (ClassNotFoundException ex) {
      throw new IOException("Class not found", ex);
    } catch (InvalidClassException ex) {
      // serialized class version mismatch, don't return anything
      return null;
    }
  }

}
