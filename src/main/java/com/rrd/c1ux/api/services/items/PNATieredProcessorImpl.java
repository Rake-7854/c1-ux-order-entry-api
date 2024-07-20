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
 *  04/19/22    S Ramachandran  CAP-33763   Initial Creation, added to get PNATiered Item
 *  06/03/24	C Codina		CAP-38842	C1UX BE - Unused API Standardization - Get Tiered PNA conversion to standards
 */

package com.rrd.c1ux.api.services.items;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.items.PNATieredPriceRequest;
import com.rrd.c1ux.api.models.items.PNATieredPriceResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OEPricingAndAvailabilityComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.IOEPricingAndAvailability;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.TierPriceRequest;
import com.wallace.atwinxs.orderentry.vo.TieredPriceVO;

@Service
public class PNATieredProcessorImpl extends BaseOEService implements PNATieredProcessor {

	private static final Logger logger = LoggerFactory.getLogger(PNATieredProcessorImpl.class);
	
	private final OEPricingAndAvailabilityComponentLocatorService oePricingAndAvailabilityComponentLocatorService;
	
	protected PNATieredProcessorImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			OEPricingAndAvailabilityComponentLocatorService oePricingAndAvailabilityComponentLocatorService) {
		super(translationService, objectMapFactoryService);
		this.oePricingAndAvailabilityComponentLocatorService = oePricingAndAvailabilityComponentLocatorService;
	}
	
	@Override
	public PNATieredPriceResponse processPNATierPrice(PNATieredPriceRequest request, SessionContainer sc) 
			throws AtWinXSException {
		
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		PNATieredPriceResponse response = new PNATieredPriceResponse();
		boolean hasOrdersService = appSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID);
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		
		if (!hasOrdersService || !userSettings.isShowOrderLinePrice()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		String corporateNumber = request.getCorpNum();
		String itemNumber = request.getWcssItemNum();
		String orderType = request.getOrderType();
		boolean useCSSListPrice = request.isUseCSSListPrice();
		boolean useJLJLSPrice = request.isUseJLJLSPrice();
		boolean useCustomersCustomerPrice = request.isUseCustomersCustomerPrice();
		boolean useCatalogPrice=request.isUseCatalogPrice();
		int siteID=request.getSiteId();
		String priceClass=request.getPriceClass();

		TierPriceRequest tierPrcReq = new TierPriceRequest();
		tierPrcReq.setCorpNum(corporateNumber);
		tierPrcReq.setSoldToNumber("");
		tierPrcReq.setWcssItemNum(itemNumber);
		tierPrcReq.setOrderQtyEA(1);
		tierPrcReq.setOrderType(orderType);
		tierPrcReq.setSkipNSTPrice(true);
		tierPrcReq.setCheckJobs(true);
		tierPrcReq.setUseCSSListPrice(useCSSListPrice);
		tierPrcReq.setUseJLJLSPrice(useJLJLSPrice);
		tierPrcReq.setUseCustomersCustomerPrice(useCustomersCustomerPrice);
		tierPrcReq.setUseCatalogPrice(useCatalogPrice);
		tierPrcReq.setSiteId(siteID);
		tierPrcReq.setPriceClass(priceClass);
		
		try {

			IOEPricingAndAvailability pricingAndAvail = oePricingAndAvailabilityComponentLocatorService.locate(appSessionBean.getCustomToken());
			@SuppressWarnings("rawtypes")
			TieredPriceVO[] vo = pricingAndAvail.getTieredPricing(tierPrcReq,new ArrayList());

			if(null != vo) {
				response.setTieredPrice(vo);
				response.setSuccess(true);
			}
			else {
				response.setMessage(SFTranslationTextConstants.TIERED_PRICING_ERR);
			}

		} catch(Exception e) {
			logger.error("Failed to retrieve Tiered pricing" + e.getMessage(), e);
			response.setMessage(SFTranslationTextConstants.TIERED_PRICING_ERR);
		}

		return response;
	}

}