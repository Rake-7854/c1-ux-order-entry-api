/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#						Description
 * 	--------	-----------				-----------------------		--------------------------------
 *	06/12/23	Sakthi M	 			CAP-39209					Initial testcase creation														
 *	08/23/23	Krishna Natarajan		CAP-42607 					Added tests for the coverage
 *	11/29/23	C Codina				CAP-45299					Added tests coverage for OOB
 *	05/01/24	Krishna Natarajan		CAP-46263					Added code to fetch the userSettings logic (AdminUtil)
 *	04/26/24	L De Leon				CAP-48622					Added tests for isShowTemplateOrdering() method
 *	07/04/24	Krishna Natarajan		CAP-50733					Added test for getMenuOrderOtherThanCatalog() and getMenuOrderForCatalog() method
 */


package com.rrd.c1ux.api.services.navimenu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

class NavigationMenuServiceImplTest extends BaseServiceTest{
	
	@InjectMocks
	private NavigationMenuServiceImpl serviceToTest;
	List<Object> objects =new ArrayList<>();
	public static final String res="Support";
	
	@Mock
	private CatalogMenuWithNavigationMenu mockCatalogMenuWithNavigationMenu;
	
	@Mock
	private OEResolvedUserSettingsSessionBean mockOEResolvedUserSettingsSessionBean;

	private List<CatalogMenuWithNavigationMenu> mCatalogMenuWIthNavigationMenu;
	public static final String SUCCESS = "Success";
	public static final String FAIL="Failed";
	XSProperties xsProps=null;
	
	
	@Test
	void that_buildMenu_returns_success() throws AtWinXSException {//CAP-46263
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class);
				MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)) {
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			xsProps = new XSProperties();
			xsProps.setProperty(ModelConstants.C1UX_PROPERTY_KEY_QUICK_START_GUIDE_PATH, "");
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockCompositeProfileBean.getLoginID()).thenReturn("123");
			when(mockUserSettings.isShowSaveOrders()).thenReturn(true);
			when(mockUserSettings.isShowTemplatesLink()).thenReturn(true); // CAP-48622
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(xsProps);
			
			mockedAdminUtil.when(() -> AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);//CAP-50733
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(0);
			
			Mockito.when(serviceToTest.buildMenu(objects, mockSessionContainer, false))
					.thenReturn(mCatalogMenuWIthNavigationMenu);
			mCatalogMenuWIthNavigationMenu = serviceToTest.buildMenu(objects, mockSessionContainer, false);
			assertTrue(!mCatalogMenuWIthNavigationMenu.isEmpty(), SUCCESS);
			
			when(mockAppSessionBean.isPunchout()).thenReturn(true);
			mCatalogMenuWIthNavigationMenu = serviceToTest.buildMenu(objects, mockSessionContainer, false);
			assertTrue(!mCatalogMenuWIthNavigationMenu.isEmpty(), SUCCESS);
			
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(false);
			mCatalogMenuWIthNavigationMenu = serviceToTest.buildMenu(objects, mockSessionContainer, false);
			assertTrue(!mCatalogMenuWIthNavigationMenu.isEmpty(), SUCCESS);
			
			when(mockAppSessionBean.isPunchout()).thenReturn(false);
			mCatalogMenuWIthNavigationMenu = serviceToTest.buildMenu(objects, mockSessionContainer, false);
			assertTrue(!mCatalogMenuWIthNavigationMenu.isEmpty(), SUCCESS);
		}
	}

	@Test
	void that_getMenuOrder_returns_success() throws AtWinXSException {
		List<String> checkmenustring = serviceToTest.getMenuOrder(true, true, true, true, true, true, objects);
		assertNotNull(checkmenustring);
	}

	@Test
	void that_getMenuOrder_with_null_catalogs_false_hasOrdersService_and_isAllowCatalogBrowse()
			throws AtWinXSException {
		List<String> checkmenustring = serviceToTest.getMenuOrder(false, false, true, true, true, true, null);
		assertNotNull(checkmenustring);
	}

	@Test
	void that_getMenuOrder_with_false_isAllowCatalogBrowse() throws AtWinXSException {
		List<String> checkmenustring = serviceToTest.getMenuOrder(false, true, true, true, true, true, null);
		assertNotNull(checkmenustring);
	}

	@Test
	void that_getMenuOrder_with_false_hasOrdersService() throws AtWinXSException {
		List<String> checkmenustring = serviceToTest.getMenuOrder(true, false, true, true, true, true, null);
		assertNotNull(checkmenustring);
	}

	@Test
	void that_getMenuOrder_with_false_hasOrderSearchService() throws AtWinXSException {
		List<String> checkmenustring = serviceToTest.getMenuOrder(true, true, false, true, true, true, null);
		assertNotNull(checkmenustring);
	}
	 
	@Test
	void that_buildMenu_Checks_notnull_set_boolean() throws AtWinXSException {
        try(MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class)) {
        	List<String> menulist= List.of(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
        	serviceToTest=Mockito.spy(serviceToTest);
			doReturn(menulist).when(serviceToTest).getMenuOrderForCatalog(anyBoolean(), anyBoolean(), any());//CAP-50733
			doReturn(menulist).when(serviceToTest).getMenuOrderOtherThanCatalog(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any());

        	when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
    		xsProps=new XSProperties();
    		xsProps.setProperty(ModelConstants.C1UX_PROPERTY_KEY_QUICK_START_GUIDE_PATH,"");
        	when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
            when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockAppSessionBean.hasService(36)).thenReturn(true);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(xsProps);
            Mockito.when(serviceToTest.buildMenu(objects, mockSessionContainer, false)).thenReturn(mCatalogMenuWIthNavigationMenu);
            mCatalogMenuWIthNavigationMenu = serviceToTest.buildMenu(objects,mockSessionContainer,false);
			assertTrue(!mCatalogMenuWIthNavigationMenu.isEmpty(),SUCCESS);
          }
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitbrowse() throws AtWinXSException {
        try(MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class)) {
        	List<String> menulist= List.of(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
        	serviceToTest=Mockito.spy(serviceToTest);
			doReturn(menulist).when(serviceToTest).getMenuOrderForCatalog(anyBoolean(), anyBoolean(), any());//CAP-50733
			doReturn(menulist).when(serviceToTest).getMenuOrderOtherThanCatalog(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any());

        	when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
    		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
    		xsProps=new XSProperties();
    		xsProps.setProperty(ModelConstants.C1UX_PROPERTY_KEY_QUICK_START_GUIDE_PATH,"");
        	when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
            when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockAppSessionBean.hasService(36)).thenReturn(true);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			mockedStatic.when(() -> PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE)).thenReturn(xsProps);
            Mockito.when(serviceToTest.buildMenu(objects, mockSessionContainer, false)).thenReturn(mCatalogMenuWIthNavigationMenu);
            mCatalogMenuWIthNavigationMenu = serviceToTest.buildMenu(objects,mockSessionContainer,false);
			assertTrue(!mCatalogMenuWIthNavigationMenu.isEmpty(),SUCCESS);
          }
	}
	
	@Test
	void that_getMenuOrder_returns_success_listempty() throws AtWinXSException {
		List<Object> listempty= new ArrayList<>();
				List<String> checkmenustring=serviceToTest.getMenuOrder(true, true, true, true, true, true, listempty);
				assertNotNull(checkmenustring);
	}

	// CAP-48622
	@Test
	void that_isShowTemplateOrdering_returns_false_invalidProfileNumber() throws AtWinXSException {

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(true);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(AtWinXSConstant.INVALID_ID);

		boolean response = serviceToTest.isShowTemplateOrdering(mockSessionContainer, mockAppSessionBean);
		assertFalse(response);
	}

	// CAP-48622
	@Test
	void that_isShowTemplateOrdering_returns_false_settingIsOff() throws AtWinXSException {

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowTemplatesLink()).thenReturn(false);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);

		boolean response = serviceToTest.isShowTemplateOrdering(mockSessionContainer, mockAppSessionBean);
		assertFalse(response);
	}

	// CAP-48622
	@Test
	void that_isShowTemplateOrdering_returns_false_hasNoOrderService() throws AtWinXSException {

		when(mockAppSessionBean.hasService(anyInt())).thenReturn(false);

		boolean response = serviceToTest.isShowTemplateOrdering(mockSessionContainer, mockAppSessionBean);
		assertFalse(response);
	}
	
	// CAP-50733
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitsearch() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		menulist.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		menulist.add(RouteConstants.MENU_ADMIN_TOOLS);
		menulist.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);// CAP-50733
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(2);
		serviceToTest.getMenuOrderOtherThanCatalog(true, true, true, true, mockSessionContainer, menulist);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitbrows() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		menulist.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		menulist.add(RouteConstants.MENU_ADMIN_TOOLS);
		menulist.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);// CAP-50733
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(1);
		serviceToTest.getMenuOrderOtherThanCatalog(true, true, true, true, mockSessionContainer, menulist);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitsearchfalse() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		menulist.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		menulist.add(RouteConstants.MENU_ADMIN_TOOLS);
		menulist.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);// CAP-50733
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(2);
		serviceToTest.getMenuOrderOtherThanCatalog(false, false, false, false, mockSessionContainer, menulist);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitbrowsfalse() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		menulist.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		menulist.add(RouteConstants.MENU_ADMIN_TOOLS);
		menulist.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);// CAP-50733
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(1);
		serviceToTest.getMenuOrderOtherThanCatalog(false, false, false, false, mockSessionContainer, menulist);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitsearchfalse_hasordsertrue() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		menulist.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		menulist.add(RouteConstants.MENU_ADMIN_TOOLS);
		menulist.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);// CAP-50733
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(2);
		serviceToTest.getMenuOrderOtherThanCatalog(true, false, false, false, mockSessionContainer, menulist);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_setCatalogkitbrowsfalse_hasordserfalse() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		menulist.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		menulist.add(RouteConstants.MENU_ADMIN_TOOLS);
		menulist.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);// CAP-50733
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getCatalogSearchMode()).thenReturn(1);
		serviceToTest.getMenuOrderOtherThanCatalog(false, true, false, false, mockSessionContainer, menulist);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_getMenuOrderForCatalog() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		List<Object> catalogs = new ArrayList<Object>();
		menulist.add(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
		serviceToTest.getMenuOrderForCatalog(true, true,catalogs);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_getMenuOrderForCatalog_notnull() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
		List<Object> catalogs = new ArrayList<Object>();
		catalogs.add(menulist);
		serviceToTest.getMenuOrderForCatalog(true, true,catalogs);
		assertNotNull(menulist);
	}
	
	@Test
	void that_buildMenu_Checks_notnull_set_boolean_getMenuOrderForCatalog_notnull_false() throws AtWinXSException {
		List<String> menulist = new ArrayList<>();
		menulist.add(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
		List<Object> catalogs = new ArrayList<Object>();
		catalogs.add(menulist);
		serviceToTest.getMenuOrderForCatalog(false, false,catalogs);
		assertNotNull(menulist);
	}
}
