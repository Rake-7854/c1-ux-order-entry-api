
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#										Description
 * 	--------	-----------		----------------------------------------	------------------------------
 *	06/05/2024	Sakthi M		CAP-49782	               					 Initial creation
 *  06/27/24	Krishna Natarajan	CAP-50184								 Handled the volatile session bean with setting -1 for Order ID, if found null (CP code refers to order ID)
 *  06/27/24	Krishna Natarajan	CAP-50540								 Handled the noImage for C1UX no image items
 *  07/01/24	Krishna Natarajan	CAP-50540								 Added logic to add FeatureFavoriteData and RoutingBadge to the ItemThumbnailCellData
 *	07/09/24	L De Leon		CAP-50834									 Modified populateSuggestItemsResults() method to check if item is customizable when setting value for itemInCart field
 */

package com.rrd.c1ux.api.services.suggesteditems;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.items.COSuggestedItemSearchResult;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.CMCompanionItemComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.gwt.titlepanel.searchresults.widget.SuggestedItemSearchResult;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.Item;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.rrd.custompoint.service.helper.ItemSearchServiceImplHelper;
import com.wallace.atwinxs.catalogs.ao.CMCompanionItemAssembler;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.util.CatalogConstant;
import com.wallace.atwinxs.catalogs.vo.CompanionItemRelationshipCompositeVO;
import com.wallace.atwinxs.catalogs.vo.CompanionItemRelationshipVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVOKey;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PageTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.Util.ContextPath;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.ICompanionItem;
import com.wallace.atwinxs.interfaces.IOECompanionItemComponent;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemResultsExtendedVO;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemResultsVO;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemSearchCriteriaVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

@Service
public class SuggestedItemsServiceImpl  extends BaseOEService implements SuggestedItemsService{
	
	private static final Logger logger = LoggerFactory.getLogger(SuggestedItemsServiceImpl.class);
	
	private final CMCompanionItemComponentLocatorService cmCompanionItemComponentLocatorService;
	
	protected SuggestedItemsServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			CMCompanionItemComponentLocatorService cmCompanionItemComponentLocatorService) {
		super(translationService, objectMapFactoryService);
		this.cmCompanionItemComponentLocatorService=cmCompanionItemComponentLocatorService;
	}

	@Override
	public CatalogItemsResponse getSuggestedItems(SessionContainer sc, String vItemNum, String cItemNum, String lineNum,
			boolean isIgnoreSessionSave) throws AtWinXSException, IllegalAccessException, InvocationTargetException {
		
		
		boolean isOrderLevel = false;
		if(Util.isBlank(vItemNum) && Util.isBlank(cItemNum))//CAP-19456
		{
			isOrderLevel = true;
		}

		

		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		
		CatalogItemsResponse response=new CatalogItemsResponse();
		
		List<ItemThumbnailCellData> suggestedItemsResults = null;

		if (volatileSessionBean != null)
		{
			
				Collection<OECompanionItemResultsExtendedVO> suggestedVOs = null;
				OEShoppingCartAssembler sca = new OEShoppingCartAssembler(appSessionBean.getCustomToken(), appSessionBean.getCurrencyLocale(), 
						appSessionBean.getApplyExchangeRate());
				IOECompanionItemComponent companionComp = objectMapFactoryService.getComponentObjectMap()
					.getObject(IOECompanionItemComponent.class, appSessionBean.getCustomToken());
				
				//order level
				if(isOrderLevel)
				{
					
					suggestedVOs=getOrderLevelSuggestedItem(oeSessionBean, sc, sca, companionComp, oeSession);
	
				}
				//item level
				else
				{
					suggestedVOs=getItemLevelSuggestedItem(sc, oeSession, isIgnoreSessionSave, cItemNum, vItemNum, lineNum);
					
				}
				
				//CAP-315 - Pass the indicator if we are in item level suggested popup.
				suggestedItemsResults = populateSuggestItemsResults(suggestedVOs, appSession, applicationVolatileSession, oeSession, isIgnoreSessionSave);
				setImageURL(suggestedItemsResults);//CAP-50540
				response.setSuccess(true);
				response.setItemThumbnailCellData(suggestedItemsResults);
	     	}else {
		     	response.setSuccess(false);
		   }
		return response;
	}
	
	//CAP-50540
	public void setImageURL(List<ItemThumbnailCellData> suggestedItemsResults) {
		for(ItemThumbnailCellData loopThroughThumbnailCellData: suggestedItemsResults) {
			if (null!=loopThroughThumbnailCellData.getImgURL() && loopThroughThumbnailCellData.getImgURL().contains(ModelConstants.CP_NO_IMAGE_NO_CONTEXT)) {
				loopThroughThumbnailCellData.setImgURL(ModelConstants.C1UX_NO_IMAGE_MEDIUM);
			}
		}
	}
	
	
	public OECompanionItemSearchCriteriaVO createSearchCriteriaVO(AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSessionBean, int orderId)
	{
		return new OECompanionItemSearchCriteriaVO(OrderEntryConstants.TEXT_ONLY_VIEW, OrderEntryConstants.COMPANION_ITEMS_DEFAULT_PAGE,
					OrderEntryConstants.COMPANION_ITEMS_DEFAULT_SORT, oeOrderSessionBean
					.getUserSettings().isAllowCatalogViewSelection(), OrderEntryConstants
					.COMPANION_ITEM_MAX_ITEMS_TEXT_ONLY_VIEW, appSessionBean.getSessionID(),
					appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getGroupName(), orderId);
		
	}
	
	protected Collection<OECompanionItemResultsExtendedVO> filterItemLevelSuggestedItems(Collection<OECompanionItemResultsExtendedVO> suggestedVOs)
	{
		Collection<OECompanionItemResultsExtendedVO> finalSuggestedVOs = new ArrayList<>();
		
		int catelogLineNum = -999;
		
		for(OECompanionItemResultsExtendedVO suggestedVO : suggestedVOs)
		{
			if(!OrderEntryConstants.COMPANION_ITEMS_ITEM_LVL_IND.equals(suggestedVO.getRelationshipLvlCd()))
			{
				suggestedVO.setCatalogLineNum(--catelogLineNum);
				finalSuggestedVOs.add(suggestedVO);
			}
		}
		
		return finalSuggestedVOs;
	}
	
	
	private Collection<OECompanionItemResultsExtendedVO> getExtendedVOs(Collection<OECompanionItemResultsVO> resultsVOs, int siteID, CustomizationToken token) throws AtWinXSException
	{
		ICompanionItem companionComp = cmCompanionItemComponentLocatorService.locate(token);
		Collection<OECompanionItemResultsExtendedVO> extendedVos = new ArrayList<>();
		CompanionItemRelationshipCompositeVO cirVO;
		OECompanionItemResultsExtendedVO extendedVo;
		for(OECompanionItemResultsVO vo : resultsVOs)
		{
			try
			{
				cirVO = companionComp.getCompanionAssignmentsByID(siteID, vo.getRelationshipID());
				
				//CP-9184
				String description = doAlternateDescriptionProcessing(vo);

				//create an create a OECompanionItemResultsExtendedVO and update the Message HTML
				//CP-9184
				extendedVo = new OECompanionItemResultsExtendedVO(vo.getCompanionItemKey().getApplicationSessionID(), 
						vo.getCompanionItemKey().getCompanionItemResultsNum(), vo.getRelationshipID(), vo.getRelationshipLvlCd(),
						vo.getWcsItemNum(), vo.getItemNum(), description, vo.isInCart(), vo.isViewOnlyFlag(), 
						vo.alwaysRouteFlagLineReview(), vo.getApprovalLimitQty(), vo.getUnspscCd(), vo.getSpecialItemType(), 
						false, false, vo.isInCatalog(), vo.getClassification(),	vo.getApprovalLimtQtyLineReview(),
						vo.getCompanionItemRouteApprvInd(), cirVO.getCompanionLevel(), cirVO.getShowRatio(), //CP-10882
						cirVO.getCompanionRatio(), cirVO.getItemRatio(), cirVO.getMessageHTML(), vo.getEdocUrl(), vo.getDeliveryOption());
				
				//CAP-15939
				extendedVo.setUnitPriceAmt(vo.getUnitPriceAmt());
				
				extendedVos.add(extendedVo);
			} catch (AtWinXSException e)
			{
				// CAP-16460 call to logger.
				logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
			}
			
		}
		return extendedVos;
	}
	
	public String doAlternateDescriptionProcessing(OECompanionItemResultsVO vo)	{
				String alternateDesc = Util.htmlEncodeQuotes(Util.nullToEmpty(vo.getAltCatData().getAlternateCatalogDesc()));
				String altDescDispType = vo.getAltCatData().getAlternateCatalogDescDisplayType();
				
				String descriptionWithAltDesc = "";
				String altDescDisp = "";
				
				if (alternateDesc.length() > 0
						&& (altDescDispType
								.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_APPEND) || altDescDispType
								.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_REPLACE)))
				{
					altDescDisp = alternateDesc;
								
					//Append to or replace the description
					if(altDescDispType.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_APPEND))
					{
							descriptionWithAltDesc = vo.getDescription() + " - " + altDescDisp;
					}
					else if(altDescDispType.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_REPLACE))
					{
							descriptionWithAltDesc = altDescDisp;
					}
				}
				else
					descriptionWithAltDesc = vo.getDescription();
				
				return descriptionWithAltDesc;
			}
	
	protected Collection<OECompanionItemResultsVO> searchItemLevelSuggestedItems(String cItemNum, String vItemNum, String lineNum, VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean)
	{
		Collection<OECompanionItemResultsVO> resultsVOs = null;
		CMCompanionItemAssembler cia = new CMCompanionItemAssembler(appSessionBean.getCustomToken(), appSessionBean.getCurrencyLocale());
		
		try
		{
			String attrSQL = cia.loadCompanionItemSiteAttrFilterSQL(volatileSessionBean.getSelectedSiteAttribute(),
					appSessionBean.getProfileAttributes(), oeSessionBean.getUsrSrchOptions(), appSessionBean.getSiteID(), true);
			//CAP-2183 8.1.19 [SRN] changed the method called to load the Suggested Items of the order on the shopcart when Companion Items Display is set to At Line Level
			if(volatileSessionBean.getOrderId()==null) {//CAP-50184
				volatileSessionBean.setOrderId(-1);
			}
			resultsVOs = cia.retrieveCompanionItemsByUseLevelItemLevel(0, volatileSessionBean, appSessionBean,
					oeSessionBean.getUserSettings(), attrSQL, false, vItemNum, cItemNum, lineNum);
			// CAP-5538 8.1.20[SRN] Added lineNum
		}
		catch (AtWinXSException e)
		{
			// CAP-16460 call to logger.
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
		
		return resultsVOs;
	}
	
	
	public List<ItemThumbnailCellData> populateSuggestItemsResults(Collection<OECompanionItemResultsExtendedVO> suggestedVOs, ApplicationSession appSession, ApplicationVolatileSession volatileSession, OrderEntrySession oeSession, boolean isIgnoreSessionSave) throws IllegalAccessException, InvocationTargetException
	{
		
		
		//CP-12605 - Refactor codes to reuse the new isExternalESP method in CustomDocumentItem class.
		CustomDocumentItem cdItem = objectMapFactoryService.getEntityObjectMap().getEntity(CustomDocumentItem.class, appSession.getAppSessionBean().getCustomToken());
		
		//CAP-15939 JBS user settings
		OEResolvedUserSettingsSessionBean userSettings = oeSession.getOESessionBean().getUserSettings();
		List<ItemThumbnailCellData> suggestedItemResults = new ArrayList<>();
		ICatalog catalogComp = objectMapFactoryService.getComponentObjectMap().getObject(ICatalog.class, appSession.getAppSessionBean().getCustomToken());
		SuggestedItemSearchResult thumb = null;
		try
		{
			if (suggestedVOs != null && !suggestedVOs.isEmpty())
			{	
				ItemImagesVO imageVO = null;
				ItemImagesVOFilter image = null;
				ItemImagesVOKey imageVOKey;
				String mediumImage = "";
        		String lineLevelRouteMessage=""; 
        		String orderLevelRouteMessage="";
                
				int index = 0;
				
				//CP-13132 look up order lines once, so we don't have to do it for every search result
				OECatalogAssembler assembler = new OECatalogAssembler(appSession.getAppSessionBean().getCustomToken(), appSession.getAppSessionBean().getDefaultLocale());
				Collection<OrderLineVO> orderLines = assembler.getOrderLines(null != volatileSession.getVolatileSessionBean().getOrderId() ? volatileSession.getVolatileSessionBean().getOrderId().intValue() : AtWinXSConstant.INVALID_ID);

				//CAP-2476 look to see if they have featured items
				//CAP-23736 Added hideHiddenFeatured, doesn't show featured icons from XST392 with hide indicator 'Y'.
				boolean hasFeaturedItems = ItemHelper.hasFeaturedItems(appSession.getAppSessionBean(), volatileSession.getVolatileSessionBean(), false);
				
				//CAP-15083 SRN Set the allocation field label and values if Icon plus view enabled
				if(appSession.getAppSessionBean().isIconPlusViewEnabled())
				{
					oeSession.getOESessionBean().getUserSettings().setAdditionalIconFldLabel();
					oeSession.getOESessionBean().getUserSettings().setAdditonalIconValues();
				}
				
				for (OECompanionItemResultsExtendedVO vo : suggestedVOs)
				{
					//CAP-50540
					Item item = objectMapFactoryService.getEntityObjectMap().getEntity(Item.class,
							appSession.getAppSessionBean().getCustomToken());
					item.setItemNumber(vo.getItemNum());
					item.setVendorItemNumber(vo.getWcsItemNum());
					item.populateFeatureFavoriteData(appSession.getAppSessionBean());

					//CP-13132 store local variable for isExternalESP to prevent repetitive DB calls
					boolean isExternalESP = ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(vo.getClassification()) && cdItem.isExternalESP(appSession.getAppSessionBean().getSiteID(), vo.getItemNum(), vo.getWcsItemNum());
					
					//CP-9184
					String description = this.doAlternateDescriptionProcessing(vo);
					
					thumb = new SuggestedItemSearchResult(vo.getItemNum(), description);//CP-9184
					imageVOKey = new ItemImagesVOKey(appSession.getAppSessionBean().getSiteID(), vo.getItemNum(), vo.getWcsItemNum());
					imageVO = catalogComp.getImagesForItem(imageVOKey);
					if (imageVO != null
							&& (!Util.isBlankOrNull(imageVO.getItemFullImgLocURL())
								|| !Util.isBlankOrNull(imageVO.getItemMedImgLocURL())
								|| !Util.isBlankOrNull(imageVO.getItemThumbImgLocURL())))
					{
						image = new ItemImagesVOFilter(imageVO);
						mediumImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
					}
					else
					{
						mediumImage = getContextPath() + "/images/global/NoImageAvailable.png";
					}
					thumb.setCartImgURL(mediumImage);
					thumb.setImgURL(mediumImage);
					//CAP-1199 removed call to getPrice.  We don't display the price and user must click button to get price.
					thumb.setInStockCol(Integer.toString(vo.getItemRatio()));
					thumb.setHref("#");
					//CP-9168 Start
					StringBuilder suggestedBecause = new StringBuilder(150);
					suggestedBecause.append(Util.stripHtml(vo.getMsgHtmlTxt()));
					//CP-10410 don't put the margin on if there was no HTML displayed above it.
					boolean hasHTML = !Util.isBlankOrNull(suggestedBecause.toString());
					if(hasHTML)
					{
						suggestedBecause.append("<div style=\"margin-top: 8px;\">");
					}
					
					//CP-10882
					if (Boolean.TRUE.equals(vo.getShowRatioInd()))
					{
						suggestedBecause.append(getRatioMessage(vo.getRelationshipLvlCd(), appSession, vo));
					}
					
					if(hasHTML)
					{
						suggestedBecause.append("</div>");
					}
					//CP-9168 End
					thumb.setSuggestedBecauseCol(suggestedBecause.toString());
					
					//RAR - Set the Catalog Line Number and Vendor Item Number of the Suggested Item.
					thumb.setVendorItemNumber(vo.getWcsItemNum());
					
					//CP-8483 RAR - Retrieve and set the Item UOM Options.
					//CP-8942	RAR - Pass the deliveryOption so we can determine if the UOM Options need to be built.
					//CP-9486 RAR - Pass the mergeCode to handle Mail Merge Items.
					//CP-8970 removed token parameter
					//CP-13132 added isExternalESP parameter
					thumb.setUomOptions(ItemHelper.getItemUOMOptions(vo.getWcsItemNum(), vo.getItemNum(), vo.getClassification(), oeSession.getOESessionBean().getUserSettings(), volatileSession.getVolatileSessionBean(), appSession.getAppSessionBean(), vo.getDeliveryOption(), vo.getMergeOptionCode(), vo.isViewOnlyFlag(), isExternalESP));
					
					//RAR - Set other needed information about the item.
					thumb.setAllowFavorites(oeSession.getOESessionBean().getUserSettings().isAllowUserFavoritesInd());
					thumb.setFavorite(ItemHelper.isItemFavorite(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getProfileNumber(), vo.getWcsItemNum(), vo.getItemNum(), appSession.getAppSessionBean().getCustomToken()));
					thumb.setCheckInventoryEnabled(ItemHelper.isCanCheckInventory(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getGroupName(), appSession.getAppSessionBean().getCustomToken()));
					thumb.setManageItemsEnabled(ItemHelper.isItemManager(appSession.getAppSessionBean().getSiteID(), appSession.getAppSessionBean().getBuID(), appSession.getAppSessionBean().getGroupName(), appSession.getAppSessionBean().getCustomToken()));
					thumb.setEdocEnabled(vo.isEDocFlag());
					//CP-9607 RAR - Fixed issue on the eDoc URL.
					thumb.setEdocUrl(appSession.getAppSessionBean().getQualifiedImageSrc(vo.getEdocUrl(), AtWinXSConstant.ITEM_EDOC));
					thumb.setFileDeliveryOptionCd(vo.getDeliveryOption());
					thumb.setSpecialItemTypeCd(String.valueOf(vo.getSpecialItemType()));
					thumb.setItemAlwaysRoute(vo.alwaysRouteFlagLineReview());
					thumb.setItemRouteQuantity(vo.getApprovalLimtQtyLineReview());
					//CP-9225 use admin default quantity
					thumb.setItemQuantity(Integer.toString(appSession.getAppSessionBean().getSiteDefaultQty()));
					//CP-8994
					thumb.setShowVendorItemNum(appSession.getAppSessionBean().isShowWCSSItemNumber());
					thumb.setItemClassification(vo.getClassification());
					//CAP-2476 check if they have featured items set up
					if(hasFeaturedItems)
					{
						thumb.setFeatureMap(ItemHelper.buildFeatureMap(vo, appSession.getAppSessionBean()));
					}
					//CP-13132 pass order lines instead of volatile session bean
					thumb.setItemInCart(isItemInCart(appSession, orderLines, vo));// CAP-50834
					thumb.setItemOrderable(vo.isItemOrderable());
					thumb.setViewOnlyFlag(vo.isViewOnlyFlag());
					thumb.setInvalidItemFlag(vo.isInvalidItemFlag());
					thumb.setAlwaysRoute((vo.isAlwaysRouteFlag() && (oeSession.getOESessionBean().getUserSettings().getAssignedApprovalQueue() > 0)));
					thumb.setRouteQuantity((int) vo.getApprovalLimitQty());
					thumb.setAllowEFD(oeSession.getOESessionBean().getUserSettings().isAllowEFD());
					thumb.setAllowPrintOverride(oeSession.getOESessionBean().getUserSettings().isAllowPrintOverride());
					thumb.setShowRAInfo(((oeSession.getOESessionBean().getUserSettings().isShowRoutingInfo() && oeSession.getOESessionBean().getUserSettings().isRoutingAvailable() && oeSession.getOESessionBean().getUserSettings().isSubjToRnA()) && !volatileSession.getVolatileSessionBean().isKitTemplateMode()));
					thumb.setShowAvailability(oeSession.getOESessionBean().getUserSettings().isShowOrderLineAvailability());
                    thumb.setShowPrice(oeSession.getOESessionBean().getUserSettings().isShowOrderLinePrice());
        			//CP-12787 - Set value of DisplayQuantityAsText only if Xert item.
                    //CP-12605 - Reuse the new isExternalESP method in CustomDocumentItem class.
                    //CP-13132 use local variable instead of making repetitive DB calls
        			if (isExternalESP)
        			{
        				thumb.setDisplayQuantityAsText(isExternalESP);
        			}
					
                    //CP-9359 RAR - Set the CatalogLineNumber so we can have unique ID for each suggested items.
                    thumb.setCatalogLineNumber(--index);
                    
                    //CAP-315 RAR - isIgnoreSessionSave is true if we are in Line Level Suggested popup, so set the render location
                    //to show the error message in the popup.
                    if(isIgnoreSessionSave)
                    {
                        thumb.setRenderLocation("linesgstditms_results");
                    }
                    else
                    {
                        // CP-10066
                        thumb.setRenderLocation("sgstditms_results");
                    }
                    
                    //CAP-15083 SRN If icon plus view enabled then set the info needed
                    if(appSession.getAppSessionBean().isIconPlusViewEnabled())
                    {
                    	ItemSearchServiceImplHelper.setIconPlusFldAndVal(thumb, oeSession.getOESessionBean().getUserSettings());
                    }
                    
                    //CAP-15939 unit price hard stop logic
                    ItemSearchServiceImplHelper.processItemPriceLimit(thumb, userSettings.getItemPrcLimitPerOrder(), vo.getUnitPriceAmt());
                    COSuggestedItemSearchResult c1UXThump=new COSuggestedItemSearchResult();
                    BeanUtils.copyProperties(c1UXThump, thumb);
                    c1UXThump.setFeatureFavoriteItemData(item.getFeatureFavoriteItemData());//CAP-50540
					// CAP-50540
					CatalogItem cat = objectMapFactoryService.getEntityObjectMap().getEntity(CatalogItem.class,
							appSession.getAppSessionBean().getCustomToken());
					cat.populate(appSession.getAppSessionBean(), oeSession.getOESessionBean(), thumb.getItemNumber(),
							thumb.getVendorItemNumber(), thumb.getCatalogLineNumber(),
							!userSettings.isShowPricingGrid());
					if (cat.isShowRAInfo() && cat.isItemRouting()) {
						lineLevelRouteMessage = ItemUtility.getLineLevelRoutingInformation(cat.isAlwaysRouteInd(),
								cat.isRouteIndicatorLineReview(), cat.getApprovalQuantity(),
								cat.getApprovalQuantityLineReview(), appSession.getAppSessionBean(),
								oeSession.getOESessionBean());
						orderLevelRouteMessage = ItemUtility.getOrderLevelRoutingInformation(
								userSettings.isAlwaysRouteOrders(), userSettings.getRouteDollarAmount(),
								userSettings.isRouteOnShipMethodChange(), appSession.getAppSessionBean(),
								oeSession.getOESessionBean());
						c1UXThump.setRoutingBadge(ItemUtility.getRoutingInformation(lineLevelRouteMessage,
								orderLevelRouteMessage, appSession.getAppSessionBean()));
					}
					// CAP-50540
                    suggestedItemResults.add(c1UXThump);			
				}	
			}
		} catch (AtWinXSException e1)
		{
			// CAP-16460 call to logger.
			logger.error(this.getClass().getName() + " - " + e1.getMessage(),e1);
		}
		
		return suggestedItemResults;
	}

	// CAP-50834
	protected boolean isItemInCart(ApplicationSession appSession, Collection<OrderLineVO> orderLines,
			OECompanionItemResultsExtendedVO vo) throws AtWinXSException {
		return !isCustomizableItem(vo.getClassification(), vo.getWcsItemNum(),
				appSession.getAppSessionBean(), appSession.getPunchoutSessionBean())
				&& ItemHelper.isItemInCart(appSession.getAppSessionBean(), vo.getItemNum(),
						vo.getWcsItemNum(), orderLines);
	}
	
	public String getRatioMessage(
			String relationshipLevelCd, 
			ApplicationSession appSession, 
			OECompanionItemResultsExtendedVO line) throws AtWinXSException
	{
		String ratioOrigMsg = "";
		String ratioMsg = "";
		String mainItemUnit = "";
		String compItemUnit = "";
		String consUnits="units";
		Map<String, Object> replaceMap = new HashMap<>();

 		if (relationshipLevelCd.equals(OrderEntryConstants.COMPANION_ITEMS_ITEM_LVL_IND))
		{
 			ICompanionItem companionComponent = cmCompanionItemComponentLocatorService.locate(appSession.getAppSessionBean().getCustomToken());
 			CompanionItemRelationshipVO mainItem = companionComponent.retrieveCompanionItemRelationship(appSession.getAppSessionBean().getSiteID(), line.getRelationshipID());
 			String mainCustItemNum = mainItem.getItemCItemNumber();
 			
 			if(Util.isBlankOrNull(mainCustItemNum))
 			{
 				mainCustItemNum = mainItem.getItemVItemNumber();
 			}
			replaceMap.put(PageTextConstants.REP_TAG_ITEM_NUM, mainCustItemNum);
 			 			
			replaceMap.put(PageTextConstants.REP_TAG_NUM_UNITS_CUST, String.valueOf(line.getCompanionRatio()));
			replaceMap.put(PageTextConstants.REP_TAG_NUM_UNITS, String.valueOf(line.getItemRatio()));
			
			mainItemUnit = (line.getItemRatio() > 1) ? consUnits : "unit";
			compItemUnit = (line.getCompanionRatio() > 1) ? consUnits : "unit";			
			replaceMap.put(PageTextConstants.REP_TAG_UNITS_ITEM, mainItemUnit);
			replaceMap.put(PageTextConstants.REP_TAG_UNITS_COMP_ITEM, compItemUnit);
			
			//CP-8970 changed token from String to an Object
			ratioMsg = translationService.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(), PageTextConstants.ELEM_NM_ITEM_LVL_RATIO_INSTR, replaceMap);
		}
		else if (relationshipLevelCd.equals(OrderEntryConstants.COMPANION_ITEMS_ORD_LVL_IND))
		{
			replaceMap.put(PageTextConstants.REP_TAG_NO_OF_ITEMS, String.valueOf(line.getCompanionRatio()));
			
			compItemUnit = (line.getCompanionRatio() > 1) ? consUnits : "unit";
			replaceMap.put(PageTextConstants.REP_TAG_UNITS_COMP_ITEM, compItemUnit);
			
			//CP-8970 changed token from String to an Object
			ratioMsg = translationService.processMessage(appSession.getAppSessionBean().getDefaultLocale(), appSession.getAppSessionBean().getCustomToken(), PageTextConstants.ELEM_NM_ORD_LVL_RATIO_INSTR, replaceMap);
		}
		else
		{
			ratioMsg = ratioOrigMsg;			
		}
		return ratioMsg;
	}
	
	public String getContextPath() {
		return Util.getContextPath(ContextPath.Classic);
	}

	
	public Collection<OECompanionItemResultsExtendedVO> getOrderLevelSuggestedItem(OEOrderSessionBean oeSessionBean ,SessionContainer sc,OEShoppingCartAssembler sca, 
								IOECompanionItemComponent companionComp, OrderEntrySession oeSession) {
		
		Collection<OECompanionItemResultsExtendedVO> suggestedVOs=null;
		try
		{		
			boolean hasSuggestedItems = false;
			
			hasSuggestedItems = sca.loadCompanionItems(sc.getApplicationVolatileSession().getVolatileSessionBean(),sc.getApplicationSession().getAppSessionBean(), oeSessionBean.getUserSettings(),
					oeSessionBean.getUsrSrchOptions(), false);
			
			if(hasSuggestedItems)
			{
				// CAP-603: Assigned -1 order ID if not yet in an order
				OECompanionItemSearchCriteriaVO searchCriteriaVO = this.createSearchCriteriaVO(
						sc.getApplicationSession().getAppSessionBean(), 
						oeSession.getOESessionBean(), 
						sc.getApplicationVolatileSession().getVolatileSessionBean().getOrderId() == null ? -1 : sc.getApplicationVolatileSession().getVolatileSessionBean().getOrderId());
				
				suggestedVOs = companionComp.retrieveCompanionItemsFromCache(searchCriteriaVO);
				
				//CAP-35 RAR - If the Site is configured to show the Item Level Suggested Items,
				//then suppress those items from showing in the Suggested Items widget.
				if(oeSession.getOESessionBean().getUserSettings().getSugItemsDfltDsply().equals("L")) //CAP-411
				{
					suggestedVOs = filterItemLevelSuggestedItems(suggestedVOs);
				}
			}
		} catch (AtWinXSException e)
		{
			// CAP-16460 call to logger.
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
		return  suggestedVOs;
	}
	
	
	public Collection<OECompanionItemResultsExtendedVO> getItemLevelSuggestedItem(SessionContainer sc,
			 OrderEntrySession oeSession,boolean isIgnoreSessionSave,String cItemNum, String vItemNum, String lineNum) {
		
		Collection<OECompanionItemResultsExtendedVO> suggestedVOs=null;
		
		Collection<OECompanionItemResultsVO> resultsVOs = (Collection<OECompanionItemResultsVO>)oeSession.getParameter("suggestedItems");
		
		//CAP-35 RAR - If we are in the Shopping Cart, we'll need to do the search each time an item is selected.
		if(resultsVOs != null && !isIgnoreSessionSave)
		{
			try
			{
				suggestedVOs = getExtendedVOs(resultsVOs, sc.getApplicationSession().getAppSessionBean().getSiteID(), sc.getApplicationSession().getAppSessionBean().getCustomToken());
			}
			catch(AtWinXSException ae)
			{
				// CAP-16460 call to logger.
				logger.error(this.getClass().getName() + " - " + ae.getMessage(),ae);
			}
		}
		else
		{
			//CAP-35 - If isIgnoreSessionSave is true, we do not want the one saved in the sesssion, do the search.
			if(isIgnoreSessionSave)
			{
				try
				{
					//CAP-5538 8.1.20 [SRN] - Added lineNum
					resultsVOs = searchItemLevelSuggestedItems(cItemNum, vItemNum, lineNum, sc.getApplicationVolatileSession().getVolatileSessionBean(), sc.getApplicationSession().getAppSessionBean(), oeSession.getOESessionBean());
					suggestedVOs = getExtendedVOs(resultsVOs, sc.getApplicationSession().getAppSessionBean().getSiteID(), sc.getApplicationSession().getAppSessionBean().getCustomToken());
				} 
				catch (AtWinXSException e)
				{
					// CAP-16460 call to logger.
					logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
				} 
			}
			else
			{
				suggestedVOs = new ArrayList<>();
			}
		}
		
		return suggestedVOs;
		
	}

}
