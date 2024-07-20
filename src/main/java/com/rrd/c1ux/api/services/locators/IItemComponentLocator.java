package com.rrd.c1ux.api.services.locators;

import com.rrd.custompoint.services.interfaces.IItemServicesComponent;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

public interface IItemComponentLocator {

    IItemServicesComponent locate(CustomizationToken token) throws AtWinXSException;
}
