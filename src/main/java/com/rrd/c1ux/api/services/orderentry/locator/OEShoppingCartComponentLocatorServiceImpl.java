package com.rrd.c1ux.api.services.orderentry.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.orderentry.locator.OEShoppingCartComponentLocator;

@Service
public class OEShoppingCartComponentLocatorServiceImpl implements OEShoppingCartComponentLocatorService {
	
	@Override
	public IOEShoppingCartComponent locate(CustomizationToken token)  throws AtWinXSException {
		return OEShoppingCartComponentLocator.locate(token);
	}

}
