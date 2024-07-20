package com.rrd.c1ux.api.services.admin.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.admin.locator.ProfileComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IProfileInterface;

@Service
public class ProfileComponentLocatorServiceImpl implements ProfileComponentLocatorService {
	
	@Override
	public IProfileInterface locate(CustomizationToken token) throws AtWinXSException {

		return ProfileComponentLocator.locate(token);
	}
}
