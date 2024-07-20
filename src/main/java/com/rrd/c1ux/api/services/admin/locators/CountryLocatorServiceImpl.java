package com.rrd.c1ux.api.services.admin.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICountryComponent;
import com.wallace.atwinxs.locale.locator.CountryLocator;

@Service
public class CountryLocatorServiceImpl implements CountryLocatorService {

	@Override
	public ICountryComponent locate(CustomizationToken token) throws AtWinXSException {

		return CountryLocator.locate(token);
	}
}
