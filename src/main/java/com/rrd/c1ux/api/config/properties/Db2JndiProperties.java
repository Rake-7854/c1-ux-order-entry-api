package com.rrd.c1ux.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "connect-one-api.db2-jndi")
public class Db2JndiProperties {
	
	private String name;
	private String factory;
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private String initialSize;
	private String maxIdle;
	private String maxTotal;
	private String maxWait;
	private String minEvictableIdleTimeMillis;
	private String minIdle;
}
