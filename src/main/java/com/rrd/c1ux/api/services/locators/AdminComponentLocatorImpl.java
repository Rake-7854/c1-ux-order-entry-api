package com.rrd.c1ux.api.services.locators;

import com.wallace.atwinxs.admin.locator.BusinessUnitComponentLocator;
import com.wallace.atwinxs.admin.locator.LoginComponentLocator;
import com.wallace.atwinxs.admin.locator.ProfileComponentLocator;
import com.wallace.atwinxs.admin.locator.SiteComponentLocator;
import com.wallace.atwinxs.admin.locator.UserGroupComponentLocator;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IBusinessUnit;
import com.wallace.atwinxs.interfaces.ILoginInterface;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.interfaces.IUserGroup;

import org.springframework.stereotype.Service;

@Service
public class AdminComponentLocatorImpl implements IAdminComponentLocator {

    @Override
    public ILoginInterface locateLoginComponent(CustomizationToken token) throws AtWinXSException {

        return LoginComponentLocator.locate(token);
    }

    @Override
    public IUserGroup locateUserGroupComponent(CustomizationToken token) throws AtWinXSException {

        return UserGroupComponentLocator.locate(token);
    }

    @Override
    public IBusinessUnit locateBusinessUnitComponent(CustomizationToken token) throws AtWinXSException {

        return BusinessUnitComponentLocator.locate(token);
    }

    @Override
    public IProfileInterface locateProfileComponent(CustomizationToken token) throws AtWinXSException {

        return ProfileComponentLocator.locate(token);
    }

    @Override
    public ISite locateSiteComponent(CustomizationToken token) throws AtWinXSException {

        return SiteComponentLocator.locate(token);
    }
}
