/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  10/18/2023  C Porter        CAP-44260                   Allow for custom Content Security Policies by site
 */

package com.rrd.c1ux.api.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.header.HeaderWriter;

public class PropertyHeaderWriter implements HeaderWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHeaderWriter.class);

	private final HeaderResolver headerResolver;

	public PropertyHeaderWriter(HeaderResolver headerResolver) {		
		this.headerResolver = headerResolver;
	}

	@Override
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {

		try {
			
			headerResolver.resolve(request).ifPresent(h -> response.setHeader(h.getKey(), h.getValue()));

		} catch (Exception ex) {
			
			LOGGER.error("unable to read property for header", ex);
			
		}

	}

}
