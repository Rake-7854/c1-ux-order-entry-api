package com.rrd.c1ux.api.services.locators;

import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.IBusinessUnit;
import com.wallace.atwinxs.interfaces.ILoginInterface;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.interfaces.IUserGroup;

public interface IAdminComponentLocator {

    ILoginInterface locateLoginComponent(CustomizationToken token) throws AtWinXSException;

    IUserGroup locateUserGroupComponent(CustomizationToken token) throws AtWinXSException;

    IBusinessUnit locateBusinessUnitComponent(CustomizationToken token) throws AtWinXSException;

    IProfileInterface locateProfileComponent(CustomizationToken token) throws AtWinXSException;

    ISite locateSiteComponent(CustomizationToken token) throws AtWinXSException;
}
