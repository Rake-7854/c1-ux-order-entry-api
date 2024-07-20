package com.rrd.c1ux.api.services.items.locator;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.catalogs.locator.CMCompanionItemComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICompanionItem;

@Service
public class CMCompanionItemComponentLocatorServiceImpl implements CMCompanionItemComponentLocatorService{

	@Override
	public ICompanionItem locate(CustomizationToken token) throws AtWinXSException {
		return CMCompanionItemComponentLocator.locate(token);
	}

}
