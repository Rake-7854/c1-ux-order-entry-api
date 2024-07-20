/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#																				Description
 * 	--------	-----------		-------------------------------------------------------------------------------		--------------------------------
 *	04/20/22	Sakthi M		CAP-33762- Item Detail Pricing/Availability Call (Single Item no tiered pricing)	Initial creation
 */
package com.rrd.c1ux.api.services.notiereditems;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.notiereditems.PNANoTieredPriceRequest;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.IOEPricingAndAvailability;
import com.wallace.atwinxs.orderentry.locator.OEPricingAndAvailabilityComponentLocator;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

@Service
public class PNANoTieredServicesImpl implements PNANoTieredServices{

	/**
	 * 
	 * @param PNANoTieredPriceRequest
	 * @return PriceAndAvailabilityVO
	 * @throws AtWinXSException 
	 */
	public PriceAndAvailabilityVO getPNANoTieredItemPricing(ApplicationSession appSession,ApplicationVolatileSession volatileSession,
			OrderEntrySession oeSession,PNANoTieredPriceRequest request)
			throws AtWinXSException {
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
	
		IOEPricingAndAvailability pna = OEPricingAndAvailabilityComponentLocator.locate(appSessionBean.getCustomToken());
		PriceAndAvailabilityVO paVO=pna.getPriceAndAvailability(request.isShowPrice(),request.isShowAvailability(),request.getItemNumber(),
				request.getOrderType(),request.getOrderQtyEA(),request.getCorporateNumber(), request.getSoldToNumber(),
				request.isCheckJobs(), request.isUseCSSListPrice(),request.isUseJLJLSPrice(), request.isUseCustomersCustomerPrice(), 
				request.isUseCatalogPrice(), request.getSiteID(),request.getCustomerItemNumber(),request.getLastJLMarkupPct(),
				request.getPromoCode(), request.getRounding(), request.isUseTPP(),request.getTppClass(), request.isComponent());
		return paVO;
		
	}
}
