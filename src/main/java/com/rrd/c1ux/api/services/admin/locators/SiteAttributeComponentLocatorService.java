package com.rrd.c1ux.api.services.admin.locators;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ISiteAttribute;

public interface SiteAttributeComponentLocatorService {
	
	ISiteAttribute locate(CustomizationToken token) throws AtWinXSException;
}
