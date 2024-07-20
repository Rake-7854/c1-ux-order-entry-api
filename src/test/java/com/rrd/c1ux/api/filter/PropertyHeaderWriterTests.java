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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.filter.HeaderResolver;
import com.rrd.c1ux.api.filter.PropertyHeaderWriter;
import com.rrd.custompoint.services.vo.KeyValuePair;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.XSProperties;

@ExtendWith(MockitoExtension.class)
class PropertyHeaderWriterTests {

	private static final String CSP = "default 'unsafe-inline' *";
	private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

	@Mock
	private XSProperties mockProperties;
	
	@Mock
	private HeaderResolver mockHeaderResolver;

	@InjectMocks
	private PropertyHeaderWriter testSubject;

	private MockHttpServletRequest mockRequest;

	private MockHttpServletResponse mockResponse;

	@BeforeEach
	void setup() throws Exception {
		mockRequest = new MockHttpServletRequest();
		mockResponse = new MockHttpServletResponse();
	}

	@Test
	void that_header_in_response() throws Exception {
		
		when(mockHeaderResolver.resolve(any()))
				.thenReturn(Optional.of(new KeyValuePair(CONTENT_SECURITY_POLICY, CSP)));

		testSubject.writeHeaders(mockRequest, mockResponse);

		assertTrue(mockResponse.containsHeader(CONTENT_SECURITY_POLICY));
	}

	@Test
	void that_no_header_in_response() throws Exception {

		when(mockHeaderResolver.resolve(any()))
				.thenReturn(Optional.empty());

		testSubject.writeHeaders(mockRequest, mockResponse);

		assertFalse(mockResponse.containsHeader(CONTENT_SECURITY_POLICY));
	}
	
	@Test
	void that_exception_thrown() throws Exception {
		
		when(mockHeaderResolver.resolve(any())).thenThrow(new AtWinXSException("msg", "classname"));
		
		testSubject.writeHeaders(mockRequest, mockResponse);
		
		assertFalse(mockResponse.containsHeader(CONTENT_SECURITY_POLICY));
	}

}
