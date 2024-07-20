package com.rrd.c1ux.api.services.items.locator;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICompanionItem;

public interface CMCompanionItemComponentLocatorService {
	ICompanionItem locate(CustomizationToken token) throws AtWinXSException;
}
