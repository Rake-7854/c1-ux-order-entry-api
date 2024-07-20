package com.rrd.c1ux.api.services.admin.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.admin.locator.SiteAttributeComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ISiteAttribute;

@Service
public class SiteAttributeComponentLocatorServiceImpl implements SiteAttributeComponentLocatorService {
	
	@Override
	public ISiteAttribute locate(CustomizationToken token) throws AtWinXSException {
		return SiteAttributeComponentLocator.locate(token);
	}

}
