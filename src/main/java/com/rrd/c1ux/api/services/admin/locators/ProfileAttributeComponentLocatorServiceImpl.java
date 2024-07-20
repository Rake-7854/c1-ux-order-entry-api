package com.rrd.c1ux.api.services.admin.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.admin.locator.ProfileAttributeComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IProfileAttribute;

@Service
public class ProfileAttributeComponentLocatorServiceImpl implements ProfileAttributeComponentLocatorService {

	@Override
	public IProfileAttribute locate(CustomizationToken token) throws AtWinXSException {
		
		return ProfileAttributeComponentLocator.locate(token);
	}
}
