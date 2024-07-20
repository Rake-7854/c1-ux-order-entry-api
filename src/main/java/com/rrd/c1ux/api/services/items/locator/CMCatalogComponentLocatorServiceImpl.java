package com.rrd.c1ux.api.services.items.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICatalog;

@Service
public class CMCatalogComponentLocatorServiceImpl implements CMCatalogComponentLocatorService {

	@Override
	public ICatalog locate(CustomizationToken token) throws AtWinXSException {
		return CMCatalogComponentLocator.locate(token);
	}

}
