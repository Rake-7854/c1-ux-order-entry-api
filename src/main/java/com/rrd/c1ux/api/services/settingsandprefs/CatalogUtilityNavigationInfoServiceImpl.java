/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	04/27/22	Krishna Natarajan	CAP-34022	    Created service as per the requirement to fetch the settings and preferences for catalog navigation
 *  08/16/22	Sumit Kumar			CAP-35419		Modify API service for Catalog Utility navigation – Line view
 *  09/16/22	Krishna Natarajan	CAP-35964		Defect fix on handling the preferences for null profile
 *  10/06/22	A Boomker			CAP-35544		Add fix so novendor users can get in without preferences saved
 *  10/10/22	Krishna Natarajan	CAP-36438/36448 Modify the API to get the required JSON response for FE
 *  10/16/22    E Anderson          CAP-36438       Data type changes for defaultSortBylabel.
 *  03/06/23	Sakthi M			CAP-39070		getOrderSearchPagination API needs to return common Settings and Preferences object in response and correct bad data
 *	04/20/23	A Boomker			CAP-39337		Fix translation of sort terms
 *  11/29/23	Krishna Natarajan	CAP-45483		Added a new method to get the Module and Customer Item Number from login query string parameters
 *  12/12/23	C Codina			CAP-45600		C1UX BE - Modify SSO to look for Entry Point fields and pass to front-end for Order Search entry point
 *  12/19/23	Krishna Natarajan	CAP-45596		C1UX BE - Modify SSO to look for Entry Point fields and pass to front-end for Catalog Ordering entry point
 *  12/07/23	N Caceres			CAP-45601		Modify SSO to look for Entry Point fields and pass to front-end for Order Search Details entry point
 *  12/21/23    Krishna Natarajan	CAP-45596		C1UX BE - Modified method to handle the congnitive complexity
 *  03/15/24	T Harmon			CAP-47986		Fixed login to set Module=SR when entry point is self reg
 */

package com.rrd.c1ux.api.services.settingsandprefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.catalog.CatalogMenuProcessor;
import com.rrd.c1ux.api.models.settingsandprefs.CatalogUtilityNavigationInfo;
import com.rrd.c1ux.api.models.settingsandprefs.SettingsandPrefs;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.admin.profile.dao.ProfilePreferenceDAO;
import com.rrd.custompoint.admin.profile.entity.ProfilePreference;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.admin.component.GroupUserComponentHelper;
import com.wallace.atwinxs.orderentry.admin.vo.UserGroupOrderStatusPropertiesVO;

/**
 * @author Krishna Natarajan
 *
 */
@Service
public class CatalogUtilityNavigationInfoServiceImpl extends BaseService implements CatalogUtilityNavigationInfoService  {

	// CAP-45601 Constants for Order Search Details entry point
	private static final String TEAM_ORDERS = "teamorders";
	private static final String ALL_ORDERS = "allorders";
	private static final String MY_ORDERS = "myorders";
	private static final String S = "S";
	private static final String N = "N";
	private static final String Y = "Y";
	private static final String SALES_REF_NUM = "salesrefnum";
	private static final String ORDER_SEARCH_DETAILS = "OSD";
	private static final String MODULE = "Module";
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	protected CatalogUtilityNavigationInfoServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService) {
		super(translationService, objectMapFactoryService);
	}

	/**
	 * @returns an object, list of the fields, preferences, custom references
	 */
	public CatalogUtilityNavigationInfo getCatalogUtilityNavigationInfoService(SessionContainer sc) throws AtWinXSException {
		CatalogUtilityNavigationInfo catalognavisetandprefs = new CatalogUtilityNavigationInfo();
		SettingsandPrefs setandprefs = new SettingsandPrefs();
		 // CAP-39337
		Properties translationProps = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(), SFTranslationTextConstants.CATALOG_SEARCH_NAV_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);

		boolean showVendorItemNum = sc.getApplicationSession().getAppSessionBean().showWCSSItemNumber();
		if (showVendorItemNum) {
			Map<String, String> sortOpt1 = new HashMap<>();
			sortOpt1.put(ModelConstants.LABEL, Util.nullToEmpty(translationMap.get("vendorItemNumber"))); // CAP-39337
			sortOpt1.put(ModelConstants.VALUE, ModelConstants.VENDOR_ITEM_NUMBER_NOSPACE);
			setandprefs.getSortOptions().add(sortOpt1);
		}

		Map<String, String> sortOpt2 = new HashMap<>();
		sortOpt2.put(ModelConstants.LABEL, Util.nullToEmpty(translationMap.get("itemNumber"))); // CAP-39337
		sortOpt2.put(ModelConstants.VALUE, ModelConstants.ITEM_NUMBER_NOSPACE);

		Map<String, String> sortOpt3 = new HashMap<>();
		sortOpt3.put(ModelConstants.LABEL, Util.nullToEmpty(translationMap.get("itemDescription"))); // CAP-39337
		sortOpt3.put(ModelConstants.VALUE, ModelConstants.ITEM_DESCRIPTION_NOSPACE);

		setandprefs.getSortOptions().add(sortOpt2);
		setandprefs.getSortOptions().add(sortOpt3);

		Map<String, Object> label1 = new HashMap<>();
		label1.put(ModelConstants.LABEL, "24");
		label1.put(ModelConstants.VALUE, 24);
		Map<String, Object> label2 = new HashMap<>();
		label2.put(ModelConstants.LABEL, "48");
		label2.put(ModelConstants.VALUE, 48);
		Map<String, Object> label3 = new HashMap<>();
		label3.put(ModelConstants.LABEL, "96");
		label3.put(ModelConstants.VALUE, 96);
		Map<String, Object> label4 = new HashMap<>();
		label4.put(ModelConstants.LABEL, "192");
		label4.put(ModelConstants.VALUE, 192);
		setandprefs.getDefaultSortBylabel().add(label1);
		setandprefs.getDefaultSortBylabel().add(label2);
		setandprefs.getDefaultSortBylabel().add(label3);
		setandprefs.getDefaultSortBylabel().add(label4);

		//CAP-39070
		setandprefs.getShowNumberOptions().add(label1);
		setandprefs.getShowNumberOptions().add(label2);
		setandprefs.getShowNumberOptions().add(label3);
		setandprefs.getShowNumberOptions().add(label4);

		catalognavisetandprefs.setSettingsandPrefs(setandprefs);

		User user = ObjectMapFactory.getEntityObjectMap().getEntity(User.class, null);
		Integer defaultNumItems = 24;

		setExpressShopping(sc, setandprefs, user, defaultNumItems);
		
		try {
			setandprefs = setModuleAndOtherParameters(sc, setandprefs);
		} catch (CPRPCException e) {
			e.printStackTrace();
		}

		catalognavisetandprefs.setSettingsandPrefs(setandprefs);
		return catalognavisetandprefs;

	}

	private void setExpressShopping(SessionContainer sc, SettingsandPrefs setandprefs, User user,
			Integer defaultNumItems) throws AtWinXSException {
		if (user.getProfile() != null) {
			ProfilePreference profilePreference = user.getProfile().getProfilePreferences();
			ProfilePreferenceDAO profilePrefDAO = ObjectMapFactory.getDAOObjectMap().getObject(
					ProfilePreferenceDAO.class, "ProfilePreference",
					sc.getApplicationSession().getAppSessionBean().getCustomToken());
			profilePrefDAO.populateById(profilePreference);
			setandprefs.setExpressShopping((profilePreference.getExpressShoppingPref() != null) ? profilePreference.getExpressShoppingPref(): Y);
			setandprefs.setPromptForSavingUIPages((profilePreference.getPromptForSavingCustDocPages() != null)
					? profilePreference.getPromptForSavingCustDocPages() : Y);
			setandprefs.setDefaultDisplayAs((profilePreference.getSearchResultViewPref() != null) ? profilePreference.getSearchResultViewPref()
							: "T");
			// CAP-35419 As per QA/story excepted.
			String sortBys = profilePreference.getSortBy();
			if ("Your Item".equalsIgnoreCase(sortBys)) {
				sortBys = ModelConstants.ITEM_NUMBER_NOSPACE;
			} else if ("Vendor Item".equalsIgnoreCase(sortBys)) {
				sortBys = ModelConstants.VENDOR_ITEM_NUMBER_NOSPACE;// CAP-35419 change as per Front End team Req
			} else if ("Item Description".equalsIgnoreCase(sortBys)) {
				sortBys = ModelConstants.ITEM_DESCRIPTION_NOSPACE;
			} else {
				sortBys = ModelConstants.ITEM_NUMBER_NOSPACE;
			}
			setandprefs.setDefaultSortBy(sortBys);
			setandprefs.setDefaultNoOfItems((profilePreference.getViewTypeDefaultValue() > 0)
							? profilePreference.getViewTypeDefaultValue()
							: defaultNumItems);
		} else {
			setandprefs.setExpressShopping(RouteConstants.YES_FLAG);
			setandprefs.setPromptForSavingUIPages(RouteConstants.YES_FLAG);
			setandprefs.setDefaultDisplayAs("T");
			setandprefs.setDefaultSortBy(ModelConstants.ITEM_NUMBER_NOSPACE);
			setandprefs.setDefaultNoOfItems(defaultNumItems);
		}
		

	}
	
	public SettingsandPrefs setModuleAndOtherParameters(SessionContainer sc, SettingsandPrefs setandprefs) throws CPRPCException, AtWinXSException {
		String[] splitParameters = sc.getApplicationSession().getAppSessionBean().getLoginQueryString().toString()
				.replace("{", "").replace("}", "").replace("Cat=,", "Cat=noparam,").split(",");
		HashMap<String, String> splitParametersAsMap = new HashMap<>();
		for (String params : splitParameters) {
			String[] paramArr = params.split("=");
			if (paramArr.length > 1) {
				String key = paramArr[0];
				String value = paramArr[1];
				splitParametersAsMap.put(key, value);
			}
		}
		
		Iterator<?> iterateThroughParams = splitParametersAsMap.entrySet().iterator();
		boolean isCustItemNumberAvailableAndSetModule = checkCustItemNumber(splitParametersAsMap.entrySet().iterator());
		// CAP-45596
		boolean isCatalogOrderingAndSetModule = checkCatalogOrdering(splitParametersAsMap.entrySet().iterator(), sc);
		
		// CAP-45596
		if (isCustItemNumberAvailableAndSetModule
				&& sc.getApplicationSession().getAppSessionBean().hasService(AtWinXSConstant.ORDERS_SERVICE_ID)) {
			updateSetAndPrefsForVI(splitParametersAsMap.entrySet().iterator(), setandprefs);
		}
		// CAP-45596
		if (isCatalogOrderingAndSetModule
				&& sc.getApplicationSession().getAppSessionBean().hasService(AtWinXSConstant.ORDERS_SERVICE_ID)) {
			updateSetAndPrefsForCat(iterateThroughParams, setandprefs);
		}
		
		// CAP-45601 Extract code blocks to reduce complexity
		processOrderSearchEntryPoint(sc, setandprefs, splitParametersAsMap);
		processOrderSearchDetailsEntryPoint(sc, setandprefs, splitParametersAsMap);
		
		processSelfRegistrationEntryPoint(sc, setandprefs, splitParametersAsMap);
		
		return setandprefs;
	}

	// CAP-47986
	private void processSelfRegistrationEntryPoint(SessionContainer sc, SettingsandPrefs setandprefs,
			HashMap<String, String> splitParametersAsMap) {
		// For self registration, check the AppSessionBean
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		if (appSessionBean.getEntryPoint().equalsIgnoreCase("C1UXSR"))
		{
			setandprefs.setModule("SR");
		}
	}
	
	// CAP-45601 - Set parameters for Order Search Details entry point
	private void processOrderSearchDetailsEntryPoint(SessionContainer sc, SettingsandPrefs setandprefs,
			HashMap<String, String> splitParametersAsMap) {
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		UserGroupVOKey ugKey = new UserGroupVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(),
				appSessionBean.getGroupName());
		boolean isOrderSearchDetails = isOrderSeachDetails(splitParametersAsMap.entrySet().iterator());
		if (isOrderSearchDetails && appSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID)) {
			splitParametersAsMap.forEach((key, value) -> {
				try {
					setOrderSearchDetailsParams(setandprefs, key, value,
							sc.getApplicationSession().getAppSessionBean().getCustomToken(), ugKey,
							isOrderSearchDetails);
				} catch (AtWinXSException e) {
					logger.error(this.getClass().getName() + " - " + e.getMessage(), e);
				}
			});
		}
	}
	
	public boolean isOrderSeachDetails(Iterator<?> iterateThroughParams) {
		boolean salesRefNumExists = false;
		boolean isModule = false;
		while (iterateThroughParams.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) iterateThroughParams.next();
			if (pair.getKey().toString().contains(SALES_REF_NUM) && !Util.isBlankOrNull(pair.getValue().toString())) {
				salesRefNumExists = true;
			}
			if (pair.getKey().toString().contains(MODULE) && !Util.isBlankOrNull(pair.getValue().toString())
					&& ORDER_SEARCH_DETAILS.equals(pair.getValue().toString())) {
				isModule = true;
			}
		}
		return salesRefNumExists && isModule;
	}

	private void setOrderSearchDetailsParams(SettingsandPrefs setandprefs, String key, String value,
			CustomizationToken token, UserGroupVOKey ugKey, boolean isOrderSearchDetails) throws AtWinXSException {
		if (key.contains(MODULE) && !Util.isBlankOrNull(value) && ORDER_SEARCH_DETAILS.equals(value)) {
			setandprefs.setModule(value);
			isOrderSearchDetails = true;
		}
		if (isOrderSearchDetails && key.contains(SALES_REF_NUM) && !Util.isBlankOrNull(value)) {
			setandprefs.setSalesRefNum(value);

			Order order = searchOrderBySalesRefNum(value, token);

			if (null != order.getOrderPlacedTimestamp()) {
				setandprefs.setFromDate(dateFormat.format(order.getOrderPlacedTimestamp()));
				setandprefs.setToDate(dateAfter90DaysString(order.getOrderPlacedTimestamp()));
			} else {
				setandprefs.setFromDate(dateBefore90DaysString());
				setandprefs.setToDate(currentDateString());
			}
			String orderStatusRestriction = getOrderStatusRestriction(token, ugKey);
			setOrderSearchScope(setandprefs, orderStatusRestriction);
		}
	}

	protected void setOrderSearchScope(SettingsandPrefs setandprefs, String orderStatusRestriction) {
		switch (orderStatusRestriction) {
			case Y:
				setandprefs.setScope(MY_ORDERS);
				break;
			case N:
				setandprefs.setScope(ALL_ORDERS);
				break;
			case S:
				setandprefs.setScope(TEAM_ORDERS);
				break;
			default:
				setandprefs.setScope(AtWinXSConstant.EMPTY_STRING);
		
		}
	}

	private Order searchOrderBySalesRefNum(String salesRefNum, CustomizationToken token) throws AtWinXSException {
		Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class, token);
		order.populateBySalesRefNumber(salesRefNum);
		return order;
	}
	
	private String dateAfter90DaysString(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, 90);
		Date dateAfter90Days = calendar.getTime();
		return dateFormat.format(dateAfter90Days);
	}
	
	private String currentDateString() {
		Date currentDate = new Date();
		return dateFormat.format(currentDate);
	}
	
	private String dateBefore90DaysString() {
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DAY_OF_YEAR, -90);
		Date dateBefore90Days = calendar.getTime();
		return dateFormat.format(dateBefore90Days);
	}
	
	private String getOrderStatusRestriction(CustomizationToken token, UserGroupVOKey ugKey) throws AtWinXSException {
		GroupUserComponentHelper helper = new GroupUserComponentHelper(token);
		UserGroupOrderStatusPropertiesVO status = helper.getGroupOrderStatusDetails(ugKey);
		return status.getOrderStatusRestriction();
	}
	
	// CAP-45596
	public SettingsandPrefs updateSetAndPrefsForVI(Iterator<?> iterateThroughParams, SettingsandPrefs setandprefs) {
		while (iterateThroughParams.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) iterateThroughParams.next();
			if (pair.getKey().toString().contains(SFTranslationTextConstants.MODULE)) {
				setandprefs.setModule(pair.getValue().toString());
			}
			if (pair.getKey().toString().contains(SFTranslationTextConstants.CUST_ITEM_NUM)) {
				setandprefs.setCustItemNum(pair.getValue().toString());
			}
		}
		return setandprefs;
	}
	
	// CAP-45596
	public boolean checkCatalogOrdering(Iterator<?> iterateThroughParams, SessionContainer sc)
			throws CPRPCException, AtWinXSException {
		boolean isCatalogOrdering = false;
		boolean setModule = false;
		while (iterateThroughParams.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) iterateThroughParams.next();
			if (pair.getKey().toString().contains(SFTranslationTextConstants.CATALOG)) {
				pair.setValue(splitCatParameterSearchAndMatchCatalog(
						pair.getValue().toString().equals("noparam") ? "" : pair.getValue().toString(), sc));
				isCatalogOrdering = true;
			}
			if (pair.getKey().toString().contains(SFTranslationTextConstants.MODULE)
					&& !Util.isBlankOrNull(pair.getValue().toString())
					&& pair.getValue().toString().equals(SFTranslationTextConstants.CATALOG_ORDERING)) {
				setModule = true;
			}
		}
		return isCatalogOrdering && setModule;
	}
	
	// CAP-45596
	public String getRouterLink(Collection<TreeNodeVO> calalogs, String catalog, String firslLevel, String secondLevel,
			String thirdLevel) {
		String amaconcatenator = "shopbycatalog";
		boolean checkFlag=false;
		for (TreeNodeVO calalog : calalogs) {
			if (calalog.getChildren().isEmpty()) {
				if (firslLevel.equals(calalog.getNodeName())) {
					catalog = amaconcatenator + RouteConstants.FOWARD_SLASH + calalog.getNodeID();
					checkFlag=true;
				}
			} else {
				if (firslLevel.equals(calalog.getNodeName())) {
					catalog = amaconcatenator + RouteConstants.FOWARD_SLASH + calalog.getNodeID();
					checkFlag=true;
				}
				ArrayList<TreeNodeVO> children = calalog.getChildren();
				if(checkFlag) {
				catalog = loopThroughLoopChildren(children, catalog, amaconcatenator, thirdLevel, secondLevel);
				}
			}
		}
		catalog=checkAndSetRightCatalog(calalogs,catalog,amaconcatenator);
		return catalog;
	}
	
	// CAP-45596
	public String checkAndSetRightCatalog(Collection<TreeNodeVO> calalogs, String catalog, String amaconcatenator) {
		if ((Util.isBlankOrNull(catalog) || catalog.equals("noparam") || !catalog.contains("shopbycatalog/")
				|| catalog.equals("shopbycatalog/")) && !calalogs.isEmpty()) {
			catalog = amaconcatenator + RouteConstants.FOWARD_SLASH
					+ calalogs.stream().findFirst().map(vo -> String.valueOf(vo.getNodeID())).orElse("");
		}
		return catalog;
	}
	
	// CAP-45596
	public String loopThroughInnerLoopChildren(List<TreeNodeVO> childOfChild, String catalog, String amaconcatenator,
			String thirdLevel, boolean secondLevelCheckFlag) {
		for (TreeNodeVO innerloopchildren : childOfChild) {
			if (thirdLevel.equals(innerloopchildren.getNodeName())) {
				catalog = secondLevelCheckFlag
						? amaconcatenator + RouteConstants.FOWARD_SLASH + innerloopchildren.getNodeID()
						: catalog;
			}
		}
		return catalog;
	}
	
	// CAP-45596
	public String loopThroughLoopChildren(List<TreeNodeVO> children, String catalog, String amaconcatenator,
			String thirdLevel, String secondLevel) {
		boolean secondLevelCheckFlag=false;
		for (TreeNodeVO loopchildren : children) {
			if (secondLevel.equals(loopchildren.getNodeName())) {
				catalog = amaconcatenator + RouteConstants.FOWARD_SLASH + loopchildren.getNodeID();
				secondLevelCheckFlag=true;
			}
			if (!loopchildren.getChildren().isEmpty()) {
				ArrayList<TreeNodeVO> childOfChild = loopchildren.getChildren();
				catalog = (secondLevelCheckFlag)
						? loopThroughInnerLoopChildren(childOfChild, catalog, amaconcatenator, thirdLevel,
								secondLevelCheckFlag)
						: catalog;
			}
		}
		return catalog;
	}
	
	// CAP-45596
	public SettingsandPrefs updateSetAndPrefsForCat(Iterator<?> iterateThroughParams, SettingsandPrefs setandprefs) {
		while (iterateThroughParams.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) iterateThroughParams.next();
			if (pair.getKey().toString().contains(SFTranslationTextConstants.MODULE)) {
				setandprefs.setModule(pair.getValue().toString());
			}
			if (pair.getKey().toString().contains(SFTranslationTextConstants.CATALOG)) {
				setandprefs.setCatalogRouterLink(pair.getValue().toString());
			}
		}
		return setandprefs;
	}
	
	// CAP-45596
	public String splitCatParameterSearchAndMatchCatalog(String cat, SessionContainer sc) throws CPRPCException, AtWinXSException {
		String[] catArr = cat.split("~");
		String firstLevel = "";
		String secondLevel = "";
		String thirdLevel = "";
		String catalog = "";
		if (catArr.length > 2) {
			firstLevel = catArr[0];
			secondLevel = catArr[1];
			thirdLevel = catArr[2];
		} else if (catArr.length == 2) {
			firstLevel = catArr[0];
			secondLevel = catArr[1];
		} else if (catArr.length == 1) {
			firstLevel = catArr[0];
		}
		CatalogMenuProcessor catalogMenuProcessor = new CatalogMenuProcessor();
		Collection<TreeNodeVO> calalogs = catalogMenuProcessor.retrieveCatalogMenuDetails(sc);
		catalog = getRouterLink(calalogs, cat, firstLevel, secondLevel, thirdLevel);
		return catalog;
	}
	
	// CAP-45600
	private void processOrderSearchEntryPoint(SessionContainer sc, SettingsandPrefs setandprefs,
			HashMap<String, String> splitParametersAsMap) {
		Iterator<?> iterateThroughParams = splitParametersAsMap.entrySet().iterator();
		if (sc.getApplicationSession().getAppSessionBean().hasService(AtWinXSConstant.ORDERS_SERVICE_ID)) {
			while (iterateThroughParams.hasNext()) {
				Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) iterateThroughParams.next();
				if (pair.getKey().toString().contains(SFTranslationTextConstants.MODULE)
						&& pair.getValue().equals(SFTranslationTextConstants.ORDER_SEARCH)) {
					setandprefs.setModule(pair.getValue().toString());
				}
			}
		}
	}
	
	public boolean checkCustItemNumber(Iterator<?> iterateThroughParams) {
		boolean isCustItemNumberAvailable = false;
		boolean setModule = false;
		while (iterateThroughParams.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) iterateThroughParams.next();
			if (pair.getKey().toString().contains(SFTranslationTextConstants.CUST_ITEM_NUM)
					&& !Util.isBlankOrNull(pair.getValue().toString())) {
				isCustItemNumberAvailable = true;
			}
			if (pair.getKey().toString().contains(SFTranslationTextConstants.MODULE)
					&& !Util.isBlankOrNull(pair.getValue().toString())) {
				setModule = true;
			}
		}
		return isCustItemNumberAvailable && setModule;
	}

}
