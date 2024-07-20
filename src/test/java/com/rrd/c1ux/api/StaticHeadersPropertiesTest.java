package com.rrd.c1ux.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.header.Header;

class StaticHeadersPropertiesTest {

	@Test
	void testAsHeaders() {

		StaticHeadersProperties testSubject = new StaticHeadersProperties();

		assertTrue(testSubject.asHeaders().isEmpty());

		testSubject.setStaticHeaders(Map.of("X-Auth-Token", "1234"));
		assertNotNull(testSubject.getStaticHeaders());
		assertFalse(testSubject.getStaticHeaders().isEmpty());

		List<Header> headers = testSubject.asHeaders();

		assertFalse(headers.isEmpty());

		Header header = headers.get(0);
		assertEquals("X-Auth-Token", header.getName());
		assertEquals("1234", header.getValues().get(0));
	}

}
