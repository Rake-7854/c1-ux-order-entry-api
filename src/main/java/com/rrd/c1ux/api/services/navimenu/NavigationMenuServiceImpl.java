/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#						Description
 * 	--------	-----------				-----------------------		--------------------------------
 *	09/14/22	Krishna Natarajan	 	CAP-35165					made changes for adaptability added storemetemporarily concatenation to reouterlink														
 *	01/12/23	Sumit Kumar			 	CAP-37667					display menu based on selected services		
 *  01/19/23    Sakthi M				CAP-37494					Saved Order – Add Saved Order to order main menu (API Change)	
 *  01/26/23	A Salcedo				CAP-37494					Refactored code.	
 *  01/31/23	A Salcedo				CAP-38417					Fix main menu "Orders" display.	
 *  04/19/23	Sakthi M				CAP-39245					Modify Navigation Menu API (the one used) to refactor generation and to make/use new translation text values
 *  05/17/23    S Ramachandran			CAP-39973       			Added Quick Start Guide sub menu under Support menu
 *  06/07/23    Sakthi M				CAP-39209					API Change - Modify Navigation Menu API (the one used) to add Admin Tools options
 *  06/15/23    Sakthi M				CAP-39752					C1UX BE - API Change - Modify Navigation Menu API (the one used) to add Reports option
 *  06/23/23	A Salcedo				CAP-41241					Encrypt entryPoint for CP redirect.
 *  07/05/23	Sakthi M				CAP-41961					C1UX BE - API Change - Modify Navigation Menu API - update 'reports' style class for "Reports" Menu
 *  07/19/23	A Salcedo				CAP-42336					Encode reports entryPoint.
 *  08/23/23	Krishna Natarajan		CAP-42607 					Added a null check on the catalogs to build 'Shop By Catalogs' menu in getMenuOrder
 *  09/06/23	Krishna Natarajan		CAP-42607					Added an empty check on catalog object in getMenuOrder
 *  08/25/23	Krishna Natarajan		CAP-43281 					Added logic to set boolean true if the menu has shop by catalog
 *  11/28/23	C Codina				CAP-45299					Modify menu to add order for another user if user has ability to do OOB
 *  12/29/23	Satishkumar A			CAP-45801					C1UX BE_Bug - Menu for Prime API - Punchout user can see Order on Behalf menu option
 *  01/05/24	Krishna Natarajan		CAP-46263					To use settings for order originator for both Order For Another User 
 *	04/26/24	L De Leon				CAP-48622					Modified processOrderSubMenu() method to add check for template orders
 *	07/04/24	Krishna Natarajan		CAP-50733					Added getMenuOrderOtherThanCatalog() and getMenuOrderForCatalog() methods to build menu on kit edit search and browse mode
 */
package com.rrd.c1ux.api.services.navimenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.catalog.HrefItemsForNaviMenu;
import com.rrd.c1ux.api.models.catalog.ItemsForNaviMenu;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.ui.menu.MenuGroup;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

/**
 * @author Krishna Natarajan
 *
 */
@Service
public class NavigationMenuServiceImpl extends BaseService implements NavigationMenuService {
	//Added translation service
	public NavigationMenuServiceImpl(TranslationService translationService) {
	    super(translationService);
	  }

	/**
	 * @param appSession {@link AppSessionBean}
	 * @return the collection of menu groups Collection<MenuGroups>
	 * @throws AtWinXSException
	 */
	
	public Collection<MenuGroup> getNavigationMenu(SessionContainer sc) throws AtWinXSException
	{
		return sc.getApplicationSession().getAppSessionBean().getMenuGroups();
	}

	//CAP-38417
	public boolean displaySaveOrdersMenu(AppSessionBean asb, boolean hasOrdersService, boolean userSettingShowSaveOrders)
	{
		return hasOrdersService && AtWinXSConstant.INVALID_PROFILE_NUMBER != asb.getProfileNumber() && !asb.isPunchout() && userSettingShowSaveOrders;
	} 
	
	/**
	 * @param appSession {@link AppSessionBean}
	 * @return the collection of menu groups Collection<MenuGroups>
	 * @throws AtWinXSException
	 */
	
    //CAP-39245
	//CAP-39973 - Add Quick Start Guide sub menu under Support menu
	public List<CatalogMenuWithNavigationMenu> buildMenu(List<Object> catalogs, SessionContainer sc,boolean aMenuTools)
			throws AtWinXSException 
	{	
		ArrayList<CatalogMenuWithNavigationMenu> cMnavMenu = new ArrayList<>();
		CatalogMenuWithNavigationMenu cmenunav = null;
		ArrayList<Object> cMItemsSubCat = null;
		boolean isAllowCatalogBrowse=false;
		boolean showSaveOrdersSubMenu=false;
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		boolean hasOrderSearchService = asb.hasService(AtWinXSConstant.ORDER_SEARCH_SERVICE_ID); // Order Search service
		boolean hasOrdersService = asb.hasService(AtWinXSConstant.ORDERS_SERVICE_ID); // PMO service
		boolean hasReport = asb.hasService(AtWinXSConstant.REPORTS_SERVICE_ID);//Report Service
		boolean isAllowOrderOnBehalf = false;
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		
		if (hasOrdersService) {
			//CAP-46263 
			OEResolvedUserSettingsSessionBean origSettings = AdminUtil.getUserSettings(
					new LoginVOKey(asb.getSiteID(), asb.getOriginatorProfile().getLoginID()), asb.getSessionID(),
					asb.getCustomToken());
			isAllowOrderOnBehalf = origSettings.isAllowOrderOnBehalf();

			//CAP-45299
			isAllowCatalogBrowse = userSettings.isAllowCatalogBrowse();
			showSaveOrdersSubMenu = displaySaveOrdersMenu(asb, hasOrdersService, userSettings.isShowSaveOrders());
			
		}
		List<String> order = getMenuOrderForCatalog(isAllowCatalogBrowse, hasOrdersService, catalogs);
		order = getMenuOrderOtherThanCatalog(hasOrderSearchService, showSaveOrdersSubMenu, aMenuTools, hasReport, sc, order);
		int mainOrder = 1;
		for (String topLevelMenu : order) {
			cMItemsSubCat = new ArrayList<>();
			cmenunav = new CatalogMenuWithNavigationMenu();
			cmenunav.setId(mainOrder);
			cmenunav.setLabel(
					translationService.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
							sc.getApplicationSession().getAppSessionBean().getCustomToken(),
							SFTranslationTextConstants.PREFIX_SF + topLevelMenu));
			if(topLevelMenu.equalsIgnoreCase(RouteConstants.MENU_ADMIN_TOOLS)) {
				cmenunav.setRouterLink(RouteConstants.CP_REDIRECT);
				cmenunav.setStyleClass(RouteConstants.ADMIN_TOOLS_SYTLE_CLASS); 
			}
			//CAP-39752
			else if(topLevelMenu.equalsIgnoreCase(RouteConstants.TOP_LVL_MENU_REPORTS))
			{
				cmenunav.setRouterLink(RouteConstants.CP_REDIRECT + "/" + Util.encryptString(RouteConstants.CP_REDIRECT_RPT));//CAP-42336
				//CAP-41961
				cmenunav.setStyleClass(RouteConstants.REPORTS_SYTLE_CLASS); 
			}
			else {
				cmenunav.setRouterLink(topLevelMenu);
				cmenunav.setStyleClass(RouteConstants.EMPTY_STRING);
			}
			cMnavMenu.add(cmenunav);
			mainOrder++;

			switch (topLevelMenu) {
			case RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG:
					cMItemsSubCat.add(catalogs);
					cmenunav.setItems(catalogs);
					cmenunav.setDisplayShopByCatalog(true);
				break;

			case RouteConstants.TOP_LVL_MENU_ORDERS:
				mainOrder = processOrderSubMenu(sc, cmenunav, cMItemsSubCat, showSaveOrdersSubMenu,
						isAllowOrderOnBehalf, mainOrder, topLevelMenu);
				break;
				
				
			case RouteConstants.TOP_LVL_MENU_SUPPORT:
				cMItemsSubCat.add(buildSubMenuList(sc, mainOrder++,
						SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.SUB_MENU_MESSAGE_CENTER,
						topLevelMenu+"/"+SFTranslationTextConstants.SUB_MENU_MESSAGE_CENTER));
				cMItemsSubCat.add(buildSubMenuList(sc, mainOrder++,
						SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.SUB_MENU_HELP_CONTACT,
						topLevelMenu+"/"+SFTranslationTextConstants.SUB_MENU_HELP_CONTACT));
				cMItemsSubCat.add(buildHrefSubMenuList(sc, mainOrder++,
						SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.SUB_MENU_QUICK_START_GUIDE,
						getQuickStartGuidePathC1UX(),ModelConstants.HLINK_TARGET));
				cmenunav.setItems(cMItemsSubCat);
				break;
				
			default:
				//To add any default case available in future
			}
		}
		return cMnavMenu;
	}

	private int processOrderSubMenu(SessionContainer sc, CatalogMenuWithNavigationMenu cmenunav,
			ArrayList<Object> cMItemsSubCat, boolean showSaveOrdersSubMenu,
			boolean isAllowOrderOnBehalf, int mainOrder, String topLevelMenu) throws AtWinXSException {
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		boolean hasOrderSearchService = asb.hasService(AtWinXSConstant.ORDER_SEARCH_SERVICE_ID);
		if (hasOrderSearchService) {
			cMItemsSubCat.add(buildSubMenuList(sc, mainOrder++,
					SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.SUB_MENU_ORDER_SEARCH,
					topLevelMenu+"/"+SFTranslationTextConstants.SUB_MENU_ORDER_SEARCH));
			cmenunav.setItems(cMItemsSubCat);
		}
		if (showSaveOrdersSubMenu) {
			cMItemsSubCat.add(buildSubMenuList(sc, mainOrder++,
					SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.SUB_MENU_ORDER_SAVED,
					topLevelMenu+"/"+SFTranslationTextConstants.SUB_MENU_ORDER_SAVED));
			cmenunav.setItems(cMItemsSubCat);
		}
		//CAP-45299 CAP-45801
		if (!asb.isPunchout() && isAllowOrderOnBehalf) {
			cMItemsSubCat.add(buildSubMenuList(sc, mainOrder++, SFTranslationTextConstants.ORDER_ON_BEHALF_MENU_LABEL,
					topLevelMenu+"/"+ SFTranslationTextConstants.ORDER_FOR_ANOTHER_USER));
			cmenunav.setItems(cMItemsSubCat);
		}
		// CAP-48622
		if (isShowTemplateOrdering(sc, asb)) {
			cMItemsSubCat.add(buildSubMenuList(sc, mainOrder++, SFTranslationTextConstants.TEMPLATE_ORDERING_MENU_LBL,
					new StringBuilder(topLevelMenu).append(AtWinXSConstant.FORWARD_SLASH)
							.append(SFTranslationTextConstants.TEMPLATE_ORDERING).toString()));
			cmenunav.setItems(cMItemsSubCat);
		}

		return mainOrder;
	}

	// CAP-48622
	protected boolean isShowTemplateOrdering(SessionContainer sc, AppSessionBean asb) {
		return asb.hasService(AtWinXSConstant.ORDERS_SERVICE_ID)
				&& ((OrderEntrySession) sc.getModuleSession()).getOESessionBean().getUserSettings()
						.isShowTemplatesLink()
				&& AtWinXSConstant.INVALID_ID != asb.getProfileNumber()
				&& !asb.isPunchout();
	}

	public List<String> getMenuOrder(boolean isAllowCatalogBrowse, boolean hasOrdersService,
			boolean hasOrderSearchService, boolean showSaveOrdersSubMenu, boolean aMenuTools, boolean hasReport,
			List<Object> catalogs) {
		List<String> topLevelMenu = new ArrayList<>();

		if (hasOrdersService && isAllowCatalogBrowse && null!=catalogs && !catalogs.isEmpty()) {//CAP-42607 added a null check
			topLevelMenu.add(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
		}
		if (hasOrderSearchService || showSaveOrdersSubMenu) {
			topLevelMenu.add(RouteConstants.TOP_LVL_MENU_ORDERS);
		}
		//CAP-39752
		if (hasReport) {
			topLevelMenu.add(RouteConstants.TOP_LVL_MENU_REPORTS);
		}
		if (aMenuTools) {
			topLevelMenu.add(RouteConstants.MENU_ADMIN_TOOLS);
		}
		topLevelMenu.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
		return topLevelMenu;
	}

	public ItemsForNaviMenu buildSubMenuList(SessionContainer sc, int id, String label, String routerLink)
			throws AtWinXSException {
		ItemsForNaviMenu items = new ItemsForNaviMenu();
		items.setId(id);
		items.setLabel(
				translationService.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
						sc.getApplicationSession().getAppSessionBean().getCustomToken(), label));
		items.setRouterLink(routerLink);
		return items;
	}
	
	//CAP-39973 HRef Menu Item builder
	public HrefItemsForNaviMenu buildHrefSubMenuList(SessionContainer sc, int id, String label, String url, String target)
			throws AtWinXSException {
		HrefItemsForNaviMenu hRefItems = new HrefItemsForNaviMenu();
		hRefItems.setId(id);
		hRefItems.setLabel(
				translationService.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
						sc.getApplicationSession().getAppSessionBean().getCustomToken(), label));
		hRefItems.setUrl(url);
		hRefItems.setTarget(target);
		return hRefItems;
	}

	//CAP-39973 - Read quickStartGuidePath from XST331_CSTM_PT_PROP 
	protected String  getQuickStartGuidePathC1UX() throws AtWinXSException {
		
		XSProperties prop = PropertyUtil.getProperties(ModelConstants.C1UX_PROPERTY_TYPE);		
		return Util.nullToEmpty(prop.getProperty(ModelConstants.C1UX_PROPERTY_KEY_QUICK_START_GUIDE_PATH));
	}
	
	// CAP-50733
	public List<String> getMenuOrderOtherThanCatalog(boolean hasOrderSearchService, boolean showSaveOrdersSubMenu,
			boolean aMenuTools, boolean hasReport, SessionContainer sc, List<String> topLevelMenu) {
		if (sc.getApplicationVolatileSession().getVolatileSessionBean()
				.getCatalogSearchMode() != OrderEntryConstants.CATALOG_SEARCH_MODE_KIT_BROWSE
				&& sc.getApplicationVolatileSession().getVolatileSessionBean()
						.getCatalogSearchMode() != OrderEntryConstants.CATALOG_SEARCH_MODE_KIT_SEARCH) {
			if (hasOrderSearchService || showSaveOrdersSubMenu) {
				topLevelMenu.add(RouteConstants.TOP_LVL_MENU_ORDERS);
			}
			// CAP-39752
			if (hasReport) {
				topLevelMenu.add(RouteConstants.TOP_LVL_MENU_REPORTS);
			}
			if (aMenuTools) {
				topLevelMenu.add(RouteConstants.MENU_ADMIN_TOOLS);
			}
			topLevelMenu.add(RouteConstants.TOP_LVL_MENU_SUPPORT);
			return topLevelMenu;
		} else {
			return topLevelMenu;
		}
	}
	
	// CAP-50733
	public List<String> getMenuOrderForCatalog(boolean isAllowCatalogBrowse, boolean hasOrdersService,
			List<Object> catalogs) {
		List<String> topLevelMenu = new ArrayList<>();
		if (hasOrdersService && isAllowCatalogBrowse && null != catalogs && !catalogs.isEmpty()) {
			topLevelMenu.add(RouteConstants.TOP_LVL_MENU_SHOP_BY_CATALOG);
		}
		return topLevelMenu;
	}
	
}	 
