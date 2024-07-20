package com.rrd.c1ux.api.services.factory;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;

@Service
public class OEAssemblerFactoryServiceImpl implements OEAssemblerFactoryService {
	
	@Override
	public OEShoppingCartAssembler getShoppingCartAssembler(CustomizationToken customToken, Locale locale, boolean applyExchangeRate) {
		return OEAssemblerFactory.getShoppingCartAssembler(customToken, locale, applyExchangeRate);
	}

	// CAP-42663
	@Override
	public OECheckoutAssembler getCheckoutAssembler(VolatileSessionBean volatileSessionBean,
			AppSessionBean appSessionBean) {
		return OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());
	}
}
