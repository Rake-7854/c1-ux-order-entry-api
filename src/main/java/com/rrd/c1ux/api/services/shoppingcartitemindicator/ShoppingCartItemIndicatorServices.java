package com.rrd.c1ux.api.services.shoppingcartitemindicator;

import com.rrd.c1ux.api.models.items.ShoppingCartItemIndicatorResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface ShoppingCartItemIndicatorServices {
		public ShoppingCartItemIndicatorResponse getItemCount(SessionContainer sc) throws AtWinXSException;
}
