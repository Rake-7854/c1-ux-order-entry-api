package com.rrd.c1ux.api.services.factory;

import java.util.Locale;

import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;

public interface OEAssemblerFactoryService {
	
	OEShoppingCartAssembler getShoppingCartAssembler(CustomizationToken customToken, Locale locale, boolean applyExchangeRate);

	// CAP-42663
	OECheckoutAssembler getCheckoutAssembler(VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean);

}
