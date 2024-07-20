package com.rrd.c1ux.api.services.notiereditems;

import com.rrd.c1ux.api.models.notiereditems.PNANoTieredPriceRequest;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

public interface PNANoTieredServices {
	public PriceAndAvailabilityVO getPNANoTieredItemPricing(ApplicationSession appSession,ApplicationVolatileSession volatileSession,
			OrderEntrySession oeSession,PNANoTieredPriceRequest request)throws AtWinXSException;
}
