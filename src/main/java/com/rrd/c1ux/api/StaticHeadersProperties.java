/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  09/21/2023  C Porter                                    All for per-environment configuration of static HTTP repsonse headers
 */

package com.rrd.c1ux.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Reads the application.yml, and collects properties under a specific key
 */
@Component
@ConfigurationProperties(prefix = "spring.security")
@Getter
@Setter
@NoArgsConstructor
public class StaticHeadersProperties {

	/*
	 * given prefix "spring.security", pulls all properties under the key
	 * "spring.security.static-headers" in the application.yml file
	 */
	private Map<String, String> staticHeaders = new HashMap<>();

	public List<Header> asHeaders() {
		return staticHeaders.entrySet().stream().map(e -> new Header(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}
}
