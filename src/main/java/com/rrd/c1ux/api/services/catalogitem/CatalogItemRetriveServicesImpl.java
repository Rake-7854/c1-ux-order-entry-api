/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#										Description
 * 	--------	-----------		----------------------------------------	------------------------------
 *	04/11/2022	Sakthi M		CAP-33726- Catalog Items	                Initial creation
 *  05/13/2022  Sakthi M        CAP-33726- Catalog Items                    Review Comments changes
 *  05/24/2022  Sakthi M		CAP-34332- Catalog Items with Min Max Qty	Added Min Max Qty into response
 *  06/23/2022  Sakthi M        CAP-34647- Get the UOM Acronyms Desc        Replaced Short Acronyms Desc into Full Desc
 *  07/12/2022  S Ramachandran  CAP-34884- Get Line item result          	Retrieve LineItems result for Universal Search
 *  09/05/22    M Sakthi        CAP-35437                                   Create API service to return URL for destination for continue shopping
 *  07/12/2022  S Ramachandran  CAP-34884- Get Line item result          	Retrieve LineItems result for Universal Search
 *  09/01/2022	Sumit Kumar		CAP-35732  Cache Navigation Details			Create API service to cache selected item navigation info from nav-bar calls
 *  09/08/2022  Sakthi M        CAP-35437  Catalog repeat item search       Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
 *	09/13/22	A Boomker		CAP-35958									Efficiency improvements
 *  09/27/22	Sakthi M		CAP-35949									Punchout inspect needs to set item unorderable in existing Catalog Items call for
 																		 	category navigation and search results line item for full search
 *	09/27/22	A Boomker		CAP-35610									Fixing menu check
 *	10/21/22	A Boomker		CAP-35224									Fix images starting with http
 *	03/29/23	A Boomker		CAP-39515									In getItemThumbnail(), look up large image for every single result returned
 *	04/26/23	C Codina		CAP-39333									API Change - PNA labels in multiple APIs to make/use translation text values
 *	06/27/23	C Codina 		CAP-40833									Address Low Priority Security Hotspots Identified by SonarQube - Dev Only
 *	08/30/23	A Boomker		CAP-43405									Fixing item in cart flags for customizable items
 *	09/08/23	N Caceres		CAP-42856									Featured items should return standard catalog item response
 *	09/14/23	S Ramachandran	CAP-43669									Routing Info Msg for Universal Search included in Catalog Items search results
 *  09/18/23	Krishna Natarajan	CAP-43967 								Added logic to set and get FeatureFavoriteItemData
 *  09/25/23    M Sakthi		CAP-38861									Added BaseResponse to the CatalogItemsResponse and set the success value
 *  10/06/23	N Caceres		CAP-44349       							Retrieves the HTML text assigned to the selected category
 *  10/11/23	T Harmon		CAP-44548									Modified edoc URL
 *  12/13/23	A Salcedo		CAP-45941									Updated buildCriteriaForCatalogSearch() to include category peers.
 *	01/12/24	L De Leon		CAP-46323									Updated buildCriteriaForCatalogSearch() to call buildItemAttributesSearchCriteria() to set attributes to search criteria
 *	02/08/24	Krishna Natarajan CAP-47074									Copy ItemHelper methods into this class to avoid overriding attributes criteria to perform items search
 *  02/14/24	T Harmon		CAP-46323									Added method to fix standard options.
 *  02/19/24	Krishna Natarajan	CAP-47291								Handled the standard attributes for the call to get featured items
 *  02/20/24	Krishna Natarajan	CAP-47291								Added case for the standard attributes if featured items list is empty
 *  02/20/24	Krishna Natarajan	CAP-47314								Handled the attributes filter for universal search - search results 
 *	02/28/24	Krishna Natarajan	CAP-47544								Removed the block of only setting the featured items in setFeaturedSearch()
 *	03/28/24	Krishna Natarajan	CAP-48287								Added logic in searchItems method to convert serach term to upper case and set Alternate Catalog Desc Ind
 *	04/09/24	Krishna Natarajan	CAP-48537								Set isWizard to false for getCatalog items call
 * 	04/15/24	Krishna Natarajan	CAP-48534								Added a new method getCatalogMessages
 *	04/30/24	A Salcedo			CAP-48827								Added peers to merge catalog logic.
 *	05/13/24	L De Leon			CAP-48938								Modified methods to populate delivery options list
 *	07/01/24	S Ramachandran		CAP-50502								Add locations for the add buttons when in KitTemplateMode
 */

package com.rrd.c1ux.api.services.catalogitem;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.catalog.StandardAttributesC1UX;
import com.rrd.c1ux.api.models.catalogitems.CacheResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogMessageResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogMessagesResponse;
import com.rrd.c1ux.api.models.catalogitems.CatalogNavigationCacheRequest;
import com.rrd.c1ux.api.models.catalogitems.CatalogSearchResultsResponse;
import com.rrd.c1ux.api.models.favorite.FeaturedCatalogItemsRequest;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.models.items.UniversalSearchRequest;
import com.rrd.c1ux.api.models.kittemplate.KitContainerLocation;
import com.rrd.c1ux.api.rest.catalog.CatalogItemRequest;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.CMFeaturedItemsComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.admin.entity.Message;
import com.rrd.custompoint.admin.entity.Messages;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.rrd.custompoint.catalog.entity.CategoryHTML;
import com.rrd.custompoint.catalog.search.CatalogSearchResultsCriteriaBean;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.SearchApplianceFactory.SearchAppliance;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.rrd.custompoint.gwt.catalog.entity.FeaturedSearchCriteria;
import com.rrd.custompoint.gwt.catalog.entity.StandardAttributes;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.rrd.custompoint.service.helper.ItemSearchServiceImplHelper;
import com.wallace.atwinxs.admin.locator.UserGroupComponentLocator;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.admin.vo.SiteAttributesVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.util.CatalogConstant;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.FeaturedItemsTypesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVOKey;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IFeaturedItems;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.interfaces.IUserGroup;
import com.wallace.atwinxs.items.locator.ManageItemsInterfaceLocator;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OECatalogTreeResponseBean;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

@Service
public class CatalogItemRetriveServicesImpl extends BaseOEService implements CatalogItemRetriveServices{

	// CAP-42856 Dependency injection for new wrapper object
	private final CMFeaturedItemsComponentLocatorService featuredItemsComponentLocatorService;
	
	protected CatalogItemRetriveServicesImpl(TranslationService translationService, ObjectMapFactoryService objService,
			CMFeaturedItemsComponentLocatorService featuredItemsComponentLocatorService) {
		super(translationService, objService);
		this.featuredItemsComponentLocatorService = featuredItemsComponentLocatorService;
	}

	private static final Logger logger = LoggerFactory.getLogger(CatalogItemRetriveServicesImpl.class);

	public static final String STR_BUNDLE_DEFAULT_QTY = "1";
	public static final String DIV_TAGS="<div>&nbsp;</div>";
	public static final String CACHED_PAGE_NUMBER="page";
	public static final String CACHED_SHOW_NUMBER="showNum";
	public static final String CACHED_VIEW="view";
	public static final String CACHED_SORT="sort";
	public static final String CACHED_SORT_DIRECTION="sortOrder";


	/**
	 *
	 * @param servletContext
	 * @param appSession {@link-ApplicationSession}
	 * @param volatileSession {@link-ApplicationVolatileSession}
	 * @param oeSession{@link-OrderEntrySession}
	 * @param criteria{@link-OEItemSearchCriteriaSessionBean}
	 * @param hideHiddenFeatured{@link-boolean value}
	 * @param request {@link-CatalogItemRequest}
	 * @return -This is list of {@link-CatalogItemsResponse}
	 * @throws Exception
	 */

	public CatalogItemsResponse getCatalogItems(ServletContext servletContext,
			ApplicationSession appSession, ApplicationVolatileSession volatileSession, OrderEntrySession oeSession,
			OEItemSearchCriteriaSessionBean criteria, boolean hideHiddenFeatured, CatalogItemRequest request) throws AtWinXSException
	 {
		CatalogItemsResponse catalogItemsResp=new CatalogItemsResponse();
		List<ItemThumbnailCellData> searchResults = null;

		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OEResolvedUserSettingsSessionBean oeResUserSettingsSessionBean=oeSessionBean.getUserSettings();
		buildCriteriaForCatalogSearch(criteria, request, oeSession, appSession, volatileSession); // CAP-46323
		oeSessionBean.setWizard(false);	//CAP-48537	
		searchResults = doSearch(servletContext, appSession, volatileSession, oeSession, criteria, false);

		//CAP-35437   Create API service to return URL for destination for continue shopping
		saveSession(oeSession, criteria, oeSessionBean, appSessionBean);

		catalogItemsResp.setItemThumbnailCellData(searchResults);

		catalogItemsResp.setShowOrderLinePrice(oeResUserSettingsSessionBean.isShowOrderLinePrice());
		catalogItemsResp.setShowOrderLineAvailability(oeResUserSettingsSessionBean.isShowOrderLineAvailability());
		catalogItemsResp.setPriceLineAvailabilityLbl(getVariantLabelValues(oeResUserSettingsSessionBean.isShowOrderLinePrice(),oeResUserSettingsSessionBean.isShowOrderLineAvailability(),appSessionBean));
		
		// CAP-44349 - Add HTML content assigned to the selected category ID
		catalogItemsResp.setCategoryHtml(retrieveHtmlText(oeSessionBean, appSessionBean.getCustomToken(), request.getSelectedCategoryId()));
		
		catalogItemsResp.setDeliveryOptionsList(populateDeliveryOptionsList(oeResUserSettingsSessionBean, appSessionBean));// CAP-48938
		catalogItemsResp.setSuccess(true);
		return catalogItemsResp;
	}

	/**
	 *
	 * @param sessionContainer {@link-SessionContainer}
	 * @param request {@link-CatalogNavigationCacheRequest}
	 * @return -This is list of {@link-CacheResponse}
	 * @throws Exception
	 */

	//CAP-35732  Create API service to cache selected item navigation info from nav-bar calls
	public CacheResponse cacheNavigation(SessionContainer sc, CatalogNavigationCacheRequest request) throws AtWinXSException
	 {
		Logger logger = LoggerFactory.getLogger(CatalogItemRetriveServicesImpl.class);
		CacheResponse cacheResponse=new CacheResponse();
		cacheResponse.setSuccess("N");
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		OEItemSearchCriteriaSessionBean cr=oeSessionBean.getSearchCriteriaBean();

		if(cr==null)
		{
			cr=new OEItemSearchCriteriaSessionBean();
		}

		try {
			String sortType=request.getSort();
			String showNum=request.getNumShown();
			String view=request.getView();
			String page=request.getPage();
			String sortOrder=request.getSortOrder();

			boolean foundChanges=false;

			if(!Util.isBlankOrNull(sortType) && (sortType.equals("itemNumber")||sortType.equals("vendorItemNumber")||sortType.equals("itemDescription")))
			{
				cr.setSortCode(sortType);
				oeSessionBean.setSearchCriteriaBean(cr);
				oeSession.putParameter(CACHED_SORT, sortType);
				foundChanges=true;
			}
			if(!Util.isBlankOrNull(showNum))
			{
				oeSession.putParameter(CACHED_SHOW_NUMBER,showNum);
				foundChanges=true;
			}
			if(!Util.isBlankOrNull(view))
			{
				oeSession.putParameter(CACHED_VIEW,view);
				foundChanges=true;
			}
			if(!Util.isBlankOrNull(page) && Util.isInt(page))
			{
				oeSession.putParameter(CACHED_PAGE_NUMBER,page);
				foundChanges=true;
			}
			if(!Util.isBlankOrNull(sortOrder))
			{
				oeSession.putParameter(CACHED_SORT_DIRECTION,sortOrder);
				foundChanges=true;
			}


			//store session in XST154_mod_sess table and set response Y
			if(foundChanges)
			{
				AppSessionBean appSessionBean=sc.getApplicationSession().getAppSessionBean();
				SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
				cacheResponse.setSuccess("Y");
			}

		}catch (Exception exp) {
			logger.error(Util.class.getName() + " - " + exp.getMessage(), exp);
		}

		return cacheResponse;
	 }





	/**
 	 *
 	 * @param mainSession - {@link SessionContainer}
 	 * @param request - {@link UniversalSearchRequest}
 	 * @param servletContext - {@link ServletContext}
 	 * @return - This will return CatalogItemsResponse which includes
 	 *           List of Line items(s) as ItemThumbnailCellData {@link CatalogItemsResponse}
 	 * @throws AtWinXSException
	 */

	//CAP-34884 - Retrieve LineItems result for Universal Search
	public CatalogItemsResponse getUniversalSearchLineitemResult(SessionContainer mainSession,
			ServletContext servletContext, UniversalSearchRequest request,boolean repeat) throws AtWinXSException
	 {

		AppSessionBean appSessionBean = null;
		OEOrderSessionBean oeSessionBean =  null;
		OEItemSearchCriteriaSessionBean criteria = null;

		CatalogItemsResponse catalogItemsResp=new CatalogItemsResponse();
		List<ItemThumbnailCellData> searchResults = null;

		ApplicationSession appSession = mainSession.getApplicationSession();
		ApplicationVolatileSession volatileSession =  mainSession.getApplicationVolatileSession();

		appSessionBean = mainSession.getApplicationSession().getAppSessionBean();

		CatalogSearchResultsCriteriaBean searchCriteriaBean = new CatalogSearchResultsCriteriaBean();
		OrderEntrySession oeSession = (OrderEntrySession) mainSession.getModuleSession();

		oeSessionBean = oeSession.getOESessionBean();
		//Continue Shopping
		oeSessionBean.setContinueShopping(false);

		OEResolvedUserSettingsSessionBean oeResUserSettingsSessionBean=oeSessionBean.getUserSettings();
		//CAP-47314
		if ( null != oeSession.getOESessionBean() && null != oeSession.getOESessionBean().getSearchCriteriaBean()) {
			criteria = oeSession.getOESessionBean().getSearchCriteriaBean();
		} else {
			criteria = ItemSearchServiceImplHelper.buildSearchCriteria(searchCriteriaBean, appSession, volatileSession,
					oeSession);
		}
		// CAP-35610 - a NEW universal search must not be using the selected category from before
		criteria.setSelectedCategoryId(-1);
		criteria.setUnifiedSearchCriteria(request.getTerm());
		criteria.setBuID(appSessionBean.getBuID());
		criteria.setLoginID(appSessionBean.getLoginID());
		criteria.setProfileNr(appSessionBean.getProfileNumber());
		criteria.setSearchAppliance(SearchAppliance.fromCode("ES"));
		criteria.setSiteID(appSessionBean.getSiteID());
		criteria.setUserGroup(appSessionBean.getGroupName());
		criteria.setSearchOptions(oeSessionBean.getUsrSrchOptions());
		criteria.setBrowse(false);
		criteria.setUnifiedSearch(true);
		criteria.setSearchSelectedCategory(false);
		//criteria.setUniversalSearchAutoComplete(true);
		//criteria.setSelectedCategoryId(0);

		criteria.setAliasSetting("N");
		criteria.setContainsForDescription(true);
		criteria.setDisplayUnassigned(true);
		criteria.setIncludeViewOnlyOnSearch(true);
		criteria.setSearchSelectedCategory(true);
		criteria.setNewItemsDays(30);
		
		criteria.setOrderWizard(request.isWizard());
		
		searchResults = doSearch(servletContext, appSession, volatileSession, oeSession, criteria, false);
		
		criteria.setOrderWizard(false);
		if (!request.isWizard()) {// CAP-48537
			oeSessionBean.setWizard(false);
		}
		saveSession(oeSession, criteria, oeSessionBean, appSessionBean);

		catalogItemsResp.setItemThumbnailCellData(searchResults);

		catalogItemsResp.setShowOrderLinePrice(oeResUserSettingsSessionBean.isShowOrderLinePrice());
		catalogItemsResp.setShowOrderLineAvailability(oeResUserSettingsSessionBean.isShowOrderLineAvailability());
		catalogItemsResp.setPriceLineAvailabilityLbl(getVariantLabelValues(oeResUserSettingsSessionBean.isShowOrderLinePrice(),oeResUserSettingsSessionBean.isShowOrderLineAvailability(),appSessionBean));

		//CAP-35938 changes
		catalogItemsResp.setResetPageNumber(Util.boolToY(!repeat));
		catalogItemsResp.setDeliveryOptionsList(populateDeliveryOptionsList(oeResUserSettingsSessionBean, appSessionBean)); // CAP-48938
		
		// CAP-50502
		setKitContainerLocations(mainSession,catalogItemsResp);
		
		return catalogItemsResp;
	}
	
	// CAP-50502
	public void setKitContainerLocations(SessionContainer mainSession, CatalogItemsResponse catalogItemsResponse)
			throws AtWinXSException {

		AppSessionBean asb = mainSession.getApplicationSession().getAppSessionBean();
		VolatileSessionBean vsb = mainSession.getApplicationVolatileSession().getVolatileSessionBean();

		if (vsb.isKitTemplateMode()) {

			List<KitContainerLocation> kitTemplateLocations = new ArrayList<>();
			int kitContainerLocationSize = vsb.getKitTemplateContainerLocations();
			List<String> kitContainerLocationNames = vsb.getKitTemplateContainerLocationNames();

			int locationIndex = 0;
			if (kitContainerLocationSize > 1 && !kitContainerLocationNames.isEmpty()
					&& kitContainerLocationSize == kitContainerLocationNames.size()) {

				while (kitContainerLocationNames.size() > locationIndex) {

					KitContainerLocation kitContainerLocation = new KitContainerLocation();
					kitContainerLocation.setLocationNumber(locationIndex);
					kitContainerLocation.setLocationName(translationService.processMessage(asb.getDefaultLocale(),
							asb.getCustomToken(), "add_to_" + kitContainerLocationNames.get(locationIndex).toLowerCase() + "_lbl"));
					kitTemplateLocations.add(kitContainerLocation);
					locationIndex++;
				}
			} else if (kitContainerLocationSize == 1) {

				KitContainerLocation kitContainerLocation = new KitContainerLocation();
				kitContainerLocation.setLocationNumber(locationIndex);
				kitContainerLocation.setLocationName(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), "add_to_kit_lbl"));
				kitTemplateLocations.add(kitContainerLocation);
			}
			catalogItemsResponse.setKitTemplateMode(vsb.isKitTemplateMode());
			catalogItemsResponse.setKitContainerLocations(kitTemplateLocations);
		}
	}

  /**
	 * Search for an item for a given criteria.
	 *
	 * @param servletContext
	 *            ServletContext.
	 * @param sessionID
	 *            String session ID.
	 * @param token
	 *            String token to be used.
	 * @param criteria
	 *            OEItemSearchCriteriaSessionBean criteria used for search.
	 * @param hideHiddenFeatured
	 * 			  boolean
	 * @return List<ItemThumbnailCellData> list of items.
	 * @throws CPRPCException
	 *             throws CPRPCException when RPC exception occurs.
	 * @throws AtWinXSException
	 *             throws CPRPCException when winxs exception occurs.
	 */
	//CP-8970 changed token from String to an Object
	//CP-10922 take session objects as parameters so we don't have to look them up again
	//CAP-23736 Added hideHiddenFeatured, doesn't show featured icons from XST392 with hide indicator 'Y'.
	public List<ItemThumbnailCellData> doSearch(ServletContext servletContext,
														ApplicationSession appSession,
														ApplicationVolatileSession volatileSession,
														OrderEntrySession oeSession,
														OEItemSearchCriteriaSessionBean criteria,
														boolean hideHiddenFeatured) throws AtWinXSException
	{

		List<ItemThumbnailCellData> searchResults = null;
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();
		//CP-10922
		criteria.setSearchOptions(oeSessionBean.getUsrSrchOptions());
		
		// CAP-46323 TH - New method to set standard attributes
		buildStandardAttributesSearchCriteria(criteria, oeSession);
		
		// Put wizard information in there if there is wizard information		
		if (oeSessionBean.getOrderWizardSearchAttributes() != null && criteria.isOrderWizard())
		{
			if (criteria.getAttributesCriteria() == null)
			{
				criteria.setAttributesCriteria(new HashMap<Integer, ArrayList<Integer>>());
			}
			criteria.getAttributesCriteria().putAll(oeSessionBean.getOrderWizardSearchAttributes());			
		}

		//CP-9767 removed variable declarations
		//CP-10769 NMB Removed unnecessary null checks.
		try
		{
			searchResults = new ArrayList<ItemThumbnailCellData>();

			CatalogLineVO[] items = null;
			//CP-9767 changed method signature
			items = searchItems(criteria, appSessionBean, volatileSessionBean, oeSessionBean);

			SimpleDateFormat formatter = new SimpleDateFormat(OrderEntryConstants.CP_SHIPPING_LIST_DATE_FORMAT);
			logger.info("About to call populateSearchResults " + formatter.format(new java.util.Date()));

			//CP-10341 added criteria parameter
			searchResults = populateSearchResults(items, appSession, servletContext, oeSession, volatileSession, criteria, hideHiddenFeatured);
			logger.info("Finished populateSearchResults " + formatter.format(new java.util.Date()));
			
		}
		catch (AtWinXSException e)
		{
			//CAP-40833 - Replaced printStrackTrace with logger
			logger.error("Exception when processing searchResults" + e.getMessage(), e);
		}
		return searchResults;
	}

	// CAP-46323
	protected void buildStandardAttributesSearchCriteria(OEItemSearchCriteriaSessionBean criteria, OrderEntrySession oeSession)
	{				
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		if(criteria.isAdvancedSearch())
		{
			criteria.setAdvancedSearch(false);
		}
		
		boolean newItems = false;
		if (criteria.getStandardAttributes() != null && criteria.getStandardAttributes().isFilterNewItems())
		{
			newItems = true;
		}
		criteria.setNewItemsOnly(newItems); // CP-8466 JT 10.21.13
		if (newItems)
		{
			criteria.setNewItemsDays(oeSessionBean.getUserSettings().getNewItemsDays());
		}

		boolean isFave = false;
		if (criteria.getStandardAttributes() != null && criteria.getStandardAttributes().isFilterFavorites())
		{
			isFave = true;
		}
		criteria.setFaveItemsOnly(isFave);

		ArrayList<String> featuredItemNames = new ArrayList<String>();
		StringBuilder featuredItemTypes = new StringBuilder();
		boolean isFeaturedItemsOnly = false;

		if (oeSessionBean.getCatalogSearchFeaturesFavoritesBean().getFeaturedItemsDefined() != null
				&& oeSessionBean.getCatalogSearchFeaturesFavoritesBean().getFeaturedItemsDefined().length > 0)
		{
			//CP-10769 RAR - Always clear the Featured Item Search.
			criteria.getFeaturedItemsSearch().clear();
			
			AlertCounts[] alerts = oeSessionBean.getCatalogSearchFeaturesFavoritesBean().getFeaturedItemsDefined();
			if (criteria.getStandardAttributes() != null)
			{
				Collection<FeaturedSearchCriteria> featuredSearchCriteria = criteria.getStandardAttributes().getFeaturedSearchCriteria();
				// CAP-14488 This loop would only show terms with alerts on them but should show all from advanced search
				for (FeaturedSearchCriteria featCriteria : featuredSearchCriteria)
				{
					boolean isChecked = false;
	
					for (AlertCounts alertCnt : alerts)
					{
						isChecked = isChecked || (featCriteria.getTypeID() == Util.safeStringToDefaultInt(alertCnt.getAlertCd(), AtWinXSConstant.INVALID_ID) && featCriteria.isSelected());
					}
					// CAP-14488 - still need to show it if it is not selected
					criteria.getFeaturedItemsSearch().put(String.valueOf(featCriteria.getTypeID()), isChecked);
					if (isChecked)
					{
						featuredItemTypes.append(featuredItemTypes.length() > 0 ? ", " : "").append(featCriteria.getTypeID());
						featuredItemNames.add(featCriteria.getLabel());
						isFeaturedItemsOnly = true;
					}
				}
			}
			else
			{
				featuredItemTypes = new StringBuilder();
				featuredItemNames.clear();
				isFeaturedItemsOnly = false;
			}
			
		}
		criteria.setFeaturedItemTypes(featuredItemTypes.toString());
		criteria.setFeaturedItemnames(featuredItemNames);
		criteria.setFeaturedItemsOnly(isFeaturedItemsOnly);
	}
	

	//CAP-23736 Added hideHiddenFeatured, doesn't show featured icons from XST392 with hide indicator 'Y'.
		/**
		 * Transform array of CatalogLineVO to List of ItemThumbnailCellData to
		 *
		 * @param items
		 *            CatalogLineVO[] array of item.
		 * @param appSession
		 *            ApplicationSession.
		 * @param servletContext
		 *            ServletContext.
		 * @param oeSession
		 * 			  OrderEntrySession
		 * @param volatileSession
		 *			  ApplicationVolatileSession
		 * @param criteria
		 * 			  OEItemSearchCriteriaSessionBean
		 * @param hideHiddenFeatured
		 * 			  boolean
		 * @return List<ItemThumbnailCellData> list of ItemThumbnailCellData.
		 * @throws AtWinXSException
		 *             throws AtWinXSException when exception occurs.
		 */
		public  List<ItemThumbnailCellData> populateSearchResults(CatalogLineVO[] items, ApplicationSession appSession,
				//CP-10341 added criteria parameter
				ServletContext servletContext, OrderEntrySession oeSession, ApplicationVolatileSession volatileSession, OEItemSearchCriteriaSessionBean criteria,
				boolean hideHiddenFeatured) throws AtWinXSException
		{
			List<ItemThumbnailCellData> catalogSearchResults = new ArrayList<ItemThumbnailCellData>();
			SearchResult thumb = null;

			//CAP-35949-Punchout inspect changes
			PunchoutSessionBean punchoutSessionBean=appSession.getPunchoutSessionBean();

			//CP 9054 PDN Additional Routing messages
			//CP-8970 changed token from String to an Object
			String routingShippingMethodMsg = AtWinXSConstant.EMPTY_STRING; //.nullToEmpty(TranslationTextTag.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(), TranslationTextConstants.ROUTING_SHIPPING_METHOD_MSG));
			String routingExceedAmountMsg = AtWinXSConstant.EMPTY_STRING; //Util.nullToEmpty(TranslationTextTag.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(), TranslationTextConstants.ROUTING_EXCEED_AMOUNT_MSG));
			String routingAlwaysRouteOrders = AtWinXSConstant.EMPTY_STRING; //Util.nullToEmpty(TranslationTextTag.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(), TranslationTextConstants.ROUTING_ORDERS_ALWAYS_MSG));
			String routingMessageReasonsText = AtWinXSConstant.EMPTY_STRING; //Util.nullToEmpty(TranslationTextTag.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(), TranslationTextConstants.ROUTING_REASONS_MSG));

			if (items != null && items.length > 0)
			{
				int faveCatLineNum = 0;

				OEResolvedUserSettingsSessionBean userSettings = oeSession.getOESessionBean().getUserSettings();
				//CAP-2476 check if they have featured items before trying to load all of the featured item data.
				boolean hasFeaturedItems = ItemHelper.hasFeaturedItems(appSession.getAppSessionBean(), volatileSession.getVolatileSessionBean(), hideHiddenFeatured);


				//CAP-15083 SRN Set the allocation field label and values if Icon plus view enabled
				if(appSession.getAppSessionBean().isIconPlusViewEnabled())
				{
					userSettings.setAdditionalIconFldLabel();
					userSettings.setAdditonalIconValues();
				}

				//CP-13132 look up order lines once, so we don't have to do it for every search result
				OECatalogAssembler assembler = new OECatalogAssembler(appSession.getAppSessionBean().getCustomToken(), appSession.getAppSessionBean().getDefaultLocale());
				Collection<OrderLineVO> orderLines = assembler.getOrderLines(null != volatileSession.getVolatileSessionBean().getOrderId() ? volatileSession.getVolatileSessionBean().getOrderId().intValue() : AtWinXSConstant.INVALID_ID);

				// CAP-2589 - make sure that we pull info from existing kit session and not reload here
				KitSession kitSession = null;
				if (volatileSession.getVolatileSessionBean().isKitTemplateMode())
				{
					try
					{ // if we have no kit session, we should not be in kit template mode
						kitSession = (KitSession) SessionHandler.loadSession(appSession.getAppSessionBean().getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
					}
					catch(AtWinXSWrpException eofex)
					{
						//CAP-18657 Replaced printstacktrace() call with Logger
						logger.error(Util.class.getName() + " - " + eofex.getMessage(), eofex);
						throw eofex;
					}
				}
				IManageItemsInterface itemInterface = ManageItemsInterfaceLocator.locate(appSession.getAppSessionBean().getCustomToken());
				ItemRptVO itemVO = null;
				ICatalog catalogComp = CMCatalogComponentLocator.locate(appSession.getAppSessionBean().getCustomToken());

				for (CatalogLineVO vo : items)
				{
					thumb = new SearchResult();
					// CP-10235 EZL Moved the block of code in a method to make it reusable
					//CP-10341 added criteria parameter
					//CP-8970 removed token parameter
					//CAP-2476 pass the hasFeaturedItems flag
					getItemThumbnail(thumb, userSettings, appSession, vo, servletContext, volatileSession, criteria, routingShippingMethodMsg, routingExceedAmountMsg,
							routingAlwaysRouteOrders, hasFeaturedItems, orderLines, catalogComp, itemInterface, kitSession, oeSession.getOESessionBean());
					thumb.setRenderLocation("searchResults"); // CP-10066

					if (null != thumb)
					{
						thumb.setRoutingMessageReasonsText(routingMessageReasonsText);
					}

					//CP-11269 RAR - Set a unique ID for each of the Favorite Items so UI can
					//update the right item.
					if(criteria.isFaveItemsOnly() && vo.getCatalogLineNum() == -1)
					{
						thumb.setCatalogLineNumber(--faveCatLineNum);
					}

					//CAP-35949 - Punchout inspect changes
					boolean isItemOrdarable=vo.isItemOrderable();
					if(punchoutSessionBean!=null && punchoutSessionBean.getOperation().equals(OrderEntryConstants.PUNCHOUT_OPERATION_INSPECT)) {
						isItemOrdarable=false;
					}

					thumb.setItemOrderable(isItemOrdarable);

					//CAP-43669
					thumb.setRoutingBadge(retrieveRoutingInformationBadge(thumb, appSession.getAppSessionBean(), 
							userSettings, oeSession.getOESessionBean()));

					catalogSearchResults.add(thumb);
				}

			}
			return catalogSearchResults;
		}



	/**
	 * This method will search for an item for the given catalog or criteria.
	 *
	 * @param criteria
	 *            OEItemSearchCriteriaSessionBean.
	 * @param token
	 *            String token to be used.
	 * @param isMobileSession
	 *            boolean indicator if from mobile session.
	 * @param isKitTempalteMode
	 *            boolean indicator if mode is kit tremplate.
	 * @param isAllowCustomDocumentsInd
	 *            boolean indicator if allow custom documents.
	 * @param primarySiteAttr
	 *            int primary site attribute.
	 * @param secondarySiteAttr
	 *            int secondary site attribute.
	 * @param isBatchVDPMode
	 *            boolean indicator if from BatchVDPMode.
	 * @param isWizard
	 *            boolean indicator if from wizard.
	 * @return CatalogLineVO[] array of Catalog lines.
	 * @throws AtWinXSException
	 *             throws AtWinXSException when exception occurs.
	 */
	//CP-9767 Changed method signature
	public CatalogLineVO[] searchItems(OEItemSearchCriteriaSessionBean criteria, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
												OEOrderSessionBean oeSessionBean) throws AtWinXSException
	{

		//CP-9767 moved local vars to inside this method
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		int primaryAttribute = userSettings.getPrimaryAttribute();
		int secondaryAttribute = userSettings.getSecondaryAttribute();
		boolean isAllowCustomDocuments = userSettings.isAllowCustomDocumentsInd();
		boolean isMobileSession = appSessionBean.isMobileSession();
		boolean isKitTemplateMode = volatileSessionBean.isKitTemplateMode();
		boolean isBatchVDPMode = volatileSessionBean.isBatchVDPMode();
		boolean isWizard = criteria.isOrderWizard();

		SimpleDateFormat formatter = new SimpleDateFormat(OrderEntryConstants.CP_SHIPPING_LIST_DATE_FORMAT);

		ICatalog cat = objectMapFactoryService.getComponentObjectMap().getObject(ICatalog.class, appSessionBean.getCustomToken());
		logger.info("About to call Manage catalogs searchCatalog " + formatter.format(new java.util.Date()));

		//CAP-48287
		criteria.setUnifiedSearchCriteria(criteria.getUnifiedSearchCriteria().toUpperCase());
		criteria.setAlternateCatalogDescInd(userSettings.isAlternateCatalogDescInd());
		
		CatalogLineVO[] items = cat.searchCatalog(criteria, isMobileSession, isKitTemplateMode, isAllowCustomDocuments, primaryAttribute,
				secondaryAttribute, isBatchVDPMode, isWizard);
		logger.info("Finish to call Manage catalogs searchCatalog " + formatter.format(new java.util.Date()));

		//CP-11235 filter within category when browsing only
		OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		if(criteria.isBrowse() && criteria.isSearchSelectedCategory() &&
				(criteria.isFaveItemsOnly() || criteria.isNewItemsOnly() || criteria.isFeaturedItemsOnly()))
		{
			items = assembler.applyCategoryCriteria(criteria.getSelectedCategoryId(), criteria.getSelectedCategoryPeers(), items); //CAP-4179
			logger.info("AppliedCategoryCriteria finished at " + formatter.format(new java.util.Date()));
		}

		//CP-9767 Filter the results here if neede before returning and doing extra lookups for items that will be filtered.
		if(!cat.isGSACompliantSearch(criteria))
		{
			//CP-11000 removed "applyAttrCriteria variable and just used "criteria" object
			boolean isAttrUdfFilter = assembler.isAttrUdfFilter(criteria.getAttributesSearchCriteria(), criteria.getUdfCriteria());
			//CP-11000 changed parameters
			items = assembler.filterCatalogSearchWithAttrUDF(oeSessionBean, isWizard?-1:primaryAttribute, criteria, isAttrUdfFilter, items, false);
			logger.info("filterCatalogSearchWithAttrUDF finished at " + formatter.format(new java.util.Date()));
		}

		//CP-8983 remove duplicate search results
		OECatalogTreeResponseBean treeBean = new OECatalogTreeResponseBean();
		TreeNodeVO searchedNode = new TreeNodeVO();
		searchedNode.setItems(items);
		searchedNode.setNodeID(-1);

		Collection<TreeNodeVO> categories = new ArrayList<TreeNodeVO>(1);
		categories.add(searchedNode);
		logger.info("About to set categories on treebean at " + formatter.format(new java.util.Date()));

		treeBean.setCategories(categories, true);

		items = treeBean.getSelectedNode().getItems();

		return items;
	}

	/**
	 * Get the item thumbnail of an item.
	 *
	 * @param SearchResult thumb
	 * @param OEResolvedUserSettingsSessionBean userSettings
	 * @param ApplicationSession appSession
	 * @param CatalogLineVO vo
	 * @param ServletContext servletContext
	 * @param ApplicationVolatileSession volatileSession
	 * @param  String token
	 * @return SearchResult
	 * @throws AtWinXSException
	 */
	public SearchResult getItemThumbnail(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings, ApplicationSession appSession, CatalogLineVO vo,
			ServletContext servletContext,
			// CP-10341 added criteria parameter
			ApplicationVolatileSession volatileSession, OEItemSearchCriteriaSessionBean criteria, String routingShippingMethodMsg, String routingExceedAmountMsg,
			//CAP-2476 added hasFeaturedItems
			String routingAlwaysRouteOrders, boolean hasFeaturedItems, Collection<OrderLineVO> orderLines, ICatalog catalogComp,
			IManageItemsInterface itemInterface, KitSession kitSession,
			OEOrderSessionBean oeOrderSessionBean
			)	throws AtWinXSException
	{
		//CP-12605 - Refactor codes to reuse the new isExternalESP method in CustomDocumentItem class.
		//CP-13132 changed to make sure the item is a cust doc before checking if it is External ESP to prevent unnecessary DB calls.  Also, store result for future use to prevent repetitive DB calls
		// CAP-35958 - at this time, there are no email service provider items in CP, much less C1UX
		//	CustomDocumentItem cdItem = ObjectMapFactory.getEntityObjectMap().getEntity(CustomDocumentItem.class, appSession.getAppSessionBean().getCustomToken());
		boolean isExternalESP = false; //ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(vo.getClassification()) && cdItem.isExternalESP(appSession.getAppSessionBean().getSiteID(), vo.getItemNum(), vo.getWcsItemNum());
		thumb.setItemNumber(vo.getItemNum());
		thumb.setVendorItemNumber(vo.getWcsItemNum());
		//CP-9023 Start
		String description = doAlternateDescriptionProcessing(vo);
		thumb.setItemDescription(description);
		//CP-9023 End
		//CP-8914 RAR - Update codes to display Medium Image instead of the Thumbnail Image.
		String medImage = AtWinXSConstant.EMPTY_STRING;
		String voMediumImg = vo.getItemMedImgLocURL();
		// CAP-27678 - add large image
		String largeImage = AtWinXSConstant.EMPTY_STRING;
// CAP-39515 - need to look up large image for every single item now
		ItemImagesVOKey imageKey = new ItemImagesVOKey(appSession.getAppSessionBean().getSiteID(),
					Util.htmlUnencodeQuotes(Util.nullToEmpty(vo.getItemNum())),
					Util.htmlUnencodeQuotes(Util.nullToEmpty(vo.getWcsItemNum())));
		ItemImagesVO imageVO = catalogComp.getImagesForItem(imageKey);
		if (imageVO != null)
		{
			ItemImagesVOFilter image = new ItemImagesVOFilter(imageVO);
			medImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
			largeImage = image.getQualifiedItemFullImgLocURL(appSession.getAppSessionBean());

			// set medium to no image image if this is blank
			if (Util.isBlankOrNull(medImage))
			{
				medImage = "assets/images/No-image-106x106.svg";
			}
		}
		else
		{
			medImage = "assets/images/No-image-106x106.svg";
		}


		thumb.setLargeImageURL(largeImage);
		thumb.setCartImgURL(medImage);
		thumb.setImgURL(medImage);
		thumb.setCatalogLineNumber(vo.getCatalogLineNum());
		thumb.setSelectedUom("");
		thumb.setPrimaryText(description);//CP-9023
		thumb.setSecondaryText(vo.getItemNum());
		thumb.setHref("#");

		//CP-8466 RAR - Retrieve and set the Item UOM Options.
		//CP-8942	RAR - Pass the deliveryOptions to determine if we need to build the UOM Options.
		//CP-9486 RAR - Pass the mergeCode to handle Mail Merge Items.
		//JW - Added view only to the logic to determine to show UOM options or not.
		//CP-8970 removed token parameter
		//CP-13132 added isExternalESP parameter

		// CAP=34647 This method used to replace existing UOM Acronyms to full Acronyms description
		ItemUtility.setItemUOMOptions(thumb, appSession.getAppSessionBean(), vo.getClassification(), isExternalESP,
				volatileSession.getVolatileSessionBean(), vo.getWcsItemNum(), kitSession, itemInterface,
				userSettings, vo.getMergeOptionCode());

		// CAP-34332-Min.Max integration with CataloglineItems
/*		if (!Util.isBlankOrNull(vo.getWcsItemNum())) {
			itemVO = itemInterface.getWCSSItem(vo.getWcsItemNum());
			thumb.setItemMininumOrderQty(itemVO.getItemMininumOrderQty());
			thumb.setMaxinumOrderQty(itemVO.getMaxinumOrderQty());
		} */

		//thumb.setUomOptions(ItemHelper.getItemUOMOptions(vo.getWcsItemNum(), vo.getItemNum(), vo.getClassification(), userSettings, volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean(), vo.getDeliveryOption(), vo.getMergeOptionCode(), vo.isViewOnlyFlag(), isExternalESP));
		//RAR - Set other needed information about the item.
		thumb.setAllowFavorites(userSettings.isAllowUserFavoritesInd());
		//CP-8970 changed token from String to an Object
		//CP-13132 Only check if the item is a Favorite if the user is allowed to do favorites
		if(userSettings.isAllowUserFavoritesInd())
		{
			thumb.setFavorite(ItemHelper.isItemFavorite(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getProfileNumber(), vo.getWcsItemNum(), vo.getItemNum(), appSession.getAppSessionBean().getCustomToken()));
		}

		//CP-13132 Don't make DB call to check if the user has the service.  Use the service list in AppSessionBean
//		thumb.setCheckInventoryEnabled(ItemHelper.isCanCheckInventory(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getGroupName(), appSession.getAppSessionBean().getCustomToken()));
//		thumb.setManageItemsEnabled(ItemHelper.isItemManager(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getGroupName(), appSession.getAppSessionBean().getCustomToken()));
		thumb.setCheckInventoryEnabled(appSession.getAppSessionBean().hasService(AtWinXSConstant.INVENTORY_SERVICE_ID));
		thumb.setManageItemsEnabled(appSession.getAppSessionBean().hasService(AtWinXSConstant.ITEM_SERVICE_ID));

		thumb.setEdocEnabled(vo.isEDocFlag());
		//CP-9607 RAR - Fixed issue on the eDoc URL.
		// CAP-44548 TH - Added code to get appropriate edoc url
		String edocUrl = ItemUtility.getEdocUrl(vo.getItemNum(), vo.getWcsItemNum(), true, appSession.getAppSessionBean().getQualifiedImageSrc(vo.getEdocUrl(), AtWinXSConstant.ITEM_EDOC), vo.getEdocUrl(), appSession.getAppSessionBean().getSiteLoginID());		
		thumb.setEdocUrl(edocUrl);
		
		thumb.setFileDeliveryOptionCd(vo.getDeliveryOption());
		thumb.setSpecialItemTypeCd(String.valueOf(vo.getSpecialItemType()));
		thumb.setItemAlwaysRoute(vo.alwaysRouteFlagLineReview());
		thumb.setItemRouteQuantity(vo.getApprovalLimtQtyLineReview());
		//CP-8994 set showVendorItemNum properly
		thumb.setShowVendorItemNum(appSession.getAppSessionBean().isShowWCSSItemNumber());
		thumb.setItemClassification(vo.getClassification());

		//CAP-2476 only look up featured item data if they have featured items
		// CAP-35958 - C1UX only has plans for favorites not featured
     	if(hasFeaturedItems)
		{
			thumb.setFeatureFavoriteItemData(ItemUtility.buildFeatureMap(vo, appSession.getAppSessionBean()));//CAP-43967
		}

		thumb.setItemOrderable(vo.isItemOrderable());
		thumb.setViewOnlyFlag(vo.isViewOnlyFlag());
		thumb.setInvalidItemFlag(vo.isInvalidItemFlag());
		thumb.setAlwaysRoute((vo.isAlwaysRouteFlag() && (userSettings.getAssignedApprovalQueue() > 0)));
		thumb.setRouteQuantity((int) vo.getApprovalLimitQty());
		thumb.setAllowEFD(userSettings.isAllowEFD());
		thumb.setAllowPrintOverride(userSettings.isAllowPrintOverride());
		thumb.setShowRAInfo(((userSettings.isShowRoutingInfo() && userSettings.isRoutingAvailable() && userSettings.isSubjToRnA()) && !volatileSession.getVolatileSessionBean().isKitTemplateMode()));

		thumb.setKitContainerLocations(volatileSession.getVolatileSessionBean().getKitTemplateContainerLocations());
		// CAP-15146 - add location names
		thumb.setKitContainerLocationNames(volatileSession.getVolatileSessionBean().getKitTemplateContainerLocationNames());
		thumb.setKitTemplateMode(volatileSession.getVolatileSessionBean().isKitTemplateMode());

		thumb.setAlwaysRouteOrders(userSettings.isAlwaysRouteOrders());

		boolean isCustomizableItem = isCustomizableItem(vo.getClassification(), vo.getWcsItemNum(), appSession.getAppSessionBean(), appSession.getPunchoutSessionBean());
		if(volatileSession.getVolatileSessionBean().isKitTemplateMode())
		{
			thumb.setAllowDupCustDoc(kitSession.getAdminBean().isAllowDuplicateCustDocs()); // CAP-2589
			thumb.setItemInKitInd(ItemHelper.getItemInKitIndicator(volatileSession.getVolatileSessionBean().getSelectedKitComponents(), vo.getItemNum(), vo.getWcsItemNum(), appSession.getAppSessionBean().getCustomToken(), appSession.getAppSessionBean().getDefaultLocale()));

			thumb.setItemQuantity(ItemHelper.getWildCardDefaultQuantity(vo.getWcsItemNum(), appSession.getAppSessionBean(), volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean().getCustomToken()));
			thumb.setDisplayQuantityAsText((!Util.isBlankOrNull(thumb.getItemQuantity()) && !isCustomizableItem));
		}
		else
		{
			//CP-9225 use admin default quantity
			thumb.setItemQuantity(Integer.toString(appSession.getAppSessionBean().getSiteDefaultQty()));
			//CP-13132 changed parameters to ItemHelper.isItemInCart
			// CAP-43405 - need to set customizable items "item in cart" flag differently
			thumb.setItemInCart(!isCustomizableItem && catalogComp.isInCart(vo.getItemNum(), vo.getWcsItemNum(), orderLines));

			//CP-12787 - Set value of DisplayQuantityAsText only if Xert item.
			//CP-12605 - Reuse the new isExternalESP method in CustomDocumentItem class.
			//CP-13132 use local variable for isExternalESP so we don't make repetitive DB calls
			if (isExternalESP)
			{
				thumb.setDisplayQuantityAsText(isExternalESP);
			}

			//CP-13104 - Set Quantity to 1 for Bundle Item
			if (ItemConstants.ITEM_CLASS_BUNDLE.equals(Util.nullToEmpty(vo.getClassification())))
			{
				thumb.setItemQuantity(STR_BUNDLE_DEFAULT_QTY);
				thumb.setDisplayQuantityAsText(true);
			}
		}

		thumb.setShowAvailability(userSettings.isShowOrderLineAvailability());
		thumb.setShowPrice(userSettings.isShowOrderLinePrice());

		//CP-9054
		thumb.setRouteDollarAmount(userSettings.getRouteDollarAmount());
		thumb.setRouteOnShipMethodChange(userSettings.isRouteOnShipMethodChange());

		String routingAmtLevel= Util.getStringFromCurrency(userSettings.getRouteDollarAmount(),
				appSession.getAppSessionBean().getCurrencyLocale(), appSession.getAppSessionBean().getApplyExchangeRate()).getAmountText();

		thumb.setRouteDollarAmountText(routingExceedAmountMsg + " " + routingAmtLevel + ".");
		thumb.setRouteOnShipMethodChangeText(routingShippingMethodMsg);

		thumb.setAlwaysRouteOrders(userSettings.isAlwaysRouteOrders());
		thumb.setAlwaysRouteOrdersText(routingAlwaysRouteOrders);
		thumb.setDisplayOrder(vo.getDisplayOrder());//CP-11027

		//CP-9635 set replacement item data
		thumb.setReplacementItemNumber(vo.getReplaceItemNum());
		thumb.setUnorderableDate(Util.getStringFromDate(vo.getNotOrderableDt(), appSession.getAppSessionBean().getDefaultLocale()));
		thumb.setUnorderableReason(vo.getReasonNotOrderableTxt());
		thumb.setShowAdditionalItemStatInfo(userSettings.isShowAddlItemStatInfo());

		// Check if there's primary attribute assigned
		thumb.setHasPrimaryAttrib(false);
		if(userSettings.getPrimaryAttribute() > 0)
		{
			thumb.setHasPrimaryAttrib(true);

			//CAP-2774, CAP-3248
			//only populate the attribute value if there is a primary attribute selected in the user settings
			String value = vo.getPrimaryCategorization();
			thumb.setCategorizationAttribVal(value);
		}
		// Check if the page is in Catalog page
		thumb.setDisplayAttrVal(false);
		if(criteria != null)
		{
			if(criteria.isFaveItemsOnly() || criteria.isFeaturedItemsOnly())
			{
				thumb.setDisplayAttrVal(true);
			}
		}
		else
		{
			thumb.setDisplayAttrVal(true);
		}

		// CAP-16158 - attempt to populate custom columns if any exist
/*		catalogComp.getCustomColumnSearchResults(thumb.getVendorItemNumber(), thumb.getItemNumber(), appSession.getAppSessionBean().getSiteID(),
				appSession.getAppSessionBean().getCorporateNumber(), vo, thumb.getCustomColumnData());

		//CAP-15083 SRN - Attempt to populate the additional field name and value if icon plus view enabled
		if(appSession.getAppSessionBean().isIconPlusViewEnabled())
		{
			setIconPlusFldAndVal(thumb, userSettings);
		}

		//CAP-15939 JBS unit price hard stop
		processItemPriceLimit(thumb, userSettings.getItemPrcLimitPerOrder(), vo.getUnitPriceAmt());	 */
	
		//CAP-43670
		thumb.setRoutingBadge(retrieveRoutingInformationBadge(thumb, appSession.getAppSessionBean(), userSettings, oeOrderSessionBean));

		return thumb;
	}

	//CP-9023 Start
		/**
		 *
		 * @param vo Information about the Catalog line
		 * @return The catalog line item description with/without alt desc.
		 * @throws AtWinXSException
		 */
		public String doAlternateDescriptionProcessing(CatalogLineVO vo) throws AtWinXSException
		{
			//CP-10326 removed HTML encoding of quotes.
			String alternateDesc = Util.nullToEmpty(vo.getAltCatData().getAlternateCatalogDesc());
			String altDescDispType = vo.getAltCatData().getAlternateCatalogDescDisplayType();

			String descriptionWithAltDesc = "";

			if (alternateDesc.length() > 0
					&& (altDescDispType
							.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_APPEND) || altDescDispType
							.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_REPLACE)))
			{

				//Append to or replace the description
				if(altDescDispType.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_APPEND))
				{
					//CAP-35671 - Handling/ Rendering of HTML tags in catalog line view descriptions
					descriptionWithAltDesc = vo.getDescription() + " - " + alternateDesc; // CAP-2237
				}
				else if(altDescDispType.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_REPLACE))
				{
					//CAP-35671 - Handling/ Rendering of HTML tags in catalog line view descriptions
						descriptionWithAltDesc = alternateDesc; // CAP-2237
				}
			}
			else
			{
				//CAP-35671 - Handling/ Rendering of HTML tags in catalog line view descriptions
				descriptionWithAltDesc = vo.getDescription();
			}

			return descriptionWithAltDesc;
		}

		//CAP-15083 SRN
		/**
		 * Method to set the Icon Plus (UDF) field label and Value
		 * @param thumb
		 * @param userSettings
		 */
		public void setIconPlusFldAndVal(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings)
		{
			String additonalIconVal = Util.nullToEmpty((userSettings.getAdditonalIconValues().get(thumb.getItemNumber())));
			thumb.setAdditionalFieldValue(Util.isBlankOrNull(additonalIconVal)? OrderAdminConstants.NOT_AVAILABLE : additonalIconVal);
			thumb.setAdditionalFieldLabel(userSettings.getAdditionalIconFldLabel());
		}

		//CAP-15939 JBS Logic for item price hard stop
		public void processItemPriceLimit(SearchResult thumb, double itemPrcLimitPerOrder, double unitPriceAmount)
		{
			if(itemPrcLimitPerOrder > 0 && thumb.isItemOrderable())
			{
				thumb.setItemOrderable(unitPriceAmount <= itemPrcLimitPerOrder);
			}
		}

		// CAP-2237 - added method to replace line breaks only in html
		public String removeLineBreaks(String altDescDisp)
		{
			int firstLeftLT = (altDescDisp.indexOf("<"));
			if ((firstLeftLT > -1) && (altDescDisp.indexOf(">") > firstLeftLT))
			{
				altDescDisp = altDescDisp.replace("<br>", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<br >", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<br/>", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<br />", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<BR>", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<BR >", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<BR/>", DIV_TAGS);
				altDescDisp = altDescDisp.replace("<BR />", DIV_TAGS);
			}
			return altDescDisp;
		}

		//CAP-39333: C1UX BE - API Change - PNA labels in multiple APIs to make/use translation text values
		public String getVariantLabelValues(boolean priceValue,boolean availabilityValue,AppSessionBean asb) throws AtWinXSException {
			String res="";

			if(priceValue && availabilityValue) {
				res= getTranslation(asb, SFTranslationTextConstants.CHECK_PRC_AVL_LBL_VAL, SFTranslationTextConstants.PRICING_AND_AVAIL_VAL);
			}
			if(priceValue && !availabilityValue) {
				res= getTranslation(asb, SFTranslationTextConstants.CHECK_PRC_LBL_VAL, SFTranslationTextConstants.PRICING_VAL);
			}
			if(!priceValue && availabilityValue) {
				res=getTranslation(asb, SFTranslationTextConstants.CHECK_AVL_LBL_VAL, SFTranslationTextConstants.AVAIL_VAL);
			}
			return res;
		}

//CAP-35437-Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
	public CatalogItemsResponse repeatItemSearch(SessionContainer mainSession,ServletContext servletContext) throws AtWinXSException {
		UniversalSearchRequest req=new UniversalSearchRequest();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) mainSession.getModuleSession()).getOESessionBean();
		req.setTerm(oeOrderSessionBean.getSearchCriteriaBean().getUnifiedSearchCriteria());
		return getUniversalSearchLineitemResult(mainSession,servletContext,req,true);
	}

	public CatalogItemsResponse getCatalogItemsRepeat(SessionContainer sc,ServletContext servletContext,OEItemSearchCriteriaSessionBean criteria) throws AtWinXSException {
		CatalogItemRequest request=new CatalogItemRequest();
		ApplicationSession appSession = sc.getApplicationSession();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		request.setSelectedCategoryId(oeOrderSessionBean.getSearchCriteriaBean().getSelectedCategoryId());
		return getCatalogItems(servletContext,appSession, volatileSession, oeSession,
				criteria, false,request);
	}
	
	// CAP-42856 Retrieve all featured items
	public CatalogItemsResponse getFeaturedCatalogItems(ServletContext servletContext, ApplicationSession appSession,
			ApplicationVolatileSession volatileSession, OrderEntrySession oeSession,
			OEItemSearchCriteriaSessionBean criteria, FeaturedCatalogItemsRequest request) throws AtWinXSException {
		CatalogItemsResponse featuredItemsResponse = new CatalogItemsResponse();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OEResolvedUserSettingsSessionBean oeResUserSettingsSessionBean = oeSessionBean.getUserSettings();
		List<ItemThumbnailCellData> searchResults = null;

		oeSessionBean.setContinueShopping(false);
				try {
					buildCriteriaForAllFeaturedItems(appSession, volatileSession, oeSession, criteria, appSessionBean);
				} catch (IllegalAccessException | InvocationTargetException | AtWinXSException | CPRPCException e1) {
					logger.error(this.getClass().getName() + " - " + e1.getMessage(),e1);				
					}
			
			//CAP-47291
			criteria.clearStandardAttributes();
			criteria.setStandardAttributes(null);
			try {
				getStandardAttributeList(appSessionBean, oeSession);
				criteria.setStandardAttributes(oeSession.getOESessionBean().getSearchCriteriaBean().getStandardAttributes());
			} catch (IllegalAccessException | InvocationTargetException | CPRPCException | AtWinXSException e) {
				logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
			} 
			criteria.setAttributesCriteria(null);
			
			criteria.getStandardAttributes().setFilterFeatured(true);
			setFeaturedSearch(criteria);//added a call to method
			//CAP-47291
			
			//CAP-47314
			if (null == oeSessionBean.getCatalogSearchFeaturesFavoritesBean().getFeaturedItemsDefined()) {
				searchResults = new ArrayList<>();
			} else {

				searchResults = doSearch(servletContext, appSession, volatileSession, oeSession, criteria, true);
			}
		featuredItemsResponse.setItemThumbnailCellData(searchResults);
		featuredItemsResponse.setShowOrderLinePrice(oeResUserSettingsSessionBean.isShowOrderLinePrice());
		featuredItemsResponse.setShowOrderLineAvailability(oeResUserSettingsSessionBean.isShowOrderLineAvailability());
		featuredItemsResponse.setPriceLineAvailabilityLbl(getVariantLabelValues(oeResUserSettingsSessionBean.isShowOrderLinePrice(),
						oeResUserSettingsSessionBean.isShowOrderLineAvailability(), appSessionBean));
		featuredItemsResponse.setDeliveryOptionsList(populateDeliveryOptionsList(oeResUserSettingsSessionBean, appSessionBean)); // CAP-48938
		featuredItemsResponse.setSuccess(true);
		
		return featuredItemsResponse;
	}

	private void buildCriteriaForAllFeaturedItems(ApplicationSession appSession, ApplicationVolatileSession volatileSession,
			OrderEntrySession oeSession, OEItemSearchCriteriaSessionBean criteria, AppSessionBean appSessionBean) throws AtWinXSException, IllegalAccessException, InvocationTargetException, CPRPCException {
		criteria.setCanSearchOutsideCatalog(false);
		criteria.setSearchSelectedCategory(false);
		criteria.setUseFeaturedSearchOROperand(true);
		criteria.setFeaturedItemsOnly(true);
		criteria.setSiteID(appSessionBean.getSiteID());
		criteria.setBuID(appSessionBean.getBuID());
		criteria.setUserGroup(appSessionBean.getGroupName());
		criteria.setProfileNr(appSessionBean.getProfileNumber());
		criteria.setSearchAppliance(oeSession.getOESessionBean().getUserSettings().getSearchAppliance());
		
		CatalogSearchResultsCriteria searchBean = null;
		ItemHelper.buildItemAttributesSearchCriteria(criteria, searchBean, appSession, volatileSession, oeSession);
		IFeaturedItems fComp = featuredItemsComponentLocatorService.locate(appSessionBean.getCustomToken());
		Collection<FeaturedItemsTypesVO> featuredTypes = fComp.getFeaturedTypes(appSessionBean.getSiteID(), 
				appSessionBean.getDefaultLocale(), true);
		
		if (CollectionUtils.isNotEmpty(featuredTypes)) {
			for (FeaturedItemsTypesVO feature : featuredTypes) {
				criteria.getFeaturedItemsSearch().put(String.valueOf(feature.getFeaturedTypeID()), true);
			}
		}
	}
	
	
	//CAP-43669: Retrieve Routing Info for Items submitted (in Line view) for Universal Search term 
	protected Map<String, String> retrieveRoutingInformationBadge(SearchResult thumb,AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, OEOrderSessionBean oeOrderSessionBean) throws AtWinXSException {
		
		String lineLevelRouteMessage="";
		String orderLevelRouteMessage="";
		Map<String, String> routingBadgeMap=new HashMap<>();
		if(userSettings.isShowRoutingInfo() && userSettings.isRoutingAvailable() && userSettings.isSubjToRnA()) {
			
			//set Line level Routing Message  
			lineLevelRouteMessage = ItemUtility.getLineLevelRoutingInformation(thumb.isAlwaysRoute(),
					thumb.isItemAlwaysRoute(), thumb.getRouteQuantity(), thumb.getItemRouteQuantity(),
					appSessionBean, oeOrderSessionBean);
			//set Order level Routing Message
			orderLevelRouteMessage = ItemUtility.getOrderLevelRoutingInformation(
					thumb.isAlwaysRouteOrders(), thumb.getRouteDollarAmount(),
					thumb.isRouteOnShipMethodChange(), appSessionBean, oeOrderSessionBean);
			
			routingBadgeMap=ItemUtility.getRoutingInformation(lineLevelRouteMessage, orderLevelRouteMessage, 
					appSessionBean);
		}
		return routingBadgeMap;
	}
	
	// CAP-44349
	public CatalogSearchResultsResponse doSearchWithCategory(ServletContext servletContext,
			ApplicationSession appSession, ApplicationVolatileSession volatileSession,
			OrderEntrySession oeSession, OEItemSearchCriteriaSessionBean criteria, boolean hideHiddenFeatured, CatalogItemRequest request) throws AtWinXSException {
		CatalogSearchResultsResponse catSearchResponse = new CatalogSearchResultsResponse();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		buildCriteriaForCatalogSearch(criteria, request, oeSession, appSession, volatileSession); // CAP-46323
		String htmlContent = retrieveHtmlText(oeSessionBean, appSessionBean.getCustomToken(), request.getSelectedCategoryId());
		List<ItemThumbnailCellData> searchResults = doSearch(servletContext, appSession, volatileSession, oeSession, criteria, hideHiddenFeatured);
		saveSession(oeSession, criteria, oeSessionBean, appSessionBean);
		
		catSearchResponse.setCategoryHtml(htmlContent);
		catSearchResponse.setSearchResults(searchResults);
		catSearchResponse.setSuccess(true);
		
		return catSearchResponse;
	}

	// CAP-46323
	/**
	 * Method to set attributes to catalog search criteria
	 * 
	 * @param appSession
	 * @param volatileSession
	 * @param oeSession
	 * @param criteria
	 * @throws AtWinXSException
	 */
	protected void buildItemAttributesSearchCriteria(ApplicationSession appSession,
			ApplicationVolatileSession volatileSession, OrderEntrySession oeSession,
			OEItemSearchCriteriaSessionBean criteria) throws AtWinXSException {
		buildItemAttributesSearchCriteria(criteria, appSession, volatileSession, oeSession);
	}
	
	private String retrieveHtmlText(OEOrderSessionBean oeOrderSession, CustomizationToken token, int categoryId) {
		String htmlContent = AtWinXSConstant.EMPTY_STRING;
		try {
			// This code gets the merged categories (peers) of the selected category
			Collection<Integer> selectedPeers = oeOrderSession.getSearchCriteriaBean().getSelectedCategoryPeers();
			CategoryHTML catHTML = objectMapFactoryService.getEntityObjectMap().getEntity(CategoryHTML.class, token);
			// Call the method that would updated the HTML text
			htmlContent = catHTML.getHtmlText(categoryId, selectedPeers);
			
		} catch (AtWinXSException e) {
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
		return htmlContent;
	}

	// CAP-46323 Modified method signature to pass session objects instead of session beans
	private void buildCriteriaForCatalogSearch(OEItemSearchCriteriaSessionBean criteria, CatalogItemRequest request,
			OrderEntrySession oeSession, ApplicationSession appSession, ApplicationVolatileSession volatileSession) throws AtWinXSException {

		// CAP-46323 Retrieve session beans from session objects
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();

		//Continue Shopping
		oeSessionBean.setContinueShopping(true);

		criteria.setAliasSetting("N");
		criteria.setBuID(appSessionBean.getBuID());
		criteria.setContainsForDescription(true);
		criteria.setDisplayUnassigned(true);
		criteria.setBrowse(true);
		criteria.setIncludeViewOnlyOnSearch(true);
		criteria.setSearchSelectedCategory(true);
		criteria.setLoginID(appSessionBean.getLoginID());
		criteria.setNewItemsDays(30);
		criteria.setProfileNr(appSessionBean.getProfileNumber());
		criteria.setSearchAppliance(SearchAppliance.fromCode("ES"));
		criteria.setSelectedCategoryId(request.getSelectedCategoryId());
		criteria.setSelectedCategoryPeers(getPeers(appSessionBean, oeSessionBean, request.getSelectedCategoryId(), false));//CAP-45941 CAP-48827
		criteria.setSiteID(appSessionBean.getSiteID());
		criteria.setUserGroup(appSessionBean.getGroupName());
		criteria.setSearchOptions(oeSessionBean.getUsrSrchOptions());
		criteria.setUnifiedSearchCriteria(AtWinXSConstant.EMPTY_STRING);//CAP-48287

		// CAP-46323 //CAP-47074 - changed the method
		buildItemAttributesSearchCriteria(criteria, appSession, volatileSession, oeSession);
	}
	
	private void saveSession(OrderEntrySession oeSession, OEItemSearchCriteriaSessionBean criteria,
			OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean) throws AtWinXSException {
		oeSessionBean.setSearchCriteriaBean(criteria);
		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
	}
	
	//CAP-45941 CAP-48827
	private Collection<Integer> getPeers(AppSessionBean asb, OEOrderSessionBean oeSessionBean, int selectedCategoryId, boolean isKitTemplateMode) throws AtWinXSException
	{
		ICatalog catalogManager = CMCatalogComponentLocator.locate(asb.getCustomToken());
		Collection<TreeNodeVO> categories = isKitTemplateMode ? oeSessionBean.getMergedCategoriesKitTemplateMode() : oeSessionBean.getMergedCategories();
		List<Integer> peers = new ArrayList<>();
		
		if(categories == null)
		{
			categories = catalogManager.getMergedCatalogCategories(asb.getSiteID(),
				asb.getBuID(),
				asb.getGroupName(),
				asb.getLoginID(),
				asb.getProfileNumber(),
				oeSessionBean.getSiteAttrFilterSQL(),
				asb.isMobileSession(),
				isKitTemplateMode,
				oeSessionBean.getUserSettings().isAllowCustomDocumentsInd(),
				-1, //primaryAttribute
				-1); //secondaryAttribute
			
			if(isKitTemplateMode)
			{
				oeSessionBean.setMergedCategoriesKitTemplateMode(categories);
			}
			else
			{	
				oeSessionBean.setMergedCategories(categories);			
			}
		}
		
		if(categories != null)
		{
			boolean isFound = false;
			//Check Top level Cats
			for(TreeNodeVO cat: categories)
			{
				if(cat.getNodeID() == selectedCategoryId )
				{
					for(int i : cat.getPeerIds())
					{
						peers.add(i);
						isFound = true;
					}
				}
				
				//If no match, Check 1st level sub-Cat.
				if(!isFound)
				{
					ArrayList level1SubCat = cat.getChildren();
					TreeNodeVO[] lvl1Subcategories = new TreeNodeVO[level1SubCat.size()];
					lvl1Subcategories = (TreeNodeVO[])level1SubCat.toArray(lvl1Subcategories);
					
					for(TreeNodeVO subCat : lvl1Subcategories)
					{
						//Check 1st level sub-Cat
						if(subCat.getNodeID() == selectedCategoryId )
						{
							for(int i : subCat.getPeerIds())
							{
								peers.add(i);
								isFound = true;
							}
						}
						
						//If no match, Check 2nd level sub-Cat.
						if(!isFound)
						{
							ArrayList lvl2SubCats = subCat.getChildren(); //level 2 sub-categories
							TreeNodeVO[] lvl2Subcategories = new TreeNodeVO[lvl2SubCats.size()];
							lvl2Subcategories = (TreeNodeVO[]) lvl2SubCats.toArray(lvl2Subcategories);
								
							//Check 2nd level sub-Cat
							for(TreeNodeVO lvl2Subcat : lvl2Subcategories)
							{
								if(lvl2Subcat.getNodeID() == selectedCategoryId )
								{
									for(int i : lvl2Subcat.getPeerIds())
									{
										peers.add(i);
										isFound = true;
									}
								}
							}	
						}			
					}
				}
			}
		}
		
		return peers;
	}
	
	public void buildItemAttributesSearchCriteria(OEItemSearchCriteriaSessionBean criteria, ApplicationSession appSession,
			ApplicationVolatileSession volatileSession, OrderEntrySession oeSession) throws AtWinXSException //CAP-47074 - copied from CP ItemHelper the method
	{
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		//CAP-2208 JBS empty out selectedItemAttributes if there arent any selectedItemAttributeNames
		
		//CP-11000 refactored method to build criteria.
		HashMap<Integer, ArrayList<Integer>> attrSearchCriteria = criteria.getAttributesCriteria();
		HashMap<Integer, ArrayList<Integer>> attrProfileCriteria = new HashMap<>();//CAP-47074
		
		// CAP-46543 TH - Added code for EOO
		if (attrSearchCriteria==null)
		{
			attrSearchCriteria = new HashMap<Integer, ArrayList<Integer>>();
		}
		
		boolean hasImpliedAttributes = false;
		
		//CP-10858 RAR - If there are selected Alternate Profiles, then get the Attributes from the selected profiles.
		if(null != oeSession.getOESessionBean().getProfileSelections() && 
				(null != oeSession.getOESessionBean().getProfileSelections().getProfileSelections() && !oeSession.getOESessionBean().getProfileSelections().getProfileSelections().isEmpty()))
		{
			//CAP-12426, need to pass in eoo & eoc setting to see if apply alternate profile attributes
			String attributeCriteriaFilters = oeSession.getOESessionBean().getProfileSelections().getProfileAttributesOfSelectedAltProfiles(appSessionBean, volatileSession.getVolatileSessionBean().getSelectedSiteAttribute(), oeSession.getOESessionBean().getUsrSrchOptions() );
			
			if(!Util.isBlankOrNull(attributeCriteriaFilters))
			{
				hasImpliedAttributes = true;
				appendEnteredSearchCriteria(attrSearchCriteria, attributeCriteriaFilters);
			}
		}
		
		//CP-10858 RAR - Execute what we have currently if there are alternate profile selections.
		if(!hasImpliedAttributes)
		{
			appendEOOCriteria(attrSearchCriteria, volatileSession.getVolatileSessionBean().getSelectedSiteAttribute());
			appendEOCCriteria(attrProfileCriteria, appSessionBean);
		}
		
		criteria.setAttributesProfileCriteria(attrProfileCriteria);
		criteria.setAttributesCriteria(attrSearchCriteria);
	}
	
	
	
	//CP-11000
		/**
		 * Method appendEOOCriteria()
		 * 
		 * This method is build the EOO attributes to use for filtering catalog items.
		 * 
		 * @param attrSearchCriteria
		 * @param selectedSiteAttribute
		 */
		protected static void appendEOOCriteria(HashMap<Integer, ArrayList<Integer>> attrSearchCriteria, HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttribute)
		{
			if(selectedSiteAttribute != null)
			{
				for(Map.Entry<Integer, SiteAttrValuesVO[]> entry : selectedSiteAttribute.entrySet())
				{
					Integer attrID = entry.getKey();
					SiteAttrValuesVO[] vals = entry.getValue();
					ArrayList<Integer> currentVals = attrSearchCriteria.get(attrID);
					if(currentVals == null)
					{
						currentVals = new ArrayList<Integer>();
						attrSearchCriteria.put(attrID, currentVals);
					}
					for(SiteAttrValuesVO valVO : vals)
					{
						Integer val = valVO.getAttrValID();
						if(!currentVals.contains(val))
						{
							currentVals.add(val);
						}
					}
				}
			}
		}

		//CP-11000
		/**
		 * Method appendEnteredSearchCriteria()
		 * 
		 * This method is build the search attributes that the user selected to filter catalog items.
		 * 
		 * @param attrSearchCriteria
		 * @param selectedItemAttributes
		 */
		protected static void appendEnteredSearchCriteria(HashMap<Integer, ArrayList<Integer>> attrSearchCriteria, String selectedItemAttributes)//CAP-47074 - copied from CP ItemHelper the method
		{
			String[] attributes = Util.nullToEmpty(selectedItemAttributes).split("\\|");
			for(String attr : attributes)
			{
				String[] attrVals = attr.split("\\~");
				int attrID = -1;
				if(attrVals.length > 0)
				{
					attrID = Util.safeStringToDefaultInt(attrVals[0], -1);
				}
				
				if(attrID > -1)
				{
					ArrayList<Integer> currentVals = attrSearchCriteria.get(attrID);
					if(currentVals == null)
					{
						currentVals = new ArrayList<Integer>();
						attrSearchCriteria.put(attrID, currentVals);
					}
					for(int i=1; i < attrVals.length; i++)
					{
						String attrVal = attrVals[i];
						//JW - fix multi-select attributes throwin number format exception
						String[] attrValArray = attrVal.split(",");
						for(String valStr : attrValArray)
						{
							int val = Util.safeStringToDefaultInt(valStr, AtWinXSConstant.INVALID_ID);
							if(val > -1 && !currentVals.contains(val))
							{
								currentVals.add(val);
							}
						}
					}
				}
			}
		}
		
		//CP-10858 RAR
		/**
		 * Method appendSearchCriteria()
		 * 
		 * This method will determine if the attribute search criteria (selectedItemAttributes) is/are already in the implied (attributeCriteriaFilters).
		 * If an attribute search criteria is not yet in the implies search criteria, then we need to add it.
		 * 
		 * @param attributeCriteriaFilters
		 * @param selectedItemAttributes
		 * @return String
		 */
		protected static String appendSearchCriteria(String attributeCriteriaFilters, String selectedItemAttributes)//CAP-47074 - copied from CP ItemHelper the method
		{
			String finalAttributes = "";
			
			String[] impliedAttributes = Util.nullToEmpty(attributeCriteriaFilters).split("\\|");
			String[] searchAttributes = Util.nullToEmpty(selectedItemAttributes).split("\\|");
			
			boolean isFirstAttr = true;
			
			//Loop through the implied search criteria
			for(String impliedAttr : impliedAttributes)
			{
				String[] impliedAttrVals = impliedAttr.split("\\~");
				int impliedAttrID = -1;
				if(impliedAttrVals.length > 0)
				{
					impliedAttrID = Util.safeStringToDefaultInt(impliedAttrVals[0], -1);
				}
				
				boolean modified = false;
				String newAttrs = impliedAttr;
				
				if(impliedAttrID > -1)
				{				
					//Loop through the attribute search criteria
					for(String attr : searchAttributes)
					{
						boolean attrFound = false;
						
						String[] attrVals = attr.split("\\~");
						int attrID = -1;
						if(attrVals.length > 0)
						{
							attrID = Util.safeStringToDefaultInt(attrVals[0], -1);
						}
						
						if(attrID > -1)
						{
							//If the attribute search criteria is found in the implied search criteria.
							if(impliedAttrID == attrID)
							{
								attrFound = true;
								
								//Check the attribute search criteria values if already existing in the implied search criteria
								for(int i = 1; i < attrVals.length; i++)
								{
									boolean found = false;
									
									for(int j = 1; j < impliedAttrVals.length; j++)
									{
										if(impliedAttrVals[j].equals(attrVals[i]))
										{
											found = true;
											break;
										}
									}
									
									//If the attribute search criteria is not in the implied search criteria values, then add it.
									if(!found && !"-1".equals(attrVals[i]))
									{
										modified = true;
										newAttrs += "~" + attrVals[i];
									}
								}
							}
							
							//If the attribute search criteria is not found, then see if there are valid values.  If there is/are
							//valid value, then add it.
							if(!attrFound)
							{
								String values = "";
								
								for(int i = 1; i < attrVals.length; i++)
								{
									if(!"-1".equals(attrVals[i]) && !finalAttributes.contains(attrID + values))
									{
										modified = true;
										values += "~" + attrVals[i];
									}
								}
								
								if(!values.isEmpty())
								{
									finalAttributes += isFirstAttr ? "" : "|";
									finalAttributes += attrID + values;
									
									isFirstAttr = false;
								}
							}
						}
					}				
					
					//modified means that the attribute search criteria is found in implied search criteria but the value of attribute search criteria is not found in implied search criteria.
					if(!modified)
					{
						finalAttributes += isFirstAttr || finalAttributes.contains(impliedAttr) ? "" : "|";
						finalAttributes += finalAttributes.contains(impliedAttr) ? "" : impliedAttr;
					}
					//If attribute search criteria is not found at all, add it.
					else
					{
						finalAttributes += isFirstAttr ? "" : "|";
						finalAttributes += newAttrs;
					}
					
					isFirstAttr = false;
				}
			}
			
			return finalAttributes;
		}

		/**
		 * Method appendEOCCriteria()
		 * 
		 * This method is build the EOC attributes to use for filtering catalog items.
		 * 
		 * @param attrProfileCriteria
		 * @param asb
		 */
		protected static void appendEOCCriteria(HashMap<Integer, ArrayList<Integer>> attrProfileCriteria, AppSessionBean asb)//CAP-47074 - copied from CP ItemHelper the method
		{
			Map<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> profileAttrs = asb.getProfileAttributes();
			
			if(profileAttrs != null)
			{
				// CAP-33833 TH - Removed reference to OAOrderAdminAssembler to within class
				for(Map.Entry<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> profileAttr : profileAttrs.entrySet())
				{
					SiteAttributesVO attr = profileAttr.getKey();
					// CAP-33833 TH - Called method in class rather than Assembler
					if (enforceUserGroupSearchOption(asb.getCustomToken(), asb.getSiteID(), asb.getBuID(), asb.getGroupName(), attr.getAttrID()))
					{
						ArrayList<SiteAttrValuesVO> attrVals = profileAttr.getValue();
						ArrayList<Integer> valIDs = new ArrayList<Integer>();
						for(SiteAttrValuesVO attrVal : attrVals)
						{
							valIDs.add(attrVal.getAttrValID());
						}
						
						attrProfileCriteria.put(attr.getAttrID(), valIDs);
					}
				}
			}
		}
		
		/**
		 * Method enforceUserGroupSearchOption.
		 * This should return true only if the site attribute search term is specifying enforce on catalog
		 * 
		 * @param int siteID
		 * @param int buID
		 * @param String usrGrpName
		 * @param int attributeID
		 * @return ArrayList
		 * @throws AtWinXSException
		 * 
		 */
		// CAP-33833 TH - Create method here to call IUserGroup directly instead of in OECatalogAssembler
		protected static boolean enforceUserGroupSearchOption(
			CustomizationToken token,
			int siteID, 
			int buID, 
			String usrGrpName,
			int attributeID) //CAP-47074 - copied from CP ItemHelper the method
		{	
			Logger logger = LoggerFactory.getLogger(ItemHelper.class);
			
			boolean enforceOnCatalog=false;
			try
			{
					IUserGroup iUserGroup = UserGroupComponentLocator.locate(token);						
					UserGroupSearchVO ugVO= iUserGroup.getUserGroupSearchOption(siteID, buID, usrGrpName, attributeID);
					if (ugVO!=null) enforceOnCatalog=ugVO.isEnforceOnCatalog();
			} catch(AtWinXSException e) 
			{
				// CAP-18392 call to logger instead of empty brackets
				logger.debug("ItemHelper " + " - " + e.getMessage(),e);
			}
			return enforceOnCatalog;
		}
		
		//CAP-47291
		public StandardAttributesC1UX getStandardAttributeList(AppSessionBean appSessionBean, OrderEntrySession oeSession) 
				throws CPRPCException, AtWinXSException, IllegalAccessException, InvocationTargetException {
			
			StandardAttributesC1UX standardAttributesC1UX = new StandardAttributesC1UX();
			StandardAttributes result = null;
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			
			if (oeSessionBean != null) {
				
				if (oeSessionBean.getSearchCriteriaBean().getStandardAttributes() != null) {
					
					return getStandardAttributesFromSession(appSessionBean ,oeSessionBean ,oeSession,standardAttributesC1UX);
				}
				else {
					
					CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean = null;
					catSearchFeatFaveBean = getCatSearchFeatFaveBean(appSessionBean, oeSession, catSearchFeatFaveBean);
					OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
					result = new StandardAttributes();

					if (catSearchFeatFaveBean != null) {

						setFavAndFeaToResultObject(appSessionBean, oeSessionBean, catSearchFeatFaveBean,  result );
					}

					if (userSettings != null && userSettings.isNewItemsFlag()) {
						
							result.setShowNewItems(true);
							result.setNewItemsLabel(Util.nullToEmpty(userSettings.getNewItemsLabel()));
					}
				}
			}
			
			oeSession.getOESessionBean().getSearchCriteriaBean().setStandardAttributes(result);
			SessionHandler.persistServiceInSession(oeSession, appSessionBean.getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
			BeanUtils.copyProperties(standardAttributesC1UX, result);
			
			return standardAttributesC1UX;
		}
		
		public StandardAttributesC1UX  getStandardAttributesFromSession(AppSessionBean appSessionBean, 
				OEOrderSessionBean oeSessionBean, OrderEntrySession oeSession,  StandardAttributesC1UX response) 
				throws CPRPCException, AtWinXSException, IllegalAccessException, InvocationTargetException {
					
			// CAP-46323 - Removed bad code
			StandardAttributes result = oeSessionBean.getSearchCriteriaBean().getStandardAttributes();				
			BeanUtils.copyProperties(response, result);

			return response;
		}
		public CatalogSearchFeaturesFavoritesBean  getCatSearchFeatFaveBean(AppSessionBean appSessionBean, 
				OrderEntrySession oeSession, CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean) 
				throws CPRPCException, AtWinXSException {
			
			OECatalogAssembler catalogAssembler = new OECatalogAssembler(appSessionBean.getCustomToken(), 
					appSessionBean.getDefaultLocale());
			List<AlertCountResponseBean> featFaveItemsLst = null;
			try
			{
				
				featFaveItemsLst = catalogAssembler.getFavoritesFeaturedItemsCount(appSessionBean);
			} catch (AtWinXSException e) {
				
				throw Util.asCPRPCException(e);
			}
		
			if (featFaveItemsLst != null && !featFaveItemsLst.isEmpty()) {
				
				catSearchFeatFaveBean = new CatalogSearchFeaturesFavoritesBean();
				for (AlertCountResponseBean featFaveItems: featFaveItemsLst) {
					
					if (featFaveItems.getAlertCategory().equals(AtWinXSConstant.QUICK_FIND_FAVORITE_ITEMS)) {
						
						catSearchFeatFaveBean.setHasFavoriteItems(true);
					} else {
						
						catSearchFeatFaveBean.setFeaturedItemsDefined(featFaveItems.getAlertCounts());
					}
				}
		
				oeSession.getOESessionBean().setCatalogSearchFeaturesFavoritesBean(catSearchFeatFaveBean);
				SessionHandler.persistServiceInSession(oeSession, appSessionBean.getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
			}
			
			return catSearchFeatFaveBean;
		}
		public void setFavAndFeaToResultObject(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, 
				CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean, StandardAttributes result ) 
				throws AtWinXSException {
			
			if (oeSessionBean.isAllowUserFavorites() && catSearchFeatFaveBean.hasFavoriteItems()) {
				
				result.setShowFavorites(true);
				result.setFavoritesLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), 
						appSessionBean.getCustomToken(), SFTranslationTextConstants.STANDARD_FAVORITE_ATTRIBUTE_LBL));
				
			}
		
			if (catSearchFeatFaveBean.getFeaturedItemsDefined() != null) {

				for (AlertCounts alertCnt : catSearchFeatFaveBean.getFeaturedItemsDefined()) {
				
					if (!Util.isBlankOrNull(alertCnt.getAlertCd()) && catSearchFeatFaveBean.getFeaturedItemsSearch() != null) {
					
						FeaturedSearchCriteria criteria = new FeaturedSearchCriteria();
						criteria.setLabel(alertCnt.getAlertName());
						criteria.setTypeID(Util.safeStringToDefaultInt(alertCnt.getAlertCd(), AtWinXSConstant.INVALID_ID));
						result.getFeaturedSearchCriteria().add(criteria);
					}
				}
			}
		}
		//*****/
		
		void setFeaturedSearch(OEItemSearchCriteriaSessionBean criteria) {
			boolean foundFeatured=false;
			for (FeaturedSearchCriteria featuredSearchCriteria : criteria.getStandardAttributes().getFeaturedSearchCriteria()) {
					foundFeatured=true;
					featuredSearchCriteria.setSelected(true);
			}
			if(!foundFeatured) {
				FeaturedSearchCriteria featuredSearch= new FeaturedSearchCriteria();
				featuredSearch.setTypeID(1);
				featuredSearch.setSelected(true);
				featuredSearch.setLabel(AtWinXSConstant.EMPTY_STRING);
				criteria.getStandardAttributes().getFeaturedSearchCriteria().add(featuredSearch);
			}
		}
		
		//CAP-48534
		public CatalogMessageResponse getCatalogMessages(ApplicationSession appSession) throws AtWinXSException {
			AppSessionBean appSessionBean = appSession.getAppSessionBean();
			UserGroup ug = objectMapFactoryService.getEntityObjectMap().getEntity(UserGroup.class,
					appSessionBean.getCustomToken());
			UserGroupVOKey ugKey = new UserGroupVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(),
					appSessionBean.getGroupName());
			ug.setKey(ugKey, false);
			Messages catalogMessages = ug.getCatalogMessages();
			CatalogMessageResponse buildCatalogMessageResponses = new CatalogMessageResponse();
			List<CatalogMessagesResponse> listnewcheck = new ArrayList<>();
			if (catalogMessages.asCollection() != null) {
				for (Message featuredSearchCriteria : catalogMessages.asCollection()) {
					CatalogMessagesResponse catalogmessageResponse = new CatalogMessagesResponse();
					catalogmessageResponse.setCatalogMessageLocation(featuredSearchCriteria.getCatalogValue());
					catalogmessageResponse.setCatalogMessage(featuredSearchCriteria.getMessageBody());
					listnewcheck.add(catalogmessageResponse);
				}
				buildCatalogMessageResponses.setCatalogMessagesResponse(listnewcheck);
				buildCatalogMessageResponses.setSuccess(true);
			}
			return buildCatalogMessageResponses;
		}
		
}
