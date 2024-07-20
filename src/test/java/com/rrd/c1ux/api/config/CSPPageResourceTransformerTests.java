package com.rrd.c1ux.api.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import com.rrd.c1ux.api.filter.CSPHeaderResolver;
import com.rrd.custompoint.services.vo.KeyValuePair;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@ExtendWith(MockitoExtension.class)
class CSPPageResourceTransformerTests {

	@Mock
	private CSPHeaderResolver mockCSPHeaderResolver;
	
	@InjectMocks
	private CSPPageResourceTransformer testSubject;
	
	private MockHttpServletRequest mockRequest;
	
	@Mock
	private ResourceTransformerChain mockTransformerChain;
	
	private String indexPage = "<!DOCTYPE html>"
			+ "<html lang=\"en\">"
			+ "  <head>"
			+ "    <meta charset=\"utf-8\"/>"
			+ "    <title>ConnectOne Storefront</title>"
			+ "    <base href=\"/\"/>"
			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
			+ "    <link rel=\"icon\" type=\"image/x-icon\" href=\"favicon.ico\"/>"
			+ "  <link rel=\"stylesheet\" href=\"styles.e768b838672438b9.css\"></head>"
			+ "  <body>"
			+ "    <app-root></app-root>"
			+ "  </body>"
			+ "</html>";
	
	@BeforeEach
	void setup() {
		mockRequest = new MockHttpServletRequest();
	}
	
	@Test
	void that_transform_skips_nonce() throws Exception {
		
		Optional<KeyValuePair> empty = Optional.empty();
		Optional<KeyValuePair> noNonce = Optional.of(new KeyValuePair("Content-Security-Policy", "script-src 'self';"));
		
		for (Optional<KeyValuePair> headers : Arrays.array(empty, noNonce)) {
			Resource page = new ByteArrayResource(indexPage.getBytes());
			
			when(mockCSPHeaderResolver.lookupCSPHeaders(mockRequest)).thenReturn(headers);
			when(mockTransformerChain.transform(mockRequest, page)).thenReturn(page);
			
			Resource result = testSubject.transform(mockRequest, page, mockTransformerChain);
			
			Assertions.assertInstanceOf(ByteArrayResource.class, result);	
		}
	}
	
	@Test
	void that_transform_adds_nonce(@TempDir Path tempDir) throws Exception {
		
		Path htmlPage = tempDir.resolve("index.html");
		FileCopyUtils.copy(indexPage.getBytes(), htmlPage.toFile());
		Resource resource = new FileUrlResource(htmlPage.toUri().toURL());
		
		Optional<KeyValuePair> headers = Optional.of(new KeyValuePair("Content-Security-Policy", "script-src 'self' 'nonce-${NONCE}';"));
		when(mockCSPHeaderResolver.lookupCSPHeaders(mockRequest)).thenReturn(headers);
		when(mockTransformerChain.transform(eq(mockRequest), any(TransformedResource.class))).thenAnswer(args -> args.getArgument(1));
		
		Resource result = testSubject.transform(mockRequest, resource, mockTransformerChain);
		
		Assertions.assertInstanceOf(TransformedResource.class, result);
	}
	
	@Test
	void that_throws_ioexception() throws Exception {
		
		when(mockCSPHeaderResolver.lookupCSPHeaders(mockRequest)).thenThrow(new AtWinXSException("msg", "cls"));
		
		assertThrows(IOException.class, () -> {
			testSubject.transform(mockRequest, null, mockTransformerChain);
		});
		
	}
}
