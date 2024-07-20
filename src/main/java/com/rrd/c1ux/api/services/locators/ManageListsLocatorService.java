package com.rrd.c1ux.api.services.locators;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IManageList;

public interface ManageListsLocatorService {

    IManageList locate(CustomizationToken token) throws AtWinXSException;
}
