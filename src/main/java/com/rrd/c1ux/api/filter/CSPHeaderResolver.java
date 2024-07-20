package com.rrd.c1ux.api.filter;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.exceptions.AtWinXSSessionException;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.util.PropertyUtilService;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.rrd.custompoint.services.vo.KeyValuePair;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.tt.arch.TTException;
import com.wallace.tt.arch.web.TTPerformanceTracker;
import com.wallace.tt.vo.TTSession;

public class CSPHeaderResolver implements HeaderResolver {
	
	private static final Logger LOG = LoggerFactory.getLogger(CSPHeaderResolver.class);

	public static final String NONCE_TEMPLATE = "${NONCE}";

	public static final String CSP_NONCE_TEMPLATE = "'nonce-" + NONCE_TEMPLATE + "'";
	
	public static final String CSP_NONCE = "CSP-NONCE";

	private static final String DEFAULT_SUFFIX = "_DEFAULT";
	
	public static final String CSP_VALUE_KEY = "SF_CSP";
	
	public static final String CSP_HEADER_KEY = "SF_CSP_HEADER";
	
	private final CPSessionReader cpSessionReader;
	
	private final PropertyUtilService propertyUtilService;
	
	public CSPHeaderResolver(CPSessionReader cpSessionReader, PropertyUtilService propertyUtilService) {
		this.cpSessionReader = cpSessionReader;
		this.propertyUtilService = propertyUtilService;
	}
	
	@Override
	public Optional<KeyValuePair> resolve(HttpServletRequest request) throws AtWinXSException {
		
		Optional<KeyValuePair> headers = lookupCSPHeaders(request);
		
		if (headers.isPresent() && headers.get().getValue().contains(NONCE_TEMPLATE)) {
			
			String cspString;
			if (request.getAttribute(CSP_NONCE) != null) {
				cspString = headers.get().getValue().replace(NONCE_TEMPLATE, (String) request.getAttribute(CSP_NONCE));
			} else {
				cspString = headers.get().getValue().replace(CSP_NONCE_TEMPLATE, "");
			}
			
			headers = Optional.of(new KeyValuePair(headers.get().getKey(), cspString));
		}

		return headers;
	}
	
	public Optional<KeyValuePair> lookupCSPHeaders(HttpServletRequest request) throws AtWinXSException {
		
		Optional<KeyValuePair> result = Optional.empty();
		
		XSProperties systemProps = propertyUtilService.getProperties(LoginConstants.PROP_SYSTEM);
		
		int siteID = 0;
		try {
			siteID = findSiteId(request);
		} catch (TTException e) {
			throw new AtWinXSException("unable to find site ID", e);
		}

		String siteValueKey = siteID + "_" + CSP_VALUE_KEY;
		String siteHeaderKey = siteID + "_" + CSP_HEADER_KEY;

		String cspString = systemProps.getProperty(siteValueKey);
		String cspHeader = systemProps.getProperty(siteHeaderKey);
		
		if (cspHeader == null && cspString == null) {
			cspString = systemProps.getProperty(CSP_VALUE_KEY + DEFAULT_SUFFIX);
			cspHeader = systemProps.getProperty(CSP_HEADER_KEY + DEFAULT_SUFFIX);
		}
		
		if (cspHeader != null && cspString != null) {
			result = Optional.of(new KeyValuePair(cspHeader, cspString));
		}
		
		return result;
	}

	protected int findSiteId(HttpServletRequest request) throws TTException, AtWinXSException {
		
		int siteID = 0;
				
		// Try and get Session from Request
		String session = request.getHeader(AtWinXSConstant.TT_SESSION);

		try {
		// If our session is not blank/null, we will get the settings for it
			if (session != null) {
				TTSession ttSession = new TTSession();
				ttSession.setTokenPassedIn(ttSession.decodeURL(session));
				ttSession.setId(ttSession.parseIdFromToken(ttSession.getTokenPassedIn()));
	
				if (ttSession.select(ttSession.getId(), new TTPerformanceTracker())) {
					siteID = ttSession.getSite();
				}
	
			} else {
				// otherwise, look for a session in the session container
				SessionContainer sc = cpSessionReader.getSessionContainer(AtWinXSConstant.EMPTY_STRING,
						AtWinXSConstant.APPSESSIONSERVICEID);
				AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
				siteID = asb.getSiteID();
			}
		} catch (AtWinXSSessionException | TTException e) {
			LOG.debug("unable to retrieve session", e);
		}
		
		return siteID;
	}
	
}
