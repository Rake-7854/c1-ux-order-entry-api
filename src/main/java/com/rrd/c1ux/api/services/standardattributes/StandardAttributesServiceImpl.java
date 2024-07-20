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
 *  10/03/23	Satishkumar A	CAP-43282	C1UX BE - API Build - Get OE Item Filter Options - including favorites, featured types
 */
package com.rrd.c1ux.api.services.standardattributes;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.standardattributes.StandardAttributesResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.catalog.entity.FeaturedSearchCriteria;
import com.rrd.custompoint.gwt.catalog.entity.StandardAttributes;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class StandardAttributesServiceImpl extends BaseOEService implements StandardAttributesService{

	
	public StandardAttributesServiceImpl(TranslationService translationService,ObjectMapFactoryService objectMapFactoryService) {
	      super(translationService,objectMapFactoryService);
	    }
	
	@Override
	public StandardAttributesResponse getStandardAttributeList(SessionContainer sc) throws CPRPCException, AtWinXSException, IllegalAccessException, InvocationTargetException {
	
			ApplicationSession appSession = sc.getApplicationSession();
			AppSessionBean appSessionBean = appSession.getAppSessionBean();

			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			
			StandardAttributes result = null;
			StandardAttributesResponse response=new StandardAttributesResponse();
			//CP-10769 NMB  Removed unnecessary null checks.
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			if (oeSessionBean != null)
			{
				if (oeSessionBean.getSearchCriteriaBean().getStandardAttributes() != null)
				{
					return  getStandardAttributesFromSession(sc ,oeSessionBean ,oeSession,response);
					
				}
				else
				{
					CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean = null;
					
					catSearchFeatFaveBean = getCatSearchFeatFaveBean(sc, appSession, oeSession, catSearchFeatFaveBean);

					OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
					result = new StandardAttributes();

					if (catSearchFeatFaveBean != null)
					{

						setFavAndFeaToResultObject(appSessionBean, oeSessionBean, catSearchFeatFaveBean,  result );

					}

					if (userSettings != null && userSettings.isNewItemsFlag())
					{
							result.setShowNewItems(true);
							result.setNewItemsLabel(Util.nullToEmpty(userSettings.getNewItemsLabel()));
					}
				}
			}
			
			// CP-10769
			oeSession.getOESessionBean().getSearchCriteriaBean().setStandardAttributes(result);
			SessionHandler.persistServiceInSession(oeSession, sc.getApplicationSession().getAppSessionBean().getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
			BeanUtils.copyProperties(response, result);
			response.setSuccess(true);
			return response;
		}
	
	public StandardAttributesResponse  getStandardAttributesFromSession(SessionContainer sc, OEOrderSessionBean oeSessionBean, OrderEntrySession oeSession,  StandardAttributesResponse response) throws CPRPCException, AtWinXSException, IllegalAccessException, InvocationTargetException {
				
			StandardAttributes result = oeSessionBean.getSearchCriteriaBean().getStandardAttributes();

			// CAP-14294 - cannot do this or we wipe out new items check
			if ((oeSessionBean.getSearchCriteriaBean().isNewItemsOnly()) && !result.isFilterNewItems())
			{
				result.setFilterNewItems(true);
				SessionHandler.persistServiceInSession(oeSession, sc.getApplicationSession().getAppSessionBean().getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
			} 
			BeanUtils.copyProperties(response, result);

			response.setSuccess(true);
			return response;
	}
	
	public CatalogSearchFeaturesFavoritesBean  getCatSearchFeatFaveBean(SessionContainer sc, ApplicationSession appSession, OrderEntrySession oeSession, CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean) throws CPRPCException, AtWinXSException {
		
		OECatalogAssembler catalogAssembler = new OECatalogAssembler(appSession.getAppSessionBean().getCustomToken(), appSession.getAppSessionBean().getDefaultLocale());
		List<AlertCountResponseBean> quickFindAlert = null;
		try
		{
			quickFindAlert = catalogAssembler.getFavoritesFeaturedItemsCount(appSession.getAppSessionBean());
		} catch (AtWinXSException e)
		{
			throw Util.asCPRPCException(e);
		}

		if (quickFindAlert != null && !quickFindAlert.isEmpty())
		{
			catSearchFeatFaveBean = new CatalogSearchFeaturesFavoritesBean();
			for (AlertCountResponseBean resBean : quickFindAlert)
			{
				if (resBean.getAlertCategory().equals(AtWinXSConstant.QUICK_FIND_FAVORITE_ITEMS))
				{
					catSearchFeatFaveBean.setHasFavoriteItems(true);
				} else
				{
					catSearchFeatFaveBean.setFeaturedItemsDefined(resBean.getAlertCounts());
				}
			}

			oeSession.getOESessionBean().setCatalogSearchFeaturesFavoritesBean(catSearchFeatFaveBean);
			SessionHandler.persistServiceInSession(oeSession, sc.getApplicationSession().getAppSessionBean().getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
		}
		return catSearchFeatFaveBean;

	}
	public void setFavAndFeaToResultObject(AppSessionBean appSessionBean,OEOrderSessionBean oeSessionBean,CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean, StandardAttributes result ){
		if (oeSessionBean.isAllowUserFavorites() && catSearchFeatFaveBean.hasFavoriteItems())
		{
			result.setShowFavorites(true);
			//CAP-42171 SRN Translate Favorites as well
			try {
				result.setFavoritesLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "favoritesStandardAttrLbl"));
			} catch (AtWinXSException e) {
				result.setFavoritesLabel("Favorites");
			}
		}

		if (catSearchFeatFaveBean.getFeaturedItemsDefined() != null)
		{
			for (AlertCounts alertCnt : catSearchFeatFaveBean.getFeaturedItemsDefined())
			{
				if (!Util.isBlankOrNull(alertCnt.getAlertCd()) && catSearchFeatFaveBean.getFeaturedItemsSearch() != null)
				{
					FeaturedSearchCriteria criteria = new FeaturedSearchCriteria();
					criteria.setLabel(alertCnt.getAlertName());
					criteria.setTypeID(Util.safeStringToDefaultInt(alertCnt.getAlertCd(), AtWinXSConstant.INVALID_ID));

					result.getFeaturedSearchCriteria().add(criteria);
				}
			}
		}
	}
}
