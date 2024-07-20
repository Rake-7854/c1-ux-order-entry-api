package com.rrd.c1ux.api.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import com.rrd.c1ux.api.filter.CSPHeaderResolver;
import com.rrd.custompoint.services.vo.KeyValuePair;

public class CSPPageResourceTransformer implements ResourceTransformer {
	
	private static final String HTTP_ATTR_NGCSPNONCE = "ngcspnonce";
	
	private static final String HTTP_META_NAME_CSP_NONCE = "CSP_NONCE";
	
	private final CSPHeaderResolver cspHeaderResolver;
	
	public CSPPageResourceTransformer(CSPHeaderResolver cspHeaderResolver) {
		this.cspHeaderResolver = cspHeaderResolver;
	}
	
	@Override
	public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain)
			throws IOException {
		
		try {
		
			Optional<KeyValuePair> cspHeader =  cspHeaderResolver.lookupCSPHeaders(request);
			
			if (cspHeader.isPresent() && cspHeader.get().getValue().contains(CSPHeaderResolver.CSP_NONCE_TEMPLATE)) {
				
				String nonce = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
				
				request.setAttribute(CSPHeaderResolver.CSP_NONCE, nonce);

				Document index = Jsoup.parse(asString(resource), StandardCharsets.UTF_8.name());
				
				index.head().appendElement("meta").attr("name", HTTP_META_NAME_CSP_NONCE).attr("content", nonce);
				
				index.getElementsByTag("app-root").stream().findFirst().ifPresent(t -> t.attr(HTTP_ATTR_NGCSPNONCE, nonce));
				
				return transformerChain.transform(request, new TransformedResource(resource, index.toString().getBytes()));
			}
		
			return transformerChain.transform(request, resource);
		
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	protected String asString(Resource resource) throws IOException {
		
		try (Reader reader = new InputStreamReader(resource.getInputStream())) {
			return FileCopyUtils.copyToString(reader);
		}
	}
}
