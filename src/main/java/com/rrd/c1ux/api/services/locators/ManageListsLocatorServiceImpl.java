package com.rrd.c1ux.api.services.locators;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.lists.locator.ManageListsLocator;

@Service
public class ManageListsLocatorServiceImpl implements ManageListsLocatorService {

    @Override
    public IManageList locate(CustomizationToken token) throws AtWinXSException {

    	return ManageListsLocator.locate(token);
    }
}
