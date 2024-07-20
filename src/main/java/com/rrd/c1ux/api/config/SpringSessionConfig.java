/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/14/23    C Porter        CAP-37146                   Spring Session
 *  05/12/2023  C Porter        CAP-39738                   Update Spring Boot and Tomcat versions
 */
package com.rrd.c1ux.api.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.transaction.PlatformTransactionManager;
import com.rrd.c1ux.api.config.properties.Db2JndiProperties;

@Configuration
@EnableJdbcHttpSession(tableName = "SF_SPRING_SESSION")
public class SpringSessionConfig {

  @Bean
  public DataSource db2DataSource(Db2JndiProperties db2JndiProperties) {
    return  new JndiDataSourceLookup().getDataSource(db2JndiProperties.getName());
  }
  
  @Bean
  public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
  
  @Bean  
  public ConversionService  springSessionConversionService() {
    
    GenericConversionService converter = new GenericConversionService();
    
    converter.addConverter(Object.class, byte[].class, new SerializingConverter());
    converter.addConverter(byte[].class, Object.class, new DeserializingConverter(new AppDeserializer()));
    
    return converter;
  }
  
}
