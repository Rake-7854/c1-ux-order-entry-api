package com.rrd.c1ux.api.services.locators;

import com.rrd.custompoint.services.interfaces.IItemServicesComponent;
import com.rrd.custompoint.services.locator.ItemServiceComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

import org.springframework.stereotype.Service;

@Service
public class ItemComponentLocatorImpl implements IItemComponentLocator {

    @Override
    public IItemServicesComponent locate(CustomizationToken token) throws AtWinXSException {

        return ItemServiceComponentLocator.locate(token);
    }
}
