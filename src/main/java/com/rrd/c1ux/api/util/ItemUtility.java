/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#				Description
 * 	--------	-----------				-----------		    --------------------------------
 *	09/19/22    Sakthi M        		CAP-35464           Modify API loading of product details to make catalog line 
 *                                                  		number optional for cart call and return results fields															
 *	09/19/22	A Boomker				CAP-35958			Efficiency improvements
 *  
 *  10/13/22	Krishna Natarajan		CAP-36604			contains is replaced with equals
 *  
 *  03/23/23 	Sakthi M				CAP-38561 			Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
 *  
 *	09/12/23	Krishna Natarajan		CAP-43384			Added methods to get the routing information line and order level
 *
 *  09/18/23	Krishna Natarajan	    CAP-43967 			Added logic in buildFeatureMap() to get FeatureFavoriteItemData
 *  10/11/23	T Harmon				CAP-44548			Added method to get edoc url (encrypted)
 *  10/13/23	T Harmon				CAP-44548			Fixed URL for items without edoc
 *  11/10/23	Krishna Natarajan		CAP-44548 			Additional changes made to indicate EDOC internal or external
 */
package com.rrd.c1ux.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.models.items.UOMItems;
import com.rrd.c1ux.api.services.translation.C1UXTranslationService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.catalogs.locator.CMFeaturedItemsComponentLocator;
import com.wallace.atwinxs.catalogs.util.CatalogConstant;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.FeatureFavoriteItemData;
import com.wallace.atwinxs.catalogs.vo.FeaturedItemsCompositeVO;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CPUrlBuilder;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IFeaturedItems;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKUOMInfo;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OEUomSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

public class ItemUtility {
	
	public static final String EDOC_API_URL 				= "/api/edoc/retrieve";
	public static final String EDOC_PARM_URL 				= "URL";
	public static final String EDOC_PARM_ITEM_NUM 			= "itemNumber";
	public static final String EDOC_PARM_VENDOR_ITEM_NUM	= "vendorItemNumber";
	public static final String EDOC_PARM_LOG_FLAG			= "logFlag";
	public static final String EDOC_PARM_LOCAL_FILE			= "localFile";
	public static final String EDOC_API_EXTERNAL_URL 		= "/api/edoc/retrieveExternal";
	
	
	
	
	public static TranslationService getTranslationService() {
		return new C1UXTranslationService();
	}

	
	/* Assign the RouteConstant value into the Map */
	public static final String[][] FULL_TEXT_UOM_MAP =  {
	 	{RouteConstants.C1UX_UOM_EA,RouteConstants.C1UX_UOM_EA_VALUE},
		{RouteConstants.C1UX_UOM_CS,RouteConstants.C1UX_UOM_CS_VALUE},
		{RouteConstants.C1UX_UOM_RL,RouteConstants.C1UX_UOM_RL_VALUE},
		{RouteConstants.C1UX_UOM_M,RouteConstants.C1UX_UOM_M_VALUE},
		{RouteConstants.C1UX_UOM_BX,RouteConstants.C1UX_UOM_BX_VALUE},
		{RouteConstants.C1UX_UOM_CT,RouteConstants.C1UX_UOM_CT_VALUE},
		{RouteConstants.C1UX_UOM_BG,RouteConstants.C1UX_UOM_BG_VALUE}, 
		{RouteConstants.C1UX_UOM_BK,RouteConstants.C1UX_UOM_BK_VALUE},
		{RouteConstants.C1UX_UOM_BL,RouteConstants.C1UX_UOM_BL_VALUE},
		{RouteConstants.C1UX_UOM_BR,RouteConstants.C1UX_UOM_BR_VALUE},
		{RouteConstants.C1UX_UOM_C,RouteConstants.C1UX_UOM_C_VALUE},
		{RouteConstants.C1UX_UOM_DZ,RouteConstants.C1UX_UOM_DZ_VALUE},
		{RouteConstants.C1UX_UOM_FM,RouteConstants.C1UX_UOM_FM_VALUE},
		{RouteConstants.C1UX_UOM_GR,RouteConstants.C1UX_UOM_GR_VALUE},
		{RouteConstants.C1UX_UOM_JK,RouteConstants.C1UX_UOM_JK_VALUE},
		{RouteConstants.C1UX_UOM_KT,RouteConstants.C1UX_UOM_KT_VALUE},
		{RouteConstants.C1UX_UOM_LR,RouteConstants.C1UX_UOM_LR_VALUE},
		{RouteConstants.C1UX_UOM_LT,RouteConstants.C1UX_UOM_LT_VALUE},
		{RouteConstants.C1UX_UOM_PD,RouteConstants.C1UX_UOM_PD_VALUE},
		{RouteConstants.C1UX_UOM_PK,RouteConstants.C1UX_UOM_PK_VALUE},
		{RouteConstants.C1UX_UOM_PL,RouteConstants.C1UX_UOM_PL_VALUE},
		{RouteConstants.C1UX_UOM_PR,RouteConstants.C1UX_UOM_PR_VALUE},
		{RouteConstants.C1UX_UOM_RM,RouteConstants.C1UX_UOM_RM_VALUE},
		{RouteConstants.C1UX_UOM_SH,RouteConstants.C1UX_UOM_SH_VALUE},
		{RouteConstants.C1UX_UOM_ST,RouteConstants.C1UX_UOM_ST_VALUE},
		{RouteConstants.C1UX_UOM_TB,RouteConstants.C1UX_UOM_TB_VALUE},
		{RouteConstants.C1UX_UOM_TT,RouteConstants.C1UX_UOM_TT_VALUE},
		{RouteConstants.C1UX_UOM_US,RouteConstants.C1UX_UOM_US_VALUE},
		{RouteConstants.C1UX_UOM_UT,RouteConstants.C1UX_UOM_UT_VALUE}};

	/* CAP-34647 This method used to replace existing UOM Acronyms to full Acronyms description */
	public static String getUOMAcronyms(String strDesc,boolean fullDesc,AppSessionBean appSessionBean) throws AtWinXSException {
		String acronymDesc="";
		String[] arrOfAcronym = strDesc.split(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"ofLbl"));//CAP-36604
		for (int i = 0; i < FULL_TEXT_UOM_MAP.length; i++) 
		{
			//CAP-36604 removed contains() and checking acronym with equals()
			//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
			if (arrOfAcronym[0].trim().equals(FULL_TEXT_UOM_MAP[i][0])) {
				  acronymDesc= (fullDesc?strDesc.replaceFirst(FULL_TEXT_UOM_MAP[i][0], 
						  TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),(RouteConstants.SF_PREFIX+FULL_TEXT_UOM_MAP[i][1]))):strDesc.replace(strDesc,TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),(RouteConstants.SF_PREFIX+FULL_TEXT_UOM_MAP[i][1]))));
			}
		}	  
		return acronymDesc;
	}
	

	public static final String DIV_TAGS="<div>&nbsp;</div>";
	
	public String doAlternateDescriptionProcessing(CatalogItem item) throws AtWinXSException
	{
		//CP-10326 removed HTML encoding of quotes.
		String alternateDesc = Util.nullToEmpty(item.getAltCatData().getAlternateCatalogDesc());
		String altDescDispType = item.getAltCatData().getAlternateCatalogDescDisplayType();
		
		String descriptionWithAltDesc = "";
		String altDescDisp = "";
		
		if (alternateDesc.length() > 0
				&& (altDescDispType
						.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_APPEND) || altDescDispType
						.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_REPLACE)))
		{
			// CAP-23824 - replace paragraph tags
			altDescDisp = Util.replace(Util.replace(alternateDesc, "<p>", "<br>"), "<P>", "<br>");
						
			//Append to or replace the description
			if(altDescDispType.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_APPEND))
			{
				//CP-11173 escape the base item description so HTML does not render.	
				descriptionWithAltDesc = HtmlUtils.htmlEscape(item.getDescription()) + " - " + removeLineBreaks(altDescDisp); // CAP-2237
			}
			else if(altDescDispType.equalsIgnoreCase(CatalogConstant.ALT_CATLG_DESC_DISPLAY_TYPE_REPLACE))
			{
					descriptionWithAltDesc = removeLineBreaks(altDescDisp); // CAP-2237
			}
		}
		else
		{
			//CP-11173 escape the base item description so HTML does not render.
			descriptionWithAltDesc = HtmlUtils.htmlEscape(item.getDescription());
		}
		
		return descriptionWithAltDesc;
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
     
		public static void setItemUOMOptions(SearchResult thumb, AppSessionBean appSession, String itemClassification, boolean isExternalESP, 
				VolatileSessionBean volatileSession, String vendorItemNumber, KitSession kitSession, IManageItemsInterface itemManager,
				OEResolvedUserSettingsSessionBean userSettings, String mergeCd) throws AtWinXSException
		{
			Map<String, String> uomOptions = new LinkedHashMap<String, String>();
			
			//CP-11880 PTB translation for of label
			String ofLbl = TranslationTextTag.processMessage(appSession.getDefaultLocale(), appSession.getCustomToken(), "ofLbl");
			
			//CP-12787 - Updated condition to lock down UOM to EA of 1 for Xert items.
				//CP-13104 - Lock down UOM to EA of 1 for Bundle Item
			if(ItemConstants.ITEM_CLASS_KIT_TEMPLATE.equals(itemClassification) || 
						ItemConstants.ITEM_CLASS_BUNDLE.equals(itemClassification) ||
						ItemConstants.ITEM_CLASS_CAMPAIGN.equals(itemClassification) ||
						(Util.isBlankOrNull(vendorItemNumber)) || // CP-11928 - old special items and non-VM kit templates
						(ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(itemClassification) && ICustomDocsAdminConstants.TMPLT_UI_MERGE_OPT_CD_M.equalsIgnoreCase(mergeCd)) ||
						//CP-13132 use passed in variable instead of making repetitive DB calls
						isExternalESP)
				{
					String key = "EA" + "_" + 1;
					//CP-11880 PTB used translated of label
					String value = "EA " + ofLbl +" 1";
					uomOptions.put(key, value);
				}
				else if(volatileSession.isKitTemplateMode())
				{
						//CP-8970 changed token from String to an Object
						MKUOMInfo[] wildCardUoms = ItemHelper.getWildCardUOMs(vendorItemNumber, appSession, volatileSession, appSession.getCustomToken(), kitSession); // CAP-2589
						
						if(null != wildCardUoms && wildCardUoms.length > 0)
						{
							for(MKUOMInfo bean : wildCardUoms)
							{
								String key = bean.getUOMCd() + "_" + bean.getUOMFactor();
								//CP-11880 PTB used translated of label
								String value = bean.getUOMCd() + " " + ofLbl + " " + bean.getUOMFactor();
								uomOptions.put(key, value);
							}
						}
				}
				else
				{
					ItemRptVO itemRptVO = itemManager.getWCSSItem(vendorItemNumber);
					List<OEUomSessionBean> uomsList = new ArrayList<OEUomSessionBean>(); 
					OEUomSessionBean[] uomsArray = null;
					
					if(itemRptVO != null)
					{
						uomsArray = OrderEntryUtil.getUOMCodes(userSettings, itemRptVO.getStockingUOM(), String.valueOf(itemRptVO.getStockUOMConversionFactor()), 
								itemRptVO.getPriceUOMCode(), String.valueOf(itemRptVO.getPricingConversionFactor()), itemRptVO.getMasterPackUOM(), 
							String.valueOf(itemRptVO.getMasterPackUOMConversionFactor()), itemRptVO.getInnerPackUOM(), String.valueOf(itemRptVO.getInnerPackUOMConversionFactor()), 
							String.valueOf(itemRptVO.getMaxinumOrderQty()));
					}
					
					uomsList = uomsArray != null ? Arrays.asList(uomsArray) : new ArrayList<OEUomSessionBean>();
					
					if(uomsList.size() > 0)
					{
						for(OEUomSessionBean bean : uomsList)
						{
							String key = bean.getUomCode() + "_" + bean.getUomFactor();
							//CP-11880 PTB used translated of label
							String value = bean.getUomCode() + " " + ofLbl + " " + bean.getUomFactor();
							uomOptions.put(key, value);
						}
					}
				}
		
			List<UOMItems> uomArrList=new ArrayList<UOMItems>();
			for (Map.Entry<String,String> entry : uomOptions.entrySet()) 
			{
				UOMItems uomitm=new UOMItems();
				uomitm.setUnitName(entry.getKey());
				uomitm.setUnitValue(getUOMAcronyms(entry.getValue(),true,appSession));
				uomitm.setSoldAs(getUOMAcronyms(entry.getValue(),false,appSession));
				uomArrList.add(uomitm);
			} 
			thumb.setUomArrLst(uomArrList);
			thumb.setUomOptions(uomOptions);		
		}
		
		
		
		/**
		 * Method buildFeatureMap()
		 * 
		 * This method will build a {@link Map} to store the Feature details of the Item.
		 * 			key - Feature Item Name (eg. Feature, Hot, New)
		 * 			value - Icon path
		 * 
		 * @param featureFavoriteItemData
		 * @param appSessionBean
		 * @return Map<String, String>
		 * @throws AtWinXSException 
		 */
		public static FeatureFavoriteItemData buildFeatureMap(CatalogLineVO line, AppSessionBean appSessionBean) throws AtWinXSException
		{
			FeatureFavoriteItemData featureFavoriteItemData = line.getFeatureFavoriteItemData();
			
			IFeaturedItems iFeaturedItems = CMFeaturedItemsComponentLocator.locate(appSessionBean.getCustomToken());
			int relID = iFeaturedItems.getRelationShipIDOfItem(line.getWcsItemNum(), line.getItemNum(), appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getGroupName());
			featureFavoriteItemData.setRelationshipID(relID);
			
			// CAP-33833 Removed reference to OECatalogAssembler
			
			Collection<FeaturedItemsCompositeVO> featureItemData = new ArrayList<>();
			
			//Ideal.. if we get the Feature Item Details from Catalog use it.
			if(null != featureFavoriteItemData.getFeaturedItems() && !featureFavoriteItemData.getFeaturedItems().isEmpty())
			{
				featureItemData = featureFavoriteItemData.getFeaturedItems();
			}
			//If we only get the relationshipID from the call to Catalog, then retrieve the Feature Item Details using that id.
			else if(null == featureFavoriteItemData.getFeaturedItems() && featureFavoriteItemData.getRelationshipID() > 0)
			{
				// CAP-33833 TH - Called method directly from iFeaturedItems instead of assembler
				//CAP-42171 SRN Added locale parameter
				featureItemData = iFeaturedItems.getFeaturedItemsCompById(featureFavoriteItemData.getRelationshipID(), appSessionBean.getSiteID(), appSessionBean.getDefaultLocale());			
			}
			
			//CAP-43967
			featureFavoriteItemData.setFeaturedItems(featureItemData);
			return featureFavoriteItemData;
		}
		
		// CAP-43384
		public static String getLineLevelRoutingInformation(boolean isAlwaysRouteInd,
				boolean isRouteIndicatorLineReview, int getApprovalQuantity, int getApprovalQuantityLineReview,
				AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSessionBean) throws AtWinXSException {
			String lineLevelRoutingMessage = "";
			TranslationService service = getTranslationService();
			if ((isAlwaysRouteInd && oeOrderSessionBean.getUserSettings().getAssignedApprovalQueue() > 0)
					|| isRouteIndicatorLineReview) {
				lineLevelRoutingMessage += SFTranslationTextConstants.DASH_NO_SPACE
						+ service.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								SFTranslationTextConstants.ITEM_REQUIRE_APPROVAL_LABEL)
						+ SFTranslationTextConstants.BREAK_LINE + AtWinXSConstant.BLANK_SPACE;
			} else if (getApprovalQuantity > 0 || getApprovalQuantityLineReview > 0) {

				Map<String, Object> replaceMap = new HashMap<>();

				if (getApprovalQuantityLineReview > 0
						&& (getApprovalQuantity < 1 || getApprovalQuantityLineReview <= getApprovalQuantity)) {
					replaceMap.put(SFTranslationTextConstants.ORDER_QTY_ROUTING_MAP_VALUE,
							getApprovalQuantityLineReview);
				} else if (getApprovalQuantity > 0
						&& (getApprovalQuantityLineReview < 1 || getApprovalQuantity < getApprovalQuantityLineReview)) {
					replaceMap.put(SFTranslationTextConstants.ORDER_QTY_ROUTING_MAP_VALUE, getApprovalQuantity);
				}

				lineLevelRoutingMessage += SFTranslationTextConstants.DASH_NO_SPACE
						+ service.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								SFTranslationTextConstants.ROUTE_QUANTITY_WARN_LABEL, replaceMap)
						+ SFTranslationTextConstants.BREAK_LINE + AtWinXSConstant.BLANK_SPACE;
			}
			return lineLevelRoutingMessage;
		}

		// CAP-43384
		public static String getOrderLevelRoutingInformation(boolean isAlwaysRouteOrders, double routeDollarAmount,
				boolean isRouteOnShipMethodChange, AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSessionBean)
				throws AtWinXSException {
			String orderLevelRoutingMessage = "";
			TranslationService service = getTranslationService();
			if (isAlwaysRouteOrders) {
				orderLevelRoutingMessage += SFTranslationTextConstants.DASH_NO_SPACE
						+ service.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								TranslationTextConstants.ROUTING_ORDERS_ALWAYS_MSG)
						+ SFTranslationTextConstants.BREAK_LINE + AtWinXSConstant.BLANK_SPACE;
			}
			if (routeDollarAmount > 0) {
				String routingAmtLevel = Util
						.getStringFromCurrency(oeOrderSessionBean.getUserSettings().getRouteDollarAmount(),
								appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate())
						.getAmountText();
				orderLevelRoutingMessage += SFTranslationTextConstants.DASH_NO_SPACE
						+ service.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								TranslationTextConstants.ROUTING_EXCEED_AMOUNT_MSG)
						+ AtWinXSConstant.BLANK_SPACE + routingAmtLevel + "." + SFTranslationTextConstants.BREAK_LINE
						+ AtWinXSConstant.BLANK_SPACE;
			}
			if (isRouteOnShipMethodChange) {
				orderLevelRoutingMessage += SFTranslationTextConstants.DASH_NO_SPACE
						+ service.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								TranslationTextConstants.ROUTING_SHIPPING_METHOD_MSG)
						+ SFTranslationTextConstants.BREAK_LINE + AtWinXSConstant.BLANK_SPACE;
			}
			return orderLevelRoutingMessage;
		}

		// CAP-43384
		public static Map<String, String> getRoutingInformation(String lineLevelRoutingMessage,
				String orderLevelRoutingMessage, AppSessionBean appSessionBean) throws AtWinXSException {
			String routeMessage = "";
			Map<String, String> routeMap = new HashMap<>();
			TranslationService service = getTranslationService();
			if (lineLevelRoutingMessage.length() > 0 || orderLevelRoutingMessage.length() > 0) {
				routeMessage += service.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.ROUTING_REASONS_MSG)
						+ SFTranslationTextConstants.BREAK_LINE + AtWinXSConstant.BLANK_SPACE + lineLevelRoutingMessage
						+ orderLevelRoutingMessage;
			}
			routeMap.put("routing", routeMessage);// CAP-43384 the key and value replaced
			return routeMap;
		}
		
		// CAP-44548	
		public static String getEdocUrl(String itemNumber, String vendorItemNumber, boolean logFlag, String originalUrl, String fileName, String siteLoginID)
		{
			String url=EDOC_API_EXTERNAL_URL;// CAP-44548 additional changes
			// If the original URL is empty, we don't have an edoc, so return empty string
			if (Util.isBlankOrNull(originalUrl)) {
				return AtWinXSConstant.EMPTY_STRING;
			}
			
			// Build edoc local file location
			String localFileName = AtWinXSConstant.EMPTY_STRING;
			if (!originalUrl.toUpperCase().startsWith("HTTP"))
			{
				StringBuilder sBuilder = new StringBuilder();		
				sBuilder.append(AppProperties.getBaseLocal()).append(AppProperties.getPathSeparator()).append(AppProperties.getDirBaseFiles())
					.append(AppProperties.getPathSeparator()).append(AppProperties.getDirCustomerFiles()).append(AppProperties.getPathSeparator())
					.append(siteLoginID).append(AppProperties.getPathSeparator()).append(AppProperties.getDirEDocs()).append(AppProperties.getPathSeparator())
					.append(fileName);
				localFileName = sBuilder.toString();
				url=EDOC_API_URL;// CAP-44548 additional changes
			}
			
			CPUrlBuilder builder = new CPUrlBuilder();
			builder.setBaseUrl(url);// CAP-44548 additional changes
			builder.addParameter(EDOC_PARM_URL, Util.decodeURL(originalUrl));
			builder.addParameter(EDOC_PARM_ITEM_NUM, itemNumber);
			builder.addParameter(EDOC_PARM_VENDOR_ITEM_NUM, vendorItemNumber);
			builder.addParameter(EDOC_PARM_LOG_FLAG, Boolean.toString(logFlag));
			builder.addParameter(EDOC_PARM_LOCAL_FILE, localFileName);	
					    			
			return builder.getUrl(true);					
		}
}