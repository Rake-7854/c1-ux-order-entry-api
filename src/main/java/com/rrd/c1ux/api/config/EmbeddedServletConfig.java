/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/30/2023  C Porter        CAP-39073                   Address Invicti security issue for X-XSS-Protection header.  
 */

package com.rrd.c1ux.api.config;

import javax.sql.DataSource;
import org.apache.catalina.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rrd.c1ux.api.config.properties.Db2JndiProperties;
import com.rrd.c1ux.api.config.properties.DocSqlJndiProperties;

/**
 * This config class is intended to handle any servlet bootstrapping when the
 * embedded Tomcat servlet is used.
 * 
 * @implNote The CustomPoint code uses JNDI to get data sources. This needs to
 *           be setup here. See the following:
 *           https://stackoverflow.com/questions/24941829/how-to-create-jndi-context-in-spring-boot-with-embedded-tomcat-container
 */
@Configuration
public class EmbeddedServletConfig {

	@Bean
    public TomcatServletWebServerFactory tomcatFactory(Db2JndiProperties db2JndiConfig, DocSqlJndiProperties docSqlJndiConfig) {
		return new TomcatServletWebServerFactory() {

			@Override
			protected TomcatWebServer getTomcatWebServer(org.apache.catalina.startup.Tomcat tomcat) {			  
				tomcat.enableNaming();
				return super.getTomcatWebServer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context) {

				// DB2 context
				setDb2ContextResource(context);

				// Doc SQL context
				setDocSqlContextResource(context);
			}

			private void setDocSqlContextResource(Context context) {

				ContextResource docSqlResource = new ContextResource();
				docSqlResource.setName(docSqlJndiConfig.getName());
				docSqlResource.setProperty("factory", docSqlJndiConfig.getFactory());
				docSqlResource.setType(DataSource.class.getName());
				docSqlResource.setProperty("driverClassName", docSqlJndiConfig.getDriverClassName());
				docSqlResource.setProperty("url", docSqlJndiConfig.getUrl());
				docSqlResource.setProperty("username", docSqlJndiConfig.getUsername());
				docSqlResource.setProperty("password", docSqlJndiConfig.getPassword());
				if (StringUtils.isNotBlank(docSqlJndiConfig.getInitialSize())) {
					docSqlResource.setProperty("initialSize", docSqlJndiConfig.getInitialSize());
				}
				docSqlResource.setProperty("maxIdle", docSqlJndiConfig.getMaxIdle());
				docSqlResource.setProperty("maxTotal", docSqlJndiConfig.getMaxTotal());
				docSqlResource.setProperty("maxWait", docSqlJndiConfig.getMaxWait());
				docSqlResource.setProperty("minEvictableIdleTimeMillis",
						docSqlJndiConfig.getMinEvictableIdleTimeMillis());
				if (StringUtils.isNotBlank(docSqlJndiConfig.getMinIdle())) {
					docSqlResource.setProperty("minIdle", docSqlJndiConfig.getMinIdle());
				}
				context.getNamingResources().addResource(docSqlResource);
			}

			private void setDb2ContextResource(Context context) {

				ContextResource db2Resource = new ContextResource();
				db2Resource.setName(db2JndiConfig.getName());
				db2Resource.setProperty("factory", db2JndiConfig.getFactory());
				db2Resource.setType(DataSource.class.getName());
				db2Resource.setProperty("driverClassName", db2JndiConfig.getDriverClassName());
				db2Resource.setProperty("url", db2JndiConfig.getUrl());
				db2Resource.setProperty("username", db2JndiConfig.getUsername());
				db2Resource.setProperty("password", db2JndiConfig.getPassword());
				if (StringUtils.isNoneBlank(db2JndiConfig.getInitialSize())) {
					db2Resource.setProperty("initialSize", db2JndiConfig.getInitialSize());
				}
				db2Resource.setProperty("maxIdle", db2JndiConfig.getMaxIdle());
				db2Resource.setProperty("maxTotal", db2JndiConfig.getMaxTotal());
				db2Resource.setProperty("maxWait", db2JndiConfig.getMaxWait());
				db2Resource.setProperty("minEvictableIdleTimeMillis", db2JndiConfig.getMinEvictableIdleTimeMillis());
				if (StringUtils.isNotBlank(db2JndiConfig.getMinIdle())) {
					db2Resource.setProperty("minIdle", db2JndiConfig.getMinIdle());
				}
				context.getNamingResources().addResource(db2Resource);
			}
		};
	}
}
