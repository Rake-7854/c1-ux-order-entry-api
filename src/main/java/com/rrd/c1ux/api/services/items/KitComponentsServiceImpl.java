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
 *  06/05/24    S Ramachandran  CAP-49887   Return components in SingleItemDetailsResponse if the item is a kit template
 */

package com.rrd.c1ux.api.services.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.cell.ItemThumbnailCell.ItemThumbnailCellData;
import com.rrd.custompoint.gwt.search.client.SearchResult;
import com.rrd.custompoint.gwt.titlepanel.searchresults.widget.KitComponentSearchResult;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.KitComponent;
import com.rrd.custompoint.orderentry.entity.KitComponentImpl;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.rrd.custompoint.service.helper.ItemSearchServiceImplHelper;
import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVOKey;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.items.ao.ItemInventoryAssembler;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.ao.MKKitAssembler;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKComponentInfo;
import com.wallace.atwinxs.kits.session.MKHeaderInfo;
import com.wallace.atwinxs.kits.session.MKUOMInfo;
import com.wallace.atwinxs.kits.util.KitsConstants;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;


@Service
public class KitComponentsServiceImpl implements KitComponentItemsService
{
	
	
	public static final String STR_BUNDLE_DEFAULT_QTY = "1";

	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getKitComponents()   
	@Override
	public List<ItemThumbnailCellData> getKitComponents(SessionContainer sc, String kitCustItemNum,
			String kitVendorItemNum, String kitClassification) throws AtWinXSException {

		List<ItemThumbnailCellData> results = new ArrayList<>();
		try {
			
			ApplicationSession appSession = sc.getApplicationSession();
			ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
			OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();

			AppSessionBean appSessionBean = appSession.getAppSessionBean();

			List<KitComponent> components = getKitComponentItems(kitCustItemNum, kitVendorItemNum, kitClassification,
					appSessionBean);
			String routingShippingMethodMsg = Util.nullToEmpty(TranslationTextTag.processMessage(
					appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(),
					TranslationTextConstants.ROUTING_SHIPPING_METHOD_MSG));
			String routingExceedAmountMsg = Util.nullToEmpty(TranslationTextTag.processMessage(
					appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(),
					TranslationTextConstants.ROUTING_EXCEED_AMOUNT_MSG));
			String routingAlwaysRouteOrders = Util.nullToEmpty(TranslationTextTag.processMessage(
					appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(),
					TranslationTextConstants.ROUTING_ORDERS_ALWAYS_MSG));
			String routingMessageReasonsText = Util.nullToEmpty(TranslationTextTag.processMessage(
					appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(),
					TranslationTextConstants.ROUTING_REASONS_MSG));
			boolean hasFeaturedItems = ItemHelper.hasFeaturedItems(appSession.getAppSessionBean(),
					volatileSession.getVolatileSessionBean(), false);
			int displayOrder = 1;
		
			if (appSession.getAppSessionBean().isIconPlusViewEnabled()) {

				oeSessionBean.getUserSettings().setAdditionalIconFldLabel();
				oeSessionBean.getUserSettings().setAdditonalIconValues();
			}

			for (KitComponent kitComp : components) {

				KitComponentSearchResult thumb = null;

				thumb = new KitComponentSearchResult(kitComp.getItemNumber(), kitComp.getDescription());
				thumb.setVendorItemNumber(kitComp.getVendorItemNumber());
				thumb.setKitCompInclusion(getInclusion(kitComp.getInclusion(), appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken()));
				thumb.setKitCompCriticality(getCriticalIndicator(kitComp.getCriticalIndicator()));
				thumb.setKitCompSequencing(kitComp.getSequenceNum());
				thumb.setKitCompUOMs(getUOM(kitComp));
				thumb.setKitCompMinQtys(getMinQty(kitComp));
				thumb.setKitCompMaxQtys(getMaxQty(kitComp));
				thumb.setDisplayOrder(displayOrder++);
				CatalogLineVO vo = getCatalogLineVO(kitComp);
				OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
				getItemThumbnail(thumb, userSettings, appSession, vo, volatileSession, null, routingShippingMethodMsg,
						routingExceedAmountMsg, routingAlwaysRouteOrders, hasFeaturedItems);

				if (kitComp.isWildCardItemInAdmin()) {

					thumb.setStaticData(true);
				}
				thumb.setRoutingMessageReasonsText(routingMessageReasonsText);
				results.add(thumb);
			}

		} catch (AtWinXSException ex) {
			// handle exception
		}
		return results;
	}

	
	// CAP-49887 - Method copied from CPWeb=>ItemSearchServiceImplHelper.getItemThumbnail()
	// removed servletContext of method's input parameter  
	/**
	 * Get the item thumbnail of an item.
	 * 
	 * @param SearchResult                      thumb
	 * @param OEResolvedUserSettingsSessionBean userSettings
	 * @param ApplicationSession                appSession
	 * @param CatalogLineVO                     vo
	 * @param ApplicationVolatileSession        volatileSession
	 * @param String                            token
	 * @return SearchResult
	 * @throws AtWinXSException
	 */
	public SearchResult getItemThumbnail(SearchResult thumb, OEResolvedUserSettingsSessionBean userSettings,
			ApplicationSession appSession, CatalogLineVO vo, ApplicationVolatileSession volatileSession,
			OEItemSearchCriteriaSessionBean criteria, String routingShippingMethodMsg, String routingExceedAmountMsg,
			String routingAlwaysRouteOrders, boolean hasFeaturedItems) throws AtWinXSException {

		// CP-12605 - Refactor codes to reuse the new isExternalESP method in
		// CustomDocumentItem class.
		// CP-13132 changed to make sure the item is a cust doc before checking if it is
		// External ESP to prevent unnecessary DB calls. Also, store result for future
		// use to prevent repetitive DB calls
		CustomDocumentItem cdItem = ObjectMapFactory.getEntityObjectMap().getEntity(CustomDocumentItem.class,
				appSession.getAppSessionBean().getCustomToken());
		boolean isExternalESP = ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(vo.getClassification()) && cdItem
				.isExternalESP(appSession.getAppSessionBean().getSiteID(), vo.getItemNum(), vo.getWcsItemNum());

		// CP-13132 look up order lines once, 
		// so we don't have to do it for every search result
		// CAP-33833 TH - Moved to iCatalog
		Collection<OrderLineVO> orderLines = ItemSearchServiceImplHelper
				.getOrderLines(null != volatileSession.getVolatileSessionBean().getOrderId()
						? volatileSession.getVolatileSessionBean().getOrderId().intValue()
						: AtWinXSConstant.INVALID_ID, appSession.getAppSessionBean().getCustomToken());

		thumb.setItemNumber(vo.getItemNum());
		thumb.setVendorItemNumber(vo.getWcsItemNum());
		// CP-9023 Start
		String description = ItemSearchServiceImplHelper.doAlternateDescriptionProcessing(vo);
		thumb.setItemDescription(description);
		// CP-9023 End
		// CP-8914 RAR - Update codes to display Medium Image instead of the Thumbnail Image.
		String medImage;
		String voMediumImg = vo.getItemMedImgLocURL();
		// CAP-27678 - add large image
		String largeImage = AtWinXSConstant.EMPTY_STRING;

		ICatalog catalogComp = CMCatalogComponentLocator.locate(appSession.getAppSessionBean().getCustomToken());
		ItemImagesVOKey imageKey = new ItemImagesVOKey(appSession.getAppSessionBean().getSiteID(),
				Util.htmlUnencodeQuotes(Util.nullToEmpty(vo.getItemNum())),
				Util.htmlUnencodeQuotes(Util.nullToEmpty(vo.getWcsItemNum())));

		ItemImagesVO imageVO = catalogComp.getImagesForItem(imageKey);
		// CAP-27678 - this will default to blank, not the placeholder
		if (imageVO != null) {

			ItemImagesVOFilter image = new ItemImagesVOFilter(imageVO);
			largeImage = image.getQualifiedItemFullImgLocURL(appSession.getAppSessionBean());
		}

		if (!Util.isBlankOrNull(voMediumImg)) {

			ItemImagesVO medImageVO = new ItemImagesVO(appSession.getAppSessionBean().getSiteID(), vo.getItemNum(),
					vo.getWcsItemNum(), AtWinXSConstant.EMPTY_STRING, voMediumImg, AtWinXSConstant.EMPTY_STRING);
			ItemImagesVOFilter image = new ItemImagesVOFilter(medImageVO);
			medImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
		}
		// CP-9877 EZL Get medium image for non-GSA user
		// CP-10341 Changed check for GSA compliant search
		else if (criteria == null || !catalogComp.isGSACompliantSearch(criteria)) {
			if (imageVO != null) {
				ItemImagesVOFilter image = new ItemImagesVOFilter(imageVO);
				medImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
			} else {
				// extends RemoteServiceServlet to get the context path
				medImage = "images/global/NoImageAvailable.png";
			}
		} else {
			medImage = "images/global/NoImageAvailable.png";
		}

		thumb.setLargeImageURL(largeImage);
		thumb.setCartImgURL(medImage);
		thumb.setImgURL(medImage);
		thumb.setCatalogLineNumber(vo.getCatalogLineNum());
		thumb.setSelectedUom("");
		thumb.setPrimaryText(description);// CP-9023
		thumb.setSecondaryText(vo.getItemNum());
		thumb.setHref("#");

		// CP-8466 RAR - Retrieve and set the Item UOM Options.
		// CP-8942 RAR - Pass the deliveryOptions to determine 
		// if we need to build the UOM Options.
		// CP-9486 RAR - Pass the mergeCode to handle Mail Merge Items.
		// JW - Added view only to the logic to determine to show UOM options or not.
		// CP-8970 removed token parameter
		// CP-13132 added isExternalESP parameter
		thumb.setUomOptions(ItemHelper.getItemUOMOptions(vo.getWcsItemNum(), vo.getItemNum(), vo.getClassification(),
				userSettings, volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean(),
				vo.getDeliveryOption(), vo.getMergeOptionCode(), vo.isViewOnlyFlag(), isExternalESP));

		// RAR - Set other needed information about the item.
		thumb.setAllowFavorites(userSettings.isAllowUserFavoritesInd());
		// CP-8970 changed token from String to an Object
		// CP-13132 Only check if item is a Favorite if user is allowed to do favorites
		if (userSettings.isAllowUserFavoritesInd()) {
			thumb.setFavorite(ItemHelper.isItemFavorite(appSession.getAppSessionBean().getSiteID(),
					appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getProfileNumber(),
					vo.getWcsItemNum(), vo.getItemNum(), appSession.getAppSessionBean().getCustomToken()));
		}

		// CP-13132 Don't make DB call to check if the user has the service. Use the
		// service list in AppSessionBean
		thumb.setCheckInventoryEnabled(appSession.getAppSessionBean().hasService(AtWinXSConstant.INVENTORY_SERVICE_ID));
		thumb.setManageItemsEnabled(appSession.getAppSessionBean().hasService(AtWinXSConstant.ITEM_SERVICE_ID));

		thumb.setEdocEnabled(vo.isEDocFlag());
		// CP-9607 RAR - Fixed issue on the eDoc URL.
		thumb.setEdocUrl(
				appSession.getAppSessionBean().getQualifiedImageSrc(vo.getEdocUrl(), AtWinXSConstant.ITEM_EDOC));
		thumb.setFileDeliveryOptionCd(vo.getDeliveryOption());
		thumb.setSpecialItemTypeCd(String.valueOf(vo.getSpecialItemType()));
		thumb.setItemAlwaysRoute(vo.alwaysRouteFlagLineReview());
		thumb.setItemRouteQuantity(vo.getApprovalLimtQtyLineReview());
		// CP-8994 set showVendorItemNum properly
		thumb.setShowVendorItemNum(appSession.getAppSessionBean().isShowWCSSItemNumber());
		thumb.setItemClassification(vo.getClassification());

		// CAP-2476 only look up featured item data if they have featured items
		if (hasFeaturedItems) {

			thumb.setFeatureMap(ItemHelper.buildFeatureMap(vo, appSession.getAppSessionBean()));
		}

		thumb.setItemOrderable(vo.isItemOrderable());
		thumb.setViewOnlyFlag(vo.isViewOnlyFlag());
		thumb.setInvalidItemFlag(vo.isInvalidItemFlag());
		thumb.setAlwaysRoute((vo.isAlwaysRouteFlag() && (userSettings.getAssignedApprovalQueue() > 0)));
		thumb.setRouteQuantity((int) vo.getApprovalLimitQty());
		thumb.setAllowEFD(userSettings.isAllowEFD());
		thumb.setAllowPrintOverride(userSettings.isAllowPrintOverride());
		thumb.setShowRAInfo(
				((userSettings.isShowRoutingInfo() && userSettings.isRoutingAvailable() && userSettings.isSubjToRnA())
						&& !volatileSession.getVolatileSessionBean().isKitTemplateMode()));

		thumb.setKitContainerLocations(volatileSession.getVolatileSessionBean().getKitTemplateContainerLocations());
		thumb.setKitContainerLocationNames(
				volatileSession.getVolatileSessionBean().getKitTemplateContainerLocationNames());
		thumb.setKitTemplateMode(volatileSession.getVolatileSessionBean().isKitTemplateMode());

		thumb.setAlwaysRouteOrders(userSettings.isAlwaysRouteOrders());

		if (volatileSession.getVolatileSessionBean().isKitTemplateMode()) {

			Logger logger = LoggerFactory.getLogger(ItemSearchServiceImplHelper.class);
			// make sure that we pull info from existing kit session and not reload here
			KitSession kitSession = null;

			try {

				// if we have no kit session, we should not be in kit template mode
				kitSession = (KitSession) SessionHandler.loadSession(volatileSession.getSessionID(),
						AtWinXSConstant.KITS_SERVICE_ID);
			} catch (AtWinXSWrpException eofex) {

				// CAP-18657 Replaced printstacktrace() call with Logger
				logger.error(Util.class.getName() + " - " + eofex.getMessage(), eofex);
				throw eofex;
			}

			thumb.setAllowDupCustDoc(kitSession.getAdminBean().isAllowDuplicateCustDocs()); // CAP-2589
			thumb.setItemInKitInd(ItemHelper.getItemInKitIndicator(
					volatileSession.getVolatileSessionBean().getSelectedKitComponents(), vo.getItemNum(),
					vo.getWcsItemNum(), appSession.getAppSessionBean().getCustomToken(),
					appSession.getAppSessionBean().getDefaultLocale()));

			thumb.setItemQuantity(
					ItemHelper.getWildCardDefaultQuantity(vo.getWcsItemNum(), appSession.getAppSessionBean(),
							volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean().getCustomToken()));
			thumb.setDisplayQuantityAsText((!Util.isBlankOrNull(thumb.getItemQuantity())
					&& !ItemHelper.isCustomizableItem(vo.getClassification(), vo.getWcsItemNum(),
							appSession.getAppSessionBean(), appSession.getPunchoutSessionBean())));
		} else {
		
			// CP-9225 use admin default quantity
			thumb.setItemQuantity(Integer.toString(appSession.getAppSessionBean().getSiteDefaultQty()));
			// CP-13132 changed parameters to ItemHelper.isItemInCart
			// CP-13533 - restored old code from prior version of isItemInCart
			thumb.setItemInCart((null != volatileSession.getVolatileSessionBean().getOrderId()
					&& volatileSession.getVolatileSessionBean().getOrderId() > -1) && 
							ItemHelper.isItemInCart(appSession.getAppSessionBean(), vo.getItemNum(), vo.getWcsItemNum(), orderLines)); 

			// CP-12787 - Set value of DisplayQuantityAsText only if Xert item.
			// CP-12605 - Reuse the new isExternalESP method in CustomDocumentItem class.
			// CP-13132 use local variable for isExternalESP so we don't make repetitive DB
			// calls
			if (isExternalESP) {

				thumb.setDisplayQuantityAsText(isExternalESP);
			}

			// CP-13104 - Set Quantity to 1 for Bundle Item
			if (ItemConstants.ITEM_CLASS_BUNDLE.equals(Util.nullToEmpty(vo.getClassification()))) {

				thumb.setItemQuantity(STR_BUNDLE_DEFAULT_QTY);
				thumb.setDisplayQuantityAsText(true);
			}
		}

		thumb.setShowAvailability(userSettings.isShowOrderLineAvailability());
		thumb.setShowPrice(userSettings.isShowOrderLinePrice());
		thumb.setRouteDollarAmount(userSettings.getRouteDollarAmount());
		thumb.setRouteOnShipMethodChange(userSettings.isRouteOnShipMethodChange());

		String routingAmtLevel = Util.getStringFromCurrency(userSettings.getRouteDollarAmount(),
				appSession.getAppSessionBean().getCurrencyLocale(),
				appSession.getAppSessionBean().getApplyExchangeRate()).getAmountText();

		thumb.setRouteDollarAmountText(routingExceedAmountMsg + " " + routingAmtLevel + ".");
		thumb.setRouteOnShipMethodChangeText(routingShippingMethodMsg);
		thumb.setAlwaysRouteOrders(userSettings.isAlwaysRouteOrders());
		thumb.setAlwaysRouteOrdersText(routingAlwaysRouteOrders);
		thumb.setDisplayOrder(vo.getDisplayOrder());// CP-11027

		// CP-9635 set replacement item data
		thumb.setReplacementItemNumber(vo.getReplaceItemNum());
		thumb.setUnorderableDate(
				Util.getStringFromDate(vo.getNotOrderableDt(), appSession.getAppSessionBean().getDefaultLocale()));
		thumb.setUnorderableReason(vo.getReasonNotOrderableTxt());
		thumb.setShowAdditionalItemStatInfo(userSettings.isShowAddlItemStatInfo());

		// Check if there's primary attribute assigned
		thumb.setHasPrimaryAttrib(false);
		if (userSettings.getPrimaryAttribute() > 0) {

			thumb.setHasPrimaryAttrib(true);

			// CAP-2774, CAP-3248
			// only populate the attribute value if there is a primary attribute selected in
			// the user settings
			String value = vo.getPrimaryCategorization();
			thumb.setCategorizationAttribVal(value);
		}
		// Check if the page is in Catalog page
		thumb.setDisplayAttrVal(false);

		if (criteria != null) {

			if (criteria.isFaveItemsOnly() || criteria.isFeaturedItemsOnly()) {

				thumb.setDisplayAttrVal(true);
			}
		} else {

			thumb.setDisplayAttrVal(true);
		}

		// CAP-16158 - attempt to populate custom columns if any exist
		catalogComp.getCustomColumnSearchResults(thumb.getVendorItemNumber(), thumb.getItemNumber(),
				appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getCorporateNumber(), vo,
				thumb.getCustomColumnData());

		// CAP-15083 SRN - Attempt to populate the additional field name and value if
		// icon plus view enabled
		if (appSession.getAppSessionBean().isIconPlusViewEnabled()) {

			ItemSearchServiceImplHelper.setIconPlusFldAndVal(thumb, userSettings);
		}

		// CAP-15939 JBS unit price hard stop
		ItemSearchServiceImplHelper.processItemPriceLimit(thumb, userSettings.getItemPrcLimitPerOrder(), vo.getUnitPriceAmt());
		return thumb;
	}
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getCriticalIndicator() 
	private String getCriticalIndicator(String indicator) {

		String criticalInstruction = null;

		if (KitsConstants.CRITICAL_ITEM_INDICATOR_CRITICAL.equals(indicator)) {

			criticalInstruction = KitsConstants.CRITICAL_ITEM_INDICATOR_CRITICAL_TXT;
		} else if (KitsConstants.CRITICAL_ITEM_INDICATOR_NON_CRITICAL.equals(indicator)) {

			criticalInstruction = KitsConstants.CRITICAL_ITEM_INDICATOR_NON_CRITICAL_TXT;
		} else if (KitsConstants.CRITICAL_ITEM_INDICATOR_BACK_ORDER_TXT.equals(indicator)) {

			criticalInstruction = KitsConstants.CRITICAL_ITEM_INDICATOR_BACK_ORDER_TXT;
		} else if (KitsConstants.CRITICAL_ITEM_INDICATOR_USER_DEFINED.equals(indicator)) {

			criticalInstruction = KitsConstants.CRITICAL_ITEM_INDICATOR_USER_DEFINED_TXT;
		} else {

			criticalInstruction = "";
		}

		return criticalInstruction;
	}
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getInclusion()
	private String getInclusion(String inclusion, Locale locale, CustomizationToken customToken)
			throws AtWinXSException {
	
		String inclusionTxt = null;
		if ("Y".equals(inclusion)) {
		
			// CP-11880 [CCB] Issue #50
			inclusionTxt = TranslationTextTag.processMessage(locale, customToken, "keyProfileRequired");
		} else if ("N".equals(inclusion)) {
			
			// CP-11880 [CCB] Issue #50
			inclusionTxt = TranslationTextTag.processMessage(locale, customToken, "optionalLbl");
		} else if ("S".equals(inclusion)) {
			
			// CP-11880 [CCB] Issue #50
			inclusionTxt = TranslationTextTag.processMessage(locale, customToken, "suggestedLbl");
		} else {
			
			inclusionTxt = "";
		}
		return inclusionTxt;
	}
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getKitComponentItems()
	private List<KitComponent> getKitComponentItems(String kitCustItemNum, String kitVendorItemNum,
			String kitClassification, AppSessionBean asb) throws AtWinXSException {

		ArrayList<KitComponent> compCollection = new ArrayList<>();
		if (ItemConstants.ITEM_CLASS_KIT.equals(kitClassification)) {

			KitSession kitSession = getKitSession(kitClassification, kitCustItemNum, kitVendorItemNum, asb);
			MKComponentInfo[] components = kitSession.getComponents();

			// CAP-12651 - if locked sequence, get components in that order
			if (components == null) {
				
				components = new MKComponentInfo[0];
			} else if (components.length > 0) {
				
				MKComponentInfo[] sortedComponents = new MKComponentInfo[components.length];
				// CAP-13897 - sort by customer item number if a master component
				if (Boolean.TRUE.equals(kitSession.getHeader().getKitMasterComponentInd())) {
					
					sortedComponents = kitSession.getComponentsSortedByCustItem();
				} else {
					
					java.util.Vector[][] componentsSorted = kitSession.getComponentsSortedByWeightAndLocationNew(false);
					int index = 0;
					for (int i = 0; i < componentsSorted.length; i++) {
						for (int compIndex = 0; compIndex < componentsSorted[i][0].size(); compIndex++) {
							sortedComponents[index++] = (MKComponentInfo) componentsSorted[i][0].get(compIndex);
						}
					}
				}
				components = sortedComponents;
			}

			for (MKComponentInfo kitComp : components) {
			
				KitComponentImpl comp = new KitComponentImpl();
				String compCustItem = kitComp.getCustomerItemNum();
				comp.setItemNumber((Util.isBlankOrNull(compCustItem) ? "" : compCustItem));
				comp.setDescription(kitComp.getCustomerItemDesc());
				comp.setVendorItemNumber(kitComp.getWCSSItemNum());
				comp.setCriticalIndicator(kitComp.getTmpltComponentItemCriticalInd());
				comp.setInclusion(kitComp.getRequiredItemInd());
				int sequence = -1;
				if (!"N".equals(kitSession.getSeqTypeInd())) {
					String seqSequence = kitComp.getItemSequenceNum();
					if (!kitComp.isWildCardItemInAdmin()) {
						sequence = Util.isBlankOrNull(seqSequence) ? -1 : Integer.parseInt(seqSequence);
					}
				}
				comp.setSequenceNum(sequence);
				String seqLocation = kitComp.getSequenceLocationID();
				comp.setLocationId(Util.isBlankOrNull(seqLocation) ? -1 : Integer.parseInt(seqLocation));
				comp.setWildCardItemInAdmin(kitComp.isWildCardItemInAdmin());
				MKUOMInfo[] compUOMs = kitComp.getItemUOMs();
				ArrayList<MKUOMInfo> uomColl = null;
				if (compUOMs != null) {
					uomColl = new ArrayList<>(compUOMs.length);
						Collections.addAll(uomColl,compUOMs);
				}
				comp.setComponentAvailUOM(uomColl);
				compCollection.add(comp);
			}

		} else if (ItemConstants.ITEM_CLASS_KIT_TEMPLATE.equals(kitClassification)) {
			KitSession kitSession = getKitSession(kitClassification, kitCustItemNum, kitVendorItemNum, asb);

			MKComponentInfo[] components = kitSession.getComponents();

			if (components == null) {
				
				components = new MKComponentInfo[0];
			}
			// CAP-12651 - if locked sequence, get components in that order
			else if (components.length > 0) {
				
				MKComponentInfo[] sortedComponents = new MKComponentInfo[components.length];
				java.util.Vector[][] componentsSorted = kitSession.getComponentsSortedByWeightAndLocationNew(false);
				int index = 0;
				for (int i = 0; i < componentsSorted.length; i++) {
					for (int compIndex = 0; compIndex < componentsSorted[i][0].size(); compIndex++) {
						sortedComponents[index++] = (MKComponentInfo) componentsSorted[i][0].get(compIndex);
					}
				}
				components = sortedComponents;
			}

			for (MKComponentInfo kitComp : components) {
			
				KitComponentImpl comp = new KitComponentImpl();
				String compCustItem = kitComp.getCustomerItemNum();
				comp.setItemNumber((Util.isBlankOrNull(compCustItem) ? "" : compCustItem));
				String itemDesc = kitComp.getCustomerItemDesc();
				if (kitComp.isWildCardItemInAdmin()) {
					
					// CP-10139 Use translated text here.
					Map<String, Object> replaceMap = new HashMap<>();
					replaceMap.put("{numWildCards}", kitSession.getHeader().getNumOfWildCards());
					// CP-8970 changed token from String to an Object
					itemDesc = Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(),
							asb.getCustomToken(), "wild_card_item_desc", replaceMap)); // CP-11880 [CCB] Issue #50

					// CP-12478 RAR - Decode encoded html.
					itemDesc = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(itemDesc);
				}
				comp.setDescription(itemDesc);
				comp.setVendorItemNumber(kitComp.getWCSSItemNum());
				comp.setCriticalIndicator(kitComp.getTmpltComponentItemCriticalInd());
				comp.setInclusion(kitComp.getRequiredItemInd());
				int sequence = -1;
				if (!"N".equals(kitSession.getSeqTypeInd()) &&  (!kitComp.isWildCardItemInAdmin())) {
				
						sequence = kitComp.getTmpltComponentItemSeqNR();
				}
				comp.setSequenceNum(sequence);
				comp.setWildCardItemInAdmin(kitComp.isWildCardItemInAdmin());
				MKUOMInfo[] compUOMs = kitComp.getItemUOMs();
				ArrayList<MKUOMInfo> uomColl = null;
				if (compUOMs != null) {
					uomColl = new ArrayList<>(compUOMs.length);
					Collections.addAll(uomColl,compUOMs);
				}
				comp.setComponentAvailUOM(uomColl);
				compCollection.add(comp);
			}
		} else {
			return new ArrayList<>(0);
		}
		return compCollection;

	}
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getKitSession()
	protected KitSession getKitSession(String kitClassification, String kitCustItemNum, String kitVendorItemNum,
			AppSessionBean asb) throws AtWinXSException {

		KitSession kitsession = null;
		MKKitAssembler asmKit = new MKKitAssembler(asb.getCustomToken(), asb.getDefaultLocale());

		if (ItemConstants.ITEM_CLASS_KIT.equals(kitClassification)) {

			kitsession = new KitSession();
			MKHeaderInfo header = new MKHeaderInfo();
			header.setWCSSItemNum(kitVendorItemNum);
			kitsession.setHeader(header);
			// CP-11779 - load differently depending on status

			String status = asmKit.getKitStatus(asb.getSiteID(), kitCustItemNum);
			boolean masterComponent = false;

			// CP-11779 - pull values from XRT002 for master and virtual master
			if (!Util.isBlankOrNull(kitVendorItemNum)) {

				ItemInventoryAssembler invAssembler = new ItemInventoryAssembler(asb.getCustomToken(),
						asb.getDefaultLocale(), asb.getApplyExchangeRate(), asb.getCurrencyLocale());
				masterComponent = invAssembler.isMasterComponent(kitVendorItemNum);
			}

			if ((ItemConstants.KIT_STATUS_ASSOCIATIONS.equals(status))
					|| (ItemConstants.KIT_STATUS_FULL.equals(status))) {

				asmKit.loadKit(kitsession, asb, kitCustItemNum, true, false);
			} else if (((ItemConstants.KIT_STATUS_STUB.equals(status))
					|| (ItemConstants.KIT_STATUS_DELETED.equals(status))) && (masterComponent)) {

				asmKit.loadKit(kitsession, asb, kitCustItemNum, true, true);
			} else { // if neither of these, don't load the kit - there IS NO INFO

				header.setMKDefinedKitInd(false);
			}
		} else if (ItemConstants.ITEM_CLASS_KIT_TEMPLATE.equals(kitClassification)) {

			kitsession = new KitSession();
			kitsession.init(asb);
			asmKit.loadKitTemplate(kitsession, asb, kitCustItemNum, asb.getSiteID());

			// CP-12893 - remove bad components from view
			for (int i = 0; kitsession.getComponents() != null && i < kitsession.getComponents().length; i++) {

				if (kitsession.getComponents()[i].isInvalidToOrder()) {

					asmKit.removeComponent(kitsession, i);
					i--; // need to redo the index since deleting current value
				}
			}
		}
		return kitsession;
	}
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getUOM()
	private Collection<String> getUOM(KitComponent kitComp) {
	
		Collection<MKUOMInfo> uoms = kitComp.getComponentAvailUOM();
		ArrayList<String> list = new ArrayList<>(5);
		StringBuilder buffer = new StringBuilder();
		
		if (uoms != null) {
			for (MKUOMInfo uom : uoms) {
				if (Boolean.TRUE.equals(uom.getUOMIndAvail())) {
					if (kitComp.isWildCardItemInAdmin()) {
		
						buffer.append(uom.getUomDisplay());
					} else {
						
						buffer.append(uom.getUOMCd()).append(" of ").append(uom.getUOMFactor());
					}
					list.add(buffer.toString());
					buffer.delete(0, buffer.length());
				}
			}
		}
		return list;
	}
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getMinQty()
	private Collection<String> getMinQty(KitComponent kitComp) {
	
		Collection<MKUOMInfo> uoms = kitComp.getComponentAvailUOM();
		ArrayList<String> list = new ArrayList<>(5);
		
		if (uoms != null) {
			for (MKUOMInfo uom : uoms) {
				if (Boolean.TRUE.equals(uom.getUOMIndAvail())) {
		
					list.add(uom.getUOMMinimumQty());
				}
			}
		}
		return list;
	}
	
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getMaxQty()
	/**
	 * Get CatalogLineVO object of the Kit component.
	 * 
	 * @param KitComponent kitComp
	 * @return CatalogLineVO
	 */
	private Collection<String> getMaxQty(KitComponent kitComp) {
	
		Collection<MKUOMInfo> uoms = kitComp.getComponentAvailUOM();
		ArrayList<String> list = new ArrayList<>(5);
		
		if (uoms != null) {
			
			for (MKUOMInfo uom : uoms) {
			
				if (Boolean.TRUE.equals(uom.getUOMIndAvail())) {

					list.add(uom.getUOMMaximumQty());
				}
			}
		}
		return list;
	}
	
	
	// CAP-49887 - Method copied from CPWeb=>KitComponentsService.getCatalogLineVO()
	/**
	 * Get CatalogLineVO object of the Kit component.
	 * 
	 * @param KitComponent kitComp
	 * @return CatalogLineVO
	 */
	private CatalogLineVO getCatalogLineVO(KitComponent kitComp) {

		return new CatalogLineVO(-1, -1, 		// catalogLineNum,
				kitComp.getVendorItemNumber(), 	// wcsItemNum,
				kitComp.getItemNumber(), 		// itemNum,
				kitComp.getDescription(), 		// description,
				-1, 	// displayOrder,
				true, 	// viewOnlyFlag,
				false, 	// alwaysRouteFlag,
				0.0, 	// approvalLimitQty,
				false, 	// alwaysKeepFlag,
				false, 	// invalidItemFlag,
				false, 	// eDocFlag,
				"", 	// edocUrl,
				"", 	// unspscCd,
				-1, 	// catalogID,
				new Date(), 	// changeTmstmp,
				-1, 	// approvalLimtQtyLineReview,
				false, 	// alwaysRouteFlagLineReview,
				"", 	// deliveryOption,
				null 	// efdSourceSettings
		);
	}
}
