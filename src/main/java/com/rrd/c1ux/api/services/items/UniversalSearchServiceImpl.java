/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  04/08/22    S Ramachandran  CAP-33686   Initial Creation, added for Multiple Item Search (predictive search)
 *  04/21/22    S Ramachandran  CAP-33825	Added ItemHelper, ItemSearchServiceImplHelper via cp-bundle8.3.13.35
 *  05/02/22    S Ramachandran  CAP-34043   Cloned CP methods to C1 to detach GWT dependency
 *	09/19/22	A Boomker		CAP-35958	Efficiency improvements
 *	10/03/22	A Boomker		CAP-35542	Change response object
 *  10/12/22	S Ramachandran	CAP-35950	set itemOrderable flag for Punchout inspect mode in Universal Search dropdown
  *	10/21/22	A Boomker		CAP-35224	Fix images starting with http
  * 12/01/22    M Sakthi        CAP-37551  	Change universal search API to only return a max of 5 items and add flag indicating if more
  * 05/31/23	N Caceres		CAP-39050	Resolve concurrency issues and remove unused method
  * 08/30/23	A Boomker		CAP-43405	Adding flag for customizable item to results
  *	09/15/23	L De Leon		CAP-43196	Added setFeatureMap() method
 *  09/18/23	Krishna Natarajan	CAP-43967 Added logic to set and get FeatureFavoriteItemData
 *  10/11/23	T Harmon		CAP-44548									Modified edoc URL
 */

package com.rrd.c1ux.api.services.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.cmx.internal.json4j.JSONObject;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.models.items.UniversalSearch;
import com.rrd.c1ux.api.models.items.UniversalSearchRequest;
import com.rrd.c1ux.api.models.items.UniversalSearchResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.catalog.search.CatalogSearchResultsCriteriaBean;
import com.rrd.custompoint.framework.util.SearchApplianceFactory;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.rrd.custompoint.service.helper.ItemSearchServiceImplHelper;
import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.util.CatalogConstant;
import com.wallace.atwinxs.catalogs.util.CatalogItemFilterBean;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
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
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.interfaces.ISearchAppliance;
import com.wallace.atwinxs.items.locator.ManageItemsInterfaceLocator;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OECatalogTreeResponseBean;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

@Service
public class UniversalSearchServiceImpl extends BaseOEService implements UniversalSearchService {


	private static final Logger logger = LoggerFactory.getLogger(UniversalSearchServiceImpl.class);

	public static final int AUTOCOMPLETE_ITEM_LIMIT = 5;
	public static final String STR_BUNDLE_DEFAULT_QTY = "1";
	public static final String NOT_AVAILABLE = "N/A";

	public UniversalSearchServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService) {
	      super(translationService, objectMapFactoryService);
	    }

	/**
 	 *
 	 * @param mainSession - {@link SessionContainer}
 	 * @param request - {@link UniversalSearchRequest}
 	 * @return - This will return UniversalSearchResponse which includes
 	 *           Array of search Item(s) & Status {@link UniversalSearchResponse}
 	 * @throws AtWinXSException
	 */
	//CAP-33686   Method logic copied from CustomPointWeb UniversalSearchAjaxController.doAction()
	public UniversalSearchResponse processUniversalSearch(SessionContainer mainSession, ServletContext servletContext, UniversalSearchRequest request)
			throws AtWinXSException {

		ApplicationSession appSession =null ;
		ApplicationVolatileSession volatileSession =  null;
		PunchoutSessionBean punchoutSessionBean = null;
		OEItemSearchCriteriaSessionBean criteria = null;
		CatalogItemFilterBean filterBean = null;

		appSession = mainSession.getApplicationSession();
		// CAP-39050 Resolve concurrency issues
		AppSessionBean appSessionBean = mainSession.getApplicationSession().getAppSessionBean();

		volatileSession =  mainSession.getApplicationVolatileSession();
		// CAP-39050 Resolve concurrency issues
		VolatileSessionBean volatileSessionBean =  mainSession.getApplicationVolatileSession().getVolatileSessionBean();

		punchoutSessionBean = mainSession.getApplicationSession().getPunchoutSessionBean();

		String term = request.getTerm();
		// CAP-39050 Replace the type specification in this constructor call with the diamond operator ("<>").
		ArrayList<JSONObject> jsonLineItems = new ArrayList<>();

		CustomizationToken customToken = appSessionBean.getCustomToken();

		CatalogSearchResultsCriteriaBean searchCriteriaBean = new CatalogSearchResultsCriteriaBean();
		ICatalog catalogComponent = CMCatalogComponentLocator.locate(customToken);
		OrderEntrySession oeSession = (OrderEntrySession) mainSession.getModuleSession();
		OEOrderSessionBean oosb = ((OrderEntrySession) mainSession.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oosb.getUserSettings();

		//reset some values first before we build the search criteria
		oosb.getSearchCriteriaBean().setAttributesProfileCriteria(new HashMap<Integer, ArrayList<Integer>>());
		oosb.getSearchCriteriaBean().setNewItemsDays(oosb.getUserSettings().getNewItemsDays());
		oosb.getSearchCriteriaBean().setVendorItemNum(null);
		oosb.getSearchCriteriaBean().setSearchSelectedCategory(false);
		oosb.getSearchCriteriaBean().setCustItemNum(null);
		oosb.getSearchCriteriaBean().setDescription(null);
		oosb.getSearchCriteriaBean().setUnifiedSearch(true);
		oosb.setLastSelectedCatalogId(AtWinXSConstant.INVALID_ID);
		oosb.getSearchCriteriaBean().setSelectedCategoryId(AtWinXSConstant.INVALID_ID);
		oosb.getSearchCriteriaBean().setSelectedCategoryPeers(null);
		oosb.getSearchCriteriaBean().setBrowse(false);
		oosb.getSearchCriteriaBean().setAliasSetting(oosb.getUserSettings().getAliasSetting());
		oosb.getSearchCriteriaBean().setCustomerItemAliases(oosb.getUserSettings().getItemUDFAliases());

		//CAP-33825	Used buildSearchCriteria() from ItemSearchServiceImplHelper via cp-bundle.jar
		criteria = ItemSearchServiceImplHelper.buildSearchCriteria(searchCriteriaBean, appSession, volatileSession, oeSession);

		// Put wizard information in there if there is wizard information
		boolean isWizard = false;
		if (oeSession.getOESessionBean().getOrderWizardSearchAttributes() != null && request.isWizard())
		{
			criteria.getAttributesCriteria().putAll(oeSession.getOESessionBean().getOrderWizardSearchAttributes());
			isWizard = true;
		}
		
	    //get the search term from universal search
	    criteria.setUnifiedSearchCriteria(term);
	    criteria.setUnifiedSearch(true);
	    criteria.setUniversalSearchAutoComplete(true);


		filterBean = catalogComponent.getItemExclusions(criteria,
				appSessionBean.isMobileSession(), volatileSessionBean.isKitTemplateMode(),
				userSettings.isAllowCustomDocumentsInd(), userSettings.getPrimaryAttribute(),
				userSettings.getSecondaryAttribute(), volatileSessionBean.isBatchVDPMode() );


		CatalogLineVO[] items = null;
		//CAP-21121  use proper search appliance implementation
    	ISearchAppliance sa = SearchApplianceFactory.getSearchAppliance(criteria, appSessionBean.getCustomToken());
    	items = sa.executeSearch(criteria, filterBean, isWizard);

    	// CAP-39050 Replace by SLF4J string formatting
    	logger.debug("UniversalSearchServiceImpl : {}{}", criteria, filterBean);

		//CP-13132 look up order lines once, so we don't have to do it for every search result
		OECatalogAssembler assembler = new OECatalogAssembler(appSession.getAppSessionBean().getCustomToken(), appSession.getAppSessionBean().getDefaultLocale());
		Collection<OrderLineVO> orderLines = assembler.getOrderLines(null != volatileSession.getVolatileSessionBean().getOrderId() ? volatileSession.getVolatileSessionBean().getOrderId().intValue() : AtWinXSConstant.INVALID_ID);
		ICatalog catalogComp = CMCatalogComponentLocator.locate(appSession.getAppSessionBean().getCustomToken());
		IManageItemsInterface itemInterface = ManageItemsInterfaceLocator.locate(appSession.getAppSessionBean().getCustomToken());
		// CAP-2589 - make sure that we pull info from existing kit session and not reload here
		KitSession kitSession = null;
		if (volatileSession.getVolatileSessionBean().isKitTemplateMode())
		{
			try
			{ // if we have no kit session, we should not be in kit template mode
				kitSession = (KitSession) SessionHandler.loadSession(appSession.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
			}
			catch(AtWinXSWrpException eofex)
			{
				//CAP-18657 Replaced printstacktrace() call with Logger
				logger.error(Util.class.getName() + " - " + eofex.getMessage(), eofex);
				throw eofex;
			}
		}

		//CAP-15939 same remove duplicate logic from ItemSearchServiceImplHelper.searchItems()
		items = removeDuplicateItems(items);

		//set the autocomplete flag to false once gsa search is done
		criteria.setUniversalSearchAutoComplete(false);

		//CAP-16563 set the icon plus enabled UDF Field
		if(appSessionBean.isIconPlusViewEnabled()) {

			userSettings.setAdditionalIconFldLabel();
			userSettings.setAdditonalIconValues();
		}

		boolean moreThanAllowed = false;

		for(CatalogLineVO line : items) {

		//CAP-37551 Change universal search API to only return a max of 5 items and add flag indicating if more
		 if(jsonLineItems.size() == AUTOCOMPLETE_ITEM_LIMIT) {
			 moreThanAllowed = true;
			 break;
		 }
		 else {
			//CAP-16452 Reuse existing helper code to retrieve item image thumbnail
			SearchResult result = new SearchResult();
			getItemThumbnail(result, userSettings, mainSession.getApplicationSession(), line, servletContext,
					mainSession.getApplicationVolatileSession(), criteria, "", "", "", false, orderLines, catalogComp, itemInterface, kitSession);


			JSONObject obj = new JSONObject();
			obj.put("description", line.getDescription());
			obj.put("itemNum", line.getItemNum());
			obj.put("wcsItemNum", line.getWcsItemNum());
			obj.put("catLineNum", line.getCatalogLineNum());
			//CAP-15221 include item image
			String imageUrl = result.getCartImgURL();
			obj.put("imageUrl", imageUrl);
			obj.put("defaultQty", result.getItemQuantity()); //CAP-27898

			//CAP-16144
			boolean isCustomizableItem = isCustomizableItem(line.getClassification(),
					line.getWcsItemNum(), appSessionBean, punchoutSessionBean);
			// CAP-43405 - need to set customizable items "item in cart" flag differently
			obj.put("customizable", isCustomizableItem);
			boolean isItemInCart = !isCustomizableItem && isItemInCart(line, appSessionBean, volatileSessionBean);

			//CAP-35950 - set itemOrderable flag for Punchout inspect mode
			if(punchoutSessionBean!=null && punchoutSessionBean.getOperation().equals(OrderEntryConstants.PUNCHOUT_OPERATION_INSPECT)) {
				result.setItemOrderable(false);
			}

			//add flag when to show add to cart
			boolean showAddToCart = showAddToCart(result, isCustomizableItem, isItemInCart);
			//build add to cart icon based on logic in ItemSearchData.getAddToCartColumnDef()
			// CAP-35542
			obj.put("addToCartAllowed", Util.boolToY(showAddToCart));
			obj.put("itemInCart", Util.boolToY(isItemInCart && !isCustomizableItem));
	//		obj.put("addToCartAnchorAndIcon", getAddToCartAnchorAndIcon(result, showAddToCart, isCustomizableItem, isItemInCart, line)); // CAP-26250

			//CAP-16563 additional icon udf
			obj.put("iconPlusUDF", getAdditionalIconPlusField(userSettings, line.getItemNum()));

			//only show a limited number based on AUTOCOMPLETE_ITEM_LIMIT
			if ( jsonLineItems.size() < AUTOCOMPLETE_ITEM_LIMIT)
				jsonLineItems.add(obj);
		}
	  }
		UniversalSearch[] arrLineItems= null;
		String objectsStr = jsonLineItems.toString();
		ObjectMapper mapper = new ObjectMapper();

		try {

			arrLineItems = mapper.readValue(objectsStr, UniversalSearch[].class);
		} catch (JsonProcessingException e) {
			// CAP-39050 Replace by SLF4J string formatting
			logger.debug("processUniversalSearch() mapping ArrayList to Array {}", e.toString());

		}


		UniversalSearchResponse response = new UniversalSearchResponse();

		if(arrLineItems != null)	{
			//CAP-37551 Change universal search API to only return a max of 5 items and add flag indicating if more
			response.setMoreResultsAvailable(moreThanAllowed);

			response.setLineItemsVO(arrLineItems);
			response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
		} else {

			response.setStatus(RouteConstants.REST_RESPONSE_FAIL);
		}

		return  response;
	}


	//CAP-33686   Method copied from CustomPointWeb UniversalSearchAjaxController.removeDuplicateItems()
	//CAP-15939 JBS same logic from ItemSearchServiceImplHelper.searchItems()
	public CatalogLineVO[] removeDuplicateItems(CatalogLineVO[] items) {

		OECatalogTreeResponseBean treeBean = new OECatalogTreeResponseBean();
		TreeNodeVO searchedNode = new TreeNodeVO();
		searchedNode.setItems(items);
		searchedNode.setNodeID(-1);

		Collection<TreeNodeVO> categories = new ArrayList<TreeNodeVO>(1);
		categories.add(searchedNode);

		treeBean.setCategories(categories, true);

		items = treeBean.getSelectedNode().getItems();

		return items;
	}


	//CAP-33686   Method copied from CustomPointWeb UniversalSearchAjaxController.getAdditionalIconPlusField()
	//CAP-16563
	public String getAdditionalIconPlusField(OEResolvedUserSettingsSessionBean userSettings, String itemNum) {

		String additionalFieldValue = userSettings.getAdditonalIconValues() != null ?
				Util.nullToEmpty(userSettings.getAdditonalIconValues().get(itemNum)) : "";

		return userSettings.getAdditionalIconPlusFld() > -1 &&
				!additionalFieldValue.isEmpty() ? "<br>" + additionalFieldValue : "";
	}

	//CAP-33686   Method copied from CustomPointWeb UniversalSearchAjaxController.showAddToCart()
	//CAP-15939 JBS use SearchResult from ItemSearchServiceImplHelper.getItemThumbnail instead of raw CatalogLineVO
	//CAP-16144
	// CAP-39050 Immediately return this expression instead of assigning it to the temporary variable
	public boolean showAddToCart(SearchResult result, boolean isCustomizableItem, boolean isItemInCart) {
		return !result.isViewOnlyFlag() &&
				!result.isInvalidItemFlag() &&
				result.isItemOrderable() &&
				(!isItemInCart || isCustomizableItem);
	}

	//CAP-33686   Method copied from CustomPointWeb UniversalSearchAjaxController.isItemInCart()
	//CAP-16144
	// CAP-39050 Remove unused parameter and add volatileSessionBean as method parameter
	public boolean isItemInCart(CatalogLineVO line, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
			throws AtWinXSException {

		boolean isItemInCart = false;

		//Existing logic from ItemSearchServiceImplHelper.getItemThumbnail() line 646
		OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		Collection<OrderLineVO> orderLines = assembler.getOrderLines(null != volatileSessionBean.getOrderId() ? volatileSessionBean.getOrderId().intValue() : AtWinXSConstant.INVALID_ID);

		if(null != volatileSessionBean.getOrderId() && volatileSessionBean.getOrderId() > -1)
			isItemInCart = ItemHelper.isItemInCart(appSessionBean, line.getItemNum(), line.getWcsItemNum(), orderLines) ;

		return isItemInCart;
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
	//CAP-34043   Method/Logic copied from CustomPointWeb ItemSearchServiceImplHelper.getItemThumbnail()
	public SearchResult getItemThumbnail(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings, ApplicationSession appSession, CatalogLineVO vo,
			ServletContext servletContext,
			// CP-10341 added criteria parameter
			ApplicationVolatileSession volatileSession, OEItemSearchCriteriaSessionBean criteria, String routingShippingMethodMsg, String routingExceedAmountMsg,
			//CAP-2476 added hasFeaturedItems
			String routingAlwaysRouteOrders, boolean hasFeaturedItems, Collection<OrderLineVO> orderLines, ICatalog catalogComp,
			IManageItemsInterface itemInterface, KitSession kitSession)	throws AtWinXSException {

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

		ItemImagesVOKey imageKey =
				new ItemImagesVOKey(appSession.getAppSessionBean().getSiteID(),
					Util.htmlUnencodeQuotes(Util.nullToEmpty(vo.getItemNum())),
					Util.htmlUnencodeQuotes(Util.nullToEmpty(vo.getWcsItemNum())));


		if (!Util.isBlankOrNull(voMediumImg))
		{
			if (!voMediumImg.toLowerCase().startsWith("http"))
			{
				ItemImagesVO medImageVO = new ItemImagesVO(appSession.getAppSessionBean().getSiteID(), vo.getItemNum(),
						vo.getWcsItemNum(),	AtWinXSConstant.EMPTY_STRING,	voMediumImg, AtWinXSConstant.EMPTY_STRING);
				ItemImagesVOFilter image = new ItemImagesVOFilter(medImageVO);
				medImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
				logger.info("Search result object pulled back med image: " + vo.getItemMedImgLocURL()
				+ " but we calculated this one instead- " + medImage);
			}
			else // CAP-35224
			{
				medImage = voMediumImg;
			}
			// CAP-35224
			if (Util.isBlankOrNull(medImage))
			{
				medImage = "assets/images/No-image-88x88.svg";
			}
		}
		//CP-9877 EZL Get medium image for non-GSA user
		//CP-10341 Changed check for GSA compliant search
		else if (criteria == null || !catalogComp.isGSACompliantSearch(criteria))
		{
			ItemImagesVO imageVO = catalogComp.getImagesForItem(imageKey);
			//CAP-27678 - this will default to blank, not the placeholder
			if (imageVO != null)
			{
				ItemImagesVOFilter image = new ItemImagesVOFilter(imageVO);
				largeImage = image.getQualifiedItemFullImgLocURL(appSession.getAppSessionBean());
				medImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
			}
			else
			{
				medImage = "assets/images/No-image-88x88.svg";
			}
		}
		else
		{
			medImage = "assets/images/No-image-88x88.svg";
		}

		setItemDetailsFieldValues(thumb, vo, description, medImage, largeImage);

		//CP-8466 RAR - Retrieve and set the Item UOM Options.
		//CP-8942	RAR - Pass the deliveryOptions to determine if we need to build the UOM Options.
		//CP-9486 RAR - Pass the mergeCode to handle Mail Merge Items.
		//JW - Added view only to the logic to determine to show UOM options or not.
		//CP-8970 removed token parameter
		//CP-13132 added isExternalESP parameter
		ItemUtility.setItemUOMOptions(thumb, appSession.getAppSessionBean(), vo.getClassification(), isExternalESP,
				volatileSession.getVolatileSessionBean(), vo.getWcsItemNum(), kitSession, itemInterface,
				userSettings, vo.getMergeOptionCode());

		thumb.setUomOptions(ItemHelper.getItemUOMOptions(vo.getWcsItemNum(), vo.getItemNum(), vo.getClassification(), userSettings, volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean(), vo.getDeliveryOption(), vo.getMergeOptionCode(), vo.isViewOnlyFlag(), isExternalESP));

		//RAR - Set other needed information about the item.
		thumb.setAllowFavorites(userSettings.isAllowUserFavoritesInd());
		//CP-8970 changed token from String to an Object
		//CP-13132 Only check if the item is a Favorite if the user is allowed to do favorites
		if(userSettings.isAllowUserFavoritesInd())
		{
			thumb.setFavorite(ItemHelper.isItemFavorite(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getProfileNumber(), vo.getWcsItemNum(), vo.getItemNum(), appSession.getAppSessionBean().getCustomToken()));
		}

		//CP-13132 Don't make DB call to check if the user has the service.  Use the service list in AppSessionBean
		//thumb.setCheckInventoryEnabled(ItemHelper.isCanCheckInventory(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getGroupName(), appSession.getAppSessionBean().getCustomToken()));
		//thumb.setManageItemsEnabled(ItemHelper.isItemManager(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getGroupName(), appSession.getAppSessionBean().getCustomToken()));
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

		// CAP-43196
		setFeatureMap(thumb, appSession.getAppSessionBean(), volatileSession.getVolatileSessionBean(), vo);

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

		if(volatileSession.getVolatileSessionBean().isKitTemplateMode())
		{
			thumb.setAllowDupCustDoc(kitSession.getAdminBean().isAllowDuplicateCustDocs()); // CAP-2589
			thumb.setItemInKitInd(ItemHelper.getItemInKitIndicator(volatileSession.getVolatileSessionBean().getSelectedKitComponents(), vo.getItemNum(), vo.getWcsItemNum(), appSession.getAppSessionBean().getCustomToken(), appSession.getAppSessionBean().getDefaultLocale()));

			thumb.setItemQuantity(ItemHelper.getWildCardDefaultQuantity(vo.getWcsItemNum(), appSession.getAppSessionBean(), volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean().getCustomToken()));
			thumb.setDisplayQuantityAsText((!Util.isBlankOrNull(thumb.getItemQuantity()) && !ItemHelper.isCustomizableItem(vo.getClassification(), vo.getWcsItemNum(), appSession.getAppSessionBean(), appSession.getPunchoutSessionBean())) ? true : false);
		}
		else
		{
			//CP-9225 use admin default quantity
			thumb.setItemQuantity(Integer.toString(appSession.getAppSessionBean().getSiteDefaultQty()));
			//CP-13132 changed parameters to ItemHelper.isItemInCart
			thumb.setItemInCart(catalogComp.isInCart(vo.getItemNum(), vo.getWcsItemNum(), orderLines));

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

		return thumb;
	}

	protected void setItemDetailsFieldValues(SearchResult thumb, CatalogLineVO vo, String description, String medImage,
			String largeImage) {
		thumb.setLargeImageURL(largeImage);
		thumb.setCartImgURL(medImage);
		thumb.setImgURL(medImage);
		thumb.setCatalogLineNumber(vo.getCatalogLineNum());
		thumb.setSelectedUom("");
		thumb.setPrimaryText(description);//CP-9023
		thumb.setSecondaryText(vo.getItemNum());
		thumb.setHref("#");
	}

	//CAP-43196
	protected void setFeatureMap(SearchResult thumb, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			CatalogLineVO vo) throws AtWinXSException {
		boolean hasFeaturedItems = ItemHelper.hasFeaturedItems(appSessionBean, volatileSessionBean, false);

		//CAP-2476 only look up featured item data if they have featured items
		if(hasFeaturedItems)
		{
			thumb.setFeatureFavoriteItemData(ItemUtility.buildFeatureMap(vo, appSessionBean));//CAP-43967
		}
	}


	/**
	 *
	 * @param vo Information about the Catalog line
	 * @return The catalog line item description with/without alt desc.
	 * @throws AtWinXSException
	 */
	//CAP-34043   Method/Logic copied from CustomPointWeb ItemSearchServiceImplHelper.doAlternateDescriptionProcessing()
	//CP-9023 Start
	public String doAlternateDescriptionProcessing(CatalogLineVO vo)
	throws AtWinXSException {

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
					descriptionWithAltDesc = alternateDesc; // CAP-2237
			}
		}
		else
		{
			//CP-11173 escape the base item description so HTML does not render.
			descriptionWithAltDesc = vo.getDescription();
		}

		return descriptionWithAltDesc;
	}

	// CAP-34043   Method/Logic copied from CustomPointWeb ItemSearchServiceImplHelper.removeLineBreaks()
	// CP-9023 End
	// CAP-2237 - added method to replace line breaks only in html
	public String removeLineBreaks(String altDescDisp) {

		int firstLeftLT = (altDescDisp.indexOf("<"));
		if ((firstLeftLT > -1) && (altDescDisp.indexOf(">") > firstLeftLT))
		{
			altDescDisp = altDescDisp.replace("<br>", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<br >", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<br/>", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<br />", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<BR>", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<BR >", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<BR/>", "<div>&nbsp;</div>");
			altDescDisp = altDescDisp.replace("<BR />", "<div>&nbsp;</div>");
		}
		return altDescDisp;
	}



	/**
	 * Method to set the Icon Plus (UDF) field label and Value
	 * @param thumb
	 * @param userSettings
	 */
	// CAP-34043   Method/Logic copied from CustomPointWeb ItemSearchServiceImplHelper.setIconPlusFldAndVal()
	public void setIconPlusFldAndVal(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings) {
		String additonalIconVal = Util.nullToEmpty((userSettings.getAdditonalIconValues().get(thumb.getItemNumber())));
		thumb.setAdditionalFieldValue(Util.isBlankOrNull(additonalIconVal)? NOT_AVAILABLE : additonalIconVal);
		thumb.setAdditionalFieldLabel(userSettings.getAdditionalIconFldLabel());
	}


	// CAP-34043   Method/Logic copied from CustomPointWeb ItemSearchServiceImplHelper.processItemPriceLimit()
	public  void processItemPriceLimit(SearchResult thumb, double itemPrcLimitPerOrder, double unitPriceAmount) {
		if(itemPrcLimitPerOrder > 0 && thumb.isItemOrderable())
		{
			thumb.setItemOrderable(unitPriceAmount <= itemPrcLimitPerOrder);
		}
	}


}
