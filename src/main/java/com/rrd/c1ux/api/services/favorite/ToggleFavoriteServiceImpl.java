
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  08/09/23 	Satishkumar A		CAP-42720		C1UX API - API Build - Favorite Toggle Call
 */
package com.rrd.c1ux.api.services.favorite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.favorite.ToggleFavoriteResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.orders.savedorders.SavedOrderServiceImpl;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class ToggleFavoriteServiceImpl extends BaseService implements ToggleFavoriteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ToggleFavoriteServiceImpl.class);

	protected ToggleFavoriteServiceImpl(TranslationService translationService) {
		super(translationService);
	}

	public ToggleFavoriteResponse toggleFavorite(SessionContainer sc, String custItem, String vendorItem)
			throws AtWinXSException {

		ToggleFavoriteResponse response = new ToggleFavoriteResponse();

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			OEOrderSessionBean oeSession = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
			OECatalogAssembler asm = new OECatalogAssembler(appSessionBean.getCustomToken(),
					appSessionBean.getDefaultLocale());
			if (!validate(appSessionBean, oeSession, custItem, vendorItem, response))
				return response;
			
		try {
			boolean isFavorite = asm.setUnsetFavoriteItem(appSessionBean, custItem, vendorItem);
			response.setIsFavorite(isFavorite);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setIsFavorite(false);
			response.setSuccess(false);
		}
		return response;

	}

	public boolean validate(AppSessionBean appSessionBean, OEOrderSessionBean oeSession, String custItem,
			String vendorItem, ToggleFavoriteResponse response) throws AtWinXSException {

		if ((appSessionBean.getProfileNumber() <= 0) || (!oeSession.isAllowUserFavorites())) {

			LOGGER.error(getErrorPrefix(appSessionBean),
					"from ToggleFavoriteServiceImpl(), user should not have access to this process since they do not have a valid profile number or AllowUserFavorites is false.",
					-1);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}

		if (Util.isBlankOrNull(custItem) && Util.isBlankOrNull(vendorItem)) {
			response.setSuccess(false);
			response.setIsFavorite(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.NO_ITEM_SELECTED_ERR));
			return false;
		}
		return true;
	}
}
