package com.rrd.c1ux.api.services.orderentry.locator;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;

public interface OEShoppingCartComponentLocatorService {

	IOEShoppingCartComponent locate(CustomizationToken token)  throws AtWinXSException;
	
}
