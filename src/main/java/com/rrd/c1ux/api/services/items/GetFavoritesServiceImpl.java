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
 * 	04/08/22	Krishna Natarajan	CAP-33867	    Created as per the requiremet to fetch the favorite items
 *	08/30/23	A Boomker			CAP-43405		Fixing item in cart flags for customizable items
 *	09/25/23	M Sakthi			CAP-38861		C1UX BE - API Standardization - Get favorite items for C1UX conversion to standard catalog item response
 *	10/17/23	Krishna Natarajan	CAP-44716		Updated code to set the flags for show price & availability in favorite widget
 *	10/18/23	Krishna Natarajan	CAP-44716		Updated code to set the Price Line Availability Label
 *	05/13/24	L De Leon			CAP-48938		Modified getFavoriteItemsProcessed() method to populate delivery options list
 */

package com.rrd.c1ux.api.services.items;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;
import com.rrd.c1ux.api.models.items.FavouriteItems;
import com.rrd.c1ux.api.models.items.GetOEItemSearchCriteriaSessionBean;
import com.rrd.c1ux.api.models.items.ItemThumbnailCellData;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

/**
 *  @author Krishna Natarajan
 *
 *	This class should contain all methods that can be reused for getting favorite items.
 *
 * */
@Service
public class GetFavoritesServiceImpl extends BaseOEService implements GetFavoritesService {

	private static final Logger logger = LoggerFactory.getLogger(GetFavoritesServiceImpl.class);
	
	private final CatalogItemRetriveServices catalogItemRetriveServices;

	public static final int AUTOCOMPLETE_ITEM_LIMIT = 10;
	public static final String STR_BUNDLE_DEFAULT_QTY = "1";
	public static final String NOT_AVAILABLE = "N/A";

	boolean isCustomizableItem=false;

	public GetFavoritesServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,CatalogItemRetriveServices mCatalogItemRetriveServices) {
	      super(translationService, objectMapFactoryService);
	      this.catalogItemRetriveServices=mCatalogItemRetriveServices;
	    }


	/** Method getFavoriteItemsProcessed(), to process and get the favorite items
	 * @param searchCriteriaBean {@link OEItemSearchCriteriaSessionBean}
	 * @param mfavoriteItems {@link FavoriteItemsMapper}
	 * @param appSessBean {@link AppSessionBean}
	 * @param volatilesSssnBean {@link VolatileSessionBean}
	 * @param userSettings {@link OEResolvedUserSettingsSessionBean}
	 * @param appSession {@link ApplicationSession}
	 * @param volatileSession {@link ApplicationVolatileSession}
	 * @param punchoutSessionBean {@link PunchoutSessionBean}
	 * @return List of Favorite items {@link FavouriteItems}
	 * @throws AtWinXSException
	 */
	public CatalogItemsResponse getFavoriteItemsProcessed(SessionContainer sc,  FavoriteItemsMapper mfavoriteItems, ServletContext servletContext) throws AtWinXSException{
	
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessBean = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatilesSssnBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		CatalogItemsResponse catalogItemsResp=new CatalogItemsResponse();
		boolean hasOrdersService = appSessBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID); // PMO service	
		if(!hasOrdersService) {
			throw new AccessForbiddenException(this.getClass().getName());
		}		
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
			OEItemSearchCriteriaSessionBean searchCriteriaBean = oeSessionBean.getSearchCriteriaBean();
			
			int primaryAttribute = userSettings.getPrimaryAttribute();
			int secondaryAttribute = userSettings.getSecondaryAttribute();
			boolean isAllowCustomDocuments = userSettings.isAllowCustomDocumentsInd();
			boolean isMobileSession = appSessBean.isMobileSession();
			boolean isKitTemplateMode = volatilesSssnBean.isKitTemplateMode();
			boolean isBatchVDPMode = volatilesSssnBean.isBatchVDPMode();
			boolean isWizard = searchCriteriaBean.isOrderWizard();

			catalogItemsResp.setShowOrderLinePrice(userSettings.isShowOrderLinePrice());//CAP-44716
			catalogItemsResp.setShowOrderLineAvailability(userSettings.isShowOrderLineAvailability());//CAP-44716
			catalogItemsResp.setPriceLineAvailabilityLbl(getVariantLabelValues(userSettings.isShowOrderLinePrice(),userSettings.isShowOrderLineAvailability(),appSessBean));//CAP-44716
				
			AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
			if (!oeSessionBean.isAllowUserFavorites() || asb.getProfileNumber() <= 0)
			{
				throw new AccessForbiddenException(this.getClass().getName());
			}
			
			
			OEItemSearchCriteriaSessionBean searchCriteria = null;
			searchCriteria=new GetOEItemSearchCriteriaSessionBean().getOEItemSearchCriteriaSessionBean(appSessBean);
			ICatalog cat = objectMapFactoryService.getComponentObjectMap().getObject(ICatalog.class, null);
			CatalogLineVO[] items = cat.searchCatalog(searchCriteria, isMobileSession, isKitTemplateMode, isAllowCustomDocuments, primaryAttribute,secondaryAttribute, isBatchVDPMode, isWizard);
			
			//CAP-38861
			List<ItemThumbnailCellData> catalogSearchResults = new ArrayList<>();
			try
			{
				catalogSearchResults =catalogItemRetriveServices.populateSearchResults(items, appSession, servletContext, oeSession, volatileSession, searchCriteria, false);
				catalogItemsResp.setItemThumbnailCellData(catalogSearchResults);
				catalogItemsResp.setDeliveryOptionsList(populateDeliveryOptionsList(userSettings, appSessBean)); // CAP-48938
				catalogItemsResp.setSuccess(true);
			}catch (AtWinXSException e) {
				logger.error("Exception when processing searchResults" + e.getMessage(), e);
			}    
			return catalogItemsResp;
			
	}
	
	//CAP-44716 - copied from catalog items retrieve service impl 
	public String getVariantLabelValues(boolean priceValue,boolean availabilityValue,AppSessionBean asb) {
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
}
