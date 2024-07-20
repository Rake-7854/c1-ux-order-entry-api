package com.rrd.c1ux.api.services.routing;

import com.rrd.c1ux.api.models.routing.RoutingInformationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface RoutingInformationService {

	public RoutingInformationResponse getRoutingInformation(SessionContainer sc) throws AtWinXSException;
}
