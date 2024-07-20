package com.rrd.c1ux.api.services.addtocart;

import com.rrd.c1ux.api.models.addtocart.ItemAddToCartRequest;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartResponse;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.gwt.common.exception.CPRPCRedirectException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface ItemAddToCartService {

	public ItemAddToCartResponse addItemToCart(SessionContainer sc, ItemAddToCartRequest itemAddToCartRequest) throws AtWinXSException, CPRPCRedirectException, CPRPCException;



}
